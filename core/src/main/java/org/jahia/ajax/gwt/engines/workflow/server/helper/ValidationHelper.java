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

package org.jahia.ajax.gwt.engines.workflow.server.helper;

import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowElement;
import org.jahia.ajax.gwt.client.data.GWTJahiaNodeOperationResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaNodeOperationResultItem;
import org.jahia.ajax.gwt.utils.JahiaObjectCreator;
import org.jahia.content.ContentObject;
import org.jahia.content.JahiaObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.NodeOperationResult;
import org.jahia.params.ProcessingContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.StateModificationContext;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.fields.WAIValidForActivationResults;
import org.jahia.services.fields.URLIntegrityValidForActivationResults;
import org.jahia.services.lock.LockService;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.resourcebundle.JahiaResourceBundle;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Validation class
 *
 * User: rfelden
 * Date: 14 ao�t 2008 - 11:50:46
 */
public class ValidationHelper {

    private static Logger logger = Logger.getLogger(ValidationHelper.class) ;
    protected static final WorkflowService workflowService = ServicesRegistry.getInstance().getWorkflowService();
    protected static final LockService lockService = ServicesRegistry.getInstance().getLockService();
    protected static final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();

    /**
     * This method adds validation information to the given workflow object. It returns false if the object can't be accessed by
     * the current user (this case shouldn't happen since entries are from the same user).
     *
     * @param workflowElement the workflow element to add the information to
     * @param object the corresponding content object
     * @param jParams the processing context
     * @return true if the validation could be done, false otherwise (e.g. no access)
     * @throws org.jahia.exceptions.JahiaException sthg bad happened
     */
    public static boolean checkValidation(GWTJahiaWorkflowElement workflowElement, ContentObject object, ProcessingContext jParams) throws JahiaException {
        //final Map<String, Integer> languagesStates = workflowService.getLanguagesStates(object);
        final Set<String> languageCodes = new HashSet<String>(WorkflowServiceHelper.retrieveOrderedLanguageCodesForSite(jParams.getSite())) ;
        final ObjectKey objectKey = object.getObjectKey() ;
        JahiaUser currentUser = jParams.getUser() ;
        boolean accessAuthorized = object.checkAdminAccess(currentUser) || object.checkWriteAccess(currentUser);


        if (!accessAuthorized) {
            // test the linked children
            final List<JahiaObject> list = workflowService.getLinkedContentObjects(object, false);
            for (int i = 0; i < list.size() && !accessAuthorized; i++) {
                ContentObject contentObject = (ContentObject) list.get(i);
                accessAuthorized = contentObject.checkWriteAccess(currentUser);
            }
        }

        if (!accessAuthorized) {
            if (logger.isDebugEnabled()) {
                logger.debug("access unauthorized, finished processing validation for object " + objectKey) ;
            }
            return false ;
        } else {
            boolean isUserAnAdminMember = currentUser.isAdminMember(jParams.getSiteID());
            boolean hasIntegrityBypassRole = isUserAnAdminMember
                    || !jParams.getSite().isURLIntegrityCheckEnabled()
                    || aclService.getSiteActionPermission(
                            "integrity.LinkIntegrity", currentUser,
                            JahiaBaseACL.READ_RIGHTS, jParams.getSiteID()) <= 0;
            boolean hasWAIBypassRole = isUserAnAdminMember
                    || !jParams.getSite().isWAIComplianceCheckEnabled()
                    || aclService.getSiteActionPermission(
                            "integrity.WaiCompliance", currentUser,
                            JahiaBaseACL.READ_RIGHTS, jParams.getSiteID()) <= 0;

            workflowElement.setCanBypassWaiChecks(hasWAIBypassRole);
            workflowElement.setCanBypassUrlChecks(hasIntegrityBypassRole);
            workflowElement.setCanBypassOtherChecks(isUserAnAdminMember);

            boolean blockingErrors = false;


            //final int mode = workflowService.getInheritedMode(object);
            //ActivationTestResults results ;
            /* TODO take care of specific actions
            if (WorkflowService.EXTERNAL == mode /* && !NStepWorkflow.isValidationNeededForAction(action)) {
                // skip validation for N-Step actions, which do not require validation
                results = new ActivationTestResults();
            } else { */
                ActivationTestResults results = workflowService.isValidForActivation(object,
                        languageCodes, jParams, new StateModificationContext(
                                objectKey, languageCodes, false));
            //}
            final boolean failedOperationStatus = results.getStatus() == ActivationTestResults.FAILED_OPERATION_STATUS;
            /*final boolean failedForLanguageOnly = results.getStatus() == ActivationTestResults.PARTIAL_OPERATION_STATUS &&
                    (object.getClass() == ContentPage.class || object.getClass() == ContentContainer.class);*/

            // this will contains validation information for the current object
            Map<String, GWTJahiaNodeOperationResult> validationsForCurrentObject = new HashMap<String, GWTJahiaNodeOperationResult>() ;

            for (Object warning : results.getWarnings()) {
            	NodeOperationResult res = (NodeOperationResult)warning;
                String lang = res.getLanguageCode() ;
                if (logger.isDebugEnabled()) {
                    logger.debug("Found a warning for object " + objectKey + " in " + lang) ;
                }
                // create the validation object for the given language if it doesn't exist yet
                if (!validationsForCurrentObject.containsKey(lang)) {
                    validationsForCurrentObject.put(lang, new GWTJahiaNodeOperationResult()) ;
                }
                validationsForCurrentObject.get(lang).addErrorOrWarning(getValidationResult(res, lang, jParams, GWTJahiaNodeOperationResultItem.WARNING, GWTJahiaNodeOperationResultItem.VALIDATION));
                blockingErrors |= res.isBlocker();
//                blockingErrors = !blockingErrors
//                        && (res.getClass() == WAIValidForActivationResults.class
//                                && !workflowElement.canBypassWaiChecks() || res
//                                .getClass() == URLIntegrityValidForActivationResults.class
//                                && !workflowElement.canBypassUrlChecks()); 
            }

            for (Object error : results.getErrors()) {
            	NodeOperationResult res = (NodeOperationResult)error;
                // Field errors cannot block a whole language
                if (!"ContentField".equals(res.getObjectType()) || object.getClass() != ContentPage.class) {
                    String lang = res.getLanguageCode() ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Found an error for object " + objectKey + " in " + lang + (res.isBlocker() ? " (blocker)" : "")) ;
                    }
                    // create the validation object for the given language if it doesn't exist yet
                    if (!validationsForCurrentObject.containsKey(lang)) {
                        validationsForCurrentObject.put(lang, new GWTJahiaNodeOperationResult()) ;
                    }
                    GWTJahiaNodeOperationResult currentValidation = validationsForCurrentObject.get(lang) ;
                    if (res instanceof WAIValidForActivationResults) {
//                        if (!hasWAIBypassRole && failedForLanguageOnly && !isUserAnAdminMember) {
//                            if (currentValidation.getValidationStatus()) {
//                                currentValidation.setValidationStatus(false);
//                            }
//                        }
                        currentValidation.addErrorOrWarning(getValidationResult(res, lang, jParams, GWTJahiaNodeOperationResultItem.ERROR, GWTJahiaNodeOperationResultItem.WAI));
                    } else if (res instanceof URLIntegrityValidForActivationResults) {
//                        if (!hasIntegrityBypassRole && failedForLanguageOnly && !isUserAnAdminMember) {
//                            if (currentValidation.getValidationStatus()) {
//                                currentValidation.setValidationStatus(false);
//                            }
//                        }
                        currentValidation.addErrorOrWarning(getValidationResult(res, lang, jParams, GWTJahiaNodeOperationResultItem.ERROR, GWTJahiaNodeOperationResultItem.URL));
                    } else {
//                        if (failedForLanguageOnly && !isUserAnAdminMember) {
//                            if (currentValidation.getValidationStatus()) {
//                                currentValidation.setValidationStatus(false);
//                            }
//                        }
                        currentValidation.addErrorOrWarning(getValidationResult(res, lang, jParams, GWTJahiaNodeOperationResultItem.ERROR, GWTJahiaNodeOperationResultItem.VALIDATION));
                    }
                    blockingErrors |= res.isBlocker();
//                    blockingErrors = !blockingErrors
//                    && (res.getClass() == WAIValidForActivationResults.class
//                            && !workflowElement.canBypassWaiChecks() || res
//                            .getClass() == URLIntegrityValidForActivationResults.class
//                            && !workflowElement.canBypassUrlChecks()); 
                }
            }
            if (blockingErrors || (failedOperationStatus && !isUserAnAdminMember)) {
                workflowElement.setValidationBlocker(true);
            }

            workflowElement.setValidation(validationsForCurrentObject);
        }

        return true ;
    }

    private static GWTJahiaNodeOperationResultItem getValidationResult(
            NodeOperationResult operationResult, String languageCode,
            ProcessingContext ctx, int level, int type) throws JahiaException {
        
        GWTJahiaNodeOperationResultItem gwtOperationResultItem = new GWTJahiaNodeOperationResultItem();
        gwtOperationResultItem.setMessage(JahiaResourceBundle.getMessageResource(operationResult.getMsg().getKey(),
                ctx.getLocale(), operationResult.getMsg().getValues()));
        gwtOperationResultItem.setComment(operationResult.getComment());

        try {
            ContentObject faulty = JahiaObjectCreator
                    .getContentObjectFromKey(operationResult.getNodeKey());
            if (faulty != null) {
                ContentPage faultyPage = ContentPage
                        .getPage(faulty.getPageID());
                if (faultyPage != null) {
                    gwtOperationResultItem.setUrl(faultyPage.getURL(ctx, languageCode));
                }
            }
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        gwtOperationResultItem.setBlocker(operationResult.isBlocker());
        gwtOperationResultItem.setType(type);
        gwtOperationResultItem.setLevel(level);
        return gwtOperationResultItem;
    }

}
