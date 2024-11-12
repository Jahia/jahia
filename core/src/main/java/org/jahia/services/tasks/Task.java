/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.tasks;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a single task item.
 *
 * @author Sergiy Shyrkov
 */
public class Task implements Serializable {

    private static final long serialVersionUID = 2865115744967740455L;

    public enum Priority {
        HIGH, LOW, MEDIUM, VERY_HIGH, VERY_LOW;
    }

    public enum State {
        ACTIVE, FINISHED, SUSPENDED;
    }

    private String description;

    private Date dueDate;

    private Priority priority = Priority.MEDIUM;
    private State state = State.ACTIVE;
    private String title;
    /**
     * Initializes an instance of this class.
     */
    public Task() {
        super();
    }
    /**
     * Initializes an instance of this class.
     *
     * @param title the title of the new task
     * @param description a short description for this task
     */
    public Task(String title, String description) {
        this();
        this.title = title;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public State getState() {
        return state;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setState(State status) {
        this.state = status;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
