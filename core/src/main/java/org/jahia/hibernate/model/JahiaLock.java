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
 * @hibernate.class table="jahia_locks_non_excl" lazy="false"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaLock implements Serializable {

    /**
     * identifier field
     */
    private org.jahia.hibernate.model.JahiaLockPK comp_id;

    /**
     * nullable persistent field
     */
    private String owner;

    /**
     * nullable persistent field
     */
    private Long expirationDate;

    /**
     * nullable persistent field
     */
    private Integer timeout;

    /**
     * nullable persistent field
     */
    private String serverId;

    /**
     * nullable persistent field
     */
    private String stolen;

    /**
     * full constructor
     */
    public JahiaLock(org.jahia.hibernate.model.JahiaLockPK comp_id, String ownerLocks,
                     Integer timeout, Long expirationDate, String stolenLocks) {
        this.comp_id = comp_id;
        this.owner = ownerLocks;
        this.timeout = timeout;
        this.expirationDate = expirationDate;
        this.stolen = stolenLocks;
    }

    /**
     * default constructor
     */
    public JahiaLock() {
    }

    /**
     * minimal constructor
     */
    public JahiaLock(org.jahia.hibernate.model.JahiaLockPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.id generator-class="assigned"
     *
     */
    public org.jahia.hibernate.model.JahiaLockPK getComp_id() {
        return this.comp_id;
    }

    public void setComp_id(org.jahia.hibernate.model.JahiaLockPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.property column="owner_locks"
     * length="50"
     */
    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @hibernate.property column="timeout_locks"
     */
    public Integer getTimeout() {
        return this.timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * @hibernate.property column="expirationDate_locks"
     */
    public Long getExpirationDate() {
        return this.expirationDate;
    }

    public void setExpirationDate(Long timeout) {
        this.expirationDate = timeout;
    }


    /**
     * @hibernate.property column="serverid_locks"
     * length="30"
     */
    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * @hibernate.property column="stolen_locks"
     * length="10"
     */
    public String getStolen() {
        return this.stolen;
    }

    public void setStolen(String stolen) {
        this.stolen = stolen;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("comp_id", getComp_id())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaLock castOther = (JahiaLock) obj;
            return new EqualsBuilder()
                .append(this.getComp_id(), castOther.getComp_id())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getComp_id())
                .toHashCode();
    }

}
