package com.smalik.choreographer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class Metrics {

    private final MeterRegistry registry;
    private final SLA sla;

    public Timer createTimer(String name, String... tags) {
        return Timer.builder(name)
                .tags(tags)
                .publishPercentileHistogram()
                .sla(Duration.ofMillis(sla.getMillis()))
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofMillis(sla.getTimeoutMillis()))
                .register(registry);
    }
}
