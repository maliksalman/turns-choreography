package com.smalik.choreographer.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Move {

    public static final List<String> STEPS = List.of("breathe", "think", "act", "react");

    String turnId;
    String playerId;
    String moveId;
    String type;
    int quantity;

    Map<String, StepStatus> statuses;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StepStatus {
        String step;
        boolean complete;
        OffsetDateTime startTime;
        OffsetDateTime finishTime;
        String status;
    }
}
