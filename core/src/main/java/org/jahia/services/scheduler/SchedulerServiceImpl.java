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
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cluster.ClusterListener;
import org.jahia.services.cluster.ClusterMessage;
import org.jahia.services.cluster.ClusterService;
import org.jahia.services.lock.LockService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.AbstractActivationJob;
import org.jgroups.Address;
import org.quartz.*;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.TriggerFiredBundle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * @version $Id$
 */


public class SchedulerServiceImpl extends SchedulerService implements ClusterListener {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(SchedulerServiceImpl.class);

    private static SchedulerServiceImpl singletonInstance;

    private Scheduler scheduler = null;
    private Scheduler ramscheduler = null;
    private ThreadLocal<List<JobDetail>> waitingJobs = new ThreadLocal<List<JobDetail>>();

    private boolean schedulerRunning = false;
    //private List executingProcesses;

    private ClusterService clusterService;
    private LockService lockService;

    private static final String[] JOBTYPES = {"import", "copypaste", "pickercopy", "workflow", "picked", "propagate1", "propagate2", "production"};
    //last job's time
    public long lastJobCompletedTime =0;
    private JobDetail lastCompletedJobDetail = null;
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

    Map<String, Thread> threads = new HashMap<String, Thread>();

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
            scheduler = getScheduler(new File(settingsBean.getSchedulerConfigFile()));
            ramscheduler = getScheduler(new File(settingsBean.getRamSchedulerConfigFile()));
            schedulerRunning = true;
            clusterService.addListener(this);
            scheduler.setJobFactory(new SimpleJobFactory() {
                public Job newJob(TriggerFiredBundle triggerFiredBundle) throws SchedulerException {
                    JobDetail jobDetail = triggerFiredBundle.getJobDetail();
                    JobDataMap data = jobDetail.getJobDataMap();
                    data.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_WAITING);
                    try {
                        scheduler.addJob(jobDetail, true);
                    } catch (SchedulerException e) {
                        logger.warn("Cannot update job",e);
                    }
                    return super.newJob(triggerFiredBundle);
                }
            });
//            executingProcesses = new ArrayList();
            TriggerListener triggerListener = new TriggerListener() {
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

                        threads.put(jobDetail.getName(), Thread.currentThread());

                        data.putAsString(BackgroundJob.JOB_BEGIN, System.currentTimeMillis());//execution begin
                        data.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_RUNNING);//status
                        data.put(BackgroundJob.JOB_SERVER, clusterService.getServerId());

                        // Hack to update datamap inside quartz store
                        scheduler.addJob(jobDetail, true);
                    } catch (SchedulerException e) {
                        logger.warn("Cannot update job",e);
                    }
                }

                public boolean vetoJobExecution(Trigger trigger, JobExecutionContext jobExecutionContext) {
                    // test if trigger is in the JobStore
                    try {
                        // Hack to prevent job execution if deleted
                       Trigger triggerJobStore =  scheduler.getTrigger(trigger.getName(), trigger.getGroup());
                        if(triggerJobStore == null){
                            logger.debug("Trigger["+trigger.getName()+","+ trigger.getGroup()+"] not found in JobStore --> Veto");
                            return true;
                        }
                    } catch (SchedulerException e) {
                        logger.warn("Cannot update job",e);
                    }
                    return false;
                }

                public void triggerMisfired(Trigger trigger) {
                }

                public void triggerComplete(Trigger trigger, JobExecutionContext jobExecutionContext, int i) {
                    lastJobCompletedTime =System.currentTimeMillis();
                    lastCompletedJobDetail = jobExecutionContext.getJobDetail();
                    logger.debug("trigger completed");
                }
            };

            TriggerListener ramTriggerListener = new TriggerListener() {
                public String getName() {
                    return "Jahia ram jobs listener";
                }

                public void triggerFired(Trigger trigger, JobExecutionContext jobExecutionContext) {
                    JobDetail jobDetail = jobExecutionContext.getJobDetail();
                    JobDataMap data = jobDetail.getJobDataMap();

                    threads.put(jobDetail.getName(), Thread.currentThread());

                    data.putAsString(BackgroundJob.JOB_BEGIN, System.currentTimeMillis());//execution begin
                    data.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_RUNNING);//status
                    data.put(BackgroundJob.JOB_SERVER, clusterService.getServerId());

                    try {
                        // Hack to update datamap inside quartz store
                        ramscheduler.addJob(jobDetail, true);
                    } catch (SchedulerException e) {
                        logger.warn("Cannot update job",e);
                    }
                }

                public boolean vetoJobExecution(Trigger trigger, JobExecutionContext jobExecutionContext) {
                    return false;
                }

                public void triggerMisfired(Trigger trigger) {
                }

                public void triggerComplete(Trigger trigger, JobExecutionContext jobExecutionContext, int i) {
                }
            };

            scheduler.addGlobalTriggerListener(triggerListener);
            ramscheduler.addGlobalTriggerListener(ramTriggerListener);
            SchedulerListener schedulerListener = new SchedulerListener() {
                public void jobScheduled(Trigger trigger) {
                    if (!settingsBean.isProcessingServer()) {
                        clusterService.sendMessage(new ClusterMessage(new QueuedJobMessage(trigger)));
                    }
                }

                public void jobUnscheduled(String string, String string1) {
                }

                public void triggerFinalized(Trigger trigger) {
                }

                public void triggersPaused(String string, String string1) {
                }

                public void triggersResumed(String string, String string1) {
                }

                public void jobsPaused(String string, String string1) {
                }

                public void jobsResumed(String string, String string1) {
                }

                public void schedulerError(String string, SchedulerException schedulerException) {
                }

                public void schedulerShutdown() {
                }
            };
            scheduler.addSchedulerListener(schedulerListener);

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

    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    private Scheduler getScheduler(File schedulerConfigFile) throws SchedulerException {
        org.quartz.impl.StdSchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
        if (schedulerConfigFile.exists()) {
            Properties properties = new Properties();
            try {
                properties.load(new BufferedInputStream(new FileInputStream(schedulerConfigFile)));
                if (properties.containsKey("org.quartz.jobStore.isClustered")) {
                    String clusterActivated = getSettingsBean().getPropertiesFile().getProperty("cluster.activated");
                    if (clusterActivated != null) {
                        properties.put("org.quartz.jobStore.isClustered", clusterActivated);
                    }
                }
                schedFact.initialize(properties);
            } catch (IOException e) {
                throw new SchedulerException("Properties file: '" + schedulerConfigFile + "' could not be read.", e);
            }
        } else {
            logger.error("Couldn't find Quartz configuration file, using default configuration....");
        }
        Scheduler scheduler = schedFact.getScheduler();

        return scheduler;
    }

    public void startSchedulers() throws SchedulerException {
        // here we remove the zombies process
        // maybe we can flag them as stopped later?
        try {
            List all = getAllJobsDetails();
            for (Iterator iterator = all.iterator(); iterator.hasNext();) {
                JobDetail jd = (JobDetail) iterator.next();
                JobDataMap data = jd.getJobDataMap();
                //data.clearDirtyFlag();
                if (BackgroundJob.STATUS_RUNNING.equalsIgnoreCase(data.getString(BackgroundJob.JOB_STATUS)) &&
                        (data.getString(BackgroundJob.JOB_SERVER) == null || data.getString(BackgroundJob.JOB_SERVER).equals(clusterService.getServerId()))) {
                    data.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_FAILED);
                    scheduler.addJob(jd, true);
                    unscheduleJob(jd);
                    lockService.purgeLockForContext(jd.getName());
                }
                if (BackgroundJob.STATUS_WAITING.equalsIgnoreCase(data.getString(BackgroundJob.JOB_STATUS)) &&
                        (data.getString(BackgroundJob.JOB_SERVER) == null || data.getString(BackgroundJob.JOB_SERVER).equals(clusterService.getServerId()))) {
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

        logger.debug("Starting scheduler...\n instanceId:"
                + scheduler.getMetaData().getSchedulerInstanceId() +
                " instanceName:" + scheduler.getMetaData().getSchedulerName()
                + "\n" + scheduler.getMetaData().getSummary());
        ramscheduler.start();
        if (settingsBean.isProcessingServer()) {
            scheduler.start();
        }
    }

    public void stat() {
        if (logger.isDebugEnabled()) {
            try {
                List l = getAllJobsDetails();
                for (Iterator iterator = l.iterator(); iterator.hasNext();) {
                    JobDetail jd = (JobDetail) iterator.next();
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

            logger.debug("schedule job " + jobDetail.getName() + " volatile(" + jobDetail.isVolatile() + ") @ " + new Date(System.currentTimeMillis()));
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException se) {
            throw getJahiaException(se);
        }
    }

    public void startRequest() {
        synchronized(waitingJobs){
            waitingJobs.set(new ArrayList<JobDetail>());
        }
    }

    public void endRequest() throws JahiaException {
        synchronized(waitingJobs){
            List<JobDetail> l = waitingJobs.get();
            if (l != null) {
                for (JobDetail jobDetail: l) {
                    unscheduleJob(jobDetail);
                    scheduleJobNow(jobDetail);
                }
            }
        }
    }

    public void scheduleJobAtEndOfRequest(JobDetail jobDetail) {
        synchronized(waitingJobs){
            List<JobDetail> l = waitingJobs.get();
            if (l != null) {
                Iterator<JobDetail> it = l.iterator();
                JobDetail jd ;
                int index = 0;
                while ( it.hasNext() ){
                    index+=1;
                    jd = it.next();
                    if ( jd.getFullName().equals(jobDetail.getFullName()) ){
                        l.set(index-1,jobDetail);
                        return;
                    }
                }
                l.add(jobDetail);
            }
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
            } catch ( Exception t ){
                logger.debug("fireTimeAfter may be null");
            }
            if ( fireTimeDate != null ){
                data.putAsString(BackgroundJob.JOB_SCHEDULED, fireTimeDate.getTime()); //scheduled
                data.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_POOLED);
            } else {
                data.putAsString(BackgroundJob.JOB_SCHEDULED,  System.currentTimeMillis()); //scheduled now
                data.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_WAITING);
            }
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException se) {
            throw getJahiaException(se);
        }
    }

    public void unscheduleJob(JobDetail detail) throws JahiaException {
        unscheduleJob(detail,false);
    }

    public void unscheduleJob(JobDetail detail, boolean ramScheduler) throws JahiaException {
        try {
            Trigger[] trigs ;
            if ( !ramScheduler ){
                trigs = scheduler.getTriggersOfJob(detail.getName(),detail.getGroup());
            } else {
                trigs = ramscheduler.getTriggersOfJob(detail.getName(),detail.getGroup());
            }
            for (int i = 0; i < trigs.length; i++) {
                Trigger trig = trigs[i];
                if ( !ramScheduler ){
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
            Trigger[] trigs = ramscheduler.getTriggersOfJob(detail.getName(),detail.getGroup());
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
     * @param jobName job name
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
            if ( ramScheduler ){
                return ramscheduler.getJobDetail(jobName, groupName);
            } else {
                return scheduler.getJobDetail(jobName, groupName);
            }
        } catch (SchedulerException se) {
            logger.error("Cannot get details for job "+jobName, se);
            return new JobDetail();
        }
    }

    public List getCurrentlyExecutingJobs()
            throws JahiaException {
        if (!schedulerRunning) {
            logger.debug("scheduler is not running!");
            return null;
        }
        try{
            return scheduler.getCurrentlyExecutingJobs();
        } catch (SchedulerException se) {
            logger.debug("error in scheduler", se);
            throw getJahiaException(se);
        }
    }

    public List<JobExecutionContext> getCurrentlyExecutingRamJobs()
            throws JahiaException {
        if (!schedulerRunning) {
            logger.debug("scheduler is not running!");
            return null;
        }
        try{
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
     * @param jobName the job name
     * @param groupName the group name
     * @throws JahiaException
     */
    public void deleteJob(String jobName, String groupName)
            throws JahiaException {
        if(!groupName.equals("RetentionRuleJob")){
            logger.debug("try to delete job:"+jobName+" gn:"+groupName);
        }
        if (!schedulerRunning) {
            return;
        }
        try {
            JobDetail jobDetail = getJobDetail(jobName, groupName);
            if (jobDetail != null && jobDetail.getJobClass().getName().equals(AbstractActivationJob.class.getName())) {
                lockService.purgeLockForContext(jobDetail.getName());
            }
            scheduler.deleteJob(jobName, groupName);
        } catch (SchedulerException se) {
            logger.debug(se);
            throw getJahiaException(se);
        }
    }

    public void interruptJob(String jobName, String groupName) throws JahiaException {
        Thread t = threads.get(jobName);
        if ( t!= null ){
            t.interrupt();
        }
    }

    public String[] getJobNames(String jobGroupName) throws JahiaException {
        return getJobNames(jobGroupName,false);
    }

    public String[] getJobNames(String jobGroupName, boolean ramScheduler) throws JahiaException {
        try {
            if ( ramScheduler ){
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
                Trigger t = scheduler.getTrigger(trigs[n],gn);
                if (t != null && !t.getJobGroup().equals(SYSTEM_JOB_GROUP)) {
                    JobDetail jd = getJobDetail(t.getJobName(), t.getJobGroup());
                    all.add(jd);
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
        return getAllJobsDetails(groupname,false);
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
        return getJobsDetails(user,groupname,false);
    }

    /**
     * A convenient method to Get a list of scheduled and queued jobdetails.<br>
     *
     * @param user      the current jahia user
     * @param groupname (could be empty or null)
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
        List adminsites = getSiteAdminList(user);
        if (!adminsites.isEmpty()) {

            // second we grab the jobdetails with the same sitekeys
            for (Iterator it = all.iterator(); it.hasNext();) {
                JobDetail jd = (JobDetail) it.next();
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
        for (Iterator it = all.iterator(); it.hasNext();) {
            JobDetail jd = (JobDetail) it.next();
            JobDataMap data = jd.getJobDataMap();
            if (data.get("userkey") != null && ((String) data.get("userkey")).equalsIgnoreCase(user.getUserKey())) {
                list.add(jd);
            }
        }
        return list;

    }

    /**
     * internal method to compute average data of previously(persisted) successfully executed process
     * @throws org.jahia.exceptions.JahiaException sthg bad happened
     */
    private void loadData() throws JahiaException {


        String[] process = getJobNames(Scheduler.DEFAULT_GROUP);
        //get all process
        List<JobDetail> all = new ArrayList<JobDetail>();
        if (process.length > 0) {
            for (int n = 0; n < process.length; n++) {
                JobDetail jd = getJobDetail(process[n], Scheduler.DEFAULT_GROUP);
                all.add(jd);
            }
        } else return;
        if (all.size() == 0) return;
        //get successfully executed only
        List<JobDetail> p = new ArrayList<JobDetail>();
        for (Iterator it = all.iterator(); it.hasNext();) {
            JobDetail jd = (JobDetail) it.next();
            JobDataMap data = jd.getJobDataMap();
            if (data.get(BackgroundJob.JOB_STATUS) != null
                    && ((String) data.get(BackgroundJob.JOB_STATUS)).equalsIgnoreCase(BackgroundJob.STATUS_SUCCESSFUL))
                p.add(jd);
        }
        if (p.size() == 0) return;

        //init vars
        Map<String, List<JobDetail>> types = new HashMap<String, List<JobDetail>>();
        typeAverages = new int[JOBTYPES.length][3];
        //set at 0
        for (int i = 0; i < JOBTYPES.length; i++) {
            for (int j = 0; j < 3; j++) {
                typeAverages[i][j] = 0;
            }
        }
        //compute average durations
        // here looping all types

        for (int tcount = 0; tcount < JOBTYPES.length; tcount++) {
            String type = JOBTYPES[tcount];
            logger.debug("computing " + type + " process....");
            List<JobDetail> l = getProcessByType(p, type);
            types.put(type, l);


            int count = 1;
            for (Iterator it = l.iterator(); it.hasNext();) {
                JobDetail jd = (JobDetail) it.next();
                JobDataMap data = jd.getJobDataMap();
                int duration = 0;
                if (data.get(BackgroundJob.JOB_DURATION) != null) {
                    duration = Integer.parseInt((String) data.get(BackgroundJob.JOB_DURATION));
                }
                logger.debug("duration found:" + duration);
                if (typeAverages[tcount][0] == 0) typeAverages[tcount][0] = duration;
                if (duration < typeAverages[tcount][0]) typeAverages[tcount][0] = duration; //best time
                else if (duration > typeAverages[tcount][2]) typeAverages[tcount][2] = duration; //worse time
                //average
                typeAverages[tcount][1] = (typeAverages[tcount][1] / count) + (duration / count);//ignoring decimal
                count++;
            }
            logger.debug(type + " averages:" + typeAverages[tcount][0] + "/" + typeAverages[tcount][1] + "/" + typeAverages[tcount][2]);
        }
    }

    /**
     * to get an average time depending of types of jobs
     *
     * @param type the type
     * @return average time
     */
    public int[] getAverageTimesByType(String type) {
        try {
            loadData();
        } catch (JahiaException e) {
            logger.error("Error while loading data", e);
        }
        int t = 0;
        for (int i = 0; i < JOBTYPES.length; i++) {
            if (JOBTYPES[i].equalsIgnoreCase(type)) t = i;
        }

        int[] r;
        r = new int[3];
        //System.arraycopy(typeAverages[t], 0, r, 0, 3);
        for (int i = 0; i < 3; i++) {
            r[i] = typeAverages[t][i];
        }
        return r;
    }

    /**
     * filter process list by type
     *
     * @param process the process
     * @param type the type
     * @return a list
     */
    private List<JobDetail> getProcessByType(List process, String type) {
        List<JobDetail> v = new ArrayList<JobDetail>();
        for (Iterator it = process.iterator(); it.hasNext();) {
            JobDetail jd = (JobDetail) it.next();
            JobDataMap data = jd.getJobDataMap();
            if (data.get(BackgroundJob.JOB_TYPE) != null
                    && data.get(BackgroundJob.JOB_TYPE).equals(type)) {
                v.add(jd);
            }
        }
        return v;

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
            infos[1] = "" + data.numJobsExecuted();
            infos[2] = "" + data.runningSince();
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
     * @throws org.jahia.exceptions.JahiaException sthg bad happened
     */
    private List<String> getSiteAdminList(JahiaUser user) throws JahiaException {
        List<String> adminsites = new ArrayList<String>();
        Iterator sites = ServicesRegistry.getInstance().getJahiaSitesService().getSites();
        while (sites.hasNext()) {
            JahiaSite site = (JahiaSite) sites.next();
            int siteid = site.getID();
            if (user.isAdminMember(siteid)) adminsites.add(site.getSiteKey());
        }
        return adminsites;
    }

    private int[][] typeAverages;//in-memory process averages

    public void messageReceived(ClusterMessage message) {
        Object o = message.getObject();
        if (o instanceof QueuedJobMessage) {
            QueuedJobMessage m = (QueuedJobMessage) o;
            try {
                scheduler.resumeTrigger(m.trigger.getName(), m.trigger.getGroup());
            } catch (SchedulerException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void memberJoined(Address address) {
    }

    public void memberLeft(Address address) {
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public JobDetail getLastCompletedJobDetail(){
        return lastCompletedJobDetail;
    }

     public long getLastJobCompletedTime(){
        return lastJobCompletedTime;
    }
}
