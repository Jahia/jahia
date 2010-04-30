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
 * History record for a workflow process instance.
 * 
 * @author Sergiy Shyrkov
 */
public class HistoryWorkflow extends WorkflowBase {

    private String definitionId;

    private Long duration;

    private String endActivityName;

    private Date endTime;

    private boolean finished;

    private String id;

    private Date startTime;

    /**
     * Initializes an instance of this class.
     * 
     * @param id workflow instance ID
     * @param name the name of the item
     * @param provider the provider key
     */
    public HistoryWorkflow(String id, String name, String provider) {
        super(name, provider);
        this.id = id;
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param id workflow instance ID
     * @param name the name of the item
     * @param provider the provider key
     * @param startTime the start point of the process instance
     * @param endTime the end point of the process instance or <code>null</code>
     *            if it is not completed yet
     * @param endActivityName the name of the last activity
     */
    public HistoryWorkflow(String id, String name, String provider, Date startTime, Date endTime, String endActivityName) {
        this(id, name, provider);
        this.startTime = startTime;
        setEndTime(endTime);
        this.endActivityName = endActivityName;
    }

    /**
     * @return the definitionId
     */
    public String getDefinitionId() {
        return definitionId;
    }

    /**
     * duration of the process instance in milliseconds or null if the process
     * instance has not yet ended
     */
    public Long getDuration() {
        return duration;
    }

    /**
     * Returns the name of the end state that was reached when the process was
     * ended.
     */
    public String getEndActivityName() {
        return endActivityName;
    }

    /**
     * Returns the time when the process instance ended (only not null if the
     * process instance already ended).
     * 
     * @return the time when the process instance ended (only not null if the
     *         process instance already ended)
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Returns the workflow instance ID.
     * 
     * @return the workflow instance ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the time when the process instance was started.
     * 
     * @return the time when the process instance was started
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Returns <code>true</code> if this process instance is already completed.
     * 
     * @return <code>true</code> if this process instance is already completed
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * @param definitionId the definitionId to set
     */
    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    /**
     * @param endActivityName the endActivityName to set
     */
    public void setEndActivityName(String endActivityName) {
        this.endActivityName = endActivityName;
    }

    /**
     * Sets the end time for the workflow process instance.
     * 
     * @param endTime the endTime to set
     */
    public void setEndTime(Date endTime) {
        if (endTime == null) {
            return;
        }
        this.endTime = endTime;
        this.duration = endTime.getTime() - startTime.getTime();
        this.finished = true;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

}
