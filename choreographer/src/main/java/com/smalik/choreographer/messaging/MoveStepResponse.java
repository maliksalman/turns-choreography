package com.smalik.choreographer.messaging;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MoveStepResponse {

    String turnId;
    String playerId;
    String moveId;

    boolean failed;

}
