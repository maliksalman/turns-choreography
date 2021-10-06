package com.smalik.choreographer.db.redis;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RedisRequestsRepository extends CrudRepository<RedisTurnRequestInfo, String> {
    List<RedisTurnRequestInfo> findByPlayerId(String playerId);
}
