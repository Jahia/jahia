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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.notification;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.UserProperties;
import org.jahia.services.usermanager.UserProperty;

/**
 * Unregistered user that has subscribed to one of Jahia notification services.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscriptionUser implements JahiaUser {

    private Properties properties;

    private String username;

    /**
     * Initializes an instance of this class.
     * 
     * @param username
     * @param properties
     */
    public SubscriptionUser(String username, Map<String, String> properties) {
        this(username, (Properties) null);
        if (properties != null && !properties.isEmpty()) {
            this.properties.putAll(properties);
        }
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param username
     * @param properties
     */
    public SubscriptionUser(String username, Properties properties) {
        this.username = username;
        this.properties = properties != null ? properties : new Properties();
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#byPassUserAliasing()
     */
    public boolean byPassUserAliasing() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#getHomepageID()
     */
    public int getHomepageID() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#getLanguageCodes()
     */
    public List<String> getLanguageCodes() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#getName()
     */
    public String getName() {
        return getUsername();
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#getProperties()
     */
    public Properties getProperties() {
        return properties;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jahia.services.usermanager.JahiaUser#getProperty(java.lang.String)
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#getProviderName()
     */
    public String getProviderName() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#getUserKey()
     */
    public String getUserKey() {
        return username;
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#getUsername()
     */
    public String getUsername() {
        return username;
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#getUserProperties()
     */
    public UserProperties getUserProperties() {
        return new UserProperties(properties, true);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jahia.services.usermanager.JahiaUser#getUserProperty(java.lang.String
     * )
     */
    public UserProperty getUserProperty(String key) {
        return new UserProperty(key, getProperty(key), true);
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#isAdminMember(int)
     */
    public boolean isAdminMember(int siteID) {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#isMemberOfGroup(int,
     * java.lang.String)
     */
    public boolean isMemberOfGroup(int siteID, String name) {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#isMixLanguagesActive()
     */
    public boolean isMixLanguagesActive() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#isPasswordReadOnly()
     */
    public boolean isPasswordReadOnly() {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#isProxied()
     */
    public boolean isProxied() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#isRoot()
     */
    public boolean isRoot() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#isUserLanguagesOnlyActive()
     */
    public boolean isUserLanguagesOnlyActive() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jahia.services.usermanager.JahiaUser#removeProperty(java.lang.String)
     */
    public boolean removeProperty(String key) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jahia.services.usermanager.JahiaUser#setByPassUserAliasing(boolean)
     */
    public void setByPassUserAliasing(boolean bypassUserAliasing) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#setHomepageID(int)
     */
    public boolean setHomepageID(int id) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jahia.services.usermanager.JahiaUser#setLanguageCodes(java.util.List)
     */
    public void setLanguageCodes(List<String> userLanguages) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jahia.services.usermanager.JahiaUser#setMixLanguagesActive(boolean)
     */
    public void setMixLanguagesActive(boolean mixLanguagesActive) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jahia.services.usermanager.JahiaUser#setPassword(java.lang.String)
     */
    public boolean setPassword(String password) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jahia.services.usermanager.JahiaUser#setProperty(java.lang.String,
     * java.lang.String)
     */
    public boolean setProperty(String key, String value) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.usermanager.JahiaUser#setProxied(boolean)
     */
    public void setProxied(boolean proxied) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jahia.services.usermanager.JahiaUser#setUserLanguagesOnlyActive(boolean
     * )
     */
    public void setUserLanguagesOnlyActive(boolean userLanguagesOnlyActive) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jahia.services.usermanager.JahiaUser#verifyPassword(java.lang.String)
     */
    public boolean verifyPassword(String password) {
        throw new UnsupportedOperationException();
    }

}
