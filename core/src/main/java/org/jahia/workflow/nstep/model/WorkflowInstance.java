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

