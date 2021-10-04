package com.smalik.choreographer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "sla")
@Component
public class SLA {

    private long millis;
    private long timeoutMillis;
}
