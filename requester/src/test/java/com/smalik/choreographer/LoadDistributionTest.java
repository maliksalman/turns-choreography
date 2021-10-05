package com.smalik.choreographer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoadDistributionTest {

    LoadDistribution create(int arrivalRate, int durationSeconds) {
        return new LoadDistribution(Load.builder()
                .arrivalRate(arrivalRate)
                .durationSeconds(durationSeconds)
                .build());
    }

    @Test
    void testSimple() {
        LoadDistribution distribution = create(1, 1);
        assertLoadDistribution(distribution, 1000, 1);
    }

    @Test
    void testSimpleForMultipleSeconds() {
        LoadDistribution distribution = create(1, 10);
        assertLoadDistribution(distribution, 1000, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
    }

    @Test
    void testEvenDistribution() {
        LoadDistribution distribution = create(10, 1);
        assertLoadDistribution(distribution, 100, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
    }

    @Test
    void testUnevenDistribution() {
        LoadDistribution distribution = create(7, 1);
        assertLoadDistribution(distribution, 200, 2, 2, 1, 1, 1);
    }

    @Test
    void testUnevenDistributionMultipleSeconds() {
        LoadDistribution distribution = create(7, 2);
        assertLoadDistribution(distribution, 200, 2, 2, 1, 1, 1, 2, 2, 1, 1, 1);
    }

    @Test
    void testUnevenDistributionOver20() {
        LoadDistribution distribution = create(23, 1);
        assertLoadDistribution(distribution, 50,
                2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
    }

    @Test
    void testUnevenDistributionOver20MultipleSeconds() {
        LoadDistribution distribution = create(23, 2);
        assertLoadDistribution(distribution, 50, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
    }

    void assertLoadDistribution(LoadDistribution distribution, long expectedMillis, int... expectedMessagesForTick) {

        assertEquals(expectedMillis, distribution.getDelayMillis());
        assertEquals(expectedMessagesForTick.length, distribution.getTicks());

        for(int i = 0; i < expectedMessagesForTick.length; i++) {
            assertEquals(expectedMessagesForTick[i], distribution.getMessagesForTick(i));
        }
    }
}