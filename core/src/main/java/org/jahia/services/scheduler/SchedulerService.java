/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Scheduler;

import java.util.List;

/**
 * @version $Id$
 */
public abstract class SchedulerService extends JahiaService {
    public static final String SYSTEM_JOB_GROUP = "system";

    public static final String INSTANT_TRIGGER_GROUP = "instant";
    public static final String SCHEDULED_TRIGGER_GROUP = "scheduled";
    public static final String REPEATED_TRIGGER_GROUP = "repeated";

    public abstract void startSchedulers() throws SchedulerException;

    public abstract void scheduleJobNow(JobDetail jobDetail)
            throws JahiaException;

    public abstract void startRequest();

    public abstract void endRequest()
            throws JahiaException;

    public abstract void scheduleJobAtEndOfRequest(JobDetail jobDetail)
            throws JahiaException;

    public abstract void scheduleJob(JobDetail jobDetail, Trigger trigger)
            throws JahiaException;

    public abstract void unscheduleJob(JobDetail detail) throws JahiaException;

    public abstract void scheduleRamJob(JobDetail jobDetail, Trigger trigger)
            throws JahiaException;

    public abstract void unscheduleRamJob(JobDetail detail) throws JahiaException;

    public abstract void unscheduleJob(JobDetail detail, boolean ramScheduler) throws JahiaException;

    /**
     * Delete the given Ram Job
     *
     * @param jobName
     * @param groupName
     * @throws JahiaException
     */
    public abstract void deleteRamJob(String jobName, String groupName)
            throws JahiaException;

    public abstract JobDetail getRamJobDetail(String jobName, String groupName)
            throws JahiaException;

    /**
     * Delete the given Job and associated trigger
     *
     * @param jobName
     * @param groupName
     * @throws JahiaException
     */
    public abstract void deleteJob(String jobName, String groupName)
            throws JahiaException;

    public abstract JobDetail getJobDetail(String jobName, String groupName)
            throws JahiaException;

    public abstract JobDetail getJobDetail(String jobName, String groupName, boolean ramScheduler)
            throws JahiaException;

    public abstract List<JobDetail> getAllActiveJobsDetails() throws JahiaException;

    public abstract List<JobDetail> getAllActiveJobsDetails(String groupname)
            throws JahiaException;

    public abstract List<JobDetail> getAllJobsDetails() throws JahiaException;

    public abstract List<JobDetail> getAllJobsDetails(String groupname)
            throws JahiaException;

    public abstract void interruptJob(String jobName, String groupName) throws JahiaException;

    /**
     * To get a list of scheduled waiting jobdetails.<br>
     *
     * @param user      the current jahia user
     * @param groupname (could be empty or null)
     * @return a list of jobdetails available for this user
     * @throws JahiaException
     */
    public abstract List getJobsDetails(JahiaUser user, String groupname)
            throws JahiaException;

    /**
     * A convenient method to Get a list of scheduled and queued jobdetails.<br>
     *
     * @param user      the current jahia user
     * @param groupname (could be empty or null)
     * @param ramScheduler if true, use the ram scheduler
     * @return a list of jobdetails available for this user
     * @throws JahiaException
     */
    public abstract List getJobsDetails(JahiaUser user, String groupname, boolean ramScheduler)
            throws JahiaException;

    /**
     * Returns the list of currenlty executing jobs
     *
     * @return
     * @throws JahiaException
     */
    public abstract List getCurrentlyExecutingJobs() throws JahiaException;

    /**
     * Returns the list of currenlty executing ram jobs
     *
     * @return
     * @throws JahiaException
     */
    public abstract List<JobExecutionContext> getCurrentlyExecutingRamJobs() throws JahiaException;

    public abstract String[] getJobNames(String jobGroupName)
            throws JahiaException;

    public abstract void triggerJobWithVolatileTrigger(String jobName, String jobGroupName)
            throws JahiaException;

    public abstract String[] getSchedulerInfos(JahiaUser user) throws JahiaException;

    public abstract int[] getAverageTimesByType(String type);

    public abstract Scheduler getScheduler();

    /**
     * to get the last time where a job was executed
     * @return time as long
     */
    public abstract long getLastJobCompletedTime();

     /**
     * to get the last executed job detail
     * @return time as long
     */
    public abstract JobDetail getLastCompletedJobDetail();
}
