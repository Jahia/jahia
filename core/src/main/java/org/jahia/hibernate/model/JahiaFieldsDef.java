/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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

    /**
     * persistent field
     */
    private Integer id;
    private Integer isMetadata = new Integer(0);
    private Map<String, String> properties;
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
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
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

