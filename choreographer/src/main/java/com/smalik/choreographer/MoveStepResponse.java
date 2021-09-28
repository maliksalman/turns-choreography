package com.smalik.choreographer;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MoveStepResponse {

    String turnId;
    String playerId;
    String moveId;

    boolean failed;
    OffsetDateTime time;
}
