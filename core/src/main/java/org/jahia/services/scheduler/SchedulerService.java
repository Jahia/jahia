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

import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.JahiaService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Jahia background task scheduling and management service.
 * 
 * @author Sergiy Shyrkov
 */
public class SchedulerService extends JahiaService implements JahiaAfterInitializationService {

    /**
     * Jahia Spring factory bean that creates, but does not start Quartz scheduler instance. So the instance remain in standby mode until
     * the scheduler is explicitly started.
     * 
     * @author Sergiy Shyrkov
     */
    public static class JahiaSchedulerFactoryBean extends SchedulerFactoryBean {

        @Override
        public void start() {
            // do nothing
        }
    }

    public static final String INSTANT_TRIGGER_GROUP = "instant";

    static Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    private static final Map<Pattern, Long> PURGE_ALL_STRATEGY = Collections.singletonMap(
            Pattern.compile(".*"), Long.valueOf(0));

    public static final String SYSTEM_JOB_GROUP = "system";

    private Scheduler ramScheduler = null;

    private Scheduler scheduler = null;

    private ThreadLocal<List<JobDetail>> scheduledAtEndOfRequest = new ThreadLocal<List<JobDetail>>();

    public Integer deleteAllCompletedJobs() throws SchedulerException {
        return deleteAllCompletedJobs(PURGE_ALL_STRATEGY, true);
    }

    public Integer deleteAllCompletedJobs(Map<Pattern, Long> purgeStrategy,
            boolean purgeWithNoEndDate) throws SchedulerException {
        logger.info("Start looking for completed jobs");
        int deletedCount = 0;

        for (String jobGroup : scheduler.getJobGroupNames()) {
            String[] jobNames = scheduler.getJobNames(jobGroup);
            logger.info("Processing job group {} with {} jobs", jobGroup, jobNames.length);
            for (String jobName : jobNames) {
                logger.debug("Checking job {}.{}", jobGroup, jobName);
                if (ArrayUtils.isEmpty(scheduler.getTriggersOfJob(jobName, jobGroup))) {
                    Long age = getAge(jobName, jobGroup, purgeStrategy);
                    if (age != null && age.longValue() >= 0) {
                        JobDetail job = scheduler.getJobDetail(jobName, jobGroup);
                        if (job == null) {
                            logger.warn("Unable to find job {}.{}", jobGroup, jobName);
                            continue;
                        }
                        String status = job.getJobDataMap().getString(JOB_STATUS);
                        if (STATUS_SUCCESSFUL.equals(status) || STATUS_FAILED.equals(status)
                                || STATUS_CANCELED.equals(status)) {
                            Long ended = job.getJobDataMap().containsKey(JOB_END) ? job
                                    .getJobDataMap().getLongFromString(JOB_END) : null;
                            if (ended != null && (System.currentTimeMillis() - ended > age)
                                    || ended == null && purgeWithNoEndDate) {
                                logger.debug("Job {} matches purge policy. Deleting it.",
                                        job.getFullName());
                                try {
                                    scheduler.deleteJob(jobName, jobGroup);
                                    deletedCount++;
                                } catch (SchedulerException e) {
                                    logger.warn("Error deleting job " + jobGroup + "." + jobName, e);
                                }
                            }
                        }
                    }
                }
            }

        }

        logger.info("Removed {} completed jobs", deletedCount);

        return deletedCount;
    }

    protected Long getAge(String jobName, String jobGroup, Map<Pattern, Long> purgeStrategy) {
        Long expiration = null;
        String key = jobGroup + "." + jobName;
        for (Map.Entry<Pattern, Long> purgeEntry : purgeStrategy.entrySet()) {
            if (purgeEntry.getKey().matcher(key).matches()) {
                expiration = purgeEntry.getValue();
                break;
            }
        }

        return expiration;
    }

    public List<JobDetail> getAllActiveJobs() throws SchedulerException {
        List<JobDetail> l = new LinkedList<JobDetail>();
        for (String group : scheduler.getTriggerGroupNames()) {
            l.addAll(getAllActiveJobs(group));
        }
        return l;
    }

    public List<JobDetail> getAllActiveJobs(String triggerGroup) throws SchedulerException {

        String group = StringUtils.isNotEmpty(triggerGroup) ? triggerGroup
                : Scheduler.DEFAULT_GROUP;

        String[] trigs = scheduler.getTriggerNames(group);
        if (trigs == null || trigs.length == 0) {
            return Collections.emptyList();
        }
        List<JobDetail> all = new LinkedList<JobDetail>();

        for (String name : trigs) {
            Trigger t = scheduler.getTrigger(name, group);
            if (t != null && !SYSTEM_JOB_GROUP.equals(t.getJobGroup())) {
                JobDetail jd = scheduler.getJobDetail(t.getJobName(), t.getJobGroup());
                if (jd != null) {
                    if (STATUS_EXECUTING.equals(jd.getJobDataMap().getString(JOB_STATUS))) {
                        all.add(jd);
                    }
                }
            }
        }

        return all;
    }

    public List<JobDetail> getAllJobs() throws SchedulerException {
        List<JobDetail> l = new LinkedList<JobDetail>();
        for (String group : scheduler.getJobGroupNames()) {
            l.addAll(getAllJobs(group));
        }
        return l;
    }

    public List<JobDetail> getAllJobs(String groupname) throws SchedulerException {
        return getAllJobs(groupname, false);
    }

    public List<JobDetail> getAllJobs(String groupname, boolean useRamScheduler)
            throws SchedulerException {

        String group = StringUtils.isNotEmpty(groupname) ? groupname : Scheduler.DEFAULT_GROUP;

        List<JobDetail> all = new LinkedList<JobDetail>();
        for (String process : getScheduler(useRamScheduler).getJobNames(group)) {
            all.add(getScheduler(useRamScheduler).getJobDetail(process, group));
        }
        return all;
    }

    public Scheduler getRAMScheduler() {
        return ramScheduler;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    protected Scheduler getScheduler(boolean isRamScheduler) {
        return isRamScheduler ? ramScheduler : scheduler;
    }

    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        try {
            ramScheduler.start();

            if (settingsBean.isProcessingServer()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Starting scheduler...\n instanceId:"
                            + scheduler.getMetaData().getSchedulerInstanceId() + " instanceName:"
                            + scheduler.getMetaData().getSchedulerName() + "\n"
                            + scheduler.getMetaData().getSummary());
                }

                scheduler.start();
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            throw new JahiaInitializationException(e.getMessage(), e);
        }
    }

    public void scheduleJobNow(JobDetail jobDetail) throws SchedulerException {
        JobDataMap data = jobDetail.getJobDataMap();
        SimpleTrigger trigger = new SimpleTrigger(jobDetail.getName() + "_Trigger",
                INSTANT_TRIGGER_GROUP);
        trigger.setVolatility(jobDetail.isVolatile());// volatility of trigger
                                                      // depending of the
                                                      // jobdetail's volatility

        data.put(JOB_STATUS, STATUS_ADDED);
        if (logger.isDebugEnabled()) {
            logger.debug("schedule job " + jobDetail.getName() + " volatile("
                    + jobDetail.isVolatile() + ") @ " + new Date(System.currentTimeMillis()));
        }
        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void scheduleJobAtEndOfRequest(JobDetail jobDetail) throws SchedulerException {
        List<JobDetail> jobList = scheduledAtEndOfRequest.get();
        if (jobList == null) {
            jobList = new ArrayList<JobDetail>();
            scheduledAtEndOfRequest.set(jobList);
        }
        jobList.add(jobDetail);
    }

    public void triggerEndOfRequest() {
        List<JobDetail> jobList = scheduledAtEndOfRequest.get();
        scheduledAtEndOfRequest.set(null);
        if (jobList != null) {
            for (JobDetail detail : jobList) {
                try {
                    scheduleJobNow(detail);
                } catch (SchedulerException e) {
                    logger.error("Cannot schedule job", e);
                }
            }
        }

    }

    public void setRamScheduler(Scheduler ramscheduler) {
        this.ramScheduler = ramscheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void start() throws JahiaInitializationException {

        try {
            ramScheduler.addSchedulerListener(new JahiaSchedulerListener(ramScheduler));
            scheduler.addSchedulerListener(new JahiaSchedulerListener(scheduler));

            ramScheduler.addGlobalJobListener(new JahiaJobListener(true));
            scheduler.addGlobalJobListener(new JahiaJobListener(false));

            // new LoggingJobHistoryPlugin().initialize("LoggingJobListener", scheduler);
            // new LoggingJobHistoryPlugin().initialize("RAMLoggingJobListener", ramScheduler);
        } catch (SchedulerException se) {
            if (se.getUnderlyingException() != null) {
                throw new JahiaInitializationException(
                        "Error while initializing scheduler service", se.getUnderlyingException());
            } else {
                throw new JahiaInitializationException(
                        "Error while initializing scheduler service", se);
            }
        }
    }

    public void stop() {
        if (scheduler == null || ramScheduler == null) {
            return;
        }
        try {
            scheduler.shutdown(true);
            ramScheduler.shutdown(true);
            scheduler = null;
            ramScheduler = null;
        } catch (Exception se) {
            logger.error(se.getMessage(), se);
        }
    }

}
