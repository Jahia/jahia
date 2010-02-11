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

import java.util.Collections;
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
import org.jahia.services.usermanager.jcr.JCRGroupManagerProvider;

/**
 * Service implementation for the Role Based Access Control.
 * 
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public class RoleBasedAccessControlServiceImpl extends RoleBasedAccessControlService {

    private static Logger logger = Logger.getLogger(RoleBasedAccessControlServiceImpl.class);

    private JahiaGroupManagerService groupManager;

    private JCRGroupManagerProvider jcrGroupManagerProvider;

    private RoleManager roleManager;

    private List<String> getMembership(JahiaPrincipal principal) {
        List<String> groups = Collections.emptyList();
        if (principal instanceof JahiaUser) {
            groups = groupManager.getUserMembership((JahiaUser) principal);
        } else if (principal instanceof JahiaGroup) {
            groups = jcrGroupManagerProvider.getMembership(principal);
        } else {
            logger.warn("Unknown principal type " + principal.getClass().getName() + " for principal "
                    + principal.getName());
        }

        return groups;
    }

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
        boolean hasIt = false;
        for (String groupKey : getMembership(principal)) {
            JahiaGroup group = groupManager.lookupGroup(groupKey);
            if (group != null) {
                hasIt = group.isPermitted(permission);
                if (hasIt) {
                    break;
                }
            } else {
                logger.warn("Unable to find group for key '" + groupKey + "'");
            }
        }

        return hasIt;
    }

    private boolean hasInheritedRole(JahiaPrincipal principal, JCRNodeWrapper principalNode, String role,
            JCRSessionWrapper session) {
        boolean hasIt = false;
        for (String groupKey : getMembership(principal)) {
            JahiaGroup group = groupManager.lookupGroup(groupKey);
            if (group != null) {
                hasIt = group.hasRole(role);
                if (hasIt) {
                    break;
                }
            } else {
                logger.warn("Unable to find group for key '" + groupKey + "'");
            }
        }

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
     * Injects the dependency to the {@link JahiaGroupManagerService}.
     * 
     * @param groupManager the {@link JahiaGroupManagerService} instance
     */
    public void setGroupManager(JahiaGroupManagerService groupManager) {
        this.groupManager = groupManager;
    }

    /**
     * Injects the dependency to the {@link JCRGroupManagerProvider}.
     * 
     * @param jcrGroupManagerProvider the {@link JCRGroupManagerProvider}
     *            instance
     */
    public void setJCRGroupManagerProvider(JCRGroupManagerProvider jcrGroupManagerProvider) {
        this.jcrGroupManagerProvider = jcrGroupManagerProvider;
    }

    /**
     * Injects the dependency to the {@link RoleManager}.
     * 
     * @param roleManager the {@link RoleManager} instance
     */
    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }
}