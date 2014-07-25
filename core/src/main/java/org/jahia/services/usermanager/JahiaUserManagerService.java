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

import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.utils.EncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import java.security.Principal;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * The user manager is responsible to manage all the users in the Jahia
 * environment.
 * The is the general interface that Jahia code uses to offer user management
 * services throughout the product (administration, login, ACL popups, etc..)
 *
 * @author Fulco Houkes
 * @author Khue NGuyen
 * @version 2.0
 */
public class JahiaUserManagerService extends JahiaService {

    public static Logger logger = LoggerFactory.getLogger(JahiaUserManagerService.class);

    /**
     * Guest user unique identification name.
     * Each usermanager should create this special user, who is assigned
     * automatically for each anonymous session internally in Jahia.
     */
    public static final String GUEST_USERNAME = Constants.GUEST_USERNAME;

    public static final String MULTI_CRITERIA_SEARCH_OPERATION = "multi_criteria_search_op";
    public static final String COUNT_LIMIT = "countLimit";

    private JahiaUserSplittingRule userSplittingRule;
    private JCRUserManagerProvider defaultProvider;
// -------------------------- STATIC METHODS --------------------------

    //--------------------------------------------------------------------------
    // FH       29 Mar. 2001
    //          Initial implementation
    //
    /**
     * Encrypt the specified password using the SHA algorithm.
     *
     * @param password String representation of a password.
     *
     * @return Return a String representation of the password encryption. Return
     *         null on any failure.
     */
    public static String encryptPassword (String password) {
        if (StringUtils.isEmpty(password)) {
            return null;
        }

        return EncryptionUtils.sha1DigestLegacy(password);
    }
        
    public static String getKey(Principal p) {
        if (p instanceof JahiaUser) {
            return ((JahiaUser)p).getUserKey();
        } else if (p instanceof JahiaGroup) {
            return ((JahiaGroup)p).getGroupKey();
        } else {
            return p.getName();
        }
    }


    /**
     * Returns <code>true</code> if the specified user is <code>null</code> or a
     * guest user.
     * 
     * @param user
     *            the user to be tested
     * @return <code>true</code> if the specified user is <code>null</code> or a
     *         guest user
     */
    public static boolean isGuest(JCRUserNode user) {
        return user == null || GUEST_USERNAME.equals(user.getName());
    }

    public static boolean isGuest(JahiaUser user) {
        return user == null || GUEST_USERNAME.equals(user.getName());
    }

    /**
     * Returns <code>true</code> if the specified user is not <code>null</code>
     * and is not a guest user.
     * 
     * @param user
     *            the user to be tested
     * @return <code>true</code> if the specified user is not <code>null</code>
     *         and is not a guest user
     */
    public static boolean isNotGuest(String userPath) {
        return userPath != null && !GUEST_USERNAME.equals(userPath);
    }

    public static boolean isNotGuest(JCRUserNode user) {
        return user != null && !GUEST_USERNAME.equals(user.getName());
    }

    public static boolean isNotGuest(JahiaUser user) {
        return user != null && !GUEST_USERNAME.equals(user.getName());
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final JahiaUserManagerService INSTANCE = new JahiaUserManagerService();
    }

    public static JahiaUserManagerService getInstance() {
        return Holder.INSTANCE;
    }

// -------------------------- OTHER METHODS --------------------------

    //-------------------------------------------------------------------------
    /**
     * This is the method that creates a new user in the system, with all the
     * specified properties.
     *
     * @param name   User identification name.
     * @param password   User password
     * @param properties User additional parameters. If the user has no additional
     */
    public JCRUserNode createUser(String name, String password,
                                         Properties properties){
        return defaultProvider.createUser(name, password, properties);
    }


    //-------------------------------------------------------------------------
    /**
     * This method removes a user from the system. All the user's attributes are
     * remove, and also all the related objects belonging to the user. On success,
     * true is returned and the user parameter is not longer valid. Return false
     * on any failure.
     *
     * @param user reference on the user to be deleted.
     */
    public boolean deleteUser (String userPath){
        return defaultProvider.deleteUser(userPath);
    }


    //-------------------------------------------------------------------------
    /**
     * Return the number of user in the system.
     *
     * @return Return the number of users in the system.
     */
    public int getNbUsers ()
            throws JahiaException {
        return defaultProvider.getNbUsers();
    }

    //-------------------------------------------------------------------------
    /**
     * This method return all users' keys in the system.
     *
     * @return Return a List of strings holding the user identification key .
     */
    public List<String> getUserList (){
        return defaultProvider.getUserList();
    }

    //-------------------------------------------------------------------------
    /**
     * This method returns the list of all the user names registered in the system.
     *
     * @return Return a List of strings holding the user identification names.
     */
    public List<String> getUsernameList(){
        return defaultProvider.getUsernameList();
    }


    //-------------------------------------------------------------------------
    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created jahiaUser object.
     */
    public JCRUserNode lookupUserByKey(String userKey) {
        try {
            return defaultProvider.lookupUserByKey(userKey);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }


    //-------------------------------------------------------------------------
    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @param name User's identification name.
     *
     * @return Return a reference on a new created jahiaUser object.
     */
    public JCRUserNode lookupUser(String name) {
        return defaultProvider.lookupUser(name);
    }


    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criterias
     *
     * @return List a List of JahiaUser elements that correspond to those
     *         search criterias
     */
    public Set<JCRUserNode> searchUsers (Properties searchCriterias){
        return defaultProvider.searchUsers(searchCriterias);
    }

    //-------------------------------------------------------------------------
    /**
     * This function checks into the system if the username has already been
     * assigned to another user.
     *
     * @param name User login name.
     *
     * @return Return true if the specified username has not been assigned yet,
     *         return false on any failure.
     */
    public boolean userExists(String name){
        return defaultProvider.userExists(name);
    }

    /**
     * Validates provided user name against a regular expression pattern, specified in the Jahia configuration.
     * 
     * @param name
     *            the user name to be validated
     * @return <code>true</code> if the specified user name matches the validation pattern
     */
    public boolean isUsernameSyntaxCorrect(String name){
        return defaultProvider.isUsernameSyntaxCorrect(name);
    }

    public void setUserSplittingRule(JahiaUserSplittingRule userSplittingRule) {
        this.userSplittingRule = userSplittingRule;
    }

    public JahiaUserSplittingRule getUserSplittingRule() {
        return userSplittingRule;
    }

    @Override
    public void start() throws JahiaInitializationException {

    }

    @Override
    public void stop() throws JahiaException {

    }

    public void setDefaultProvider(JCRUserManagerProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }
}
