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
        if (!this.properties.containsKey("email")) {
            this.properties.put("email", username);
        }
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
