package com.smalik.choreographer;

import com.smalik.choreographer.api.TurnResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TurnResponseSink {

    private final Map<String, Sinks.One<TurnResponse>> sinks = new ConcurrentHashMap<>();

    public Mono<TurnResponse> findResponse(String turnId) {
        Sinks.One<TurnResponse> one = Sinks.one();
        sinks.put(turnId, one);
        return one.asMono();
    }

    public void registerResponse(TurnResponse response) {
        sinks.get(response.getTurnId())
                .tryEmitValue(response);
        sinks.remove(response.getTurnId());
    }

    public void registerTimeout(String turnId) {
        sinks.remove(turnId);
    }
}
