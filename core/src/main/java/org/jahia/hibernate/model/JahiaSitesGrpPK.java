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
public class JahiaSitesGrpPK extends CachedPK implements Serializable {

    /**
     * identifier field
     */
    private String groupName;

    /**
     * identifier field
     */
    private JahiaSite site;

    /**
     * full constructor
     */
    public JahiaSitesGrpPK(String grpnameSitesGrps, JahiaSite siteidSitesGrps) {
        this.groupName = grpnameSitesGrps;
        this.site = siteidSitesGrps;
    }

    /**
     * default constructor
     */
    public JahiaSitesGrpPK() {
    }

    /**
     * @hibernate.property column="grpname_sites_grps"
     * length="50"
     */
    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        updated();
        this.groupName = groupName;
    }

    /**
     * @hibernate.many-to-one update="false"
     * insert="false" column="siteid_sites_grps"
     */
    public JahiaSite getSite() {
        return this.site;
    }

    public void setSite(JahiaSite site) {
        updated();
        this.site = site;
    }

    public String effectiveToString() {
        return new ToStringBuilder(this)
                .append("groupName", getGroupName())
                .append("site", getSite())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaSitesGrpPK castOther = (JahiaSitesGrpPK) obj;
            return new EqualsBuilder()
                .append(this.getGroupName(), castOther.getGroupName())
                .append(this.getSite(), castOther.getSite())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getGroupName())
                .append(getSite())
                .toHashCode();
    }

}
