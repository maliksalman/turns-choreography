package com.smalik.choreographer;

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

    public static final List<String> STEPS = List.of("forward", "right", "back", "left");
    public enum Status { NONE, REQUESTED, DONE }

    String moveId;

    String turnId;
    String playerId;
    String type;
    int quantity;
    Status status;

    Map<String, StepStatus> statuses;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StepStatus {
        String step;
        boolean failed;
        OffsetDateTime startTime;
        OffsetDateTime finishTime;
        Status status;
    }
}
