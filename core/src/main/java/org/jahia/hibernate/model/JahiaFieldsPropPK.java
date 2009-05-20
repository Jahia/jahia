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
 * @author Hibernate CodeGenerator
 */
public class JahiaFieldsPropPK extends CachedPK implements Serializable {

    /**
     * identifier field
     */
    private Integer fieldId;

    /**
     * identifier field
     */
    private String name;

    /**
     * full constructor
     */
    public JahiaFieldsPropPK(Integer fieldidJahiaFieldsProp, String propertynameJahiaFieldsProp) {
        this.fieldId = fieldidJahiaFieldsProp;
        this.name = propertynameJahiaFieldsProp;
    }

    /**
     * default constructor
     */
    public JahiaFieldsPropPK() {
    }

    /**
     * @hibernate.property column="fieldid_jahia_fields_prop"
     * length="11"
     */

    public Integer getFieldId() {
        return this.fieldId;
    }

    public void setFieldId(Integer fieldId) {
        updated();
        this.fieldId = fieldId;
    }

    /**
     * @hibernate.property column="propertyname_jahia_fields_prop"
     * length="250"
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        updated();
        this.name = name;
    }

    public String effectiveToString() {
        return new StringBuffer(getClass().getName())
                .append("fieldId="+getFieldId())
                .append("name="+getName())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaFieldsPropPK castOther = (JahiaFieldsPropPK) obj;
            return new EqualsBuilder()
                .append(this.getFieldId(), castOther.getFieldId())
                .append(this.getName(), castOther.getName())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getFieldId())
                .append(getName())
                .toHashCode();
    }

}
