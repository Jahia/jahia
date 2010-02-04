/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.usermanager.jcr;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.*;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerProvider;
import org.jahia.utils.Base64;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * JCR implementation of the user manager provider interface. This class stores and
 * retrieves users from a JCR backend.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 7 juil. 2009
 */
public class JCRUserManagerProvider extends JahiaUserManagerProvider {
    private transient static Logger logger = Logger.getLogger(JCRUserManagerProvider.class);
    private transient JCRTemplate jcrTemplate;
    private transient JCRPublicationService publicationService;
    private static JCRUserManagerProvider mUserManagerService;
    private transient CacheService cacheService;
    private transient Cache cache;
    private static transient Map<String, String> mappingOfProperties;

    static {
        mappingOfProperties = new HashMap<String, String>();
        mappingOfProperties.put("lastname", "j:lastName");
        mappingOfProperties.put("firstname", "j:firstName");
        mappingOfProperties.put("organization", "j:organization");
    }

    /**
     * Create an new instance of the User Manager Service if the instance do not
     * exist, or return the existing instance.
     *
     * @return Return the instance of the User Manager Service.
     */
    public static JCRUserManagerProvider getInstance() {
        if (mUserManagerService == null) {
            mUserManagerService = new JCRUserManagerProvider();
        }
        return mUserManagerService;
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * This is the method that creates a new user in the system, with all the
     * specified attributes.
     *
     * @param password User password
     */
    public JahiaUser createUser(final String name, final String password, final Properties properties) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaUser>() {
                public JahiaUser doInJCR(JCRSessionWrapper jcrSessionWrapper) throws RepositoryException {
                    JCRNodeWrapper parentNodeWrapper = jcrSessionWrapper.getNode( "/users");

                    jcrSessionWrapper.getWorkspace().getVersionManager().checkout(parentNodeWrapper.getPath());
                    Node userNode = parentNodeWrapper.addNode(name, Constants.JAHIANT_USER);
                    if (parentNodeWrapper.hasProperty("j:usersFolderSkeleton")) {
                        try {
                            jcrSessionWrapper.importXML(parentNodeWrapper.getPath() + "/" + name, new FileInputStream(
                                    org.jahia.settings.SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/" + parentNodeWrapper.getProperty(
                                            "j:usersFolderSkeleton").getString()),
                                                      ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, true);
                        } catch (IOException e) {
                            throw new RepositoryException("Could not create user due to some import issues", e);
                        }
                        JCRNodeWrapperImpl.changePermissions(userNode, "u:" + name, "rw");
                    } else {
                        JCRNodeWrapperImpl.changePermissions(userNode, "u:" + name, "rw");
                    }
                    String l_password;
                    if (!password.startsWith("SHA-1:")) {
                        // Encrypt the password
                        l_password = encryptPassword(password);
                    } else {
                        l_password = password.substring(6);
                    }

                    userNode.setProperty(JCRUser.J_PASSWORD, l_password);
                    userNode.setProperty(JCRUser.J_EXTERNAL, false);
                    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                        String key = (String) entry.getKey();
                        if (mappingOfProperties.containsKey(key)) {
                            key = mappingOfProperties.get(key);
                        }
                        userNode.setProperty(key, (String) entry.getValue());
                    }
                    jcrSessionWrapper.save();
                    jcrSessionWrapper.getWorkspace().getVersionManager().checkin(parentNodeWrapper.getPath());
                    publicationService.publish(userNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null,
                            true, true);
                    return new JCRUser(userNode.getIdentifier(), jcrTemplate);
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while creating user " + name, e);
            return null;
        }
    }

    /**
     * This method removes a user from the system. All the user's attributes are
     * remove, and also all the related objects belonging to the user. On success,
     * true is returned and the user parameter is not longer valid. Return false
     * on any failure.
     *
     * @param user reference on the user to be deleted.
     */
    public boolean deleteUser(JahiaUser user) {
        if (user instanceof JCRUser) {
            final JCRUser jcrUser = (JCRUser) user;
            try {
                return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        Node node = session.getNodeByUUID(jcrUser.getNodeUuid());
                        session.checkout(node.getParent());
                        session.checkout(node);                        
                        node.remove();
                        session.save();
                        return true;
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Error while deleting user", e);
                return false;
            }
        }
        return false;
    }

    /**
     * Return the number of user in the system.
     *
     * @return Return the number of users in the system.
     */
    public int getNbUsers() {
        // TODO to be implemented or removed.
        return 0;
    }

    /**
     * This method return all users' keys in the system.
     *
     * @return Return a List of strings holding the user identification key .
     */
    public List<String> getUserList() {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<List<String>>() {
                public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    List<String> users = new ArrayList<String>();
                    if (session.getWorkspace().getQueryManager() != null) {
                        String query = "SELECT u.[j:nodename] FROM [" + Constants.JAHIANT_USER + "] as u ORDER BY u.[j:nodename]";
                        Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                        QueryResult qr = q.execute();
                        RowIterator rows = qr.getRows();
                        while (rows.hasNext()) {
                            Row usersFolderNode = rows.nextRow();
                            String userName = "{jcr}" + usersFolderNode.getValue("j:nodename").getString();
                            if (!users.contains(userName)) {
                                users.add(userName);
                            }
                        }
                    }
                    return users;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while retrieving user list", e);
            return new ArrayList<String>();
        }
    }

    /**
     * This method returns the list of all the user names registed into the system.
     *
     * @return Return a List of strings holding the user identification names.
     */
    public List<String> getUsernameList() {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<List<String>>() {
                public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    List<String> users = new ArrayList<String>();
                    if (session.getWorkspace().getQueryManager() != null) {
                        String query = "SELECT u.[j:nodename] FROM [" + Constants.JAHIANT_USER + "] as u ORDER BY u.[j:nodename]";
                        Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                        QueryResult qr = q.execute();
                        RowIterator rows = qr.getRows();
                        while (rows.hasNext()) {
                            Row usersFolderNode = rows.nextRow();
                            String userName = usersFolderNode.getValue("j:nodename").getString();
                            if (!users.contains(userName)) {
                                users.add(userName);
                            }
                        }
                    }
                    return users;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while retrieving user name list", e);
            return new ArrayList<String>();
        }
    }

    /**
     * Performs a login of the specified user.
     *
     * @param userKey      the user identifier defined in this service properties
     * @param userPassword the password of the user
     * @return boolean true if the login succeeded, false otherwise
     */
    public boolean login(String userKey, String userPassword) {
        JahiaUser jahiaUser = lookupUser(userKey);
        return jahiaUser.verifyPassword(userPassword);
    }

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created jahiaUser object.
     */
    public JahiaUser lookupUserByKey(String userKey) {
        try {
            final String name = userKey.split("}")[1];
            if (cache == null) {
                start();
            }
            if (cache.containsKey(name)) {
                return (JahiaUser) cache.get(name);
            }
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaUser>() {
                public JahiaUser doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node usersFolderNode = session.getNode( "/users/" + name);
                    if (!usersFolderNode.getProperty(JCRUser.J_EXTERNAL).getBoolean()) {
                        JCRUser user = new JCRUser(usersFolderNode.getIdentifier(), jcrTemplate);
                        cache.put(name, user);
                        return user;
                    }
                    return null;
                }
            });
        } catch (PathNotFoundException pnfe) {
            // This is expected in the case the user doesn't exist in the repository. We will simply return null.
        } catch (JahiaInitializationException e) {
            logger.error("Error while looking up user by key " + userKey, e);
        } catch (RepositoryException e) {
            logger.error("Error while looking up user by key " + userKey, e);
        }
        return null;
    }

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created jahiaUser object.
     */
    public JahiaUser lookupUser(final String name) {
        try {
            if (cache == null) {
                start();
            }
            if (cache.containsKey(name)) {
                return (JahiaUser) cache.get(name);
            }
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaUser>() {
                public JahiaUser doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node usersFolderNode = session.getNode("/users/" + name.trim());
                    if (!usersFolderNode.getProperty(JCRUser.J_EXTERNAL).getBoolean()) {
                        JCRUser user = new JCRUser(usersFolderNode.getIdentifier(), jcrTemplate);
                        cache.put(name, user);
                        return user;
                    }
                    return null;
                }
            });
        } catch (PathNotFoundException pnfe) {
            // This is expected in the case the user doesn't exist in the repository. We will simply return null.
        } catch (JahiaInitializationException e) {
            logger.error("Error while looking up user by name " + name, e);
        } catch (RepositoryException e) {
            logger.error("Error while looking up user by name " + name, e);
        }
        return null;
    }

    public JahiaUser lookupExternalUser(final String name) {
        try {
            if (cache == null) {
                start();
            }
            if (cache.containsKey(name)) {
                return (JahiaUser) cache.get(name);
            }
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaUser>() {
                public JahiaUser doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node usersFolderNode = session.getNode( "/users/" + name.trim());
                    if (usersFolderNode.getProperty(JCRUser.J_EXTERNAL).getBoolean()) {
                        JCRUser user = new JCRUser(usersFolderNode.getIdentifier(), jcrTemplate);
                        cache.put(name, user);
                        return user;
                    }
                    return null;
                }
            });
        } catch (PathNotFoundException pnfe) {
            // This is expected in the case the user doesn't exist in the repository. We will simply return null.
        } catch (JahiaInitializationException e) {
            logger.error("Error while looking up external user by name " + name, e);
        } catch (RepositoryException e) {
            logger.error("Error while looking up external user by name " + name, e);
        }
        return null;
    }


    public JahiaUser lookupUserByKey(String name, String searchAttributeName) {
        // TODO this should be removed, implemented or throw an unsupported exception
        return null;
    }

    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criterias
     * @return Set a set of JahiaUser elements that correspond to those
     *         search criterias
     */
    public Set<JahiaUser> searchUsers(int siteID, final Properties searchCriterias) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Set<JahiaUser>>() {
                public Set<JahiaUser> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Set<JahiaUser> users = new HashSet<JahiaUser>();
                    if (session.getWorkspace().getQueryManager() != null) {
                        StringBuffer query = new StringBuffer("SELECT * FROM [" + Constants.JAHIANT_USER + "] as u");
                        if (searchCriterias != null && searchCriterias.size() > 0) {
                            // Avoid wildcard attribute
                            if (!(searchCriterias.containsKey(
                                    "*") && searchCriterias.size() == 1 && searchCriterias.getProperty("*").equals(
                                    "*"))) {
                                query.append(" WHERE u.[" + JCRUser.J_EXTERNAL + "] = 'false' ");
                                Iterator<Map.Entry<Object, Object>> objectIterator = searchCriterias.entrySet().iterator();
                                if (objectIterator.hasNext()) {
                                    query.append(" AND ");
                                }
                                while (objectIterator.hasNext()) {
                                    Map.Entry<Object, Object> entry = objectIterator.next();
                                    String propertyKey = (String) entry.getKey();
                                    if ("username".equals(propertyKey)) {
                                        propertyKey = "j:nodename";
                                    }
                                    String propertyValue = (String) entry.getValue();
                                    if ("*".equals(propertyValue)) {
                                        propertyValue = "%";
                                    } else {
                                        if (propertyValue.contains("*")) {
                                            propertyValue = propertyValue.replaceAll("\\*", "%");
                                        } else {
                                            propertyValue = propertyValue + "%";
                                        }
                                    }
                                    if ("*".equals(propertyKey)) {
                                        query.append("CONTAINS(u.*,'" + propertyValue.replaceAll("%", "") + "')");
                                    } else {
                                        query.append("u.[" + propertyKey + "]").append(
                                                " LIKE '").append(propertyValue).append("'");
                                    }
                                    if (objectIterator.hasNext()) {
                                        query.append(" OR ");
                                    }
                                }
                            }
                        }
                        query.append(" ORDER BY u.[j:nodename]");
                        if (logger.isDebugEnabled()) {
                            logger.debug(query);
                        }
                        Query q = session.getWorkspace().getQueryManager().createQuery(query.toString(),
                                                                                       Query.JCR_SQL2);
                        QueryResult qr = q.execute();
                        NodeIterator ni = qr.getNodes();
                        while (ni.hasNext()) {
                            Node usersFolderNode = ni.nextNode();
                            users.add(new JCRUser(usersFolderNode.getIdentifier(), jcrTemplate));
                        }
                    }

                    return users;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while searching for users", e);
            return new HashSet<JahiaUser>();
        }
    }

    /**
     * This method indicates that any internal cache for a provider should be
     * updated because the value has changed and needs to be transmitted to the
     * other nodes in a clustering environment.
     *
     * @param jahiaUser JahiaUser the user to be updated in the cache.
     */
    public void updateCache(JahiaUser jahiaUser) {
        cache.remove(jahiaUser.getName());
    }

    /**
     * This function checks into the system if the username has already been
     * assigned to another user.
     *
     * @return Return true if the specified username has not been assigned yet,
     *         return false on any failure.
     */
    public boolean userExists(final String name) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    if (session.getWorkspace().getQueryManager() != null) {
                        String query = "SELECT * FROM [" + Constants.JAHIANT_USER + "] as u WHERE u.[j:nodename] = '" + name + "'";
                        Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                        QueryResult qr = q.execute();
                        NodeIterator ni = qr.getNodes();
                        return ni.hasNext();
                    }
                    return false;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error when testing user existence", e);
            return false;
        }
    }

    public void start() throws JahiaInitializationException {
        if (cacheService != null) {
            cache = cacheService.createCacheInstance("JCR_USER_CACHE");
        }
    }

    public void stop() throws JahiaException {
    }

    public static String encryptPassword(String password) {
        if (password == null) {
            return null;
        }

        if (password.length() == 0) {
            return null;
        }

        String result = null;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            if (md != null) {
                md.reset();
                md.update(password.getBytes());
                result = new String(Base64.encode(md.digest()));
            }
            md = null;
        } catch (NoSuchAlgorithmException ex) {
            result = null;
        }

        return result;
    }
}
