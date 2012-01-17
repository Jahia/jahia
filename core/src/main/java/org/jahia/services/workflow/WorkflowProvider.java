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

package org.jahia.services.workflow;

import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * BPM engine provider.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 2 févr. 2010
 */
public interface WorkflowProvider {
    String getKey();

    List<WorkflowDefinition> getAvailableWorkflows(Locale locale);

    WorkflowDefinition getWorkflowDefinitionByKey(String key, Locale locale);

    List<Workflow> getActiveWorkflowsInformations(List<String> processIds, Locale locale);

    String startProcess(String processKey, Map<String,Object> args);

    void signalProcess(String processId, String transitionName, Map<String, Object> args);

    void signalProcess(String processId, String transitionName, String signalName, Map<String, Object> args);

    void abortProcess(String processId);

    Workflow getWorkflow(String processId, Locale locale);

    Set<WorkflowAction> getAvailableActions(String processId, Locale locale);

    List<WorkflowTask> getTasksForUser(JahiaUser user, Locale locale);

    List<Workflow> getWorkflowsForDefinition(String definition, Locale locale);

    List<Workflow> getWorkflowsForUser(JahiaUser user, Locale locale);

    void assignTask(String taskId, JahiaUser user);

    void completeTask(String taskId, String outcome, Map<String, Object> args);

    void addParticipatingGroup(String taskId, JahiaGroup group, String role);

    void deleteTask(String taskId, String reason);

    List<String> getConfigurableRoles(String processKey);

    void addComment(String processId, String comment, String user);

    WorkflowTask getWorkflowTask(String taskId, Locale locale);

    List<HistoryWorkflow> getHistoryWorkflowsForNode(String nodeId, Locale locale);

    List<HistoryWorkflow> getHistoryWorkflowsForPath(String path, Locale locale);

    /**
     * Returns a list of process instance history records for the specified
     * process IDs. This method also returns "active" (i.e. not completed)
     * workflow process instance.
     * 
     * @param processIds list of process IDs to retrieve history records for
     * @param locale
     * @return a list of process instance history records for the specified
     *         process IDs
     */
    List<HistoryWorkflow> getHistoryWorkflows(List<String> processIds, Locale locale);

    /**
     * Returns a list of history records for workflow tasks.
     * This method also returns not completed tasks.
     * 
     * @param processId the process instance ID
     * @param locale
     * @return a list of history records for workflow tasks
     */
    List<HistoryWorkflowTask> getHistoryWorkflowTasks(String processId, Locale locale);

    void deleteProcess(String processId);
}