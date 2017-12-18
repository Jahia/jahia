/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.security;

import org.apache.jackrabbit.core.RepositoryContext;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.WorkspaceAccessManager;
import org.apache.jackrabbit.core.session.SessionContext;
import org.apache.jackrabbit.core.session.SessionOperation;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;

import javax.jcr.*;
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
    
    private static final AccessControlPolicy[] ACCESS_CONTROL_POLICIES = new AccessControlPolicy[0];
    
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


    /**
     * Returns <code>true</code> if an item exists at <code>absPath</code> and
     * this <code>Session</code> has read access to it; otherwise returns
     * <code>false</code>.
     *
     * Shortcut to standard itemExists method when a Path object is already available
     *
     * @param absPath An absolute path.
     * @return a <code>boolean</code>
     * @throws RepositoryException if <code>absPath</code> is not a well-formed
     *                             absolute path.
     */
    public boolean itemExists(final Path absPath) throws RepositoryException {
        return context.getSessionState().perform(new SessionOperation<Boolean>() {
            @Override @SuppressWarnings("deprecation")
            public Boolean perform(SessionContext context) throws RepositoryException {
                return context.getItemManager().itemExists(absPath);
            }
        });
    }

    /**
     * Returns the node at the specified absolute path in the workspace. If no
     * such node exists, then it returns the property at the specified path.
     * <p>
     * This method should only be used if the application does not know whether
     * the item at the indicated path is property or node. In cases where the
     * application has this information, either {@link #getNode} or {@link
     * #getProperty} should be used, as appropriate. In many repository
     * implementations the node and property-specific methods are likely to be
     * more efficient than <code>getItem</code>.
     *
     * Shortcut to standard getItem method when a Path object is already available
     *
     * @param absPath An absolute path.
     * @return the specified <code>Item</code>.
     * @throws PathNotFoundException if no accessible item is found at the
     *                               specified path.
     * @throws RepositoryException   if another error occurs.
     */
    public Item getItem(final Path absPath) throws RepositoryException {
        return context.getSessionState().perform(new SessionOperation<Item>() {
            @Override @SuppressWarnings("deprecation")
            public Item perform(SessionContext context) throws RepositoryException {
                return context.getItemManager().getItem(absPath);
            }
        });
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
