package com.smalik.choreographer;

import com.smalik.choreographer.api.Move;
import com.smalik.choreographer.api.TurnRequest;
import com.smalik.choreographer.db.TurnsInMemoryDatabase;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MoveCompletedListener {

    private final MoveInitiator moveInitiator;
    private final TurnResponseGenerator responseGenerator;
    private final TurnsInMemoryDatabase memory;
    private final Metrics metrics;

    private Timer moveCompletedTimer;
    private Timer moveResponseTimer;
    private Timer moveResponseInflightTimer;

    @PostConstruct
    public void init() {
        moveCompletedTimer = metrics.createTimer("turns.move-completed");
        moveResponseTimer = metrics.createTimer("turns.move-response");
        moveResponseInflightTimer = metrics.createTimer("turns.move-response-inflight");
    }

    @Bean
    public Consumer<Flux<Move>> moveCompleted() {
        return flux -> flux
                .parallel()
                .runOn(Schedulers.newBoundedElastic(Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE, Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE, "move"))
                .doOnNext(move -> handleMoveCompleted(move))
                .subscribe();
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
                            moveInitiator.initiateMove(request, nextMoveRequest);
                        } else {
                            // if this was last move, turn is complete
                            responseGenerator.generateTurnResponse(moveInitiator, request, false);
                        }

                        return request;
                    });
        });
    }
}
