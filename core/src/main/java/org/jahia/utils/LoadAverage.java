/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

/**
 * This class makes it easy to calculate a load average, using an average calculation like the following formula:
 * load(t) = load(t – 1) e^(-5/60m) + n (1 – e^(-5/60m))
 * where n = what we are evaluating over time (number of active threads, requests, etc...)
 * and m = time in minutes over which to perform the average
 */
public abstract class LoadAverage implements Runnable {

    protected double oneMinuteLoad = 0.0;
    protected double fiveMinuteLoad = 0.0;
    protected double fifteenMinuteLoad = 0.0;

    private long calcFreqMillis = 5000;
    private double loggingTriggerValue;

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
    public abstract void tickCallback();

    private Thread loadCalcThread;
    private final String threadName;
    private boolean running = false;

    public LoadAverage(String threadName) {
        this.threadName = threadName;
    }

    public void start() {
        if (calcFreqMillis > 0) {
            loadCalcThread = new Thread(this, threadName);
            loadCalcThread.setDaemon(true);
            running = true;
            loadCalcThread.start();
        }
    }

    public void stop() {
        if (running) {
            running = false;
            loadCalcThread.interrupt();
            try {
                loadCalcThread.join(200);
            } catch (InterruptedException e) {
            }
        }
    }

    public void run() {
        double calcFreqDouble = calcFreqMillis / 1000d;
        while (running) {
            double timeInMinutes = 1;
            oneMinuteLoad = oneMinuteLoad * Math.exp(-calcFreqDouble / (60.0 * timeInMinutes)) + getCount() * (1 - Math.exp(-calcFreqDouble / (60.0 * timeInMinutes)));
            timeInMinutes = 5;
            fiveMinuteLoad = fiveMinuteLoad * Math.exp(-calcFreqDouble / (60.0 * timeInMinutes)) + getCount() * (1 - Math.exp(-calcFreqDouble / (60.0 * timeInMinutes)));
            timeInMinutes = 15;
            fifteenMinuteLoad = fifteenMinuteLoad * Math.exp(-calcFreqDouble / (60.0 * timeInMinutes)) + getCount() * (1 - Math.exp(-calcFreqDouble / (60.0 * timeInMinutes)));
            tickCallback();
            try {
                Thread.sleep(calcFreqMillis);
            } catch (InterruptedException e) {
            }
        }
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
}
