/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.services.JahiaService;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class is keep to maintain compatibility with olds users/groups providers
 * @author kevan
 * @deprecated
 */
@Deprecated
public abstract class JahiaUserManagerProvider extends JahiaService implements InitializingBean, DisposableBean {

    private static class PatternHolder {
        static final Pattern pattern = Pattern.compile(SettingsBean.getInstance().lookupString("userManagementUserNamePattern"));
    }

    private static final Logger logger = LoggerFactory.getLogger(JahiaUserManagerProvider.class);

    private boolean defaultProvider = false;
    private boolean readOnly = false;
    private int priority = 99;
    private String key;

    protected JahiaUserManagerService userManagerService;

    private static Pattern getUserNamePattern() {
        return PatternHolder.pattern;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isDefaultProvider() {
        return defaultProvider;
    }

    /**
     * There can be only one... default provider
     * @param defaultProvider
     */
    public void setDefaultProvider(boolean defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public void afterPropertiesSet() {
        if (userManagerService != null) {
            userManagerService.registerProvider(this);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (userManagerService != null) {
            userManagerService.unregisterProvider(this);
        }
    }

    /**
     * This is the method that creates a new user in the system, with all the
     * specified attributes.
     *
     * @param password   User password
     */
    public abstract JahiaUser createUser(String name, String password,
                                         Properties properties);

    /**
     * This method removes a user from the system. All the user's attributes are
     * remove, and also all the related objects belonging to the user. On success,
     * true is returned and the user parameter is not longer valid. Return false
     * on any failure.
     *
     * @param user reference on the user to be deleted.
     */
    public abstract boolean deleteUser (JahiaUser user);


    /**
     * Return the number of user in the system.
     *
     * @return Return the number of users in the system.
     */
    public abstract int getNbUsers ();

    /**
     * This method return all users' keys in the system.
     *
     * @return Return a List of strings holding the user identification key .
     */
    public abstract List<String> getUserList ();

    /**
     * This method returns the list of all the user names registed into the system.
     *
     * @return Return a List of strings holding the user identification names.
     */
    public abstract List<String> getUsernameList();

    /**
     * Performs a login of the specified user.
     *
     * @param userKey      the user identifier defined in this service properties
     * @param userPassword the password of the user
     *
     * @return boolean true if the login succeeded, false otherwise
     */
    public abstract boolean login (String userKey, String userPassword);

    /**
     * Validates provided user name against a regular expression pattern,
     * specified in the Jahia configuration.
     *
     * @param name
     *            the user name to be validated
     * @return <code>true</code> if the specified user name matches the
     *         validation pattern
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

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created jahiaUser object.
     */
    public abstract JahiaUser lookupUserByKey(String userKey);

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created jahiaUser object.
     */
    public abstract JahiaUser lookupUser(String name);

    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criterias
     *
     * @return Set a set of JahiaUser elements that correspond to those
     *         search criterias
     */
    public abstract Set<JahiaUser> searchUsers (Properties searchCriterias);

    /**
     * This method indicates that any internal cache for a provider should be
     * updated because the value has changed and needs to be transmitted to the
     * other nodes in a clustering environment.
     * @param jahiaUser JahiaUser the group to be updated in the cache.
     */
    public abstract void updateCache(JahiaUser jahiaUser);

    /**
     * This function checks into the system if the username has already been
     * assigned to another user.
     *
     * @return Return true if the specified username has not been assigned yet,
     *         return false on any failure.
     */
    public abstract boolean userExists(String name);

    /**
     * Returns an instance of the user manager.
     *
     * @return an instance of the user manager
     */
    protected JahiaUserManagerService getUserManagerService() {
        return userManagerService;
    }

    /**
     * Injects the user management service instance.
     *
     * @param userManagerService an instance of the user management service
     */
    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }
}
