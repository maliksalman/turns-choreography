package com.smalik.choreographer.db;

import java.util.List;

public interface RequestsDatabase {

    void addRequest(TurnRequestInfo request);

    List<TurnRequestInfo> findRequests(String playerId);

    void removeRequest(String turnId);
}
