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

package org.apache.jackrabbit.core.security;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.*;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.Privilege;
import javax.security.auth.Subject;

import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.JahiaRepositoryImpl;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.config.LoginModuleConfig;
import org.apache.jackrabbit.core.config.SecurityConfig;
import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.security.authentication.AuthContext;
import org.apache.jackrabbit.core.security.authentication.AuthContextProvider;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.PrivilegeRegistry;
import org.apache.jackrabbit.core.security.authorization.WorkspaceAccessManager;
import org.apache.jackrabbit.core.security.principal.AdminPrincipal;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.jahia.jaas.JahiaPrincipal;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jahia specific implementation for Jackrabbit security manager.
 * @author Thomas Draier
 * Date: Jul 25, 2008
 * Time: 1:50:44 PM
 */
public class JahiaSecurityManager implements JackrabbitSecurityManager {

    private static final Logger log = LoggerFactory.getLogger(JahiaSecurityManager.class);

    /**
     *
     */
    private boolean initialized;

    /**
     * the repository implementation
     */
    private JahiaRepositoryImpl repository;

    /**
     * session on the system workspace.
     */
    private Session securitySession;

    /**
     * factory for login-context {@see Repository#login())
     */
    private AuthContextProvider authContextProvider;

    //------------------------------------------< JackrabbitSecurityManager >---
    /**
     * @see JackrabbitSecurityManager#init(Repository, Session)
     */
    public synchronized void init(Repository repository, Session systemSession) throws RepositoryException {
        if (initialized) {
            throw new IllegalStateException("already initialized");
        }

        securitySession = systemSession;
        this.repository = (JahiaRepositoryImpl) repository;

        SecurityConfig config = this.repository.getConfig().getSecurityConfig();
        LoginModuleConfig loginModConf = config.getLoginModuleConfig();

        // build AuthContextProvider based on appName + optional LoginModuleConfig
        authContextProvider = new JahiaAuthContextProvider(config.getAppName(), loginModConf);
        if (authContextProvider.isLocal()) {
            log.info("init: use Repository Login-Configuration for " + config.getAppName());
        } else if (authContextProvider.isJAAS()) {
            log.info("init: use JAAS login-configuration for " + config.getAppName());
        } else {
            String msg = "Neither JAAS nor RepositoryConfig contained a valid Configuriation for " + config.getAppName();
            log.error(msg);
            throw new RepositoryException(msg);
        }

        initialized = true;
    }

    public String getUserID(Subject subject, String workspace) throws RepositoryException {
        /*if (!subject.getPrincipals(SystemPrincipal.class).isEmpty()) {
            return " system ";
        }*/

        Set<JahiaPrincipal> s =  subject.getPrincipals(JahiaPrincipal.class);
        for (JahiaPrincipal jahiaPrincipal : s) {
            return jahiaPrincipal.getName();
        }
        return null;
    }

    public UserManager getUserManager(Session session) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException("UserManager not supported.");
    }

    public PrincipalManager getPrincipalManager(Session session) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException("PrincipalManager not supported.");
    }

    public AccessManager getAccessManager(Session session, AMContext amContext) throws RepositoryException {
        checkInitialized();
        try {
            JahiaAccessManager accessMgr = new JahiaAccessManager();
            // create subject with SystemPrincipal
            Set<SystemPrincipal> principals = new HashSet<SystemPrincipal>();
            principals.add(new SystemPrincipal());
            Subject subject = new Subject(true, principals, Collections.EMPTY_SET, Collections.EMPTY_SET);
            SystemSession systemSession = new SystemSession(repository, subject,
                    repository.getConfig().getWorkspaceConfig(session.getWorkspace().getName()));
            accessMgr.init(amContext, null, null, systemSession);
            return accessMgr;
        } catch (AccessDeniedException ade) {
            // re-throw
            throw ade;
        } catch (Exception e) {
            // wrap in RepositoryException
            String msg = "Failed to instantiate JahiaAccessManager";
            log.error(msg, e);
            throw new RepositoryException(msg, e);
        }
    }

    public AuthContext getAuthContext(Credentials credentials, Subject subject, String workspace) throws RepositoryException {
        checkInitialized();
        return authContextProvider.getAuthContext(credentials, subject, securitySession, null, JahiaLoginModule.SYSTEM, JahiaLoginModule.GUEST);
    }

    public SecurityConfig getSecurityConfig() throws RepositoryException {
        return repository.getConfig().getSecurityConfig();
    }

    public void close() {
        log.info("close");
    }

    public void dispose(String s) {
        log.info("dispose");
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Not initialized");
        }
    }


    public class SystemSession extends SessionImpl {

    /**
     * private constructor
     *
     * @param rep
     * @param wspConfig
     */
    private SystemSession(RepositoryImpl rep, Subject subject,
                          WorkspaceConfig wspConfig)
            throws RepositoryException {
        super(rep, subject, wspConfig);
    }

    /**
     * Always returns the name of the <code>SystemPrincipal</code>.
     *
     * @return the name of <code>SystemPrincipal</code>.
     */
    protected String retrieveUserId(Subject subject, String workspaceName) throws RepositoryException {
        return new SystemPrincipal().getName();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden in order to create custom access manager
     *
     * @return access manager for system session
     * @throws AccessDeniedException is never thrown
     * @throws RepositoryException   is never thrown
     */
    protected AccessManager createAccessManager(Subject subject,
                                                HierarchyManager hierMgr)
            throws AccessDeniedException, RepositoryException {
        /**
         * use own AccessManager implementation rather than relying on
         * configurable AccessManager to handle SystemPrincipal privileges
         * correctly
         */
        return new SystemAccessManager();
    }

    //--------------------------------------------------------< inner classes >
    /**
     * An access manager that grants access to everything.
     */
    private class SystemAccessManager extends AbstractAccessControlManager implements AccessManager {

        private final PrivilegeRegistry privilegeRegistry;

        SystemAccessManager() {
            privilegeRegistry = new PrivilegeRegistry(SystemSession.this);
        }

        //----------------------------------------------------< AccessManager >
        /**
         * {@inheritDoc}
         *
         * @throws AccessDeniedException is never thrown
         * @throws Exception             is never thrown
         */
        public void init(AMContext context)
                throws AccessDeniedException, Exception {
            // nop
        }

        public void init(AMContext context, AccessControlProvider acProvider, WorkspaceAccessManager wspAccessMgr) throws AccessDeniedException, Exception {
            // nop
        }

        /**
         * {@inheritDoc}
         */
        public void close() throws Exception {
            // nop
        }

        /**
         * {@inheritDoc}
         *
         * @throws AccessDeniedException is never thrown
         * @throws RepositoryException   is never thrown
         */
        public void checkPermission(ItemId id, int permissions)
                throws AccessDeniedException, RepositoryException {
            // allow everything
        }

        /**
         * {@inheritDoc}
         */
        public void checkPermission(Path absPath, int permissions) throws AccessDeniedException, RepositoryException {
            // allow everything
        }

        /**
         * {@inheritDoc}
         *
         * @return always <code>true</code>
         * @throws RepositoryException   is never thrown
         */
        public boolean isGranted(ItemId id, int permissions) throws RepositoryException {
            // allow everything
            return true;
        }

        /**
         * Always returns true.
         *
         * @see AccessManager#isGranted(Path, int)
         */
        public boolean isGranted(Path absPath, int permissions) throws RepositoryException {
            // allow everything
            return true;
        }

        /**
         * Always returns true.
         *
         * @see AccessManager#isGranted(Path, org.apache.jackrabbit.spi.Name , int)
         */
        public boolean isGranted(Path parentPath, Name childName, int permissions) throws RepositoryException {
            // allow everything
            return true;
        }

        /**
         * Always returns true.
         *
         * @see AccessManager#canRead(Path)
         * @param itemPath
         */
        public boolean canRead(Path itemPath) throws RepositoryException {
            return true;
        }

        /**
         * {@inheritDoc}
         *
         * @return always <code>true</code>
         * @throws RepositoryException      is never thrown
         */
        public boolean canAccess(String workspaceName) throws RepositoryException {
            return true;
        }

        //-----------------------------------< AbstractAccessControlManager >---
        /**
         * @see AbstractAccessControlManager#checkInitialized()
         */
        protected void checkInitialized() throws IllegalStateException {
            // nop
        }

        /**
         * @see AbstractAccessControlManager#checkPermission(String,int)
         */
        protected void checkPermission(String absPath, int permission) throws
                AccessDeniedException, PathNotFoundException, RepositoryException {
            // allow everything
        }

        /**
         * @see AbstractAccessControlManager#getPrivilegeRegistry()
         */
        protected PrivilegeRegistry getPrivilegeRegistry()
                throws RepositoryException {
            return privilegeRegistry;
        }

        /**
         * @see AbstractAccessControlManager#checkValidNodePath(String)
         */
        protected void checkValidNodePath(String absPath)
                throws PathNotFoundException, RepositoryException {
            Path p = getQPath(absPath);
            if (!p.isAbsolute()) {
                throw new RepositoryException("Absolute path expected.");
            }
            if (hierMgr.resolveNodePath(p) == null) {
                throw new PathNotFoundException("No such node " + absPath);
            }
        }

        //-------------------------------------------< AccessControlManager >---
        /**
         * @see javax.jcr.security.AccessControlManager#hasPrivileges(String, javax.jcr.security.Privilege[])
         */
        public boolean hasPrivileges(String absPath, Privilege[] privileges)
                throws PathNotFoundException, RepositoryException {
            checkValidNodePath(absPath);
            // allow everything
            return true;
        }

        /**
         * @see javax.jcr.security.AccessControlManager#getPrivileges(String)
         */
        public Privilege[] getPrivileges(String absPath)
                throws PathNotFoundException, RepositoryException {
            checkValidNodePath(absPath);
            return new Privilege[] {getPrivilegeRegistry().getPrivilege(Privilege.JCR_ALL)};
        }

        /**
         * @see javax.jcr.security.AccessControlManager#getEffectivePolicies(String)
         */
        public AccessControlPolicy[] getEffectivePolicies(String absPath) throws
                PathNotFoundException, AccessDeniedException, RepositoryException {
            // cannot determine the effective policies for the system session.
            return new AccessControlPolicy[0];
        }

        /**
     * @see org.apache.jackrabbit.api.security.JackrabbitAccessControlManager#hasPrivileges(String, Set, Privilege[])
     */
        public boolean hasPrivileges(String absPath, Set<Principal> principals, Privilege[] privileges) throws PathNotFoundException, RepositoryException {
            throw new UnsupportedOperationException("not implemented");
        }

        /**
         * @see org.apache.jackrabbit.api.security.JackrabbitAccessControlManager#getPrivileges(String, Set)
         */
        public Privilege[] getPrivileges(String absPath, Set<Principal> principals) throws PathNotFoundException, RepositoryException {
            throw new UnsupportedOperationException("not implemented");
        }
    }
}
}
