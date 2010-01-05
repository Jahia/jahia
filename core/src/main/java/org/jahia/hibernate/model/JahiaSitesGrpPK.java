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
    private Integer site;

    /**
     * full constructor
     */
    public JahiaSitesGrpPK(String grpnameSitesGrps, Integer siteidSitesGrps) {
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
    public Integer getSite() {
        return this.site;
    }

    public void setSite(Integer site) {
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
