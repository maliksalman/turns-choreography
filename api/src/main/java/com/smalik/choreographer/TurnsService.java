package com.smalik.choreographer;

import com.smalik.choreographer.api.TurnRequest;
import com.smalik.choreographer.api.TurnResponse;
import com.smalik.choreographer.db.PlayerLockService;
import com.smalik.choreographer.db.RequestsDatabase;
import com.smalik.choreographer.db.TurnRequestInfo;
import com.smalik.choreographer.db.TurnsInMemoryDatabase;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j
public class TurnsService {

    private final MoveInitiator moveInitiator;
    private final TurnResponseGenerator responseGenerator;
    private final TurnResponseSink sink;

    private final TurnsInMemoryDatabase memory;
    private final PlayerLockService lockService;
    private final RequestsDatabase requests;
    private final Metrics metrics;

    private Timer processTimer;

    @PostConstruct
    public void init() {
        processTimer = metrics.createTimer("turns.process");
    }

    public Mono<TurnResponse> turn(TurnRequest req) {
        return Mono
                .fromRunnable(() -> processTimer.record(() -> {
                            // save the request
                            memory.addTurnRequest(req);
                            requests.addRequest(TurnRequestInfo.builder()
                                    .playerId(req.getPlayerId())
                                    .turnId(req.getTurnId())
                                    .time(req.getTime())
                                    .build());

                            if (lockService.lock(req.getPlayerId())) {
                                log.info("Processing turn: Player={}, Turn={}", req.getPlayerId(), req.getTurnId());
                                TurnRequest.MoveRequest mr = req.getMoves().get(0);
                                moveInitiator.initiateMove(req, mr);
                            } else {
                                log.info("Waiting: Player={}, Turn={}", req.getPlayerId(), req.getTurnId());
                                memory.addWaiting(req);
                            }
                        }))
                .then(sink.findResponse(req.getTurnId()));
    }

    public Mono<TurnResponse> turnTimedOut(TurnRequest request) {
        return Mono
                .fromCallable(() -> responseGenerator.generateTurnResponse(moveInitiator, request, true));
    }
}
