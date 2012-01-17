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

package org.jahia.ajax.gwt.client.data.workflow.history;

import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowTask;

/**
 * Represents a history record with a workflow process instance.
 * 
 * @author Sergiy Shyrkov
 */
public class GWTJahiaWorkflowHistoryItem extends BaseTreeModel {

    private static final long serialVersionUID = 3266320499313875823L;

    private List<GWTJahiaWorkflowTask> availableTasks;

    /**
     * Initializes an instance of this class.
     */
    public GWTJahiaWorkflowHistoryItem() {
        super();
    }

    /**
     * Initializes an instance of this class.
     *
     * @param id
     * @param name
     * @param displayName the display name for this item
     * @param processId the ID of the workflow process instance
     * @param provider the workflow provider key
     * @param finished is the workflow completed?
     * @param user
     * @param startDate the start data of the process
     * @param endDate the end date of the process; is <code>null</code> in case
*            the process is still running
     * @param duration the process duration
     */
    public GWTJahiaWorkflowHistoryItem(String id, String name, String displayName, String processId, String provider,
                                       boolean finished, String user, Date startDate, Date endDate, Long duration) {
        this();
        setName(name);
        setDisplayName(displayName);
        setProcessId(processId);
        setProvider(provider);
        setFinished(finished);
        setUser(user);
        setStartDate(startDate);
        setEndDate(endDate);
        setDuration(duration);
        setId(id);
    }

    public String getId() {
        return get("id");
    }

    public void setId(String id) {
        set("id", id);
    }

    /**
     * @return the duration
     */
    public Long getDuration() {
        return get("duration");
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(Long duration) {
        set("duration", duration);
    }

    public String getDisplayName() {
        return get("displayName");
    }

    public void setDisplayName(String displayName) {
        set("displayName", displayName);
    }

    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name", name);
    }


    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return get("endDate");
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(Date endDate) {
        set("endDate", endDate);
    }



    /**
     * @return the processId
     */
    public String getProcessId() {
        return get("processId");
    }

    /**
     * @param processId the processId to set
     */
    public void setProcessId(String processId) {
        set("processId", processId);
    }


    public String getProvider() {
        return get("provider");
    }

    public void setProvider(String provider) {
        set("provider", provider);
    }



    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return get("startDate");
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        set("startDate", startDate);
    }


    public String getUser() {
        return get("user");
    }

    public void setUser(String user) {
        set("user", user);
    }

    /**
     * @return the finished
     */
    public boolean isFinished() {
        Boolean finished = (Boolean) get("finished");
        return finished != null && finished;
    }

    /**
     * @param finished the finished to set
     */
    public void setFinished(boolean finished) {
        set("finished", Boolean.valueOf(finished));
    }

    public List<GWTJahiaWorkflowTask> getAvailableTasks() {
        return availableTasks;
    }

    public void setAvailableTasks(List<GWTJahiaWorkflowTask> availableTasks) {
        this.availableTasks = availableTasks;
    }
}
