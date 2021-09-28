package com.smalik.choreographer;

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

    private final MovesDatabase database;
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
                        move.setStatus(Move.Status.DONE);
                        sendMoveCompletedEvent(move);
                    }
                    return move;
                });
    }

    private void sendMoveCompletedEvent(Move move) {
        streamBridge.send("move-completed-out-0", move);
        database.removeMove(move.moveId);
    }

    public void startProcessingMove(Move move) {
        // save the move
        move.setStatuses(Move.STEPS.stream()
                .map(name -> Move.StepStatus.builder()
                        .step(name)
                        .status(Move.Status.NONE)
                        .build())
                .collect(Collectors.toMap(s -> s.getStep(), s -> s)));
        database.addMove(move);

        // send out the first message here
         sendMoveStepRequestedEvent(Move.STEPS.get(0), move);
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
