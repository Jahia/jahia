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
public class JahiaAclEntryPK extends CachedPK implements Serializable {

    private static final long serialVersionUID = -4075611401660154615L;
    /**
     * identifier field
     */
    private JahiaAcl id;

    /**
     * identifier field
     */
    private Integer type;

    /**
     * identifier field
     */
    private String target;

    /**
     * full constructor
     */
    public JahiaAclEntryPK(JahiaAcl idJahiaAcl, Integer typeJahiaAclEntries, String targetJahiaAclEntries) {
        this.id = idJahiaAcl;
        this.type = typeJahiaAclEntries;
        this.target = targetJahiaAclEntries;
    }

    /**
     * default constructor
     */
    public JahiaAclEntryPK() {
    }

    /**
     * @hibernate.many-to-one column="id_jahia_acl"
     * cascade="none"
     * class="org.jahia.hibernate.model.JahiaAcl"
     * length="11"
     */
    public JahiaAcl getId() {
        return this.id;
    }

    public void setId(JahiaAcl id) {
        updated();
        this.id = id;
    }

    /**
     * @hibernate.property column="type_jahia_acl_entries"
     * length="11"
     */
    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type) {
        updated();
        this.type = type;
    }

    /**
     * @hibernate.property column="target_jahia_acl_entries"
     * length="50"
     */
    public String getTarget() {
        return this.target;
    }

    public void setTarget(String target) {
        updated();
        this.target = target;
    }

    public String effectiveToString() {
        return new StringBuffer(getClass().getName())
                    .append("id=" + getId())
                    .append("type=" + getType())
                    .append("target=" + getTarget())
                    .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaAclEntryPK castOther = (JahiaAclEntryPK) obj;
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .append(this.getType(), castOther.getType())
                .append(this.getTarget(), castOther.getTarget())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId())
                .append(getType())
                .append(getTarget())
                .toHashCode();
    }

}
