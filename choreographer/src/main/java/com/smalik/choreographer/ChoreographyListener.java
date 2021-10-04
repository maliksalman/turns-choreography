package com.smalik.choreographer;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class ChoreographyListener {

    private final MoveChoreographer moveChoreographer;

    @Bean
    public Consumer<Flux<Move>> move() {
        return flux -> flux
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(move -> moveChoreographer.startProcessingMove(move))
                .subscribe();
    }


    @Bean
    public Consumer<Flux<MoveStepResponse>> breathe() {
        return flux -> flux
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(resp -> moveChoreographer.handleMoveStepCompleted(resp.getTurnId(), resp.getMoveId(), "breathe", resp.isFailed()))
                .subscribe();
    }

    @Bean
    public Consumer<Flux<MoveStepResponse>> think() {
        return flux -> flux
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(resp -> moveChoreographer.handleMoveStepCompleted(resp.getTurnId(), resp.getMoveId(), "think", resp.isFailed()))
                .subscribe();
    }

    @Bean
    public Consumer<Flux<MoveStepResponse>> act() {
        return flux -> flux
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(resp -> moveChoreographer.handleMoveStepCompleted(resp.getTurnId(), resp.getMoveId(), "act", resp.isFailed()))
                .subscribe();
    }

    @Bean
    public Consumer<Flux<MoveStepResponse>> react() {
        return flux -> flux
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(resp -> moveChoreographer.handleMoveStepCompleted(resp.getTurnId(), resp.getMoveId(), "react", resp.isFailed()))
                .subscribe();
    }
}
