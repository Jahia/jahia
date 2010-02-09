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

package org.jahia.services.rbac.jcr;

import static org.jahia.services.rbac.jcr.RoleManager.JMIX_ROLE_BASED_ACCESS_CONTROLLED;
import static org.jahia.services.rbac.jcr.RoleManager.PROPERTY_PERMISSSIONS;
import static org.jahia.services.rbac.jcr.RoleManager.PROPERTY_ROLES;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.rbac.RoleBasedAccessControlService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Service implementation for the Role Based Access Control.
 * 
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public class RoleBasedAccessControlServiceImpl extends RoleBasedAccessControlService {

    private static Logger logger = Logger.getLogger(RoleBasedAccessControlServiceImpl.class);

    private JahiaGroupManagerService groupManager;

    private RoleManager roleManager;

    private boolean hasDirectPermission(String principal, String permission, JCRSessionWrapper session)
            throws InvalidQueryException, RepositoryException {
        boolean hasIt = false;
        QueryResult result = session.getWorkspace().getQueryManager().createQuery(
                "/jcr:root" + principal + "/jcr:deref(@" + PROPERTY_ROLES + ", '*')/jcr:deref(@"
                        + PROPERTY_PERMISSSIONS + ", "
                        + JCRContentUtils.stringToQueryLiteral(StringUtils.substringAfterLast(permission, "/")) + ")",
                Query.XPATH).execute();
        // the permission is specified by path
        for (NodeIterator iterator = result.getNodes(); iterator.hasNext();) {
            Node node = iterator.nextNode();
            if (node.getPath().equals(permission)) {
                hasIt = true;
                break;
            }
        }

        return hasIt;
    }

    private boolean hasDirectRole(JCRNodeWrapper principal, JCRNodeWrapper role, JCRSessionWrapper session)
            throws InvalidQueryException, RepositoryException {
        boolean hasIt = false;
        JCRPropertyWrapper rolesProperty = null;
        try {
            rolesProperty = principal.isNodeType(JMIX_ROLE_BASED_ACCESS_CONTROLLED) ? principal
                    .getProperty(PROPERTY_ROLES) : null;
        } catch (PathNotFoundException ex) {
            // no roles property found
        }
        Value[] roles = rolesProperty != null ? rolesProperty.getValues() : null;

        if (roles != null && roles.length > 0) {
            String targetIdentifier = role.getIdentifier();
            for (Value roleValue : roles) {
                if (targetIdentifier.equals(roleValue.getString())) {
                    hasIt = true;
                    break;
                }
            }
        }

        return hasIt;
    }

    private boolean hasInheritedPermission(JahiaPrincipal principal, JCRNodeWrapper principalNode, String permission,
            JCRSessionWrapper session) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean hasInheritedRole(JahiaPrincipal principal, JCRNodeWrapper principalNode, String role,
            JCRSessionWrapper session) {
        boolean hasIt = false;
        if (principal instanceof JahiaUser) {
            List<String> groups = groupManager.getUserMembership((JahiaUser) principal);
        } else if (principal instanceof JahiaGroup) {

        } else {

        }

        // TODO check for group or groups membership case

        return hasIt;
    }

    @Override
    protected boolean isPermissionGranted(final JahiaPrincipal principal, final String permission) {
        boolean hasPermisson = false;
        try {
            hasPermisson = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper principalNode = roleManager.getPrincipalNode(principal, session);
                    return principalNode != null ? hasDirectPermission(principalNode.getPath(), permission, session)
                            || hasInheritedPermission(principal, principalNode, permission, session) : false;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return hasPermisson;
    }

    @Override
    protected boolean isPrincipalInRole(final JahiaPrincipal principal, final String role) {
        boolean hasRole = false;
        try {
            hasRole = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper principalNode = roleManager.getPrincipalNode(principal, session);
                    JCRNodeWrapper roleNode = null;
                    try {
                        roleNode = session.getNode(role);
                    } catch (PathNotFoundException e) {
                        logger.debug("Corresponding role node for path " + role + " cannot be found.", e);
                    }
                    return principalNode != null && roleNode != null ? hasDirectRole(principalNode, roleNode, session)
                            || hasInheritedRole(principal, principalNode, role, session) : false;
                }

            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return hasRole;
    }

    /**
     * @param groupManager the groupManager to set
     */
    public void setGroupManager(JahiaGroupManagerService groupManager) {
        this.groupManager = groupManager;
    }

    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }
}