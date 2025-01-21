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
package org.jahia.services.usermanager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.osgi.FrameworkService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.JahiaService;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.pwd.PasswordService;
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
public class JahiaUserManagerService extends JahiaService implements JahiaAfterInitializationService, ServletContextAware, org.jahia.api.usermanager.JahiaUserManagerService {

    private static final Logger logger = LoggerFactory.getLogger(JahiaUserManagerService.class);
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
    private volatile String rootUserName;
    private Map<String, JahiaUserManagerProvider> legacyUserProviders = new HashMap<>();
    private UserCacheHelper cacheHelper;
    private PasswordService passwordService;

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final JahiaUserManagerService INSTANCE = new JahiaUserManagerService();
    }

    public static JahiaUserManagerService getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public JahiaUserSplittingRule getUserSplittingRule() {
        return userSplittingRule;
    }

    public void setUserSplittingRule(JahiaUserSplittingRule userSplittingRule) {
        this.userSplittingRule = userSplittingRule;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void start() throws JahiaInitializationException {
    }

    @Override
    public void stop() throws JahiaException {
    }

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created JCRUserNode object.
     * @deprecated use lookupUserByPath() instead
     */
    @Override
    public JCRUserNode lookupUserByKey(String userKey) {
        return lookupUserByPath(userKey);
    }

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created JCRUserNode object.
     */
    @Override
    public JCRUserNode lookupUserByPath(String path) {
        JCRUserNode userNode = lookupUserByPath(path, Constants.LIVE_WORKSPACE);
        if (userNode == null) {
            userNode = lookupUserByPath(path, Constants.EDIT_WORKSPACE);
        }
        return userNode;
    }

    private JCRUserNode lookupUserByPath(String path, String workspace) {
        try {
            return lookupUserByPath(path, JCRSessionFactory.getInstance().getCurrentSystemSession(workspace, null, null));
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
    @Override
    public JCRUserNode lookupUserByPath(String userKey, JCRSessionWrapper session) {
        try {
            return (JCRUserNode) session.getNode(userKey);
        } catch (PathNotFoundException e) {
            // Does not exist anymore
            return null;
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
    @Override
    public JCRUserNode lookupUser(String name) {
        return lookupUser(name, null, false);
    }

    @Override
    public JCRUserNode lookupUser(String name, String site) {
        return lookupUser(name, site, true);
    }

    @Override
    public JCRUserNode lookupUser(String name, String site, boolean checkSiteAndGlobalUsers) {
        JCRUserNode userNode = null;
        try {
            userNode = lookupUser(name, site, checkSiteAndGlobalUsers, Constants.LIVE_WORKSPACE);
            if (userNode == null) {
                userNode = lookupUser(name, site, checkSiteAndGlobalUsers, Constants.EDIT_WORKSPACE);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return userNode;
    }

    private JCRUserNode lookupUser(String name, String site, boolean checkSiteAndGlobalUsers, String workspace) throws RepositoryException {
        JCRSessionWrapper s = JCRSessionFactory.getInstance().getCurrentSystemSession(workspace, null, null);
        JCRUserNode userNode = null;
        if (checkSiteAndGlobalUsers && site != null) {
            userNode = lookupUser(name, null, s);
        }
        if (userNode == null) {
            userNode = lookupUser(name, site, s);
        }
        return userNode;
    }

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @param name User's identification name.
     * @return Return a reference on a new created JCRUserNode object.
     */
    @Override
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
    @Override
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

    @Override
    public String getUserPath(String name) {
        return getUserPath(name, "");
    }

    @Override
    public String getUserPath(String name, String site) {
        return cacheHelper.getUserPath(name, site);
    }

    /**
     * This function checks into the system if the username has already been
     * assigned to another user.
     *
     * @param name User login name.
     * @return Return true if the specified username has not been assigned yet,
     * return false on any failure.
     */
    @Override
    public boolean userExists(String name) {
        return lookupUser(name) != null;
    }

    @Override
    public boolean userExists(String name, String siteKey) {
        return lookupUser(name, siteKey) != null;
    }

    /**
     * Returns the system root user name (cached)
     *
     * @return the system root user name (cached)
     */
    @Override
    public String getRootUserName() {
        // Thread-safe lazy loading, using double-checked locking pattern
        // see https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        String userName = rootUserName;
        if (userName == null) {
            synchronized (this) {
                userName = rootUserName;
                if (userName == null) {
                    rootUserName = userName = lookupRootUser().getName();
                }
            }
        }
        return userName;
    }

    /**
     * Returns the system root user (not cached).
     *
     * @return the system root user (not cached)
     */
    @Override
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
    @Override
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
    @Override
    public List<String> getUserList() {
        return getUserList(null);
    }

    @Override
    public List<String> getUserList(String siteKey) {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.LIVE_WORKSPACE, null, null);
            List<String> users = new ArrayList<>();
            if (session.getWorkspace().getQueryManager() != null) {
                String query = siteKey == null
                        ? "SELECT [j:nodename] FROM [" + Constants.JAHIANT_USER
                                + "] AS username WHERE isdescendantnode(username,'/users/') ORDER BY localname(username)"
                        : "SELECT [j:nodename] FROM [" + Constants.JAHIANT_USER
                                + "] AS username WHERE isdescendantnode(username,'/sites/" + siteKey
                                + "/users/') ORDER BY localname(username)";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                QueryResult qr = q.execute();
                RowIterator rows = qr.getRows();
                while (rows.hasNext()) {
                    Row row = rows.nextRow();
                    if (row.getNode() != null) {
                        String userPath = row.getPath();
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
    @Override
    public List<String> getUsernameList() {
        return getUsernameList(null);
    }

    /**
     * This method returns the list of all the user names registered in the system.
     *
     * @return Return a List of strings holding the user identification names.
     */
    @Override
    public List<String> getUsernameList(String siteKey) {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.LIVE_WORKSPACE, null, null);
            Set<String> users = new TreeSet<>();
            if (session.getWorkspace().getQueryManager() != null) {
                String usersPath = (siteKey == null) ? "/users/" : "/sites/" + siteKey + "/users/";
                String query = "SELECT [j:nodename] FROM [" + Constants.JAHIANT_USER + "] where isdescendantnode('" + usersPath + "')";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                QueryResult qr = q.execute();
                RowIterator rows = qr.getRows();
                while (rows.hasNext()) {
                    Row row = rows.nextRow();
                    String userName = row.getValue("j:nodename").getString();
                    users.add(userName);
                }
            }
            return new ArrayList<>(users);
        } catch (RepositoryException e) {
            logger.error("Error while retrieving user name list", e);
            return new ArrayList<>();
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
    @Override
    public Set<JCRUserNode> searchUsers(Properties searchCriterias) {
        try {
            return searchUsers(searchCriterias, JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.LIVE_WORKSPACE, null, null));
        } catch (RepositoryException e) {
            logger.error("Error while searching for users", e);
            return new HashSet<>();
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
    @Override
    public Set<JCRUserNode> searchUsers(Properties searchCriterias, String[] providers) {
        try {
            return searchUsers(searchCriterias, providers, JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.LIVE_WORKSPACE, null, null));
        } catch (RepositoryException e) {
            logger.error("Error while searching for users", e);
            return new HashSet<>();
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
    @Override
    public Set<JCRUserNode> searchUsers(Properties searchCriterias, JCRSessionWrapper session) {
        return searchUsers(searchCriterias, null, session);
    }

    @Override
    public Set<JCRUserNode> searchUsers(final Properties searchCriterias, final String[] providerKeys, JCRSessionWrapper session) {
        return searchUsers(searchCriterias, null, providerKeys, session);
    }

    @Override
    public Set<JCRUserNode> searchUsers(final Properties searchCriterias, String siteKey, final String[] providerKeys, JCRSessionWrapper session) {
        return searchUsers(searchCriterias, siteKey, providerKeys, false, session);
    }

    @Override
    public Set<JCRUserNode> searchUsers(Properties searchCriterias, String siteKey, final String[] providerKeys, boolean excludeProtected, JCRSessionWrapper session) {

        if (providerKeys != null) {
            Set<JCRUserNode> users = new HashSet<>();

            int limit = -1;
            if (searchCriterias.containsKey(COUNT_LIMIT)) {
                searchCriterias = (Properties) searchCriterias.clone();
                limit = Integer.parseInt((String) searchCriterias.get(COUNT_LIMIT));
            }

            for (String providerKey : providerKeys) {
                Set<JCRUserNode> userNodes = searchUsers(searchCriterias, siteKey, providerKey, excludeProtected, session);
                users.addAll(userNodes);
                if (limit > -1) {
                    limit -= userNodes.size();
                    if (limit <= 0) {
                        return users;
                    }
                    searchCriterias.put(COUNT_LIMIT, Integer.toString(limit));
                }
            }
            return users;
        } else {
            return searchUsers(searchCriterias, siteKey, (String) null, excludeProtected, session);
        }
    }

    @Override
    public Set<JCRUserNode> searchUsers(final Properties searchCriterias, String siteKey, final String providerKey, boolean excludeProtected, JCRSessionWrapper session) {
        try {
            int limit = 0;
            Set<JCRUserNode> users = new HashSet<>();

            if (session.getWorkspace().getQueryManager() != null) {

                StringBuilder query = new StringBuilder();

                // Add provider to query
                if (providerKey != null) {
                    JCRStoreProvider provider = getProvider(siteKey, providerKey, session);
                    if (provider != null) {
                        query.append("(");
                        if (provider.isDefault()) {
                            query.append("u.[j:external] = false");
                        } else {
                            query.append("ISDESCENDANTNODE('").append(provider.getMountPoint()).append("')");
                        }
                        query.append(")");
                    } else {
                        return users;
                    }
                }

                // Add criteria
                if (searchCriterias != null && !searchCriterias.isEmpty()) {
                    Properties filters = (Properties) searchCriterias.clone();
                    String operation = " OR ";
                    if (filters.containsKey(MULTI_CRITERIA_SEARCH_OPERATION)) {
                        if (((String) filters.get(MULTI_CRITERIA_SEARCH_OPERATION)).trim().toLowerCase().equals(
                                "and")) {
                            operation = " AND ";
                        }
                        filters.remove(MULTI_CRITERIA_SEARCH_OPERATION);
                    }
                    if (filters.containsKey(COUNT_LIMIT)) {
                        limit = Integer.parseInt((String) filters.get(COUNT_LIMIT));
                        logger.debug("Limit of results has be set to {}", limit);
                        filters.remove(COUNT_LIMIT);
                    }
                    // Avoid wildcard attribute
                    if (!(filters.containsKey(
                            "*") && filters.size() == 1 && filters.getProperty("*").equals(
                            "*"))) {
                        Iterator<Map.Entry<Object, Object>> criteriaIterator = filters.entrySet().iterator();
                        if (criteriaIterator.hasNext()) {
                            query.append(query.length() > 0 ? " AND " : "").append(" (");
                            while (criteriaIterator.hasNext()) {
                                Map.Entry<Object, Object> entry = criteriaIterator.next();
                                String propertyKey = (String) entry.getKey();
                                if ("username".equals(propertyKey)) {
                                    propertyKey = "j:nodename";
                                }
                                String propertyValue = (String) entry.getValue();
                                if ("*".equals(propertyValue)) {
                                    propertyValue = "%";
                                } else {
                                    if (propertyValue.indexOf('*') != -1) {
                                        propertyValue = Patterns.STAR.matcher(propertyValue).replaceAll("%");
                                    } else {
                                        propertyValue = propertyValue + "%";
                                    }
                                }
                                propertyValue = JCRContentUtils.sqlEncode(propertyValue);
                                if ("*".equals(propertyKey)) {
                                    query.append("(CONTAINS(u.*,'" + QueryParser.escape(Patterns.PERCENT.matcher(propertyValue).replaceAll(""))
                                            + "') OR LOWER(u.[j:nodename]) LIKE '")
                                            .append(propertyValue.toLowerCase()).append("') ");
                                } else {
                                    query.append("LOWER(u.[" + Patterns.DOT.matcher(propertyKey).replaceAll("\\\\.") + "])").append(
                                            " LIKE '").append(propertyValue.toLowerCase()).append("'");
                                }
                                if (criteriaIterator.hasNext()) {
                                    query.append(operation);
                                }
                            }
                            query.append(")");
                        }
                    }
                }

                if (query.length() > 0) {
                    query.insert(0, " and ");
                }
                if (excludeProtected) {
                    query.insert(0, " and [j:nodename] <> '" + GUEST_USERNAME + "'");
                }
                String s = (siteKey == null) ? "/users/" : "/sites/" + siteKey + "/users/";
                query.insert(0, "SELECT * FROM [" + Constants.JAHIANT_USER + "] as u where isdescendantnode(u,'" + JCRContentUtils.sqlEncode(s) + "')");
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
                    Node userNode = ni.nextNode();
                    users.add((JCRUserNode) userNode);
                }
            }

            return users;
        } catch (RepositoryException e) {
            logger.error("Error while searching for users", e);
            return new HashSet<>();
        }
    }

    /**
     * Provide the list of all available users providers
     *
     * @param session the session used
     * @return list of JCRStoreProvider
     */
    @Override
    public List<JCRStoreProvider> getProviderList(JCRSessionWrapper session) {
        return getProviderList(null, session);
    }

    @Override
    public List<JCRStoreProvider> getProviderList(String siteKey, JCRSessionWrapper session) {
        List<JCRStoreProvider> providers = new LinkedList<>();
        try {
            providers.add(JCRSessionFactory.getInstance().getDefaultProvider());
            String providersParentPath = (siteKey == null ? "" : "/sites/" + siteKey) + "/users/providers";
            JCRNodeWrapper providersNode = session.getNode(providersParentPath);
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
    @Override
    public List<JCRStoreProvider> getProviders(String[] providerKeys, JCRSessionWrapper session) {
        return getProviders(null, providerKeys, session);
    }

    @Override
    public List<JCRStoreProvider> getProviders(String siteKey, String[] providerKeys, JCRSessionWrapper session) {
        if (ArrayUtils.isEmpty(providerKeys)) {
            return Collections.emptyList();
        }
        List<JCRStoreProvider> providers = new LinkedList<>();
        for (JCRStoreProvider provider : getProviderList(siteKey, session)) {
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
    @Override
    public JCRStoreProvider getProvider(String providerKey, JCRSessionWrapper session) {
        return getProvider(null, providerKey, session);
    }

    @Override
    public JCRStoreProvider getProvider(String siteKey, String providerKey, JCRSessionWrapper session) {
        List<JCRStoreProvider> providers = getProviders(siteKey, new String[]{providerKey}, session);
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
    @Override
    public JCRUserNode createUser(final String name, final String password, final Properties properties, JCRSessionWrapper session) {
        return createUser(name, null, password, properties, session);
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
    @Override
    public JCRUserNode createUser(final String name, String siteKey, final String password, final Properties properties, JCRSessionWrapper session) {
        try {
            String jcrUsernamePath[] = Patterns.SLASH.split(StringUtils.substringAfter(
                    ServicesRegistry.getInstance().getJahiaUserManagerService().getUserSplittingRule().getPathForUsername(
                            name), "/"
            ));
            JCRNodeWrapper startNode = session.getNode(((siteKey == null) ? "/" : "/sites/" + siteKey));
            int length = jcrUsernamePath.length;
            for (int i = 0; i < length; i++) {
                try {
                    startNode = startNode.getNode(jcrUsernamePath[i]);
                } catch (PathNotFoundException e) {
                    try {
                        if (i == (length - 1)) {
                            JCRNodeWrapper userNode = startNode.addNode(name, Constants.JAHIANT_USER);
                            userNode.grantRoles("u:" + name, Collections.singleton("owner"));
                            String l_password;
                            if (!password.startsWith("SHA-1:")) {
                                // Encrypt the password
                                l_password = passwordService.digest(password);
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
    @Override
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

            // Site of origin for the user, if the user is from /users this will be an empty string
            String site = StringUtils.substringBefore(StringUtils.substringAfter(userPath, "/sites/"), "/");
            StringBuilder sb = new StringBuilder("SELECT * FROM [jnt:ace] as ace where ");

            // In the case user was created for specific site remove only aces which are under the site to prevent cross site issues
            if (!"".equals(site)) {
                sb.append(String.format("isdescendantnode(ace,'/sites/%s') and ", site));
            }

            sb.append(String.format("ace.[j:principal]='u:%s'", JCRContentUtils.sqlEncode(node.getName())));

            Query q = session.getWorkspace().getQueryManager().createQuery(sb.toString(), Query.JCR_SQL2);
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

    /**
     * Returns <code>true</code> if the specified user is <code>null</code> or a
     * guest user.
     *
     * @param user the user to be tested
     * @return <code>true</code> if the specified user is <code>null</code> or a
     * guest user
     */
    public static boolean isGuest(JahiaUser user) {
        return user == null || GUEST_USERNAME.equals(user.getName());
    }

    /**
     * Encrypt the specified password using the SHA algorithm.
     *
     * @param password String representation of a password.
     * @return Return a String representation of the password encryption. Return
     * null on any failure.
     * @deprecated since 7.1.0.1 use {@link PasswordService#digest(String)} instead
     */
    @Deprecated(since = "7.1.0.1", forRemoval = true)
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
    @Override
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
        for (String key : legacyUserProviders.keySet()) {
            BridgeEvents.sendEvent(key, BridgeEvents.USER_PROVIDER_REGISTER_BRIDGE_EVENT_KEY);
        }
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
        cacheHelper.updateAdded(userPath);
    }

    public void updatePathCacheRemoved(String userPath) {
        cacheHelper.updateRemoved(userPath);
    }

    /**
     * get the list of olds users providers
     * this method is used to maintain compatibility with old users providers
     *
     * @deprecated
     */
    @Override
    @Deprecated(since = "7.1.0.0", forRemoval = true)
    public List<? extends JahiaUserManagerProvider> getProviderList() {
        return new ArrayList<>(legacyUserProviders.values());
    }

    /**
     * get the provider corresponding to a given key
     * this method is used to maintain compatibility with old users providers
     *
     * @deprecated
     */
    @Override
    @Deprecated(since = "7.1.0.0", forRemoval = true)
    public JahiaUserManagerProvider getProvider(String key) {
        return legacyUserProviders.get(key);
    }

    /**
     * register a user provider
     * this method is used to maintain compatibility with old users providers
     *
     * @deprecated
     */
    @Override
    @Deprecated(since = "7.1.0.0", forRemoval = true)
    public void registerProvider(JahiaUserManagerProvider jahiaUserManagerProvider) {
        legacyUserProviders.put(jahiaUserManagerProvider.getKey(), jahiaUserManagerProvider);
        if (FrameworkService.getInstance().isStarted()) {
            BridgeEvents.sendEvent(jahiaUserManagerProvider.getKey(), BridgeEvents.USER_PROVIDER_REGISTER_BRIDGE_EVENT_KEY);
        }
    }

    /**
     * unregister a user provider
     * this method is used to maintain compatibility with old users providers
     *
     * @deprecated
     */
    @Deprecated(since = "7.1.0.0", forRemoval = true)
    public void unregisterProvider(JahiaUserManagerProvider jahiaUserManagerProvider) {
        legacyUserProviders.remove(jahiaUserManagerProvider.getKey());
        BridgeEvents.sendEvent(jahiaUserManagerProvider.getKey(), BridgeEvents.USER_PROVIDER_UNREGISTER_BRIDGE_EVENT_KEY);
    }

    public void setCacheHelper(UserCacheHelper userPathCache) {
        this.cacheHelper = userPathCache;
    }

    /**
     * Remove all cache entries for non existing groups
     */
    public void clearNonExistingUsersCache() {
        cacheHelper.clearNonExistingUsersCache();
    }

    /**
     * Performs the lookup of the user, detecting the type of the provided lookup key (either a JCR path, a user name or a legacy user key).
     *
     * @param lookupKey
     *            the identifier to lookup the user (can be a JCR path, user name or legacy user key)
     * @return the corresponding user node or null if no user can be found for the specified key
     */
    @Override
    public JCRUserNode lookup(String lookupKey) {
        JCRUserNode user = null;
        char first = lookupKey.charAt(0);
        if ('/' == first) {
            // by path
            user = lookupUserByPath(lookupKey);
        } else if ('{' == first) {
            // by legacy user key
            user = lookupUser(StringUtils.substringAfter(lookupKey, "}"));
        } else {
            // by user name
            user = lookupUser(lookupKey);
        }
        return user;
    }

    public void setPasswordService(PasswordService passwordService) {
        this.passwordService = passwordService;
    }
}
