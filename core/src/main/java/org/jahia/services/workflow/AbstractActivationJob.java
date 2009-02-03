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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.JahiaObject;
import org.jahia.content.ObjectKey;
import org.jahia.data.fields.FieldTypes;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaACLException;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.fields.ContentField;
import org.jahia.services.mail.MailHelper;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;

/**
 * Created by IntelliJ IDEA.
 * Date: 14 nov. 2005 - 15:10:04
 *
 * @author toto
 * @version $Id$
 */
public abstract class AbstractActivationJob extends BackgroundJob {

    protected static final WorkflowService service = ServicesRegistry.getInstance().getWorkflowService();

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AbstractActivationJob.class);
    public static final String COMMENTS_INPUT = "commentsInput";
    public static final String WORKFLOW_TYPE = "workflow";
    public static final String SELECTED_ENTRIES = "selectedEntries";
    public static final String KEYS = "keys";

    public static final String NOTIFY_PAGES = "notify";
    public static final String REFUSE_PENDING_PAGES = "rollback_to_author";
    public static final String PUBLISH_PENDING_PAGES = "publish";


    protected ExternalWorkflow processWorkflow(final ProcessingContext jParams,
                                               final ObjectKey key,
                                               final String action,
                                               final Set<String> languageCodes,
                                               final JahiaSaveVersion saveVersion,
                                               Map<RecipientInfo, Object> userNotifData,
                                               String comment,
                                               ActivationTestResults activationTestResults, StateModificationContext stateModifContext)
            throws JahiaException {

        ContentObject object = null;
        ExternalWorkflow externalWorkflow = null;
        try {
            object = (ContentObject) JahiaObject.getInstance(key);
        } catch (ClassNotFoundException e) {
            logger.error(e, e);
        }

        boolean canWrite = object.checkWriteAccess(jParams.getUser());
        boolean canAdmin = object.checkAdminAccess(jParams.getUser());

        final int mode = service.getInheritedMode(object);
        boolean isMailServiceEnabled = ServicesRegistry.getInstance()
                .getMailService().isEnabled();

        try {
            ContentObject contentObject = (ContentObject) ContentObject.getInstance(key, true);
            if (mode == WorkflowService.INACTIVE) {
                if (action.equals(PUBLISH_PENDING_PAGES) && canAdmin) {
                    try {
                        activationTestResults.merge(service.activate(
                                contentObject,
                                languageCodes,
                                saveVersion, jParams,
                                stateModifContext));
                    } catch (JahiaException je) {
                        logger.error("Cannot change Jahia Page staging status", je);
                    }
                }
            } else if (mode == WorkflowService.JAHIA_INTERNAL) {
                PageNotifData pageNotifData = null;
                try {
                    int pageID = 0;
                    ContentPage parentPage = null;
                    if (contentObject instanceof ContentPage) {
                        pageID = contentObject.getID();
                        parentPage = (ContentPage) contentObject;
                    } else if (contentObject instanceof ContentContainer ||
                            contentObject instanceof ContentContainerList) {
                        pageID = contentObject.getPageID();
                        parentPage = ContentPage.getPage(pageID, false, true);
                    } else if (contentObject instanceof ContentField) {
                        ContentField contentField = (ContentField) contentObject;
                        if (contentField.getType() != FieldTypes.PAGE) {
                            pageID = contentField.getPageID();
                            parentPage = ContentPage.getPage(pageID, false, true);
                        }
                    }
                    String useLanguage = null;
                    if (pageID > 0 && languageCodes.size() == 1) {
                        try {
                            useLanguage = jParams.getSite()
                                    .getLanguageSettings(true).size() > 1 ? (String) languageCodes
                                    .iterator().next()
                                    : null;
                        } catch (JahiaException e) {
                            logger.warn(
                                    "Unable to retrieve active languages for site with key '"
                                            + jParams.getSiteKey() + "'", e);
                        }
                    }
                    
                    if (action.equals(NOTIFY_PAGES) && canWrite) {
                        service.changeStagingStatus(contentObject, languageCodes,
                                EntryLoadRequest.WAITING_WORKFLOW_STATE, stateModifContext, jParams,
                                false);
                        if (pageID > 0) {
                            pageNotifData = new PageNotifData(
                                    jParams.getSiteURL(jParams.getSite(), pageID, false, ProcessingContext.EDIT, useLanguage, true), comment,
                                    parentPage, languageCodes, true, null, null);
                        }
                    } else if (action.equals(PUBLISH_PENDING_PAGES) && canAdmin) {
                        ActivationTestResults res = service.activate(contentObject, languageCodes, saveVersion, jParams,
                                stateModifContext);
                        final boolean workflowSuccessful = (res.getStatus() == ActivationTestResults.COMPLETED_OPERATION_STATUS);
                        final boolean partialStatus = (res.getStatus() == ActivationTestResults.PARTIAL_OPERATION_STATUS);
                        if (pageID > 0)
                            pageNotifData = new PageNotifData(
                                    jParams.getSiteURL(jParams.getSite(), pageID, false, false, useLanguage, true), comment,
                                    parentPage, languageCodes, workflowSuccessful, res.getErrors(),
                                    res.getWarnings());
                        pageNotifData.setPartialSuccessful(partialStatus);
                        activationTestResults.merge(res);
                    } else if (action.equals(REFUSE_PENDING_PAGES) && canAdmin) {
                        service.changeStagingStatus(contentObject, languageCodes,
                                EntryLoadRequest.STAGING_WORKFLOW_STATE, stateModifContext, jParams,
                                true);
                        if (pageID > 0)
                            pageNotifData = new PageNotifData(
                                    jParams.getSiteURL(jParams.getSite(), pageID, false, ProcessingContext.EDIT, useLanguage, true), comment,
                                    parentPage, languageCodes, true, null, null);
                    }
                } catch (JahiaException je) {
                    logger.error("Cannot change Jahia Page staging status", je);
                }

                if (isMailServiceEnabled) {
                    updateUserNotifData(pageNotifData, object, jParams, userNotifData, action);
                }
            } else if (mode == WorkflowService.EXTERNAL) {
                String wfName = service.getInheritedExternalWorkflowName((ContentObjectKey) key);
                externalWorkflow = service.getExternalWorkflow(wfName);
                final String processId = service.getInheritedExternalWorkflowProcessId(object);

                for (final String language : languageCodes) {
                    jParams.setAttribute("nstepcomment", comment);
                    if (externalWorkflow.isProcessStarted(processId, key.toString(), language)) {
                        boolean result = externalWorkflow.sendAction(processId, key.toString(), language,
                                action, jParams, activationTestResults,
                                userNotifData);
                        if (!result) {
                            if (action.equals(PUBLISH_PENDING_PAGES) && canAdmin) {
                                try {
                                    ActivationTestResults res = service.activate(contentObject, languageCodes, saveVersion,
                                            jParams, stateModifContext);
                                    if (res.getStatus() == ActivationTestResults.COMPLETED_OPERATION_STATUS) {
                                        externalWorkflow.abortProcess(processId, key.toString(), language, jParams);
                                    }
                                    activationTestResults.merge(res);
                                } catch (JahiaException je) {
                                    logger.error("Cannot change Jahia Page staging status", je);
                                }

                            } else if (action.equals(REFUSE_PENDING_PAGES) && canAdmin) {
                                try {
                                    service.changeStagingStatus(contentObject, languageCodes,
                                            EntryLoadRequest.STAGING_WORKFLOW_STATE, stateModifContext,
                                            jParams, true);
                                } catch (JahiaException je) {
                                    logger.error("Cannot change Jahia Page staging status", je);
                                }
                                externalWorkflow.abortProcess(processId, key.toString(), language, jParams);
                                externalWorkflow.initProcess(processId, key.toString(), language, jParams);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error during workflow operation! We must flush all caches to ensure integrity between database and viewing", e);
            ServicesRegistry.getInstance().getCacheService().flushAllCaches();
            throw new JahiaException(e.getMessage(), e.getMessage(),
                    JahiaException.DATABASE_ERROR,
                    JahiaException.CRITICAL_SEVERITY, e);
        }
        return externalWorkflow;
    }

    protected void updateUserNotifData(PageNotifData pageNotifData, ContentObject object, ProcessingContext jParams, Map<RecipientInfo, Object> userNotifData, String action) {
        if (pageNotifData != null) {
            // new system, per user messaging
        	Set<RecipientInfo> recipients = getRecipients(object, 0x3, jParams);
            for (final RecipientInfo recipient : recipients) {
                Map<String, Set<PageNotifData>> operationMap = (Map<String, Set<PageNotifData>>)userNotifData.get(recipient);
                if (operationMap == null) {
                    operationMap = new HashMap<String, Set<PageNotifData>>();
                }

                Set<PageNotifData> pageList = operationMap.get(action);
                if (pageList == null) {
                    pageList = new HashSet<PageNotifData>();
                }
                pageList.add(pageNotifData);
                operationMap.put(action, pageList);
                userNotifData.put(recipient, operationMap);
            }
        }
    }


    protected Set<RecipientInfo> getRecipients(ContentObject contentObject, int access, ProcessingContext ctx) {
        Set<RecipientInfo> result = new HashSet<RecipientInfo>();

        // result.add(new RecipientInfo(ServicesRegistry.getInstance().getMailService().defaultRecipient()));

        /**
         * todo we could optimize this if we retrieved really only the list
         * of users that have write and admin accesses instead of retrieving
         * all users in the ACL.
         */
        JahiaBaseACL pageACL = contentObject.getACL();
        try {
            List<String> users = pageACL.getUsernameListAlsoGroupUsers(null, true);

            for (int i = 0; i < users.size(); i++) {
                JahiaUser user = ServicesRegistry.getInstance().
                        getJahiaUserManagerService().lookupUserByKey(users.get(i));
                if (user == null)
                    continue;
                String email = MailHelper.getEmailAddress(user);
                if (email != null && !MailHelper.areEmailNotificationsDisabled(user)) {
                    boolean shouldCheckWriteAccess = (access & 0x01) != 0;
                    boolean shouldCheckAdminAccess = (access & 0x02) != 0;
                    if (shouldCheckWriteAccess &&
                            contentObject.checkWriteAccess(user, true) ||
                            shouldCheckAdminAccess &&
                                    contentObject.checkAdminAccess(user, true)) {
                        result.add(new RecipientInfo(user, ctx.getSiteID()));
                    }
                }
            }
        } catch (JahiaACLException jae) {
            logger.error("Error while retrieving users from ACL", jae);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Using email recipient list :" + result);
        }
        
        return result;
    }


}
