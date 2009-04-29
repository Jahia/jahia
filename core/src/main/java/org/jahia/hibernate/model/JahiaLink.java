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
import java.util.Date;

/**
 * @hibernate.class table="jahia_link" lazy="false"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaLink implements Serializable {

    /**
     * identifier field
     */
    private Integer id;

    /**
     * nullable persistent field
     */
    private String leftOid;

    /**
     * nullable persistent field
     */
    private String rightOid;

    /**
     * nullable persistent field
     */
    private String type;

    /**
     * full constructor
     */
    public JahiaLink(Integer id, String leftOid, String rightOid, String type, Integer status, Date creationDate,
                     String creationUser, Date lastmodifDate, String lastmodifUser) {
        this.id = id;
        this.leftOid = leftOid;
        this.rightOid = rightOid;
        this.type = type;
    }

    /**
     * default constructor
     */
    public JahiaLink() {
    }

    /**
     * minimal constructor
     */
    public JahiaLink(Integer id) {
        this.id = id;
    }

    /**
     * @hibernate.id generator-class="assigned"
     * type="java.lang.Integer"
     * column="id"
     *
     */
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @hibernate.property column="left_oid"
     * length="100"
     */
    public String getLeftOid() {
        return this.leftOid;
    }

    public void setLeftOid(String leftOid) {
        this.leftOid = leftOid;
    }

    /**
     * @hibernate.property column="right_oid"
     * length="100"
     */
    public String getRightOid() {
        return this.rightOid;
    }

    public void setRightOid(String rightOid) {
        this.rightOid = rightOid;
    }

    /**
     * @hibernate.property column="type"
     * length="100"
     */
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaLink castOther = (JahiaLink) obj;
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
