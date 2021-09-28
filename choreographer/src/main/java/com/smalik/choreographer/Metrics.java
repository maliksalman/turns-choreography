package com.smalik.choreographer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class Metrics {

    private final MeterRegistry registry;
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();

    public Timer getTimer(String name, String... tags) {
        return timers.computeIfAbsent(name, nameAsKey -> Timer.builder(nameAsKey)
                .tags(tags)
                .publishPercentileHistogram()
                .sla(Duration.ofMillis(2500))
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofMillis(5000))
                .register(registry));
    }
}
