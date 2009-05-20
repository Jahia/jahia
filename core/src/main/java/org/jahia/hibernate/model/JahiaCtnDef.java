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
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @hibernate.class table="jahia_ctn_def"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaCtnDef implements Serializable {

    private static final long serialVersionUID = 1249917416490388459L;

// ------------------------------ FIELDS ------------------------------

    /**
     * persistent field
     */
    private Integer id;

    private Map<Object, Object> properties;

    /**
     * nullable persistent field
     */
    private Integer jahiaSite;

    /**
     * persistent field
     */
    private Set<JahiaCtnDefProperty> subDefinitions;

    /**
     * identifier field
     */
    private String name;

    private String containerType;

    private String parentCtnName;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * default constructor
     */
    public JahiaCtnDef() {
        properties = new ConcurrentHashMap<Object, Object>(53);
        subDefinitions = new HashSet<JahiaCtnDefProperty>(53);
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * @hibernate.id generator-class="assigned"
     * column="id_jahia_ctn_def"
     *
     */
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @hibernate.property column="jahiaid_jahia_ctn_def"
     */
    public Integer getJahiaSiteId() {
        return this.jahiaSite;
    }

    public void setJahiaSiteId(Integer jahiaSite) {
        this.jahiaSite = jahiaSite;
    }

    /**
     * @hibernate.property column="name_jahia_ctn_def"
     * length="250"
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * @hibernate.property column="ctntype_jahia_ctndef_def"
     * length="150"
     */
    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    /**
     * @hibernate.property column="pctnname_jahia_ctndef_def"
     * length="250"
     */
    public String getParentCtnName() {
        return parentCtnName;
    }

    public void setParentCtnName(String ctnName) {
        this.parentCtnName = ctnName;
    }

    /**
     * @hibernate.map lazy="true" inverse="false" cascade="all" table="jahia_ctndef_prop"
     * @hibernate.collection-key column="id_jahia_ctn_def" type="int"
     * @hibernate.collection-index column="name_jahia_ctndef_prop" type="string"
     * @hibernate.collection-element column="value_jahia_ctndef_prop" type="string"
     * @hibernate.collection-cache usage="nonstrict-read-write"
     * @return
     */
    public Map<Object, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<Object, Object> properties) {
        this.properties = properties;
    }

    /**
     * @hibernate.set lazy="true"
     * inverse="true"
     * cascade="all"
     * @hibernate.collection-key column="ctndefid_jahia_ctn_def_prop"
     * @hibernate.collection-one-to-many class="org.jahia.hibernate.model.JahiaCtnDefProperty"
     * @hibernate.collection-cache usage="nonstrict-read-write"
     */
    public Set<JahiaCtnDefProperty> getSubDefinitions() {
        return this.subDefinitions;
    }

    public void setSubDefinitions(Set<JahiaCtnDefProperty> subDefinitions) {
        this.subDefinitions = subDefinitions;
    }

// ------------------------ CANONICAL METHODS ------------------------

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaCtnDef castOther = (JahiaCtnDef) obj;
            return new EqualsBuilder().append(this.getId(), castOther.getId()).isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

    public String toString() {
        return new StringBuffer(getClass().getName()).append("id="+getId()).toString();
    }
}

