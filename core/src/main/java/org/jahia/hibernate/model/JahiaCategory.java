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
