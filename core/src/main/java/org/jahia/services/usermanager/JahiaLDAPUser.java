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
//

package org.jahia.services.usermanager;

import org.jahia.hibernate.manager.JahiaUserManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;

import java.io.Serializable;
import java.util.*;

/**
 * A JahiaUser represents a physical person who is defined by a username and
 * a password for authentification purpose. Every other property of a JahiaUser
 * is stored in it's properties list, which hold key-value string pairs.
 * For example email, firstname, lastname, ... information should be stored in
 * this properties list.
 *
 * @author Serge Huber
 * @version 1.0
 */
public class JahiaLDAPUser implements JahiaUser, Serializable {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaLDAPUser.class);

    private static final long serialVersionUID = 949596639726348808L;

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

    /** DN in ldap repository **/
    private String mDn;

    /** groups **/
    private List<String> mGroups;

    /** User additional parameters. */
    private UserProperties mProperties = new UserProperties ();

    /** User home page property * */
    private static final String mHOMEPAGE_PROP = "user_homepage";

    // language property constants
    private static final String mLANGUAGES_ORDER_PROP = "language_codes";
    private static final String mLANGUAGES_ORDER_PROP_SEPARATOR = ",";
    private static final String mLANGUAGES_MIX_PROP = "langage_mix";
    private static final String mLANGUAGES_ONLYUSER_PROP = "language_onlyuser";

    private transient JahiaUserManagerLDAPProvider provider;
    private transient boolean propLoaded = false;

    private boolean proxied;
    private boolean bypassUserAliasing;
    
    /**
     * Create a new JahiaDBUser class instance. The passed in password must
     * already be encrypted, no ecryption will be done. If the passed in
     * properties is null, then the user will have no additional parameter than
     * it's id, name and password.
     *
     * @param id         User unique identification number.
     * @param name       User identification name.
     * @param password   User password.
     *                   The site id
     * @param properties User properties.
     */
    protected JahiaLDAPUser (int id, String name, String password,
                             String userKey, int siteID,
                           UserProperties properties, String dn, JahiaUserManagerLDAPProvider provider)
    {
        mID = id;
        mUsername = name;
        mPassword = password;
        mUserKey = "{"+provider.getKey()+"}" + userKey;
        mSiteID = siteID;
        mDn         = dn;
        if (properties != null) {
            mProperties = properties;
        }
        this.provider = provider;
    }


    public boolean equals (Object another) {
        
        if (this == another) return true;
        
        if (another != null && this.getClass() == another.getClass()) {
            return (getUserKey().equals(((JahiaUser) another).getUserKey()));
        }
        return false;
        
//        if (another instanceof Principal) {
//            if (another != null) {
//                if (mUsername == null) {
//                    logger.debug (
//                            "Username for user key [" + mUserKey + "] is null , please check your users.ldap.properties mapping file !");
//                    if (((Principal) another).getName () == null) {
//                        return true;
//                    } else {
//                        return false;
//                    }
//                }
//                return (getName ().equals (((Principal) another).getName ()));
//            }
//        }
//        return false;
    }


    /**
     * Retrieve the user's unique database identification number.
     *
     * @return The user unique identification number.
     */
    public int getID () {
        return mID;
    }


    public String getName () {
        return getUsername();
    }

    public String getUsername () {
        return mUsername;
    }

    public String getUserKey () {
        return mUserKey;
    }

    public int getSiteID () {
        return mSiteID;
    }

    public String getDN() {
        return mDn;
    }

    /**
     * @deprecated use getUserProperties() instead
     * @return Properties the properties returned here should NEVER be modified,
     * only modifications through setProperty() are supported and serialized.
     */
    public Properties getProperties () {
        if (mProperties != null) {
            return mProperties.getProperties();
        } else {
            return null;
        }
    }

    /**
     * The properties here should not be modified, as the modifications will
     * not be serialized. Use the setProperty() method to modify a user's
     * properties.
     * @return UserProperties
     */
    public UserProperties getUserProperties() {
        if (!propLoaded ) {
            provider.mapDBToJahiaProperties(mProperties, mUserKey);
            propLoaded = true;
        }
        return mProperties;
    }

    //
    public String getProperty (String key) {
        if (key != null) {
            if (mProperties.getProperty(key) != null) {
                return mProperties.getProperty(key);
            }
            if (getUserProperties() != null) {
                return getUserProperties().getProperty (key);
            }
        }
        return null;
    }

    public UserProperty getUserProperty(String key) {
        if ((getUserProperties() != null) && (key != null)) {
            return getUserProperties().getUserProperty (key);
        }
        return null;
    }


    /**
     * Return a unique hashcode identifiying the user.
     *
     * @return Return a valid hashcode integer of the user, On failure, -1 is
     *         returned.
     */
    public int hashCode () {
        return mID;
    }


    /**
     * Remove the specified property from the properties list.
     *
     * @param key Property's name.
     *
     * @return Return true on success or false on any failure.
     *
     * @todo FIXME : not supported in this read-only implementation
     */
    public synchronized boolean removeProperty (String key) {
        boolean result = false;
        UserProperties mProperties = getUserProperties();
        if (mProperties == null) {
            return result;
        }

        if ((key != null) && (key.length () > 0) && (!mProperties.isReadOnly(key))) {
            // Remove these lines if LDAP problem --------------------
            JahiaUserManager userManager = (JahiaUserManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaUserManager.class.getName());
            result = userManager.removeProperty (key, -1, getProviderName (), getUserKey ());
        }
        //Predrag
        if (result) {
            mProperties.removeUserProperty (key);
        }
        //Predrag
        return result;
    }

    public boolean isPasswordReadOnly() {
        return true;
    }

    /**
     * Change the user's password.
     *
     * @param newPassword New user's password
     *
     * @return Return true id the old password is the same as the current one and
     *         the new password is valid. Return false on any failure.
     *
     * @todo FIXME : not supported in this read-only LDAP implementation
     */
    public boolean setPassword (String password) {
        return false;
    }


    /**
     * Add (or update if not already in the property list) a property key-value
     * pair in the user's properties list.
     *
     * @param key   Property's name.
     * @param value Property's value.
     *
     * @return Return true on success or false on any failure.
     *
     * @todo FIXME : not supported in LDAP implementation, only sets internal
     * values in memory.
     * @todo FIXME : These following lines are a quick hack to permit to store
     * user properties for readonly LDAP users. A better solution would be to
     * create a service that is in charge of managing user properties.
     */
    public synchronized boolean setProperty (String key, String value) {
        boolean result = false;
        UserProperties mProperties = getUserProperties();
        if (mProperties == null) {
            return result;
        }

        if ((key != null) && (value != null) && (!mProperties.isReadOnly(key))) {
            // Remove these lines if LDAP problem --------------------
            JahiaUserManager userManager = (JahiaUserManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaUserManager.class.getName());

            if (getProperty(key) == null) {
                result = userManager.addProperty(key, value, -1, getProviderName(), getUserKey());
            } else {
                result = userManager.updateProperty(key, value, -1, getProviderName(), getUserKey());
            }

            // End remove --------------------
            if (result) {
                try {
                    mProperties.setProperty(key, value);
                } catch (UserPropertyReadOnlyException uproe) {
                    logger.warn("Cannot set read-only property " + key);
                }
                ServicesRegistry.getInstance().getJahiaUserManagerService().updateCache(this);
            }
        }
        return result;
    }

    //-------------------------------------------------------------------------
    /**
     * Returns the user's home page id.
     * -1 : undefined
     *
     * @return int The user homepage id.
     */
    public int getHomepageID () {

        if (getUserProperties() != null) {

            try {
                // Get the home page from the Jahia DB.
                // By default an external user is represented with a -1 user ID.
                String value = mProperties.getProperty (mHOMEPAGE_PROP);
                if (value == null) {
                    return -1;
                }
                return Integer.parseInt (value);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return -1;
    }

    //-------------------------------------------------------------------------
    /**
     * Set the home page id.
     *
     * @param int The user homepage id.
     *
     * @return false on error
     */
    public boolean setHomepageID (int id) {

        // Set the home page into the Jahia DB.
        // By default an external user is represented with a -1 user ID.
        return setProperty (mHOMEPAGE_PROP, String.valueOf (id));
    }

    public List<String> getGroups() {
        return mGroups;
    }

    public void setGroups(List<String> mGroups) {
        this.mGroups = mGroups;
    }

    public boolean verifyPassword (String password) {

        if (password != null) {
			boolean localLoginResult = false;

			if (mPassword.length() > 0) {
				String test = JahiaUserManagerService.encryptPassword(password);
				localLoginResult = mPassword.equals(test);
			}
			// test the provided password with the internal memory encrypted
			// password.
			if (localLoginResult) {
				// both passwords match.
				return true;
			} else {
				// the local encrypted password does not match the one in
				// parameter
				// forward to the ldap authN in case of there was a ldap
				// password change from the last user's visit.
				boolean loginResult = JahiaUserManagerLDAPProvider
						.getInstance().login(mUserKey, password);
				if (loginResult) {
					/**
					 * @todo here we must now update the properties of the user
					 *       since he has access to more of his attributes once
					 *       logged in
					 */
					mPassword = JahiaUserManagerService
							.encryptPassword(password);
					return true;
				}
			}
			/** @todo insert here LDAP connection check... */

		}
        return false;
    }

    /**
     * Return a string representation of the user and it's internal state.
     *
     * @return A string representation of this user.
     */
    public String toString () {
        StringBuffer output = new StringBuffer ("Detail of user [" + mUsername + "]\n");
        output.append ("  - ID [" + Integer.toString (mID) + "]");
        output.append ("  - password [" + mPassword + "]\n");

        if (getUserProperties() != null) {
            output.append("  - properties :");

            Iterator<?> nameIter = mProperties.propertyNameIterator();
            String name;
            if (nameIter.hasNext()) {
                output.append("\n");
                while (nameIter.hasNext()) {
                    name = (String) nameIter.next();
                    output.append(
                        "       " + name + " -> [" +
                        (String) mProperties.getProperty(name) + "]\n");
                }
            } else {
                output.append(" -no properties-\n");
            }
        }
        return output.toString ();
    }


    /**
     * Test if the user is an admin member
     *
     * @return Return true if the user is an admin member
     *         false on any error.
     */
    public boolean isAdminMember (int siteID) {

        return isMemberOfGroup (siteID, JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
    }

    /**
     * Test if the user is the root user
     *
     * @return Return true if the user is the root user
     *         false on any error.
     */
    public boolean isRoot () {
        /** @todo FIXME in this implementation the super user is necessarily
         *  always in the jahia database implementation
         */
        return false;
    }


    //-------------------------------------------------------------------------
    public boolean isMemberOfGroup (int siteID, String name) {
        // Get the services registry
        ServicesRegistry servicesRegistry = ServicesRegistry.getInstance ();
        if (servicesRegistry != null) {

            // get the group management service
            JahiaGroupManagerService groupService =
                    servicesRegistry.getJahiaGroupManagerService ();

            // lookup the requested group
            JahiaGroup group = groupService.lookupGroup (siteID, name);
            if (group != null) {
                return group.isMember (this);
            }
        }
        return false;
    }

    /**
     * Retrieve a List of language codes stored for this user. The order
     * of these codes is important. The first language is the first choice, and
     * as the list goes down so does the importance of the languages.
     *
     * @return a List containing String objects that contain language codes,
     *         the List may be empty if this property was never set for the user.
     */
    public List<String> getLanguageCodes () {
        String encodedLanguagesCodes = getProperty (mLANGUAGES_ORDER_PROP);
        List<String> result = new ArrayList<String>();
        if (encodedLanguagesCodes != null) {
            StringTokenizer strTokens = new StringTokenizer (encodedLanguagesCodes,
                    mLANGUAGES_ORDER_PROP_SEPARATOR);
            while (strTokens.hasMoreTokens ()) {
                String curLanguageCode = strTokens.nextToken ();
                result.add (curLanguageCode);
            }
        }
        return result;
    }

    /**
     * Sets the language codes for a user. This List contains String object
     * that contain language codes. The order of this list is defined so as that
     * the most important language is first.
     *
     * @param userLanguages a List of String object containing the language
     *                      codes. The order is from the most important language first to the least
     *                      important one last.
     */
    public void setLanguageCodes (List<String> userLanguages) {
        StringBuffer encodedLanguageCodes = new StringBuffer ();
        Iterator<String> userLanguagesEnum = userLanguages.iterator();
        while (userLanguagesEnum.hasNext ()) {
            String curLanguage = userLanguagesEnum.next ();
            if (curLanguage.indexOf (mLANGUAGES_ORDER_PROP_SEPARATOR) != 0) {
                // we found the separator character inside the string.
                logger.debug ("Invalid " + mLANGUAGES_ORDER_PROP_SEPARATOR +
                        " character in language code : " + curLanguage +
                        ". Not storing this language code.");
            } else {
                encodedLanguageCodes.append (curLanguage);
                encodedLanguageCodes.append (mLANGUAGES_ORDER_PROP_SEPARATOR);
            }
        }
        String encodedLanguageCodeStr = encodedLanguageCodes.toString ();
        // we must now remove the extra separator at the end of the encoded list
        encodedLanguageCodeStr =
                encodedLanguageCodeStr.substring (0, encodedLanguageCodeStr.length () - 1);
        setProperty (mLANGUAGES_ORDER_PROP, encodedLanguageCodeStr);
    }

    /**
     * Returns true if this user has activated the language mixing function
     * of Jahia. This means that the content that will be displayed on his
     * page will content multiple languages based on language preference list.
     *
     * @return true if the property is active.
     */
    public boolean isMixLanguagesActive () {
        String mixActiveStr = getProperty (mLANGUAGES_MIX_PROP);
        if (mixActiveStr != null) {
            Boolean mixActiveBool = Boolean.valueOf (mixActiveStr);
            return mixActiveBool.booleanValue ();
        } else {
            logger.debug (mLANGUAGES_MIX_PROP +
                    " property not found for user " +
                    this.getUsername () +
                    ". Defaulting to false.");
            return false;
        }
    }

    /**
     * Sets the user property that indicates whether the user wants to see
     * mixed language content in the pages he is browsing or not.
     *
     * @param mixLanguagesActive a boolean set to true to allow language mixing
     */
    public void setMixLanguagesActive (boolean mixLanguagesActive) {
        setProperty (mLANGUAGES_MIX_PROP,
                Boolean.valueOf(mixLanguagesActive).toString ());
    }

    /**
     * Returns true if the user has setup this property indicating that he
     * doesn't want to default to any other languages than the ones he has
     * setup in his users settings. This will shortcut defaulting to the
     * browser and site settings.
     *
     * @return true if the user only wants to see the languages he has configured
     *         and never the ones configured in his browser or in the site settings.
     */
    public boolean isUserLanguagesOnlyActive () {
        String userLanguagesOnlyStr = getProperty (mLANGUAGES_ONLYUSER_PROP);
        if (userLanguagesOnlyStr != null) {
            Boolean userLanguagesOnlyBool = Boolean.valueOf (userLanguagesOnlyStr);
            return userLanguagesOnlyBool.booleanValue ();
        } else {
            logger.debug (mLANGUAGES_ONLYUSER_PROP +
                    " property not found for user " +
                    this.getUsername () +
                    ". Defaulting to false.");
            return false;
        }

    }

    /**
     * Sets the value indicating whether a user wants to fallback to browser
     * or site setting languages. If set to true this means we are NOT falling
     * back to the above mentioned settings.
     *
     * @param userLanguagesOnlyActive true means we are not falling back to
     *                                browser or site settings.
     */
    public void setUserLanguagesOnlyActive (boolean userLanguagesOnlyActive) {
        setProperty (mLANGUAGES_ONLYUSER_PROP,
                Boolean.valueOf(userLanguagesOnlyActive).toString ());

    }

    /**
     * Get the name of the provider of this user.
     *
     * @return String representation of the name of the provider of this user
     */
    public String getProviderName () {
        return JahiaUserManagerLDAPProvider.PROVIDER_NAME;
    }

    public boolean isProxied() {
        return proxied;
    }

    public void setProxied(boolean proxied) {
        this.proxied = proxied;
    }

    /**
     * if true, ignore user aliasing check at ACL level.
     * @return
     */
    public boolean byPassUserAliasing() {
        return bypassUserAliasing;
    }

    /**
     * if true, force ignore user aliasing check at ACL level
     * @param bypassUserAliasing
     */
    public void setByPassUserAliasing(boolean bypassUserAliasing) {
        this.bypassUserAliasing = bypassUserAliasing;
    }

}
