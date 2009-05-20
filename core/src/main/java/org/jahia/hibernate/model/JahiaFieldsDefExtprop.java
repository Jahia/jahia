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
 * @hibernate.class table="jahia_fields_def_extprop"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaFieldsDefExtprop implements Serializable {

    
    /**
     * identifier field
     */
    private org.jahia.hibernate.model.JahiaFieldsDefExtpropPK comp_id;

    /**
     * nullable persistent field
     */
    private String propValue;

    /**
     * default constructor
     */
    public JahiaFieldsDefExtprop() {
    }

    public JahiaFieldsDefExtprop(JahiaFieldsDefExtpropPK comp_id, String propValue) {
        this.comp_id = comp_id;
        this.propValue = propValue;
    }

    /**
     * @hibernate.id generator-class="assigned"
     */
    public org.jahia.hibernate.model.JahiaFieldsDefExtpropPK getComp_id() {
        return this.comp_id;
    }

    public void setComp_id(org.jahia.hibernate.model.JahiaFieldsDefExtpropPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.property column="prop_value"
     * length="255"
     */
    public String getPropValue() {
        return this.propValue;
    }

    public void setPropValue(String propValue) {
        this.propValue = propValue;
    }

    public String toString() {
        return new StringBuffer(getClass().getName())
                .append("comp_id="+getComp_id())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaFieldsDefExtprop castOther = (JahiaFieldsDefExtprop) obj;
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
