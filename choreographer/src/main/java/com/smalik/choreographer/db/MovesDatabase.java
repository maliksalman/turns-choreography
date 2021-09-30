package com.smalik.choreographer.db;

import com.smalik.choreographer.Move;

import java.util.Optional;

public interface MovesDatabase {

    void save(Move move);

    Optional<Move> findOne(String moveId);

    void remove(String moveId);
}
