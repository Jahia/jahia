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
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @hibernate.class table="jahia_ctn_def"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaCtnDef implements Serializable {
// ------------------------------ FIELDS ------------------------------

    /**
     * persistent field
     */
    private Integer id;

    private Map properties;

    /**
     * nullable persistent field
     */
    private Integer jahiaSite;

    /**
     * persistent field
     */
    private Set subDefinitions;

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
        properties = new ConcurrentHashMap(53);
        subDefinitions = new HashSet(53);
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
    public Map getProperties() {
        return properties;
    }

    public void setProperties(Map properties) {
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
    public Set getSubDefinitions() {
        return this.subDefinitions;
    }

    public void setSubDefinitions(Set subDefinitions) {
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

