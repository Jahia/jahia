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

import java.io.Serializable;

/**
 * @author Hibernate CodeGenerator
 */
public class JahiaPagesUsersPropPK extends CachedPK implements Serializable {

    /**
     * identifier field
     */
    private Integer pageId;

    /**
     * identifier field
     */
    private String principalKey;

    /**
     * identifier field
     */
    private String principalType;

    /**
     * identifier field
     */
    private String propType;

    /**
     * identifier field
     */
    private String name;

    public JahiaPagesUsersPropPK() {
    }

    /**
     * full constructor
     */
    public JahiaPagesUsersPropPK(Integer pageId, String principaleKey, String principaleType, String propType, String propName) {
        this.pageId = pageId;
        this.principalKey = principaleKey;
        this.principalType = principaleType;
        this.propType = propType;
        this.name = propName;
    }

    /**
     * @hibernate.property column="page_id"
     * length="11"
     */
    public Integer getPageId() {
        return pageId;
    }

    public void setPageId(Integer pageId) {
        updated();
        this.pageId = pageId;
    }

    /**
     * @hibernate.property column="principal_key"
     * length="70"
     */
    public String getPrincipalKey() {
        return principalKey;
    }

    public void setPrincipalKey(String principalKey) {
        this.principalKey = principalKey;
    }

    /**
     * @hibernate.property column="principal_type"
     * length="40"
     */
    public String getPrincipalType() {
        return principalType;
    }

    public void setPrincipalType(String principalType) {
        this.principalType = principalType;
    }

    /**
     * @hibernate.property column="prop_type"
     * length="40"
     */
    public String getPropType() {
        return propType;
    }

    public void setPropType(String propType) {
        this.propType = propType;
    }

    /**
     * @hibernate.property column="prop_name"
     * length="150"
     */
    public String getName() {
        updated();
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String effectiveToString() {
        return new StringBuffer(getClass().getName())
                .append("pageId=" + getPageId())
                .append("principalKey=" + getPrincipalKey())
                .append("principalType=" + getPrincipalType())
                .append("propType=" + getPropType())
                .append("name=" + getName())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaPagesUsersPropPK castOther = (JahiaPagesUsersPropPK) obj;
            return new EqualsBuilder()
                    .append(this.getPageId(), castOther.getPageId())
                    .append(this.getPrincipalKey(), castOther.getPrincipalKey())
                    .append(this.getPrincipalType(), castOther.getPrincipalType())
                    .append(this.getPropType(), castOther.getPropType())
                    .append(this.getName(), castOther.getName())
                    .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getPageId())
                .append(getPrincipalKey())
                .append(getPrincipalType())
                .append(getPropType())
                .append(getName())
                .toHashCode();
    }
}
