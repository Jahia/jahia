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
public class JahiaCtnStructPK extends CachedPK implements Serializable {

    /**
     * identifier field
     */
    private JahiaCtnDefProperty ctnsubdefid;

    /**
     * identifier field
     */
    private Integer objType;

    /**
     * identifier field
     */
    private Integer objDefId;

    /**
     * full constructor
     */
    public JahiaCtnStructPK(JahiaCtnDefProperty ctnsubdefidJahiaCtnStruct, Integer objtypeJahiaCtnStruct,
                            Integer objdefidJahiaCtnStruct) {
        this.ctnsubdefid = ctnsubdefidJahiaCtnStruct;
        this.objType = objtypeJahiaCtnStruct;
        this.objDefId = objdefidJahiaCtnStruct;
    }

    /**
     * default constructor
     */
    public JahiaCtnStructPK() {
    }

    /**
     * @hibernate.many-to-one not-null="true" update="true"
     * insert="true" cascade="all"
     * class="org.jahia.hibernate.model.JahiaCtnDefProperty"
     * column="ctnsubdefid_jahia_ctn_struct"
     */
    public JahiaCtnDefProperty getCtnsubdefid() {
        return this.ctnsubdefid;
    }

    public void setCtnsubdefid(JahiaCtnDefProperty ctnsubdefid) {
        updated();
        this.ctnsubdefid = ctnsubdefid;
    }

    /**
     * @hibernate.property column="objtype_jahia_ctn_struct"
     * length="11"
     */
    public Integer getObjType() {
        return this.objType;
    }

    public void setObjType(Integer objType) {
        updated();
        this.objType = objType;
    }

    /**
     * @hibernate.property column="objdefid_jahia_ctn_struct"
     * length="11"
     */
    public Integer getObjDefId() {
        return this.objDefId;
    }

    public void setObjDefId(Integer objDefId) {
        updated();
        this.objDefId = objDefId;
    }

    public String effectiveToString() {
        return new StringBuffer(getClass().getName())
                .append("ctnsubdefid="+getCtnsubdefid())
                .append("objType="+getObjType())
                .append("objDefId="+getObjDefId())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaCtnStructPK castOther = (JahiaCtnStructPK) obj;
            return new EqualsBuilder()
                .append(this.getCtnsubdefid(), castOther.getCtnsubdefid())
                .append(this.getObjType(), castOther.getObjType())
                .append(this.getObjDefId(), castOther.getObjDefId())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getCtnsubdefid())
                .append(getObjType())
                .append(getObjDefId())
                .toHashCode();
    }

}
