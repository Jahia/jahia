/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.modules.roles.rules;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaPrivilegeRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.rbac.Role;
import org.jahia.services.rbac.RoleIdentity;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;

/**
 * Roles/permissions service class that is used in right-hand-side
 * (consequences) of rules.
 * 
 * @author Sergiy Shyrkov
 */
public class RoleService {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(RoleService.class);

    private org.jahia.services.rbac.jcr.RoleService roleService;


    private Role getRole(String role) {
        return new RoleIdentity(role.contains("/") ? StringUtils.substringAfterLast(role, "/") : role);
    }

//    /**
//     * Assign provided permissions to a specified role.
//     *
//     * @param role the role to be modified
//     * @param permissionsToGrant the list of permissions to be granted
//     * @param drools the rule engine helper class
//     * @throws RepositoryException in case of an error
//     */
//    public void grantPermissionsToRole(final String role, final List<String> permissionsToGrant, KnowledgeHelper drools)
//            throws RepositoryException {
//        List<Permission> allSitePermissions = roleService.getPermissions();
//        List<Permission> permissions = new LinkedList<Permission>();
//
//        for (String perm : permissionsToGrant) {
//            if (perm.endsWith("/*")) {
//                // granting group
//                String permGroup = StringUtils.substringBeforeLast(perm, "/*");
//                for (Permission sitePermission : allSitePermissions) {
//                    if (sitePermission.getName().equals(permGroup)) {
//                        permissions.add(sitePermission);
//                    }
//                }
//            } else {
//                permissions.add(new PermissionIdentity(perm));
//            }
//        }
//        if (!permissions.isEmpty()) {
//            roleService.grantPermissions(getRole(role), permissions);
//        }
//    }

//    /**
//     * Assign permission to a specified role.
//     *
//     * @param role the role to be modified
//     * @param permission permission to be granted
//     * @param drools the rule engine helper class
//     * @throws RepositoryException in case of an error
//     */
//    public void grantPermissionToRole(final String role, final String permission, KnowledgeHelper drools)
//            throws RepositoryException {
//        roleService.grantPermission(getRole(role), new PermissionIdentity(permission));
//    }
//

    /**
     * Injects an instance of the role manager service.
     * 
     * @param roleService an instance of the role manager service
     */
    public void setRoleService(org.jahia.services.rbac.jcr.RoleService roleService) {
        this.roleService = roleService;
    }

//    /**
//     * Creates permissions for site languages.
//     *
//     * @param node the site node
//     * @param drools the rule engine helper class
//     * @throws RepositoryException in case of an error
//     */
//    public void updateSiteLangPermissions(final AddedNodeFact node, KnowledgeHelper drools) throws RepositoryException {
//        JCRNodeWrapper siteNode = node.getNode();
//        final String siteKey = siteNode.getName();
//        final Value[] languages = siteNode.getProperty("j:languages").getValues();
//        for (Value language : languages) {
//            roleService.savePermission(new PermissionIdentity(language.getString()));
//        }
//    }
//
//    public void createTranslatorRole(final AddedNodeFact node, String name, String base, KnowledgeHelper drools) throws RepositoryException {
//        JCRNodeWrapper siteNode = node.getNode();
//        final String siteKey = siteNode.getName();
//        RoleImpl translatorRole = roleService.getRole(new RoleIdentity(base));
//        if (translatorRole != null) {
//            Set<Permission> perms = translatorRole.getPermissions();
//            final Value[] languages = siteNode.getProperty("j:languages").getValues();
//            for (Value language : languages) {
//                final RoleImpl role = new RoleImpl(name.replace("*", language.getString()));
//                if (roleService.getRole(role) != null) {
//                    continue;
//                }
//                final Set<Permission> permissions = new HashSet<Permission>(perms);
//                for (Permission perm : perms) {
//                    if (perm.getPath().contains("languages")) {
//                        permissions.remove(perm);
//                    }
//                }
//                permissions.add(roleService.getPermission(new PermissionImpl(language.getString())));
//                role.setPermissions(permissions);
//                roleService.saveRole(role);
//            }
//        }
//    }
//
    public void refreshPermissions() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JahiaPrivilegeRegistry.init(session);
                return null;
            }
        });

    }

}