/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.util.security;

import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTBitSet;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * User: ktlili
 * Date: Feb 5, 2010
 * Time: 11:16:25 AM
 * 
 */
public class PermissionsUtils {
    private static List<String> grantedPermissions = new ArrayList<String>();

    /**
     * load permission. Implemented thanks to an ajax call or read from a javascript object
     */
    public static void loadPermissions(List<String> permissions) {
           grantedPermissions = permissions;       
    }


    /**
     * Check if a permission is granted to the current user
     *
     * @param permissionName permission name
     * @return
     */
    public static boolean isPermitted(String permissionName) {
        // todo !
        return true;
    }

    /**
     * Check if a site based permission is granted to the current user
     *
     * @param permissionName permission name
     * @return
     */
    public static boolean isPermitted(String permissionName, GWTJahiaNode node) {
        if (node == null) {
            // todo !
            return true;
        }

        return isPermitted(permissionName, node.getPermissions());
    }

    public static boolean isPermitted(String permissionName, GWTBitSet permissions) {
        if (permissions == null) {
            return false;
        }
        if("root".equals(JahiaGWTParameters.getCurrentUser())) {
            return true;
        }
        int i = grantedPermissions.indexOf(permissionName);

        if (i == -1) {
            i = grantedPermissions.indexOf(permissionName + "_" + JahiaGWTParameters.getWorkspace());
        }

        if (i == -1) {
            Window.alert(Messages.get("message.unknownPermission", "Unknown permission") + " " + permissionName);
            return false;
        }
        return permissions.get(i);
    }


}
