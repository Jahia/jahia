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
public class JahiaFieldsDefExtpropPK extends CachedPK implements Serializable {

    /**
     * identifier field
     */
    private Integer jahiaFieldsDef;

    /**
     * identifier field
     */
    private String propName;

    /**
     * default constructor
     */
    public JahiaFieldsDefExtpropPK() {
    }

    public JahiaFieldsDefExtpropPK(Integer jahiaFieldsDef, String propName) {
        this.jahiaFieldsDef = jahiaFieldsDef;
        this.propName = propName;
    }

    /**
     * @hibernate.property column="id_jahia_fields_def"
     */
    public Integer getJahiaFieldsDef() {
        return this.jahiaFieldsDef;
    }

    public void setJahiaFieldsDef(Integer jahiaFieldsDef) {
        updated();
        this.jahiaFieldsDef = jahiaFieldsDef;
    }

    /**
     * @hibernate.property column="prop_name"
     * length="200"
     */
    public String getPropName() {
        return this.propName;
    }

    public void setPropName(String propName) {
        updated();
        this.propName = propName;
    }

    public String effectiveToString() {
        return new StringBuffer(getClass().getName())
                .append("idJahiaFieldsDef="+getJahiaFieldsDef())
                .append("propName="+getPropName())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaFieldsDefExtpropPK castOther = (JahiaFieldsDefExtpropPK) obj;
            return new EqualsBuilder()
                .append(this.getJahiaFieldsDef(), castOther.getJahiaFieldsDef())
                .append(this.getPropName(), castOther.getPropName())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getJahiaFieldsDef())
                .append(getPropName())
                .toHashCode();
    }

}
