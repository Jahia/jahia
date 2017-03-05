/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import java.util.Date;

/**
 * History record for a workflow process instance.
 * 
 * @author Sergiy Shyrkov
 */
public class HistoryWorkflow extends HistoryWorkflowBase {

    private static final long serialVersionUID = 3272275138569315268L;

    private String endActivityName;

    private WorkflowDefinition workflowDefinition;

    private String nodeId;

    /**
     * Initializes an instance of this class.
     * 
     * @param id workflow process instance ID
     * @param workflowDefinition process definition key
     * @param name the name of the item
     * @param provider the provider key
     * @param startTime the start point of the process instance
     * @param endTime the end point of the process instance or <code>null</code>
     *            if it is not completed yet
     * @param endActivityName the name of the last activity
     */
    public HistoryWorkflow(String id, WorkflowDefinition workflowDefinition, String name, String provider, String user, Date startTime, Date endTime, String endActivityName, String nodeId) {
        super(id, name, provider, user, startTime, endTime);
        setWorkflowDefinition(workflowDefinition);
        setNodeId(nodeId);
        this.endActivityName = endActivityName;
    }

    /**
     * Returns the name of the end state that was reached when the process was
     * ended.
     */
    public String getEndActivityName() {
        return endActivityName;
    }

    public WorkflowDefinition getWorkflowDefinition() {
        return workflowDefinition;
    }

    public void setWorkflowDefinition(WorkflowDefinition workflowDefinition) {
        this.workflowDefinition = workflowDefinition;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
