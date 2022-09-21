/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.deamons.filewatcher;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.registries.ServicesRegistry;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.StatefulJob;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background job that executes the specified {@link FileMonitor}.
 * 
 * @author Sergiy Shyrkov
 */
public class FileMonitorJob implements StatefulJob {

    private static final Logger logger = LoggerFactory.getLogger(FileMonitorJob.class);

    /**
     * Schedules a periodical execution of the specified monitor as a background job.
     * 
     * @param jobName
     *            the background job name to use
     * @param interval
     *            the execution interval in milliseconds
     * @param monitor
     *            the monitor to schedule execution for
     */
    public static void schedule(String jobName, long interval, FileMonitor monitor) {
        JobDetail jobDetail = new JobDetail(jobName, Scheduler.DEFAULT_GROUP, FileMonitorJob.class);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("fileMonitor", monitor);
        jobDetail.setJobDataMap(jobDataMap);

        Trigger trigger = new SimpleTrigger(jobName + "_Trigger", Scheduler.DEFAULT_GROUP,
                SimpleTrigger.REPEAT_INDEFINITELY, interval);
        // not persisted Job and trigger
        trigger.setVolatility(true);

        jobDetail.setRequestsRecovery(false);
        jobDetail.setDurability(false);
        jobDetail.setVolatility(true);

        try {
            ServicesRegistry.getInstance().getSchedulerService().getRAMScheduler()
                    .deleteJob(jobName, Scheduler.DEFAULT_GROUP);
        } catch (SchedulerException e) {
            logger.warn("Unable to delete the job " + jobName + ". Cause: " + e.getMessage());
        }
        try {
            ServicesRegistry.getInstance().getSchedulerService().getRAMScheduler().scheduleJob(jobDetail, trigger);
        } catch (SchedulerException je) {
            logger.error("Error while scheduling file monitor job " + jobName, je);
        }

    }

    /**
     * Unschedules the execution of the specified background job.
     * 
     * @param jobName
     *            the background job name to stop and delete
     */
    public static void unschedule(String jobName) {
        if (!JahiaContextLoaderListener.isRunning()) {
            return;
        }
        try {
            ServicesRegistry.getInstance().getSchedulerService().getRAMScheduler()
                    .deleteJob(jobName, Scheduler.DEFAULT_GROUP);
        } catch (SchedulerException e) {
            logger.warn("Unable to delete the job " + jobName + ". Cause: " + e.getMessage());
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        FileMonitor monitor = (FileMonitor) jobDataMap.get("fileMonitor");
        monitor.run();
    }
}
