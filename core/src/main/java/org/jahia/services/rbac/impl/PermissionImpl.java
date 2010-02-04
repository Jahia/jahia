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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jahia.services.rbac.Permission;

/**
 * Default implementation of a permission in Jahia.
 * 
 * @author Sergiy Shyrkov
 */
public class PermissionImpl extends JCRItem implements Permission {

    private String description;

    private String group;

    private String name;

    /**
     * Initializes an instance of this class.
     * 
     * @param name the name of the the permission
     */
    public PermissionImpl(String name) {
        super();
        this.name = name;
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param name the name of the the permission
     * @param group the name of the permission group
     */
    public PermissionImpl(String name, String group) {
        this(name);
        this.group = group;
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param name the name of the the permission
     * @param group the name of the permission group
     * @param description a short description for this permission
     */
    public PermissionImpl(String name, String group, String description) {
        this(name, group);
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof PermissionImpl)) {
            return false;
        }
        PermissionImpl another = (PermissionImpl) obj;
        return new EqualsBuilder().append(getName(), another.getName()).append(getGroup(), another.getGroup())
                .isEquals();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.services.rbac.impl.Permission#getDescription()
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the name of the permission group
     * 
     * @return the the name of the permission group
     */
    public String getGroup() {
        return group;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.services.rbac.impl.Permission#getName()
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getName()).append(getGroup()).toHashCode();
    }

    /**
     * Sets a short description for this permission.
     * 
     * @param description a short description for this permission
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the name of the permission group
     * 
     * @param group the name of the permission group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Sets the name of this permission.
     * 
     * @param name the name of this permission
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}