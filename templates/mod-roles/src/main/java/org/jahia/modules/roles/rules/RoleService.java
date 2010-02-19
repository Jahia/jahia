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

package org.jahia.modules.roles.rules;

import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.drools.spi.KnowledgeHelper;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.rules.NodeWrapper;
import org.jahia.services.rbac.PermissionIdentity;
import org.jahia.services.rbac.Role;
import org.jahia.services.rbac.RoleIdentity;
import org.jahia.services.rbac.jcr.PermissionImpl;
import org.jahia.services.rbac.jcr.RoleBasedAccessControlService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;

/**
 * Roles/permissions service class that is used in right-hand-side
 * (consequences) of rules.
 * 
 * @author Sergiy Shyrkov
 */
public class RoleService {

    private static Logger logger = Logger.getLogger(RoleService.class);

    private JahiaGroupManagerService groupService;

    private RoleBasedAccessControlService rbacService;

    private org.jahia.services.rbac.jcr.RoleService roleService;

    private JahiaUserManagerService userService;

    private Role getRole(String role) {
        return new RoleIdentity(role.contains("/") ? StringUtils.substringAfterLast(role, "/") : role, JCRContentUtils
                .getSiteKey(role));
    }

    /**
     * Assign a group of permissions to a specified role.
     * 
     * @param role the role to be modified
     * @param permissionGroup the name of the group of permission to be granted
     * @param drools the rule engine helper class
     * @throws RepositoryException in case of an error
     */
    public void grantPermissionGroupToRole(final String role, final String permissionGroup, KnowledgeHelper drools)
            throws RepositoryException {
        List<PermissionImpl> permissions = roleService.getPermissions(JCRContentUtils.getSiteKey(role));
        for (Iterator<PermissionImpl> iterator = permissions.iterator(); iterator.hasNext();) {
            PermissionImpl permission = iterator.next();
            if (!permission.getGroup().equals(permissionGroup)) {
                iterator.remove();
            }
        }
        if (!permissions.isEmpty()) {
            roleService.grantPermissions(getRole(role), permissions);
        }
    }

    /**
     * Assign permission to a specified role.
     * 
     * @param role the role to be modified
     * @param permission permission to be granted
     * @param drools the rule engine helper class
     * @throws RepositoryException in case of an error
     */
    public void grantPermissionToRole(final String role, final String permission, KnowledgeHelper drools)
            throws RepositoryException {
        roleService.grantPermission(getRole(role), new PermissionIdentity(permission, null, JCRContentUtils.getSiteKey(role)));
    }

    /**
     * Grant role to a specified group.
     * 
     * @param group to be modified
     * @param role the role to be granted
     * @param drools the rule engine helper class
     * @throws RepositoryException in case of an error
     */
    public void grantRoleToGroup(final String group, final String role, KnowledgeHelper drools)
            throws RepositoryException {
        JahiaGroup principal = groupService.lookupGroup(group);
        if (principal != null) {
            rbacService.grantRole(principal, getRole(role));
        } else {
            logger.warn("Unable to look up the specified group for name '" + group + "'. Skip granting role.");
        }
    }

    /**
     * Grant role to a specified user.
     * 
     * @param user to be modified
     * @param role the role to be granted
     * @param drools the rule engine helper class
     * @throws RepositoryException in case of an error
     */
    public void grantRoleToUser(final String user, final String role, KnowledgeHelper drools)
            throws RepositoryException {
        JahiaUser principal = userService.lookupUser(user);
        if (principal != null) {
            rbacService.grantRole(principal, getRole(role));
        } else {
            logger.warn("Unable to look up the specified user for name '" + user + "'. Skip granting role.");
        }
    }

    /**
     * @param groupService the groupService to set
     */
    public void setGroupService(JahiaGroupManagerService groupService) {
        this.groupService = groupService;
    }

    public void setRoleBasedAccessControlService(RoleBasedAccessControlService rbacService) {
        this.rbacService = rbacService;
    }

    /**
     * Injects an instance of the role manager service.
     * 
     * @param roleService an instance of the role manager service
     */
    public void setRoleService(org.jahia.services.rbac.jcr.RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * @param userService the userService to set
     */
    public void setUserService(JahiaUserManagerService userService) {
        this.userService = userService;
    }

    /**
     * Creates permissions for site languages.
     * 
     * @param node the site node
     * @param drools the rule engine helper class
     * @throws RepositoryException in case of an error
     */
    public void updateSiteLangPermissions(final NodeWrapper node, KnowledgeHelper drools) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper siteNode = session.getNode(node.getPath());
                final String siteKey = siteNode.getName();
                final Value[] languages = siteNode.getProperty("j:languages").getValues();
                for (Value language : languages) {
                    roleService.savePermission(new PermissionIdentity(language.getString(), "languages", siteKey));
                }
                return true;
            }
        });
    }

}