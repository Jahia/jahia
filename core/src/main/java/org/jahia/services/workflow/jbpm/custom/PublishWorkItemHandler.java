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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.workflow.jbpm.custom;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.PublicationJob;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.workflow.WorkflowVariable;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
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
        final List<String> uuids = (List<String>) workItem.getParameter("nodeIds");
        String workspace = (String) workItem.getParameter("workspace");
        String userKey = (String) workItem.getParameter("user");
        if (workItem.getParameter("currentUser") != null) {
            userKey = (String) workItem.getParameter("currentUser");
        }

        List<String> publicationPath = null;
        try {
            publicationPath = JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null, new JCRCallback<List<String>>() {
                public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    List<String> result = new ArrayList<>();
                    for (String uuid : uuids) {
                        try {
                            result.add(session.getNodeByIdentifier(uuid).getPath());
                        } catch (RepositoryException e) {
                            logger.debug("Cannot get item " + uuid, e);
                        }
                    }
                    return result;
                }
            });
        } catch (RepositoryException e) {
            logger.debug("Error occured when getting node paths for uuids", e);
        }

        JobDetail jobDetail = BackgroundJob.createJahiaJob("Publication", PublicationJob.class);
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put(BackgroundJob.JOB_USERKEY, userKey);
        jobDataMap.put(PublicationJob.PUBLICATION_UUIDS, uuids);
        jobDataMap.put(PublicationJob.PUBLICATION_PATHS, publicationPath);
        if (workItem.getParameter("jcr:title") != null) {
            jobDataMap.put(PublicationJob.PUBLICATION_TITLE, ((WorkflowVariable) workItem.getParameter("jcr:title")).getValue());
        }
        jobDataMap.put(PublicationJob.SOURCE, workspace);
        jobDataMap.put(PublicationJob.DESTINATION, Constants.LIVE_WORKSPACE);
        jobDataMap.put(PublicationJob.LOCK, "publication-process-" + workItem.getProcessInstanceId());
        jobDataMap.put(PublicationJob.CHECK_PERMISSIONS, false);

        try {
            ServicesRegistry.getInstance().getSchedulerService().scheduleJobAtEndOfRequest(jobDetail);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        logger.debug("publish job schedule and send complete work item {}", workItem);
        manager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        manager.abortWorkItem(workItem.getId());
    }
}
