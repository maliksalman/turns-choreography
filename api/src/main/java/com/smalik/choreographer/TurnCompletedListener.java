package com.smalik.choreographer;

import com.smalik.choreographer.api.TurnRequest;
import com.smalik.choreographer.db.RequestsDatabase;
import com.smalik.choreographer.db.TurnsInMemoryDatabase;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TurnCompletedListener {

    private final MoveInitiator moveInitiator;
    private final TurnsInMemoryDatabase memory;
    private final RequestsDatabase requests;
    private final Metrics metrics;

    private Timer turnCompletedTimer;

    @PostConstruct
    public void init() {
        turnCompletedTimer = metrics.createTimer("turns.turn-completed");
    }

    @Bean
    public Consumer<Flux<TurnCompleted>> turnCompleted() {
        return flux -> flux
                .publishOn(Schedulers.boundedElastic(), 1)
                .doOnNext(event -> handleTurnCompleted(event))
                .subscribe();
    }

    public void handleTurnCompleted(TurnCompleted event) {
        turnCompletedTimer.record(() -> {

            log.info("Handling turn completed: Turn={}, Player={}, Timeout={}", event.getTurnId(), event.getPlayerId(), event.isTimeout());
            if (!event.isTimeout()) {
                requests.findRequests(event.getPlayerId()).stream()
                        .findFirst()
                        .map(req -> req.getTurnId())
                        .ifPresent(nextTurnId -> {
                            if (memory.isNextWaitingRequest(event.getPlayerId(), nextTurnId)) {
                                memory.findTurnRequest(nextTurnId)
                                        .ifPresentOrElse(request -> {
                                            memory.removeWaiting(request);
                                            log.info("Processing turn: Player={}, Turn={}", request.getPlayerId(), request.getTurnId());
                                            TurnRequest.MoveRequest mr = request.getMoves().get(0);
                                            moveInitiator.initiateMove(request, mr);
                                        }, () -> {
                                            log.warn("cant find request even though should have it: Player={}, Turn={}", event.getPlayerId(), nextTurnId);
                                        });

                            }
                        });
            }
        });
    }
}
