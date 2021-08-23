package com.smalik.choreographer.api;

import com.smalik.choreographer.TurnsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/turns")
public class TurnsController {

    private final TurnsService service;

    @PostMapping
    public Mono<TurnResponse> turn(@RequestBody TurnRequest request) {
        return service.turn(request)
                .timeout(Duration.ofSeconds(30), Mono.just(TurnResponse.builder()
                        .turnId(request.getTurnId())
                        .playerId(request.getPlayerId())
                        .time(OffsetDateTime.now())
                        .timeout(true)
                        .build()));
    }
}
