/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.usermanager;

import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.utils.EncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;

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
public abstract class JahiaUserManagerService extends JahiaService {

    public static Logger LOGGER = LoggerFactory.getLogger(JahiaUserManagerService.class);

    /**
     * Guest user unique identification name.
     * Each usermanager should create this special user, who is assigned
     * automatically for each anonymous session internally in Jahia.
     */
    public static final String GUEST_USERNAME = Constants.GUEST_USERNAME;

    private JahiaUserSplittingRule userSplittingRule;
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
    public static boolean isGuest(JahiaUser user) {
        return user == null || GUEST_USERNAME.equals(user.getUsername());
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
    public static boolean isNotGuest(JahiaUser user) {
        return user != null && !GUEST_USERNAME.equals(user.getUsername());
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
    public abstract JahiaUser createUser(String name, String password,
                                         Properties properties);


    //-------------------------------------------------------------------------
    /**
     * This method removes a user from the system. All the user's attributes are
     * remove, and also all the related objects belonging to the user. On success,
     * true is returned and the user parameter is not longer valid. Return false
     * on any failure.
     *
     * @param user reference on the user to be deleted.
     */
    public abstract boolean deleteUser (JahiaUser user);


    //-------------------------------------------------------------------------
    /**
     * Return the number of user in the system.
     *
     * @return Return the number of users in the system.
     */
    public abstract int getNbUsers ()
            throws JahiaException;

    /**
     * Returns a List of {@link JahiaUserManagerProvider} objects, describing the
     * available user management providers
     *
     * @return result a List of {@link JahiaUserManagerProvider} objects that describe
     *         the providers. This will never be null but may be empty if no providers
     *         are available.
     */
    public abstract List<? extends JahiaUserManagerProvider> getProviderList ();

    /**
     * Returns a {@link JahiaUserManagerProvider} for the specified name.
     *
     * @return a {@link JahiaUserManagerProvider} for the specified name
     */
    public abstract JahiaUserManagerProvider getProvider(String name);

    //-------------------------------------------------------------------------
    /**
     * This method return all users' keys in the system.
     *
     * @return Return a List of strings holding the user identification key .
     */
    public abstract List<String> getUserList ();

    public abstract List<String> getUserList (String provider);

    //-------------------------------------------------------------------------
    /**
     * This method returns the list of all the user names registered in the system.
     *
     * @return Return a List of strings holding the user identification names.
     */
    public abstract List<String> getUsernameList();


    //-------------------------------------------------------------------------
    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @return Return a reference on a new created jahiaUser object.
     */
    public abstract JahiaUser lookupUserByKey(String userKey);


    //-------------------------------------------------------------------------
    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @param name User's identification name.
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
     * @return List a List of JahiaUser elements that correspond to those
     *         search criterias
     */
    public abstract Set<Principal> searchUsers (Properties searchCriterias);

    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param providerKey     key of the provider in which to search, may be
     *                        obtained by calling getProviderList()
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criterias
     *
     * @return Set a set of JahiaUser elements that correspond to those
     *         search criterias
     */
    public abstract Set<Principal> searchUsers (String providerKey, Properties searchCriterias);

    /**
     * This method indicates that any internal cache for a provider should be
     * updated because the value has changed and needs to be transmitted to the
     * other nodes in a clustering environment.
     * @param jahiaUser JahiaGroup the group to be updated in the cache.
     */
    public abstract void updateCache(JahiaUser jahiaUser);


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
    public abstract boolean userExists(String name);

    /**
	 * Validates provided user name against a regular expression pattern,
	 * specified in the Jahia configuration.
	 * 
	 * @param name
	 *            the user name to be validated
	 * @return <code>true</code> if the specified user name matches the
	 *         validation pattern
	 */
	public abstract boolean isUsernameSyntaxCorrect(String name);

	/**
	 * Adds the specified user provider to the registry.
	 * 
	 * @param jahiaUserManagerProvider
	 *            an instance of the user provider to register
	 */
	public abstract void registerProvider(JahiaUserManagerProvider jahiaUserManagerProvider);

    public void setUserSplittingRule(JahiaUserSplittingRule userSplittingRule) {
        this.userSplittingRule = userSplittingRule;
    }

    public JahiaUserSplittingRule getUserSplittingRule() {
        return userSplittingRule;
    }
}
