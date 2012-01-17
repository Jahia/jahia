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

package org.jahia.services.content;

import org.jahia.api.Constants;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.WorkflowService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Publication job
 */
public class PublicationJob extends BackgroundJob {
    public static final String PUBLICATION_UUIDS = "publicationInfos";
    public static final String PUBLICATION_PROPERTIES = "publicationProperties";
    public static final String PUBLICATION_COMMENTS = "publicationComments";
    public static final String SOURCE = "source";
    public static final String DESTINATION = "destination";
    public static final String LOCK = "lock";

    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        List<String> uuids = (List<String>) jobDataMap.get(PUBLICATION_UUIDS);
        String source = (String) jobDataMap.get(SOURCE);
        String destination = (String) jobDataMap.get(DESTINATION);
        String lock = (String) jobDataMap.get(LOCK);
        List<String> comments = (List<String>) jobDataMap.get(PUBLICATION_COMMENTS);

        JCRPublicationService.getInstance().publish(uuids, source, destination, comments);

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
                if (uuids.containsAll(nodeIds)) {
                    JCRPublicationService.getInstance().unlockForPublication(nodeIds, (String)workflow.getVariables().get("workspace"), "publication-process-" + workflow.getId());
                    workflowService.abortProcess(workflow.getId(), workflow.getProvider());
                }
            }
        }
    }
}
