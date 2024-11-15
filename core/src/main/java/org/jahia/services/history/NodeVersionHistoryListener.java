/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.history;

import java.util.HashSet;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCR listener for purging the version history of nodes when a site is deleted.
 * TODO (TECH-1834): to be rework and replace by a better system to manage version history of removed nodes
 * @author Sergiy Shyrkov
 */
public class NodeVersionHistoryListener extends DefaultEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NodeVersionHistoryListener.class);

    private static boolean disabled;

    private SchedulerService schedulerService;

    @Override
    public int getEventTypes() {
        return Event.NODE_REMOVED;
    }

    /**
     * Processes a bundle of events to detect if this is a site deletion. If yes, a background maintenance job is started to delete the
     * version histories of site nodes.
     *
     * @param events
     *            a bundle of events to process
     */
    public void onEvent(EventIterator events) {
        if (isDisabled()) {
            return;
        }
        String siteDeleted = null;
        Set<String> ids = new HashSet<String>();
        try {
            while (events.hasNext()) {
                Event ev = events.nextEvent();
                ids.add(ev.getIdentifier());
                if (siteDeleted == null && ev.getPath().startsWith("/sites/")
                        && ev.getPath().lastIndexOf("/") == 6) {
                    siteDeleted = ev.getPath();
                }
            }

            if (siteDeleted != null && !ids.isEmpty()) {
                scheduleJob(siteDeleted, ids);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void scheduleJob(String site, Set<String> ids) {
        JobDetail jobDetail = BackgroundJob.createJahiaJob("Node version history purge for site "
                + site, NodeVersionHistoryJob.class);
        jobDetail.setGroup("Maintenance");
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put(NodeVersionHistoryJob.JOB_SITE, site);
        jobDataMap.put(NodeVersionHistoryJob.JOB_NODE_IDS, ids);

        logger.info("Scheduling node version history purge job for {} nodes in site {}",
                ids.size(), site);

        try {
            schedulerService.scheduleJobAtEndOfRequest(jobDetail);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Injects an instance of {@link SchedulerService}.
     *
     * @param schedulerService
     *            the schedulerService to set
     */
    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    /**
     * Disables or enables the processing of JCR events after site deletion, which triggers the background job for purging of node version
     * histories for the nodes of the deleted site.<br/>
     * Note, please, this method is only used during the execution of tests.
     *
     * @param disabled
     *            <code>true</code> if the listener should be disabled; <code>false</code> otherwise
     * @since 7.1.2.6
     */
    public static synchronized void setDisabled(boolean disabled) {
        NodeVersionHistoryListener.disabled = disabled;
    }

    private static synchronized boolean isDisabled() {
        return disabled;
    }
}
