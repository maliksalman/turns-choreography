package com.smalik.choreographer;

import com.smalik.choreographer.api.Move;
import com.smalik.choreographer.api.TurnRequest;
import com.smalik.choreographer.api.TurnResponse;
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
    private final TurnResponseSink sink;

    public void turnNow(TurnRequest request) {

        // save the request
        database.addInProgress(request);

        // process the first move's first step
        TurnRequest.MoveRequest mr = request.getMoves().get(0);
        sendMoveRequestedEvent(request, mr);
    }

    public TurnResponse turnTimedOut(TurnRequest request) {
        return generateTurnResponse(request, true);
    }

    public void handleMoveCompleted(Move move) {
        log.info("Handling move completed: Turn={}, Move={}", move.getTurnId(), move.getMoveId());
        database.addUpdateMove(move);

        // find the next move in the turn
        database.findInProgressRequest(move.getTurnId())
                .map(request -> {
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
        database.cleanup(request);

        // let everybody know turn is complete
        sendTurnCompletedEvent(request.getTurnId(), request.getPlayerId(), timeout);

        return response;
    }

    private void sendTurnCompletedEvent(String turnId, String playerId, boolean timeout) {
        streamBridge.send("turnCompleted-out-0", TurnCompleted.builder()
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
                .status(initialMoveStatus)
                .build();
    }

    private void sendMoveRequestedEvent(TurnRequest request, TurnRequest.MoveRequest moveRequest) {
        Move move = toMoveFromRequest(request, moveRequest, Move.Status.REQUESTED);
        database.addUpdateMove(move);
        streamBridge.send("moveRequested-out-0", move);
    }

    public void turnLater(TurnRequest request) {
        // TODO: add turn to a pending list - organize by player-id
    }

    public void handleTurnCompleted(String turnId, String playerId, boolean timeout) {
        log.info("Handling turn completed: Turn={}, Player={}, Timeout={}", turnId, playerId, timeout);
    }
}