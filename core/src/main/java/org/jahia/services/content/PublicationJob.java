/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.jahia.api.Constants;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.WorkflowService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Publication job
 */
public class PublicationJob extends BackgroundJob {
    public static final String PUBLICATION_UUIDS = "publicationInfos";
    public static final String PUBLICATION_PATHS = "publicationPaths";
    public static final String PUBLICATION_PROPERTIES = "publicationProperties";
    public static final String PUBLICATION_COMMENTS = "publicationComments";
    public static final String PUBLICATION_TITLE = "publicationTitle";
    public static final String SOURCE = "source";
    public static final String DESTINATION = "destination";
    public static final String LOCK = "lock";
    public static final String CHECK_PERMISSIONS = "checkPermissions";

    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        List<String> uuids = (List<String>) jobDataMap.get(PUBLICATION_UUIDS);
        String source = (String) jobDataMap.get(SOURCE);
        String destination = (String) jobDataMap.get(DESTINATION);
        String lock = (String) jobDataMap.get(LOCK);
        Boolean checkPermissions = (Boolean) jobDataMap.get(CHECK_PERMISSIONS);
        List<String> comments = (List<String>) jobDataMap.get(PUBLICATION_COMMENTS);

        JCRPublicationService.getInstance().publish(uuids, source, destination, checkPermissions, comments);

        if (lock != null) {
            JCRPublicationService.getInstance().unlockForPublication(uuids, source, lock);
        }

        String label = "published_at_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(GregorianCalendar.getInstance().getTime());
        JCRVersionService.getInstance().addVersionLabel(uuids, label, Constants.LIVE_WORKSPACE);

        // Clean up other workflows that are not relevant anymore
        WorkflowService workflowService = WorkflowService.getInstance();
        List<Workflow> l =  workflowService.getWorkflowsForType("publish", null);
        for (Workflow workflow : l) {
            if (!("publication-process-"+workflow.getId()).equals(lock)) {
                List<String> nodeIds = (List<String>) workflow.getVariables().get("nodeIds");
                if (nodeIds != null && uuids.containsAll(nodeIds)) {
                    JCRPublicationService.getInstance().unlockForPublication(nodeIds, (String) workflow.getVariables().get("workspace"), "publication-process-" + workflow.getId());
                    workflowService.abortProcess(workflow.getId(), workflow.getProvider());
                }
            }
        }
    }
}
