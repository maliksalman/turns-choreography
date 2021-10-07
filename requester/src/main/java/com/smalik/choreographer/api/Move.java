package com.smalik.choreographer.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Move {

    public enum Status { NONE, REQUESTED, DONE }

    String turnId;
    String playerId;
    String moveId;
    String type;
    int quantity;

    MoveStatus status;
    Map<String, StepStatus> stepStatuses;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MoveStatus {
        boolean failed;
        OffsetDateTime startTime;
        OffsetDateTime finishTime;
        Status status;
    }

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
