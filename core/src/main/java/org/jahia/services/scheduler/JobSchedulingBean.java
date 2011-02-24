/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.scheduler;

import static org.jahia.services.scheduler.BackgroundJob.JOB_STATUS;
import static org.jahia.services.scheduler.BackgroundJob.STATUS_ADDED;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Convenient Spring bean to schedule background RAM as well as persistent jobs.
 * 
 * @author Cédric Mailleux
 * @author Sergiy Shyrkov
 * @since JAHIA 6.5
 */
public class JobSchedulingBean implements InitializingBean {

	private transient static Logger logger = LoggerFactory.getLogger(JobSchedulingBean.class);

	private boolean isRamJob;

	private JobDetail jobDetail;

	private boolean overwriteExisting = false;

	private SchedulerService schedulerService;

	private List<Trigger> triggers = new LinkedList<Trigger>();

	public void afterPropertiesSet() throws Exception {
		if (jobDetail == null) {
			logger.info("No JobDetail data was specified. Skip scheduling job.");
			return;
		}

		if (overwriteExisting) {
			logger.info("Deleting job {}", jobDetail.getFullName());
			getScheduler().deleteJob(jobDetail.getName(), jobDetail.getGroup());
			if (triggers.size() == 1) {
				logger.info("Scheduling {} job {}", isRamJob ? "RAM" : "persistent",
				        jobDetail.getFullName());
				getScheduler().scheduleJob(jobDetail, triggers.get(0));
			} else {
				if (triggers.size() == 0) {
					logger.info("Job has no triggers configured. Only the JobDetail data will be stored.");
				}
				getScheduler().addJob(jobDetail, true);
				for (Trigger trigger : triggers) {
					trigger.setJobName(jobDetail.getName());
					trigger.setJobGroup(jobDetail.getGroup());
					logger.info("Scheduling {} job {} using trigger {}",
					        new String[] { isRamJob ? "RAM" : "persistent",
					                jobDetail.getFullName(), String.valueOf(trigger) });
					getScheduler().scheduleJob(trigger);
				}
			}
		} else {
			if (getScheduler().getJobDetail(jobDetail.getName(), jobDetail.getGroup()) == null) {
				getScheduler().addJob(jobDetail, true);
			} else {
				Trigger[] existingTriggers = getScheduler().getTriggersOfJob(jobDetail.getName(),
				        jobDetail.getGroup());
				if (existingTriggers != null && existingTriggers.length > 0) {
					Map<String, Trigger> newTriggers = new HashMap<String, Trigger>();
					for (Trigger newTrigger : triggers) {
						newTriggers.put(newTrigger.getFullName(), newTrigger);
					}
					boolean doneUnscheduling = false;
					for (Trigger existingTrigger : existingTriggers) {
						if (!newTriggers.containsKey(existingTrigger.getFullName())) {
							logger.info("Removing no longer needed trigger {} for job {}",
							        String.valueOf(existingTrigger), jobDetail.getFullName());
							getScheduler().unscheduleJob(existingTrigger.getName(),
							        existingTrigger.getGroup());
							doneUnscheduling = true;
						} else {
							logger.info("Skip updating existing trigger {} for job {}",
							        String.valueOf(existingTrigger), jobDetail.getFullName());
							newTriggers.remove(existingTrigger.getFullName());
						}
					}
					for (Trigger newTrigger : newTriggers.values()) {
						newTrigger.setJobName(jobDetail.getName());
						newTrigger.setJobGroup(jobDetail.getGroup());
						logger.info(
						        "Scheduling {} job {} using trigger {}",
						        new String[] { isRamJob ? "RAM" : "persistent",
						                jobDetail.getFullName(), String.valueOf(newTrigger) });
						getScheduler().scheduleJob(newTrigger);
					}
					if (doneUnscheduling
					        && newTriggers.isEmpty()
					        && !STATUS_ADDED
					                .equals(jobDetail.getJobDataMap().getString(JOB_STATUS))) {
						jobDetail.getJobDataMap().put(JOB_STATUS, STATUS_ADDED);
						getScheduler().addJob(jobDetail, true);
					}
				}
			}
		}
	}

	protected Scheduler getScheduler() {
		return isRamJob ? schedulerService.getRAMScheduler() : schedulerService.getScheduler();
	}

	public void setJobDetail(JobDetail jobDetail) {
		this.jobDetail = jobDetail;
	}

	public void setOverwriteExisting(boolean overwriteExisting) {
		this.overwriteExisting = overwriteExisting;
	}

	public void setRamJob(boolean ramJob) {
		isRamJob = ramJob;
	}

	public void setSchedulerService(SchedulerService schedulerService) {
		this.schedulerService = schedulerService;
	}

	public void setTrigger(Trigger trigger) {
		this.triggers.add(trigger);
	}

	public void setTriggers(List<Trigger> triggers) {
		this.triggers.addAll(triggers);
	}
}
