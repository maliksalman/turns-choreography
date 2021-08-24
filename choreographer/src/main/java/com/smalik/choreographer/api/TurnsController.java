package com.smalik.choreographer.api;

import com.smalik.choreographer.TurnsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/turns")
public class TurnsController {

    private final TurnsService service;

    @PostMapping
    public Mono<TurnResponse> turn(@RequestBody TurnRequest request, @RequestParam(name = "serverOverridesTime", required = false, defaultValue = "false") boolean serverOverridesTime) {
        if (serverOverridesTime || request.getTime() == null) {
            request.setTime(OffsetDateTime.now());
        }
        return service.turn(request)
                .timeout(Duration.ofMillis(3))
                .onErrorResume(throwable -> service.turnTimedOut(request));
    }
}
