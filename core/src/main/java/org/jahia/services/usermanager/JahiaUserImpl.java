/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.usermanager;

import java.io.Serializable;
import java.util.Properties;

/**
 * Represents a user with properties in the Digital Experience Manager.
 */
public class JahiaUserImpl implements JahiaUser, Serializable {

    private static final long serialVersionUID = -133494787519812151L;
    
    private final String name;
    private final String path;
    private final String realm;
    private final Properties properties;
    private final boolean isRoot;
    private final String providerName;

    /**
     * Initializes a global non-root user.
     * 
     * @param name
     *            the user name
     * @param path
     *            the JCR path of the user
     * @param properties
     *            user properties
     * @param providerName
     *            the name of the provider
     */
    public JahiaUserImpl(String name, String path, Properties properties, String providerName) {
        this(name, path, properties, false, providerName, null);
    }

    /**
     * Initializes a global user.
     * 
     * @param name
     *            the user name
     * @param path
     *            the JCR path of the user
     * @param properties
     *            user properties
     * @param isRoot
     *            <code>true</code> for root user and <code>false</code> for everyone else
     * @param providerName
     *            the name of the provider
     */
    public JahiaUserImpl(String name, String path, Properties properties, boolean isRoot, String providerName) {
        this(name, path, properties, isRoot, providerName, null);
    }

    /**
     * Initializes a realm-specific (site-specific) non-root user.
     * 
     * @param name
     *            the user name
     * @param path
     *            the JCR path of the user
     * @param properties
     *            user properties
     * @param providerName
     *            the name of the provider
     * @param realm
     *            the key of the corresponding user realm (site)
     */
    public JahiaUserImpl(String name, String path, Properties properties, String providerName, String realm) {
        this(name, path, properties, false, providerName, realm);
    }

    /**
     * Initializes a realm-specific (site-specific) user.
     * 
     * @param name
     *            the user name
     * @param path
     *            the JCR path of the user
     * @param properties
     *            user properties
     * @param isRoot
     *            <code>true</code> for root user and <code>false</code> for everyone else
     * @param providerName
     *            the name of the provider
     * @param realm
     *            the key of the corresponding user realm (site)
     */
    public JahiaUserImpl(String name, String path, Properties properties, boolean isRoot, String providerName, String realm) {
        this.name = name;
        this.path = path;
        this.properties = properties;
        this.isRoot = isRoot;
        this.providerName = providerName;
        this.realm = realm;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public String getUserKey() {
        return path;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public UserProperties getUserProperties() {
        return new UserProperties(properties, true);
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public UserProperty getUserProperty(String key) {
        return getUserProperties().getUserProperty(key);
    }

    @Override
    public boolean removeProperty(String key) {
        throw new UnsupportedOperationException("Setter not supported here, use JCRUserNode instead");
    }

    @Override
    public boolean setProperty(String key, String value) {
        throw new UnsupportedOperationException("Setter not supported here, use JCRUserNode instead");
    }

    @Override
    public boolean setPassword(String password) {
        throw new UnsupportedOperationException("Setter not supported here, use JCRUserNode instead");
    }

    @Override
    public boolean isMemberOfGroup(int siteID, String name) {
        throw new UnsupportedOperationException("Site id not supported, use sitekey");
    }

    @Override
    public boolean isAdminMember(int siteID) {
        throw new UnsupportedOperationException("Site id not supported, use sitekey");
    }

    @Override
    public boolean isRoot() {
        return isRoot;
    }

    @Override
    public boolean verifyPassword(String password) {
        throw new UnsupportedOperationException("Method not supported here, use JCRUserNode instead");
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLocalPath() {
        return path;
    }

    @Override
    public boolean isAccountLocked() {
        return !isRoot() && Boolean.valueOf(getProperty("j:accountLocked"));
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JahiaUserImpl jahiaUser = (JahiaUserImpl) o;

        if (!path.equals(jahiaUser.path)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
