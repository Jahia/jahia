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
