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
