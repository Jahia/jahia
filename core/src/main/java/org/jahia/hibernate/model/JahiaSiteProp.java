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
 * @hibernate.class table="jahia_site_prop"
 * select-before-update = "true"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaSiteProp implements Serializable {

    /**
     * identifier field
     */
    private org.jahia.hibernate.model.JahiaSitePropPK comp_id;

    private Integer siteId;

    private String name;

    /**
     * nullable persistent field
     */
    private String value;

    /**
     * full constructor
     */
    public JahiaSiteProp(org.jahia.hibernate.model.JahiaSitePropPK comp_id, String valueJahiaSiteProp) {
        this.comp_id = comp_id;
        this.value = valueJahiaSiteProp;
    }

    /**
     * default constructor
     */
    public JahiaSiteProp() {
    }

    /**
     * minimal constructor
     */
    public JahiaSiteProp(org.jahia.hibernate.model.JahiaSitePropPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.id generator-class="assigned"
     */
    public org.jahia.hibernate.model.JahiaSitePropPK getComp_id() {
        return this.comp_id;
    }

    public void setComp_id(org.jahia.hibernate.model.JahiaSitePropPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.property column="id_jahia_site"
     * unique="false"
     * update="false" insert="false"
     * @return
     */
    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    /**
     * @hibernate.property column="name_jahia_site_prop"
     * unique="false"
     * update="false" insert="false"
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @hibernate.property column="value_jahia_site_prop"
     * length="255"
     */
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        return new StringBuffer(getClass().getName())
                .append("comp_id="+getComp_id())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaSiteProp castOther = (JahiaSiteProp) obj;
            return new EqualsBuilder()
                .append(this.getComp_id(), castOther.getComp_id())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getComp_id())
                .toHashCode();
    }

}
