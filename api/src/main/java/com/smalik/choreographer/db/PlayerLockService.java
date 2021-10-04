package com.smalik.choreographer.db;

public interface PlayerLockService {

    boolean lock(String playerId);

    void unlock(String playerId);

}
