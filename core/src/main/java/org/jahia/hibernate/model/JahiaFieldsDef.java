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
import java.util.Map;

/**
 * @hibernate.class table="jahia_fields_def" lazy="false"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaFieldsDef implements Serializable {
// ------------------------------ FIELDS ------------------------------

    private static final long serialVersionUID = 7447354819272528455L;
    /**
     * persistent field
     */
    private Integer id;
    private Integer isMetadata = new Integer(0);
    private Map<Object, Object> properties;
    private String ctnName;
    /**
     * nullable persistent field
     */
    private Integer jahiaSite;

    /**
     * identifier field
     */
    private String name;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * default constructor
     */
    public JahiaFieldsDef() {
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * @hibernate.id generator-class="assigned"
     * column="id_jahia_fields_def"
     *
     */
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @hibernate.property column="ismdata_jahia_fields_def" not-null="false"
     * @return
     */
    public Integer getIsMetadata() {
        return isMetadata;
    }

    public void setIsMetadata(Integer isMetadata) {
        this.isMetadata = isMetadata;
    }

    /**
     * @hibernate.property column="jahiaid_jahia_fields_def"
     */
    public Integer getJahiaSiteId() {
        return this.jahiaSite;
    }

    public void setJahiaSiteId(Integer jahiaSite) {
        this.jahiaSite = jahiaSite;
    }
    
    /**
     * @hibernate.property column="ctnname_jahia_fields_def"
     * length="250"
     */
    public String getCtnName() {
        return ctnName;
    }

    public void setCtnName(String ctnName) {
        this.ctnName = ctnName;
    }

    /**
     * @hibernate.property column="name_jahia_fields_def"
     * length="250"
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @hibernate.map lazy="true" inverse="true" cascade="save-update" table="jahia_fields_def_extprop"
     * @hibernate.collection-key column="id_jahia_fields_def" type="int"
     * @hibernate.collection-index column="prop_name" type="string"
     * @hibernate.collection-element column="prop_value" type="string"
     * @hibernate.collection-cache usage="nonstrict-read-write"
     * @return
     */
    public Map<Object, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<Object, Object> properties) {
        this.properties = properties;
    }

// ------------------------ CANONICAL METHODS ------------------------

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaFieldsDef castOther = (JahiaFieldsDef) obj;
            return new EqualsBuilder()
                .append(this.getJahiaSiteId(), castOther.getJahiaSiteId())
                .append(this.getName(), castOther.getName())
                .isEquals();
        }
        return false;        
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getJahiaSiteId())
                .append(getName())
                .toHashCode();
    }

    public String toString() {
        return new StringBuffer(getClass().getName())
                .append("site="+getJahiaSiteId())
                .append("name="+getName())
                .toString();
    }
}

