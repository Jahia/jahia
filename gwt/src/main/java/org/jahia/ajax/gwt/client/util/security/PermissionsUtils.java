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
 **/
package org.jahia.ajax.gwt.client.util.security;

import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.GWTJahiaPermission;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Feb 5, 2010
 * Time: 11:16:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class PermissionsUtils {
    private static List<GWTJahiaPermission> grantedPermissions = new ArrayList<GWTJahiaPermission>();

    /**
     * load permission. Implemented thanks to an ajax call or read from a javascript object
     */
    public static void loadPermissions(List<GWTJahiaPermission> permissions) {
           grantedPermissions = permissions;       
    }


    /**
     * Check if a permission is granted to the current user
     *
     * @param permissionName permission name
     * @return
     */
    public static boolean isPermitted(String permissionName) {
        return isPermitted(permissionName, null);

    }

    /**
     * Check if a site based permission is granted to the current user
     *
     * @param permissionName permission name
     * @param siteKey site key 
     * @return
     */
    public static boolean isPermitted(String permissionName, String siteKey) {
        GWTJahiaPermission perm = new GWTJahiaPermission();
        String[] splittedName = permissionName.split("/");
        if (splittedName.length > 1) {
            perm.setGroup(splittedName[0]);
            perm.setName(splittedName[1]);            
        } else {
            perm.setName(permissionName);            
        }
        if (siteKey != null) {
            perm.setSite(siteKey);
        }
        return isPermitted(perm);

    }
    
    
    /**
     * Check if a permission is granted to the current user
     *
     * @param permission
     * @return
     */
    public static boolean isPermitted(GWTJahiaPermission permission) {
        return grantedPermissions.contains(permission);
    }


}
