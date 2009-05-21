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
