package com.smalik.choreographer.db.geode;

import com.smalik.choreographer.db.TurnRequestInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GeodeRequestsRepository extends CrudRepository<TurnRequestInfo, String> {

    List<TurnRequestInfo> findByPlayerId(String playerId);
}
