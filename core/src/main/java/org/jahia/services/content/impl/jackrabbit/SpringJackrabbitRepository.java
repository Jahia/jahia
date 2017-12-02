/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.impl.jackrabbit;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.commons.AbstractRepository;
import org.apache.jackrabbit.core.JahiaRepositoryImpl;
import org.apache.jackrabbit.core.cluster.ClusterException;
import org.apache.jackrabbit.core.cluster.ClusterNode;
import org.apache.jackrabbit.core.cluster.JahiaClusterNode;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.jahia.settings.SettingsBean;
import org.jahia.settings.readonlymode.ReadOnlyModeCapable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;

import javax.jcr.*;
import javax.servlet.ServletContext;

import java.io.File;
import java.io.IOException;

/**
 * Spring-configured Jackrabbit repository.
 *
 * User: toto
 * Date: Feb 9, 2009
 * Time: 11:21:20 AM
 */
public class SpringJackrabbitRepository extends AbstractRepository implements JackrabbitRepository, ServletContextAware, ApplicationContextAware, ReadOnlyModeCapable {

    private static class Holder {
        static final SpringJackrabbitRepository INSTANCE = new SpringJackrabbitRepository();
    }

    private static final Logger logger = LoggerFactory.getLogger(SpringJackrabbitRepository.class);

    /**
     * Returns a singleton instance of this class.
     *
     * @return a singleton instance of this class
     */
    public static SpringJackrabbitRepository getInstance() {
        return Holder.INSTANCE;
    }

    private JackrabbitRepository repository;

    private Resource configFile;
    private Resource homeDir;

    private String servletContextAttributeName;
    private ServletContext servletContext;

    private boolean useDataStore;

    private ApplicationContext applicationContext;

    private String dataStoreGarbageCollectorBeanId;

    private SettingsBean settings;

    private boolean performMigrationToDataStoreIfNeeded = true;

    private long timeoutSwitchingToReadOnlyMode;

    public Resource getConfigFile() {
        return configFile;
    }

    public void setConfigFile(Resource configFile) {
        this.configFile = configFile;
    }

    public Resource getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(Resource homeDir) {
        this.homeDir = homeDir;
    }

    public String getServletContextAttributeName() {
        return servletContextAttributeName;
    }

    public void setServletContextAttributeName(String servletContextAttributeName) {
        this.servletContextAttributeName = servletContextAttributeName;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Creates a repository instance based on the contained JNDI reference.
     * Can be overridden by subclasses to return different repositories.
     * A subclass can access the JNDI reference through the
     *
     * @return repository instance
     * @throws RepositoryException if the repository could not be created
     */
    protected JackrabbitRepository createRepository()
            throws RepositoryException, IOException {
        RepositoryConfig config = RepositoryConfig.create(configFile.getFile().toString(), homeDir.getFile().toString());
        JahiaRepositoryConfig jahiaConfig = new JahiaRepositoryConfig(config);
        useDataStore = jahiaConfig.getDataStore() != null;
        try {
            return JahiaRepositoryImpl.create(jahiaConfig);
        } catch (RepositoryException e) {
            if (e.getMessage().startsWith("internal error: failed to read custom node type definitions stored in ")) {
                throw new RepositoryException(e.getMessage() +
                        "\nPlease refer to the following Jahia Knowledge Base entry for more details on the issue and how to fix it : https://jira.jahia.org/browse/JKB-29",
                        e.getCause());
            } else {
                throw e;
            }
        }
    }


    public void start() throws RepositoryException, IOException {
        if (homeDir == null) {
            homeDir = settings.getRepositoryHomeResource();
        }
        String targetRepositoryConfig = System
                .getProperty("jahia.jackrabbit.targetRepositoryConfig");
        if (settings.isProcessingServer()
                && (performMigrationToDataStoreIfNeeded || targetRepositoryConfig != null)) {
            new RepositoryMigrator(configFile.getFile(), homeDir.getFile(),
                    targetRepositoryConfig != null ? new File(targetRepositoryConfig) : null,
                    performMigrationToDataStoreIfNeeded).migrate();
        }

        repository = createRepository();

        if ((servletContextAttributeName != null) &&
            (servletContext != null)) {
            servletContext.setAttribute(servletContextAttributeName, this);
        }

        if (settings.isProcessingServer() && useDataStore && dataStoreGarbageCollectorBeanId != null && dataStoreGarbageCollectorBeanId.length() > 0) {
            // this schedules the background job
            applicationContext.getBean(dataStoreGarbageCollectorBeanId);
        }
    }

    public void stop() {
        shutdown();
    }

    //-----------------------------------------------------------< Repository >

    /**
     * Delegated to the underlying repository instance.
     * {@inheritDoc}
     */
    @Override
    public Session login(Credentials credentials, String workspaceName)
            throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return repository.login(credentials, workspaceName);
    }

    /**
     * Delegated to the underlying repository instance.
     * {@inheritDoc}
     */
    @Override
    public String getDescriptor(String key) {
        return repository.getDescriptor(key);
    }

    /**
     * Delegated to the underlying repository instance.
     * {@inheritDoc}
     */
    @Override
    public String[] getDescriptorKeys() {
        return repository.getDescriptorKeys();
    }

    @Override
    public boolean isSingleValueDescriptor(String key) {
        return repository.isSingleValueDescriptor(key);
    }

    @Override
    public Value getDescriptorValue(String key) {
        return repository.getDescriptorValue(key);
    }

    @Override
    public Value[] getDescriptorValues(String key) {
        return repository.getDescriptorValues(key);
    }

    public JackrabbitRepository getRepository() {
        return repository;
    }

    /**
     * Delegated to the underlying repository instance.
     */
    @Override
    public void shutdown() {
        repository.shutdown();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setDataStoreGarbageCollectorBeanId(String dataStoreGarbageCollectorBeanId) {
        this.dataStoreGarbageCollectorBeanId = dataStoreGarbageCollectorBeanId;
    }

    public void setSettings(SettingsBean settings) {
        this.settings = settings;
    }

    public void setPerformMigrationToDataStoreIfNeeded(boolean performMigrationToDataStoreIfNeeded) {
        this.performMigrationToDataStoreIfNeeded = performMigrationToDataStoreIfNeeded;
    }

    /**
     * Jackrabbit cluster node instance or <code>null</code> if clustering is not activated.
     *
     * @return Jackrabbit cluster node instance or <code>null</code> if clustering is not activated
     */
    public ClusterNode getClusterNode() {
        return settings.isClusterActivated() ? ((JahiaRepositoryImpl) getRepository()).getContext().getClusterNode()
                : null;
    }

    /**
     * Returns current Jackrabbit cluster node revision or <code>0</code> if clustering is not activated.
     *
     * @return current Jackrabbit cluster node revision or <code>0</code> if clustering is not activated
     */
    public long getClusterRevision() {
        ClusterNode clusterNode = getClusterNode();
        return clusterNode != null ? clusterNode.getRevision() : 0;
    }

    /**
     * Performs the sync on the Jackrabbit cluster node. If clustering is not activated this method does nothing. Possible exceptions during
     * cluster sync are logged by not propagated upper by this method.
     */
    public void syncClusterNode() {
        ClusterNode clusterNode = getClusterNode();
        if (clusterNode != null) {
            try {
                clusterNode.sync();
            } catch (ClusterException e) {
                logger.error("Error ocurred synchronizing cluster", e);
            }
        }
    }

    @Override
    public void switchReadOnlyMode(boolean enable) {
        ClusterNode clusterNode = getClusterNode();
        if (clusterNode == null) {
            return;
        }
        ((JahiaClusterNode) clusterNode).setReadOnlyMode(enable, timeoutSwitchingToReadOnlyMode);
    }

    @Override
    public int getReadOnlyModePriority() {
        return 100;
    }

    public void setTimeoutSwitchingToReadOnlyMode(long timeoutSwitchingToReadOnlyMode) {
        this.timeoutSwitchingToReadOnlyMode = timeoutSwitchingToReadOnlyMode;
    }
}
