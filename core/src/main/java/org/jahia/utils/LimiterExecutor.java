package org.jahia.utils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility class to perform limited process execution.
 */
public class LimiterExecutor {

    private LimiterExecutor() {}

    private static final Map<String, Long> lastExecuteOncePerInterval = new ConcurrentHashMap<>();

    /**
     * Executes the given callback if the specified interval has passed since the last execution for the given key.
     * This is useful for preventing excessive calls to a method within a short time frame.
     *
     * @param key      A unique identifier for the operation.
     * @param interval The interval in milliseconds between executions.
     * @param callback The code to execute if the interval has passed.
     */
    public static void executeOncePerInterval(String key, long interval, Runnable callback) {
        // Validate parameters
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(callback, "Callback cannot be null");
        if (interval <= 0) {
            throw new IllegalArgumentException("Interval must be positive");
        }

        long currentTime = System.currentTimeMillis();
        lastExecuteOncePerInterval.compute(key, (k, lastExecutionTime) -> {
            if (lastExecutionTime == null || (currentTime - lastExecutionTime) > interval) {
                callback.run();
                return currentTime; // Update the last execution time
            }
            return lastExecutionTime; // Keep the same last execution time if within interval
        });
    }
}
