/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager.impl;

import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.persistence.ModuleInfoPersister;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Base class for module operation processors.
 * 
 * @author Sergiy Shyrkov
 */
abstract class BaseOperationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(BaseOperationProcessor.class);

    private long jobDelay = 2000;

    private String jobGroup;

    private String jobName;

    protected ModuleInfoPersister persister;

    private Scheduler scheduler;

    /**
     * Creates an instance of the job trigger with the specified fire delay.
     * 
     * @param delay
     *            the delay in milliseconds to fire the job
     * @return an instance of the job trigger with the specified fire delay
     */
    protected SimpleTrigger createJobTrigger(long delay) {
        SimpleTrigger trigger = new SimpleTrigger(jobName + "Trigger-" + BackgroundJob.idGen.nextIdentifier(), jobGroup,
                delay > 0 ? new Date(System.currentTimeMillis() + delay) : new Date(), null, 0, 0);
        trigger.setJobName(jobName);
        trigger.setJobGroup(jobGroup);

        return trigger;
    }

    /**
     * Processes all available open operations.
     * 
     * @throws ModuleManagementException
     *             in case of an error
     */
    public void process() {
        while (processSingleOperation()) {
            // perform processing of all available open operations
        }
    }

    /**
     * Checks for the next open operation and processes it.
     * 
     * @return <code>true</code> in case an open operation was started or processed; <code>false</code> if no open operation are found or if
     *         there is another operation in progress already
     * @throws ModuleManagementException
     *             in case of an error
     */
    protected abstract boolean processSingleOperation() throws ModuleManagementException;

    /**
     * Sets the background job delay interval in milliseconds.
     * 
     * @param jobDelay
     *            the background job delay interval in milliseconds
     */
    public void setJobDelay(long jobDelay) {
        this.jobDelay = jobDelay;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Injects an instance of the persistence service.
     * 
     * @param persister
     *            an instance of the persistence service
     */
    public void setPersister(ModuleInfoPersister persister) {
        this.persister = persister;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Schedule a background task for operation processing.
     */
    protected void tryLater() {
        logger.debug("Scheduling background task for processing module operations in {}", jobDelay);
        try {
            if (scheduler.getTriggersOfJob(jobName, jobGroup).length <= 1) {
                scheduler.scheduleJob(createJobTrigger(jobDelay));
            }
        } catch (SchedulerException e) {
            throw new ModuleManagementException(e);
        }
    }
}
