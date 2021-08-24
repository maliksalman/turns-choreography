package com.smalik.choreographer.messaging;

import com.smalik.choreographer.Choreographer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class ChoreographyListener {

    private final Choreographer choreographer;

    @Bean
    public Consumer<MoveStepResponse> breathe() {
        return resp -> {
            choreographer.handleMoveStepCompleted(resp.getTurnId(), resp.getMoveId(), "breathe", resp.isFailed());
        };
    }

    @Bean
    public Consumer<MoveStepResponse> think() {
        return resp -> {
            choreographer.handleMoveStepCompleted(resp.getTurnId(), resp.getMoveId(), "think", resp.isFailed());
        };
    }


    @Bean
    public Consumer<MoveStepResponse> act() {
        return resp -> {
            choreographer.handleMoveStepCompleted(resp.getTurnId(), resp.getMoveId(), "act", resp.isFailed());
        };
    }

    @Bean
    public Consumer<MoveStepResponse> react() {
        return resp -> {
            choreographer.handleMoveStepCompleted(resp.getTurnId(), resp.getMoveId(), "react", resp.isFailed());
        };
    }

    @Bean
    public Consumer<MoveCompleted> move() {
        return resp -> {
            choreographer.handleMoveCompleted(resp.getTurnId(), resp.getMoveId());
        };
    }

    @Bean
    public Consumer<TurnCompleted> turn() {
        return resp -> {
            choreographer.handleTurnCompleted(resp.getTurnId(), resp.getPlayerId());
        };
    }
}
