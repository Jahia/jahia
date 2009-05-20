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


import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jahia.content.ContentObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ActivationTestResults;

/**
 * Interface for defining external workflow functionality. 
 * User: toto
 * Date: May 26, 2003
 * Time: 2:51:22 PM
 */
public interface ExternalWorkflow {


    /**
     * Returns localized message using the specified locale and the workflow's
     * resource bundle.
     *
     * @param messageKey the message key
     * @param locale     current locale
     * @return localized message using the specified locale and the workflow's
     *         resource bundle
     */
    String localize(String messageKey, Locale locale);

    /**
     * Get the name of the workflow engine, as it will be displayed in jahia engine.
     *
     * @param locale current locale
     * @return localized  workflow name
     */
    String getDisplayName(Locale locale);

    /**
     * Get a list of all processes that can be applied to jahia workflow
     *
     * @return A List of process ids
     */
    List<String> getAvailableProcesses();

    /**
     * Get the name of the process.
     *
     * @param processName the name of the process
     * @param locale      current locale
     * @return localized process name
     */
    String getProcessName(String processName, Locale locale);

    /**
     * Start a new instance of a process. This is called on the first modification of a jahia object.
     *
     * @param processName
     * @param objectKey
     * @param languageCode
     * @param jParams
     */
    String initProcess(String processName, String objectKey, String languageCode, ProcessingContext jParams);

    /**
     * Check if a process is already started for this object
     *
     * @param processName
     * @param objectKey
     * @param languageCode
     */
    boolean isProcessStarted(String processName, String objectKey, String languageCode);

    /**
     * Cancel a process currently running. All running operations should be aborted
     *
     * @param processName
     * @param objectKey
     * @param languageCode
     */
    void abortProcess(String processName, String objectKey, String languageCode, ProcessingContext jParams);

    /**
     * Get the list of all possible actions used in a process.
     *
     * @param processName
     */
    List<String> getAllActions(String processName);

    /**
     * Get the list of all possible roles used in a process.
     *
     * @param processName
     */
    List<String> getAllActionRoles(String processName);

    boolean hasRightsOnAction(ContentObject object, String action, JahiaUser user) throws JahiaException;

    Collection<String> getAvailableActions(String processName, String objectKey, String languageCode, ProcessingContext jParams);

    String getActionName(String processName, String actionName, Locale locale);

    String getParameterForm(String processName, String actionName, ProcessingContext jParams);

    boolean sendAction(String processName, String objectKey, String languageCode, String actionName, ProcessingContext jParams, ActivationTestResults activationTestResults);

    boolean isUserAuthorizedForWorkflow(String processName, ContentObject objectKey, JahiaUser user);

    boolean isUserAuthorizedForAllFollowingSteps(String processName, ContentObject objectKey, JahiaUser user, Set<String> languageCodes);

    boolean isUserAuthorizedForNextStep(String processName, ContentObject objectKey, JahiaUser user, Set<String> languageCodes);

    List<ExternalWorkflowHistoryEntry> getWorkflowHistoryByObject(String objectKey);

    ExternalWorkflowInstanceCurrentInfos getCurrentInfo(String objectKey, String languageCode);

    /**
     * Check if the already started process needs to be aborted and restarted
     * again. This method can be used to check if the object is in the edit mode
     * in the middle of the workflow process to allow content updates without
     * restarting the complete workflow.
     *
     * @param processName  the name of the process
     * @param objectKey    the object key
     * @param languageCode the language code
     * @return <code>true</code> if the workflow needs to be restarted after
     *         content update
     */
    boolean needToRestartProcess(String processName, String objectKey, String languageCode);
}
