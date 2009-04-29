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


/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

/**
 * User model.
 *
 * @hibernate.class table="jahia_nstep_workflowuser" lazy="false"
 */
public class User extends org.jahia.workflow.nstep.model.BaseObject {
// ------------------------------ FIELDS ------------------------------

    private static final long serialVersionUID = -2397313820817175633L;
    private Long id;
    private String login;

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * @return Returns the id.
     *
     * @hibernate.id column="id" generator-class="assigned" unsaved-value="null"
     *
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return Returns the login
     *
     * @hibernate.property column="login" not-null="true"
     */
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}

