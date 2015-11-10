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