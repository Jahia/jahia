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


import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jahia.content.ContentObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ActivationTestResults;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 26, 2003
 * Time: 2:51:22 PM
 * To change this template use Options | File Templates.
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

    boolean sendAction(String processName, String objectKey, String languageCode, String actionName, ProcessingContext jParams, ActivationTestResults activationTestResults, Map<RecipientInfo, Object> userNotifData);

    void sendResults(ProcessingContext processingContext, ActivationTestResults activationTestResults, Map<RecipientInfo, Object> userNotifData);

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
