package com.smalik.choreographer.messaging;

import com.smalik.choreographer.MoveChoreographer;
import com.smalik.choreographer.TurnChoreographer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class ChoreographyListener {

    private final TurnChoreographer turnChoreographer;
    private final MoveChoreographer moveChoreographer;

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

    @Bean
    public Consumer<MoveCompleted> move() {
        return resp -> {
            turnChoreographer.handleMoveCompleted(resp.getTurnId(), resp.getMoveId());
        };
    }

    @Bean
    public Consumer<TurnCompleted> turn() {
        return resp -> {
            turnChoreographer.handleTurnCompleted(resp.getTurnId(), resp.getPlayerId(), resp.isTimeout());
        };
    }
}
