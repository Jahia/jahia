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

import static org.jahia.services.rbac.jcr.RoleManager.PROPERTY_PERMISSSIONS;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.rbac.Permission;
import org.jahia.services.rbac.Role;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRGroup;
import org.jahia.services.usermanager.jcr.JCRGroupManagerProvider;
import org.jahia.services.usermanager.jcr.JCRPrincipal;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;

/**
 * Service implementation for the Role Based Access Control.
 * 
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public class RoleBasedAccessControlManager {

    private static final List<JahiaPrincipal> EMPTY_PRINCIPAL_LIST = Collections.emptyList();

    private static final String JMIX_ROLE_BASED_ACCESS_CONTROLLED = "jmix:roleBasedAccessControlled";

    private static Logger logger = Logger.getLogger(RoleBasedAccessControlManager.class);

    private static final String PROPERTY_ROLES = "j:roles";

    private JahiaGroupManagerService groupManager;

    private JCRGroupManagerProvider jcrGroupManagerProvider;

    private JCRUserManagerProvider jcrUserManagerProvider;

    private RoleManager roleManager;

    private JahiaSitesService sitesService;

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

    /**
     * Retrieved a JCR node that corresponds to the specified principal.
     * 
     * @param principal the principal to look up
     * @param session current JCR session
     * @return a JCR node that corresponds to the specified principal or null if
     *         the node cannot be retrieved
     * @throws RepositoryException in case of an error
     */
    protected JCRNodeWrapper getPrincipalNode(JahiaPrincipal principal, JCRSessionWrapper session)
            throws RepositoryException {
        return getPrincipalNode(principal, session, false);
    }

    /**
     * Retrieved a JCR node that corresponds to the specified principal.
     * 
     * @param principal the principal to look up
     * @param session current JCR session
     * @param createExternalIfNeeded should we create a a node in the JCR for an
     *            external principal if needed?
     * @return a JCR node that corresponds to the specified principal or null if
     *         the node cannot be retrieved
     * @throws RepositoryException in case of an error
     */
    protected JCRNodeWrapper getPrincipalNode(JahiaPrincipal principal, JCRSessionWrapper session,
            boolean createExternalIfNeeded) throws RepositoryException {
        JCRNodeWrapper principalNode = null;
        if (principal instanceof JCRPrincipal) {
            try {
                principalNode = ((JCRPrincipal) principal).getNode(session);
            } catch (ItemNotFoundException e) {
                logger.warn("Unable to find principal node with identifier "
                        + ((JCRPrincipal) principal).getIdentifier());
            }
        } else if (principal instanceof JahiaUser) {
            JCRUser externalUser = jcrUserManagerProvider.lookupExternalUser(principal.getName());
            if (externalUser == null && createExternalIfNeeded) {
                JCRStoreService.getInstance().deployExternalUser(principal.getName(),
                        ((JahiaUser) principal).getProviderName());
                externalUser = jcrUserManagerProvider.lookupExternalUser(principal.getName());
            }
            if (externalUser != null) {
                try {
                    principalNode = externalUser.getNode(session);
                } catch (ItemNotFoundException e) {
                    logger.warn("Unable to find user node with identifier " + externalUser.getIdentifier());
                }
            }
        } else if (principal instanceof JahiaGroup) {
            JahiaGroup grp = (JahiaGroup) principal;
            JCRGroup externalGroup = jcrGroupManagerProvider.lookupExternalGroup(grp.getGroupname());
            if (externalGroup == null && createExternalIfNeeded) {
                externalGroup = jcrGroupManagerProvider.createExternalGroup(grp.getGroupKey(), grp.getProviderName());
            }
            if (externalGroup != null) {
                try {
                    principalNode = externalGroup.getNode(session);
                } catch (ItemNotFoundException e) {
                    logger.warn("Unable to find group node with identifier " + externalGroup.getIdentifier());
                }
            }
        }
        return principalNode;
    }

    /**
     * Returns a list of principals having the specified permission or an empty
     * list if the permission is not granted to anyone.
     * 
     * @param permission the permission to check for
     * @param session current JCR session
     * @return a list of principals having the specified permission or an empty
     *         list if the permission is not granted to anyone
     * @throws RepositoryException in case of an error
     * @throws PathNotFoundException in case the specified permission cannot be
     *             found
     */
    public List<JahiaPrincipal> getPrincipalsInPermission(Permission permission, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        Set<JahiaPrincipal> principals = new LinkedHashSet<JahiaPrincipal>();
        for (RoleImpl role : roleManager.getRolesInPermission(permission, session)) {
            principals.addAll(getPrincipalsInRole(role, session));
        }

        return principals.isEmpty() ? EMPTY_PRINCIPAL_LIST : new LinkedList<JahiaPrincipal>(principals);
    }

    /**
     * Returns a list of principals having the specified role or an empty list
     * if the role is not granted to anyone.
     * 
     * @param role the role
     * @param session current JCR session
     * @return a list of principals having the specified role or an empty list
     *         if the role is not granted to anyone
     * @throws RepositoryException in case of an error
     */
    public List<JahiaPrincipal> getPrincipalsInRole(final Role role, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        List<JahiaPrincipal> principals = new LinkedList<JahiaPrincipal>();
        JCRNodeWrapper roleNode = null;
        try {
            roleNode = roleManager.loadRoleNode(role, session);
        } catch (PathNotFoundException e) {
            // role no found
            logger.warn("Cannot find role node for " + role);
        }
        if (roleNode != null) {
            for (PropertyIterator iterator = roleNode.getWeakReferences(PROPERTY_ROLES); iterator.hasNext();) {
                Property prop = iterator.nextProperty();
                Node principalNode = prop.getParent();
                if (principalNode != null) {
                    JahiaPrincipal principal = toPrincipal(principalNode);
                    if (principal != null) {
                        principals.add(principal);
                    }
                } else {
                    logger.warn("Principal node, referencing role '" + roleNode.getPath() + "' in property '"
                            + prop.getPath() + "' cannot be found");
                }

            }
        }
        return principals.isEmpty() ? EMPTY_PRINCIPAL_LIST : principals;
    }

    /**
     * Grants roles to the specified principal.
     * 
     * @param principal principal to grant roles to
     * @param roles the list of roles to be granted
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void grantRoles(final JahiaPrincipal principal, List<Role> roles, JCRSessionWrapper session)
            throws RepositoryException {
        if (roles == null || roles.isEmpty()) {
            return;
        }

        JCRNodeWrapper principalNode = getPrincipalNode(principal, session, true);
        if (principalNode != null) {
            session.checkout(principalNode);

            if (!principalNode.isNodeType(JMIX_ROLE_BASED_ACCESS_CONTROLLED)) {
                principalNode.addMixin(JMIX_ROLE_BASED_ACCESS_CONTROLLED);
            }

            Set<String> toBeGranted = new LinkedHashSet<String>(roles.size());
            for (Role role : roles) {
                try {
                    toBeGranted.add(roleManager.loadRoleNode(role, session).getIdentifier());
                } catch (PathNotFoundException ex) {
                    logger.warn("Unable to find a node that corresponds to a role '" + role);
                }
            }

            List<Value> newValues = new LinkedList<Value>();
            if (principalNode.hasProperty(PROPERTY_ROLES)) {
                Value[] oldValues = principalNode.getProperty(PROPERTY_ROLES).getValues();
                for (Value oldOne : oldValues) {
                    try {
                        if (session.getNodeByIdentifier(oldOne.getString()) != null) {
                            newValues.add(oldOne);
                        }
                    } catch (ItemNotFoundException e) {
                        logger.debug("Removing 'dead' reference to " + oldOne.getString());
                    }
                    toBeGranted.remove(oldOne.getString());
                }
            }
            for (String granted : toBeGranted) {
                newValues.add(new ValueImpl(granted, PropertyType.WEAKREFERENCE));
            }
            principalNode.setProperty(PROPERTY_ROLES, newValues.toArray(new Value[] {}));
            session.save();

            invalidateCache(principal);
        } else {
            logger.warn("Unable to find corresponding JCR node for principal " + principal + ". Skip granting roles.");
        }
    }

    private boolean hasDirectPermission(String principalPath, Permission permission, JCRSessionWrapper session)
            throws InvalidQueryException, RepositoryException {
        boolean hasIt = false;
        JCRNodeWrapper permissionNode = roleManager.loadPermissionNode(permission, session);
        QueryResult result = session.getWorkspace().getQueryManager().createQuery(
                "/jcr:root" + ISO9075.encodePath(principalPath) + "/jcr:deref(@" + PROPERTY_ROLES + ", '*')/jcr:deref(@"
                        + PROPERTY_PERMISSSIONS + ", " + JCRContentUtils.stringToQueryLiteral(permission.getName())
                        + ")", Query.XPATH).execute();
        // the permission is specified by path
        for (NodeIterator iterator = result.getNodes(); iterator.hasNext();) {
            Node node = iterator.nextNode();
            if (node.getIdentifier().equals(permissionNode.getIdentifier())) {
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

    private boolean hasInheritedPermission(JahiaPrincipal principal, JCRNodeWrapper principalNode,
            Permission permission, JCRSessionWrapper session) {
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

    private boolean hasInheritedRole(JahiaPrincipal principal, JCRNodeWrapper principalNode, Role role,
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

    /**
     * Returns {@code true} if this principal has the specified role, {@code
     * false} otherwise.
     * 
     * @param principal the principal to check for role
     * @param role the application-specific role identifier
     * @param session current JCR session
     * @return {@code true} if this principal has the specified role, {@code
     *         false} otherwise
     * @throws PathNotFoundException if the corresponding role node cannot be
     *             found
     * @throws RepositoryException in case of an error
     */
    public boolean hasRole(final JahiaPrincipal principal, final Role role, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        JCRNodeWrapper principalNode = getPrincipalNode(principal, session);
        JCRNodeWrapper roleNode = null;
        roleNode = roleManager.loadRoleNode(role, session);
        return principalNode != null && roleNode != null ? hasDirectRole(principalNode, roleNode, session)
                || hasInheritedRole(principal, principalNode, role, session) : false;
    }

    private void invalidateCache(JahiaPrincipal principal) {
        // TODO implement cache invalidation
    }

    /**
     * Returns {@code true} if this principal is permitted to perform an action
     * or access a resource summarized by the specified permission string.
     * 
     * @param principal the principal to check for permission
     * @param permission the identifier of a permission that is being checked
     * @param session current JCR session
     * @return {@code true} if this principal is permitted to perform an action
     *         or access a resource summarized by the specified permission
     * @throws PathNotFoundException if the corresponding permission node cannot
     *             be found
     * @throws RepositoryException in case of an error
     */
    public boolean isPermitted(final JahiaPrincipal principal, final Permission permission, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        JCRNodeWrapper principalNode = getPrincipalNode(principal, session);
        boolean permitted = principalNode != null ? hasDirectPermission(principalNode.getPath(), permission, session)
                || hasInheritedPermission(principal, principalNode, permission, session) : false;
        if (logger.isDebugEnabled()) {
            logger.debug("isPermitted('" + principal.getName() + "', '" + permission.getName() + "'): " + permitted);
        }
        return permitted;
    }

    /**
     * Revokes all roles from the specified principal.
     * 
     * @param principal principal to revoke roles from
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void revokeAllRoles(final JahiaPrincipal principal, JCRSessionWrapper session) throws RepositoryException {

        JCRNodeWrapper principalNode = getPrincipalNode(principal, session);
        if (principalNode != null && principalNode.isNodeType(JMIX_ROLE_BASED_ACCESS_CONTROLLED)
                && principalNode.hasProperty(PROPERTY_ROLES)) {
            Value[] values = principalNode.getProperty(PROPERTY_ROLES).getValues();
            if (values != null) {
                if (values.length != 0) {
                    session.checkout(principalNode);
                    principalNode.setProperty(PROPERTY_ROLES, new Value[] {});
                    session.save();
                    invalidateCache(principal);
                }
            }
        } else {
            if (principalNode == null) {
                logger.warn("Unable to find corresponding JCR node for principal " + principal
                        + ". Skip revoking roles.");
            } else {
                logger.warn("Principal '" + principal.getName()
                        + "' does not have any roles assigned. Skip revoking roles.");
            }
        }
    }

    /**
     * Revokes roles from the specified principal.
     * 
     * @param principal principal to revoke roles from
     * @param roles the list of roles to be revoked
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void revokeRoles(final JahiaPrincipal principal, List<Role> roles, JCRSessionWrapper session)
            throws RepositoryException {
        if (roles == null || roles.isEmpty()) {
            return;
        }

        JCRNodeWrapper principalNode = getPrincipalNode(principal, session);
        if (principalNode != null && principalNode.isNodeType(JMIX_ROLE_BASED_ACCESS_CONTROLLED)
                && principalNode.hasProperty(PROPERTY_ROLES)) {
            Value[] values = principalNode.getProperty(PROPERTY_ROLES).getValues();
            if (values != null) {
                Set<String> toRevoke = new LinkedHashSet<String>(roles.size());
                for (Role role : roles) {
                    try {
                        toRevoke.add(roleManager.loadRoleNode(role, session).getIdentifier());
                    } catch (PathNotFoundException ex) {
                        logger.warn("Unable to find a node that corresponds to a role '" + role);
                    }
                }

                List<Value> newValues = new LinkedList<Value>();
                for (Value value : values) {
                    JCRNodeWrapper roleNode = (JCRNodeWrapper) ((JCRValueWrapper) value).getNode();
                    if (roleNode != null && !toRevoke.contains(roleNode.getIdentifier())) {
                        newValues.add(value);
                    }
                }
                if (values.length != newValues.size()) {
                    session.checkout(principalNode);
                    principalNode.setProperty(PROPERTY_ROLES, newValues.toArray(new Value[] {}));
                    session.save();

                    invalidateCache(principal);
                }
            }
        } else {
            if (principalNode == null) {
                logger.warn("Unable to find corresponding JCR node for principal " + principal
                        + ". Skip revoking roles.");
            } else {
                logger.warn("Principal '" + principal.getName()
                        + "' does not have any roles assigned. Skip revoking roles.");
            }
        }
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
     * @param jcrUserManagerProvider the jcrUserManagerProvider to set
     */
    public void setJCRUserManagerProvider(JCRUserManagerProvider jcrUserManagerProvider) {
        this.jcrUserManagerProvider = jcrUserManagerProvider;
    }

    /**
     * Injects the dependency to {@link RoleManager}.
     * 
     * @param roleManager the dependency to {@link RoleManager}
     */
    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

    /**
     * @param sitesService the sitesService to set
     */
    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    private JahiaPrincipal toPrincipal(Node principalNode) throws RepositoryException {
        JahiaPrincipal principal = null;
        if (principalNode.isNodeType(Constants.JAHIANT_USER)) {
            // principal is a user
            JahiaUser user = null;
            if (principalNode.hasProperty(JCRUser.J_EXTERNAL)
                    && principalNode.getProperty(JCRUser.J_EXTERNAL).getBoolean()) {
                // lookup external user node
                user = jcrUserManagerProvider.lookupExternalUser(principalNode.getName());
            } else {
                // lookup internal user node
                user = jcrUserManagerProvider.lookupUser(principalNode.getName());
            }
            if (user != null) {
                principal = user;
            } else {
                logger.warn("User cannot be found by name '" + principalNode.getName() + "'");
            }
        } else if (principalNode.isNodeType(Constants.JAHIANT_GROUP)) {
            // principal is a group
            if (principalNode.hasProperty(JCRUser.J_EXTERNAL)
                    && principalNode.getProperty(JCRUser.J_EXTERNAL).getBoolean()) {
                // lookup external group node
                principal = jcrGroupManagerProvider.lookupExternalGroup(principalNode.getName());
            } else {
                int siteId = 0;
                String siteKey = StringUtils.substringBetween(principalNode.getPath(), "/sites/", "/");
                if (siteKey != null) {
                    JahiaSite site = null;
                    try {
                        site = sitesService.getSiteByKey(siteKey);
                    } catch (JahiaException e) {
                        logger.error(e.getMessage(), e);
                    }
                    if (site != null) {
                        siteId = site.getID();
                    } else {
                        logger.warn("Unable to lookup site for key '" + siteKey + "'. Skipping group '"
                                + principalNode.getPath() + "'");
                        siteId = -1;
                    }
                }
                if (siteId != -1) {
                    JahiaGroup group = jcrGroupManagerProvider.lookupGroup(siteId, principalNode.getName());
                    if (group != null) {
                        principal = group;
                    } else {
                        logger.warn("Group cannot be found for name '" + principalNode.getName() + "' and site ID "
                                + siteId);
                    }
                }
            }

        }
        return principal;
    }

}