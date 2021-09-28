package com.smalik.choreographer;

import com.smalik.choreographer.api.Move;
import com.smalik.choreographer.api.TurnRequest;
import com.smalik.choreographer.messaging.MoveCompleted;
import com.smalik.choreographer.messaging.MoveStepRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MoveChoreographer {

    private final TurnsDatabase database;
    private final StreamBridge streamBridge;

    public void handleMoveStepCompleted(String turnId, String moveId, String step, boolean failed) {
        database.findMove(moveId)
                .map(move -> {
                    Move.StepStatus stepStatus = move.getStatuses().get(step);
                    stepStatus.setStatus(Move.Status.DONE);
                    stepStatus.setFailed(failed);
                    stepStatus.setFinishTime(OffsetDateTime.now());
                    log.info("Handling move step completed: Turn={}, Move={}, Step={}, Time={}",
                            turnId,
                            moveId,
                            step,
                            Duration.between(stepStatus.getStartTime(), stepStatus.getFinishTime()).toMillis());

                    boolean moveDone = move.getStatuses().values().stream()
                            .allMatch(s -> s.getStatus() == Move.Status.DONE);

                    if (!moveDone) {
                        // find the next step in this move and request it
                        String nextStep = Move.STEPS.get(Move.STEPS.indexOf(step) + 1);
                        sendMoveStepRequestedEvent(nextStep, move);
                    } else {
                        sendMoveCompletedEvent(move);
                    }
                    return move;
                });
    }

    private void sendMoveCompletedEvent(Move move) {
        streamBridge.send("move-completed", MoveCompleted.builder()
                .turnId(move.getTurnId())
                .moveId(move.getMoveId())
                .build());
    }

    public void startProcessingMove(TurnRequest request, TurnRequest.MoveRequest mr) {
        Move move = toMoveFromRequest(request, mr, Move.Status.REQUESTED);

        // save the move
        database.addMove(move);

        // send out the first message here
        sendMoveStepRequestedEvent(Move.STEPS.get(0), move);
    }

    public Move toMoveFromRequest(TurnRequest request, TurnRequest.MoveRequest mr, Move.Status initialMoveStatus) {
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

    private void sendMoveStepRequestedEvent(String step, Move move) {

        OffsetDateTime now = OffsetDateTime.now();
        move.getStatuses().get(step).setStartTime(now);
        move.getStatuses().get(step).setStatus(Move.Status.REQUESTED);

        streamBridge.send(step + "-requested-out-0", MoveStepRequest.builder()
                .moveId(move.getMoveId())
                .playerId(move.getPlayerId())
                .turnId(move.getTurnId())
                .step(step)
                .time(now)
                .build());
    }
}
