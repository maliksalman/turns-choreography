package com.smalik.choreographer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TurnCompleted {
    String turnId;
    String playerId;
    boolean timeout;
}
