/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.apache.jackrabbit.core.security;

import org.apache.jackrabbit.core.RepositoryContext;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
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
 * Jahia JCR system session.
 *
 * @author loom
 */
public class JahiaSystemSession extends SessionImpl {
    
    static final AccessControlPolicy[] ACCESS_CONTROL_POLICIES = new AccessControlPolicy[0];
    
    private static final String SYSTEM_PRINCIPAL_NAME = new SystemPrincipal().getName();

    private static SystemAccessManager systemAccessManager;

    /**
     * private constructor
     *
     * @param repositoryContext
     * @param wspConfig
     */
    public JahiaSystemSession(RepositoryContext repositoryContext, Subject subject,
                       WorkspaceConfig wspConfig)
            throws RepositoryException {
        super(repositoryContext, subject, wspConfig);
    }

    /**
     * Always returns the name of the <code>SystemPrincipal</code>.
     *
     * @return the name of <code>SystemPrincipal</code>.
     */
    protected final String retrieveUserId(Subject subject, String workspaceName) throws RepositoryException {
        return SYSTEM_PRINCIPAL_NAME;
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
        if (systemAccessManager == null) {
            systemAccessManager = new SystemAccessManager();
        }
        return systemAccessManager;
    }

//--------------------------------------------------------< inner classes >

    /**
     * An access manager that grants access to everything.
     */
    private class SystemAccessManager extends DefaultAccessManager implements AccessManager {

        SystemAccessManager() {
            super();
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
         * {@inheritDoc}
         *
         * @return always <code>true</code>
         * @throws javax.jcr.RepositoryException is never thrown
         */
        public boolean canAccess(String workspaceName) throws RepositoryException {
            return true;
        }

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
        
        @Override
        public void checkRepositoryPermission(int permissions) throws AccessDeniedException, RepositoryException {
            // allow everything
        }

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
            return new Privilege[] { privilegeFromName(Privilege.JCR_ALL) };
        }

        /**
         * @see javax.jcr.security.AccessControlManager#getEffectivePolicies(String)
         */
        public AccessControlPolicy[] getEffectivePolicies(String absPath) throws
                PathNotFoundException, AccessDeniedException, RepositoryException {
            // cannot determine the effective policies for the system session.
            return ACCESS_CONTROL_POLICIES;
        }

        public AccessControlPolicy[] getEffectivePolicies(Set<Principal> principals) throws AccessDeniedException, AccessControlException, UnsupportedRepositoryOperationException, RepositoryException {
            return ACCESS_CONTROL_POLICIES;
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
