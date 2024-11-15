/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.workflow;

import org.jahia.services.usermanager.JahiaUser;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * BPM engine provider.
 *
 * @author rincevent
 * @since JAHIA 6.5
 */
public interface WorkflowProvider {
    String getKey();

    List<WorkflowDefinition> getAvailableWorkflows(Locale uiLocale);

    WorkflowDefinition getWorkflowDefinitionByKey(String key, Locale uiLocale);

    List<Workflow> getActiveWorkflowsInformations(List<String> processIds, Locale uiLocale);

    /**
     * Start a process instance previously registered
     *
     * @param processKey
     * @param args
     * @return the process instance identifier
     */
    String startProcess(String processKey, Map<String, Object> args);

    void abortProcess(String processId);

    Workflow getWorkflow(String processId, Locale uiLocale);

    /**
     * Returns the next possible connections for a given process (usually only for User tasks).
     *
     * @param processId
     * @param uiLocale current UI display locale
     * @return
     */
    Set<WorkflowAction> getAvailableActions(String processId, Locale uiLocale);

    List<WorkflowTask> getTasksForUser(JahiaUser user, Locale uiLocale);

    List<Workflow> getWorkflowsForDefinition(String definition, Locale uiLocale);

    List<Workflow> getWorkflowsForUser(JahiaUser user, Locale uiLocale);

    void assignTask(String taskId, JahiaUser user);

    void completeTask(String taskId, JahiaUser jahiaUser, String outcome, Map<String, Object> args);

    void addComment(String processId, String comment, String user);

    WorkflowTask getWorkflowTask(String taskId, Locale uiLocale);

    List<HistoryWorkflow> getHistoryWorkflowsForNode(String nodeId, Locale uiLocale);

    List<HistoryWorkflow> getHistoryWorkflowsForPath(String path, Locale uiLocale);

    /**
     * Returns a list of process instance history records for the specified
     * process IDs. This method also returns "active" (i.e. not completed)
     * workflow process instance.
     *
     * @param processIds list of process IDs to retrieve history records for
     * @param uiLocale the UI locale
     * @return a list of process instance history records for the specified
     *         process IDs
     */
    List<HistoryWorkflow> getHistoryWorkflows(List<String> processIds, Locale uiLocale);

    /**
     * Returns a list of history records for workflow tasks.
     * This method also returns not completed tasks.
     *
     * @param processId the process instance ID
     * @param uiLocale the UI locale
     * @return a list of history records for workflow tasks
     */
    List<HistoryWorkflowTask> getHistoryWorkflowTasks(String processId, Locale uiLocale);

    void deleteProcess(String processId);
}
