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
package org.apache.jackrabbit.core;

import java.util.LinkedList;
import java.util.List;

import org.apache.jackrabbit.core.cluster.ClusterNode;
import org.apache.jackrabbit.core.cluster.JahiaClusterNode;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.query.lucene.JahiaSearchIndex;
import org.apache.jackrabbit.core.query.lucene.JahiaSearchIndex.ReindexJob;
import org.apache.jackrabbit.core.security.authentication.AuthContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(JahiaRepositoryImpl.class);
    
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
     * Schedules the re-indexing of the whole repository content.
     * 
     * @throws RepositoryException
     *             in case of a JCR-related error
     */
    public void scheduleReindexing() throws RepositoryException {
        List<JahiaSearchIndex> indexes = new LinkedList<>();
        JahiaSearchIndex index = (JahiaSearchIndex) getSystemSearchManager("default").getQueryHandler();
        if (index.prepareReindexing()) {
            indexes.add(index);
        }
        index = (JahiaSearchIndex) getWorkspaceInfo("default").getSearchManager().getQueryHandler();
        if (index.prepareReindexing()) {
            indexes.add(index);
        }
        index = (JahiaSearchIndex) getWorkspaceInfo("live").getSearchManager().getQueryHandler();
        if (index.prepareReindexing()) {
            indexes.add(index);
        }

        if (!indexes.isEmpty()) {

            JobDetail jobDetail = BackgroundJob.createJahiaJob("Re-indexing of the repository content",
                    ReindexJob.class);
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put("indexes", indexes);
            try {
                ServicesRegistry.getInstance().getSchedulerService().scheduleJobNow(jobDetail, true);
            } catch (SchedulerException e) {
                log.error("Unable to schedule background job for re-indexing", e);
            }
        }
    }

    /**
     * Schedules the re-indexing of the repository content for the specified workspace.
     * 
     * @param workspaceName
     *            the name of the workspace to be re-indexed
     * @throws RepositoryException
     *             in case of a JCR-related error
     */
    public void scheduleReindexing(String workspaceName) throws RepositoryException {
        JahiaSearchIndex index = (JahiaSearchIndex) (workspaceName == null ?
                getSystemSearchManager("default") :
                getWorkspaceInfo(workspaceName).getSearchManager()).getQueryHandler();

        index.scheduleReindexing();
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

    @Override
    protected NodeId getSystemExcludedNodeId() {
        return JahiaSearchIndex.SKIP_VERSION_INDEX ? RepositoryImpl.VERSION_STORAGE_NODE_ID : null;
    }
}
