package com.smalik.choreographer.api;

import org.springframework.stereotype.Service;

@Service
public class PlayerLockService {

    public boolean lock(String playerId) {
        return true;
    }

    public void unlock(String playerId) {

    }

}
