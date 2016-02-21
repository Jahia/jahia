/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.deamons.filewatcher;

import org.jahia.registries.ServicesRegistry;
import org.quartz.*;
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
