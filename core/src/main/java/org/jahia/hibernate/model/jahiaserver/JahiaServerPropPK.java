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
 package org.jahia.hibernate.model.jahiaserver;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jahia.hibernate.model.CachedPK;

import java.io.Serializable;

/**
 * @author Hibernate CodeGenerator
 */
public class JahiaServerPropPK extends CachedPK implements Serializable {

    /**
     *
     */
    private String serverId;

    /**
     *
     */
    private String propName;

    /**
     * full constructor
     */
    public JahiaServerPropPK(String id, String propName) {
        this.serverId = id;
        this.propName = propName;
    }

    /**
     * default constructor
     */
    public JahiaServerPropPK() {
    }

    /**
     * @hibernate.property column="id_serverprops"
     * length="50"
     */
    public String getServerId() {
        return this.serverId;
    }

    public void setServerId(String serverId) {
        updated();
        this.serverId = serverId;
    }

    /**
     * @hibernate.property column="propname_serverprops"
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
        return new ToStringBuilder(this)
                .append("serverId", getServerId())
                .append("propName", getPropName())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaServerPropPK castOther = (JahiaServerPropPK) obj;
            return new EqualsBuilder()
                .append(this.getServerId(), castOther.getServerId())
                .append(this.getPropName(), castOther.getPropName())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getServerId())
                .append(getPropName())
                .toHashCode();
    }

}
