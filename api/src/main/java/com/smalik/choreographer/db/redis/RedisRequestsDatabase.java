package com.smalik.choreographer.db.redis;

import com.smalik.choreographer.db.RequestsDatabase;
import com.smalik.choreographer.db.TurnRequestInfo;
import com.smalik.choreographer.db.geode.GeodeTurnRequestInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Profile("redisson")
public class RedisRequestsDatabase implements RequestsDatabase {

    private final RedisRequestsRepository repository;

    @Override
    public void addRequest(TurnRequestInfo request) {
        repository.save(RedisTurnRequestInfo.builder()
                .playerId(request.getPlayerId())
                .time(request.getTime().toInstant().toEpochMilli())
                .turnId(request.getTurnId())
                .build());
    }

    @Override
    public List<TurnRequestInfo> findRequests(String playerId) {
        return repository.findByPlayerId(playerId).stream()
                .map(request -> TurnRequestInfo.builder()
                        .playerId(request.getPlayerId())
                        .time(Instant.ofEpochMilli(request.getTime()).atOffset(ZoneOffset.UTC))
                        .turnId(request.getTurnId())
                        .build())
                .sorted(Comparator.comparing(TurnRequestInfo::getTime))
                .collect(Collectors.toList());
    }

    @Override
    public void removeRequest(String turnId) {
        repository.deleteById(turnId);
    }
}
