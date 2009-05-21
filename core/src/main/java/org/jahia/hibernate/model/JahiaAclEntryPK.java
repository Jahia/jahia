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
