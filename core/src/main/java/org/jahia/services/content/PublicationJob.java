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
    public static final String PUBLICATION_PROPERTIES = "publicationProperties";
    public static final String PUBLICATION_COMMENTS = "publicationComments";
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
