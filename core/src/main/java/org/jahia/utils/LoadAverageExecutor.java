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
@Deprecated(since = "8.2.1.0", forRemoval = true)
public class LoadAverageExecutor {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private ScheduledExecutorService executor;
    private final Map<String, ScheduledFuture<?>> schedules = new HashMap<>();

    /**
     * Get or create the executor. This is necessary because in JDK 17+, once an executor
     * is shutdown, it cannot be reused. This method ensures we have a valid executor.
     */
    private synchronized ScheduledExecutorService getValidExecutor() {
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }
        return executor;
    }

    public void addLoadAverage(LoadAverage loadAverage) {
        LOGGER.info("Adding load average: {}", loadAverage.getClass().getName());
        if (schedules.containsKey(loadAverage.getClass().getName())) {
            schedules.get(loadAverage.getClass().getName()).cancel(false);
            schedules.remove(loadAverage.getClass().getName());
        }
        ScheduledFuture<?> scheduledFuture =
                getValidExecutor().scheduleAtFixedRate(loadAverage, 0, loadAverage.calcFreqMillis, TimeUnit.MILLISECONDS);
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
