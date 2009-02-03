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

//
// NK 12.04.2001 - Changed to support Multi Site
package org.jahia.services.usermanager;

import org.jahia.hibernate.manager.JahiaUserManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;

import java.io.Serializable;
import java.util.*;

/**
 * A JahiaUser represents a physical person who is defined by a username and a password for authentification purpose. Every other property
 * of a JahiaUser is stored in it's properties list, which hold key-value string pairs. For example email, firstname, lastname, ...
 * information should be stored in this properties list.
 * 
 * @author Fulco Houkes
 * @version 1.1
 */
public class JahiaDBUser implements JahiaUser, Serializable {

    private static final long serialVersionUID = 1297820203021879123L;

    public static final String PWD_HISTORY_PREFIX = "password.history.";

    private static final int PWD_HISTORY_MAX_ENTRIES = 10;

    public static final String PROP_LAST_LOGIN_DATE = "lastLoginDate";

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(JahiaDBUser.class);
    /** User unique identification number in the database. */
    private int mID;

    /** User unique identification name */
    private String mUsername;

    /** User password */
    private String mPassword;

    /** Each user has an unique String identifier * */
    private String mUserKey;

    /** Site id , the owner of this user */
    private int mSiteID = -1;

    /** User home page property * */
    private static final String mHOMEPAGE_PROP = "user_homepage";

    // language property constants
    private static final String mLANGUAGES_ORDER_PROP = "language_codes";
    private static final String mLANGUAGES_ORDER_PROP_SEPARATOR = ",";
    private static final String mLANGUAGES_MIX_PROP = "langage_mix";
    private static final String mLANGUAGES_ONLYUSER_PROP = "language_onlyuser";

    /** User additional parameters. */
    private UserProperties mProperties = new UserProperties();

    private boolean proxied;
    private boolean bypassUserAliasing;

    // --------------------------------------------------------------------------
    // FH 29 Mar. 2001
    // Initial implementation
    //
    /**
     * Create a new JahiaDBUser class instance. The passed in password must already be encrypted, no ecryption will be done. If the passed
     * in properties is null, then the user will have no additional parameter than it's id, name and password.
     * 
     * @param id
     *            User unique identification number.
     * @param name
     *            User identification name.
     * @param password
     *            User password.
     * @param userKey
     *            The user Key
     *@param properties
     *            User properties.
     */
    public JahiaDBUser(int id, String name, String password, String userKey,
            UserProperties properties) {
        mID = id;
        mUsername = name;
        mPassword = password;
        mUserKey = userKey;

        if (properties != null) {
            mProperties = properties;
        }
    }

    // --------------------------------------------------------------------------
    // FH 29 Mar. 2001
    // Initial implementation
    //
    public boolean equals(Object another) {
        return this == another || another != null
                && this.getClass() == another.getClass()
                && (getUserKey().equals(((JahiaUser) another).getUserKey()));

    }

    // --------------------------------------------------------------------------
    // FH 29 Mar. 2001
    // Initial implementation
    //
    /**
     * Retrieve the user's unique database identification number.
     * 
     * @return The user unique identification number.
     */
    public int getID() {
        return mID;
    }

    // --------------------------------------------------------------------------
    // FH 29 Mar. 2001
    // Inital implementation
    //
    public String getName() {
        return getUsername();
    }

    // --------------------------------------------------------------------------
    // FH 29 Mar. 2001
    // Initial implementation
    //
    public String getUsername() {
        return mUsername;
    }

    // --------------------------------------------------------------------------
    // NK 12 Avr. 2001
    // Initial implementation
    //
    public String getUserKey() {
        return mUserKey;
    }

    // --------------------------------------------------------------------------
    // NK 12 Avr. 2001
    // Initial implementation
    //
    public int getSiteID() {
        return mSiteID;
    }

    // -------------------------------------------------------------------------
    /**
     * Returns the user's home page id. -1 : undefined
     * 
     * @return int The user homepage id.
     */
    public int getHomepageID() {

        if (mProperties != null) {

            try {
                String value = mProperties.getProperty(mHOMEPAGE_PROP);
                if (value == null)
                    return -1;
                return Integer.parseInt(value);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return -1;
    }

    // -------------------------------------------------------------------------
    /**
     * Set the home page id.
     * 
     * @param id
     *            The user homepage id.
     * 
     * @return false on error
     */
    public boolean setHomepageID(int id) {

        /*
         * if ( !removeProperty(mHOMEPAGE_PROP) ) return false;
         */
        return setProperty(mHOMEPAGE_PROP, String.valueOf(id));
    }

    // --------------------------------------------------------------------------
    // FH 29 Mar. 2001
    // Initial implementation
    //
    /**
     * @deprecated use getUserProperties() instead
     * @return Properties the properties returned here should NEVER be modified, only modifications through setProperty() are supported and
     *         serialized.
     */
    public Properties getProperties() {
        if (mProperties != null) {
            return mProperties.getProperties();
        } else {
            return null;
        }
    }

    /**
     * The properties here should not be modified, as the modifications will not be serialized. Use the setProperty() method to modify a
     * user's properties.
     * 
     * @return UserProperties
     */
    public UserProperties getUserProperties() {
        return mProperties;
    }

    // --------------------------------------------------------------------------
    // FH 29 Mar. 2001
    // Initial implementation
    //
    public String getProperty(String key) {

        if ((mProperties != null) && (key != null)) {
            return mProperties.getProperty(key);
        }
        return null;
    }

    public UserProperty getUserProperty(String key) {
        if ((mProperties != null) && (key != null)) {
            return mProperties.getUserProperty(key);
        }
        return null;
    }

    // --------------------------------------------------------------------------
    // FH 29 Mar. 2001
    // Initial implementation
    //
    /**
     * Return a unique hashcode identifiying the user.
     * 
     * @return Return a valid hashcode integer of the user, On failure, -1 is returned.
     */
    public int hashCode() {
        return ("user" + mID).hashCode();
    }

    // --------------------------------------------------------------------------
    // FH 29 Mar. 2001
    // Initial implementation
    //
    /**
     * Remove the specified property from the properties list.
     * 
     * @param key
     *            Property's name.
     * 
     * @return Return true on success or false on any failure.
     */
    public synchronized boolean removeProperty(String key) {
        boolean result = false;

        if (mProperties == null) {
            return result;
        }

        if ((key != null) && (key.length() > 0)
                && (!mProperties.isReadOnly(key))) {
            JahiaUserManager userManager = (JahiaUserManager) SpringContextSingleton
                    .getInstance().getContext().getBean(
                            JahiaUserManager.class.getName());
            result = userManager.removeProperty(key, this.getID(),
                    getProviderName(), getUserKey());
            if (result) {
                mProperties.removeUserProperty(key);
            }
            ServicesRegistry.getInstance().getJahiaUserManagerService()
                    .updateCache(this);
        }
        return result;
    }

    public boolean isPasswordReadOnly() {
        return false;
    }

    // ---------------------------------------------------------------------------
    // FH 29 Mar. 2001
    // Initial implementation
    //
    /**
     * Change the user's password.
     * 
     * @param password
     *            New user's password
     * 
     * @return Return true id the old password is the same as the current one and the new password is valid. Return false on any failure.
     */
    public synchronized boolean setPassword(String password) {
        boolean result = false;

        // try to avoid a NullPointerException
        if (password != null) {
            // password should not be empty.
            if (password.length() > 0) {
                // Encrypt the new password
                String tmp;
                if (password.startsWith("SHA-1:")) {
                    tmp = password.substring(6);
                } else {
                    tmp = JahiaUserManagerService.encryptPassword(password);
                }
                JahiaUserManager userManager = (JahiaUserManager) SpringContextSingleton
                        .getInstance().getContext().getBean(
                                JahiaUserManager.class.getName());
                result = userManager.setPassword(tmp, mID);
                if (result) {
                    mPassword = tmp;
                    updatePasswordHistory(tmp);
                }
            }
        }
        return result;
    }

    /**
     * Returns the password history as user properties list, sorted by change date descending, i.e. the newer passwords are at the top of
     * the list.
     * 
     * @return the password history as user properties list, sorted by change date descending, i.e. the newer passwords are at the top of
     *         the list
     */
    public List<UserProperty> getPasswordHistory() {
        List<UserProperty> pwdHistory = Collections.emptyList();
        List<String> pwdHistoryNames = new LinkedList<String>();
        final Iterator<String> iterator = getUserProperties().propertyNameIterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            if (name.startsWith(PWD_HISTORY_PREFIX)) {
                pwdHistoryNames.add(name);
            }
        }
        if (pwdHistoryNames.size() > 0) {
            Collections.sort(pwdHistoryNames, new Comparator<String>() {
                public int compare(String arg1, String arg2) {
                    return arg2.compareTo(arg1);
                }
            });
            pwdHistory = new LinkedList<UserProperty>();
        }

        for (Object pwdHistoryName : pwdHistoryNames) {
            pwdHistory.add(getUserProperty((String) pwdHistoryName));
        }

        return pwdHistory;
    }

    private void updatePasswordHistory(String newPassword) {
        setProperty(PWD_HISTORY_PREFIX + System.currentTimeMillis(),
                newPassword);
        List<UserProperty> pwdHistory = getPasswordHistory();
        if (pwdHistory.size() > PWD_HISTORY_MAX_ENTRIES) {
            while (pwdHistory.size() > PWD_HISTORY_MAX_ENTRIES) {
                String entryName = ((UserProperty) pwdHistory.get(pwdHistory
                        .size() - 1)).getName();
                removeProperty(entryName);
                logger
                        .debug("Removing old password history entry "
                                + entryName);
                pwdHistory.remove(pwdHistory.size() - 1);
            }
        }
    }

    /**
     * Gets the timstamp of the last password change if available. Otherwise returns <code>0</code>.
     * 
     * @return the timstamp of the last password change if available. Otherwise returns <code>0</code>
     */
    public long getLastPasswordChangeTimestamp() {
        long timestamp = 0;
        List<UserProperty> pwdHistory = getPasswordHistory();

        if (pwdHistory.size() > 0) {
            String propValue = ((UserProperty) pwdHistory.get(0)).getName();
            try {
                timestamp = Long.parseLong(propValue
                        .substring(PWD_HISTORY_PREFIX.length()));
            } catch (NumberFormatException ex) {
                logger
                        .warn("Unable to parse last password change timestamp."
                                + " User property for password history has wrong format: "
                                + propValue);
            }
        }

        return timestamp;
    }

    public String getPassword() {
        return mPassword;
    }

    // --------------------------------------------------------------------------
    // FH 29 Mar. 2001
    // Initial implementation
    //
    /**
     * Add (or update if not already in the property list) a property key-value pair in the user's properties list.
     * 
     * @param key
     *            Property's name.
     * @param value
     *            Property's value.
     * 
     * @return Return true on success or false on any failure.
     */
    public synchronized boolean setProperty(String key, String value) {
        boolean result = false;

        if (mProperties == null) {
            return result;
        }

        if ((key != null) && (value != null) && (!mProperties.isReadOnly(key))) {
            JahiaUserManager userManager = (JahiaUserManager) SpringContextSingleton
                    .getInstance().getContext().getBean(
                            JahiaUserManager.class.getName());
            if (getProperty(key) == null) {
                result = userManager.addProperty(key, value, mID,
                        getProviderName(), getUserKey());
            } else {
                result = userManager.updateProperty(key, value, mID,
                        getProviderName(), getUserKey());
            }

            if (result) {
                try {
                    mProperties.setProperty(key, value);
                } catch (UserPropertyReadOnlyException uproe) {
                    logger.warn("Cannot set read-only property " + key);
                }
                ServicesRegistry.getInstance().getJahiaUserManagerService()
                        .updateCache(this);
            }
        }
        return result;
    }

    // --------------------------------------------------------------------------
    // FH 29 Mar. 2001
    // Initial implementation
    //
    public boolean verifyPassword(String password) {

        if (password != null) {
            String test = JahiaUserManagerService.encryptPassword(password);
            if (mPassword.equals(test)) {
                return true;
            } else {
                JahiaDBUser user = (JahiaDBUser) JahiaUserManagerDBProvider
                        .getInstance().lookupUserByKey(mUserKey);
                if (user.getPassword().equals(test)) {
                    mPassword = user.getPassword();
                    return true;
                }
            }
        }
        return false;
    }

    // --------------------------------------------------------------------------
    // FH 29 Mar. 2001
    // Initial implementation
    //
    /**
     * Return a string representation of the user and it's internal state.
     * 
     * @return A string representation of this user.
     */
    public String toString() {
        final StringBuffer output = new StringBuffer("Detail of user ["
                + mUsername + "]\n");
        output.append("  - ID [").append(Integer.toString(mID)).append("]");
        output.append("  - password [").append(mPassword).append("]\n");

        if (mProperties != null) {
            output.append("  - properties :");

            Iterator<String> nameIter = mProperties.propertyNameIterator();
            String name;
            if (nameIter.hasNext()) {
                output.append("\n");
                while (nameIter.hasNext()) {
                    name = (String) nameIter.next();
                    output.append("       ");
                    output.append(name);
                    output.append(" -> [");
                    output.append(mProperties.getProperty(name));
                    output.append("]\n");
                }
            } else {
                output.append(" -no properties-\n");
            }
        }
        return output.toString();
    }

    // --------------------------------------------------------------------------
    // NK 06 Avr. 2001
    /**
     * Test if the user is an admin member
     * 
     * @return Return true if the user is an admin member false on any error.
     */
    public boolean isAdminMember(int siteID) {

        return isMemberOfGroup(siteID,
                JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
    }

    /**
     * Test if the user is the root user
     * 
     * @return Return true if the user is the root user false on any error.
     */
    public boolean isRoot() {
        /**
         * todo FIXME in this implementation the super user is necessarily always in the jahia database implementation
         */
        return getID() == JahiaUserManagerDBProvider.ROOT_USER_ID;
    }

    // -------------------------------------------------------------------------
    public boolean isMemberOfGroup(int siteID, String name) {
        // Get the services registry
        ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
        if (servicesRegistry != null) {

            // get the group management service
            JahiaGroupManagerService groupService = servicesRegistry
                    .getJahiaGroupManagerService();

            // lookup the requested group
            JahiaGroup group = groupService.lookupGroup(siteID, name);
            if (group != null) {
                return group.isMember(this);
            }
        }
        return false;
    }

    /**
     * Retrieve a List of language codes stored for this user. The order of these codes is important. The first language is the first
     * choice, and as the list goes down so does the importance of the languages.
     * 
     * @return a List containing String objects that contain language codes, the List may be empty if this property was never set for the
     *         user.
     */
    public List<String> getLanguageCodes() {
        String encodedLanguagesCodes = getProperty(mLANGUAGES_ORDER_PROP);
        List<String> result = new ArrayList<String>();
        if (encodedLanguagesCodes != null) {
            StringTokenizer strTokens = new StringTokenizer(
                    encodedLanguagesCodes, mLANGUAGES_ORDER_PROP_SEPARATOR);
            while (strTokens.hasMoreTokens()) {
                String curLanguageCode = strTokens.nextToken();
                result.add(curLanguageCode);
            }
        }
        return result;
    }

    /**
     * Sets the language codes for a user. This List contains String object that contain language codes. The order of this list is defined
     * so as that the most important language is first.
     * 
     * @param userLanguages
     *            a List of String object containing the language codes. The order is from the most important language first to the least
     *            important one last.
     */
    public void setLanguageCodes(List<String> userLanguages) {
        StringBuffer encodedLanguageCodes = new StringBuffer();
        Iterator<String> userLanguagesEnum = userLanguages.iterator();
        while (userLanguagesEnum.hasNext()) {
            String curLanguage = userLanguagesEnum.next();
            if (curLanguage.indexOf(mLANGUAGES_ORDER_PROP_SEPARATOR) != 0) {
                // we found the separator character inside the string.
                logger.debug("Invalid " + mLANGUAGES_ORDER_PROP_SEPARATOR
                        + " character in language code : " + curLanguage
                        + ". Not storing this language code.");
            } else {
                encodedLanguageCodes.append(curLanguage);
                encodedLanguageCodes.append(mLANGUAGES_ORDER_PROP_SEPARATOR);
            }
        }
        String encodedLanguageCodeStr = encodedLanguageCodes.toString();
        // we must now remove the extra separator at the end of the encoded list
        encodedLanguageCodeStr = encodedLanguageCodeStr.substring(0,
                encodedLanguageCodeStr.length() - 1);
        setProperty(mLANGUAGES_ORDER_PROP, encodedLanguageCodeStr);
    }

    /**
     * Returns true if this user has activated the language mixing function of Jahia. This means that the content that will be displayed on
     * his page will content multiple languages based on language preference list.
     * 
     * @return true if the property is active.
     */
    public boolean isMixLanguagesActive() {
        String mixActiveStr = getProperty(mLANGUAGES_MIX_PROP);
        if (mixActiveStr != null) {
            return Boolean.valueOf(mixActiveStr);
        } else {
            logger.debug(mLANGUAGES_MIX_PROP + " property not found for user "
                    + this.getUsername() + ". Defaulting to false.");
            return false;
        }
    }

    /**
     * Sets the user property that indicates whether the user wants to see mixed language content in the pages he is browsing or not.
     * 
     * @param mixLanguagesActive
     *            a boolean set to true to allow language mixing
     */
    public void setMixLanguagesActive(boolean mixLanguagesActive) {
        setProperty(mLANGUAGES_MIX_PROP, Boolean.valueOf(mixLanguagesActive)
                .toString());
    }

    /**
     * Returns true if the user has setup this property indicating that he doesn't want to default to any other languages than the ones he
     * has setup in his users settings. This will shortcut defaulting to the browser and site settings.
     * 
     * @return true if the user only wants to see the languages he has configured and never the ones configured in his browser or in the
     *         site settings.
     */
    public boolean isUserLanguagesOnlyActive() {
        String userLanguagesOnlyStr = getProperty(mLANGUAGES_ONLYUSER_PROP);
        if (userLanguagesOnlyStr != null) {
            return Boolean.valueOf(userLanguagesOnlyStr);
        } else {
            logger.debug(mLANGUAGES_ONLYUSER_PROP
                    + " property not found for user " + this.getUsername()
                    + ". Defaulting to false.");
            return false;
        }

    }

    /**
     * Sets the value indicating whether a user wants to fallback to browser or site setting languages. If set to true this means we are NOT
     * falling back to the above mentioned settings.
     * 
     * @param userLanguagesOnlyActive
     *            true means we are not falling back to browser or site settings.
     */
    public void setUserLanguagesOnlyActive(boolean userLanguagesOnlyActive) {
        setProperty(mLANGUAGES_ONLYUSER_PROP, Boolean.valueOf(
                userLanguagesOnlyActive).toString());

    }

    /**
     * Get the name of the provider of this user.
     * 
     * @return String representation of the name of the provider of this user
     */
    public String getProviderName() {
        return JahiaUserManagerDBProvider.PROVIDER_NAME;
    }

    public boolean isProxied() {
        return proxied;
    }

    public void setProxied(boolean proxied) {
        this.proxied = proxied;
    }

    /**
     * if true, ignore user aliasing check at ACL level.
     * 
     * @return true if ignore user aliasing check at ACL level
     */
    public boolean byPassUserAliasing() {
        return bypassUserAliasing;
    }

    /**
     * if true, force ignore user aliasing check at ACL level
     * 
     * @param bypassUserAliasing
     *            if true, force ignore user aliasing check at ACL level
     */
    public void setByPassUserAliasing(boolean bypassUserAliasing) {
        this.bypassUserAliasing = bypassUserAliasing;
    }

}
