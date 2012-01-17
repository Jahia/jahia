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

import java.util.Date;

/**
 * History record for a workflow process instance.
 * 
 * @author Sergiy Shyrkov
 */
public class HistoryWorkflowBase extends WorkflowBase {

    private Long duration;

    private String user;

    private boolean completed;

    private Date startTime;

    private Date endTime;

    private String processId;

    /**
     * Initializes an instance of this class.
     * 
     * @param processId the ID of the corresponding workflow process instance
     * @param name the name of the item
     * @param provider the provider key
     * @param user
     * @param startTime the start point of the process instance
     * @param endTime the end point of the process instance or <code>null</code>
     */
    public HistoryWorkflowBase(String processId, String name, String provider, String user, Date startTime, Date endTime) {
        super(name, provider);
        this.processId = processId;
        this.startTime = startTime;
        this.user = user;
        setEndTime(endTime);
    }

    /**
     * duration of the process instance in milliseconds or null if the process
     * instance has not yet ended
     */
    public Long getDuration() {
        return duration;
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
     * Returns the time when the process instance was started.
     * 
     * @return the time when the process instance was started
     */
    public Date getStartTime() {
        return startTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getUser() {
        return user;
    }

    /**
     * Returns an ID of the corresponding workflow process instance.
     * 
     * @return an ID of the corresponding workflow process instance
     */
    public String getProcessId() {
        return processId;
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
        this.completed = true;
    }
}
