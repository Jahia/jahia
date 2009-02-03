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
