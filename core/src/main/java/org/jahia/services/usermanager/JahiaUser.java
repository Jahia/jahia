/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.usermanager;


import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Properties;


/**
 * A JahiaUser represents a physical person who is defined by a username and
 * a password for authentification purpose. Every other property of a JahiaUser
 * is stored in it's properties list, which hold key-value string pairs. For
 * example email, firstname, lastname, ... information should be stored in this
 * properties list.
 *
 * @author Fulco Houkes
 * @author Khue Nguyen
 * @version 2.2
 */
public interface JahiaUser extends Principal, Serializable {

    /**
     * Return the unique user's username ( = his user key )
     *
     * @return Return a String representation of the user. In Jahia this
     *         corresponds to the user key, which is unique within a Jahia system.
     */
    public String getName();

    /**
     * Return the user username.
     *
     * @return Return the username.
     */
    public String getUsername();

    /**
     * Return the unique String identifier of this user.
     *
     * @return the unique String identifier of this user.
     */
    public String getUserKey();

    /**
     * Returns the group's home page id.
     * -1 : undefined
     *
     * @return int The group homepage id.
     */
    public abstract int getHomepageID();

    /**
     * Set the home page id.
     *
     * @param id The group homepage id.
     * @return false on error
     */
    public abstract boolean setHomepageID(int id);

    /**
     * Return a unique hashcode identifiying the user. The returned integer
     * must be unique for each specific user. For exemple, two different users
     * must return 2 different integer values, but two instanciation of the
     * same user must return the same value.
     *
     * @return Return a valid hashcode integer of the user, on failure, -1 is
     *         returned.
     */
    public int hashCode();


    /**
     * Get user's properties list.
     * The properties here should not be modified, as the modifications will
     * not be serialized. Use the setProperty() method to modify a user's
     * properties.
     *
     * @return Return a reference on the user's properties list, or null if no
     *         property is present.
     */
    public Properties getProperties();

    /**
     * Returns the users properties format, in which we can test if a property
     * is read-only or not.
     * The properties here should not be modified, as the modifications will
     * not be serialized. Use the setProperty() method to modify a user's
     * properties.
     *
     * @return UserProperties
     */
    public UserProperties getUserProperties();

    /**
     * Retrieve the requested user property.
     *
     * @param key Property's name.
     * @return Return the property's value of the specified key, or null if the
     *         property does not exist.
     */
    public String getProperty(String key);

    /**
     * Retrieves the user property object, so that we can test is the property
     * is read-only or not.
     *
     * @param key String
     * @return UserProperty
     */
    public UserProperty getUserProperty(String key);

    /**
     * Remove the specified property from the properties list.
     * This method can fail and do no modification is the property we are
     * trying to set is read-only.
     *
     * @param key Property's name.
     * @return true if everything went well
     */
    public boolean removeProperty(String key);


    /**
     * Add (or update if not already in the property list) a property key-value
     * pair in the user's properties list.
     * This method can fail and do no modification is the property we are
     * trying to set is read-only.
     *
     * @param key   Property's name.
     * @param value Property's value.
     * @return true if everything went well
     */
    public boolean setProperty(String key, String value);


    /**
     * Return a string representation of the user and it's internal state.
     *
     * @return A string representation of this user.
     */
    public abstract String toString();


    /**
     * Compare this User to specified object. Return true if this user is the
     * same as the object passed in.
     *
     * @param another Principal compare with.
     * @return Return true if the passed in is the same as the encapsulated by this
     *         Principal, and false otherwise.
     */
    public boolean equals(Object another);


    /**
     * Method to test if the password is read-only
     *
     * @return boolean
     */
    public boolean isPasswordReadOnly();

    /**
     * Change the user's password.
     *
     * @param password New user's password
     * @return Return true if the password was successfully changed, false
     *         otherwise
     */
    public boolean setPassword(String password);


    /**
     * Test if the user is member of the specified group.
     *
     * @param name   Groupname.
     * @param siteID the site id
     * @return Return true if the user is member of the specified group, or
     *         false on any error.
     */
    public boolean isMemberOfGroup(int siteID, String name);


    /**
     * Test if the user is an admin member
     *
     * @param siteID the site id
     * @return Return true if the user is an admin member
     *         false on any error.
     */
    public boolean isAdminMember(int siteID);

    /**
     * Test if the user is the root user
     *
     * @return Return true if the user is the root user
     *         false on any error.
     */
    public boolean isRoot();

    /**
     * Verifiy if the passed in password is the same as the encapsulated by this
     * user.
     *
     * @param password String representation of an non-encrypted password.
     * @return Return true if the passed in password is the same as the
     *         encapsulated password in this user, and return false on any error.
     */
    public boolean verifyPassword(String password);

    /**
     * Retrieve a List of language codes stored for this user. The order
     * of these codes is important. The first language is the first choice, and
     * as the list goes down so does the importance of the languages.
     *
     * @return a List containing String objects that contain language codes,
     *         the List may be empty if this property was never set for the user.
     */
    public List<String> getLanguageCodes();

    /**
     * Sets the language codes for a user. This List contains String object
     * that contain language codes. The order of this list is defined so as that
     * the most important language is first.
     *
     * @param userLanguages a List of String object containing the language
     *                      codes. The order is from the most important language first to the least
     *                      important one last.
     */
    public void setLanguageCodes(List<String> userLanguages);

    /**
     * Returns true if this user has activated the language mixing function
     * of Jahia. This means that the content that will be displayed on his
     * page will content multiple languages based on language preference list.
     *
     * @return true if the property is active.
     */
    public boolean isMixLanguagesActive();

    /**
     * Sets the user property that indicates whether the user wants to see
     * mixed language content in the pages he is browsing or not.
     *
     * @param mixLanguagesActive a boolean set to true to allow language mixing
     */
    public void setMixLanguagesActive(boolean mixLanguagesActive);

    /**
     * Returns true if the user has setup this property indicating that he
     * doesn't want to default to any other languages than the ones he has
     * setup in his users settings. This will shortcut defaulting to the
     * browser and site settings.
     *
     * @return true if the user only wants to see the languages he has configured
     *         and never the ones configured in his browser or in the site settings.
     */
    public boolean isUserLanguagesOnlyActive();

    /**
     * Sets the value indicating whether a user wants to fallback to browser
     * or site setting languages. If set to true this means we are NOT falling
     * back to the above mentioned settings.
     *
     * @param userLanguagesOnlyActive true means we are not falling back to
     *                                browser or site settings.
     */
    public void setUserLanguagesOnlyActive(boolean userLanguagesOnlyActive);

    /**
     * Get the name of the provider of this user.
     *
     * @return String representation of the name of the provider of this user
     */
    public String getProviderName();

    public boolean isProxied();

    public void setProxied(boolean proxied);

    /**
     * if true, ignore user aliasing check at ACL level.
     *
     * @return true if option  byPassUserAliasing is set
     */
    public boolean byPassUserAliasing();

    /**
     * if true, force ignore user aliasing check at ACL level
     *
     * @param bypassUserAliasing set to true to activate option byPassUserAliasing
     */
    public void setByPassUserAliasing(boolean bypassUserAliasing);
}
