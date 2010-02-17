/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.rbac;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Base identity for roles and permissions.O
 * 
 * @author Sergiy Shyrkov
 */
class BaseIdentity implements Serializable {

    private String name;

    private String site;

    /**
     * Initializes an instance of this class.
     * 
     * @param name the name of this identity
     */
    public BaseIdentity(String name) {
        this(name, null);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param name the name of this identity
     * @param site the site key of this identity
     */
    public BaseIdentity(String name, String site) {
        super();
        this.name = name;
        this.site = site != null && site.length() == 0 ? null : site;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }

    /**
     * Returns the name of this identity.
     * 
     * @return the name of this identity
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the site key of this identity or <code>null</code> if it is a
     * server-level identity.
     * 
     * @return the site key of this identity or <code>null</code> if it is a
     *         server-level identity
     */
    public String getSite() {
        return site;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}