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

    private double calcFreqDouble = 5.0;
    private long calcFreqMillis = 5000;

    public abstract double getCount();
    public abstract void tickCallback();

    protected Thread loadCalcThread;
    private boolean running = false;

    public LoadAverage(String threadName) {
        loadCalcThread = new Thread(this, threadName);
    }

    public void start() {
        running = true;
        loadCalcThread.start();
    }

    public void stop() {
        running = false;
    }

    public void run() {
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
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
