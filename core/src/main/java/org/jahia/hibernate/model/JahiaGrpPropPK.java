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
public class JahiaGrpPropPK extends CachedPK implements Serializable {

    /**
     * identifier field
     */
    private Integer id;

    /**
     * identifier field
     */
    private String name;

    /**
     * identifier field
     */
    private String provider;

    /**
     * identifier field
     */
    private String groupKey;

    /**
     * full constructor
     */
    public JahiaGrpPropPK(Integer idJahiaGrp, String nameJahiaGrpProp, String providerJahiaGrpProp,
                          String grpkeyJahiaGrpProp) {
        this.id = idJahiaGrp;
        this.name = nameJahiaGrpProp;
        this.provider = providerJahiaGrpProp;
        this.groupKey = grpkeyJahiaGrpProp;
    }

    /**
     * default constructor
     */
    public JahiaGrpPropPK() {
    }

    /**
     * @hibernate.property column="id_jahia_grp"
     * length="11"
     */
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        updated();
        this.id = id;
    }

    /**
     * @hibernate.property column="name_jahia_grp_prop"
     * length="50"
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        updated();
        this.name = name;
    }

    /**
     * @hibernate.property column="provider_jahia_grp_prop"
     * length="50"
     */
    public String getProvider() {
        return this.provider;
    }

    public void setProvider(String provider) {
        updated();
        this.provider = provider;
    }

    /**
     * @hibernate.property column="grpkey_jahia_grp_prop"
     * length="200"
     */
    public String getGroupKey() {
        return this.groupKey;
    }

    public void setGroupKey(String groupKey) {
        updated();
        this.groupKey = groupKey;
    }

    public String effectiveToString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .append("name", getName())
                .append("provider", getProvider())
                .append("groupKey", getGroupKey())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaGrpPropPK castOther = (JahiaGrpPropPK) obj;
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .append(this.getName(), castOther.getName())
                .append(this.getProvider(), castOther.getProvider())
                .append(this.getGroupKey(), castOther.getGroupKey())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId())
                .append(getName())
                .append(getProvider())
                .append(getGroupKey())
                .toHashCode();
    }

}
