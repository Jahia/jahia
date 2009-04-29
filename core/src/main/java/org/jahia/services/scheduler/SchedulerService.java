/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
