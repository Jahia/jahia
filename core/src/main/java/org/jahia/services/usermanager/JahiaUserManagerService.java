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
package org.jahia.services.usermanager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.JahiaService;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.utils.EncryptionUtils;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.servlet.ServletContext;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The user manager is responsible to manage all the users in the Jahia
 * environment.
 * The is the general interface that Jahia code uses to offer user management
 * services throughout the product (administration, login, ACL popups, etc..)
 */
public class JahiaUserManagerService extends JahiaService implements JahiaAfterInitializationService, ServletContextAware {
    
    private static Logger logger = LoggerFactory.getLogger(JahiaUserManagerService.class);
    private static final String ROOT_PWD_RESET_FILE = "root.pwd";
    private static final String ROOT_PWD_RESET_FILE_PATH = "/WEB-INF/etc/config/" + ROOT_PWD_RESET_FILE;

    /**
     * Guest user unique identification name.
     * Each usermanager should create this special user, who is assigned
     * automatically for each anonymous session internally in Jahia.
     */
    public static final String GUEST_USERNAME = Constants.GUEST_USERNAME;
    public static final String GUEST_USERPATH = "/users/" + Constants.GUEST_USERNAME;

    public static final String MULTI_CRITERIA_SEARCH_OPERATION = "multi_criteria_search_op";
    public static final String COUNT_LIMIT = "countLimit";

    private static class PatternHolder {
        static final Pattern INSTANCE = Pattern.compile(org.jahia.settings.SettingsBean.getInstance().lookupString("userManagementUserNamePattern"));
    }

    private static Pattern getUserNamePattern() {
        return PatternHolder.INSTANCE;
    }

    private JahiaUserSplittingRule userSplittingRule;
    private ServletContext servletContext;

    private String rootUserName;

    private Map<String, JahiaUserManagerProvider> legacyUserProviders = new HashMap<String, JahiaUserManagerProvider>();
    
    private UserPathCache userPathCache;

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final JahiaUserManagerService INSTANCE = new JahiaUserManagerService();
    }

    public static JahiaUserManagerService getInstance() {
        return Holder.INSTANCE;
    }

    public JahiaUserSplittingRule getUserSplittingRule() {
        return userSplittingRule;
    }

    public void setUserSplittingRule(JahiaUserSplittingRule userSplittingRule) {
        this.userSplittingRule = userSplittingRule;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void start() throws JahiaInitializationException {
        // do nothing
    }

    @Override
    public void stop() throws JahiaException {
        // do nothing
    }


    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created JCRUserNode object.
     * @deprecated use lookupUserByPath() instead
     */
    public JCRUserNode lookupUserByKey(String userKey) {
        return lookupUserByPath(userKey);
    }

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created JCRUserNode object.
     */
    public JCRUserNode lookupUserByPath(String path) {
        try {
            return lookupUserByPath(path, JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created JCRUserNode object.
     */
    public JCRUserNode lookupUserByPath(String userKey, JCRSessionWrapper session) {
        try {
            return (JCRUserNode) session.getNode(userKey);
        } catch (ClassCastException e) {
            // Not a user
            return null;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @param name User's identification name.
     * @return Return a reference on a new created JCRUserNode object.
     */
    public JCRUserNode lookupUser(String name) {
        return lookupUser(name, (String) null);
    }

    public JCRUserNode lookupUser(String name, String site) {
        try {
            return lookupUser(name, site, JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @param name User's identification name.
     * @return Return a reference on a new created JCRUserNode object.
     */
    public JCRUserNode lookupUser(String name, JCRSessionWrapper session) {
        return lookupUser(name, null, session);
    }

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @param name User's identification name.
     * @return Return a reference on a new created JCRUserNode object.
     */
    public JCRUserNode lookupUser(String name, String site, JCRSessionWrapper session) {
        if (StringUtils.isBlank(name)) {
            logger.error("Should not be looking for empty name user");
            return null;
        }
        final String path = getUserPath(name, site);
        if (path == null) {
            return null;
        }
        return lookupUserByPath(path, session);
    }

    public String getUserPath(String name) {
        return getUserPath(name, "");
    }

    public String getUserPath(String name, String site) {
        return userPathCache.getUserPath(name, site);
    }

    /**
     * This function checks into the system if the username has already been
     * assigned to another user.
     *
     * @param name User login name.
     * @return Return true if the specified username has not been assigned yet,
     * return false on any failure.
     */
    public boolean userExists(String name) {
        return lookupUser(name) != null;
    }

    /**
     * Returns the system root user name (cached)
     *
     * @return the system root user name (cached)
     */
    public String getRootUserName() {
        if (rootUserName == null) {
            rootUserName = lookupRootUser().getName();
        }
        return rootUserName;
    }

    /**
     * Returns the system root user (not cached).
     *
     * @return the system root user (not cached)
     */
    public JCRUserNode lookupRootUser() {
        try {
            return lookupRootUser(JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null));
        } catch (RepositoryException e1) {
            logger.error(e1.getMessage(), e1);
        }
        return null;
    }

    /**
     * Returns the system root user (not cached).
     *
     * @return the system root user (not cached)
     */
    public JCRUserNode lookupRootUser(JCRSessionWrapper session) {
        try {
            return (JCRUserNode) session.getNodeByIdentifier(JCRUserNode.ROOT_USER_UUID);
        } catch (RepositoryException e1) {
            logger.error(e1.getMessage(), e1);
        }
        return null;
    }

    /**
     * This method return all users' keys in the system.
     *
     * @return Return a List of strings holding the user identification key .
     */
    public List<String> getUserList() {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            List<String> users = new ArrayList<String>();
            if (session.getWorkspace().getQueryManager() != null) {
                String query = "SELECT [j:nodename] FROM [" + Constants.JAHIANT_USER + "] AS username ORDER BY localname(username) and isdescendantnode(username,'/users/')";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                QueryResult qr = q.execute();
                RowIterator rows = qr.getRows();
                while (rows.hasNext()) {
                    Row usersFolderNode = rows.nextRow();
                    if (usersFolderNode.getNode() != null) {
                        String userPath = usersFolderNode.getPath();
                        if (!users.contains(userPath)) {
                            users.add(userPath);
                        }
                    }
                }
            }
            return users;
        } catch (RepositoryException e) {
            logger.error("Error while retrieving user list", e);
            return Collections.emptyList();
        }
    }

    /**
     * This method returns the list of all the user names registered in the system.
     *
     * @return Return a List of strings holding the user identification names.
     */
    public List<String> getUsernameList() {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            Set<String> users = new TreeSet<String>();
            if (session.getWorkspace().getQueryManager() != null) {
                String query = "SELECT [j:nodename] FROM [" + Constants.JAHIANT_USER + "] where isdescendantnode('/users/')";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                QueryResult qr = q.execute();
                RowIterator rows = qr.getRows();
                while (rows.hasNext()) {
                    Row usersFolderNode = rows.nextRow();
                    String userName = usersFolderNode.getValue("j:nodename").getString();
                    users.add(userName);
                }
            }
            return new ArrayList<String>(users);
        } catch (RepositoryException e) {
            logger.error("Error while retrieving user name list", e);
            return new ArrayList<String>();
        }
    }

    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criterias
     * @return a Set of JCRUserNode elements that correspond to those search criterias
     */
    public Set<JCRUserNode> searchUsers(Properties searchCriterias) {
        try {
            return searchUsers(searchCriterias, JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null));
        } catch (RepositoryException e) {
            logger.error("Error while searching for users", e);
            return new HashSet<JCRUserNode>();
        }
    }

    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criterias
     * @return a Set of JCRUserNode elements that correspond to those search criterias
     */
    public Set<JCRUserNode> searchUsers(Properties searchCriterias, String[] providers) {
        try {
            return searchUsers(searchCriterias, providers, JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null));
        } catch (RepositoryException e) {
            logger.error("Error while searching for users", e);
            return new HashSet<JCRUserNode>();
        }
    }

    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criterias
     * @return a Set of JCRUserNode elements that correspond to those search criterias
     */
    public Set<JCRUserNode> searchUsers(Properties searchCriterias, JCRSessionWrapper session) {
        return searchUsers(searchCriterias, null, session);
    }

    public Set<JCRUserNode> searchUsers(final Properties searchCriterias, final String[] providerKeys, JCRSessionWrapper session) {
        return searchUsers(searchCriterias, null, providerKeys, session);
    }

    public Set<JCRUserNode> searchUsers(final Properties searchCriterias, String siteKey, final String[] providerKeys, JCRSessionWrapper session) {
        try {
            int limit = 0;
            Set<JCRUserNode> users = new HashSet<JCRUserNode>();
            if (session.getWorkspace().getQueryManager() != null) {
                StringBuilder query = new StringBuilder(128);

                // Add provider to query
                List<JCRStoreProvider> providers = getProviders(providerKeys, session);
                if (!providers.isEmpty()) {
                    query.append("(");
                    for (JCRStoreProvider provider : providers) {
                        query.append(query.length() > 1 ? " OR " : "");
                        if (provider.isDefault()) {
                            query.append("u.[j:external] = false");
                        } else {
                            query.append("ISDESCENDANTNODE('").append(provider.getMountPoint()).append("')");
                        }
                    }
                    query.append(")");
                }

                // Add criteria
                if (searchCriterias != null && searchCriterias.size() > 0) {
                    Properties filters = (Properties) searchCriterias.clone();
                    String operation = " OR ";
                    if (filters.containsKey(JahiaUserManagerService.MULTI_CRITERIA_SEARCH_OPERATION)) {
                        if (((String) filters.get(JahiaUserManagerService.MULTI_CRITERIA_SEARCH_OPERATION)).trim().toLowerCase().equals(
                                "and")) {
                            operation = " AND ";
                        }
                        filters.remove(JahiaUserManagerService.MULTI_CRITERIA_SEARCH_OPERATION);
                    }
                    if (filters.containsKey(JahiaUserManagerService.COUNT_LIMIT)) {
                        limit = Integer.parseInt((String) filters.get(JahiaUserManagerService.COUNT_LIMIT));
                        logger.debug("Limit of results has be set to " + limit);
                        filters.remove(JahiaUserManagerService.COUNT_LIMIT);
                    }
                    // Avoid wildcard attribute
                    if (!(filters.containsKey(
                            "*") && filters.size() == 1 && filters.getProperty("*").equals(
                            "*"))) {
                        Iterator<Map.Entry<Object, Object>> objectIterator = filters.entrySet().iterator();
                        if (objectIterator.hasNext()) {
                            query.append(query.length() > 0 ? " AND " : "").append(" (");
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
                                        propertyValue = Patterns.STAR.matcher(propertyValue).replaceAll("%");
                                    } else {
                                        propertyValue = propertyValue + "%";
                                    }
                                }
                                if ("*".equals(propertyKey)) {
                                    query.append("(CONTAINS(u.*,'" + Patterns.PERCENT.matcher(propertyValue).replaceAll("")
                                            + "') OR LOWER(u.[j:nodename]) LIKE '")
                                            .append(propertyValue.toLowerCase()).append("') ");
                                } else {
                                    query.append("LOWER(u.[" + Patterns.DOT.matcher(propertyKey).replaceAll("\\\\.") + "])").append(
                                            " LIKE '").append(propertyValue.toLowerCase()).append("'");
                                }
                                if (objectIterator.hasNext()) {
                                    query.append(operation);
                                }
                            }
                            query.append(")");
                        }
                    }
                }
                if (query.length() > 0) {
                    query.insert(0, "and ");
                }
                String s = (siteKey == null) ? "/users/" : "/sites/" + siteKey + "/users/";
                query.insert(0, "SELECT * FROM [" + Constants.JAHIANT_USER + "] as u where isdescendantnode(u,'" + s + "')");
                query.append(" ORDER BY u.[j:nodename]");
                if (logger.isDebugEnabled()) {
                    logger.debug(query.toString());
                }
                Query q = session.getWorkspace().getQueryManager().createQuery(query.toString(),
                        Query.JCR_SQL2);
                if (limit > 0) {
                    q.setLimit(limit);
                }
                QueryResult qr = q.execute();
                NodeIterator ni = qr.getNodes();
                while (ni.hasNext()) {
                    Node usersFolderNode = ni.nextNode();
                    users.add((JCRUserNode) usersFolderNode);
                }
            }

            return users;
        } catch (RepositoryException e) {
            logger.error("Error while searching for users", e);
            return new HashSet<JCRUserNode>();
        }
    }

    /**
     * Provide the list of all available users providers
     *
     * @param session the session used
     * @return list of JCRStoreProvider
     */
    public List<JCRStoreProvider> getProviderList(JCRSessionWrapper session) {
        List<JCRStoreProvider> providers = new LinkedList<JCRStoreProvider>();
        try {
            providers.add(JCRSessionFactory.getInstance().getDefaultProvider());
            JCRNodeWrapper providersNode = session.getNode("/users/providers");
            NodeIterator iterator = providersNode.getNodes();
            while (iterator.hasNext()) {
                JCRNodeWrapper providerNode = (JCRNodeWrapper) iterator.next();
                providers.add(providerNode.getProvider());
            }
        } catch (PathNotFoundException e) {
            // no providers node
        } catch (RepositoryException e) {
            logger.error("Error while retrieving user providers", e);
        }
        return providers;
    }

    /**
     * Provide the list of users providers matching given keys
     *
     * @param providerKeys the keys
     * @param session      the session used
     * @return list of JCRStoreProvider
     */
    public List<JCRStoreProvider> getProviders(String[] providerKeys, JCRSessionWrapper session) {
        if (ArrayUtils.isEmpty(providerKeys)) {
            return Collections.emptyList();
        }

        List<JCRStoreProvider> providers = new LinkedList<JCRStoreProvider>();
        for (JCRStoreProvider provider : getProviderList(session)) {
            if (ArrayUtils.contains(providerKeys, provider.getKey())) {
                providers.add(provider);
            }
        }
        return providers;
    }

    /**
     * Retrieve the user provider corresponding to a given key
     *
     * @param providerKey the key
     * @param session     the session used
     * @return JCRStoreProvider if it exist or null
     */
    public JCRStoreProvider getProvider(String providerKey, JCRSessionWrapper session) {
        List<JCRStoreProvider> providers = getProviders(new String[]{providerKey}, session);
        return providers.size() == 1 ? providers.get(0) : null;
    }


    /**
     * This is the method that creates a new user in the system, with all the
     * specified properties.
     *
     * @param name       User identification name.
     * @param password   User password
     * @param properties User additional parameters. If the user has no additional
     * @param session
     */
    public JCRUserNode createUser(final String name, final String password,
                                  final Properties properties, JCRSessionWrapper session) {
        try {
            String jcrUsernamePath[] = Patterns.SLASH.split(StringUtils.substringAfter(
                    ServicesRegistry.getInstance().getJahiaUserManagerService().getUserSplittingRule().getPathForUsername(
                            name), "/"
            ));
            JCRNodeWrapper startNode = session.getNode("/" + jcrUsernamePath[0]);
            int length = jcrUsernamePath.length;
            for (int i = 1; i < length; i++) {
                try {
                    startNode = startNode.getNode(jcrUsernamePath[i]);
                } catch (PathNotFoundException e) {
                    try {
                        session.checkout(startNode);
                        if (i == (length - 1)) {
                            JCRNodeWrapper userNode = startNode.addNode(name, Constants.JAHIANT_USER);
                            userNode.grantRoles("u:" + name, Collections.singleton("owner"));
                            String l_password;
                            if (!password.startsWith("SHA-1:")) {
                                // Encrypt the password
                                l_password = JahiaUserManagerService.encryptPassword(password);
                            } else {
                                l_password = password.substring(6);
                            }

                            userNode.setProperty(JCRUserNode.J_PASSWORD, l_password);
                            userNode.setProperty(JCRUserNode.J_EXTERNAL, false);
                            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                                String key = (String) entry.getKey();
                                userNode.setProperty(key, (String) entry.getValue());
                            }
                            return (JCRUserNode) userNode;
                        } else {
                            // Simply create a folder
                            startNode = startNode.addNode(jcrUsernamePath[i], "jnt:usersFolder");
                        }
                    } catch (RepositoryException e1) {
                        logger.error("Cannot save", e1);
                    }
                }
            }
            return (JCRUserNode) startNode;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * This method removes a user from the system. All the user's attributes are
     * remove, and also all the related objects belonging to the user. On success,
     * true is returned and the user parameter is not longer valid. Return false
     * on any failure.
     *
     * @param userPath reference on the user to be deleted.
     * @param session
     */
    public boolean deleteUser(final String userPath, JCRSessionWrapper session) {
        try {
            Node node;
            try {
                node = session.getNode(userPath);
            } catch (ItemNotFoundException e) {
                // the user deletion in live is automated with jmix:autoPublish
                return true;
            }
            if (node.isNodeType(Constants.JAHIAMIX_SYSTEMNODE)) {
                return false;
            }
            String query = "SELECT * FROM [jnt:ace] as ace where ace.[j:principal]='u:" + node.getName() + "'";
            Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
            QueryResult qr = q.execute();
            NodeIterator nodeIterator = qr.getNodes();
            while (nodeIterator.hasNext()) {
                Node next = nodeIterator.nextNode();
                session.checkout(next.getParent());
                session.checkout(next);
                next.remove();
            }

            PropertyIterator pi = node.getWeakReferences("j:member");
            while (pi.hasNext()) {
                JCRPropertyWrapper propertyWrapper = (JCRPropertyWrapper) pi.next();
                propertyWrapper.getParent().remove();
            }

            session.checkout(node.getParent());
            session.checkout(node);
            node.remove();
            return true;
        } catch (RepositoryException e) {
            logger.error("Error while deleting user", e);
            return false;
        }
    }


    /**
     * Returns <code>true</code> if the specified user is <code>null</code> or a
     * guest user.
     *
     * @param user the user to be tested
     * @return <code>true</code> if the specified user is <code>null</code> or a
     * guest user
     */
    public static boolean isGuest(JCRUserNode user) {
        return user == null || GUEST_USERNAME.equals(user.getName());
    }

    public static boolean isGuest(JahiaUser user) {
        return user == null || GUEST_USERNAME.equals(user.getName());
    }

    /**
     * Encrypt the specified password using the SHA algorithm.
     *
     * @param password String representation of a password.
     * @return Return a String representation of the password encryption. Return
     * null on any failure.
     */
    public static String encryptPassword(String password) {
        if (StringUtils.isEmpty(password)) {
            return null;
        }

        return EncryptionUtils.sha1DigestLegacy(password);
    }

    /**
     * Validates provided user name against a regular expression pattern, specified in the Jahia configuration.
     *
     * @param name the user name to be validated
     * @return <code>true</code> if the specified user name matches the validation pattern
     */
    public boolean isUsernameSyntaxCorrect(String name) {
        if (name == null || name.length() == 0) {
            return false;
        }

        boolean usernameCorrect = getUserNamePattern().matcher(name)
                .matches();
        if (!usernameCorrect && logger.isDebugEnabled()) {
            logger
                    .debug("Validation failed for the user name: "
                            + name + " against pattern: "
                            + getUserNamePattern().pattern());
        }
        return usernameCorrect;
    }

    @Override
    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        checkRootUserPwd();
    }

    private void checkRootUserPwd() {
        try {
            final String pwd = getNewRootUserPwd();
            if (StringUtils.isNotEmpty(pwd)) {
                logger.info("Resetting root user password");
                JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                    @Override
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        lookupRootUser(session).setPassword(pwd);
                        session.save();
                        return null;
                    }
                });
                logger.info("New root user password set.");
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    private String getNewRootUserPwd() throws MalformedURLException, IOException {
        String pwd = null;
        File pwdFile = null;

        if (servletContext.getResource(ROOT_PWD_RESET_FILE_PATH) != null) {
            String path = servletContext.getRealPath(ROOT_PWD_RESET_FILE_PATH);
            pwdFile = path != null ? new File(path) : null;
        } else {
            pwdFile = new File(settingsBean.getJahiaVarDiskPath(), ROOT_PWD_RESET_FILE);
        }
        if (pwdFile != null && pwdFile.exists()) {
            pwd = FileUtils.readFileToString(pwdFile);
            try {
                pwdFile.delete();
            } catch (Exception e) {
                logger.warn("Unable to delete " + pwdFile + " file after resetting root password", e);
            }
        }

        return pwd != null ? StringUtils.chomp(pwd).trim() : null;
    }

    public void updatePathCacheAdded(String userPath) {
        userPathCache.updateAdded(userPath);
    }

    public void updatePathCacheRemoved(String userPath) {
        userPathCache.updateRemoved(userPath);
    }

    /**
     * get the list of olds users providers
     * this method is used to maintain compatibility with old users providers
     *
     * @deprecated
     */
    @Deprecated
    public List<? extends JahiaUserManagerProvider> getProviderList() {
        return new ArrayList<JahiaUserManagerProvider>(legacyUserProviders.values());
    }

    /**
     * get the provider corresponding to a given key
     * this method is used to maintain compatibility with old users providers
     *
     * @deprecated
     */
    @Deprecated
    public JahiaUserManagerProvider getProvider(String key) {
        return legacyUserProviders.get(key);
    }

    /**
     * register a user provider
     * this method is used to maintain compatibility with old users providers
     *
     * @deprecated
     */
    @Deprecated
    public void registerProvider(JahiaUserManagerProvider jahiaUserManagerProvider) {
        legacyUserProviders.put(jahiaUserManagerProvider.getKey(), jahiaUserManagerProvider);
        BridgeEvents.sendEvent(jahiaUserManagerProvider.getKey(), BridgeEvents.USER_PROVIDER_REGISTER_BRIDGE_EVENT_KEY);
    }

    /**
     * unregister a user provider
     * this method is used to maintain compatibility with old users providers
     *
     * @deprecated
     */
    @Deprecated
    public void unregisterProvider(JahiaUserManagerProvider jahiaUserManagerProvider) {
        legacyUserProviders.remove(jahiaUserManagerProvider.getKey());
        BridgeEvents.sendEvent(jahiaUserManagerProvider.getKey(), BridgeEvents.USER_PROVIDER_UNREGISTER_BRIDGE_EVENT_KEY);
    }

    public void setUserPathCache(UserPathCache userPathCache) {
        this.userPathCache = userPathCache;
    }

    public int getTimeToLiveForEmptyPath() {
        return userPathCache.getTimeToLiveForEmptyPath();
    }
}
