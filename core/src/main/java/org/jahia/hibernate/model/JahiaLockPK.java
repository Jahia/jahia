/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
