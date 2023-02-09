/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
