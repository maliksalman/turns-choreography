package com.smalik.choreographer.db;

import com.smalik.choreographer.WaitingRequestList;
import com.smalik.choreographer.api.Move;
import com.smalik.choreographer.api.TurnRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class TurnsInMemoryDatabase {

    private ConcurrentMap<String, TurnRequest> requests;
    private ConcurrentMap<String, Move> moves;
    private ConcurrentMap<String, WaitingRequestList> waitingRequests;

    @PostConstruct
    public void init() {
        requests = new ConcurrentHashMap<>();
        moves = new ConcurrentHashMap<>();
        waitingRequests = new ConcurrentHashMap<>();
    }

    public void addTurnRequest(TurnRequest request) {
        requests.put(request.getTurnId(), request);
    }

    public void addUpdateMove(Move move) {
        moves.put(move.getMoveId(), move);
    }

    public Optional<TurnRequest> findTurnRequest(String turnId) {
        return Optional.ofNullable(requests.get(turnId));
    }

    public Optional<Move> findMove(String moveId) {
        return Optional.ofNullable(moves.get(moveId));
    }

    public void cleanup(TurnRequest request) {
        requests.remove(request.getTurnId());
        request.getMoves().forEach(m -> moves.remove(m.getMoveId()));
        waitingRequests.remove(request);
    }

    public void addWaiting(TurnRequest request) {
        synchronized (waitingRequests) {
            waitingRequests
                    .computeIfAbsent(request.getPlayerId(), s -> new WaitingRequestList())
                    .add(request);
        }
    }

    public void removeWaiting(TurnRequest request) {
        synchronized (waitingRequests) {
            WaitingRequestList requestList = waitingRequests.get(request.getPlayerId());
            requestList.remove(request);
            if (requestList.isEmpty()) {
                waitingRequests.remove(request.getPlayerId());
            }
        }
    }

    public boolean isNextWaitingRequest(String playerId, String requestId) {
        synchronized (waitingRequests) {
            WaitingRequestList list = waitingRequests.get(playerId);
            if (list != null) {
                return requestId.equals(list.iterator().next().getTurnId());
            }
            return false;
        }
    }
}
