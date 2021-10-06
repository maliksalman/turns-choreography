package com.smalik.choreographer.db.geode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Indexed;
import org.springframework.data.gemfire.mapping.annotation.Region;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Region("requests")
public class GeodeTurnRequestInfo {

    @Id
    String turnId;

    @Indexed
    String playerId;

    OffsetDateTime time;

}
