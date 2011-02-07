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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.listeners.TriggerListenerSupport;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;

/**
 * @version $Id$
 */


public class SchedulerServiceImpl extends SchedulerService {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(SchedulerServiceImpl.class);

    private static SchedulerServiceImpl singletonInstance;

    private Scheduler scheduler = null;
    private Scheduler ramscheduler = null;

    private boolean schedulerRunning = false;

    private String serverId;

    protected SchedulerServiceImpl() {
    }

    /**
     * Return the unique service instance. If the instance does not exist,
     * a new instance is created.
     *
     * @return The unique service instance.
     */
    public static SchedulerServiceImpl getInstance() {
        if (singletonInstance == null) {
            synchronized (SchedulerServiceImpl.class) {
                if (singletonInstance == null) {
                    singletonInstance = new SchedulerServiceImpl();
                }
            }
        }
        return singletonInstance;
    }

    /**
     * Initializes the servlet dispatching service with parameters loaded
     * from the Jahia configuration file.
     *
     * @throws JahiaInitializationException thrown in the case of an error
     *                                      during this initialization, that will be treated as a critical error
     *                                      in Jahia and probably stop execution of Jahia once and for all.
     */
    public void start()
            throws JahiaInitializationException {

        try {
            schedulerRunning = true;
            scheduler.setJobFactory(new SimpleJobFactory() {
                public Job newJob(TriggerFiredBundle triggerFiredBundle) throws SchedulerException {
                    JobDetail jobDetail = triggerFiredBundle.getJobDetail();
                    JobDataMap data = jobDetail.getJobDataMap();
                    data.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_WAITING);
                    try {
                        scheduler.addJob(jobDetail, true);
                    } catch (SchedulerException e) {
                        logger.warn("Cannot update job", e);
                    }
                    return super.newJob(triggerFiredBundle);
                }
            });

            TriggerListener triggerListener = new TriggerListenerSupport() {
                public String getName() {
                    return "Jahia jobs listener";
                }

                public void triggerFired(Trigger trigger, JobExecutionContext jobExecutionContext) {
                    try {
                        if (jobExecutionContext.getScheduler().isShutdown()) {
                            return;
                        }

                        JobDetail jobDetail = jobExecutionContext.getJobDetail();
                        JobDataMap data = jobDetail.getJobDataMap();

                        data.putAsString(BackgroundJob.JOB_BEGIN, System.currentTimeMillis());//execution begin
                        data.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_RUNNING);//status
                        data.put(BackgroundJob.JOB_SERVER, serverId);

                        // Hack to update datamap inside quartz store
                        scheduler.addJob(jobDetail, true);
                    } catch (SchedulerException e) {
                        logger.warn("Cannot update job", e);
                    }
                }

                public boolean vetoJobExecution(Trigger trigger, JobExecutionContext jobExecutionContext) {
                    // test if trigger is in the JobStore
                    try {
                        // Hack to prevent job execution if deleted
                        Trigger triggerJobStore = scheduler.getTrigger(trigger.getName(), trigger.getGroup());
                        if (triggerJobStore == null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Trigger[" + trigger.getName() + "," + trigger.getGroup() + "] not found in JobStore --> Veto");
                            }
                            return true;
                        }
                    } catch (SchedulerException e) {
                        logger.warn("Cannot update job", e);
                    }
                    return false;
                }
            };

            TriggerListener ramTriggerListener = new TriggerListenerSupport() {
                public String getName() {
                    return "Jahia ram jobs listener";
                }

                public void triggerFired(Trigger trigger, JobExecutionContext jobExecutionContext) {
                    JobDetail jobDetail = jobExecutionContext.getJobDetail();
                    JobDataMap data = jobDetail.getJobDataMap();

                    data.putAsString(BackgroundJob.JOB_BEGIN, System.currentTimeMillis());//execution begin
                    data.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_RUNNING);//status
                    data.put(BackgroundJob.JOB_SERVER, serverId);

                    try {
                        // Hack to update datamap inside quartz store
                        ramscheduler.addJob(jobDetail, true);
                    } catch (SchedulerException e) {
                        logger.warn("Cannot update job", e);
                    }
                }
            };

            scheduler.addGlobalTriggerListener(triggerListener);
            ramscheduler.addGlobalTriggerListener(ramTriggerListener);
        } catch (SchedulerException se) {
            if (se.getUnderlyingException() != null) {
                throw new JahiaInitializationException(
                        "Error while initializing scheduler service",
                        se.getUnderlyingException());
            } else {
                throw new JahiaInitializationException(
                        "Error while initializing scheduler service",
                        se);
            }
        }
    }

    public void startSchedulers() throws SchedulerException {
        // here we remove the zombies process
        // maybe we can flag them as stopped later?
        try {
            List<JobDetail> all = getAllJobsDetails();
            for (JobDetail jd : all) {
                JobDataMap data = jd.getJobDataMap();
                //data.clearDirtyFlag();
                if (BackgroundJob.STATUS_RUNNING.equalsIgnoreCase(data.getString(BackgroundJob.JOB_STATUS)) &&
                        (data.getString(BackgroundJob.JOB_SERVER) == null || data.getString(BackgroundJob.JOB_SERVER).equals(serverId))) {
                    data.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_FAILED);
                    scheduler.addJob(jd, true);
                    unscheduleJob(jd);
                }
                if (BackgroundJob.STATUS_WAITING.equalsIgnoreCase(data.getString(BackgroundJob.JOB_STATUS)) &&
                        (data.getString(BackgroundJob.JOB_SERVER) == null || data.getString(BackgroundJob.JOB_SERVER).equals(serverId))) {
                    SimpleTrigger trigger = new SimpleTrigger(jd.getName() + "_Trigger", INSTANT_TRIGGER_GROUP);
                    trigger.setVolatility(jd.isVolatile());
                    trigger.setJobName(jd.getName());
                    trigger.setJobGroup(jd.getGroup());
                    scheduler.rescheduleJob(trigger.getName(), trigger.getGroup(), trigger);
                }
            }
        } catch (Exception e) {
            logger.error("Error while starting schedulers", e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Starting scheduler...\n instanceId:"
                    + scheduler.getMetaData().getSchedulerInstanceId() +
                    " instanceName:" + scheduler.getMetaData().getSchedulerName()
                    + "\n" + scheduler.getMetaData().getSummary());
        }
        ramscheduler.start();
        if (settingsBean.isProcessingServer()) {
            scheduler.start();
        }
    }

    public void stat() {
        if (logger.isDebugEnabled()) {
            try {
                List<JobDetail> l = getAllJobsDetails();
                for (JobDetail jd : l) {
                    JobDataMap data = jd.getJobDataMap();
                    if (!data.get("status").equals("successful")) {
                        logger.debug(jd.getName() + " -> " + data.get("status"));
                    }
                }
            } catch (JahiaException e) {
                logger.error("Error while retrieving job details", e);
            }
        }
    }

    public synchronized void stop()
            throws JahiaException {
        try {
            if (schedulerRunning) {
                logger.debug("Shutting down scheduler...");
                scheduler.shutdown(true);
                ramscheduler.shutdown(true);
                scheduler = null;
                ramscheduler = null;
                schedulerRunning = false;
            }
        } catch (SchedulerException se) {
            throw getJahiaException(se);
        }
    }

    public void scheduleJobNow(JobDetail jobDetail) throws JahiaException {

        if (!schedulerRunning) {
            logger.warn("Scheduler hasn't been started, will not schedule Job " + jobDetail.getFullName());
            return;
        }
        try {
            JobDataMap data = jobDetail.getJobDataMap();
            //lastJobCompletedTime=System.currentTimeMillis();
            data.putAsString(BackgroundJob.JOB_SCHEDULED, System.currentTimeMillis()); //scheduled
            SimpleTrigger trigger = new SimpleTrigger(jobDetail.getName() + "_Trigger", INSTANT_TRIGGER_GROUP);
            trigger.setVolatility(jobDetail.isVolatile());//volatility of trigger depending of the jobdetail's volatility

            data.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_WAITING);

            if (logger.isDebugEnabled()) {
                logger.debug("schedule job " + jobDetail.getName() + " volatile(" + jobDetail.isVolatile() + ") @ " + new Date(System.currentTimeMillis()));
            }
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException se) {
            throw getJahiaException(se);
        }
    }

    public void scheduleJob(JobDetail jobDetail, Trigger trigger)
            throws JahiaException {
        if (!schedulerRunning) {
            logger.warn("Scheduler hasn't been started, will not schedule Job " + jobDetail.getFullName());
            return;
        }
        try {
            JobDataMap data = jobDetail.getJobDataMap();
            Date fireTimeDate = null;
            try {
                fireTimeDate = trigger.getFireTimeAfter(null);
            } catch (Exception t) {
                logger.debug("fireTimeAfter may be null");
            }
            if (fireTimeDate != null) {
                data.putAsString(BackgroundJob.JOB_SCHEDULED, fireTimeDate.getTime()); //scheduled
                data.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_POOLED);
            } else {
                data.putAsString(BackgroundJob.JOB_SCHEDULED, System.currentTimeMillis()); //scheduled now
                data.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_WAITING);
            }
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException se) {
            throw getJahiaException(se);
        }
    }

    public void unscheduleJob(JobDetail detail) throws JahiaException {
        unscheduleJob(detail, false);
    }

    public void unscheduleJob(JobDetail detail, boolean ramScheduler) throws JahiaException {
        try {
            Trigger[] trigs;
            if (!ramScheduler) {
                trigs = scheduler.getTriggersOfJob(detail.getName(), detail.getGroup());
            } else {
                trigs = ramscheduler.getTriggersOfJob(detail.getName(), detail.getGroup());
            }
            for (int i = 0; i < trigs.length; i++) {
                Trigger trig = trigs[i];
                if (!ramScheduler) {
                    scheduler.unscheduleJob(trig.getName(), trig.getGroup());
                } else {
                    ramscheduler.unscheduleJob(trig.getName(), trig.getGroup());
                }
            }
        } catch (SchedulerException e) {
            throw getJahiaException(e);
        }
    }

    public void scheduleRamJob(JobDetail jobDetail, Trigger trigger)
            throws JahiaException {
        if (!schedulerRunning) {
            logger.warn("Scheduler hasn't been started, will not schedule Job " + jobDetail.getFullName());
            return;
        }
        try {
            ramscheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException se) {
            throw getJahiaException(se);
        }
    }

    public void unscheduleRamJob(JobDetail detail) throws JahiaException {
        try {
            Trigger[] trigs = ramscheduler.getTriggersOfJob(detail.getName(), detail.getGroup());
            for (int i = 0; i < trigs.length; i++) {
                Trigger trig = trigs[i];
                ramscheduler.unscheduleJob(trig.getName(), trig.getGroup());
            }
        } catch (SchedulerException e) {
            throw getJahiaException(e);
        }
    }

    /**
     * Delete the given Ram Job
     *
     * @param jobName   job name
     * @param groupName group name
     * @throws JahiaException
     */
    public void deleteRamJob(String jobName, String groupName)
            throws JahiaException {
        if (!schedulerRunning) {
            return;
        }
        try {
            ramscheduler.deleteJob(jobName, groupName);
        } catch (SchedulerException se) {
            throw getJahiaException(se);
        }
    }

    public JobDetail getRamJobDetail(String jobName, String groupName)
            throws JahiaException {
        if (!schedulerRunning) {
            return null;
        }
        try {
            return ramscheduler.getJobDetail(jobName, groupName);
        } catch (SchedulerException se) {
            throw getJahiaException(se);
        }
    }

    public JobDetail getJobDetail(String jobName, String groupName)
            throws JahiaException {
        return getJobDetail(jobName, groupName, false);
    }

    public JobDetail getJobDetail(String jobName, String groupName, boolean ramScheduler)
            throws JahiaException {
        if (!schedulerRunning) {
            logger.debug("scheduler is not running!");
            return null;
        }
        try {
            if (ramScheduler) {
                return ramscheduler.getJobDetail(jobName, groupName);
            } else {
                return scheduler.getJobDetail(jobName, groupName);
            }
        } catch (SchedulerException se) {
            logger.error("Cannot get details for job " + jobName, se);
            return new JobDetail();
        }
    }

    @SuppressWarnings("unchecked")
    public List<JobExecutionContext> getCurrentlyExecutingJobs()
            throws JahiaException {
        if (!schedulerRunning) {
            logger.debug("scheduler is not running!");
            return null;
        }
        try {
            return scheduler.getCurrentlyExecutingJobs();
        } catch (SchedulerException se) {
            logger.debug("error in scheduler", se);
            throw getJahiaException(se);
        }
    }

    @SuppressWarnings("unchecked")
    public List<JobExecutionContext> getCurrentlyExecutingRamJobs()
            throws JahiaException {
        if (!schedulerRunning) {
            logger.debug("scheduler is not running!");
            return null;
        }
        try {
            return this.ramscheduler.getCurrentlyExecutingJobs();
        } catch (SchedulerException se) {
            logger.debug("error in scheduler", se);
            throw getJahiaException(se);
        }
    }

    private JahiaException getJahiaException(SchedulerException se) {
        if (se.getUnderlyingException() != null) {
            return new JahiaException("Error while shutting down scheduler service",
                    "Error while shutting down scheduler service",
                    JahiaException.SERVICE_ERROR,
                    JahiaException.ERROR_SEVERITY,
                    se.getUnderlyingException());
        } else {
            return new JahiaException("Error while shutting down scheduler service",
                    "Error while shutting down scheduler service",
                    JahiaException.SERVICE_ERROR,
                    JahiaException.ERROR_SEVERITY,
                    se);
        }
    }


    /**
     * Delete the given Job and associated trigger
     *
     * @param jobName   the job name
     * @param groupName the group name
     * @throws JahiaException
     */
    public boolean deleteJob(String jobName, String groupName)
            throws JahiaException {
        if (logger.isDebugEnabled()) {
			logger.debug("try to delete job: " + jobName + " gn: " + groupName);
        }
        if (!schedulerRunning) {
            return false;
        }
        try {
            final JobDetail jobDetail = scheduler.getJobDetail(jobName, groupName);
            if (jobDetail != null) {
                unscheduleJob(jobDetail);
                return scheduler.deleteJob(jobName, groupName);
            }
        } catch (SchedulerException se) {
            logger.debug(se.getMessage(), se);
            throw getJahiaException(se);
        }
        return false;
    }

    public String[] getJobNames(String jobGroupName) throws JahiaException {
        return getJobNames(jobGroupName, false);
    }

    public String[] getJobNames(String jobGroupName, boolean ramScheduler) throws JahiaException {
        try {
            if (ramScheduler) {
                return ramscheduler.getJobNames(jobGroupName);
            } else {
                return scheduler.getJobNames(jobGroupName);
            }
        } catch (SchedulerException e) {
            throw getJahiaException(e);
        }
    }

    public void triggerJobWithVolatileTrigger(String jobName, String jobGroupName) throws JahiaException {
        try {
            scheduler.triggerJobWithVolatileTrigger(jobName, jobGroupName);
        } catch (SchedulerException e) {
            throw getJahiaException(e);
        }
    }

    public List<JobDetail> getAllActiveJobsDetails() throws JahiaException {
        List<JobDetail> l = new ArrayList<JobDetail>();
        try {
            String[] groups = scheduler.getTriggerGroupNames();
            for (int i = 0; i < groups.length; i++) {
                String group = groups[i];
                l.addAll(getAllActiveJobsDetails(group));
            }
        } catch (SchedulerException e) {
            throw getJahiaException(e);
        }
        return l;
    }

    public List<JobDetail> getAllActiveJobsDetails(String groupname) throws JahiaException {

        String gn = Scheduler.DEFAULT_GROUP;//default

        try {
            if (groupname != null && !groupname.equalsIgnoreCase("")) gn = groupname;
            String[] trigs = scheduler.getTriggerNames(groupname);
            List<JobDetail> all = new ArrayList<JobDetail>();
            if (trigs.length == 0) return all;
            for (int n = 0; n < trigs.length; n++) {
                Trigger t = scheduler.getTrigger(trigs[n], gn);
                if (t != null && !t.getJobGroup().equals(SYSTEM_JOB_GROUP)) {
                    JobDetail jd = getJobDetail(t.getJobName(), t.getJobGroup());
                    if (jd.getJobDataMap() != null) {
                        JobDataMap dataMap = jd.getJobDataMap();
                        if (BackgroundJob.STATUS_RUNNING.equals(dataMap.getString(BackgroundJob.JOB_STATUS))) {
                            all.add(jd);
                        }
                    } else {
                        all.add(jd);
                    }
                }
            }
            return all;
        } catch (SchedulerException e) {
            throw getJahiaException(e);
        }
    }

    public List<JobDetail> getAllJobsDetails() throws JahiaException {
        List<JobDetail> l = new ArrayList<JobDetail>();
        try {
            String[] groups = scheduler.getJobGroupNames();
            for (int i = 0; i < groups.length; i++) {
                String group = groups[i];
                l.addAll(getAllJobsDetails(group));
            }
        } catch (SchedulerException e) {
            throw getJahiaException(e);
        }
        return l;
    }

    public List<JobDetail> getAllJobsDetails(String groupname) throws JahiaException {
        return getAllJobsDetails(groupname, false);
    }

    public List<JobDetail> getAllJobsDetails(String groupname, boolean ramScheduler) throws JahiaException {

        String gn = Scheduler.DEFAULT_GROUP;//default

        if (groupname != null && !groupname.equalsIgnoreCase("")) gn = groupname;
        String[] process = getJobNames(gn, ramScheduler);
        List<JobDetail> all = new ArrayList<JobDetail>();
        if (process.length == 0) return all;
        for (int n = 0; n < process.length; n++) {
            JobDetail jd = getJobDetail(process[n], gn, ramScheduler);
            all.add(jd);
        }
        return all;
    }

    /**
     * A convenient method to Get a list of scheduled and queued jobdetails.<br>
     *
     * @param user      the current jahia user
     * @param groupname (could be empty or null)
     * @return a list of jobdetails available for this user
     * @throws JahiaException
     */
    public List<JobDetail> getJobsDetails(JahiaUser user, String groupname) throws JahiaException {
        return getJobsDetails(user, groupname, false);
    }

    /**
     * A convenient method to Get a list of scheduled and queued jobdetails.<br>
     *
     * @param user         the current jahia user
     * @param groupname    (could be empty or null)
     * @param ramScheduler if true, use the ram scheduler
     * @return a list of jobdetails available for this user
     * @throws JahiaException
     */
    public List<JobDetail> getJobsDetails(JahiaUser user, String groupname, boolean ramScheduler) throws JahiaException {
        if (user == null) throw new IllegalArgumentException("user cannot be null");

        // all jobdetails
        List<JobDetail> all = getAllJobsDetails(groupname, ramScheduler);

        //return the all list since the user is root
        if (user.isRoot() || all.isEmpty()) return all;

        //the user is admin?
        List<JobDetail> list = new ArrayList<JobDetail>();
        boolean isAdminSomewhere = false;

        // get 1st all sitekey of sites where the user is admin member
        List<String> adminsites = getSiteAdminList(user);
        if (!adminsites.isEmpty()) {

            // second we grab the jobdetails with the same sitekeys
            for (JobDetail jd : all) {
                JobDataMap data = jd.getJobDataMap();
                if (data.get("sitekey") != null && adminsites.contains(data.get("sitekey"))) {
                    list.add(jd);
                    isAdminSomewhere = true;
                }
            }
        }
        if (isAdminSomewhere) return list;

        // for all standard users
        // grab only own process
        list = new ArrayList<JobDetail>();
        for (JobDetail jd : all) {
            JobDataMap data = jd.getJobDataMap();
            if (data.get("userkey") != null && ((String) data.get("userkey")).equalsIgnoreCase(user.getUserKey())) {
                list.add(jd);
            }
        }
        return list;

    }

    /**
     * to get some generic info about the scheduler
     *
     * @param user the user
     * @return info about the scheduler
     * @throws JahiaException
     */
    public String[] getSchedulerInfos(JahiaUser user) throws JahiaException {
        String[] infos = new String[20];
        if (!user.isRoot()) return infos;// just in case
        try {
            SchedulerMetaData data = scheduler.getMetaData();
            infos[0] = "" + data.getThreadPoolSize();
            infos[1] = "" + data.getNumberOfJobsExecuted();
            infos[2] = "" + data.getRunningSince();
            infos[3] = "";
        } catch (SchedulerException e) {

            throw getJahiaException(e);

        }
        return infos;
    }

    /**
     * internal method to grab a list of admin sites
     *
     * @param user the user
     * @return List list of administered sites for that user
     * @throws org.jahia.exceptions.JahiaException
     *          sthg bad happened
     */
    private List<String> getSiteAdminList(JahiaUser user) throws JahiaException {
        List<String> adminsites = new ArrayList<String>();
        Iterator<JahiaSite> sites = ServicesRegistry.getInstance().getJahiaSitesService().getSites();
        while (sites.hasNext()) {
            JahiaSite site = (JahiaSite) sites.next();
            int siteid = site.getID();
            if (user.isAdminMember(siteid)) adminsites.add(site.getSiteKey());
        }
        return adminsites;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setRamscheduler(Scheduler ramscheduler) {
        this.ramscheduler = ramscheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

	public void setServerId(String serverId) {
    	this.serverId = serverId;
    }
}
