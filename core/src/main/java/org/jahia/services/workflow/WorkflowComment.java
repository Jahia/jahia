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
