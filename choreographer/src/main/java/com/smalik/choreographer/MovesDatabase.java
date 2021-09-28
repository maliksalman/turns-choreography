package com.smalik.choreographer;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MovesDatabase {

    private final Map<String, Move> moves = new ConcurrentHashMap<>();

    public void addMove(Move move) {
        moves.put(move.getMoveId(), move);
    }

    public Optional<Move> findMove(String moveId) {
        return Optional.ofNullable(moves.get(moveId));
    }

    public void removeMove(String moveId) {
        moves.remove(moveId);
    }
}
