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

import org.jahia.services.JahiaService;
import org.jahia.services.content.decorator.JCRUserNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * The user manager is responsible to manage all the users in the Jahia
 * environment.
 * blablabla .. sait pas quoi dire ....
 *
 * @author Fulco Houkes
 * @version 2.0
 */
public abstract class JahiaUserManagerProvider extends JahiaService implements InitializingBean, DisposableBean {

    private static Logger logger = LoggerFactory.getLogger(JahiaUserManagerProvider.class);

    private boolean defaultProvider = false;
    private boolean readOnly = false;
    private int priority = 99;
    private String key;

    protected JahiaUserManagerService userManagerService;

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final Pattern INSTANCE = Pattern.compile(org.jahia.settings.SettingsBean.getInstance().lookupString("userManagementUserNamePattern"));
    }

    private static Pattern getUserNamePattern() {
        return Holder.INSTANCE;
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
     *
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
    }

    @Override
    public void destroy() throws Exception {
    }

    /**
     * This is the method that creates a new user in the system, with all the
     * specified attributes.
     *
     * @param password User password
     */
    public abstract JCRUserNode createUser(String name, String password,
                                         Properties properties);

    /**
     * This method removes a user from the system. All the user's attributes are
     * remove, and also all the related objects belonging to the user. On success,
     * true is returned and the user parameter is not longer valid. Return false
     * on any failure.
     *
     * @param user reference on the user to be deleted.
     */
    public abstract boolean deleteUser(String userPath);


    /**
     * Return the number of user in the system.
     *
     * @return Return the number of users in the system.
     */
    public abstract int getNbUsers();

    /**
     * This method return all users' keys in the system.
     *
     * @return Return a List of strings holding the user identification key .
     */
    public abstract List<String> getUserList();

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
     * @return boolean true if the login succeeded, false otherwise
     */
    public abstract boolean login(String userKey, String userPassword) throws RepositoryException;

    /**
     * Validates provided user name against a regular expression pattern,
     * specified in the Jahia configuration.
     *
     * @param name the user name to be validated
     * @return <code>true</code> if the specified user name matches the
     * validation pattern
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
    public abstract JCRUserNode lookupUserByKey(String userKey) throws RepositoryException;

    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created jahiaUser object.
     */
    public abstract JCRUserNode lookupUser(String name) throws RepositoryException;

    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criterias
     * @return Set a set of JahiaUser elements that correspond to those
     * search criterias
     */
    public abstract Set<JCRUserNode> searchUsers(Properties searchCriterias);

    /**
     * This function checks into the system if the username has already been
     * assigned to another user.
     *
     * @return Return true if the specified username has not been assigned yet,
     * return false on any failure.
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
