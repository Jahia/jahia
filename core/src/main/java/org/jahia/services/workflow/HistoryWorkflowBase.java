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
package org.jahia.services.workflow;

import java.util.Date;

/**
 * History record for a workflow process instance.
 *
 * @author Sergiy Shyrkov
 */
public class HistoryWorkflowBase extends WorkflowBase {

    private static final long serialVersionUID = 3441279896781857522L;

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
