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

import java.util.Properties;

/**
 * A JahiaUser represents a physical person who is defined by a username and
 * a password for authentication purpose. Every other property of a JahiaUser
 * is stored in it's properties list, which hold key-value string pairs. For
 * example email, firstname, lastname, ... information should be stored in this
 * properties list.
 *
 * @author Fulco Houkes
 * @author Khue Nguyen
 */
public interface JahiaUser extends JahiaPrincipal {

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
     * This method can fail and do no modification if the property we are
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
     * Verify if the passed in password is the same as the encapsulated by this
     * user.
     *
     * @param password String representation of an non-encrypted password.
     * @return Return true if the passed in password is the same as the
     *         encapsulated password in this user, and return false on any error.
     */
    public boolean verifyPassword(String password);

    /**
     * Get the name of the provider of this user.
     *
     * @return String representation of the name of the provider of this user
     */
    public String getProviderName();
}
