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
