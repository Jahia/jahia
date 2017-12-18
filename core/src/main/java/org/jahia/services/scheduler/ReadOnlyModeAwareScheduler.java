/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.settings.readonlymode.ReadOnlyModeController;
import org.quartz.*;
import org.quartz.spi.JobFactory;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Scheduler wrapper class, that can block some actions depending on read only mode flag
 *
 * Created by Kevan
 */
class ReadOnlyModeAwareScheduler implements Scheduler {

    private Scheduler scheduler;

    private volatile boolean readOnlyMode = false;

    ReadOnlyModeAwareScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setReadOnly(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
    }

    private void checkReadOnlyMode() {
        if (readOnlyMode) {
            ReadOnlyModeController.readOnlyModeViolated("The scheduler is currently in read-only mode and cannot perform any data or state modifications");
        }
    }

    @Override
    public String getSchedulerName() throws SchedulerException {
        return scheduler.getSchedulerName();
    }

    @Override
    public String getSchedulerInstanceId() throws SchedulerException {
        return scheduler.getSchedulerInstanceId();
    }

    @Override
    public SchedulerContext getContext() throws SchedulerException {
        return scheduler.getContext();
    }

    @Override
    public void start() throws SchedulerException {
        scheduler.start();
    }

    @Override
    public void startDelayed(int seconds) throws SchedulerException {
        scheduler.startDelayed(seconds);
    }

    @Override
    public boolean isStarted() throws SchedulerException {
        return scheduler.isStarted();
    }

    @Override
    public void standby() throws SchedulerException {
        scheduler.standby();
    }

    @Override
    public boolean isInStandbyMode() throws SchedulerException {
        return scheduler.isInStandbyMode();
    }

    @Override
    public void shutdown() throws SchedulerException {
        scheduler.shutdown();
    }

    @Override
    public void shutdown(boolean waitForJobsToComplete) throws SchedulerException {
        scheduler.shutdown();
    }

    @Override
    public boolean isShutdown() throws SchedulerException {
        return scheduler.isShutdown();
    }

    @Override
    public SchedulerMetaData getMetaData() throws SchedulerException {
        return scheduler.getMetaData();
    }

    @Override
    public List<?> getCurrentlyExecutingJobs() throws SchedulerException {
        return scheduler.getCurrentlyExecutingJobs();
    }

    @Override
    public void setJobFactory(JobFactory factory) throws SchedulerException {
        scheduler.setJobFactory(factory);
    }

    @Override
    public Date scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
        checkReadOnlyMode();
        return scheduler.scheduleJob(jobDetail, trigger);
    }

    @Override
    public Date scheduleJob(Trigger trigger) throws SchedulerException {
        checkReadOnlyMode();
        return scheduler.scheduleJob(trigger);
    }

    @Override
    public boolean unscheduleJob(String triggerName, String groupName) throws SchedulerException {
        checkReadOnlyMode();
        return scheduler.unscheduleJob(triggerName, groupName);
    }

    @Override
    public Date rescheduleJob(String triggerName, String groupName, Trigger newTrigger) throws SchedulerException {
        checkReadOnlyMode();
        return scheduler.rescheduleJob(triggerName, groupName, newTrigger);
    }

    @Override
    public void addJob(JobDetail jobDetail, boolean replace) throws SchedulerException {
        checkReadOnlyMode();
        scheduler.addJob(jobDetail, replace);
    }

    @Override
    public boolean deleteJob(String jobName, String groupName) throws SchedulerException {
        checkReadOnlyMode();
        return scheduler.deleteJob(jobName, groupName);
    }

    @Override
    public void triggerJob(String jobName, String groupName) throws SchedulerException {
        checkReadOnlyMode();
        scheduler.triggerJob(jobName, groupName);
    }

    @Override
    public void triggerJobWithVolatileTrigger(String jobName, String groupName) throws SchedulerException {
        checkReadOnlyMode();
        scheduler.triggerJobWithVolatileTrigger(jobName, groupName);
    }

    @Override
    public void triggerJob(String jobName, String groupName, JobDataMap data) throws SchedulerException {
        checkReadOnlyMode();
        scheduler.triggerJob(jobName, groupName, data);
    }

    @Override
    public void triggerJobWithVolatileTrigger(String jobName, String groupName, JobDataMap data) throws SchedulerException {
        checkReadOnlyMode();
        scheduler.triggerJobWithVolatileTrigger(jobName, groupName, data);
    }

    @Override
    public void pauseJob(String jobName, String groupName) throws SchedulerException {
        checkReadOnlyMode();
        scheduler.pauseJob(jobName, groupName);
    }

    @Override
    public void pauseJobGroup(String groupName) throws SchedulerException {
        checkReadOnlyMode();
        scheduler.pauseJobGroup(groupName);
    }

    @Override
    public void pauseTrigger(String triggerName, String groupName) throws SchedulerException {
        checkReadOnlyMode();
        scheduler.pauseTrigger(triggerName, groupName);
    }

    @Override
    public void pauseTriggerGroup(String groupName) throws SchedulerException {
        checkReadOnlyMode();
        scheduler.pauseTriggerGroup(groupName);
    }

    @Override
    public void resumeJob(String jobName, String groupName) throws SchedulerException {
        checkReadOnlyMode();
        scheduler.resumeJob(jobName, groupName);
    }

    @Override
    public void resumeJobGroup(String groupName) throws SchedulerException {
        checkReadOnlyMode();
        scheduler.resumeJobGroup(groupName);
    }

    @Override
    public void resumeTrigger(String triggerName, String groupName) throws SchedulerException {
        checkReadOnlyMode();
        scheduler.resumeTrigger(triggerName, groupName);
    }

    @Override
    public void resumeTriggerGroup(String groupName) throws SchedulerException {
        checkReadOnlyMode();
        scheduler.resumeTriggerGroup(groupName);
    }

    @Override
    public void pauseAll() throws SchedulerException {
        checkReadOnlyMode();
        scheduler.pauseAll();
    }

    @Override
    public void resumeAll() throws SchedulerException {
        checkReadOnlyMode();
        scheduler.resumeAll();
    }

    @Override
    public String[] getJobGroupNames() throws SchedulerException {
        return scheduler.getJobGroupNames();
    }

    @Override
    public String[] getJobNames(String groupName) throws SchedulerException {
        return scheduler.getJobNames(groupName);
    }

    @Override
    public Trigger[] getTriggersOfJob(String jobName, String groupName) throws SchedulerException {
        return scheduler.getTriggersOfJob(jobName, groupName);
    }

    @Override
    public String[] getTriggerGroupNames() throws SchedulerException {
        return scheduler.getTriggerGroupNames();
    }

    @Override
    public String[] getTriggerNames(String groupName) throws SchedulerException {
        return scheduler.getTriggerNames(groupName);
    }

    @Override
    public Set<?> getPausedTriggerGroups() throws SchedulerException {
        return scheduler.getPausedTriggerGroups();
    }

    @Override
    public JobDetail getJobDetail(String jobName, String jobGroup) throws SchedulerException {
        return scheduler.getJobDetail(jobName, jobGroup);
    }

    @Override
    public Trigger getTrigger(String triggerName, String triggerGroup) throws SchedulerException {
        return scheduler.getTrigger(triggerName, triggerGroup);
    }

    @Override
    public int getTriggerState(String triggerName, String triggerGroup) throws SchedulerException {
        return scheduler.getTriggerState(triggerName, triggerGroup);
    }

    @Override
    public void addCalendar(String calName, Calendar calendar, boolean replace, boolean updateTriggers) throws SchedulerException {
        checkReadOnlyMode();
        scheduler.addCalendar(calName, calendar, replace, updateTriggers);
    }

    @Override
    public boolean deleteCalendar(String calName) throws SchedulerException {
        checkReadOnlyMode();
        return scheduler.deleteCalendar(calName);
    }

    @Override
    public Calendar getCalendar(String calName) throws SchedulerException {
        return scheduler.getCalendar(calName);
    }

    @Override
    public String[] getCalendarNames() throws SchedulerException {
        return scheduler.getCalendarNames();
    }

    @Override
    public boolean interrupt(String jobName, String groupName) throws UnableToInterruptJobException {
        checkReadOnlyMode();
        return scheduler.interrupt(jobName, groupName);
    }

    @Override
    public void addGlobalJobListener(JobListener jobListener) throws SchedulerException {
        scheduler.addGlobalJobListener(jobListener);
    }

    @Override
    public void addJobListener(JobListener jobListener) throws SchedulerException {
        scheduler.addJobListener(jobListener);
    }

    @Override
    public boolean removeGlobalJobListener(String name) throws SchedulerException {
        return scheduler.removeGlobalJobListener(name);
    }

    @Override
    public boolean removeJobListener(String name) throws SchedulerException {
        return scheduler.removeJobListener(name);
    }

    @Override
    public List<?> getGlobalJobListeners() throws SchedulerException {
        return scheduler.getGlobalJobListeners();
    }

    @Override
    public Set<?> getJobListenerNames() throws SchedulerException {
        return scheduler.getJobListenerNames();
    }

    @Override
    public JobListener getGlobalJobListener(String name) throws SchedulerException {
        return scheduler.getGlobalJobListener(name);
    }

    @Override
    public JobListener getJobListener(String name) throws SchedulerException {
        return scheduler.getJobListener(name);
    }

    @Override
    public void addGlobalTriggerListener(TriggerListener triggerListener) throws SchedulerException {
        scheduler.addGlobalTriggerListener(triggerListener);
    }

    @Override
    public void addTriggerListener(TriggerListener triggerListener) throws SchedulerException {
        scheduler.addTriggerListener(triggerListener);
    }

    @Override
    public boolean removeGlobalTriggerListener(String name) throws SchedulerException {
        return scheduler.removeGlobalTriggerListener(name);
    }

    @Override
    public boolean removeTriggerListener(String name) throws SchedulerException {
        return scheduler.removeTriggerListener(name);
    }

    @Override
    public List<?> getGlobalTriggerListeners() throws SchedulerException {
        return scheduler.getGlobalTriggerListeners();
    }

    @Override
    public Set<?> getTriggerListenerNames() throws SchedulerException {
        return scheduler.getTriggerListenerNames();
    }

    @Override
    public TriggerListener getGlobalTriggerListener(String name) throws SchedulerException {
        return scheduler.getGlobalTriggerListener(name);
    }

    @Override
    public TriggerListener getTriggerListener(String name) throws SchedulerException {
        return scheduler.getTriggerListener(name);
    }

    @Override
    public void addSchedulerListener(SchedulerListener schedulerListener) throws SchedulerException {
        scheduler.addSchedulerListener(schedulerListener);
    }

    @Override
    public boolean removeSchedulerListener(SchedulerListener schedulerListener) throws SchedulerException {
        return scheduler.removeSchedulerListener(schedulerListener);
    }

    @Override
    public List<?> getSchedulerListeners() throws SchedulerException {
        return scheduler.getSchedulerListeners();
    }
}
