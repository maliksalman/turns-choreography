package com.smalik.choreographer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoadDistributionTest {

    LoadDistribution create(Load... loads) {
        return new LoadDistribution(List.of(loads));
    }

    int[] getExpected20Pair(int val, int count) {
        int[] arr = new int[LoadDistribution.MAX_TICKS_PER_SECONDS];
        for(int i = 0; i < LoadDistribution.MAX_TICKS_PER_SECONDS; i++) {
            arr[i] = (i < count) ? val : (val-1);
        }
        return arr;
    }

    @Test
    void testSimple() {
        LoadDistribution distribution = create(new Load(1, 1));
        assertLoadDistribution(distribution, getExpected20Pair(1, 1));
    }

    @Test
    void testSimpleUneven() {
        LoadDistribution distribution = create(new Load(24, 1));
        assertLoadDistribution(distribution, getExpected20Pair(2, 4));
    }

    @Test
    void testSimpleUnevenLargeArrivalRate() {
        LoadDistribution distribution = create(new Load(210, 1));
        assertLoadDistribution(distribution, getExpected20Pair(11, 10));
    }

    @Test
    void testSimpleUnevenMultipleSeconds() {
        LoadDistribution distribution = create(new Load(24, 2));
        assertLoadDistribution(distribution, getExpected20Pair(2, 4), getExpected20Pair(2, 4));
    }

    @Test
    void testMultipleUneven() {
        LoadDistribution distribution = create(new Load(24, 1), new Load(43, 1));
        assertLoadDistribution(distribution, getExpected20Pair(2, 4), getExpected20Pair(3, 3));
    }

    @Test
    void testMultipleUnevenMultipleSeconds() {
        LoadDistribution distribution = create(new Load(24, 2), new Load(43, 2));
        assertLoadDistribution(distribution, getExpected20Pair(2, 4), getExpected20Pair(2, 4), getExpected20Pair(3, 3), getExpected20Pair(3, 3));
    }

    void assertLoadDistribution(LoadDistribution distribution, int[]... expectedMessagesForTick) {
        for(int i = 0; i < expectedMessagesForTick.length; i++) {
            for (int j = 0; j < expectedMessagesForTick[i].length; j++) {
                assertEquals(expectedMessagesForTick[i][j], distribution.getMessagesForTick((i*expectedMessagesForTick[i].length)+j));
            }
        }
    }
}