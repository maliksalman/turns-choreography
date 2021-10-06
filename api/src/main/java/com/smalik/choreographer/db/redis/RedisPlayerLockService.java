package com.smalik.choreographer.db.redis;

import com.smalik.choreographer.db.PlayerLockService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Profile("redisson")
public class RedisPlayerLockService implements PlayerLockService {

    private final RedissonClient client;

    @SneakyThrows
    public boolean lock(String playerId) {
        return client.getBucket("player:lock:" + playerId)
                .trySet(playerId, 6, TimeUnit.SECONDS);
    }

    public void unlock(String playerId) {
        client.getBucket("player:lock:" + playerId)
                .delete();
    }
}
