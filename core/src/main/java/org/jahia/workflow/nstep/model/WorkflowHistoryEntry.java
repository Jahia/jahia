/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 package org.jahia.workflow.nstep.model;

import java.util.Date;

/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

/**
 * Represent an entry in the history for a particular workflow.
 *
 * @hibernate.class table="jahia_nstep_workflowhistory" lazy="false"
 */
public class WorkflowHistoryEntry extends org.jahia.workflow.nstep.model.BaseObject {
// ------------------------------ FIELDS ------------------------------

    private static final long serialVersionUID = -7777192597383956004L;
    private Date date;
    private Long id;
    private String action;
    private String author;
    private String comment;
    private String languageCode;
    private String objectKey;
    private String process;
    private String user;

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * @return Returns the action
     *
     * @hibernate.property column="action" not-null="true"
     */
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return Returns the author
     *
     * @hibernate.property column="author" not-null="true"
     */
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return Returns the comment
     *
     * @hibernate.property column="message" type="text" length="1048576" not-null="false"
     */
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return Returns the date
     *
     * @hibernate.property column="actionDate" not-null="true" type="timestamp"
     */
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return Returns the id.
     *
     * @hibernate.id column="id" generator-class="assigned" unsaved-value="null"
     *
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return Returns the language code
     *
     * @hibernate.property column="languageCode" not-null="true"
     */
    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    /**
     * @return Returns the objectKey
     *
     * @hibernate.property column="objectKey" not-null="true"
     */
    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * @return Returns the process
     *
     * @hibernate.property column="process" not-null="true"
     */
    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    /**
     * @return Returns the user name
     *
     * @hibernate.property column="username" not-null="true"
     */
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}

