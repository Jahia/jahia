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
import org.jahia.api.user.JahiaUserService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaException;
import org.jahia.jaas.JahiaLoginModule;

import javax.jcr.*;
import javax.naming.Referenceable;
import javax.naming.Reference;
import java.io.Serializable;
import java.io.ObjectInputStream;
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
public class SpringJackrabbitRepository extends AbstractRepository implements JackrabbitRepository {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (SpringJackrabbitRepository.class);

    private JackrabbitRepository repository;

    private transient Thread hook;

    private String configFile;
    private String homeDir;
    private JahiaUserManagerService userService;

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public String getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(String homeDir) {
        this.homeDir = homeDir;
    }

    public JahiaUserManagerService getUserService() {
        return userService;
    }

    public void setUserService(JahiaUserManagerService userService) {
        this.userService = userService;
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
            throws RepositoryException {
        RepositoryConfig config = RepositoryConfig.create(configFile, homeDir);
        return RepositoryImpl.create(config);
    }


    public void start() throws RepositoryException {

        repository = createRepository();

        JahiaAccessManager.setRepository(repository);
        SpringJahiaUserService service = new SpringJahiaUserService();
        JahiaAccessManager.setUserService(service);
        JahiaLoginModule.setUserService(service);

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
                JahiaGroup admingroup = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(0, JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
                return admingroup.isMember(user);
            }
            return false;
        }

        public boolean isAdmin(String username, String site) {
            JahiaSite s = null;
            try {
                s = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(site);
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
            if (s == null ) {
                return false;
            }
            JahiaUser user = userService.lookupUser(username);
            if (user != null) {
                JahiaGroup admingroup = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(s.getID(), JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
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
                s = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(site);
                if (s != null) {
                    siteId = s.getID();
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
            JahiaUser user = userService.lookupUser(username);
            JahiaGroup group = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(siteId, groupname);
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
