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
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @author Hibernate CodeGenerator
 */
public class JahiaUserPropPK extends CachedPK implements Serializable {

    /**
     * identifier field
     */
    private Integer id;

    /**
     * identifier field
     */
    private String name;

    /**
     * identifier field
     */
    private String provider;

    /**
     * identifier field
     */
    private String userkey;

    /**
     * full constructor
     */
    public JahiaUserPropPK(Integer idJahiaUsers, String nameJahiaUserProp, String providerJahiaUserProp,
                           String userkeyJahiaUserProp) {
        this.id = idJahiaUsers;
        this.name = nameJahiaUserProp;
        this.provider = providerJahiaUserProp;
        this.userkey = userkeyJahiaUserProp;
    }

    /**
     * default constructor
     */
    public JahiaUserPropPK() {
    }

    /**
     * @hibernate.property column="id_jahia_users"
     * length="11"
     */
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        updated();
        this.id = id;
    }

    /**
     * @hibernate.property column="name_jahia_user_prop"
     * length="150"
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        updated();
        this.name = name;
    }

    /**
     * @hibernate.property column="provider_jahia_user_prop"
     * length="50"
     */
    public String getProvider() {
        return this.provider;
    }

    public void setProvider(String provider) {
        updated();
        this.provider = provider;
    }

    /**
     * @hibernate.property column="userkey_jahia_user_prop"
     * length="50"
     */
    public String getUserkey() {
        return this.userkey;
    }

    public void setUserkey(String userkey) {
        updated();
        this.userkey = userkey;
    }

    public String effectiveToString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .append("name", getName())
                .append("provider", getProvider())
                .append("userkey", getUserkey())
                .toString();
    }

    public boolean equals(Object obj) {
                if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaUserPropPK castOther = (JahiaUserPropPK) obj;
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .append(this.getName(), castOther.getName())
                .append(this.getProvider(), castOther.getProvider())
                .append(this.getUserkey(), castOther.getUserkey())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId())
                .append(getName())
                .append(getProvider())
                .append(getUserkey())
                .toHashCode();
    }

}
