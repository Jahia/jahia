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
/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import java.io.Serializable;

/**
 * @author Hibernate CodeGenerator
 */
public class JahiaCtndefPropPK extends CachedPK implements Serializable {

    /**
     * identifier field
     */
    private Integer idJahiaCtnDef;

    /**
     * identifier field
     */
    private String nameJahiaCtndefProp;

    /**
     * full constructor
     */
    public JahiaCtndefPropPK(Integer idJahiaCtnDef, String nameJahiaCtndefProp) {
        this.idJahiaCtnDef = idJahiaCtnDef;
        this.nameJahiaCtndefProp = nameJahiaCtndefProp;
    }

    /**
     * default constructor
     */
    public JahiaCtndefPropPK() {
    }

    /**
     * @hibernate.property column="id_jahia_ctn_def"
     * length="11"
     */
    public Integer getIdJahiaCtnDef() {
        return this.idJahiaCtnDef;
    }

    public void setIdJahiaCtnDef(Integer idJahiaCtnDef) {
        updated();
        this.idJahiaCtnDef = idJahiaCtnDef;
    }

    /**
     * @hibernate.property column="name_jahia_ctndef_prop"
     * length="255"
     */
    public String getNameJahiaCtndefProp() {
        return this.nameJahiaCtndefProp;
    }

    public void setNameJahiaCtndefProp(String nameJahiaCtndefProp) {
        updated();
        this.nameJahiaCtndefProp = nameJahiaCtndefProp;
    }

    public String effectiveToString() {
        return new StringBuffer(getClass().getName())
                .append("idJahiaCtnDef="+getIdJahiaCtnDef())
                .append("nameJahiaCtndefProp="+getNameJahiaCtndefProp())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaCtndefPropPK castOther = (JahiaCtndefPropPK) obj;
            return new EqualsBuilder()
                .append(this.getIdJahiaCtnDef(), castOther.getIdJahiaCtnDef())
                .append(this.getNameJahiaCtndefProp(), castOther.getNameJahiaCtndefProp())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getIdJahiaCtnDef())
                .append(getNameJahiaCtndefProp())
                .toHashCode();
    }

}
