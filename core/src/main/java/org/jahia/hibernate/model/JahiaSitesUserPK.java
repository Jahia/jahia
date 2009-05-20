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
public class JahiaSitesUserPK extends CachedPK implements Serializable {

    /**
     * identifier field
     */
    private String username;

    /**
     * identifier field
     */
    private Integer siteId;

    /**
     * full constructor
     */
    public JahiaSitesUserPK(String usernameSitesUsers, Integer site) {
        this.username = usernameSitesUsers;
        this.siteId = site;
    }

    /**
     * default constructor
     */
    public JahiaSitesUserPK() {
    }

    /**
     * @hibernate.property column="username_sites_users"
     * length="50"
     */
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        updated();
        this.username = username;
    }

    /**
     * @hibernate.property column="siteid_sites_users"
     */
    public Integer getSiteId() {
        return this.siteId;
    }

    public void setSiteId(Integer siteId) {
        updated();
        this.siteId = siteId;
    }

    public String effectiveToString() {
        return new ToStringBuilder(this)
                .append("username", getUsername())
                .append("siteidSitesUsers", getSiteId())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaSitesUserPK castOther = (JahiaSitesUserPK) obj;
            return new EqualsBuilder()
                .append(this.getUsername(), castOther.getUsername())
                .append(this.getSiteId(), castOther.getSiteId())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getUsername())
                .append(getSiteId())
                .toHashCode();
    }

}
