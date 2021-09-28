package com.smalik.choreographer;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class ChoreographyListener {

    private final MoveChoreographer moveChoreographer;

    @Bean
    public Consumer<Move> move() {
        return move -> {
            moveChoreographer.startProcessingMove(move);
        };
    }


    @Bean
    public Consumer<MoveStepResponse> breathe() {
        return resp -> {
            moveChoreographer.handleMoveStepCompleted(resp.getTurnId(), resp.getMoveId(), "breathe", resp.isFailed());
        };
    }

    @Bean
    public Consumer<MoveStepResponse> think() {
        return resp -> {
            moveChoreographer.handleMoveStepCompleted(resp.getTurnId(), resp.getMoveId(), "think", resp.isFailed());
        };
    }


    @Bean
    public Consumer<MoveStepResponse> act() {
        return resp -> {
            moveChoreographer.handleMoveStepCompleted(resp.getTurnId(), resp.getMoveId(), "act", resp.isFailed());
        };
    }

    @Bean
    public Consumer<MoveStepResponse> react() {
        return resp -> {
            moveChoreographer.handleMoveStepCompleted(resp.getTurnId(), resp.getMoveId(), "react", resp.isFailed());
        };
    }
}
