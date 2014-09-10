/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
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
 */
package org.jahia.services.content.impl.jackrabbit;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.commons.AbstractRepository;
import org.apache.jackrabbit.core.JahiaRepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.jahia.settings.SettingsBean;
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
public class SpringJackrabbitRepository extends AbstractRepository implements JackrabbitRepository, ServletContextAware, ApplicationContextAware {
    
    private JackrabbitRepository repository;

    private transient Thread hook;

    private Resource configFile;
    private Resource homeDir;

    private String servletContextAttributeName;
    private ServletContext servletContext;

    private boolean useDataStore;

    private ApplicationContext applicationContext;

    private String dataStoreGarbageCollectorBeanId;
    
    private SettingsBean settings;
    
    private boolean performMigrationToDataStoreIfNeeded = true;

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

        hook = new Thread("SpringJackrabbitRepository") {
            public void run() {
                shutdown();
            }
        };
        Runtime.getRuntime().addShutdownHook(hook);
        
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
    public Session login(Credentials credentials, String workspaceName)
            throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return repository.login(credentials, workspaceName);
    }

    /**
     * Delegated to the underlying repository instance.
     * {@inheritDoc}
     */
    public String getDescriptor(String key) {
        return repository.getDescriptor(key);
    }

    /**
     * Delegated to the underlying repository instance.
     * {@inheritDoc}
     */
    public String[] getDescriptorKeys() {
        return repository.getDescriptorKeys();
    }

    public boolean isSingleValueDescriptor(String key) {
        return repository.isSingleValueDescriptor(key);
    }

    public Value getDescriptorValue(String key) {
        return repository.getDescriptorValue(key);
    }

    public Value[] getDescriptorValues(String key) {
        return repository.getDescriptorValues(key);
    }

    public JackrabbitRepository getRepository() {
        return repository;
    }

    /**
     * Delegated to the underlying repository instance.
     */
    public void shutdown() {
        repository.shutdown();
        try {
            Runtime.getRuntime().removeShutdownHook(hook);
        } catch (IllegalStateException e) {
            // ignore. exception is thrown when hook itself calls shutdown
        }
    }

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

}
