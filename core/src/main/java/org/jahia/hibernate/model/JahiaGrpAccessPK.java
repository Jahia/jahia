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
public class JahiaGrpAccessPK extends CachedPK implements Serializable {

    /**
     * identifier field
     */
    private String memberKey;

    /**
     * identifier field
     */
    private String groupKey;

    /**
     * identifier field
     */
    private Integer memberType;

    /**
     * full constructor
     */
    public JahiaGrpAccessPK(String memberKey, String groupKey, Integer memberType) {
        this.memberKey = memberKey;
        this.groupKey = groupKey;
        this.memberType = memberType;
    }

    /**
     * default constructor
     */
    public JahiaGrpAccessPK() {
    }

    /**
     * @hibernate.property column="id_jahia_member"
     * length="150"
     */
    public String getMemberKey() {
        return this.memberKey;
    }

    public void setMemberKey(String memberKey) {
        updated();
        this.memberKey = memberKey;
    }

    /**
     * @hibernate.property column="id_jahia_grps"
     * length="150"
     */
    public String getGroupKey() {
        return this.groupKey;
    }

    public void setGroupKey(String groupKey) {
        updated();
        this.groupKey = groupKey;
    }

    /**
     * @hibernate.property column="membertype_grp_access"
     * length="11"
     */
    public Integer getMemberType() {
        return this.memberType;
    }

    public void setMemberType(Integer memberType) {
        updated();
        this.memberType = memberType;
    }

    public String effectiveToString() {
        return new ToStringBuilder(this)
                .append("memberKey", getMemberKey())
                .append("groupKey", getGroupKey())
                .append("memberType", getMemberType())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaGrpAccessPK castOther = (JahiaGrpAccessPK) obj;
            return new EqualsBuilder()
                .append(this.getMemberKey(), castOther.getMemberKey())
                .append(this.getGroupKey(), castOther.getGroupKey())
                .append(this.getMemberType(), castOther.getMemberType())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getMemberKey())
                .append(getGroupKey())
                .append(getMemberType())
                .toHashCode();
    }

}
