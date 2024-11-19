/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
@SuppressWarnings("squid:RedundantThrowsDeclarationCheck")
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
    public JahiaSystemSession(RepositoryContext repositoryContext, Subject subject, WorkspaceConfig wspConfig) throws RepositoryException {
        super(repositoryContext, subject, wspConfig);
    }

    /**
     * Always returns the name of the <code>SystemPrincipal</code>.
     *
     * @return the name of <code>SystemPrincipal</code>.
     */
    @Override
    protected final String retrieveUserId(Subject subject, String workspaceName) throws RepositoryException {
        return SYSTEM_PRINCIPAL_NAME;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden in order to create custom access manager
     *
     * @return access manager for system session
     * @throws javax.jcr.AccessDeniedException is never thrown
     * @throws javax.jcr.RepositoryException is never thrown
     */
    @Override
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
        @Override
        protected void checkInitialized() throws IllegalStateException {
            // nop
        }

        /**
         * @see org.apache.jackrabbit.core.security.AbstractAccessControlManager#checkPermission(String,int)
         */
        @Override
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
        @Override
        public boolean hasPrivileges(String absPath, Privilege[] privileges)
                throws PathNotFoundException, RepositoryException {
            checkValidNodePath(absPath);
            // allow everything
            return true;
        }

        /**
         * @see javax.jcr.security.AccessControlManager#getPrivileges(String)
         */
        @Override
        public Privilege[] getPrivileges(String absPath) throws PathNotFoundException, RepositoryException {
            checkValidNodePath(absPath);
            return new Privilege[] { privilegeFromName(Privilege.JCR_ALL) };
        }

        /**
         * @see javax.jcr.security.AccessControlManager#getEffectivePolicies(String)
         */
        @Override
        public AccessControlPolicy[] getEffectivePolicies(String absPath) throws PathNotFoundException, AccessDeniedException, RepositoryException {
            // cannot determine the effective policies for the system session.
            return ACCESS_CONTROL_POLICIES;
        }

        @Override
        public AccessControlPolicy[] getEffectivePolicies(Set<Principal> principals) throws AccessDeniedException, AccessControlException, UnsupportedRepositoryOperationException, RepositoryException {
            return ACCESS_CONTROL_POLICIES;
        }

        /**
         * @see org.apache.jackrabbit.api.security.JackrabbitAccessControlManager#hasPrivileges(String, java.util.Set, javax.jcr.security.Privilege[])
         */
        @Override
        public boolean hasPrivileges(String absPath, Set<Principal> principals, Privilege[] privileges) throws PathNotFoundException, RepositoryException {
            throw new UnsupportedOperationException("not implemented");
        }

        /**
         * @see org.apache.jackrabbit.api.security.JackrabbitAccessControlManager#getPrivileges(String, java.util.Set)
         */
        @Override
        public Privilege[] getPrivileges(String absPath, Set<Principal> principals) throws PathNotFoundException, RepositoryException {
            throw new UnsupportedOperationException("not implemented");
        }
    }

}
