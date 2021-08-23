package com.smalik.choreographer;

import com.smalik.choreographer.api.TurnRequest;
import com.smalik.choreographer.api.TurnResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Service
public class TurnsService {

    private Choreographer choreographer;

    private Flux<TurnResponse> responses;
    private FluxSink<TurnResponse> sink;

    @Autowired
    public void setChoreographer(Choreographer choreographer) {
        this.choreographer = choreographer;
    }

    @PostConstruct
    public void init() {
        this.responses = Flux.<TurnResponse>create(s -> this.sink = s);
    }

    public Mono<TurnResponse> turn(TurnRequest req) {

        choreographer.turnNow(req);

        return responses
            .filter(resp -> req.getTurnId().equals(resp.getTurnId()))
            .next();

    }

    public void registerResponse(TurnResponse response) {
        sink.next(response);
    }
}
