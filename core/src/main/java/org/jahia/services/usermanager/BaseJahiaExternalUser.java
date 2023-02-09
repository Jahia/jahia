/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jahia.registries.ServicesRegistry;

/**
 * Represent a base class for an external user.
 * This class is keep to maintain compatibility with olds users/groups providers
 * @author Sergiy Shyrkov
 * @deprecated
 */
@Deprecated
public abstract class BaseJahiaExternalUser implements JahiaUser {

    private static final long serialVersionUID = 1431128187514460913L;

    protected UserProperties externalProperties = new UserProperties();

    protected String password;

    protected String path;

    // keep as as null initially to check that they need to be initialized first
    protected UserProperties properties;

    protected String providerKey;

    protected String userKey;

    protected String username;

    /**
     * Initializes an instance of this class.
     *
     * @param providerKey
     *            the corresponding provider key
     * @param username
     *            the user name
     * @param userKey
     *            the key of this user
     */
    public BaseJahiaExternalUser(String providerKey, String username, String userKey) {
        super();
        this.providerKey = providerKey;
        this.username = username;
        this.userKey = userKey;
    }

    /**
     * Initializes an instance of this class.
     *
     * @param providerKey
     *            the corresponding provider key
     * @param username
     *            the user name
     * @param userKey
     *            the key of this user
     * @param externalProperties
     *            the user properties in the external storage system
     */
    public BaseJahiaExternalUser(String providerKey, String username, String userKey,
                                 UserProperties externalProperties) {
        this(providerKey, username, userKey);
        if (externalProperties != null) {
            this.externalProperties = externalProperties;
        }
    }

    @Override
    public boolean equals(Object another) {
        if (this == another)
            return true;

        if (another != null && this.getClass() == another.getClass()) {
            return (getUserKey().equals(((JahiaUser) another).getUserKey()));
        }
        return false;
    }

    public UserProperties getExternalProperties() {
        return externalProperties;
    }

    public String getLocalPath() {
        if (path == null) {
            path = ServicesRegistry.getInstance().getJahiaUserManagerService()
                    .getUserSplittingRule().getPathForUsername(getUsername());
        }
        return path;
    }

    public String getName() {
        return username;
    }

    public Properties getProperties() {
        return getUserProperties().getProperties();
    }

    public String getProperty(String key) {
        if (null == key) {
            return null;
        }
        return externalProperties.hasProperty(key) ? externalProperties.getProperty(key)
                : getUserProperties().getProperty(key);
    }

    protected JahiaUserManagerProvider getProvider() {
        return null;
    }

    public String getProviderName() {
        return providerKey;
    }

    public String getUserKey() {
        return userKey;
    }

    public String getUsername() {
        return username;
    }

    public UserProperties getUserProperties() {
        if (properties == null) {
            UserProperties props = initializeMergedProperties();
            properties = props != null ? props : new UserProperties(externalProperties);
        }
        return properties;
    }

    public UserProperty getUserProperty(String key) {
        return null;
    }

    @Override
    public int hashCode() {
        return getUserKey().hashCode();
    }

    protected UserProperties initializeMergedProperties() {
        return null;
    }

    public boolean isAdminMember(int siteID) {
       return false;
    }

    public boolean isMemberOfGroup(int siteID, String name) {
        return false;
    }

    public final boolean isRoot() {
        return false;
    }

    /*protected JCRUser lookupExternalUser() {
        return ((JCRUserManagerProvider) SpringContextSingleton.getBean("JCRUserManagerProvider"))
                .lookupExternalUser(this);
    }*/

    public boolean removeProperty(String key) {
        return false;
    }

    protected abstract boolean removePropertyExternal(String key);

    protected boolean removePropertyInternal(String key) {
        return false;
    }

    public boolean setProperty(String key, String value) {
        return false;
    }

    protected abstract boolean setPropertyExternal(String key, String value);

    @Deprecated
    protected boolean setPropertyInternal(String key, String value) {
        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("username", username).append("userKey", userKey)
                .append("provider", providerKey).append("localPath", getLocalPath())
                .append("externalProperties", externalProperties).toString();
    }

    public boolean verifyPassword(String passwordToVerify) {
        return false;
    }

    protected boolean verifyPasswordExternal(String password) {
        return false;
    }

    public boolean isAccountLocked() {
        return false;
    }

    @Override
    public String getRealm() {
        return null;
    }
}
