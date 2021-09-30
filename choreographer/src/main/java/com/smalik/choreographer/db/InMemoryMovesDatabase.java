package com.smalik.choreographer.db;

import com.smalik.choreographer.Move;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMovesDatabase implements MovesDatabase {

    private final Map<String, Move> moves = new ConcurrentHashMap<>();

    public void save(Move move) {
        moves.put(move.getMoveId(), move);
    }

    public Optional<Move> findOne(String moveId) {
        return Optional.ofNullable(moves.get(moveId));
    }

    public void remove(String moveId) {
        moves.remove(moveId);
    }
}
