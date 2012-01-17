/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.scheduler;

import static org.jahia.services.scheduler.BackgroundJob.*;

import org.apache.commons.lang.time.FastDateFormat;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.listeners.JobListenerSupport;

/**
 * Job listener that does status changes and logging.
 * 
 * @author Sergiy Shyrkov
 */
class JahiaJobListener extends JobListenerSupport {

    private static final FastDateFormat DF = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS");

    private static final String MSG_FINISHED = "Background job {} (of type {}) finished with status '{}' in {} {}";

    private static final String MSG_STARTED = "Background job {} (of type {}) started @ {}";

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
    }
}