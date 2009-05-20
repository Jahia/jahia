/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

/**
 * @author Xavier Lawrence
 */
public class NotifyAllJob extends AbstractActivationJob {

    private static final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(NotifyAllJob.class);

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
                if (theObject == null || !theObject.checkWriteAccess(jParams.getUser())) {
                    if (logger.isDebugEnabled()) logger.debug("No Write access on " + key);
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
                String actionName = NOTIFY_PAGES;
                int mode = service.getInheritedMode(theObject);
                if (mode == WorkflowService.EXTERNAL) {
                    String wfName = service.getInheritedExternalWorkflowName(theObject);
                    ExternalWorkflow external = service.getExternalWorkflow(wfName);
                    final String processID = service.getInheritedExternalWorkflowProcessId(theObject);
                    final ExternalWorkflowInstanceCurrentInfos infos = external.getCurrentInfo(key, jParams.getLocale().toString());
                    if (infos.isLastStep()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Ignoring action since we are on last step");
                            continue;
                        }
                    }
                    if (!jParams.getUser().isAdminMember(jParams.getSiteID())) {
                        if (!external.isUserAuthorizedForNextStep(processID, theObject, jParams.getUser(), languageCodes)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("user is not authorized for the following step on");
                            }

                            if (jParams.settings().areLocksActivated()) {
                                final LockKey lockKey = LockKey.composeLockKey(LockKey.WORKFLOW_ACTION + "_" + objectKey.getType(), theObject.getID());
                                lockRegistry.release(lockKey, jParams.getUser(), jParams.getUser().getUserKey());
                            }
                            continue;
                        }
                    }
                    actionName = new StringBuffer().append(processID).append(".").append(infos.getNextRole()).toString();
                }
                actions.add(new WorkflowAction((ContentObjectKey) objectKey, languageCodes, actionName, comment));

                final StateModificationContext stateModifContext = new StateModificationContext(objectKey, languageCodes);
                stateModifContext.addModifiedObjects(allKeys);

                final ExternalWorkflow externalWorkflow = processWorkflow(jParams, objectKey, actionName, languageCodes, saveVersion, comment, activationTestResults, stateModifContext);
                if (externalWorkflow != null) {
                    externalWorkflows.put(externalWorkflow, externalWorkflow);
                }

                final LockKey lockKey = LockKey.composeLockKey(LockKey.WORKFLOW_ACTION + "_" + objectKey.getType(), theObject.getID());
                lockRegistry.release(lockKey, jParams.getUser(), jParams.getUser().getUserKey());
            }

        jobDataMap.put(ACTIONS, actions);
        jobDataMap.put(RESULT, activationTestResults);
    }
}
