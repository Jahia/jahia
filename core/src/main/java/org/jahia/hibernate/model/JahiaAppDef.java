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
import java.util.Map;

/**
 * @hibernate.class table="jahia_app_def"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaAppDef implements Serializable {

    /**
     * identifier field
     */
    private Integer id;

    /**
     * nullable persistent field
     */
    private String name;

    /**
     * nullable persistent field
     */
    private String context;

    /**
     * nullable persistent field
     */
    private Integer visible;

    /**
     * nullable persistent field
     */
    private Integer shared;

    /**
     * nullable persistent field
     */
    private org.jahia.hibernate.model.JahiaAcl jahiaAcl;

    /**
     * nullable persistent field
     */
    private String filename;

    /**
     * nullable persistent field
     */
    private String description;

    /**
     * nullable persistent field
     */
    private String type;

    /**
     * persistent field
     */
    private Map properties;

    /**
     * full constructor
     */
    public JahiaAppDef(Integer idJahiaAppDef, String nameJahiaAppDef,
                       String contextJahiaAppDef, Integer visibleJahiaAppDef, Integer sharedJahiaAppDef,
                       JahiaAcl rightsJahiaAppDef, String filenameJahiaAppDef, String descJahiaAppDef,
                       String typeJahiaAppDef, Map jahiaAppdefProps) {
        this.id = idJahiaAppDef;
        this.name = nameJahiaAppDef;
        this.context = contextJahiaAppDef;
        this.visible = visibleJahiaAppDef;
        this.shared = sharedJahiaAppDef;
        this.jahiaAcl = rightsJahiaAppDef;
        this.filename = filenameJahiaAppDef;
        this.description = descJahiaAppDef;
        this.type = typeJahiaAppDef;
        this.properties = jahiaAppdefProps;
    }

    /**
     * default constructor
     */
    public JahiaAppDef() {
    }

    /**
     * minimal constructor
     */
    public JahiaAppDef(Integer idJahiaAppDef, Map jahiaAppdefProps) {
        this.id = idJahiaAppDef;
        this.properties = jahiaAppdefProps;
    }

    /**
     * @hibernate.id generator-class="assigned"
     * type="java.lang.Integer"
     * column="id_jahia_app_def"
     *
     */
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @hibernate.property column="name_jahia_app_def"
     * length="250"
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @hibernate.property column="context_jahia_app_def"
     * length="250"
     */
    public String getContext() {
        return this.context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    /**
     * @hibernate.property column="visible_jahia_app_def"
     * length="11"
     */
    public Integer getVisible() {
        return this.visible;
    }

    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    /**
     * @hibernate.property column="shared_jahia_app_def"
     * length="11"
     */
    public Integer getShared() {
        return this.shared;
    }

    public void setShared(Integer shared) {
        this.shared = shared;
    }

    /**
     * @hibernate.many-to-one not-null="true"
     * @hibernate.column name="rights_jahia_app_def"
     */
    public org.jahia.hibernate.model.JahiaAcl getJahiaAcl() {
        return this.jahiaAcl;
    }

    public void setJahiaAcl(org.jahia.hibernate.model.JahiaAcl jahiaAcl) {
        this.jahiaAcl = jahiaAcl;
    }

    /**
     * @hibernate.property column="filename_jahia_app_def"
     * length="250"
     */
    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @hibernate.property column="desc_jahia_app_def"
     * length="250"
     */
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @hibernate.property column="type_jahia_app_def"
     * length="30"
     */
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @hibernate.map lazy="true" inverse="true" cascade="delete" table="jahia_appdef_prop"
     * @hibernate.collection-key column="appdefid_appdef_prop" type="int"
     * @hibernate.collection-index column="propname_appdef_prop" type="string"
     * @hibernate.collection-element column="propvalue_appdef_prop" type="string"
     * @return Map
     */
    public Map getProperties() {
        return properties;
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaAppDef castOther = (JahiaAppDef) obj;
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
