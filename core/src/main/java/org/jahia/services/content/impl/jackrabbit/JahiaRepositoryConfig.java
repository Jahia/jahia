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

import org.apache.jackrabbit.core.config.*;
import org.apache.jackrabbit.core.data.DataStore;
import org.apache.jackrabbit.core.fs.FileSystem;
import org.apache.jackrabbit.core.query.QueryHandler;
import org.apache.jackrabbit.core.query.QueryHandlerContext;
import org.apache.jackrabbit.core.util.RepositoryLockMechanism;
import org.apache.jackrabbit.core.util.db.ConnectionFactory;
import org.xml.sax.InputSource;

import javax.jcr.RepositoryException;

import static org.apache.jackrabbit.core.config.RepositoryConfigurationParser.REPOSITORY_HOME_VARIABLE;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Properties;

/**
 * DX specific Jackrabbit repository configuration handler.
 *
 * @author toto
 */
public class JahiaRepositoryConfig extends RepositoryConfig {
    private RepositoryConfig config;

    /**
     * Factory method to instantiate and initialize repository configuration. Uses custom instance of {@link RepositoryConfigurationParser}
     * for parsing the configuration file that can handle nested value placeholders.
     *
     * @param file repository configuration file name
     * @param home repository home directory
     * @return repository configuration
     * @throws ConfigurationException on configuration errors
     */
    public static JahiaRepositoryConfig create(String file, String home) throws ConfigurationException {
        URI uri = new File(file).toURI();
        Properties variables = new Properties(System.getProperties());
        variables.setProperty(REPOSITORY_HOME_VARIABLE, home);

        JahiaRepositoryConfigurationParser parser = new JahiaRepositoryConfigurationParser(variables);

        RepositoryConfig config = parser.parseRepositoryConfig(new InputSource(uri.toString()));
        config.init();

        return new JahiaRepositoryConfig(config);
    }

    public JahiaRepositoryConfig(RepositoryConfig config) {
        super(null,null,null,null,null,null,0,null,null,null,null,null,null,null,null,null);
        this.config = config;
    }

    @Override
    public String getHomeDir() {
        return config.getHomeDir();
    }

    @Override
    public FileSystem getFileSystem() throws RepositoryException {
        return config.getFileSystem();
    }

    @Deprecated(since = "8.0.0.0")
    @Override
    @SuppressWarnings("squid:MissingDeprecatedCheck")
    public String getAppName() {
        return config.getAppName();
    }

    @Deprecated(since = "8.0.0.0")
    @Override
    @SuppressWarnings("squid:MissingDeprecatedCheck")
    public AccessManagerConfig getAccessManagerConfig() {
        return config.getAccessManagerConfig();
    }

    @Deprecated(since = "8.0.0.0")
    @Override
    @SuppressWarnings("squid:MissingDeprecatedCheck")
    public LoginModuleConfig getLoginModuleConfig() {
        return config.getLoginModuleConfig();
    }

    @Override
    public SecurityConfig getSecurityConfig() {
        return config.getSecurityConfig();
    }

    @Override
    public String getWorkspacesConfigRootDir() {
        return config.getWorkspacesConfigRootDir();
    }

    @Override
    public String getDefaultWorkspaceName() {
        return config.getDefaultWorkspaceName();
    }

    @Override
    public int getWorkspaceMaxIdleTime() {
        return config.getWorkspaceMaxIdleTime();
    }

    @Override
    public Collection<WorkspaceConfig> getWorkspaceConfigs() {
        return config.getWorkspaceConfigs();
    }

    @Override
    public WorkspaceConfig getWorkspaceConfig(String name) {
        return config.getWorkspaceConfig(name);
    }

    @Override
    public VersioningConfig getVersioningConfig() {
        return config.getVersioningConfig();
    }

    @Override
    public boolean isSearchEnabled() {
        return config.isSearchEnabled();
    }

    @Override
    public QueryHandler getQueryHandler(QueryHandlerContext context) throws RepositoryException {
        return config.getQueryHandler(context);
    }

    @Override
    public ClusterConfig getClusterConfig() {
        if ("true".equals(System.getProperty("cluster.activated"))) {
            return config.getClusterConfig();
        } else {
            return null;
        }
    }

    @Override
    public ConnectionFactory getConnectionFactory() {
        return config.getConnectionFactory();
    }

    @Override
    public DataStore getDataStore() throws RepositoryException {
        return config.getDataStore();
    }

    @Override
    public RepositoryLockMechanism getRepositoryLockMechanism() throws RepositoryException {
        return config.getRepositoryLockMechanism();
    }

    @Override
    public WorkspaceConfig createWorkspaceConfig(String name, StringBuffer configContent) throws ConfigurationException {
        return config.createWorkspaceConfig(name, configContent);
    }

    @Override
    public WorkspaceConfig createWorkspaceConfig(String name, InputSource template) throws ConfigurationException {
        return config.createWorkspaceConfig(name, template);
    }

}
