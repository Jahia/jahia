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

import java.util.List;
import java.util.Properties;

/**
 * This user wrapper always returns true for its method <code>byPassUserAliasing()</code>
 * to force bypassing User Aliasing check on ACL for certain right,
 * like <code>JahiaACLManagerService.getSiteActionPermission</code> 
 *
 * User: hollis
 * Date: 31 juil. 2008
 * Time: 10:33:07
 * To change this template use File | Settings | File Templates.
 */
public class JahiaUserAliasing implements JahiaUser {

    private JahiaUser user;

    public JahiaUserAliasing(JahiaUser user) {
        this.user = user;
    }

    public String getName() {
        return user.getName();
    }

    public String getUsername() {
        return user.getUsername();
    }

    public String getUserKey() {
        return user.getUserKey();
    }

    public int getHomepageID() {
        return user.getHomepageID();
    }

    public boolean setHomepageID(int id) {
        return  user.setHomepageID(id);
    }

    public Properties getProperties() {
        return user.getProperties();
    }

    public UserProperties getUserProperties() {
        return user.getUserProperties();
    }

    public String getProperty(String key) {
        return user.getProperty(key);
    }

    public UserProperty getUserProperty(String key) {
        return user.getUserProperty(key);
    }

    public boolean removeProperty(String key) {
        return user.removeProperty(key);
    }

    public boolean setProperty(String key, String value) {
        return user.setProperty(key,value);
    }

    public boolean isPasswordReadOnly() {
        return user.isPasswordReadOnly();
    }

    public boolean setPassword(String password) {
        return user.setPassword(password);
    }

    public boolean isMemberOfGroup(int siteID, String name) {
        return user.isMemberOfGroup(siteID,name);
    }

    public boolean isAdminMember(int siteID) {
        return user.isAdminMember(siteID);
    }

    public boolean isRoot() {
        return user.isRoot();
    }

    public boolean verifyPassword(String password) {
        return user.verifyPassword(password);
    }

    public List<String> getLanguageCodes() {
        return user.getLanguageCodes();
    }

    public void setLanguageCodes(List<String> userLanguages) {
        user.setLanguageCodes(userLanguages);
    }

    public boolean isMixLanguagesActive() {
        return user.isMixLanguagesActive();
    }

    public void setMixLanguagesActive(boolean mixLanguagesActive) {
        user.setMixLanguagesActive(mixLanguagesActive);
    }

    public boolean isUserLanguagesOnlyActive() {
        return user.isUserLanguagesOnlyActive();
    }

    public void setUserLanguagesOnlyActive(boolean userLanguagesOnlyActive) {
        user.setUserLanguagesOnlyActive(userLanguagesOnlyActive);
    }

    public String getProviderName() {
        return user.getProviderName();
    }

    public boolean isProxied() {
        return user.isProxied();
    }

    public void setProxied(boolean proxied) {
        user.setProxied(proxied);
    }

    /**
     * Always return true to force bypass user aliasing
     *
     * @return
     */
    public boolean byPassUserAliasing() {
        return true;
    }

    public void setByPassUserAliasing(boolean bypassUserAliasing) {
        // do nothing
    }

    JahiaUser getUser () {
        return user;
    }
}
