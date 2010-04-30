/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.workflow;

import java.util.Date;

/**
 * Represents a single history record for the workflow process instance activity
 * task.
 * 
 * @author Sergiy Shyrkov
 */
public class HistoryWorkflowTask extends HistoryWorkflowAction {

    private String assignee;

    private boolean completed;

    private String outcome;

    /**
     * Initializes an instance of this class.
     * 
     * @param workflowId the ID of the corresponding workflow process instance
     * @param name the name of the item
     * @param provider the provider key
     */
    public HistoryWorkflowTask(String workflowId, String name, String provider) {
        super(workflowId, name, provider);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param workflowId the ID of the corresponding workflow process instance
     * @param name the name of the item
     * @param provider the provider key
     * @param startTime the start point of the process instance
     * @param endTime the end point of the process instance or <code>null</code>
     *            if it is not completed yet
     * @param outcome the task outcome
     * @param asignee the key of the user, which executed the task
     */
    public HistoryWorkflowTask(String workflowId, String name, String provider, Date startTime, Date endTime,
            String outcome, String asignee) {
        super(workflowId, name, provider, startTime, endTime);
        this.outcome = outcome;
        this.assignee = asignee;
    }

    /**
     * @return the assignee
     */
    public String getAssignee() {
        return assignee;
    }

    /**
     * @return the outcome
     */
    public String getOutcome() {
        return outcome;
    }

    /**
     * @return the completed
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * @param assignee the assignee to set
     */
    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    /**
     * @param completed the completed to set
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public void setEndTime(Date endTime) {
        super.setEndTime(endTime);
        if (endTime != null) {
            completed = true;
        }
    }

    /**
     * @param outcome the outcome to set
     */
    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
}
