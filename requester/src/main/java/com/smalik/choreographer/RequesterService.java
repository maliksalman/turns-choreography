package com.smalik.choreographer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class RequesterService {

    private final WebClient webClient;

    public void generateLoad(Load load) {
        Flux.
    }
}
