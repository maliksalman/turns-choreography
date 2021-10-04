package com.smalik.choreographer;

import com.smalik.choreographer.api.TurnRequestInfo;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class RequestsDatabase {

    public void addRequest(String turnId, String playerId, OffsetDateTime time) {

    }

    public List<TurnRequestInfo> findRequests(String playerId) {
        return Collections.emptyList();
    }

    public void removeRequest(String turnId) {

    }
}
