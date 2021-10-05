package com.smalik.choreographer;

import com.github.javafaker.Faker;
import com.smalik.choreographer.api.TurnRequest;
import com.smalik.choreographer.api.TurnResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequesterService {

    private final Faker faker = Faker.instance();

    public void generateLoad(WebClient webClient, List<Load> loadRequests) {
        AtomicInteger counter = new AtomicInteger(0);
        List<Flux<TurnResponse>> fluxes = loadRequests.stream()
                .map(load -> Flux.range(1, load.getDurationSeconds()*10)
                        .limitRate(1)
                        .delayElements(Duration.ofMillis(100))
                        .map(idx -> Flux.range(1, load.getArrivalRate()/10))
                        .flatMap(idx -> idx)
                        .map(idx -> generateRequest())
                        .map(req -> makeRequest(webClient, req))
                        .flatMap(resp -> resp))
                .collect(Collectors.toList());
        Flux.concat(fluxes)
                .subscribe(resp -> collectStats(counter, resp));
    }

    private void collectStats(AtomicInteger counter, TurnResponse resp) {
        log.info("Number={}, Id={}, Timeout={}, ResponseGenerationTime={}, ResponseTime={}",
                counter.incrementAndGet(),
                resp.getTurnId(),
                resp.isTimeout(),
                Duration.between(resp.getStartTime(), resp.getFinishTime()).toMillis(),
                Duration.between(resp.getStartTime(), OffsetDateTime.now()).toMillis());
    }

    private TurnRequest generateRequest() {
        return TurnRequest.builder()
                .time(OffsetDateTime.now())
                .playerId(faker.lorem().word() + "-" + faker.random().hex(8))
                .turnId(UUID.randomUUID().toString())
                .moves(List.of(TurnRequest.MoveRequest.builder()
                        .moveId(UUID.randomUUID().toString())
                        .places(4)
                        .type("type")
                        .build()))
                .build();
    }

    private Mono<TurnResponse> makeRequest(WebClient webClient, TurnRequest request) {
        return webClient.post()
                .uri("/turns")
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(TurnResponse.class)
                .publishOn(Schedulers.boundedElastic());
    }
}
