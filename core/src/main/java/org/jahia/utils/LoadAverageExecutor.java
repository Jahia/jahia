/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Jerome Blanchard
 *
 */
@Deprecated(forRemoval = true)
public class LoadAverageExecutor {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, ScheduledFuture<?>> schedules = new HashMap<>();

    public void addLoadAverage(LoadAverage loadAverage) {
        LOGGER.info("Adding load average: {}", loadAverage.getClass().getName());
        if (schedules.containsKey(loadAverage.getClass().getName())) {
            schedules.get(loadAverage.getClass().getName()).cancel(false);
            schedules.remove(loadAverage.getClass().getName());
        }
        ScheduledFuture<?> scheduledFuture =
                executor.scheduleAtFixedRate(loadAverage, 0, loadAverage.calcFreqMillis, TimeUnit.MILLISECONDS);
        schedules.put(loadAverage.getClass().getName(), scheduledFuture);
    }

    public void removeLoadAverage(LoadAverage loadAverage) {
        LOGGER.info("Removing load average: {}", loadAverage.getClass().getName());
        if (schedules.containsKey(loadAverage.getClass().getName())) {
            schedules.get(loadAverage.getClass().getName()).cancel(false);
            schedules.remove(loadAverage.getClass().getName());
        }
    }

    public void start() {
        LOGGER.info("Starting load average executor...");
    }

    public void stop() {
        LOGGER.info("Stopping load average executor...");
        try {
            executor.shutdown();
            if (!executor.awaitTermination(1000, TimeUnit.MILLISECONDS)) { //optional *
                LOGGER.info("Load Average Executor did not terminate in the specified time."); //optional *
                List<Runnable> droppedTasks = executor.shutdownNow(); //optional **
                LOGGER.info("Load Average Executor was abruptly shut down. " + droppedTasks.size()
                        + " tasks will not be executed."); //optional **
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        schedules.clear();
    }
}
