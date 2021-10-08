package com.smalik.choreographer;

import com.smalik.choreographer.api.Move;
import com.smalik.choreographer.api.TurnRequest;
import com.smalik.choreographer.api.TurnResponse;
import com.smalik.choreographer.db.PlayerLockService;
import com.smalik.choreographer.db.RequestsDatabase;
import com.smalik.choreographer.db.TurnsInMemoryDatabase;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TurnResponseGenerator {

    private final TurnsInMemoryDatabase memory;
    private final StreamBridge streamBridge;
    private final TurnResponseSink sink;
    private final PlayerLockService lockService;
    private final RequestsDatabase requests;
    private final Metrics metrics;

    private Timer generateResponseTimer;

    @PostConstruct
    public void init() {
        generateResponseTimer = metrics.createTimer("turns.generate-response");
    }

    public TurnResponse generateTurnResponse(MoveInitiator moveInitiator, TurnRequest request, boolean timeout) {
        return generateResponseTimer.record(() -> {

            TurnResponse response = TurnResponse.builder()
                    .playerId(request.getPlayerId())
                    .turnId(request.getTurnId())
                    .startTime(request.getTime())
                    .finishTime(OffsetDateTime.now())
                    .timeout(timeout)
                    .moves(request.getMoves().stream()
                            .map(mr -> memory
                                    .findMove(mr.getMoveId())
                                    .orElse(moveInitiator.toMoveFromRequest(request, mr, Move.Status.NONE)))
                            .collect(Collectors.toList()))
                    .build();

            // notify the sink
            if (timeout) {
                sink.registerTimeout(request.getTurnId());
            } else {
                sink.registerResponse(response);
            }

            // clean up database
            memory.cleanup(request);
            requests.removeRequest(request.getTurnId());

            // only release lock if no other requests are waiting for this player
            if (requests.findRequests(request.getPlayerId()).isEmpty()) {
                lockService.unlock(request.getPlayerId());
            }

            // let everybody know turn is complete
            streamBridge.send("turn-completed", TurnCompleted.builder()
                    .turnId(request.getTurnId())
                    .playerId(request.getPlayerId())
                    .timeout(timeout)
                    .build());

            return response;
        });
    }
}
