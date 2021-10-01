package com.smalik.choreographer;

import com.smalik.choreographer.api.Move;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class ChoreographyListener {

    private final TurnChoreographer turnChoreographer;

    @Bean
    public Consumer<Flux<Move>> moveCompleted() {
        return flux -> flux
                .publishOn(Schedulers.boundedElastic(), 1)
                .doOnNext(move -> turnChoreographer.handleMoveCompleted(move))
                .subscribe();
    }

    @Bean
    public Consumer<Flux<TurnCompleted>> turnCompleted() {
        return flux -> flux
                .publishOn(Schedulers.boundedElastic(), 1)
                .doOnNext(event -> turnChoreographer.handleTurnCompleted(event.getTurnId(), event.getPlayerId(), event.isTimeout()))
                .subscribe();
    }
}
