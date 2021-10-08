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
                .parallel()
                .runOn(Schedulers.newBoundedElastic(Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE, Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE, "move"))
                .doOnNext(move -> moveChoreographer.startProcessingMove(move))
                .subscribe();
    }

    @Bean
    public Consumer<Flux<MoveStepResponse>> forward() {
        return moveStepCompleted("forward");
    }

    @Bean
    public Consumer<Flux<MoveStepResponse>> back() {
        return moveStepCompleted("back");
    }

    @Bean
    public Consumer<Flux<MoveStepResponse>> right() {
        return moveStepCompleted("right");
    }

    @Bean
    public Consumer<Flux<MoveStepResponse>> left() {
        return moveStepCompleted("left");
    }

    private Consumer<Flux<MoveStepResponse>> moveStepCompleted(String step) {
        return flux -> flux
                .parallel()
                .runOn(Schedulers.newBoundedElastic(Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE, Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE, "steps"))
                .doOnNext(resp -> moveChoreographer.handleMoveStepCompleted(resp.getTurnId(), resp.getMoveId(), step, resp.isFailed()))
                .subscribe();
    }
}
