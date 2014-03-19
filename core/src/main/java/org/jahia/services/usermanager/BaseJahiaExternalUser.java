/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;

/**
 * Represent a base class for an external user.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class BaseJahiaExternalUser implements JahiaExternalUser {

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
        return (JahiaUserManagerProvider) ServicesRegistry.getInstance()
                .getJahiaUserManagerService().getProvider(providerKey);
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
        if (null == key) {
            return null;
        }
        return externalProperties.hasProperty(key) ? externalProperties.getUserProperty(key)
                : getUserProperties().getUserProperty(key);
    }

    @Override
    public int hashCode() {
        return getUserKey().hashCode();
    }

    protected UserProperties initializeMergedProperties() {
        JCRUser jcrUser = lookupExternalUser();
        return jcrUser != null ? jcrUser.getUserProperties() : null;
    }

    public boolean isAdminMember(int siteID) {
        return isMemberOfGroup(siteID, JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
    }

    public boolean isMemberOfGroup(int siteID, String name) {
        ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
        if (servicesRegistry != null) {
            // lookup the requested group
            JahiaGroup group = servicesRegistry.getJahiaGroupManagerService().lookupGroup(siteID, name);
            if (group != null) {
                return group.isMember(this);
            }
        }
        return false;
    }

    public final boolean isRoot() {
        return false;
    }

    protected JCRUser lookupExternalUser() {
        return ((JCRUserManagerProvider) SpringContextSingleton.getBean("JCRUserManagerProvider"))
                .lookupExternalUser(this);
    }

    public boolean removeProperty(String key) {
        if (null == key) {
            return false;
        }

        boolean success = false;
        UserProperty external = externalProperties.getUserProperty(key);
        if (external != null) {
            // it is an external property
            if (!external.isReadOnly()) {
                success = removePropertyExternal(key);
                if (success) {
                    externalProperties.removeUserProperty(key);
                    properties.removeUserProperty(key);
                }
            }
        } else {
            // this is an internal JCR property
            success = removePropertyInternal(key);
        }

        return success;
    }

    protected abstract boolean removePropertyExternal(String key);

    protected boolean removePropertyInternal(String key) {
        JCRUser jcrUser = lookupExternalUser();
        return jcrUser != null && jcrUser.removeProperty(key);
    }

    public boolean setProperty(String key, String value) {
        if (null == key) {
            return false;
        }

        boolean success = false;
        UserProperty external = externalProperties.getUserProperty(key);
        if (external != null) {
            // it is an external property
            if (!external.isReadOnly()) {
                success = setPropertyExternal(key, value);
                if (success) {
                    try {
                        externalProperties.setProperty(key, value);
                        properties.setProperty(key, value);
                    } catch (UserPropertyReadOnlyException e) {
                        JahiaUserManagerService.LOGGER.warn(
                                "Cannot set read-only property {} for user {}", key, userKey);
                    }
                }
            }
        } else {
            // this is an internal JCR property
            success = setPropertyInternal(key, value);
            try {
                properties.setProperty(key, value);
            } catch (UserPropertyReadOnlyException e) {
                JahiaUserManagerService.LOGGER.warn("Cannot set read-only property {} for user {}",
                        key, userKey);
            }
        }

        return success;
    }

    protected abstract boolean setPropertyExternal(String key, String value);

    protected boolean setPropertyInternal(String key, String value) {
        JCRUser jcrUser = lookupExternalUser();
        if (jcrUser == null) {
            // deploy
            try {
                long timer = System.currentTimeMillis();
                JCRStoreService.getInstance().deployExternalUser(this);
                JahiaUserManagerService.LOGGER.info(
                        "Created internal user node for user {} in {} ms", userKey,
                        (System.currentTimeMillis() - timer));
                jcrUser = lookupExternalUser();
            } catch (RepositoryException e) {
                JahiaUserManagerService.LOGGER.error("Error deploying external user '" + getName()
                        + "' for provider '" + getProviderName() + "' into JCR repository. Cause: "
                        + e.getMessage(), e);
            }
        }

        return jcrUser != null && jcrUser.setProperty(key, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("username", username).append("userKey", userKey)
                .append("provider", providerKey).append("localPath", getLocalPath())
                .append("externalProperties", externalProperties).toString();
    }

    public boolean verifyPassword(String passwordToVerify) {
        if (passwordToVerify == null) {
            return false;
        }

        String encrypted = JahiaUserManagerService.encryptPassword(passwordToVerify);

        // if we have a cached password and it matches the provided one
        if (StringUtils.isNotEmpty(password) && password.equals(encrypted)) {
            return true;
        }
        // otherwise we perform verification in an external system

        boolean granted = verifyPasswordExternal(passwordToVerify);
        if (granted) {
            // cache the accepted password in encrypted form
            password = encrypted;
        }

        return granted;
    }

    protected boolean verifyPasswordExternal(String password) {
        return getProvider().login(getUserKey(), password);
    }

    public boolean isAccountLocked() {
        return !isRoot() && Boolean.valueOf(getProperty("j:accountLocked"));
    }
}
