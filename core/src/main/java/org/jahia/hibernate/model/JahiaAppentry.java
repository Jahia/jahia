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
 package org.jahia.hibernate.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @hibernate.class table="jahia_appentry"
 * @hibernate.class usage="read-write"
 */
public class JahiaAppentry implements Serializable {

    /**
     * identifier field
     */
    private Integer id;

    /**
     * nullable persistent field
     */
    private Integer applicationId;

    /**
     * nullable persistent field
     */
    private String definitionName;

    /**
     * not nullable persistent field
     */
    private Integer jahiaAclId;

    /**
     * not nullable persistent field
     */
    private String resourceKeyName;
    private Integer expirationTime;
    private String cacheScope;

    /**
     * full constructor
     */
    public JahiaAppentry(Integer idJahiaAppentry, Integer appidJahiaAppentry, String defnameJahiaAppentry) {
        this.id = idJahiaAppentry;
        this.applicationId = appidJahiaAppentry;
        this.definitionName = defnameJahiaAppentry;
    }

    /**
     * default constructor
     */
    public JahiaAppentry() {
    }

    /**
     * minimal constructor
     */
    public JahiaAppentry(Integer idJahiaAppentry) {
        this.id = idJahiaAppentry;
    }

    /**
     * @hibernate.id generator-class="assigned"
     * type="java.lang.Integer"
     * column="id_jahia_appentry"
     *
     */
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @hibernate.property column="appid_jahia_appentry"
     * length="11"
     */
    public Integer getApplicationId() {
        return this.applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * @hibernate.property column="defname_jahia_appentry"
     * length="250"
     */
    public String getDefinitionName() {
        return this.definitionName;
    }

    public void setDefinitionName(String definitionName) {
        this.definitionName = definitionName;
    }


    public Integer getJahiaAclId() {
        return jahiaAclId;
    }

    public void setJahiaAclId(Integer jahiaAclId) {
        this.jahiaAclId = jahiaAclId;
    }

    public String getResourceKeyName() {
        return resourceKeyName;
    }

    public void setResourceKeyName(String resourceKeyName) {
        this.resourceKeyName = resourceKeyName;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaAppentry castOther = (JahiaAppentry) obj;
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId())
                .toHashCode();
    }

    public Integer getExpirationTime() {
        return expirationTime;
    }

    public String getCacheScope() {
        return cacheScope;
    }

    public void setCacheScope(String cacheScope) {
        this.cacheScope = cacheScope;
    }

    public void setExpirationTime(Integer expirationTime) {
        this.expirationTime = expirationTime;
    }
}
