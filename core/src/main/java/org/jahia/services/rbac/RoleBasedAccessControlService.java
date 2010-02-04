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

import java.util.Collection;

import org.jahia.services.usermanager.JahiaPrincipal;

/**
 * Service implementation for the Role Based Access Control.
 * 
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public abstract class RoleBasedAccessControlService {

    /**
     * Returns {@code true} if this principal has the specified role, {@code
     * false} otherwise.
     * 
     * @param principal the principal to check for role
     * @param role the application-specific role identifier (usually a role id
     *            or role name).
     * @return {@code true} if this principal has the specified role, {@code
     *         false} otherwise
     */
    public abstract boolean hasRole(JahiaPrincipal principal, String role);

    /**
     * Returns {@code true} if this principal has all the specified roles,
     * {@code false} otherwise.
     * 
     * @param principal the principal to check for roles
     * @param role the collection of application-specific role identifiers
     *            (usually a role id or role name) to be checked
     * @return {@code true} if this principal has all the specified roles,
     *         {@code false} otherwise
     */
    public boolean hasRoles(JahiaPrincipal principal, Collection<String> roles) {
        boolean satisfies = true;
        for (String role : roles) {
            if (!hasRole(principal, role)) {
                satisfies = false;
                break;
            }
        }
        return satisfies;
    }

    /**
     * Returns {@code true} if this principal is permitted to perform all of the
     * actions or access a resource summarized by the specified permission
     * collection.
     * 
     * @param principal the principal to check for permissions
     * @param permissions a collection of the permissions that is being checked
     * @return {@code true} if this principal is permitted to perform all of the
     *         actions or access a resource summarized by the specified
     *         permission collection
     */
    public boolean isPermitted(JahiaPrincipal principal, Collection<String> permissions) {
        boolean satisfies = true;
        for (String permission : permissions) {
            if (!isPermitted(principal, permission)) {
                satisfies = false;
                break;
            }
        }
        return satisfies;
    }

    /**
     * Returns {@code true} if this principal is permitted to perform an action
     * or access a resource summarized by the specified permission string.
     * 
     * @param principal the principal to check for permission
     * @param permission the String representation of a permission that is being
     *            checked
     * @return {@code true} if this principal is permitted to perform an action
     *         or access a resource summarized by the specified permission
     *         string
     */
    public abstract boolean isPermitted(JahiaPrincipal principal, String permission);

}
