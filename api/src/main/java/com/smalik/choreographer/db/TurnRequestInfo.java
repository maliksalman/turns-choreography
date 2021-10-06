package com.smalik.choreographer.db;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class TurnRequestInfo {

    String turnId;
    String playerId;
    OffsetDateTime time;

}
