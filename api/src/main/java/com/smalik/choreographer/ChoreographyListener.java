package com.smalik.choreographer;

import com.smalik.choreographer.api.Move;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class ChoreographyListener {

    private final TurnChoreographer turnChoreographer;

    @Bean
    public Consumer<Move> moveCompleted() {
        return move -> {
            turnChoreographer.handleMoveCompleted(move);
        };
    }

    @Bean
    public Consumer<TurnCompleted> turnCompleted() {
        return resp -> {
            turnChoreographer.handleTurnCompleted(resp.getTurnId(), resp.getPlayerId(), resp.isTimeout());
        };
    }
}
