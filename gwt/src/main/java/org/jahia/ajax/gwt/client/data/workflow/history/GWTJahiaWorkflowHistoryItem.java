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
