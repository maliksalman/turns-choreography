package com.smalik.choreographer.db;

import com.smalik.choreographer.Move;
import lombok.RequiredArgsConstructor;
import org.apache.geode.cache.Region;

import java.util.Optional;

@RequiredArgsConstructor
public class GeodeMovesDatabase implements MovesDatabase {

    private final Region<String, Move> moves;

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
