package org.jahia.ajax.gwt.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:47:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContentHubHelper {
    private static Logger logger = Logger.getLogger(ContentHubHelper.class);

    private JCRSessionFactory sessionFactory;

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void mount(String name, String root, JahiaUser user) throws GWTJahiaServiceException {
        if (user.isAdminMember(0)) {
            JCRSessionWrapper session = null;
            try {
                session = sessionFactory.getSystemSession(user.getName());
                JCRNodeWrapper parent = session.getNode("/content");
                JCRNodeWrapper mounts;
                try {
                    mounts = parent.getNode("mounts");
                } catch (PathNotFoundException nfe) {
                    try {
                        mounts = parent.addNode("mounts", "jnt:systemFolder");
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                        throw new GWTJahiaServiceException("Could not create 'mounts' folder");
                    }
                }

                JCRMountPointNode childNode = null;
                if (!mounts.isFile()) {
                    try {
                        childNode = (JCRMountPointNode) mounts.addNode(name, "jnt:vfsMountPoint");
                        childNode.setProperty("j:root", root);

                        boolean valid = childNode.checkValidity();
                        if (!valid) {
                            childNode.remove();
                            throw new GWTJahiaServiceException("Invalid path");
                        }
                    } catch (RepositoryException e) {
                        logger.error("Exception", e);
                        throw new GWTJahiaServiceException("A system error happened");
                    }
                    try {
                        parent.save();
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                        throw new GWTJahiaServiceException("A system error happened");
                    }
                }
                if (childNode == null) {
                    throw new GWTJahiaServiceException("A system error happened");
                }
            } catch (RepositoryException e) {
                logger.error("A system error happened", e);
                throw new GWTJahiaServiceException("A system error happened");
            } finally {
                if (session != null) {
                    session.logout();
                }
            }
        } else {
            throw new GWTJahiaServiceException("Only root can mount folders");
        }
    }

    public Map<String, String> getStoredPasswordsProviders(JahiaUser user) {
        Map<String, String> results = new HashMap<String, String>();
        results.put(null, user.getUsername());
        for (JCRStoreProvider provider : sessionFactory.getProviders().values()) {
            if ("storedPasswords".equals(provider.getAuthenticationType())) {
                results.put(provider.getKey(), user.getProperty("storedUsername_" + provider.getKey()));
            }
        }
        return results;
    }

    public void storePasswordForProvider(JahiaUser user, String providerKey, String username, String password) {
        if (username == null) {
            user.removeProperty("storedUsername_" + providerKey);
        } else {
            user.setProperty("storedUsername_" + providerKey, username);
        }
        if (password == null) {
            user.removeProperty("storedPassword_" + providerKey);
        } else {
            user.setProperty("storedPassword_" + providerKey, password);
        }
    }
}
