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
 * @hibernate.class table="jahia_appdef_prop"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaAppdefProp implements Serializable {

    /**
     * identifier field
     */
    private org.jahia.hibernate.model.JahiaAppdefPropPK comp_id;

    /**
     * nullable persistent field
     */
    private String value;


    /**
     * full constructor
     */
    public JahiaAppdefProp(org.jahia.hibernate.model.JahiaAppdefPropPK comp_id, String propvalueAppdefProp) {
        this.comp_id = comp_id;
        this.value = propvalueAppdefProp;
    }

    /**
     * default constructor
     */
    public JahiaAppdefProp() {
    }

    /**
     * minimal constructor
     */
    public JahiaAppdefProp(org.jahia.hibernate.model.JahiaAppdefPropPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.id generator-class="assigned"
     */
    public org.jahia.hibernate.model.JahiaAppdefPropPK getComp_id() {
        return this.comp_id;
    }

    public void setComp_id(org.jahia.hibernate.model.JahiaAppdefPropPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.property column="propvalue_appdef_prop"
     * length="250"
     */
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("comp_id", getComp_id())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaAppdefProp castOther = (JahiaAppdefProp) obj;
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
