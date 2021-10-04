package com.smalik.choreographer.api;

import com.smalik.choreographer.Metrics;
import com.smalik.choreographer.SLA;
import com.smalik.choreographer.TurnsService;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
@RestController
@RequestMapping("/turns")
public class TurnsController {

    private final TurnsService service;
    private final Metrics metrics;
    private final SLA sla;

    private Timer timeoutTimer;
    private Timer successTimer;

    @PostConstruct
    public void init() {
        timeoutTimer = metrics.createTimer("turns.api", "timeout", Boolean.TRUE.toString());
        successTimer = metrics.createTimer("turns.api", "timeout", Boolean.FALSE.toString());
    }

    @PostMapping
    public Mono<TurnResponse> turn(@RequestBody TurnRequest request) {
        Instant instant = Instant.now();
        return service
                .turn(request)
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(
                        Duration.ofMillis(sla.getTimeoutMillis()),
                        service.turnTimedOut(request),
                        Schedulers.boundedElastic())
                .doOnNext(resp -> (resp.isTimeout() ? timeoutTimer : successTimer)
                        .record(Duration.between(instant, Instant.now())));
    }
}
