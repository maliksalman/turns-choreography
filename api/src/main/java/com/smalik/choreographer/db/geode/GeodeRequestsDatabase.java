package com.smalik.choreographer.db.geode;

import com.smalik.choreographer.db.RequestsDatabase;
import com.smalik.choreographer.db.TurnRequestInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Profile("geode")
public class GeodeRequestsDatabase implements RequestsDatabase {

    private final GeodeRequestsRepository repository;

    public void addRequest(TurnRequestInfo request) {
        repository.save(GeodeTurnRequestInfo.builder()
                .playerId(request.getPlayerId())
                .time(request.getTime())
                .turnId(request.getTurnId())
                .build());
    }

    public List<TurnRequestInfo> findRequests(String playerId) {
        return repository.findByPlayerId(playerId).stream()
                .map(request -> TurnRequestInfo.builder()
                        .playerId(request.getPlayerId())
                        .time(request.getTime())
                        .turnId(request.getTurnId())
                        .build())
                .sorted(Comparator.comparing(TurnRequestInfo::getTime))
                .collect(Collectors.toList());
    }

    public void removeRequest(String turnId) {
        repository.deleteById(turnId);
    }
}
