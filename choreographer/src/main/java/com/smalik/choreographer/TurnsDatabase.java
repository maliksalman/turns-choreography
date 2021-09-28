package com.smalik.choreographer;

import com.smalik.choreographer.api.Move;
import com.smalik.choreographer.api.TurnRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class TurnsDatabase {

    private ConcurrentMap<String, TurnRequest> inProgressRequests;
    private ConcurrentMap<String, Move> moves;

    @PostConstruct
    public void init() {
        inProgressRequests = new ConcurrentHashMap<>();
        moves = new ConcurrentHashMap<>();
    }

    public void addInProgress(TurnRequest request) {
        inProgressRequests.put(request.getTurnId(), request);
    }

    public void addMove(Move move) {
        moves.put(move.getMoveId(), move);
    }

    public Optional<TurnRequest> findInProgressRequest(String turnId) {
        return Optional.ofNullable(inProgressRequests.get(turnId));
    }

    public Optional<Move> findMove(String moveId) {
        return Optional.ofNullable(moves.get(moveId));
    }

    public void cleanup(TurnRequest request) {
        inProgressRequests.remove(request.getTurnId());
        request.getMoves().forEach(m -> moves.remove(m.getMoveId()));
    }
}
