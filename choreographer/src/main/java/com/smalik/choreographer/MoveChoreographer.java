package com.smalik.choreographer;

import com.smalik.choreographer.db.MovesDatabase;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MoveChoreographer {

    private final MovesDatabase database;
    private final StreamBridge streamBridge;
    private final Metrics metrics;

    private Timer findMoveTimer;
    private Timer saveMoveTimer;
    private Timer removeMoveTimer;
    private Map<String, Timer> stepTimers;

    @PostConstruct
    public void init() {
        findMoveTimer = metrics.createTimer("choreographer.moves.find");
        saveMoveTimer = metrics.createTimer("choreographer.moves.save");
        removeMoveTimer = metrics.createTimer("choreographer.moves.remove");

        stepTimers = new HashMap<>();
        for (String step : Move.STEPS) {
            stepTimers.put(step, metrics.createTimer("choreographer.steps." + step));
        }
    }

    public void handleMoveStepCompleted(String turnId, String moveId, String step, boolean failed) {
        Instant instant = Instant.now();
        database.findOne(moveId)
                .map(move -> {
                    OffsetDateTime now = OffsetDateTime.now();
                    findMoveTimer.record(Duration.between(instant, now.toInstant()));

                    Move.StepStatus stepStatus = move.getStatuses().get(step);
                    stepStatus.setStatus(Move.Status.DONE);
                    stepStatus.setFailed(failed);
                    stepStatus.setFinishTime(now);

                    Duration stepTime = Duration.between(stepStatus.getStartTime(), stepStatus.getFinishTime());
                    stepTimers.get(step).record(stepTime);
                    log.info("Handling move step completed: Turn={}, Move={}, Step={}, Time={}",
                            turnId,
                            moveId,
                            step,
                            stepTime.toMillis());

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

        // cleanup the move from the database, since we are done with it
        removeMoveTimer.record(() -> database.remove(move.moveId));
    }

    public void startProcessingMove(Move move) {
        // save the move
        move.setStatuses(Move.STEPS.stream()
                .map(name -> Move.StepStatus.builder()
                        .step(name)
                        .status(Move.Status.NONE)
                        .build())
                .collect(Collectors.toMap(s -> s.getStep(), s -> s)));

        // send out the first message here
         sendMoveStepRequestedEvent(Move.STEPS.get(0), move);
    }

    private void sendMoveStepRequestedEvent(String step, Move move) {

        OffsetDateTime now = OffsetDateTime.now();
        move.getStatuses().get(step).setStartTime(now);
        move.getStatuses().get(step).setStatus(Move.Status.REQUESTED);

        // save the current view of the move in the database
        saveMoveTimer.record(() -> database.save(move));

        // send the message for requested the step
        streamBridge.send(step + "-requested-out-0", MoveStepRequest.builder()
                .moveId(move.getMoveId())
                .playerId(move.getPlayerId())
                .turnId(move.getTurnId())
                .step(step)
                .time(now)
                .build());
    }
}
