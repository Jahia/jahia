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
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRNodeWrapperImpl;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerProvider;
import org.jahia.utils.Base64;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 7 juil. 2009
 */
public class JCRUserManagerProvider extends JahiaUserManagerProvider {
    private transient static Logger logger = Logger.getLogger(JCRUserManagerProvider.class);
    private transient JCRStoreService jcrStoreService;
    private static JCRUserManagerProvider mUserManagerService;


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

    public void setJcrStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }

    /**
     * This is the method that creates a new user in the system, with all the
     * specified attributes.
     *
     * @param password User password
     */
    public JahiaUser createUser(String name, String password, Properties properties) {
        try {
            JCRSessionWrapper jcrSessionWrapper = jcrStoreService.getSystemSession();
            JCRNodeWrapper parentNodeWrapper = jcrSessionWrapper.getNode("/" + Constants.CONTENT + "/users");
            Node userNode;
            if (parentNodeWrapper.hasProperty("j:usersFolderSkeleton")) {
                Session providerSession = jcrSessionWrapper.getProviderSession(jcrStoreService.getProvider(parentNodeWrapper.getPath()));
                providerSession.importXML(parentNodeWrapper.getPath(), new FileInputStream(org.jahia.settings.SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/" + parentNodeWrapper.getProperty("j:usersFolderSkeleton").getString()), ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
                providerSession.move(parentNodeWrapper.getPath() + "/user", parentNodeWrapper.getPath() + "/" + name);
                providerSession.save();
                userNode = parentNodeWrapper.getNode(name);
                JCRNodeWrapperImpl.changePermissions(userNode, "u:" + name, "rw");
            } else {
                userNode = parentNodeWrapper.addNode(name, Constants.JAHIANT_USER);
                JCRNodeWrapperImpl.changePermissions(userNode, "u:" + name, "rw");
            }
            userNode.setProperty(JCRUser.J_PASSWORD, encryptPassword(password));
            userNode.setProperty(JCRUser.J_EXTERNAL, false);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                userNode.setProperty((String) entry.getKey(), (String) entry.getValue());
            }
            jcrSessionWrapper.save();
            return new JCRUser(userNode.getUUID(), jcrStoreService);
        } catch (RepositoryException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        }
        return null;
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
            JCRUser jcrUser = (JCRUser) user;
            try {
                JCRSessionWrapper jcrSessionWrapper = jcrStoreService.getSystemSession();
                Node node = jcrSessionWrapper.getNodeByUUID(jcrUser.getNodeUuid());
                node.remove();
                jcrSessionWrapper.save();
                return true;
            } catch (RepositoryException e) {
                logger.error(e);
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
        return 0;
    }

    /**
     * This method return all users' keys in the system.
     *
     * @return Return a List of strings holding the user identification key .
     */
    public List<String> getUserList() {
        List<String> users = new ArrayList<String>();

        try {
            Session session = jcrStoreService.getSystemSession();
            if (session.getWorkspace().getQueryManager() != null) {
                String query = "SELECT j:nodename FROM " + Constants.JAHIANT_USER + " ORDER BY j:nodename";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.SQL);
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
        } catch (RepositoryException e) {
            logger.error(e);
        }
        return users;
    }

    /**
     * This method returns the list of all the user names registed into the system.
     *
     * @return Return a List of strings holding the user identification names.
     */
    public List<String> getUsernameList() {
        List<String> users = new ArrayList<String>();

        try {
            Session session = jcrStoreService.getSystemSession();
            if (session.getWorkspace().getQueryManager() != null) {
                String query = "SELECT j:nodename FROM " + Constants.JAHIANT_USER + " ORDER BY j:nodename";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.SQL);
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
        } catch (RepositoryException e) {
            logger.error(e);
        }
        return users;
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
            JCRSessionWrapper session = jcrStoreService.getSystemSession();
            Node usersFolderNode = session.getNode("/" + Constants.CONTENT + "/users/" + userKey.split("}")[1]);
            if(!usersFolderNode.getProperty(JCRUser.J_EXTERNAL).getBoolean())
            return new JCRUser(usersFolderNode.getUUID(), jcrStoreService);
        } catch (RepositoryException e) {
            logger.error(e);
        }
        return null;
    }

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created jahiaUser object.
     */
    public JahiaUser lookupUser(String name) {
        try {
            JCRSessionWrapper session = jcrStoreService.getSystemSession();
            Node usersFolderNode = session.getNode("/" + Constants.CONTENT + "/users/" + name.trim());
            if(!usersFolderNode.getProperty(JCRUser.J_EXTERNAL).getBoolean())
            return new JCRUser(usersFolderNode.getUUID(), jcrStoreService);
        } catch (PathNotFoundException e) {
            logger.debug(e);
        } catch (RepositoryException e) {
            logger.warn(e);
        }
        return null;
    }

    public JahiaUser lookupExternalUser(String name) {
        try {
            JCRSessionWrapper session = jcrStoreService.getSystemSession();
            Node usersFolderNode = session.getNode("/" + Constants.CONTENT + "/users/" + name.trim());
            if(usersFolderNode.getProperty(JCRUser.J_EXTERNAL).getBoolean())
            return new JCRUser(usersFolderNode.getUUID(), jcrStoreService);
        } catch (PathNotFoundException e) {
            logger.debug(e);
        } catch (RepositoryException e) {
            logger.warn(e);
        }
        return null;
    }


    public JahiaUser lookupUserByKey(String name, String searchAttributeName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
    public Set<JahiaUser> searchUsers(int siteID, Properties searchCriterias) {
        Set<JahiaUser> users = new HashSet<JahiaUser>();

        try {
            Session session = jcrStoreService.getSystemSession();
            if (session.getWorkspace().getQueryManager() != null) {
                StringBuffer query = new StringBuffer("SELECT * FROM " + Constants.JAHIANT_USER);
                if (searchCriterias != null && searchCriterias.size() > 0) {
                    // Avoid wildcard attribute
                    if (!(searchCriterias.containsKey("*") && searchCriterias.size() == 1 && searchCriterias.getProperty("*").equals("*"))) {
                        query.append(" WHERE ");
                        Iterator<Map.Entry<Object, Object>> objectIterator = searchCriterias.entrySet().iterator();
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
                                query.append("CONTAINS(*,'" + propertyValue + "')");
                            } else {
                                query.append(propertyKey.replaceAll("\\.", "___")).append(" LIKE '").append(propertyValue).append("'");
                            }
                            if (objectIterator.hasNext()) {
                                query.append(" AND ");
                            }
                        }
                    }
                }
                query.append(" ORDER BY j:nodename");
                if (logger.isDebugEnabled()) {
                    logger.debug(query);
                }
                Query q = session.getWorkspace().getQueryManager().createQuery(query.toString(), Query.SQL);
                QueryResult qr = q.execute();
                NodeIterator ni = qr.getNodes();
                while (ni.hasNext()) {
                    Node usersFolderNode = ni.nextNode();
                    users.add(new JCRUser(usersFolderNode.getUUID(), jcrStoreService));
                }
            }
        } catch (RepositoryException e) {
            logger.error(e);
        }
        return users;
    }

    /**
     * This method indicates that any internal cache for a provider should be
     * updated because the value has changed and needs to be transmitted to the
     * other nodes in a clustering environment.
     *
     * @param jahiaGroup JahiaGroup the group to be updated in the cache.
     */
    public void updateCache(JahiaUser jahiaUser) {
    }

    /**
     * This function checks into the system if the username has already been
     * assigned to another user.
     *
     * @return Return true if the specified username has not been assigned yet,
     *         return false on any failure.
     */
    public boolean userExists(String name) {
        try {
            Session session = jcrStoreService.getSystemSession();
            if (session.getWorkspace().getQueryManager() != null) {
                String query = "SELECT * FROM " + Constants.JAHIANT_USER + " WHERE j:nodename = '" + name + "'";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.SQL);
                QueryResult qr = q.execute();
                NodeIterator ni = qr.getNodes();
                return ni.hasNext();
            }
        } catch (RepositoryException e) {
            logger.error(e);
        }
        return false;
    }

    public void start() throws JahiaInitializationException {
    }

    public void stop() throws JahiaException {
    }

    public static String encryptPassword (String password) {
        if (password == null) {
            return null;
        }

        if (password.length () == 0) {
            return null;
        }

        String result = null;

        try {
            MessageDigest md = MessageDigest.getInstance ("SHA-1");
            if (md != null) {
                md.reset ();
                md.update (password.getBytes ());
                result = new String (Base64.encode (md.digest ()));
            }
            md = null;
        } catch (NoSuchAlgorithmException ex) {
            result = null;
        }

        return result;
    }
}
