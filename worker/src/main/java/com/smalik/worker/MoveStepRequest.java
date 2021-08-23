package com.smalik.worker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MoveStepRequest {

    String turnId;
    String playerId;
    String moveId;
    String step;
}
