package org.jahia.utils;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.slf4j.Logger;

/**
 * Tomcat request load average tool.
 */
public class RequestLoadAverage extends LoadAverage {

    private static transient Logger logger = org.slf4j.LoggerFactory.getLogger(RequestLoadAverage.class);
    private static transient RequestLoadAverage instance = null;

    public RequestLoadAverage(String threadName) {
        super(threadName);
        instance = this;
    }

    public static RequestLoadAverage getInstance() {
        return instance;
    }

    @Override
    public double getCount() {
        return (double) JahiaContextLoaderListener.getRequestCount();
    }

    @Override
    public void tickCallback() {
        if (oneMinuteLoad > 2.0) {
            logger.info("Jahia Request Load = " + oneMinuteLoad + " " + fiveMinuteLoad + " " + fifteenMinuteLoad);
        }
    }
}
