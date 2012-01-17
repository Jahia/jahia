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
import java.util.Collection;

/**
 * User: toto
 * Date: 3/30/11
 * Time: 18:01
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

    @Override
    public WorkspaceConfig createWorkspaceConfig(String name, StringBuffer configContent) throws ConfigurationException {
        return config.createWorkspaceConfig(name, configContent);
    }

    @Override
    public WorkspaceConfig createWorkspaceConfig(String name, InputSource template) throws ConfigurationException {
        return config.createWorkspaceConfig(name, template);
    }
}
