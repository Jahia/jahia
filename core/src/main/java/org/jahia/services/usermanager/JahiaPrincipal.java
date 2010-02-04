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

package org.jahia.services.usermanager;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;

/**
 * Represents a common notion of a principal in Jahia, including users and
 * groups.
 * 
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public interface JahiaPrincipal extends Principal, Serializable {

    /**
     * Returns {@code true} if this principal has the specified role, {@code
     * false} otherwise.
     * 
     * @param role the application-specific role identifier (usually a role id
     *            or role name).
     * @return {@code true} if this principal has the specified role, {@code
     *         false} otherwise
     * @since 6.5
     */
    boolean hasRole(String role);

    /**
     * Returns {@code true} if this principal has all the specified roles,
     * {@code false} otherwise.
     * 
     * @param role the collection of application-specific role identifiers
     *            (usually a role id or role name) to be checked
     * @return {@code true} if this principal has all the specified roles,
     *         {@code false} otherwise
     * @since 6.5
     */
    boolean hasRoles(Collection<String> roles);

    /**
     * Returns {@code true} if this principal is permitted to perform all of the
     * actions or access a resource summarized by the specified permission
     * collection.
     * 
     * @param permissions a collection of the permissions that is being checked
     * @return {@code true} if this principal is permitted to perform all of the
     *         actions or access a resource summarized by the specified
     *         permission collection
     * @since 6.5
     */
    boolean isPermitted(Collection<String> permissions);

    /**
     * Returns {@code true} if this principal is permitted to perform an action
     * or access a resource summarized by the specified permission string.
     * 
     * @param permission the String representation of a permission that is being
     *            checked
     * @return {@code true} if this principal is permitted to perform an action
     *         or access a resource summarized by the specified permission
     *         string
     * @since 6.5
     */
    boolean isPermitted(String permission);

}
