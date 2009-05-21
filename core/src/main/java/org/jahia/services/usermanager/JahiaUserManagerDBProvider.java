/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.usermanager;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaUserManager;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.utils.JahiaTools;

import java.util.*;


/**
 * @author Fulco Houkes
 * @version 2.0
 */
public class JahiaUserManagerDBProvider extends JahiaUserManagerProvider implements
        UsersDBInterface {
// ------------------------------ FIELDS ------------------------------

    public static final String USERNAME_PROPERTY_NAME = "username";

    // the DB User cache name.
    public static final String DB_USER_CACHE = "DBUsersCache";

    /** the overall provider User cache name. */
    public static final String PROVIDERS_USER_CACHE = "ProvidersUsersCache";

    public static final String PROVIDER_NAME = "jahia";

    /** Root user unique identification number */
    public static final int ROOT_USER_ID = 0;

    private static Logger logger = Logger
	        .getLogger(JahiaUserManagerDBProvider.class);

    private static JahiaUserManagerDBProvider mUserManagerDBService;

    private Cache mProvidersUserCache;

    private JahiaACLManagerService aclService = null;

    private JahiaUserManager userManager = null;

    private CacheService cacheService;

// -------------------------- STATIC METHODS --------------------------

    //--------------------------------------------------------------------------
    /**
     * Create an new instance of the User Manager Service if the instance do not
     * exist, or return the existing instance.
     *
     * @return Return the instance of the User Manager Service.
     */
    public static JahiaUserManagerDBProvider getInstance () {
        if (mUserManagerDBService == null) {
            try {
                mUserManagerDBService = new JahiaUserManagerDBProvider ();
            } catch (JahiaException ex) {
                logger.error (
                        "Could not create an instance of the JahiaUserManagerRoutingService class",
                        ex);
            }
        }
        return mUserManagerDBService;
    }

// --------------------------- CONSTRUCTORS ---------------------------

    //--------------------------------------------------------------------------
    /**
     * Default constructor
     *
     * @throws JahiaException The user manager need some Jahia services to be
     *                        able to run correctly. If one of these services are not instanciated then a
     *                        JahiaException exception is thrown.
     */
    protected JahiaUserManagerDBProvider () throws JahiaException {
        /* Modified by EP to use caches correctly
        mUserCache = new HashMap();*/
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setAclService(JahiaACLManagerService aclService) {
        this.aclService = aclService;
    }

    public void setUserManager(JahiaUserManager userManager) {
        this.userManager = userManager;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    // -------------------------- OTHER METHODS --------------------------

    public void start()
            throws JahiaInitializationException {
        mProvidersUserCache = cacheService.createCacheInstance(PROVIDERS_USER_CACHE);
    }

    public void stop() {}

    //--------------------------------------------------------------------------
    /**
     * This is the method that creates a new user in the system, with all the
     * specified attributes.
     *
     * @param name       User identification name.
     * @param password   User password
     * @param properties User additional properties. If the user has no additional
     */
    public synchronized JahiaUser createUser(String name, String password,
                                             Properties properties) {
        if (!isUsernameSyntaxCorrect(name)) {
            return null;
        }

        // try to avoid a NullPointerException
        if (password == null) {
            return null;
        }

        if (!password.startsWith("SHA-1:")) {
            // Encrypt the password
            password = encryptPassword (password);
        } else {
            password = password.substring(6);
        }


        // Check first if the user already exists in the database.
        if (userExists (name)) {
            return null;
        }

        if (password == null) {
            logger.error ("could not encrypt the user password.");
            return null;
        }

        if (properties == null)
        	properties = new Properties();

        // remember the password history
        properties.setProperty(JahiaDBUser.PWD_HISTORY_PREFIX
		        + System.currentTimeMillis(), password);
        
        // Create the user
        JahiaDBUser user = null;
        if (properties != null) {
            user = new JahiaDBUser(0, name, password, "{"+getKey()+"}"+name,
                    new UserProperties(properties, false));
        } else {
            user = new JahiaDBUser(0, name, password, "{"+getKey()+"}"+name,
                    null);
        }
        userManager.createUser(user, password);

        logger.debug("User [" + name + "] was added into the database and in the cache");

        return user;
    }

    /**
	 * This function checks into the system if the name has already been
	 * assigned to another user.
	 * 
	 * @param name
	 *            User login name.
	 * @return Return true if the specified name has not been assigned yet,
	 *         return false on any failure.
	 */
    public boolean userExists(String name) {
        // try to avoid a NullPointerException
        if (name == null) {
            return false;
        }

        // name should not be empty.
        if (name.length () == 0) {
            return false;
        }

        return (lookupUser (name) != null);
    }

    //--------------------------------------------------------------------------
    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @param name User's identification name.
     *
     * @return Return a reference on a new created jahiaUser object.
     *
     */
     public JahiaUser lookupUser(String name) {
        // try to avoid a NullPointerException
        if (!isUsernameSyntaxCorrect(name)) {
            return null;
        }

        JahiaUser user = (JahiaUser)mProvidersUserCache.get ("n"+name);

        if (user == null) {
            user = userManager.findJahiaUserByUsername(name);
            if (user != null) {
                /* 2004-16-06 : update by EP
                new cache to populate : cross providers only based upon names... */
                mProvidersUserCache.put("k"+user.getUserKey(), user);

                mProvidersUserCache.put("n"+user.getUsername(), user);
            }
        }

        return user;
    }

    //--------------------------------------------------------------------------
    /**
     * This method removes a user from the system. All the user's attributes are
     * remove, and also all the related objects belonging to the user. On success,
     * true is returned and the user parameter is not longer valid. Return false
     * on any failure.
     *
     * @param user reference on the user to be deleted.
     *
     * @return Return true on success, or false on any failure.
     */
    public synchronized boolean deleteUser (JahiaUser user) {
        logger.debug ("Started");
        // try to avoid a NullPointerException
        if (user == null) {
            return false;
        }

        /*
        // cannot delete the root and guest user
        if ((((JahiaDBUser)user).getID() == ROOT_USER_ID) ||
            (((JahiaDBUser)user).getID() == GUEST_USER_ID)) {
            return false;
        }
        */
        // cannot delete the root user
        if (((JahiaDBUser) user).getID () == ROOT_USER_ID) {
            return false;
        }

        // this part has to be synchronized in order to avoid a thread to read invalid data.
        boolean result = false;

        // remove the user from the database.
        if (userManager.deleteUser(user)) {
            logger.debug("User removed from db");

            mProvidersUserCache.remove ("k"+user.getUserKey ());
            mProvidersUserCache.remove ("n"+user.getUsername ());

            ServicesRegistry.getInstance().getJahiaGroupManagerService().removeUserFromAllGroups(user);

            logger.debug("User removed from groups");

            aclService.removeUserFromAllACLs(user);

            logger.debug("User removed from acls");

            // and now ... KILL THE VICTIM !!!!!!!
            user = null;
            result = true;
        }
        return result;
    }

    //-------------------------------------------------------------------------
    // FH   2 May 2001
    /**
     * Return the amount of users in the database.
     *
     * @return The amount of users.
     */
    public synchronized int getNbUsers () {
        return userManager.getNumberOfUsers();
    }

    //-------------------------------------------------------------------------
    /**
     * This method return all users' keys in the system.
     *
     * @return Return a List of strings holding the user identification key .
     */
    public List<String> getUserList () {
        return userManager.getUserkeyList();
    }

    //--------------------------------------------------------------------------
    /**
     * This method returns the list of all the user names registed into the system.
     *
     * @return Return a List of strings holding the user identification names.
     */
    public List getUsernameList() {
        return userManager.getUsernameList();
    }

    /**
     * Performs a login of the specified user.
     * @param userID       the user identifier defined in this service properties
     * @param userPassword the password of the user
     *
     * @return boolean true if the login succeeded, false otherwise
     */
    public boolean login (String userID, String userPassword) {
    	if (userManager.findJahiaUserByUserKey(userID) != null) {
            return true; // not necessary in this implementation.
        } else {
            return false;
        }
    }

	/**
	 * ma m�thode
	 */
	public JahiaUser lookupUserByKey(String userKey, String searchAttributeName) {
		return lookupUserByKey(userKey);
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
     * @return Set a set of JahiaUser elements that correspond to those
     *         search criterias
     *
     * @todo this code could be cleaner if username was a real user property
     * but as it isn't we have to do a lot of custom handling.
     */
    public Set searchUsers (int siteID, Properties searchCriterias) {
        Set result = new HashSet();
        Set userKeys = new HashSet();

        if (searchCriterias == null) {
            searchCriterias = new Properties();
            searchCriterias.setProperty("*", "*");
        }

        boolean haveWildCardProperty = false;
        if (searchCriterias.getProperty("*") != null) {
            haveWildCardProperty = true;
        }

        boolean onlyGroupNameInSelect = false;
        if ((searchCriterias.getProperty(USERNAME_PROPERTY_NAME) != null) ||
            (haveWildCardProperty)) {
            String curCriteriaValue;
            if (haveWildCardProperty) {
                curCriteriaValue = makeLIKEString(searchCriterias.
                                                  getProperty("*"));
            } else {
                curCriteriaValue = makeLIKEString(searchCriterias.
                                                  getProperty(USERNAME_PROPERTY_NAME));
            }
            userManager.searchUserName(curCriteriaValue, userKeys);
            if ((!haveWildCardProperty) && (searchCriterias.size() == 1)) {
                onlyGroupNameInSelect = true;
            }
        }

        if (!onlyGroupNameInSelect) {
            Iterator criteriaNames = searchCriterias.keySet().iterator();
            List criteriaValueList = new ArrayList(searchCriterias.size());
            List criteriaNameList = new ArrayList(searchCriterias.size());
            while (criteriaNames.hasNext()) {
                String curCriteriaName = (String) criteriaNames.next();
                String curCriteriaValue = makeLIKEString(searchCriterias.getProperty(curCriteriaName));
                criteriaValueList.add(curCriteriaValue);
                criteriaNameList.add(curCriteriaName);
                if ("*".equals(curCriteriaName)) {
                    // we must look in all columns, including special for
                    // the user.
                    onlyGroupNameInSelect = false;
                } else {
                    if (USERNAME_PROPERTY_NAME.equals(curCriteriaName)) {
                        // group name filter is a special case and is not
                        // stored in the property table.
                    } else {
                        onlyGroupNameInSelect = false;
                    }
                }
            }
            userManager.searchUserName(criteriaNameList,criteriaValueList, userKeys, PROVIDER_NAME);
        }

        // now that we have all the keys, let's load all the users.
        Iterator userKeyEnum = userKeys.iterator ();
        while (userKeyEnum.hasNext ()) {
            String curUserKey = (String) userKeyEnum.next ();
            JahiaUser user = lookupUserByKey(curUserKey);
            if (user == null) continue;
            if (siteID == 0) {
                result.add (user);
            } else {
                JahiaUser member = ServicesRegistry.getInstance().getJahiaSiteUserManagerService().getMember(siteID, user.getUsername());
                if (member != null && member.getUserKey().equals(user.getUserKey())) {
                    result.add (user);
                }
            }
        }

        return result;
    }

  /**
     * Transforms a search with "*" characters into a valid LIKE statement
     * with "%" characters. Also escapes the string to remove all "'" and
     * other chars that might disturb the request construct.
     *
     * @param input the original String
     *
     * @return String a resulting string that has
     */
    private String makeLIKEString (String input) {
        String result = JahiaTools.replacePattern (input, "*", "%");
        result = JahiaTools.replacePattern (result, "'", "\\'");
        result = JahiaTools.replacePattern (result, "\"", "\\\"");
        result = JahiaTools.replacePattern (result, "_", "\\_");
        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @param userKey User's identification key.
     *
     * @return Return a reference on a new created jahiaUser object.
     */
    public JahiaUser lookupUserByKey(String userKey) {
        if (!userKey.startsWith("{"+getKey()+"}") && !userKey.endsWith(":0")) {
            return null;
        }
        JahiaUser user = (JahiaUser)mProvidersUserCache.get ("k"+userKey);
        if (user == null) {
            user = userManager.findJahiaUserByUserKey(userKey);
            if (user != null) {
                mProvidersUserCache.put("k"+user.getUserKey(), user);
                mProvidersUserCache.put("n"+user.getUsername(), user);
            }
        }
        return user;
    }

    public void updateCache(JahiaUser jahiaUser) {
        mProvidersUserCache.put("k"+jahiaUser.getUserKey(), jahiaUser);
        mProvidersUserCache.put("n"+jahiaUser.getUsername(), jahiaUser);
    }
}

