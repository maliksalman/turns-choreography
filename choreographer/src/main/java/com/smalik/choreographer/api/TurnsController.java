package com.smalik.choreographer.api;

import com.smalik.choreographer.TurnsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@RequiredArgsConstructor
@RestController
@RequestMapping("/turns")
public class TurnsController {

    private final TurnsService service;

    @PostMapping
    public Mono<TurnResponse> turn(@RequestBody TurnRequest request) {
        return service
                .turn(request)
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(
                        Duration.ofSeconds(5),
                        service.turnTimedOut(request),
                        Schedulers.boundedElastic());
    }
}
