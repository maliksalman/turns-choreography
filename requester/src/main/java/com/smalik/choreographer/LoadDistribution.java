package com.smalik.choreographer;

public class LoadDistribution {

    private static final int MAX_TICKS_PER_SECONDS = 20;

    private int arrivalRate;
    private int durationSeconds;

    public LoadDistribution(Load load) {
        this.arrivalRate = load.getArrivalRate();
        this.durationSeconds = load.getDurationSeconds();
    }

    private int getTicksPerSecond() {
        if (arrivalRate < MAX_TICKS_PER_SECONDS) {
            for (int i = arrivalRate; i > 0; i--) {
                if (1000 % i == 0) {
                    return i;
                }
            }
        }
        return MAX_TICKS_PER_SECONDS;
    }

    public int getTicks() {
        return getTicksPerSecond() * durationSeconds;
    }

    public long getDelayMillis() {
        return 1000 / getTicksPerSecond();
    }

    public int getMessagesForTick(int tick) {
        int ticksPerSecond = getTicksPerSecond();
        int unevenness = arrivalRate % ticksPerSecond;

        if ((tick % ticksPerSecond) < unevenness) {
            return (arrivalRate / ticksPerSecond) + 1;
        } else {
            return arrivalRate / ticksPerSecond;
        }
    }
}
