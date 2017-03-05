/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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