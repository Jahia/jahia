/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
 * 
 * @author Sergiy Shyrkov
 */
public class NodeVersionHistoryListener extends DefaultEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NodeVersionHistoryListener.class);

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
}
