/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.scheduler;

import static org.jahia.services.scheduler.BackgroundJob.*;

import org.apache.commons.lang.time.FastDateFormat;
import org.quartz.*;
import org.quartz.listeners.JobListenerSupport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Job listener that does status changes and logging.
 *
 * @author Sergiy Shyrkov
 */
class JahiaJobListener extends JobListenerSupport {

    private static final FastDateFormat DF = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS");

    private static final String MSG_FINISHED = "Background job {} (of type {}) finished with status '{}' in {} {}";

    private static final String MSG_STARTED = "Background job {} (of type {}) started @ {}";

    private final Map<String, JobListener> jobListeners = new HashMap<>();

    private boolean isRamScheduler;

    public JahiaJobListener(boolean isRamScheduler) {
        super();
        this.isRamScheduler = isRamScheduler;
    }

    public String getName() {
        return isRamScheduler ? "JahiaRAMJobListener" : "JahiaJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext ctx) {
        try {
            long now = System.currentTimeMillis();
            JobDetail jobDetail = ctx.getJobDetail();
            if (SchedulerService.logger.isInfoEnabled()) {
                String[] params = new String[] { jobDetail.getName(), jobDetail.getGroup(),
                        DF.format(now) };
                if (isRamScheduler) {
                    SchedulerService.logger.debug(MSG_STARTED, params);
                } else {
                    SchedulerService.logger.info(MSG_STARTED, params);
                }
            }
            // set start time and status
            jobDetail.getJobDataMap().putAsString(JOB_BEGIN,
                    ctx.getFireTime() != null ? ctx.getFireTime().getTime() : now);
            jobDetail.getJobDataMap().put(JOB_STATUS, STATUS_EXECUTING);

            // update data
            ctx.getScheduler().addJob(jobDetail, true);
        } catch (SchedulerException e) {
            SchedulerService.logger.warn("Cannot update job", e);
        }

        synchronized (jobListeners) {
            for (JobListener jobListener : jobListeners.values()) {
                jobListener.jobToBeExecuted(ctx);
            }
        }
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext ctx) {
        synchronized (jobListeners) {
            for (JobListener jobListener : jobListeners.values()) {
                jobListener.jobExecutionVetoed(ctx);
            }
        }
    }

    @Override
    public void jobWasExecuted(JobExecutionContext ctx, JobExecutionException jobException) {
        JobDataMap jobData = ctx.getJobDetail().getJobDataMap();
        String status = STATUS_SUCCESSFUL;
        if (jobException != null) {
            status = STATUS_FAILED;
            jobData.put(JOB_MESSAGE, jobException.getMessage());
        }
        long duration = ctx.getJobRunTime();

        // set end time, duration and status
        jobData.putAsString(JOB_END, ctx.getFireTime().getTime() + ctx.getJobRunTime());
        jobData.putAsString(JOB_DURATION, duration);
        jobData.put(JOB_STATUS, ctx.getTrigger().getNextFireTime() != null ? STATUS_SCHEDULED
                : status);

        if (SchedulerService.logger.isInfoEnabled()) {
            String[] params = new String[] { ctx.getJobDetail().getName(),
                    ctx.getJobDetail().getGroup(), status,
                    String.valueOf(duration < 1000 ? duration : duration / 1000),
                    duration < 1000 ? "ms" : "sec" };
            if (isRamScheduler) {
                SchedulerService.logger.debug(MSG_FINISHED, params);
            } else {
                SchedulerService.logger.info(MSG_FINISHED, params);
            }
        }

        synchronized (jobListeners) {
            for (JobListener jobListener : jobListeners.values()) {
                jobListener.jobWasExecuted(ctx, jobException);
            }
        }
    }

    void addJobListener(JobListener jobListener) {
        synchronized (jobListeners) {
            jobListeners.put(jobListener.getName(), jobListener);
        }
    }

    void removeJobListener(String name) {
        synchronized (jobListeners) {
            jobListeners.remove(name);
        }
    }
}
