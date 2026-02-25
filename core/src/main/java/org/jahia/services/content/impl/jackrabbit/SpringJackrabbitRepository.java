/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.impl.jackrabbit;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.commons.AbstractRepository;
import org.apache.jackrabbit.core.JahiaRepositoryImpl;
import org.apache.jackrabbit.core.cluster.ClusterNode;
import org.apache.jackrabbit.core.cluster.JahiaClusterNode;
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
        JahiaRepositoryConfig jahiaConfig = JahiaRepositoryConfig.create(configFile.getFile().toString(), homeDir.getFile().toString());
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
     * Manual synchronization of the cluster has been deprecated and does not execute cluster.sync() anymore but log an error message instead.
     * If clustering is not activated, this method does nothing.
     *
     * @deprecated Manual cluster synchronization is no longer supported. Implement {@link org.apache.jackrabbit.core.journal.RecordConsumer} if you need to react to cluster sync events.
     */
    @Deprecated(since = "8.1.1.0", forRemoval = true)
    public void syncClusterNode() {
        ClusterNode clusterNode = getClusterNode();
        if (clusterNode != null) {
            logger.error("Manual synchronization of the cluster has been deprecated, and do not execute cluster.sync() anymore. Please implements a org.apache.jackrabbit.core.journal.RecordConsumer if you need to react on cluster sync.");
        }
    }

    @Override
    public void switchReadOnlyMode(boolean enable) {
        ClusterNode clusterNode = getClusterNode();
        if (clusterNode == null) {
            return;
        }
        ((JahiaClusterNode) clusterNode).setReadOnly(enable, timeoutSwitchingToReadOnlyMode);

        if (repository instanceof ReadOnlyModeCapable) {
            ((ReadOnlyModeCapable) repository).switchReadOnlyMode(enable);
        }
    }

    @Override
    public int getReadOnlyModePriority() {
        return 100;
    }

    /**
     * Set timeout waiting until switching to read only mode is possible.
     *
     * @param timeoutSwitchingToReadOnlyMode Timeout waiting until switching to read only mode is possible, ms.
     */
    public void setTimeoutSwitchingToReadOnlyMode(long timeoutSwitchingToReadOnlyMode) {
        this.timeoutSwitchingToReadOnlyMode = timeoutSwitchingToReadOnlyMode;
    }
}
