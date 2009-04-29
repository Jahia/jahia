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

/**
 * @author Hibernate CodeGenerator
 */
public class JahiaLinkMetadataPK extends CachedPK implements Serializable {

    /**
     * identifier field
     */
    private Integer linkId;

    /**
     * identifier field
     */
    private String linkPosition;

    /**
     * identifier field
     */
    private String propertyName;

    /**
     * full constructor
     */
    public JahiaLinkMetadataPK(Integer linkId, String linkPosition, String propertyName) {
        this.linkId = linkId;
        this.linkPosition = linkPosition;
        this.propertyName = propertyName;
    }

    /**
     * default constructor
     */
    public JahiaLinkMetadataPK() {
    }

    /**
     * @hibernate.property column="link_id"
     * length="11"
     */
    public Integer getLinkId() {
        return this.linkId;
    }

    public void setLinkId(Integer linkId) {
        updated();
        this.linkId = linkId;
    }

    /**
     * @hibernate.property column="link_position"
     * length="20"
     */
    public String getLinkPosition() {
        return this.linkPosition;
    }

    public void setLinkPosition(String linkPosition) {
        updated();
        this.linkPosition = linkPosition;
    }

    /**
     * @hibernate.property column="property_name"
     * length="255"
     */
    public String getPropertyName() {
        return this.propertyName;
    }

    public void setPropertyName(String propertyName) {
        updated();
        this.propertyName = propertyName;
    }

    public String effectiveToString() {
        return new ToStringBuilder(this)
                .append("linkId", getLinkId())
                .append("linkPosition", getLinkPosition())
                .append("propertyName", getPropertyName())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaLinkMetadataPK castOther = (JahiaLinkMetadataPK) obj;
            return new EqualsBuilder()
                .append(this.getLinkId(), castOther.getLinkId())
                .append(this.getLinkPosition(), castOther.getLinkPosition())
                .append(this.getPropertyName(), castOther.getPropertyName())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getLinkId())
                .append(getLinkPosition())
                .append(getPropertyName())
                .toHashCode();
    }

}
