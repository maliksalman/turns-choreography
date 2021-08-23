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
public class TurnResponse {

    String turnId;
    String playerId;
    OffsetDateTime startTime;
    OffsetDateTime finishTime;

    boolean timeout;
    List<Move> moves;
}
