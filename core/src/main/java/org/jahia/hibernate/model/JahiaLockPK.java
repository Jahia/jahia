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
 package org.jahia.hibernate.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @author Hibernate CodeGenerator
 */
public class JahiaLockPK extends CachedPK implements Serializable {

    /**
     * identifier field
     */
    private String name;

    /**
     * identifier field
     */
    private Integer targetID;

    /**
     * identifier field
     */
    private String action;

    /**
     * nullable persistent field
     */
    private String contextID;

    /**
     * full constructor
     */
    public JahiaLockPK(String nameLocks, Integer targetidLocks, String actionLocks, String contextID) {
        this.name = nameLocks;
        this.targetID = targetidLocks;
        this.action = actionLocks;
        this.contextID = contextID;
    }

    /**
     * default constructor
     */
    public JahiaLockPK() {
    }

    /**
     * @hibernate.property column="name_locks"
     * length="50"
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        updated();
        this.name = name;
    }

    /**
     * @hibernate.property column="targetid_locks"
     * length="11"
     */
    public Integer getTargetID() {
        return this.targetID;
    }

    public void setTargetID(Integer targetID) {
        updated();
        this.targetID = targetID;
    }

    /**
     * @hibernate.property column="action_locks"
     * length="50"
     */
    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        updated();
        this.action = action;
    }

    /**
     * @hibernate.property column="context_locks"
     * length="80"
     */
    public String getContextID() {
        return this.contextID;
    }

    public void setContextID(String contextID) {
        this.contextID = contextID;
    }

    public String effectiveToString() {
        return new ToStringBuilder(this)
                .append("name", getName())
                .append("targetID", getTargetID())
                .append("action", getAction())
                .append("contextID", getContextID())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaLockPK castOther = (JahiaLockPK) obj;
            return new EqualsBuilder()
                .append(this.getName(), castOther.getName())
                .append(this.getTargetID(), castOther.getTargetID())
                .append(this.getAction(), castOther.getAction())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getName())
                .append(getTargetID())
                .append(getAction())
                .toHashCode();
    }

}
