/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.apache.jackrabbit.core.security;

import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.RepositoryContext;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.PrivilegeRegistry;
import org.apache.jackrabbit.core.security.authorization.WorkspaceAccessManager;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.Privilege;
import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Set;

/**
 * TODO Comment me
 *
 * @author loom
 *         Date: Oct 15, 2010
 *         Time: 8:12:44 AM
 */
public class JahiaSystemSession extends SessionImpl {

    /**
     * private constructor
     *
     * @param repositoryContext
     * @param wspConfig
     */
    JahiaSystemSession(RepositoryContext repositoryContext, Subject subject,
                       WorkspaceConfig wspConfig)
            throws RepositoryException {
        super(repositoryContext, subject, wspConfig);
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
     * @throws javax.jcr.AccessDeniedException
     *                                       is never thrown
     * @throws javax.jcr.RepositoryException is never thrown
     */
    protected AccessManager createAccessManager(Subject subject)
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
    private class SystemAccessManager extends DefaultAccessManager implements AccessManager {

        private final PrivilegeRegistry privilegeRegistry;

        SystemAccessManager() {
            privilegeRegistry = new PrivilegeRegistry(JahiaSystemSession.this);
        }

        //----------------------------------------------------< AccessManager >

        /**
         * {@inheritDoc}
         *
         * @throws javax.jcr.AccessDeniedException
         *                   is never thrown
         * @throws Exception is never thrown
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
         * @throws javax.jcr.AccessDeniedException
         *                                       is never thrown
         * @throws javax.jcr.RepositoryException is never thrown
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
         * @throws javax.jcr.RepositoryException is never thrown
         */
        public boolean isGranted(ItemId id, int permissions) throws RepositoryException {
            // allow everything
            return true;
        }

        /**
         * Always returns true.
         *
         * @see org.apache.jackrabbit.core.security.AccessManager#isGranted(org.apache.jackrabbit.spi.Path, int)
         */
        public boolean isGranted(Path absPath, int permissions) throws RepositoryException {
            // allow everything
            return true;
        }

        /**
         * Always returns true.
         *
         * @see org.apache.jackrabbit.core.security.AccessManager#isGranted(org.apache.jackrabbit.spi.Path, org.apache.jackrabbit.spi.Name , int)
         */
        public boolean isGranted(Path parentPath, Name childName, int permissions) throws RepositoryException {
            // allow everything
            return true;
        }

        public boolean canRead(Path path, ItemId itemId) throws RepositoryException {
            return true;
        }

        /**
         * Always returns true.
         *
         * @param itemPath
         * @see org.apache.jackrabbit.core.security.AccessManager#canRead(org.apache.jackrabbit.spi.Path)
         */
        public boolean canRead(Path itemPath) throws RepositoryException {
            return true;
        }

        /**
         * {@inheritDoc}
         *
         * @return always <code>true</code>
         * @throws javax.jcr.RepositoryException is never thrown
         */
        public boolean canAccess(String workspaceName) throws RepositoryException {
            return true;
        }

        //-----------------------------------< AbstractAccessControlManager >---

        /**
         * @see org.apache.jackrabbit.core.security.AbstractAccessControlManager#checkInitialized()
         */
        protected void checkInitialized() throws IllegalStateException {
            // nop
        }

        /**
         * @see org.apache.jackrabbit.core.security.AbstractAccessControlManager#checkPermission(String,int)
         */
        protected void checkPermission(String absPath, int permission) throws
                AccessDeniedException, PathNotFoundException, RepositoryException {
            // allow everything
        }

        /**
         * @see org.apache.jackrabbit.core.security.AbstractAccessControlManager#getPrivilegeRegistry()
         */
        protected PrivilegeRegistry getPrivilegeRegistry()
                throws RepositoryException {
            return privilegeRegistry;
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
            return new Privilege[]{getPrivilegeRegistry().getPrivilege(Privilege.JCR_ALL)};
        }

        /**
         * @see javax.jcr.security.AccessControlManager#getEffectivePolicies(String)
         */
        public AccessControlPolicy[] getEffectivePolicies(String absPath) throws
                PathNotFoundException, AccessDeniedException, RepositoryException {
            // cannot determine the effective policies for the system session.
            return new AccessControlPolicy[0];
        }

        public AccessControlPolicy[] getEffectivePolicies(Set<Principal> principals) throws AccessDeniedException, AccessControlException, UnsupportedRepositoryOperationException, RepositoryException {
            return new AccessControlPolicy[0];
        }

        /**
         * @see org.apache.jackrabbit.api.security.JackrabbitAccessControlManager#hasPrivileges(String, java.util.Set, javax.jcr.security.Privilege[])
         */
        public boolean hasPrivileges(String absPath, Set<Principal> principals, Privilege[] privileges) throws PathNotFoundException, RepositoryException {
            throw new UnsupportedOperationException("not implemented");
        }

        /**
         * @see org.apache.jackrabbit.api.security.JackrabbitAccessControlManager#getPrivileges(String, java.util.Set)
         */
        public Privilege[] getPrivileges(String absPath, Set<Principal> principals) throws PathNotFoundException, RepositoryException {
            throw new UnsupportedOperationException("not implemented");
        }
    }
}
