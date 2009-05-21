/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.workflow;

import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.ContentObjectKey;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockService;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.util.*;

/**
 * @author Xavier Lawrence
 */
public class PublishAllJob extends AbstractActivationJob {

    private static final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PublishAllJob.class);

    /**
     *
     */
    public void executeJahiaJob(final JobExecutionContext jobExecutionContext, final ProcessingContext jParams) throws Exception {
        final JobDetail jobDetail = jobExecutionContext.getJobDetail();
        final JobDataMap jobDataMap = jobDetail.getJobDataMap();
        final String comment = (String) jobDataMap.get(AbstractActivationJob.COMMENTS_INPUT);
        final Set<String> allStagingAndWaitingObjects = service.getAllStagingAndWaitingObject(jParams.getSiteID()).keySet();
        final Set<String> languageCodes = new HashSet<String>();
        for (final SiteLanguageSettings tmp : jParams.getSite().getLanguageSettings(true)) {
            languageCodes.add(tmp.getCode());
        }
        if (logger.isDebugEnabled()) logger.debug("All Staging and Waiting: " + allStagingAndWaitingObjects);

        List<WorkflowAction> actions = new ArrayList<WorkflowAction>();

        final JahiaSaveVersion saveVersion = ServicesRegistry.getInstance().getJahiaVersionService().
                getSiteSaveVersion(jParams.getSiteID());
        final Map<ExternalWorkflow, ExternalWorkflow> externalWorkflows = new HashMap<ExternalWorkflow, ExternalWorkflow>();
        final ActivationTestResults activationTestResults = new ActivationTestResults();
            List<ObjectKey> allKeys = new ArrayList<ObjectKey>();
            for (String key : allStagingAndWaitingObjects) {
                final ObjectKey objectKey = ObjectKey.getInstance(key);
                allKeys.add(objectKey);
            }
            final Iterator<ObjectKey> iterator = allKeys.iterator();
            while (iterator.hasNext()) {
                final ObjectKey objectKey = iterator.next();
                String key = objectKey.toString();
                if (logger.isDebugEnabled()) logger.debug("Processing object: " + key);
                final ContentObject theObject = ContentObject.getContentObjectInstance(objectKey);
                if (theObject == null || !theObject.checkAdminAccess(jParams.getUser())) {
                    if (logger.isDebugEnabled()) logger.debug("No Admin access on " + key);
                    continue;
                }
                // #ifdef LOCK
                if (jParams.settings().areLocksActivated()) {
                    // Ensure that the lock can be taken.
                    final LockKey lockKey = LockKey.composeLockKey(LockKey.WORKFLOW_ACTION + "_" + objectKey.getType(), theObject.getID());
                    if (!lockRegistry.acquire(lockKey, jParams.getUser(), jParams.getUser().getUserKey(), jParams.getSessionState().getMaxInactiveInterval())) {
                        if (logger.isDebugEnabled()) logger.debug("Could not acquire lock for " + key);
                        continue;
                    }
                }
                // #endif
                int mode = service.getInheritedMode(theObject);
                if (mode == WorkflowService.EXTERNAL && !jParams.getUser().isAdminMember(jParams.getSiteID())) {
                    String wfName = service.getInheritedExternalWorkflowName(theObject);
                    ExternalWorkflow external = service.getExternalWorkflow(wfName);
                    final String processID = service.getExternalWorkflowProcessId(theObject);
                    if (!external.isUserAuthorizedForAllFollowingSteps(processID, theObject, jParams.getUser(), languageCodes)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("user is not authorized for all following steps");
                        }

                        if (jParams.settings().areLocksActivated()) {
                            final LockKey lockKey = LockKey.composeLockKey(LockKey.WORKFLOW_ACTION + "_" + objectKey.getType(), theObject.getID());
                            lockRegistry.release(lockKey, jParams.getUser(), jParams.getUser().getUserKey());
                        }
                        continue;
                    }
                }
                actions.add(new WorkflowAction((ContentObjectKey) objectKey, languageCodes, PUBLISH_PENDING_PAGES, comment));

                final StateModificationContext stateModifContext = new StateModificationContext(objectKey, languageCodes);
                stateModifContext.addModifiedObjects(allKeys);

                final ExternalWorkflow externalWorkflow = processWorkflow(jParams, objectKey, PUBLISH_PENDING_PAGES, languageCodes, saveVersion, comment, activationTestResults, stateModifContext);
                if (externalWorkflow != null) {
                    externalWorkflows.put(externalWorkflow, externalWorkflow);
                }

                if (jParams.settings().areLocksActivated()) {
                    final LockKey lockKey = LockKey.composeLockKey(LockKey.WORKFLOW_ACTION + "_" + objectKey.getType(), theObject.getID());
                    lockRegistry.release(lockKey, jParams.getUser(), jParams.getUser().getUserKey());
                }
            }

        jobDataMap.put(ACTIONS, actions);
        jobDataMap.put(RESULT, activationTestResults);
    }
}
