package org.jahia.utils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LimiterExecutor}
 *
 * @author jkevan
 */
public class LimiterExecutorTest {

    @Mock
    private Runnable mockCallback;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String TEST_KEY = "testKey";
    private static final String TEST_KEY_2 = "testKey2";
    private static final long SHORT_INTERVAL = 100; // 100ms
    private static final long LONG_INTERVAL = 1000; // 1s

    @Before
    public void setup() throws Exception {
        // Initialize mocks
        MockitoAnnotations.initMocks(this);

        // Clear the static map before each test to ensure test isolation
        Field mapField = LimiterExecutor.class.getDeclaredField("lastExecuteOncePerInterval");
        mapField.setAccessible(true);
        ((Map<?, ?>) mapField.get(null)).clear();
    }

    @Test
    public void testFirstExecutionAlwaysRuns() {
        LimiterExecutor.executeOncePerInterval(TEST_KEY, SHORT_INTERVAL, mockCallback);
        verify(mockCallback, times(1)).run();
    }

    @Test
    public void testExecutionSkippedWithinInterval() {
        LimiterExecutor.executeOncePerInterval(TEST_KEY, SHORT_INTERVAL, mockCallback);
        LimiterExecutor.executeOncePerInterval(TEST_KEY, SHORT_INTERVAL, mockCallback);
        verify(mockCallback, times(1)).run();
    }

    @Test
    public void testExecutionRunsAfterIntervalElapsed() throws InterruptedException {
        LimiterExecutor.executeOncePerInterval(TEST_KEY, SHORT_INTERVAL, mockCallback);
        Thread.sleep(SHORT_INTERVAL + 50); // Adding buffer time
        LimiterExecutor.executeOncePerInterval(TEST_KEY, SHORT_INTERVAL, mockCallback);
        verify(mockCallback, times(2)).run();
    }

    @Test
    public void testMultipleKeysTrackedIndependently() {
        LimiterExecutor.executeOncePerInterval(TEST_KEY, SHORT_INTERVAL, mockCallback);
        LimiterExecutor.executeOncePerInterval(TEST_KEY_2, SHORT_INTERVAL, mockCallback);
        verify(mockCallback, times(2)).run();
    }

    @Test
    public void testDifferentIntervals() throws InterruptedException {
        Runnable callback1 = mock(Runnable.class);
        Runnable callback2 = mock(Runnable.class);

        // Execute with different intervals
        LimiterExecutor.executeOncePerInterval(TEST_KEY, SHORT_INTERVAL, callback1);
        LimiterExecutor.executeOncePerInterval(TEST_KEY_2, LONG_INTERVAL, callback2);

        // Wait for short interval to elapse
        Thread.sleep(SHORT_INTERVAL + 50);

        // First key should execute again, second key should not
        LimiterExecutor.executeOncePerInterval(TEST_KEY, SHORT_INTERVAL, callback1);
        LimiterExecutor.executeOncePerInterval(TEST_KEY_2, LONG_INTERVAL, callback2);

        // Verify first callback executed twice, second callback executed once
        verify(callback1, times(2)).run();
        verify(callback2, times(1)).run();
    }

    @Test
    public void testConcurrentExecution() throws InterruptedException {
        final int THREAD_COUNT = 10;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch finishLatch = new CountDownLatch(THREAD_COUNT);
        final AtomicInteger executionCount = new AtomicInteger(0);

        Runnable countingCallback = () -> executionCount.incrementAndGet();

        // Create multiple threads all trying to execute the same operation at once
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                try {
                    startLatch.await(); // Wait for start signal
                    LimiterExecutor.executeOncePerInterval(TEST_KEY, LONG_INTERVAL, countingCallback);
                    finishLatch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        // Release all threads at once
        startLatch.countDown();

        // Wait for all threads to finish
        assertTrue(finishLatch.await(5, TimeUnit.SECONDS));

        // Only one thread should have executed the callback
        assertEquals(1, executionCount.get());
    }

    @Test
    public void testNullKeyRejection() {
        expectedException.expect(NullPointerException.class);
        LimiterExecutor.executeOncePerInterval(null, SHORT_INTERVAL, mockCallback);
    }

    @Test
    public void testNullCallbackRejection() {
        expectedException.expect(NullPointerException.class);
        LimiterExecutor.executeOncePerInterval(TEST_KEY, SHORT_INTERVAL, null);
    }

    @Test
    public void testNegativeIntervalRejection() {
        expectedException.expect(IllegalArgumentException.class);
        LimiterExecutor.executeOncePerInterval(TEST_KEY, -100, mockCallback);
    }

    @Test
    public void testZeroIntervalRejection() {
        expectedException.expect(IllegalArgumentException.class);
        LimiterExecutor.executeOncePerInterval(TEST_KEY, 0, mockCallback);
    }
}