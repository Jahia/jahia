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
package org.jahia.services.usermanager.jcr;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.query.QueryResultWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerProvider;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
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

/**
 * JCR implementation of the user manager provider interface. This class stores and
 * retrieves users from a JCR backend.
 *
 * @author rincevent
 * @since JAHIA 6.5
 */
public class JCRUserManagerProvider extends JahiaUserManagerProvider implements ServletContextAware, JahiaAfterInitializationService {
    private static final String ROOT_PWD_RESET_FILE = "root.pwd";
    private static final String ROOT_PWD_RESET_FILE_PATH = "/WEB-INF/etc/config/" + ROOT_PWD_RESET_FILE;
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRUserManagerProvider.class);
    private transient JCRTemplate jcrTemplate;
    private transient JCRSessionFactory jcrSessionFactory;
    private static JCRUserManagerProvider mUserManagerService;
    private ServletContext servletContext;


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

    public void setJcrSessionFactory(JCRSessionFactory jcrSessionFactory) {
        this.jcrSessionFactory = jcrSessionFactory;
    }

    /**
     * This is the method that creates a new user in the system, with all the
     * specified attributes.
     *
     * @param name       the name for the newly created user
     * @param password   User password
     * @param properties user properties
     */
    public JCRUserNode createUser(final String name, final String password, final Properties properties) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JCRUserNode>() {
                public JCRUserNode doInJCR(JCRSessionWrapper jcrSessionWrapper) throws RepositoryException {
                    String jcrUsernamePath[] = Patterns.SLASH.split(StringUtils.substringAfter(
                            ServicesRegistry.getInstance().getJahiaUserManagerService().getUserSplittingRule().getPathForUsername(
                                    name), "/"
                    ));
                    JCRNodeWrapper startNode = jcrSessionWrapper.getNode("/" + jcrUsernamePath[0]);
                    Node usersFolderNode = startNode;
                    int length = jcrUsernamePath.length;
                    for (int i = 1; i < length; i++) {
                        try {
                            startNode = startNode.getNode(jcrUsernamePath[i]);
                        } catch (PathNotFoundException e) {
                            try {
                                jcrSessionWrapper.checkout(startNode);
                                if (i == (length - 1)) {
                                    JCRNodeWrapper userNode = startNode.addNode(name, Constants.JAHIANT_USER);
                                    if (usersFolderNode.hasProperty("j:usersFolderSkeleton")) {
                                        String skeletons = usersFolderNode.getProperty(
                                                "j:usersFolderSkeleton").getString();
                                        try {
                                            JCRContentUtils.importSkeletons(skeletons,
                                                    startNode.getPath() + "/" + jcrUsernamePath[i], jcrSessionWrapper);
                                        } catch (Exception importEx) {
                                            logger.error("Unable to import data using user skeletons " + skeletons,
                                                    importEx);
                                            throw new RepositoryException(
                                                    "Could not create user due to some import issues", importEx);
                                        }
                                    }
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
                                    jcrSessionWrapper.save();
                                    return (JCRUserNode) userNode;
                                } else {
                                    // Simply create a folder
                                    startNode = startNode.addNode(jcrUsernamePath[i], "jnt:usersFolder");
                                    jcrSessionWrapper.save();
                                }
                            } catch (RepositoryException e1) {
                                logger.error("Cannot save", e1);
                            }
                        }
                    }
                    return (JCRUserNode) startNode;
                }
            });
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
     * @param user reference on the user to be deleted.
     */
    public boolean deleteUser(final String userPath) {
        try {
            JCRCallback<Boolean> deleteCallback = new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
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
                    session.save();
                    return true;
                }
            };
            jcrTemplate.doExecuteWithSystemSession(deleteCallback);
            // Now let's delete the live workspace user
            jcrTemplate.doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, deleteCallback);
        } catch (RepositoryException e) {
            logger.error("Error while deleting user", e);
            return false;
        }
        return true;
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
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            List<String> users = new ArrayList<String>();
            if (session.getWorkspace().getQueryManager() != null) {
                String query = "SELECT * FROM [" + Constants.JAHIANT_USER + "] AS username ORDER BY localname(username)";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                QueryResult qr = q.execute();
                RowIterator rows = qr.getRows();
                while (rows.hasNext()) {
                    Row usersFolderNode = rows.nextRow();
                    if (usersFolderNode.getNode() != null) {
                        String userName = "{jcr}" + usersFolderNode.getNode().getName();
                        if (!users.contains(userName)) {
                            users.add(userName);
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
                String query = "SELECT [j:nodename] FROM [" + Constants.JAHIANT_USER + "]";
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
     * Performs a login of the specified user.
     *
     * @param userKey      the user identifier defined in this service properties
     * @param userPassword the password of the user
     * @return boolean true if the login succeeded, false otherwise
     */
    public boolean login(String userKey, String userPassword) throws RepositoryException {
        return lookupUser(userKey).verifyPassword(userPassword);
    }

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return a reference on a new created jahiaUser object.
     */
    public JCRUserNode lookupUserByKey(String userKey) throws RepositoryException {
        if (!userKey.startsWith("{")) {
            logger.warn("Expected userKey with provider prefix {jcr}, defaulting to looking up by name instead for parameter=[" + userKey + "]... ");
            return lookupUser(userKey);
        }
        return lookupUser(StringUtils.substringAfter(userKey, "}"));
    }

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created jahiaUser object.
     */
    public JCRUserNode lookupUser(final String name) {
        if (StringUtils.isBlank(name)) {
            logger.error("Should not be looking for empty name user");
            return null;
        }
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            JCRNodeIteratorWrapper users = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:user] where localname()='" + name + "'", Query.JCR_SQL2).execute().getNodes();
//        Node userNode = session.getNode(ServicesRegistry.getInstance().getJahiaUserManagerService().getUserSplittingRule().getPathForUsername(name));
            if (!users.hasNext()) return null;
            return (JCRUserNode) users.nextNode();
        } catch (RepositoryException e) {
            return null;
        }
    }

    public JCRUserNode lookupExternalUser(final JahiaUser jahiaUser) {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            Node userNode = session.getNode(jahiaUser.getLocalPath());
            if (userNode.getProperty(JCRUserNode.J_EXTERNAL).getBoolean()) {
                return (JCRUserNode) userNode;
            }
            return null;
        } catch (PathNotFoundException pnfe) {
            // This is expected in the case the user doesn't exist in the repository. We will simply return null.
        } catch (RepositoryException e) {
            logger.error("Error while looking up external user by name " + jahiaUser.getName(), e);
        }
        return null;
    }

    public JCRUserNode lookupExternalUser(final String username) {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            Node userNode = session.getNode(ServicesRegistry.getInstance().getJahiaUserManagerService().getUserSplittingRule().getPathForUsername(username));
            if (userNode.getProperty(JCRUserNode.J_EXTERNAL).getBoolean()) {
                return (JCRUserNode) userNode;
            }
            return null;
        } catch (PathNotFoundException pnfe) {
            // This is expected in the case the user doesn't exist in the repository. We will simply return null.
        } catch (RepositoryException e) {
            logger.error("Error while looking up external user by name " + username, e);
        }
        return null;
    }


    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criteria
     * @return Set a set of JahiaUser elements that correspond to those
     * search criteria
     */
    public Set<JCRUserNode> searchUsers(final Properties searchCriterias) {
        return searchUsers(searchCriterias, false, null);
    }

    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param searchCriterias a Properties object that contains search criteria
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criteria
     * @param external        do we consider only external users? <code>null</code> if all users should be considered
     * @param providerKey     the key of the user provider; <code>null</code> if all users should be considered
     * @return Set a set of JahiaUser elements that correspond to those
     * search criteria
     */
    public Set<JCRUserNode> searchUsers(final Properties searchCriterias, final Boolean external, final String providerKey) {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            int limit = 0;
            Set<JCRUserNode> users = new HashSet<JCRUserNode>();
            if (session.getWorkspace().getQueryManager() != null) {
                StringBuilder query = new StringBuilder(128);
                if (external != null) {
                    query.append("u.[" + JCRUserNode.J_EXTERNAL + "] = '").append(external).append("'");
                }
                if (providerKey != null) {
                    query.append(query.length() > 0 ? " AND " : "").append(" u.[" + JCRUserNode.J_EXTERNAL_SOURCE + "] = '").append(providerKey).append("'");
                }
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
                    query.insert(0, "WHERE ");
                }
                query.insert(0, "SELECT * FROM [" + Constants.JAHIANT_USER + "] as u ");
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
     * This function checks into the system if the username has already been
     * assigned to another user.
     *
     * @return Return true if the specified username has not been assigned yet,
     * return false on any failure.
     */
    public boolean userExists(final String name) {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            if (session.getWorkspace().getQueryManager() != null) {
                String query = "SELECT * FROM [" + Constants.JAHIANT_USER + "] as u WHERE u.[j:nodename] = '" + name + "' AND u.[" + JCRUserNode.J_EXTERNAL + "] = 'false'";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                QueryResult qr = q.execute();
                NodeIterator ni = qr.getNodes();
                return ni.hasNext();
            }
            return false;
        } catch (RepositoryException e) {
            logger.error("Error when testing user existence", e);
            return false;
        }
    }

    public void start() throws JahiaInitializationException {
    }

    private void checkRootUserPwd() {
        try {
            String pwd = getNewRootUserPwd();
            if (StringUtils.isNotEmpty(pwd)) {
                logger.info("Resetting root user password");
                lookupRootUser().setPassword(pwd);
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

    public void stop() throws JahiaException {
        // do nothing
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Returns the system root user (not cached).
     *
     * @return the system root user (not cached)
     */
    public JCRUserNode lookupRootUser() {
        try {
            return (JCRUserNode) jcrSessionFactory.login(JahiaLoginModule.getSystemCredentials()).getNodeByIdentifier(JCRUserNode.ROOT_USER_UUID);
        } catch (RepositoryException e1) {
            logger.error(e1.getMessage(), e1);
        }
        return null;
    }

    @Override
    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        checkRootUserPwd();
    }
}
