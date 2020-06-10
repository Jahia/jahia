package org.jahia.api.usermanager;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.pwd.PasswordService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerProvider;
import org.jahia.services.usermanager.JahiaUserSplittingRule;
import org.jahia.utils.EncryptionUtils;

import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * The user manager is responsible to manage all the users in the Jahia
 * environment.
 * The is the general interface that Jahia code uses to offer user management
 * services throughout the product (administration, login, ACL popups, etc..)
 */
public interface JahiaUserManagerService {

    JahiaUserSplittingRule getUserSplittingRule();

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created JCRUserNode object.
     * @deprecated use lookupUserByPath() instead
     */
    JCRUserNode lookupUserByKey(String userKey);

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created JCRUserNode object.
     */
    JCRUserNode lookupUserByPath(String path);

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created JCRUserNode object.
     */
    JCRUserNode lookupUserByPath(String userKey, JCRSessionWrapper session);

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @param name User's identification name.
     * @return Return a reference on a new created JCRUserNode object.
     */
    JCRUserNode lookupUser(String name);

    JCRUserNode lookupUser(String name, String site);

    JCRUserNode lookupUser(String name, String site, boolean checkSiteAndGlobalUsers);

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @param name User's identification name.
     * @return Return a reference on a new created JCRUserNode object.
     */
    JCRUserNode lookupUser(String name, JCRSessionWrapper session);

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @param name User's identification name.
     * @return Return a reference on a new created JCRUserNode object.
     */
    JCRUserNode lookupUser(String name, String site, JCRSessionWrapper session);

    String getUserPath(String name);

    String getUserPath(String name, String site);

    /**
     * This function checks into the system if the username has already been
     * assigned to another user.
     *
     * @param name User login name.
     * @return Return true if the specified username has not been assigned yet,
     * return false on any failure.
     */
    boolean userExists(String name);

    boolean userExists(String name, String siteKey);

    /**
     * Returns the system root user name (cached)
     *
     * @return the system root user name (cached)
     */
    String getRootUserName();

    /**
     * Returns the system root user (not cached).
     *
     * @return the system root user (not cached)
     */
    JCRUserNode lookupRootUser();

    /**
     * Returns the system root user (not cached).
     *
     * @return the system root user (not cached)
     */
    JCRUserNode lookupRootUser(JCRSessionWrapper session);

    /**
     * This method return all users' keys in the system.
     *
     * @return Return a List of strings holding the user identification key .
     */
    List<String> getUserList();

    List<String> getUserList(String siteKey);

    /**
     * This method returns the list of all the user names registered in the system.
     *
     * @return Return a List of strings holding the user identification names.
     */
    List<String> getUsernameList();

    /**
     * This method returns the list of all the user names registered in the system.
     *
     * @return Return a List of strings holding the user identification names.
     */
    List<String> getUsernameList(String siteKey);

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
    Set<JCRUserNode> searchUsers(Properties searchCriterias);

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
    Set<JCRUserNode> searchUsers(Properties searchCriterias, String[] providers);

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
    Set<JCRUserNode> searchUsers(Properties searchCriterias, JCRSessionWrapper session);

    Set<JCRUserNode> searchUsers(Properties searchCriterias, String[] providerKeys, JCRSessionWrapper session);

    Set<JCRUserNode> searchUsers(Properties searchCriterias, String siteKey, String[] providerKeys, JCRSessionWrapper session);

    Set<JCRUserNode> searchUsers(Properties searchCriterias, String siteKey, String[] providerKeys, boolean excludeProtected, JCRSessionWrapper session);

    Set<JCRUserNode> searchUsers(Properties searchCriterias, String siteKey, String providerKey, boolean excludeProtected, JCRSessionWrapper session);

    /**
     * Provide the list of all available users providers
     *
     * @param session the session used
     * @return list of JCRStoreProvider
     */
    List<JCRStoreProvider> getProviderList(JCRSessionWrapper session);

    List<JCRStoreProvider> getProviderList(String siteKey, JCRSessionWrapper session);

    /**
     * Provide the list of users providers matching given keys
     *
     * @param providerKeys the keys
     * @param session      the session used
     * @return list of JCRStoreProvider
     */
    List<JCRStoreProvider> getProviders(String[] providerKeys, JCRSessionWrapper session);

    List<JCRStoreProvider> getProviders(String siteKey, String[] providerKeys, JCRSessionWrapper session);

    /**
     * Retrieve the user provider corresponding to a given key
     *
     * @param providerKey the key
     * @param session     the session used
     * @return JCRStoreProvider if it exist or null
     */
    JCRStoreProvider getProvider(String providerKey, JCRSessionWrapper session);

    JCRStoreProvider getProvider(String siteKey, String providerKey, JCRSessionWrapper session);

    /**
     * This is the method that creates a new user in the system, with all the
     * specified properties.
     *
     * @param name       User identification name.
     * @param password   User password
     * @param properties User additional parameters. If the user has no additional
     * @param session
     */
    JCRUserNode createUser(String name, String password, Properties properties, JCRSessionWrapper session);

    /**
     * This is the method that creates a new user in the system, with all the
     * specified properties.
     *
     * @param name       User identification name.
     * @param password   User password
     * @param properties User additional parameters. If the user has no additional
     * @param session
     */
    JCRUserNode createUser(String name, String siteKey, String password, Properties properties, JCRSessionWrapper session);

    /**
     * This method removes a user from the system. All the user's attributes are
     * remove, and also all the related objects belonging to the user. On success,
     * true is returned and the user parameter is not longer valid. Return false
     * on any failure.
     *
     * @param userPath reference on the user to be deleted.
     * @param session
     */
    boolean deleteUser(String userPath, JCRSessionWrapper session);

    /**
     * Validates provided user name against a regular expression pattern, specified in the Jahia configuration.
     *
     * @param name the user name to be validated
     * @return <code>true</code> if the specified user name matches the validation pattern
     */
    boolean isUsernameSyntaxCorrect(String name);

    /**
     * get the list of olds users providers
     * this method is used to maintain compatibility with old users providers
     *
     * @deprecated
     */
    @Deprecated
    List<? extends JahiaUserManagerProvider> getProviderList();

    /**
     * get the provider corresponding to a given key
     * this method is used to maintain compatibility with old users providers
     *
     * @deprecated
     */
    @Deprecated
    JahiaUserManagerProvider getProvider(String key);

    /**
     * register a user provider
     * this method is used to maintain compatibility with old users providers
     *
     * @deprecated
     */
    @Deprecated
    void registerProvider(JahiaUserManagerProvider jahiaUserManagerProvider);

    /**
     * Performs the lookup of the user, detecting the type of the provided lookup key (either a JCR path, a user name or a legacy user key).
     *
     * @param lookupKey
     *            the identifier to lookup the user (can be a JCR path, user name or legacy user key)
     * @return the corresponding user node or null if no user can be found for the specified key
     */
    JCRUserNode lookup(String lookupKey);
}
