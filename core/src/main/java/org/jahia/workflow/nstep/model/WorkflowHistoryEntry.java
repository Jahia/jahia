/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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

