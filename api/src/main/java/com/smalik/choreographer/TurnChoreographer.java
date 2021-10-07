package com.smalik.choreographer;

import com.smalik.choreographer.api.Move;
import com.smalik.choreographer.api.TurnRequest;
import com.smalik.choreographer.api.TurnResponse;
import com.smalik.choreographer.db.PlayerLockService;
import com.smalik.choreographer.db.RequestsDatabase;
import com.smalik.choreographer.db.TurnRequestInfo;
import com.smalik.choreographer.db.TurnsInMemoryDatabase;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TurnChoreographer {

    private final TurnsInMemoryDatabase memory;
    private final StreamBridge streamBridge;
    private final TurnResponseSink sink;
    private final PlayerLockService lockService;
    private final RequestsDatabase requests;
    private final Metrics metrics;

    private Timer moveCompletedTimer;
    private Timer turnCompletedTimer;
    private Timer generateResponseTimer;
    private Timer processTimer;
    private Timer moveResponseTimer;
    private Timer moveResponseInflightTimer;

    @PostConstruct
    public void init() {
        moveCompletedTimer = metrics.createTimer("turns.move-completed");
        turnCompletedTimer = metrics.createTimer("turns.turn-completed");
        generateResponseTimer = metrics.createTimer("turns.generate-response");
        processTimer = metrics.createTimer("turns.process");
        moveResponseTimer = metrics.createTimer("turns.move-response");
        moveResponseInflightTimer = metrics.createTimer("turns.move-response-inflight");
    }

    public void process(TurnRequest request) {
        processTimer.record(() -> {
            // save the request
            memory.addTurnRequest(request);
            requests.addRequest(TurnRequestInfo.builder()
                    .playerId(request.getPlayerId())
                    .turnId(request.getTurnId())
                    .time(request.getTime())
                    .build());

            if (lockService.lock(request.getPlayerId())) {
                log.info("Processing turn: Player={}, Turn={}", request.getPlayerId(), request.getTurnId());
                // process the first move's first step
                TurnRequest.MoveRequest mr = request.getMoves().get(0);
                sendMoveRequestedEvent(request, mr);
            } else {
                log.info("Waiting: Player={}, Turn={}", request.getPlayerId(), request.getTurnId());
                memory.addWaiting(request);
            }
        });
    }

    public TurnResponse processTimedOut(TurnRequest request) {
        return generateTurnResponse(request, true);
    }

    public void handleMoveCompleted(Move move) {
        moveCompletedTimer.record(() -> {

            // find the next move in the turn
            memory.findTurnRequest(move.getTurnId())
                    .map(request -> {

                        log.info("Handling move completed: Turn={}, Move={}", move.getTurnId(), move.getMoveId());
                        moveResponseTimer.record(Duration.between(move.getStatus().getStartTime(), move.getStatus().getFinishTime()));
                        moveResponseInflightTimer.record(Duration.between(move.getStatus().getFinishTime(), OffsetDateTime.now()));
                        memory.addUpdateMove(move);

                        TurnRequest.MoveRequest nextMoveRequest = null;
                        for (int i = 0; i < request.getMoves().size(); i++) {
                            TurnRequest.MoveRequest mr = request.getMoves().get(i);
                            if (mr.getMoveId().equals(move.getMoveId()) && (i + 1) < request.getMoves().size()) {
                                nextMoveRequest = request.getMoves().get(i + 1);
                                break;
                            }
                        }

                        if (nextMoveRequest != null) {
                            // if next move found - start processing it
                            sendMoveRequestedEvent(request, nextMoveRequest);
                        } else {
                            // if this was last move, turn is complete
                            generateTurnResponse(request, false);
                        }

                        return request;
                    });
        });
    }

    private TurnResponse generateTurnResponse(TurnRequest request, boolean timeout) {
        return generateResponseTimer.record(() -> {

            TurnResponse response = TurnResponse.builder()
                    .playerId(request.getPlayerId())
                    .turnId(request.getTurnId())
                    .startTime(request.getTime())
                    .finishTime(OffsetDateTime.now())
                    .timeout(timeout)
                    .moves(request.getMoves().stream()
                            .map(mr -> memory
                                    .findMove(mr.getMoveId())
                                    .orElse(toMoveFromRequest(request, mr, Move.Status.NONE)))
                            .collect(Collectors.toList()))
                    .build();

            // notify the sink
            if (timeout) {
                sink.registerTimeout(request.getTurnId());
            } else {
                sink.registerResponse(response);
            }

            // clean up database
            memory.cleanup(request);
            requests.removeRequest(request.getTurnId());

            // only release lock if no other requests are waiting for this player
            if (requests.findRequests(request.getPlayerId()).isEmpty()) {
                lockService.unlock(request.getPlayerId());
            }

            // let everybody know turn is complete
            sendTurnCompletedEvent(request.getTurnId(), request.getPlayerId(), timeout);

            return response;
        });
    }

    private void sendTurnCompletedEvent(String turnId, String playerId, boolean timeout) {
        streamBridge.send("turn-completed", TurnCompleted.builder()
                .turnId(turnId)
                .playerId(playerId)
                .timeout(timeout)
                .build());
    }

    public Move toMoveFromRequest(TurnRequest request, TurnRequest.MoveRequest mr, Move.Status initialMoveStatus) {
        return Move.builder()
                .turnId(request.getTurnId())
                .playerId(request.getPlayerId())
                .moveId(mr.getMoveId())
                .type(mr.getType())
                .quantity(mr.getPlaces())
                .status(Move.MoveStatus.builder()
                        .status(initialMoveStatus)
                        .build())
                .build();
    }

    private void sendMoveRequestedEvent(TurnRequest request, TurnRequest.MoveRequest moveRequest) {
        Move move = toMoveFromRequest(request, moveRequest, Move.Status.REQUESTED);
        move.getStatus().setStartTime(OffsetDateTime.now());

        memory.addUpdateMove(move);
        streamBridge.send("move-requested", move);
    }

    public void handleTurnCompleted(String turnId, String playerId, boolean timeout) {
        turnCompletedTimer.record(() -> {

            log.info("Handling turn completed: Turn={}, Player={}, Timeout={}", turnId, playerId, timeout);
            if (!timeout) {
                requests.findRequests(playerId).stream()
                        .findFirst()
                        .map(req -> req.getTurnId())
                        .ifPresent(nextTurnId -> {
                            if (memory.isNextWaitingRequest(playerId, nextTurnId)) {
                                memory.findTurnRequest(nextTurnId)
                                        .ifPresentOrElse(request -> {
                                            memory.removeWaiting(request);
                                            log.info("Processing turn: Player={}, Turn={}", request.getPlayerId(), request.getTurnId());
                                            TurnRequest.MoveRequest mr = request.getMoves().get(0);
                                            sendMoveRequestedEvent(request, mr);
                                        }, () -> {
                                            log.warn("cant find request even though should have it: Player={}, Turn={}", playerId, nextTurnId);
                                        });

                            }
                        });
            }
        });
    }
}
