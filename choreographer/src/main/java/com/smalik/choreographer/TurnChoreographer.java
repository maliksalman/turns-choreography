package com.smalik.choreographer;

import com.smalik.choreographer.api.Move;
import com.smalik.choreographer.api.TurnRequest;
import com.smalik.choreographer.api.TurnResponse;
import com.smalik.choreographer.messaging.TurnCompleted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TurnChoreographer {

    private final TurnsDatabase database;
    private final StreamBridge streamBridge;
    private final MoveChoreographer moveChoreographer;
    private final TurnResponseSink sink;

    public void turnNow(TurnRequest request) {

        // save the request
        database.addInProgress(request);

        // process the first move's first step
        TurnRequest.MoveRequest mr = request.getMoves().get(0);
        moveChoreographer.startProcessingMove(request, mr);
    }

    public TurnResponse turnTimedOut(TurnRequest request) {
        return generateTurnResponse(request, true);
    }

    public void handleMoveCompleted(String turnId, String moveId) {
        database.findMove(moveId)
                .map(move -> {
                    log.info("Handling move completed: Turn={}, Move={}", turnId, moveId);
                    move.setStatus(Move.Status.DONE);

                    // find the next move in the turn
                    database.findInProgressRequest(turnId)
                            .map(request -> {
                                TurnRequest.MoveRequest nextMoveRequest = null;
                                for (int i = 0; i < request.getMoves().size(); i++) {
                                    TurnRequest.MoveRequest mr = request.getMoves().get(i);
                                    if (mr.getMoveId().equals(moveId) && (i + 1) < request.getMoves().size()) {
                                        nextMoveRequest = request.getMoves().get(i + 1);
                                        break;
                                    }
                                }

                                if (nextMoveRequest != null) {
                                    // if next move found - start processing it
                                    moveChoreographer.startProcessingMove(request, nextMoveRequest);
                                } else {
                                    // if this was last move, turn is complete
                                    generateTurnResponse(request, false);
                                }

                                return request;
                            });
                    return move;
                });
    }

    private TurnResponse generateTurnResponse(TurnRequest request, boolean timeout) {
        TurnResponse response = TurnResponse.builder()
                .playerId(request.getPlayerId())
                .turnId(request.getTurnId())
                .startTime(request.getTime())
                .finishTime(OffsetDateTime.now())
                .timeout(timeout)
                .moves(request.getMoves().stream()
                        .map(mr -> database
                                .findMove(mr.getMoveId())
                                .orElse(moveChoreographer.toMoveFromRequest(request, mr, Move.Status.NONE)))
                        .collect(Collectors.toList()))
                .build();

        // notify the sink
        if (timeout) {
            sink.registerTimeout(request.getTurnId());
        } else {
            sink.registerResponse(response);
        }

        // clean up database
        database.cleanup(request);

        // let everybody know turn is complete
        sendTurnCompletedEvent(request.getTurnId(), request.getPlayerId(), timeout);

        return response;
    }

    private void sendTurnCompletedEvent(String turnId, String playerId, boolean timeout) {
        streamBridge.send("turn-completed", TurnCompleted.builder()
                .turnId(turnId)
                .playerId(playerId)
                .timeout(timeout)
                .build());
    }

    public void turnLater(TurnRequest request) {
        // TODO: add turn to a pending list - organize by player-id
    }

    public void handleTurnCompleted(String turnId, String playerId, boolean timeout) {
        log.info("Handling turn completed: Turn={}, Player={}, Timeout={}", turnId, playerId, timeout);
    }
}
