/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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

package org.apache.jackrabbit.core;

import org.apache.jackrabbit.core.cluster.ClusterNode;
import org.apache.jackrabbit.core.cluster.JahiaClusterNode;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.security.authentication.AuthContext;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.security.auth.Subject;

/**
 * Jackrabbit repository extension
 * <p/>
 * Used to return session extension
 */
public class JahiaRepositoryImpl extends RepositoryImpl {
    protected class JahiaWorkspaceInfo extends WorkspaceInfo {
        /**
         * search manager (lazily instantiated)
         */
        private SearchManager searchMgr;        

        public JahiaWorkspaceInfo(WorkspaceConfig config) {
            super(config);
        }

        /**
         * Returns the search manager for this workspace.
         *
         * @return the search manager for this workspace, or <code>null</code>
         *         if no <code>SearchManager</code>
         * @throws RepositoryException if the search manager could not be created
         */
        protected SearchManager getSearchManager() throws RepositoryException {
            if (!isInitialized()) {
                throw new IllegalStateException("workspace '" + getName()
                        + "' not initialized");
            }

            synchronized (this) {
                if (searchMgr == null && getConfig().isSearchEnabled()) {
                    // search manager is lazily instantiated in order to avoid
                    // 'chicken & egg' bootstrap problems
                    searchMgr = new JahiaSearchManager(
                            getName(),
                            context,
                            getConfig(),
                            getItemStateProvider(), getPersistenceManager(),
                            context.getRootNodeId(),
                            getSystemSearchManager(getName()),
                            SYSTEM_ROOT_NODE_ID);
                }
                return searchMgr;
            }
        }
   
    }
    
    public JahiaRepositoryImpl(RepositoryConfig repConfig) throws RepositoryException {
        super(repConfig);
    }

    /**
     * Creates a new <code>RepositoryImpl</code> instance.
     * <p/>
     *
     * @param config the configuration of the repository
     * @return a new <code>RepositoryImpl</code> instance
     * @throws RepositoryException If an error occurs
     */
    public static RepositoryImpl create(RepositoryConfig config)
            throws RepositoryException {
        return new JahiaRepositoryImpl(config);
    }

    @Override
    protected SessionImpl createSessionInstance(AuthContext loginContext, WorkspaceConfig wspConfig) throws AccessDeniedException, RepositoryException {
        return new JahiaSessionImpl(context, loginContext, wspConfig);
    }

    @Override
    protected SessionImpl createSessionInstance(Subject subject, WorkspaceConfig wspConfig) throws AccessDeniedException, RepositoryException {
        return new JahiaSessionImpl(context, subject, wspConfig);
    }

    public RepositoryContext getContext() {
        return context;
    }

    protected ClusterNode createClusterNode() throws RepositoryException {
        try {
            ClusterNode clusterNode = new JahiaClusterNode();
            clusterNode.init(new ExternalEventListener());
            return clusterNode;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }
    
    /**
     * Creates a new {@link RepositoryImpl.WorkspaceInfo} instance for
     * <code>wspConfig</code>.
     *
     * @param wspConfig the workspace configuration.
     * @return a new <code>WorkspaceInfo</code> instance.
     */
    protected WorkspaceInfo createWorkspaceInfo(WorkspaceConfig wspConfig) {
        return new JahiaWorkspaceInfo(wspConfig);
    }    
}
