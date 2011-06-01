package org.jahia.utils;

import org.slf4j.Logger;

/**
 * Load average class based on active thread count.
 */
public class ThreadLoadAverage extends LoadAverage {

    private static transient Logger logger = org.slf4j.LoggerFactory.getLogger(ThreadLoadAverage.class);

    public ThreadLoadAverage(String threadName) {
        super(threadName);
    }

    @Override
    public double getCount() {
        return (double) Thread.activeCount();
    }

    public void tickCallback() {
        if (oneMinuteLoad > 2.0) {
            logger.info("Jahia Thread Load = " + oneMinuteLoad + " " + fiveMinuteLoad + " " + fifteenMinuteLoad);
        }
    }

}
