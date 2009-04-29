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
 * Represent an instance of a workflow for an object in a language.
 *
 * @hibernate.class table="jahia_nstep_workflowinstance" lazy="false"
 */
public class WorkflowInstance {
// ------------------------------ FIELDS ------------------------------

    private Long id;
    private String authorEmail;
    private String languageCode;
    private String objectKey;
    private User user;
    private Workflow workflow;
    private WorkflowStep step;
    private Date startDate;
// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * @return Returns the author key
     *
     * @hibernate.property column="authorEmail" not-null="false"
     */
    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
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
     * @return Returns the object key
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
     * @return Returns the current step of this instance in the associated workflow.
     *
     * @hibernate.many-to-one class="org.jahia.workflow.nstep.model.WorkflowStep"
     */
    public WorkflowStep getStep() {
        return step;
    }

    public void setStep(WorkflowStep step) {
        this.step = step;
    }

    /**
     * @return Returns the user that perform the last action.
     *
     * @hibernate.many-to-one class="org.jahia.workflow.nstep.model.User" column="user_id"
     */
    public org.jahia.workflow.nstep.model.User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return Returns the workflow associated.
     *
     * @hibernate.many-to-one class="org.jahia.workflow.nstep.model.Workflow"
     */
    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    /**
     *
     * @return The date of instanciation of this workflowstep
     *
     * @hibernate.property type="timestamp"
     */
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}

