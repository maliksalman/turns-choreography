package com.smalik.choreographer;

import java.util.ArrayList;
import java.util.List;

public class LoadDistribution {

    public static final int MAX_TICKS_PER_SECONDS = 20;

    private List<Integer> messagesForTicks;

    public LoadDistribution(List<Load> loads) {

        this.messagesForTicks = new ArrayList<>();
        
        for (int i = 0; i < loads.size(); i++) {
            Load load = loads.get(i);
            for (int tick = 0; tick < load.getDurationSeconds() * MAX_TICKS_PER_SECONDS; tick++) {
                int unevenness = load.getArrivalRate() % MAX_TICKS_PER_SECONDS;
                if ((tick % MAX_TICKS_PER_SECONDS) < unevenness) {
                    messagesForTicks.add((load.getArrivalRate() / MAX_TICKS_PER_SECONDS) + 1);
                } else {
                    messagesForTicks.add(load.getArrivalRate() / MAX_TICKS_PER_SECONDS);
                }
            }
        }
    }

    public int getTicks() {
        return messagesForTicks.size();
    }

    public long getDelayMillis() {
        return 1000 / MAX_TICKS_PER_SECONDS;
    }

    public int getMessagesForTick(int tick) {
        return messagesForTicks.get(tick);
    }
}
