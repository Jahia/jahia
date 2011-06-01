package org.jahia.utils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for thread load average class
 */
public class ThreadLocalAverageTest {

    private static ThreadLoadAverage threadLoadAverage;
    private static final int MAX_LOOPS = 10;

    @BeforeClass
    public static void oneTimeSetUp() {
        threadLoadAverage = new ThreadLoadAverage("load-test");
        threadLoadAverage.start();
    }

    @AfterClass
    public static void oneTimeTearDown() {
        threadLoadAverage.stop();
    }

    @Test
    public void testThreadLoadAverage() throws InterruptedException {
        for (int i = 0; i < MAX_LOOPS; i++) {
            System.out.println("Thread load average=" + threadLoadAverage.getOneMinuteLoad() +
                    ", " + threadLoadAverage.getFiveMinuteLoad() +
                    ", " + threadLoadAverage.getFifteenMinuteLoad());
            Thread.sleep(1000);
        }
    }
}
