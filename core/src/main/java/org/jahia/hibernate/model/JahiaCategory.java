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
import org.jahia.services.categories.CategoryBean;

import java.io.Serializable;

/**
 * @hibernate.class table="jahia_category"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaCategory implements Serializable,CategoryBean {

    /**
     * identifier field
     */
    private Integer id;

    /**
     * persistent field
     */
    private String key;

    /**
     * persistent field
     */
    private org.jahia.hibernate.model.JahiaAcl jahiaAcl;

    /**
     * full constructor
     */
    public JahiaCategory(Integer idCategory, String keyCategory, org.jahia.hibernate.model.JahiaAcl jahiaAcl) {
        this.id = idCategory;
        this.key = keyCategory;
        this.jahiaAcl = jahiaAcl;
    }

    /**
     * default constructor
     */
    public JahiaCategory() {
    }

    /**
     * @hibernate.id generator-class="assigned"
     * type="java.lang.Integer"
     * column="id_category"
     *
     */
    public Integer getID() {
        return this.id;
    }

    public int getId() {
        if(id==null)
        return 0;
        return this.id.intValue();
    }


    public void setID(Integer id) {
        this.id = id;
    }

    /**
     * @hibernate.property column="key_category"
     * unique="true"
     * length="250"
     * not-null="true"
     */
    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getAclID() {
        return jahiaAcl.getId().intValue();
    }

    /**
     * @hibernate.many-to-one not-null="true"
     * @hibernate.column name="aclid_category"
     */
    public org.jahia.hibernate.model.JahiaAcl getJahiaAcl() {
        return this.jahiaAcl;
    }

    public void setJahiaAcl(org.jahia.hibernate.model.JahiaAcl jahiaAcl) {
        this.jahiaAcl = jahiaAcl;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaCategory castOther = (JahiaCategory) obj;
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId())
                .toHashCode();
    }

}
