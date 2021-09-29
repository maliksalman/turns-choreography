package com.smalik.choreographer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovesDatabase {

    private final RedissonClient client;
    private final ObjectMapper mapper;

    @SneakyThrows
    public void saveMove(Move move) {
        client.getBucket("move:" + move.getMoveId())
                .set(mapper.writeValueAsString(move));
    }

    public Optional<Move> findMove(String moveId) {
        return Optional
                .of(client.getBucket("move:" + moveId))
                .filter(bucket -> bucket.isExists())
                .map(bucket -> toMove(bucket.get().toString()));
    }

    @SneakyThrows
    private Move toMove(String json) {
        return mapper.readValue(json, Move.class);
    }

    public void removeMove(String moveId) {
        client
                .getBucket("move:" + moveId)
                .delete();
    }
}
