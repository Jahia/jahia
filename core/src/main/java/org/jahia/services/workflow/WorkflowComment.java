/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * User: toto
 * Date: Oct 6, 2010
 * Time: 11:37:02 AM
 * 
 */
public class WorkflowComment implements Serializable {
    private static final long serialVersionUID = 1342490305138328122L;

    private String comment;
    
    private Date time;
    
    private String user;
    
    /**
     * Initializes an instance of this class.
     */
    public WorkflowComment() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * @param comment the comment text
     * @param time the comment creation time
     * @param user the key of the user, who created this comment
     */
    public WorkflowComment(String comment, Date time, String user) {
        this();
        this.comment = comment;
        this.time = time;
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public Date getTime() {
        return time;
    }

    public String getUser() {
        return user;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
