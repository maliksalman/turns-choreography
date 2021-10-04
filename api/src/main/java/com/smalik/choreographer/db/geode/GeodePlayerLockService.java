package com.smalik.choreographer.db.geode;

import com.smalik.choreographer.db.PlayerLockService;
import lombok.RequiredArgsConstructor;
import org.apache.geode.cache.Region;

import java.util.UUID;

@RequiredArgsConstructor
public class GeodePlayerLockService implements PlayerLockService {

    private final Region<String, String> locksRegion;

    public boolean lock(String playerId) {
        String uuid = UUID.randomUUID().toString();
        return locksRegion.putIfAbsent(playerId, uuid) == null;
    }

    @Override
    public void unlock(String playerId) {
        locksRegion.destroy(playerId);
    }
}
