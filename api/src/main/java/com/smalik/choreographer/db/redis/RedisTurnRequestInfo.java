package com.smalik.choreographer.db.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@RedisHash("requests")
public class RedisTurnRequestInfo {

    @Id
    String turnId;

    @Indexed
    String playerId;

    long time;

}
