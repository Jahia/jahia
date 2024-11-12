/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils;

import org.jahia.tools.jvm.ThreadMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This class makes it easy to calculate a load average, using an average calculation like the following formula:
 * load(t) = load(t – 1) e^(-5/60m) + n (1 – e^(-5/60m))
 * where n = what we are evaluating over time (number of active threads, requests, etc...)
 * and m = time in minutes over which to perform the average
 *
 */
@Deprecated(forRemoval = true)
public abstract class LoadAverage implements Runnable {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected double oneMinuteLoad = 0.0;
    protected double fiveMinuteLoad = 0.0;
    protected double fifteenMinuteLoad = 0.0;

    protected long calcFreqMillis = 5000;
    private double loggingTriggerValue;
    private double threadDumpTriggerValue;

    /**
     * Sets the value above which logging will be triggered for load averages.
     *
     * @param loggingTriggerValue the value above which logging will be triggered
     */
    public void setLoggingTriggerValue(double loggingTriggerValue) {
        this.loggingTriggerValue = loggingTriggerValue;
    }

    public double getLoggingTriggerValue() {
        return loggingTriggerValue;
    }

    /**
     * Sets the frequency, in milliseconds, at which the calculation of averages occurs.
     * @param millisec how many milliseconds between average calculations
     */
    public void setCalcFrequencyInMillisec(long millisec) {
        this.calcFreqMillis = millisec;
    }

    public abstract double getCount();

    public void tickCallback() {
        if (oneMinuteLoad > getLoggingTriggerValue()) {
            logger.info(getInfo());
            if (isThreadDumpOnHighLoad() && oneMinuteLoad > getThreadDumpTriggerValue()) {
                ThreadMonitor.getInstance().dumpThreadInfo(false, true);
            }
        }
    }

    public String getInfo() {
        return getDisplayName() + " = " + oneMinuteLoad + " " + fiveMinuteLoad + " " + fifteenMinuteLoad;
    }

    private final String threadName;
    private boolean running = false;

    private String displayName;

    private boolean threadDumpOnHighLoad;

    private LoadAverageExecutor executor;

    public void setExecutor(LoadAverageExecutor executor) {
        this.executor = executor;
    }

    public LoadAverageExecutor getExecutor() {
        return executor;
    }

    public LoadAverage(String threadName) {
        this.threadName = threadName;
    }

    public void start() {
        if (calcFreqMillis > 0) {
            executor.addLoadAverage(this);
            running = true;
        }
    }

    public void stop() {
        if (running) {
            running = false;
            executor.removeLoadAverage(this);
        }
    }

    public void run() {
        double calcFreqDouble = calcFreqMillis / 1000d;
        double timeInMinutes = 1;
        oneMinuteLoad = oneMinuteLoad * Math.exp(-calcFreqDouble / (60.0 * timeInMinutes)) + getCount() * (1 - Math.exp(-calcFreqDouble / (60.0 * timeInMinutes)));
        timeInMinutes = 5;
        fiveMinuteLoad = fiveMinuteLoad * Math.exp(-calcFreqDouble / (60.0 * timeInMinutes)) + getCount() * (1 - Math.exp(-calcFreqDouble / (60.0 * timeInMinutes)));
        timeInMinutes = 15;
        fifteenMinuteLoad = fifteenMinuteLoad * Math.exp(-calcFreqDouble / (60.0 * timeInMinutes)) + getCount() * (1 - Math.exp(-calcFreqDouble / (60.0 * timeInMinutes)));
        tickCallback();
    }

    public double getOneMinuteLoad() {
        return oneMinuteLoad;
    }

    public double getFiveMinuteLoad() {
        return fiveMinuteLoad;
    }

    public double getFifteenMinuteLoad() {
        return fifteenMinuteLoad;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName != null ? displayName : threadName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the threadDumpOnHighLoad
     */
    public boolean isThreadDumpOnHighLoad() {
        return threadDumpOnHighLoad;
    }

    /**
     * @param threadDumpOnHighLoad the threadDumpOnHighLoad to set
     */
    public void setThreadDumpOnHighLoad(boolean threadDumpOnHighLoad) {
        this.threadDumpOnHighLoad = threadDumpOnHighLoad;
    }

    /**
     * @return the threadDumpTriggerValue
     */
    public double getThreadDumpTriggerValue() {
        return threadDumpTriggerValue > 0 ? threadDumpTriggerValue : loggingTriggerValue;
    }

    /**
     * @param threadDumpTriggerValue the threadDumpTriggerValue to set
     */
    public void setThreadDumpTriggerValue(double threadDumpTriggerValue) {
        this.threadDumpTriggerValue = threadDumpTriggerValue;
    }
}
