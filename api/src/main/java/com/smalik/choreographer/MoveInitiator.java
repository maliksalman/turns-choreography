package com.smalik.choreographer;

import com.smalik.choreographer.api.Move;
import com.smalik.choreographer.api.TurnRequest;
import com.smalik.choreographer.db.TurnsInMemoryDatabase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class MoveInitiator {

    private final StreamBridge streamBridge;
    private final TurnsInMemoryDatabase memory;

    public void initiateMove(TurnRequest request, TurnRequest.MoveRequest moveRequest) {
        Move move = toMoveFromRequest(request, moveRequest, Move.Status.REQUESTED);
        move.getStatus().setStartTime(OffsetDateTime.now());

        memory.addUpdateMove(move);
        streamBridge.send("move-requested", move);
    }

    public Move toMoveFromRequest(TurnRequest request, TurnRequest.MoveRequest mr, Move.Status initialMoveStatus) {
        return Move.builder()
                .turnId(request.getTurnId())
                .playerId(request.getPlayerId())
                .moveId(mr.getMoveId())
                .type(mr.getType())
                .quantity(mr.getPlaces())
                .status(Move.MoveStatus.builder()
                        .status(initialMoveStatus)
                        .build())
                .build();
    }

}
