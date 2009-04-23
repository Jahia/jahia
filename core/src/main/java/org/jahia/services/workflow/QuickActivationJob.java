/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.workflow;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.content.ContentObject;
import org.jahia.content.NodeOperationResult;
import org.jahia.content.ObjectKey;
import org.jahia.content.ContentObjectKey;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockService;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;import org.jahia.services.version.EntryLoadRequest;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

/**
 * Backgrouind job used to perform most "permissive" activation action for the
 * current user and teh secified content object.
 * 
 * @author Sergiy Shyrkov
 */
public class QuickActivationJob extends AbstractActivationJob {

    public static final String CONTENT_OBJECT_KEY = "contentObjectKey";
    public static final String ACTION = "action";
    public static final String LANGUAGE = "lang";

    private static final transient Logger logger = Logger
            .getLogger(QuickActivationJob.class);

    private boolean checkLockAcquired(ContentObject contentObject,
            ProcessingContext ctx) {
        boolean success = true;
        // locks activated?
        if (ctx.settings().areLocksActivated()) {
            LockService lockRegistry = ServicesRegistry.getInstance()
                    .getLockService();
            final LockKey lockKey = LockKey.composeLockKey(contentObject
                    .getObjectKey(), LockKey.WORKFLOW_ACTION);

            // do we already have a lock on it? If not --> acquire it
            if (lockRegistry.isAlreadyAcquiredInContext(lockKey, ctx.getUser(),
                    ctx.getUser().getUserKey())
                    || lockRegistry.acquire(lockKey, ctx.getUser(), ctx
                            .getUser().getUserKey(), ctx.getSessionState()
                            .getMaxInactiveInterval(), false)) {
                // great, we have it!
            } else {
                // failure
                success = false;
            }
        }
        return success;
    }

    public void executeJahiaJob(JobExecutionContext jobExecutionContext,
            ProcessingContext ctx) throws Exception {

        JobDataMap jobData = jobExecutionContext.getJobDetail().getJobDataMap();
        ActivationTestResults activationTestResults = new ActivationTestResults(
                ActivationTestResults.FAILED_OPERATION_STATUS);
        
        try {
            ContentObject contentObject = retrieveObject(jobData, ctx);

            JahiaSaveVersion saveVersion = ServicesRegistry.getInstance()
                    .getJahiaVersionService().getSiteSaveVersion(
                            ctx.getSiteID());

            String languageCode = jobData.getString(QuickActivationJob.LANGUAGE);

            if (languageCode == null ) {
                languageCode = ctx.getLocale().toString();
            }

            Map<String,Integer> states = service.getLanguagesStates(contentObject);
            if (!states.containsKey(languageCode)) {
                return;
            }


            String action = jobData.getString(QuickActivationJob.ACTION);
            ContentObjectKey key = (ContentObjectKey) contentObject.getObjectKey();
            if (action == null) {
                int state = states.get(languageCode);

                boolean isStagingState = state == EntryLoadRequest.STAGING_WORKFLOW_STATE;
                boolean isActiveState = state == EntryLoadRequest.ACTIVE_WORKFLOW_STATE;
                boolean canAdmin = contentObject.checkAdminAccess(ctx.getUser(), true);
                boolean canWrite = contentObject.checkWriteAccess(ctx.getUser(), true);

                int mode = service.getInheritedMode(contentObject);
                if (mode == WorkflowService.INACTIVE && !isActiveState && canAdmin) {
                    action = PUBLISH_PENDING_PAGES;
                } else if (mode == WorkflowService.JAHIA_INTERNAL && canAdmin && !isActiveState) {
                    action = PUBLISH_PENDING_PAGES;
                } else if (mode == WorkflowService.JAHIA_INTERNAL && canWrite && isStagingState) {
                    action = NOTIFY_PAGES;
                } else if (mode == WorkflowService.EXTERNAL) {
                    String wfName = service.getInheritedExternalWorkflowName(contentObject);
                    ExternalWorkflow workflow = service.getExternalWorkflow(wfName);
                    final String processId = service.getInheritedExternalWorkflowProcessId(contentObject);
                    final boolean processStarted = workflow.isProcessStarted(processId, key.toString(), languageCode);
                    if (logger.isDebugEnabled()) {
                        logger.debug("processStarted: " + processStarted + ", isActiveState: " + isActiveState);
                    }

                    if (canAdmin && !isActiveState) {
                        action = PUBLISH_PENDING_PAGES;
                    } else if (processStarted) {
                        final Collection<String> tempAvailableOptions = workflow.getAvailableActions(processId, key.toString(), languageCode, ctx);
                        final Iterator<String> iterator = tempAvailableOptions.iterator();
                        if (iterator.hasNext()) {
                            action = (String) iterator.next();
                        }
                    }
                }
            }

            if (action != null) {
                // can we get a lock on this object?
                if (!checkLockAcquired(contentObject, ctx)) {
                    throw new IllegalStateException(
                            "Unable to acquire a lock on the object '"
                                    + key
                                    + "' for workflow operation");
                }

                String comment = StringUtils.isNotEmpty(jobData
                        .getString(AbstractActivationJob.COMMENTS_INPUT)) ? jobData
                        .getString(AbstractActivationJob.COMMENTS_INPUT) : null;
                Map<ExternalWorkflow, ExternalWorkflow> externalWorkflows = new HashMap<ExternalWorkflow, ExternalWorkflow>();
                Set<String> languageCodes = new HashSet<String>();
                languageCodes.add(languageCode);

                activationTestResults = new ActivationTestResults(
                        ActivationTestResults.COMPLETED_OPERATION_STATUS);

                if (!ctx.getUser().isAdminMember(ctx.getSiteID())) {
                    activationTestResults = ServicesRegistry.getInstance().getWorkflowService().isValidForActivation(contentObject, languageCodes,
                            ctx, new StateModificationContext(key, languageCodes, false));
                }

                if (ActivationTestResults.FAILED_OPERATION_STATUS != activationTestResults.getStatus()) {
                    ExternalWorkflow externalWorkflow = processWorkflow(ctx,
                            key, action,
                            languageCodes, saveVersion, comment,
                            activationTestResults, new StateModificationContext(
                            key, languageCodes));
                    
                    if (externalWorkflow != null) {
                        externalWorkflows.put(externalWorkflow, externalWorkflow);
                    }
                }

                List<WorkflowAction> actions = new ArrayList<WorkflowAction>();
                actions.add(new WorkflowAction(key, languageCodes, action, comment));
                jobData.put(ACTIONS,actions);
                releaseLock(contentObject, ctx);
            } else {
                logger.info("No workflow action is available for user '"
                        + ctx.getUser().getUsername() + "' for object '"
                        + key + "'");
                activationTestResults
                        .setStatus(ActivationTestResults.COMPLETED_OPERATION_STATUS);
            }
        } catch (IllegalArgumentException e) {
            logger.error(e);
            activationTestResults.appendError(new NodeOperationResult(null,
                    null, e.getMessage()));
        } catch (IllegalStateException e) {
            logger.error(e);
            activationTestResults.appendError(new NodeOperationResult(null,
                    null, e.getMessage()));
        } finally {
            jobData.put(RESULT, activationTestResults);
        }
    }

    private void releaseLock(ContentObject contentObject, ProcessingContext ctx) {
        // locks activated?
        if (ctx.settings().areLocksActivated()) {
            LockService lockRegistry = ServicesRegistry.getInstance()
                    .getLockService();
            final LockKey lockKey = LockKey.composeLockKey(contentObject
                    .getObjectKey(), LockKey.WORKFLOW_ACTION);

            // release lock
            lockRegistry.release(lockKey, ctx.getUser(), ctx.getUser()
                    .getUserKey());
        }
    }

    private ContentObject retrieveObject(JobDataMap jobData,
            ProcessingContext ctx) throws ClassNotFoundException {
        // get our object
        String keyValue = jobData.getString(CONTENT_OBJECT_KEY);

        // key value is not empty?
        if (StringUtils.isEmpty(keyValue)) {
            throw new IllegalArgumentException(
                    "The object key is either null or empty");
        }

        ObjectKey key = ObjectKey.getInstance(keyValue);

        // correct key?
        if (key == null) {
            throw new IllegalArgumentException(
                    "Unable to find a content object for a key: " + key);
        }

        ContentObject contentObject = ContentObject
                .getContentObjectInstance(key);

        // content object found for this key?
        if (contentObject == null) {
            throw new IllegalArgumentException(
                    "Unable to find a content object for a key: " + key);
        }

        return contentObject;
    }

}
