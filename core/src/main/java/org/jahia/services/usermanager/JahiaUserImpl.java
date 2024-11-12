/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
 * Represents a user with properties in Jahia.
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
