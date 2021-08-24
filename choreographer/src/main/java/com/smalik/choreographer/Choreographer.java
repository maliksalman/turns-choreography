package com.smalik.choreographer;

import com.smalik.choreographer.api.Move;
import com.smalik.choreographer.api.TurnRequest;
import com.smalik.choreographer.api.TurnResponse;
import com.smalik.choreographer.messaging.MoveCompleted;
import com.smalik.choreographer.messaging.MoveStepRequest;
import com.smalik.choreographer.messaging.TurnCompleted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class Choreographer {

    private TurnsService service;
    private TurnsDatabase database;
    private StreamBridge streamBridge;

    @Autowired
    public void setStreamBridge(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Autowired
    public void setService(TurnsService service) {
        this.service = service;
    }

    @Autowired
    public void setDatabase(TurnsDatabase database) {
        this.database = database;
    }

    public void turnNow(TurnRequest request) {

        // save the request
        database.addInProgress(request);

        // process the first move's first step
        TurnRequest.MoveRequest mr = request.getMoves().get(0);
        startProcessingMove(request, mr);
    }

    public TurnResponse turnTimedOut(TurnRequest request) {
        return generateTurnResponse(request, true);
    }

    private void startProcessingMove(TurnRequest request, TurnRequest.MoveRequest mr) {
        Move move = toMoveFromRequest(request, mr, Move.Status.REQUESTED);

        // save the move
        database.addMove(move);

        // send out the first message here
        sendMoveStepRequestedEvent(Move.STEPS.get(0), move);
    }

    private Move toMoveFromRequest(TurnRequest request, TurnRequest.MoveRequest mr, Move.Status initialMoveStatus) {
        return Move.builder()
                .turnId(request.getTurnId())
                .playerId(request.getPlayerId())
                .moveId(mr.getMoveId())
                .type(mr.getType())
                .quantity(mr.getPlaces())
                .status(initialMoveStatus)
                .statuses(Move.STEPS.stream()
                        .map(name -> Move.StepStatus.builder()
                                .step(name)
                                .status(Move.Status.NONE)
                                .build())
                        .collect(Collectors.toMap(s -> s.getStep(), s -> s)))
                .build();
    }

    public void handleMoveStepCompleted(String turnId, String moveId, String step, boolean failed) {
        Move move = database.findMove(moveId);
        if (move != null) {
            log.info("Handling move step completed: Turn={}, Move={}, Step={}", turnId, moveId, step);
            Move.StepStatus stepStatus = move.getStatuses().get(step);
            if (stepStatus != null) {
                stepStatus.setStatus(Move.Status.DONE);
                stepStatus.setFailed(failed);
                stepStatus.setFinishTime(OffsetDateTime.now());
            }

            boolean moveDone = move.getStatuses().values().stream()
                    .allMatch(s -> s.getStatus() == Move.Status.DONE);

            if (!moveDone) {
                // find the next step in this move and request it
                String nextStep = Move.STEPS.get(Move.STEPS.indexOf(step) + 1);
                sendMoveStepRequestedEvent(nextStep, move);
            } else {
                sendMoveCompletedEvent(move);
            }
        }
    }

    public void handleMoveCompleted(String turnId, String moveId) {

        Move move = database.findMove(moveId);
        if (move != null) {
            log.info("Handling move completed: Turn={}, Move={}", turnId, moveId);
            move.setStatus(Move.Status.DONE);

            // find the next move in the turn
            TurnRequest request = database.findInProgressRequest(turnId);
            TurnRequest.MoveRequest nextMoveRequest = null;
            for (int i = 0; i < request.getMoves().size(); i++) {
                TurnRequest.MoveRequest mr = request.getMoves().get(i);
                if (mr.getMoveId().equals(moveId) && (i+1) < request.getMoves().size()) {
                    nextMoveRequest = request.getMoves().get(i+1);
                    break;
                }
            }

            if (nextMoveRequest != null) {
                // if next move found - start processing it
                startProcessingMove(request, nextMoveRequest);

            } else {
                // if this was last move, turn is complete
                TurnResponse turnResponse = generateTurnResponse(request, false);
                service.registerResponse(turnResponse);
            }
        }
    }

    private TurnResponse generateTurnResponse(TurnRequest request, boolean timeout) {
        TurnResponse response = TurnResponse.builder()
                .playerId(request.getPlayerId())
                .turnId(request.getTurnId())
                .startTime(request.getTime())
                .finishTime(OffsetDateTime.now())
                .timeout(timeout)
                .moves(request.getMoves().stream()
                        .map(mr -> Optional
                                .ofNullable(database.findMove(mr.getMoveId()))
                                .orElse(toMoveFromRequest(request, mr, Move.Status.NONE)))
                        .collect(Collectors.toList()))
                .build();

        // clean up database
        database.cleanup(request);

        // lets everybody know turn is complete
        sendTurnCompletedEvent(request.getTurnId(), request.getPlayerId());

        return response;
    }

    private void sendTurnCompletedEvent(String turnId, String playerId) {
        streamBridge.send("turn-completed", TurnCompleted.builder()
                .turnId(turnId)
                .playerId(playerId)
                .build());
    }

    private void sendMoveCompletedEvent(Move move) {
        streamBridge.send("move-completed", MoveCompleted.builder()
                .turnId(move.getTurnId())
                .moveId(move.getMoveId())
                .build());
    }

    private void sendMoveStepRequestedEvent(String step, Move move) {

        move.getStatuses().get(step).setStartTime(OffsetDateTime.now());
        move.getStatuses().get(step).setStatus(Move.Status.REQUESTED);

        streamBridge.send(step + "-requested", MoveStepRequest.builder()
                .moveId(move.getMoveId())
                .playerId(move.getPlayerId())
                .turnId(move.getTurnId())
                .step(step)
                .build());
    }


    public void turnLater(TurnRequest request) {
        // TODO: add turn to a pending list - organize by player-id
    }

    public void handleTurnCompleted(String turnId, String playerId) {
        // TODO: see if there are pending turns for this player - run them
        log.info("Handling turn completed: Turn={}, Player={}", turnId, playerId);
    }
}
