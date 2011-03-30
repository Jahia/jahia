package org.jahia.services.content.impl.jackrabbit;

import org.apache.jackrabbit.core.config.*;
import org.apache.jackrabbit.core.data.DataStore;
import org.apache.jackrabbit.core.data.DataStoreFactory;
import org.apache.jackrabbit.core.fs.FileSystem;
import org.apache.jackrabbit.core.fs.FileSystemFactory;
import org.apache.jackrabbit.core.query.QueryHandler;
import org.apache.jackrabbit.core.query.QueryHandlerContext;
import org.apache.jackrabbit.core.query.QueryHandlerFactory;
import org.apache.jackrabbit.core.util.RepositoryLockMechanism;
import org.apache.jackrabbit.core.util.RepositoryLockMechanismFactory;
import org.apache.jackrabbit.core.util.db.ConnectionFactory;
import org.jahia.services.uicomponents.bean.contentmanager.Repository;
import org.jahia.settings.SettingsBean;
import org.w3c.dom.Element;

import javax.jcr.RepositoryException;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 3/30/11
 * Time: 18:01
 * To change this template use File | Settings | File Templates.
 */
public class JahiaRepositoryConfig extends RepositoryConfig {
    private RepositoryConfig config;

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

    @Override
    public String getAppName() {
        return config.getAppName();
    }

    @Override
    public AccessManagerConfig getAccessManagerConfig() {
        return config.getAccessManagerConfig();
    }

    @Override
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
}
