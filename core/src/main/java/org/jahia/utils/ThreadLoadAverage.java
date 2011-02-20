package org.jahia.utils;

/**
 * Load average class based on active thread count.
 */
public class ThreadLoadAverage extends LoadAverage {

    public ThreadLoadAverage(String threadName) {
        super(threadName);
    }

    @Override
    public double getCount() {
        return (double) Thread.activeCount();
    }
}
