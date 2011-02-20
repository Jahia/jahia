package org.jahia.utils;

import org.jahia.bin.listeners.JahiaContextLoaderListener;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: 20.02.11
 * Time: 18:00
 * To change this template use File | Settings | File Templates.
 */
public class RequestLoadAverage extends LoadAverage {

    public RequestLoadAverage(String threadName) {
        super(threadName);
    }

    @Override
    public double getCount() {
        return (double) JahiaContextLoaderListener.getRequestCount();
    }
}
