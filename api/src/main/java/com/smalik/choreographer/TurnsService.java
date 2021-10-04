package com.smalik.choreographer;

import com.smalik.choreographer.api.TurnRequest;
import com.smalik.choreographer.api.TurnResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TurnsService {

    private final TurnChoreographer turnChoreographer;
    private final TurnResponseSink sink;

    public Mono<TurnResponse> turn(TurnRequest req) {
        return Mono
                .fromRunnable(() -> turnChoreographer.turn(req))
                .then(sink.findResponse(req.getTurnId()));
    }

    public Mono<TurnResponse> turnTimedOut(TurnRequest request) {
        return Mono
                .fromCallable(() -> turnChoreographer.turnTimedOut(request));
    }
}
