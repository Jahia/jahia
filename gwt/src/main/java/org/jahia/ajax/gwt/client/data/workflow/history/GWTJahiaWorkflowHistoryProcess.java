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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.data.workflow.history;

import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflow;

import java.util.Date;

/**
 * Represents a history record with a workflow process instance.
 *
 * @author Sergiy Shyrkov
 */
public class GWTJahiaWorkflowHistoryProcess extends GWTJahiaWorkflowHistoryItem {

    private static final long serialVersionUID = 7186140866566579234L;

    /**
     * Initializes an instance of this class.
     */
    public GWTJahiaWorkflowHistoryProcess() {
        super();
    }

    /**
     * Initializes an instance of this class.
     *
     * @param name
     * @param displayName the display name for this item
     * @param processId the ID of the workflow process instance
     * @param provider the workflow provider key
     * @param finished is the workflow completed?
     * @param startDate the start data of the process
     * @param endDate the end date of the process; is <code>null</code> in case
     *        the process is still running
     * @param duration the process duration
     * @param workflowStartUser
     */
    public GWTJahiaWorkflowHistoryProcess(String name, String displayName, String processId, String provider, String definitionKey, boolean finished, Date startDate, Date endDate, Long duration,
                                          String workflowStartUser, String nodeId) {
        super(processId, name, displayName, processId,provider, finished, workflowStartUser, startDate, endDate, duration);
        setDefinitionKey(definitionKey);
        setNodeId(nodeId);
    }

    public String getDefinitionKey() {
        return get("definitionKey");
    }

    public void setDefinitionKey(String definitionKey) {
        set("definitionKey", definitionKey);
    }

    public String getNodeId() {
        return get("nodeId");
    }

    public void setNodeId(String nodeId) {
        set("nodeId", nodeId);
    }

    public GWTJahiaWorkflow getRunningWorkflow() {
        return get("workflow");
    }

    public void setRunningWorkflow(GWTJahiaWorkflow workflow) {
        set("workflow", workflow);
    }


}
