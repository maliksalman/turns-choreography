package com.smalik.choreographer.db.geode;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GeodeRequestsRepository extends CrudRepository<GeodeTurnRequestInfo, String> {
    List<GeodeTurnRequestInfo> findByPlayerId(String playerId);
}
