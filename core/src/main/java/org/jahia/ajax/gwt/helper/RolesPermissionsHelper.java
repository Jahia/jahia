package org.jahia.ajax.gwt.helper;

import java.security.Principal;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaPermission;
import org.jahia.ajax.gwt.client.data.GWTJahiaPrincipal;
import org.jahia.ajax.gwt.client.data.GWTJahiaRole;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.data.GWTRolesPermissions;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.rbac.jcr.*;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;

/**
 * Roles and permission GWT helper class.
 *
 * @author ktlili
 *         Date: Feb 3, 2010
 *         Time: 4:15:49 PM
 */
public class RolesPermissionsHelper {
    private static Logger logger = Logger.getLogger(RolesPermissionsHelper.class);

    private JahiaGroupManagerService groupManagerService;
    private RoleManager roleManager;
    private JahiaUserManagerService userManagerService;

    /**
     * Grants the specified permissions to a role.
     *
     * @param role        the role to grant permissions
     * @param permissions permissions to be granted
     * @param jcrSession  current JCR session
     * @throws GWTJahiaServiceException in case of an error
     */
    public void addRolePermissions(GWTJahiaRole role, List<GWTJahiaPermission> permissions, JCRSessionWrapper jcrSession)
            throws GWTJahiaServiceException {
        List<String> permissionJcrPaths = new LinkedList<String>();
        for (GWTJahiaPermission perm : permissions) {
            permissionJcrPaths.add(perm.getId());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("addRolePermissions() ," + role.getId() + "," + permissionJcrPaths);
        }
        try {
            roleManager.grantPermissions(role.getId(), permissionJcrPaths, jcrSession);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /**
     * Get all permissions for the specified site or for the server if the site
     * is not specified.
     *
     * @param site       target site key or {@code null} if the server level
     *                   permissions are requested
     * @param jcrSession current JCR session
     * @return all permissions for the specified site or for the server if the
     *         site is not specified
     * @throws GWTJahiaServiceException in case of an error
     */
    public List<GWTJahiaPermission> getPermissions(String site, JCRSessionWrapper jcrSession)
            throws GWTJahiaServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving permissions for " + (site != null ? "site " + site : "server"));
        }

        List<GWTJahiaPermission> permissions = new LinkedList<GWTJahiaPermission>();
        try {
            for (JCRPermission permission : roleManager.getPermissions(site, jcrSession)) {
                permissions.add(toPermission(permission));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

        return permissions;
    }

    /**
     * Get list of granted permissions
     *
     * @param jcrSession
     * @return
     */
    public List<GWTJahiaPermission> getGrantedPermissions(final String site, JCRSessionWrapper jcrSession) throws GWTJahiaServiceException {
        final List<GWTJahiaPermission> permissions = new LinkedList<GWTJahiaPermission>();
        try {
            final JahiaUser user = jcrSession.getUser();
            JCRTemplate.getInstance().doExecuteWithSystemSession(
                    new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session)
                                throws RepositoryException {
                            // site permission
                            for (JCRPermission permission : roleManager
                                    .getPermissions(site, session)) {
                                if (user.isPermitted((StringUtils
                                        .isEmpty(permission.getGroup()) ? ""
                                        : permission.getGroup() + "/")
                                        + permission.getName(), site)) {
                                    permissions.add(toPermission(permission));
                                }
                            }

                            // server permission
                            for (JCRPermission permission : roleManager
                                    .getPermissions(null, session)) {
                                if (user.isPermitted((StringUtils
                                        .isEmpty(permission.getGroup()) ? ""
                                        : permission.getGroup() + "/")
                                        + permission.getName())) {
                                    permissions.add(toPermission(permission));
                                }
                            }
                            return permissions;
                        }
                    });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

        return permissions;
    }

    /**
     * Get principal in role
     *
     * @param role
     * @param jcrSession current JCR session
     * @return
     * @throws GWTJahiaServiceException in case of an error
     */
    public List<GWTJahiaPrincipal> getPrincipalsInRole(GWTJahiaRole role, JCRSessionWrapper jcrSession)
            throws GWTJahiaServiceException {

        if (logger.isDebugEnabled()) {
            logger.debug("getPrincipalsInRole() ," + role.getId());
        }

        List<GWTJahiaPrincipal> p = new LinkedList<GWTJahiaPrincipal>();
        try {
            for (JahiaPrincipal jahiaPrincipal : roleManager.getPrincipalsInRole(role.getId(), jcrSession)) {
                GWTJahiaPrincipal gwtPrincipal = null;
                if (jahiaPrincipal instanceof JahiaUser) {
                    JahiaUser user = (JahiaUser) jahiaPrincipal;
                    GWTJahiaUser gwtUser = new GWTJahiaUser(user.getUsername(), user.getUserKey());
                    gwtUser.setProvider(user.getProviderName());
                    gwtPrincipal = gwtUser;
                } else if (jahiaPrincipal instanceof JahiaGroup) {
                    JahiaGroup group = (JahiaGroup) jahiaPrincipal;
                    GWTJahiaGroup gwtGroup = new GWTJahiaGroup(group.getGroupname(), group.getGroupKey());
                    gwtGroup.setProvider(group.getProviderName());
                    gwtPrincipal = gwtGroup;
                } else {
                    logger.warn("Unknown principal type for principal " + jahiaPrincipal);
                }
                if (gwtPrincipal != null) {
                    p.add(gwtPrincipal);
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

        return p;
    }

    /**
     * Get all roles for the specified site or for the server if the site is not
     * specified.
     *
     * @param site       target site key or {@code null} if the server level roles are
     *                   requested
     * @param jcrSession current JCR session
     * @return all roles for the specified site or for the server if the site is
     *         not specified
     * @throws GWTJahiaServiceException in case of an error
     */
    public List<GWTJahiaRole> getRoles(String site, JCRSessionWrapper jcrSession) throws GWTJahiaServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving roles for " + (site != null ? "site " + site : "server"));
        }

        logger.error("Retrieving roles for " + (site != null ? "site " + site : "server"));

        List<GWTJahiaRole> roles = new LinkedList<GWTJahiaRole>();
        try {
            for (JCRRole role : roleManager.getRoles(site, jcrSession)) {
                roles.add(toRole(role));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

        return roles;
    }

    /**
     * Get all roles and all permissions
     *
     * @param site       the site key to retrieve roles and permissions
     * @param jcrSession current user JCR session
     * @return
     * @throws GWTJahiaServiceException in case of an error
     */
    public GWTRolesPermissions getRolesAndPermissions(String site, JCRSessionWrapper jcrSession)
            throws GWTJahiaServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("getting roles and permission for " + (site != null ? "site " + site : "server"));
        }

        GWTRolesPermissions rp = new GWTRolesPermissions();
        rp.setRoles(getRoles(site, jcrSession));
        rp.setPermissions(getPermissions(site, jcrSession));
        return rp;
    }

    /**
     * Grant role to principals
     *
     * @param role
     * @param principals
     * @param jcrSession current user JCR session
     */
    public void grantRoleToPrincipals(GWTJahiaRole role, List<GWTJahiaPrincipal> principals,
                                      JCRSessionWrapper jcrSession) throws GWTJahiaServiceException {
        List<JahiaPrincipal> subjects = new LinkedList<JahiaPrincipal>();
        for (GWTJahiaPrincipal principal : principals) {
            subjects.add(lookupPrincipal(principal));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("grantRoleToPrincipals() ," + role.getId() + "," + principals);
        }
        RepositoryException ex = null;
        for (JahiaPrincipal jahiaPrincipal : subjects) {
            try {
                roleManager.grantRole(jahiaPrincipal, role.getId(), jcrSession);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                ex = e;
            }
        }
        if (ex != null) {
            throw new GWTJahiaServiceException(ex.getMessage());
        }
    }

    /**
     * Grant role to users
     *
     * @param role
     */
    public void grantRoleToUser(GWTJahiaRole role, boolean isGroup, String principalKey,JCRSessionWrapper jcrSession) throws GWTJahiaServiceException{
        if (logger.isDebugEnabled()) {
            logger.debug("grantRoleToUser() ," + role + "," + principalKey);
        }
        RepositoryException ex = null;
        JahiaPrincipal p = null;
        try {
            if (!isGroup) {
                p = userManagerService.lookupUserByKey(principalKey);
            } else {
                p = groupManagerService.lookupGroup(principalKey);
            }
            roleManager.grantRole(p, role.getId(), jcrSession);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            ex = e;
        }
        if (ex != null) {
            throw new GWTJahiaServiceException(ex.getMessage());
        }


    }

    /**
     * Check if the role is granted to the user
     *
     * @param role
     * @param isGroup
     * @param principalKey
     * @return
     */
    public boolean isGrant(GWTJahiaRole role, boolean isGroup, String principalKey) {
        if (isGroup) {
            JahiaGroup group = groupManagerService.lookupGroup(principalKey);
            if (group != null) {
                return group.hasRole(role.getId());
            }
        } else {
            JahiaUser user = userManagerService.lookupUser(principalKey);
            if (user != null) {
                return user.hasRole(role.getId());
            }
        }
        return false;
    }

    private JahiaPrincipal lookupPrincipal(GWTJahiaPrincipal principal) {
        if (principal instanceof GWTJahiaUser) {
            return userManagerService.lookupUserByKey(((GWTJahiaUser) principal).getKey());
        } else if (principal instanceof GWTJahiaGroup) {
            return groupManagerService.lookupGroup(((GWTJahiaGroup) principal).getGroupKey());
        } else {
            logger.warn("Unknown principal type " + principal);
        }
        return null;
    }

    /**
     * Revokes specified permissions from a role.
     *
     * @param role        the role to revoke permissions from
     * @param permissions permissions to be revoked
     * @param jcrSession  current JCR session
     * @throws GWTJahiaServiceException in case of an error
     */
    public void removeRolePermissions(GWTJahiaRole role, List<GWTJahiaPermission> permissions,
                                      JCRSessionWrapper jcrSession) throws GWTJahiaServiceException {
        List<String> permissionJcrPaths = new LinkedList<String>();
        for (GWTJahiaPermission perm : permissions) {
            permissionJcrPaths.add(perm.getId());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("removeRolePermissions() ," + role.getId() + "," + permissionJcrPaths);
        }
        try {
            roleManager.revokePermissions(role.getId(), permissionJcrPaths, jcrSession);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /**
     * REmove role to principals
     *
     * @param role
     * @param principals
     * @param jcrSession current user JCR session
     * @throws GWTJahiaServiceException in case of an error
     */
    public void removeRoleToPrincipals(GWTJahiaRole role, List<GWTJahiaPrincipal> principals, JCRSessionWrapper jcrSession) throws GWTJahiaServiceException {
        List<JahiaPrincipal> subjects = new LinkedList<JahiaPrincipal>();
        for (GWTJahiaPrincipal principal : principals) {
            subjects.add(lookupPrincipal(principal));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("removeRoleToPrincipals() ," + role.getId() + "," + principals);
        }
        RepositoryException ex = null;
        for (JahiaPrincipal jahiaPrincipal : subjects) {
            try {
                roleManager.revokeRole(jahiaPrincipal, role.getId(), jcrSession);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                ex = e;
            }
        }
        if (ex != null) {
            throw new GWTJahiaServiceException(ex.getMessage());
        }
    }

    /**
     * Remove role to user
     *
     * @param role
     */
    public void removeRoleToUser(GWTJahiaRole role, String principalKey) {
        logger.debug("removeRoleToUser() ," + role + "," + principalKey);
    }

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    /**
     * Injects the role management service instance.
     *
     * @param roleManager the role management service instance
     */
    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    private GWTJahiaPermission toPermission(JCRPermission permission) {
        GWTJahiaPermission gwtPermission = new GWTJahiaPermission();
        gwtPermission.setId(permission.getPath());
        gwtPermission.setName(permission.getTitle() != null ? permission.getTitle() : permission.getName());
        if (permission.getGroup() != null && permission.getGroup().equalsIgnoreCase("languages")) {
            gwtPermission.setLabel(LanguageHelper.getDisplayName(gwtPermission.getName()));

        } else {
            gwtPermission.setLabel(gwtPermission.getName());
        }
        gwtPermission.setGroup(permission.getGroup());
        if (permission instanceof JCRSitePermission) {
            gwtPermission.setSite(((JCRSitePermission) permission).getSite());
        }

        return gwtPermission;
    }

    private GWTJahiaRole toRole(JCRRole role) {
        GWTJahiaRole gwtRole = new GWTJahiaRole(role.getPath(), role.getTitle() != null ? role.getTitle() : role.getName());
        List<GWTJahiaPermission> gwtPermissions = new LinkedList<GWTJahiaPermission>();
        for (JCRPermission perm : role.getPermissions()) {
            gwtPermissions.add(toPermission(perm));
        }
        gwtRole.setPermissions(gwtPermissions);
        if (role instanceof JCRSiteRole) {
            gwtRole.setSite(((JCRSiteRole) role).getSite());
        } else {
            gwtRole.setSite("-");
        }

        return gwtRole;
    }
}