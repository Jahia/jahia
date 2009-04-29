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
import java.util.Set;
import java.util.HashSet;

/**
 * @hibernate.class table="jahia_ctn_def_properties"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaCtnDefProperty implements Serializable {

    private static final long serialVersionUID = -1374545799847770866L;

// ------------------------------ FIELDS ------------------------------

    /**
     * identifier field
     */
    private Integer idJahiaCtnDefProperties;

    /**
     * nullable persistent field
     */
    private Integer pageDefinitionId;

    /**
     * persistent field
     */
    private org.jahia.hibernate.model.JahiaCtnDef jahiaCtnDef;

    /**
     * persistent field
     */
    private Set<JahiaCtnStruct> jahiaCtnStructs;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * default constructor
     */
    public JahiaCtnDefProperty() {
        jahiaCtnStructs = new HashSet<JahiaCtnStruct>(53);
    }

    /**
     * minimal constructor
     */
    public JahiaCtnDefProperty(Integer idJahiaCtnDefProperties, org.jahia.hibernate.model.JahiaCtnDef jahiaCtnDef,
                               Set<JahiaCtnStruct> jahiaCtnStructs) {
        this.idJahiaCtnDefProperties = idJahiaCtnDefProperties;
        this.jahiaCtnDef = jahiaCtnDef;
        this.jahiaCtnStructs = jahiaCtnStructs;
    }

    /**
     * full constructor
     */
    public JahiaCtnDefProperty(Integer idJahiaCtnDefProperties, Integer pagedefidJahiaCtnDefProp,
                               org.jahia.hibernate.model.JahiaCtnDef jahiaCtnDef,
                               Set<JahiaCtnStruct> jahiaCtnStructs) {
        this.idJahiaCtnDefProperties = idJahiaCtnDefProperties;
        this.pageDefinitionId = pagedefidJahiaCtnDefProp;
        this.jahiaCtnDef = jahiaCtnDef;
        this.jahiaCtnStructs = jahiaCtnStructs;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * @hibernate.id generator-class="assigned" unsaved-value="null"
     * type="java.lang.Integer"
     * column="id_jahia_ctn_def_properties"
     *
     */
    public Integer getIdJahiaCtnDefProperties() {
        return this.idJahiaCtnDefProperties;
    }

    public void setIdJahiaCtnDefProperties(Integer idJahiaCtnDefProperties) {
        this.idJahiaCtnDefProperties = idJahiaCtnDefProperties;
    }

    /**
     * @hibernate.many-to-one not-null="true" update="true"
     * insert="true" cascade="all" column="ctndefid_jahia_ctn_def_prop"
     */
    public org.jahia.hibernate.model.JahiaCtnDef getJahiaCtnDef() {
        return this.jahiaCtnDef;
    }

    public void setJahiaCtnDef(org.jahia.hibernate.model.JahiaCtnDef jahiaCtnDef) {
        this.jahiaCtnDef = jahiaCtnDef;
    }

    /**
     * @hibernate.set lazy="false"
     * inverse="true"
     * cascade="all" order-by="rank_jahia_ctn_struct"
     * @hibernate.collection-key column="ctnsubdefid_jahia_ctn_struct"
     * @hibernate.collection-one-to-many class="org.jahia.hibernate.model.JahiaCtnStruct"
     * @hibernate.collection-cache usage="nonstrict-read-write"
     */
    public Set<JahiaCtnStruct> getJahiaCtnStructs() {
        return this.jahiaCtnStructs;
    }

    public void setJahiaCtnStructs(Set<JahiaCtnStruct> jahiaCtnStructs) {
        this.jahiaCtnStructs = jahiaCtnStructs;
    }

    /**
     * @hibernate.property column="pagedefid_jahia_ctn_def_prop"
     * length="11"
     */
    public Integer getPageDefinitionId() {
        return this.pageDefinitionId;
    }

    public void setPageDefinitionId(Integer pageDefinitionId) {
        this.pageDefinitionId = pageDefinitionId;
    }

// ------------------------ CANONICAL METHODS ------------------------

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaCtnDefProperty castOther = (JahiaCtnDefProperty) obj;
            return new EqualsBuilder()
                .append(this.getIdJahiaCtnDefProperties(), castOther.getIdJahiaCtnDefProperties())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getIdJahiaCtnDefProperties())
                .toHashCode();
    }

    public String toString() {
        return new StringBuffer(getClass().getName())
                .append("idJahiaCtnDefProperties="+getIdJahiaCtnDefProperties())
                .toString();
    }
}

