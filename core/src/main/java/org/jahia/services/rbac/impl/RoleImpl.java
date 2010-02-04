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

package org.jahia.services.rbac.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jahia.services.rbac.Role;

/**
 * Default implementation of the role in Jahia that uses JCR persistence.
 * 
 * @author Sergiy Shyrkov
 */
public class RoleImpl extends JCRItem implements Role {

    private String description;

    private String name;

    private Set<PermissionImpl> permissions = new LinkedHashSet<PermissionImpl>();

    /**
     * Initializes an instance of this class.
     * 
     * @param name the name of the the role
     */
    public RoleImpl(String name) {
        super();
        this.name = name;
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param name the name of the the role
     * @param description a short description for this role
     */
    public RoleImpl(String name, String description) {
        this(name);
        this.description = description;
    }

    @Override
    public boolean equals(Object another) {
        return another != null && another instanceof RoleImpl && ((RoleImpl) another).getName().equals(getName());
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Set<PermissionImpl> getPermissions() {
        return permissions;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getName()).toHashCode();
    }

    /**
     * Sets a short description for this role.
     * 
     * @param description a short description for this role
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the name of this role.
     * 
     * @param name the name of this role
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets a set of permissions for this role.
     * 
     * @param permissions a set of permissions for this role
     */
    public void setPermissions(Set<PermissionImpl> permissions) {
        this.permissions.clear();
        if (permissions != null) {
            this.permissions.addAll(permissions);
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}