/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.hibernate.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Map;

/**
 * @hibernate.class table="jahia_fields_data"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaFieldsData implements Serializable, Cloneable {

    private static final long serialVersionUID = -7684027169916468059L;

// ------------------------------ FIELDS ------------------------------

    /**
     * nullable persistent field
     */
    private Integer connectType;

    /**
     * nullable persistent field
     */
    private Integer containerId;

    /**
     * persistent field
     */
    private Integer pageId;

    /**
     * nullable persistent field
     */
    private Integer type;

    /**
     * nullable persistent field
     */
    private JahiaFieldsDef fieldDefinition;

    private Integer metadataOwnerId;

    private String metadataOwnerType;

    /**
     * nullable persistent field
     */
    private Integer siteId;

    private transient Map<Object, Object> properties;

    /**
     * persistent field
     */
    private Integer jahiaAclId;

    /**
     * identifier field
     */
    private org.jahia.hibernate.model.JahiaFieldsDataPK comp_id;

    /**
     * nullable persistent field
     */
    private String value;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * default constructor
     */
    public JahiaFieldsData() {
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * @hibernate.id generator-class="assigned"
     */
    public org.jahia.hibernate.model.JahiaFieldsDataPK getComp_id() {
        return this.comp_id;
    }

    public void setComp_id(org.jahia.hibernate.model.JahiaFieldsDataPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.property column="connecttype_jahia_fields_data"
     * length="11"
     */
    public Integer getConnectType() {
        return this.connectType;
    }

    public void setConnectType(Integer connectType) {
        this.connectType = connectType;
    }

    /**
     * @hibernate.property column="ctnid_jahia_fields_data"
     * length="11"
     */
    public Integer getContainerId() {
        return this.containerId;
    }

    public void setContainerId(Integer containerId) {
        this.containerId = containerId;
    }

    /**
     * @hibernate.many-to-one not-null="false"
     * @hibernate.column name="fielddefid_jahia_fields_data"
     */
    public JahiaFieldsDef getFieldDefinition() {
        return this.fieldDefinition;
    }

    public void setFieldDefinition(JahiaFieldsDef fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
    }

    /**
     * @hibernate.property column="id_jahia_obj"
     */
    public Integer getMetadataOwnerId() {
        return metadataOwnerId;
    }

    public void setMetadataOwnerId(Integer metadataOwner) {
        this.metadataOwnerId = metadataOwner;
    }

    /**
     * @hibernate.property column="type_jahia_obj" length="22"
     */
    public String getMetadataOwnerType() {
        return metadataOwnerType;
    }

    public void setMetadataOwnerType(String metadataOwner) {
        this.metadataOwnerType = metadataOwner;
    }

    public Integer getJahiaAclId() {
        return this.jahiaAclId;
    }

    public void setJahiaAclId(Integer jahiaAclId) {
        this.jahiaAclId = jahiaAclId;
    }

    /**
     * @hibernate.property column="pageid_jahia_fields_data"
     */
    public Integer getPageId() {
        return this.pageId;
    }

    public void setPageId(Integer pageId) {
        this.pageId = pageId;
    }

    public Map<Object, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<Object, Object> properties) {
        this.properties = properties;
    }

    /**
     * @hibernate.property column="jahiaid_jahia_fields_data"
     */
    public Integer getSiteId() {
        return this.siteId;
    }

    public void setSiteId(Integer site) {
        this.siteId = site;
    }

    /**
     * @hibernate.property column="type_jahia_fields_data"
     * length="11"
     */
    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * @hibernate.property column="value_jahia_fields_data"
     * length="250"
     */
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

// ------------------------ CANONICAL METHODS ------------------------

    /**
     * Creates and returns a copy of this object.  The precise meaning
     * of "copy" may depend on the class of the object. The general
     * intent is that, for any object <tt>x</tt>, the expression:
     * <blockquote>
     * <pre>
     * x.clone() != x</pre></blockquote>
     * will be true, and that the expression:
     * <blockquote>
     * <pre>
     * x.clone().getClass() == x.getClass()</pre></blockquote>
     * will be <tt>true</tt>, but these are not absolute requirements.
     * While it is typically the case that:
     * <blockquote>
     * <pre>
     * x.clone().equals(x)</pre></blockquote>
     * will be <tt>true</tt>, this is not an absolute requirement.
     * <p/>
     * By convention, the returned object should be obtained by calling
     * <tt>super.clone</tt>.  If a class and all of its superclasses (except
     * <tt>Object</tt>) obey this convention, it will be the case that
     * <tt>x.clone().getClass() == x.getClass()</tt>.
     * <p/>
     * By convention, the object returned by this method should be independent
     * of this object (which is being cloned).  To achieve this independence,
     * it may be necessary to modify one or more fields of the object returned
     * by <tt>super.clone</tt> before returning it.  Typically, this means
     * copying any mutable objects that comprise the internal "deep structure"
     * of the object being cloned and replacing the references to these
     * objects with references to the copies.  If a class contains only
     * primitive fields or references to immutable objects, then it is usually
     * the case that no fields in the object returned by <tt>super.clone</tt>
     * need to be modified.
     * <p/>
     * The method <tt>clone</tt> for class <tt>Object</tt> performs a
     * specific cloning operation. First, if the class of this object does
     * not implement the interface <tt>Cloneable</tt>, then a
     * <tt>CloneNotSupportedException</tt> is thrown. Note that all arrays
     * are considered to implement the interface <tt>Cloneable</tt>.
     * Otherwise, this method creates a new instance of the class of this
     * object and initializes all its fields with exactly the contents of
     * the corresponding fields of this object, as if by assignment; the
     * contents of the fields are not themselves cloned. Thus, this method
     * performs a "shallow copy" of this object, not a "deep copy" operation.
     * <p/>
     * The class <tt>Object</tt> does not itself implement the interface
     * <tt>Cloneable</tt>, so calling the <tt>clone</tt> method on an object
     * whose class is <tt>Object</tt> will result in throwing an
     * exception at run time.
     *
     * @return a clone of this instance.
     *
     * @throws CloneNotSupportedException if the object's class does not
     *                                    support the <code>Cloneable</code> interface. Subclasses
     *                                    that override the <code>clone</code> method can also
     *                                    throw this exception to indicate that an instance cannot
     *                                    be cloned.
     * @see Cloneable
     */
    public Object clone() throws CloneNotSupportedException {
        JahiaFieldsData data = new JahiaFieldsData();
        data.setComp_id((JahiaFieldsDataPK) this.getComp_id().clone());
        data.setConnectType(this.getConnectType());
        data.setContainerId(this.getContainerId());
        data.setFieldDefinition(this.getFieldDefinition());
        data.setMetadataOwnerId(this.getMetadataOwnerId());
        data.setMetadataOwnerType(this.getMetadataOwnerType());
//        data.setJahiaAclId(this.getJahiaAclId());
        data.setPageId(this.getPageId());
        data.setSiteId(this.getSiteId());
        data.setType(this.getType());
        data.setValue(this.getValue());
        return data;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaFieldsData castOther = (JahiaFieldsData) obj;
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

    public String toString() {
        return new StringBuffer(getClass().getName())
                .append("comp_id="+getComp_id())
                .toString();
    }
}

