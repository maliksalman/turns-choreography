package com.smalik.choreographer.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TurnRequest {

    String turnId;
    String playerId;
    OffsetDateTime time;
    List<MoveRequest> moves;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MoveRequest {
        String moveId;
        String type;
        int places;
    }
}