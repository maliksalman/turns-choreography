package com.smalik.choreographer.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TurnRequestInfo {

    String turnId;
    String playerId;
    OffsetDateTime time;

}
