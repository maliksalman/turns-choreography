package com.smalik.choreographer.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smalik.choreographer.Move;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.redisson.api.RedissonClient;

import java.util.Optional;

@RequiredArgsConstructor
public class RedissonMovesDatabase implements MovesDatabase {

    private final RedissonClient client;
    private final ObjectMapper mapper;

    @Override
    @SneakyThrows
    public void save(Move move) {
        client.getBucket("move:" + move.getMoveId())
                .set(mapper.writeValueAsString(move));
    }

    @Override
    public Optional<Move> findOne(String moveId) {
        return Optional
                .of(client.getBucket("move:" + moveId))
                .filter(bucket -> bucket.isExists())
                .map(bucket -> toMove(bucket.get().toString()));
    }

    @SneakyThrows
    private Move toMove(String json) {
        return mapper.readValue(json, Move.class);
    }

    @Override
    public void remove(String moveId) {
        client
                .getBucket("move:" + moveId)
                .delete();
    }
}
