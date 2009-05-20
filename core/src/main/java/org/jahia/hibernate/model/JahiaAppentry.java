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
