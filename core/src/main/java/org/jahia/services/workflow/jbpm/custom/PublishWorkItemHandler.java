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
package org.jahia.services.workflow.jbpm.custom;

import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.PublicationJob;
import org.jahia.services.scheduler.BackgroundJob;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Publish custom activity for jBPM workflow
 * <p/>
 * Publish the current node
 */
public class PublishWorkItemHandler extends AbstractWorkItemHandler implements WorkItemHandler {
    private transient static Logger logger = LoggerFactory.getLogger(PublishWorkItemHandler.class);

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        @SuppressWarnings("unchecked")
        List<String> uuids = (List<String>) workItem.getParameter("nodeIds");
        String workspace = (String) workItem.getParameter("workspace");
        String userKey = (String) workItem.getParameter("user");
        if (workItem.getParameter("currentUser") != null) {
            userKey = (String) workItem.getParameter("currentUser");
        }

        JobDetail jobDetail = BackgroundJob.createJahiaJob("Publication", PublicationJob.class);
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put(BackgroundJob.JOB_USERKEY, userKey);
        jobDataMap.put(PublicationJob.PUBLICATION_UUIDS, uuids);
        jobDataMap.put(PublicationJob.SOURCE, workspace);
        jobDataMap.put(PublicationJob.DESTINATION, Constants.LIVE_WORKSPACE);
        jobDataMap.put(PublicationJob.LOCK, "publication-process-" + workItem.getProcessInstanceId());
        jobDataMap.put(PublicationJob.CHECK_PERMISSIONS, false);

        try {
            ServicesRegistry.getInstance().getSchedulerService().scheduleJobAtEndOfRequest(jobDetail);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        manager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        manager.abortWorkItem(workItem.getId());
    }
}
