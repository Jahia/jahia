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

import static org.jahia.services.scheduler.BackgroundJob.JOB_CREATED;
import static org.jahia.services.scheduler.BackgroundJob.JOB_STATUS;
import static org.jahia.services.scheduler.BackgroundJob.STATUS_ADDED;
import static org.jahia.services.scheduler.BackgroundJob.STATUS_SCHEDULED;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.listeners.SchedulerListenerSupport;

/**
 * Scheduler listener that does job status changes.
 *
 * @author Sergiy Shyrkov
 */
class JahiaSchedulerListener extends SchedulerListenerSupport {

	private Scheduler scheduler;

	public JahiaSchedulerListener(Scheduler scheduler) {
		super();
		this.scheduler = scheduler;
	}

	@Override
	public void jobAdded(JobDetail jobDetail) {
		boolean doUpdate = false;
		if (!jobDetail.getJobDataMap().containsKey(JOB_CREATED)) {
			jobDetail.getJobDataMap().put(JOB_CREATED, new Date());
			doUpdate = true;
		}
		if (!jobDetail.getJobDataMap().containsKey(JOB_STATUS)) {
			jobDetail.getJobDataMap().put(JOB_STATUS, STATUS_ADDED);
			doUpdate = true;
		}

		if (doUpdate) {
			try {
				scheduler.addJob(jobDetail, true);
			} catch (SchedulerException e) {
				SchedulerService.logger.warn("Unable to update status for job " + jobDetail.getFullName()
				        + ". Cause: " + e.getMessage(), e);
			}
		}
	}

	@Override
	public void jobScheduled(Trigger trigger) {
		try {
			JobDetail jobDetail = scheduler.getJobDetail(trigger.getJobName(),
			        trigger.getJobGroup());
			if (jobDetail != null) {
				if (!STATUS_SCHEDULED.equals(jobDetail.getJobDataMap().getString(JOB_STATUS))) {
					jobDetail.getJobDataMap().put(JOB_STATUS, STATUS_SCHEDULED);
					scheduler.addJob(jobDetail, true);
				}
			}
		} catch (SchedulerException e) {
			SchedulerService.logger.warn("Unable to update job status for trigger " + trigger.getFullName()
			        + ". Cause: " + e.getMessage(), e);
		}
	}
}
