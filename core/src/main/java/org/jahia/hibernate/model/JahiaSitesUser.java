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
 * @hibernate.class table="jahia_sites_users"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaSitesUser implements Serializable {

    /**
     * identifier field
     */
    private org.jahia.hibernate.model.JahiaSitesUserPK comp_id;

    /**
     * nullable persistent field
     */
    private JahiaUser user;

    /**
     * full constructor
     */
    public JahiaSitesUser(org.jahia.hibernate.model.JahiaSitesUserPK comp_id, JahiaUser useridSitesUsers) {
        this.comp_id = comp_id;
        this.user = useridSitesUsers;
    }

    /**
     * default constructor
     */
    public JahiaSitesUser() {
    }

    /**
     * minimal constructor
     */
    public JahiaSitesUser(org.jahia.hibernate.model.JahiaSitesUserPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.id generator-class="assigned"
     */
    public org.jahia.hibernate.model.JahiaSitesUserPK getComp_id() {
        return this.comp_id;
    }

    public void setComp_id(org.jahia.hibernate.model.JahiaSitesUserPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.many-to-one outer-join="true" column="userid_sites_users"
     * class="org.jahia.hibernate.model.JahiaUser" property-ref="key" update="true" insert="true"
     */
    public JahiaUser getUser() {
        return this.user;
    }

    public void setUser(JahiaUser user) {
        this.user = user;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("comp_id", getComp_id())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaSitesUser castOther = (JahiaSitesUser) obj;
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
