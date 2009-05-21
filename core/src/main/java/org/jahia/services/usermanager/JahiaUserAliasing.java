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
