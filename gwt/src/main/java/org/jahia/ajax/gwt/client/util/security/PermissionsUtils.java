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

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTBitSet;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Permission utilities used on the GWT client to check permissions for the current user.
 * 
 * A list of permissions is cached on the client. As this list may change at runtime for instance if the client
 * connects to a different cluster node on fail-over, or if new modules with new permissions are getting deployed,
 * we are identifying different versions of the permission list with a hashcode and storing them in a map.
 * 
 * The single bits in a GWTBitSet refer to the ordered permission list items. A GWTBitSet created on the server
 * also holds the hashCode of the list it refers to. If it is detected that the client does not have that right 
 * version of the list loaded, we are trying to load it from the server. If that fails, the user has to reload the page.   
 * 
 * User: ktlili
 * Date: Feb 5, 2010
 * Time: 11:16:25 AM
 * 
 */
public class PermissionsUtils {
    private static Map<Integer, List<String>> registeredPermissionCopies = new HashMap<Integer, List<String>>();
    private static int lastPermissionsHashCode;
    private static Set<Integer> notifiedMissingHashes = new HashSet<Integer>();    

    /**
     * load permission. Implemented thanks to an ajax call or read from a javascript object
     */
    public static void loadPermissions(List<String> permissions) {
        lastPermissionsHashCode = permissions.hashCode();
        registeredPermissionCopies.put(lastPermissionsHashCode, permissions);
        notifiedMissingHashes.remove(lastPermissionsHashCode);
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
        
        List<String> registeredPermissions = getRegisteredPermissions(permissions.getReferenceHashCode());
        // Here it is checked whether the registered permissions list with the required hashcode is present.
        if (registeredPermissions == null) {
            // If the registered permissiona are not present, trigger a reload from the server and open an alert message
            // While displaying the message, the matching list may have been loaded and we can continue with correct checks.
            // Otherwise deny permission and user has to reload the page.
            triggerPermissionsReload();
            
            if (notifiedMissingHashes.add(permissions.getReferenceHashCode())) {
               Window.alert(Messages.get("permission.uncheckable", "There was a problem while checking permissions. If you encounter a wrongly denied permission, please try reloading the page."));
            }
            registeredPermissions = getRegisteredPermissions(permissions.getReferenceHashCode());
            if (registeredPermissions == null) {
                return false;
            }
        }
        int i = registeredPermissions.indexOf(permissionName);

        if (i == -1) {
            i = registeredPermissions.indexOf(permissionName + "_" + JahiaGWTParameters.getWorkspace());
        }

        if (i == -1) {
            Window.alert(Messages.get("message.unknownPermission", "Unknown permission") + " " + permissionName);
            return false;
        }
        return permissions.get(i);
    }

    private static List<String> getRegisteredPermissions(int bitSetPermissionsHashCode) {
        int permissionsHashCode = bitSetPermissionsHashCode == 0 ? lastPermissionsHashCode
                : bitSetPermissionsHashCode;
        return registeredPermissionCopies.get(permissionsHashCode);
    }

    /**
     * Trigger reloading of the current available registered permissions list and on success store it scoped to the browser page.
     */
    public static void triggerPermissionsReload() {
        JahiaContentManagementService.App.getInstance().getAvailablePermissions(new BaseAsyncCallback<List<String>>() {
            public void onSuccess(List<String> permissions) {
                loadPermissions(permissions);
            }
        });
    }
}
