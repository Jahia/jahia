/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.settings.SettingsBean;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * Convenient Spring bean to schedule background RAM as well as persistent jobs.
 *
 * @author Cedric Mailleux
 * @author Sergiy Shyrkov
 * @since JAHIA 6.5
 */
public class JobSchedulingBean implements InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulingBean.class);

    private boolean disabled;

    private boolean isRamJob;

    private JobDetail jobDetail;

    private Boolean overwriteExisting;

    private SchedulerService schedulerService;

    private SettingsBean settingsBean;

    private List<Trigger> triggers = new LinkedList<Trigger>();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (disabled || !isEligibleToManageJob()) {
            return;
        }
        if (overwriteExisting == null) {
            overwriteExisting = settingsBean.isDevelopmentMode();
        }

        if (jobDetail == null) {
            LOGGER.info("No JobDetail data was specified. Skip scheduling job.");
            return;
        }

        JobDetail existingJobDetail = getScheduler().getJobDetail(jobDetail.getName(), jobDetail.getGroup());
        if (overwriteExisting || existingJobDetail == null) {
            LOGGER.info("Overwrite existing job: {} or job detail does not exists: {}", overwriteExisting, existingJobDetail == null);
            deleteJob();
            createJob(true);
            scheduleJob(true);
        } else {
            LOGGER.info("Job {} already exists. Syncing triggers.", jobDetail.getFullName());
            syncJobTriggers();
        }
    }

    @Override
    public void destroy() throws Exception {
        LOGGER.info("Destroying job scheduling bean{}", jobDetail.getFullName());
        if (!isEligibleToManageJob()) {
            return;
        }
        if (JahiaContextLoaderListener.isRunning()) {
            if (isRamJob) {
                deleteJob();
            } else {
                unscheduleJob();
            }
        }
    }

    protected Scheduler getScheduler() {
        return isRamJob ? schedulerService.getRAMScheduler() : schedulerService.getScheduler();
    }

    protected String getTriggerInfo(Trigger trigger) {
        return (trigger instanceof CronTrigger && ((CronTrigger) trigger).getCronExpression() != null) ? ("CronTrigger ["
                + ((CronTrigger) trigger).getCronExpression() + "]")
                : trigger.toString();
    }

    protected void syncJobTriggers() throws SchedulerException {
        LOGGER.info("Syncing triggers for job {}", jobDetail.getFullName());
        if (isRamJob) {
            JobDetail detail = schedulerService.getScheduler().getJobDetail(jobDetail.getName(), jobDetail.getGroup());
            if (detail != null) {
                LOGGER.debug("Job {} was found in the persistent scheduler but is RAM now. Deleting it.", jobDetail.getFullName());
                schedulerService.getScheduler().deleteJob(jobDetail.getName(), jobDetail.getGroup());
            }
        }

        Map<String, Trigger> existingTriggers = mapByName(getScheduler().getTriggersOfJob(jobDetail.getName(), jobDetail.getGroup()));
        Map<String, Trigger> newTriggers = mapByName(triggers.toArray(new Trigger[0]));

        for (Map.Entry<String, Trigger> newTrigger : newTriggers.entrySet()) {
            if (!existingTriggers.containsKey(newTrigger.getKey())) {
                createTrigger(newTrigger.getValue());
            }
        }
        for (Map.Entry<String, Trigger> existingTrigger : existingTriggers.entrySet()) {
            Trigger newTrigger = newTriggers.get(existingTrigger.getKey());
            if (newTrigger != null) {
                LOGGER.debug("Trigger {} already exists. Checking if it needs to be updated.", existingTrigger.getKey());
                if (newTrigger instanceof CronTrigger) {
                    String newCron = ((CronTrigger) newTrigger).getCronExpression();
                    String existingCron = ((CronTrigger) existingTrigger.getValue()).getCronExpression();
                    if (!newCron.equals(existingCron)) {
                        newTrigger.setJobName(existingTrigger.getValue().getJobName());
                        newTrigger.setJobGroup(existingTrigger.getValue().getJobGroup());
                        updateTrigger(newTrigger);
                    }
                } else {
                    updateTrigger(newTrigger);
                }
            } else {
                removeTrigger(existingTrigger.getValue());
            }
        }
    }

    protected void createJob(boolean deleteFirst)  throws SchedulerException {
        getScheduler().addJob(jobDetail, deleteFirst);
    }

    protected void scheduleJob(boolean deleteFirst) throws SchedulerException {
        if (deleteFirst) {
            unscheduleJob();
        }
        if (triggers.isEmpty()) {
            LOGGER.info("Job has no triggers configured. Only the JobDetail data will be stored.");
        }
        for (Trigger trigger : triggers) {
            this.createTrigger(trigger);
        }
    }

    protected void deleteJob() throws SchedulerException {
        LOGGER.info("Deleting job {}", jobDetail.getFullName());
        getScheduler().deleteJob(jobDetail.getName(), jobDetail.getGroup());
    }

    protected void unscheduleJob() throws SchedulerException {
        LOGGER.info("Unscheduling job {}", jobDetail.getFullName());
        Trigger[] triggers = getScheduler().getTriggersOfJob(jobDetail.getName(), jobDetail.getGroup());
        for (Trigger trigger : triggers) {
            removeTrigger(trigger);
        }
    }

    private void createTrigger(Trigger trigger) throws SchedulerException {
        trigger.setJobName(jobDetail.getName());
        trigger.setJobGroup(jobDetail.getGroup());
        LOGGER.info("Create trigger for {} job {} using {}", new String[] {
                isRamJob ? "RAM" : "persistent", jobDetail.getFullName(),
                getTriggerInfo(trigger) });
        getScheduler().scheduleJob(trigger);
    }

    private void removeTrigger(Trigger trigger) throws SchedulerException {
        LOGGER.info("Remove trigger for {} job {} using {}", new String[] {
                isRamJob ? "RAM" : "persistent", jobDetail.getFullName(),
                getTriggerInfo(trigger) });
        getScheduler().unscheduleJob(trigger.getName(), trigger.getGroup());
    }

    private void updateTrigger(Trigger trigger) throws SchedulerException {
        LOGGER.info("Update trigger for {} job {} using {}", new String[] {
                isRamJob ? "RAM" : "persistent", jobDetail.getFullName(),
                getTriggerInfo(trigger) });
        getScheduler().rescheduleJob(trigger.getName(), trigger.getGroup(), trigger);
    }

    /**
     * Checks if the scheduler is allowed to manage this type of job, i.e. it is either a RAM job or a persisted job and we are on a
     * processing server.
     *
     * @return Returns <code>true</code> if the scheduler is allowed to manage this type of job
     */
    private boolean isEligibleToManageJob() {
        return (isRamJob || settingsBean.isProcessingServer());
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
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

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    public void setTrigger(Trigger trigger) {
        if (trigger != null) {
            this.triggers.add(trigger);
        }
    }

    public void setTriggers(List<Trigger> triggers) {
        if (triggers != null) {
            this.triggers.addAll(triggers);
        }
    }

    protected Map<String, Trigger> mapByName(Trigger[] triggers) {
        if (triggers == null || triggers.length == 0) {
            return Collections.emptyMap();
        }

        Map<String, Trigger> map = new HashMap<String, Trigger>(triggers.length);
        for (Trigger trg : triggers) {
            map.put(trg.getFullName(), trg);
        }

        return map;
    }

}
