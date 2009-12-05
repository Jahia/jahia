/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.impl.jackrabbit;

import org.apache.jackrabbit.commons.AbstractRepository;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.api.user.JahiaUserService;
import org.jahia.exceptions.JahiaException;
import org.jahia.jaas.JahiaLoginModule;
import org.springframework.web.context.ServletContextAware;
import org.springframework.core.io.Resource;

import javax.jcr.*;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 9, 2009
 * Time: 11:21:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpringJackrabbitRepository extends AbstractRepository implements JackrabbitRepository, ServletContextAware {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (SpringJackrabbitRepository.class);

    private JackrabbitRepository repository;

    private transient Thread hook;

    private Resource configFile;
    private Resource homeDir;
    private JahiaUserManagerService userService;
    private JahiaGroupManagerService groupService;
    private JahiaSitesService sitesService;


    private String servletContextAttributeName;
    private ServletContext servletContext;

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

    public JahiaUserManagerService getUserService() {
        return userService;
    }

    public void setUserService(JahiaUserManagerService userService) {
        this.userService = userService;
    }

    public JahiaGroupManagerService getGroupService() {
        return groupService;
    }

    public void setGroupService(JahiaGroupManagerService groupService) {
        this.groupService = groupService;
    }

    public JahiaSitesService getSitesService() {
        return sitesService;
    }

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
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
        return RepositoryImpl.create(config);
    }


    public void start() throws RepositoryException, IOException {

        repository = createRepository();

        JahiaAccessManager.setRepository(repository);
        SpringJahiaUserService service = new SpringJahiaUserService();
        JahiaAccessManager.setUserService(service);
        JahiaLoginModule.setUserService(service);

        if ((servletContextAttributeName != null) &&
            (servletContext != null)) {
            servletContext.setAttribute(servletContextAttributeName, this);
        }

        hook = new Thread() {
            public void run() {
                shutdown();
            }
        };
        Runtime.getRuntime().addShutdownHook(hook);
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

    class SpringJahiaUserService implements JahiaUserService {
        public boolean checkPassword(String username, String password) {
            JahiaUser user = userService.lookupUser(username);
            if (user != null) {
                return user.verifyPassword(password);
            }
            return false;
        }

        public boolean isServerAdmin(String username) {
            JahiaUser user = userService.lookupUser(username);
            if (user != null) {
                JahiaGroup admingroup = groupService.lookupGroup(0, JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
                return admingroup.isMember(user);
            }
            return false;
        }

        public boolean isAdmin(String username, String site) {
            JahiaSite s = null;
            try {
                s = sitesService.getSiteByKey(site);
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
            if (s == null ) {
                return false;
            }
            JahiaUser user = userService.lookupUser(username);
            if (user != null) {
                JahiaGroup admingroup = groupService.lookupGroup(s.getID(), JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
                return admingroup.isMember(user);
            }
            return false;
        }

        public boolean isUserMemberOf(String username, String groupname, String site) {
            JahiaSite s = null;
            if (JahiaGroupManagerService.GUEST_GROUPNAME.equals(groupname)) {
                return true;
            }
            if (JahiaGroupManagerService.USERS_GROUPNAME.equals(groupname) && site == null && !JahiaUserManagerService.GUEST_USERNAME.equals(username)) {
                return true;
            }
            int siteId = 0;
            try {
                s = sitesService.getSiteByKey(site);
                if (s != null) {
                    siteId = s.getID();
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
            JahiaUser user = userService.lookupUser(username);
            JahiaGroup group = groupService.lookupGroup(siteId, groupname);
            return (user != null) && (group != null) && group.isMember(user);
        }

        public List getUserMembership(String username) {
            return new ArrayList();
        }

        public List getGroupMembers(String groupname) {
            return new ArrayList();
        }
    }

}
