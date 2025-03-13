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
package org.jahia.services.content.decorator;


import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.pwd.PasswordService;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PasswordHistoryEntry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserImpl;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;

import java.util.*;

/**
 * Represent a user JCR node.
 *
 * @author rincevent
 */
public class JCRUserNode extends JCRProtectedNodeAbstractDecorator {

    public static final String ROOT_USER_UUID = "b32d306a-6c74-11de-b3ef-001e4fead50b";
    public static final String PROVIDER_NAME = "jcr";
    public static final String J_DISPLAYABLE_NAME = "j:displayableName";
    public static final String J_PASSWORD = "j:password";
    public static final String J_EXTERNAL = "j:external";
    public static final String J_EXTERNAL_SOURCE = "j:externalSource";
    private static final String J_PUBLIC_PROPERTIES = "j:publicProperties";
    public final List<String> publicProperties = Arrays.asList(J_EXTERNAL, J_EXTERNAL_SOURCE, J_PUBLIC_PROPERTIES);

    private static final  Logger logger = LoggerFactory.getLogger(JCRUserNode.class);

    public JCRUserNode(JCRNodeWrapper node) {
        super(node);
    }

    public JahiaUser getJahiaUser() {
        Properties properties = new Properties();
        try {
            properties.putAll(getPropertiesAsString());
        } catch (RepositoryException e) {
            logger.error("Cannot read user properties",e);
        }
        return new JahiaUserImpl(getName(), getPath(), properties, isRoot(), getProviderName(), getRealm());

    }

    /**
     * @deprecated
     */
    public String getUsername() {
        return getName();
    }

    public String getUserKey() {
        return getPath();
    }

    public String getProviderName() {
        return getProvider().getKey();
    }

    public boolean isPropertyEditable(String name) {
        try {
            return !(J_EXTERNAL.equals(name) || Constants.CHECKIN_DATE.equals(name)) && canReadProperty(name);
        } catch (RepositoryException e) {
            return false;
        }
    }

    public boolean isRoot() {
        try {
            return getIdentifier().equals(JCRUserNode.ROOT_USER_UUID);
        } catch (RepositoryException e) {
            return false;
        }
    }

    @Override
    protected boolean canReadProperty(String s) throws RepositoryException {
        if (publicProperties.contains(s) || JahiaUserManagerService.isGuest(this) || node.hasPermission("jcr:write")) {
            return true;
        }
        if (node.hasProperty(s) && !"jnt:user".equals(node.getProperty(s).getDefinition().getDeclaringNodeType().getName())) {
            return true;
        }
        if (!node.hasProperty(J_PUBLIC_PROPERTIES)) {
            return false;
        }
        Property p = node.getProperty(J_PUBLIC_PROPERTIES);
        Value[] values = p.getValues();
        for (Value value : values) {
            if (s.equals(value.getString())) {
                return true;
            }
        }
        return false;
    }

    public boolean verifyPassword(String userPassword) {
        try {
            return StringUtils.isNotEmpty(userPassword) && PasswordService.getInstance().matches(userPassword, getProperty(J_PASSWORD).getString());
        } catch (RepositoryException e) {
            logger.warn("Unable to read j:password property for user: " + getName());
            return false;
        }
    }

    public boolean setPassword(String pwd) {
        try {
            setProperty(J_PASSWORD, PasswordService.getInstance().digest(pwd, isRoot()));
            return true;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean isAccountLocked() {
        try {
            return !isRoot() && hasProperty("j:accountLocked") && getProperty("j:accountLocked").getBoolean();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get the time before when the user sessions should be invalidated
     * @return the time in milliseconds, 0 to keep all existing session opened.
     */
    public long getInvalidatedSessionTime() {
        try {
            return hasProperty("j:invalidateSessionTime") ? getProperty("j:invalidateSessionTime").getLong() : 0L;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 0L;
        }
    }

    public List<PasswordHistoryEntry> getPasswordHistory() {
        return JahiaPasswordPolicyService.getInstance().getPasswordHistory(this);
    }

    public long getLastPasswordChangeTimestamp() {
        List<PasswordHistoryEntry> pwdHistory = getPasswordHistory();
        return pwdHistory.size() > 0 ? pwdHistory.get(0).getModificationDate().getTime() : 0;
    }

    public boolean isMemberOfGroup(String siteKey, String name) {
        return JCRContentUtils.isUserMemberOfGroup(siteKey, name, getName(), getRealm(), isRoot());
    }

    public String getRealm() {
        return getPath().startsWith("/sites/") ? StringUtils.substringBetween(getPath(), "/sites/", "/") : null;
    }

    /**
     * @deprecated for compatibility only, use getPath()
     */
    public String getLocalPath() {
        return getPath();
    }

    @Override
    public String getDisplayableName() {
        try {
            return getDisplayableName(getSession().getLocale());
        } catch (RepositoryException e) {
            logger.error("", e);
        }
        return super.getDisplayableName();
    }

    public String getDisplayableName(Locale locale) {
        final String userName = getName();
        if (Constants.GUEST_USERNAME.equals(userName)) {
            Locale l = locale;
            if (l == null) {
                try {
                    l = getSession().getLocale();
                } catch (RepositoryException e) {
                    logger.error("", e);
                }
                if (l == null) {
                    l = SettingsBean.getInstance().getDefaultLocale();
                }
                if (l == null) {
                    l = Locale.ENGLISH;
                }
            }
            return Messages.get(ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(SettingsBean.getInstance().getGuestUserResourceModuleName()),
                    SettingsBean.getInstance().getGuestUserResourceKey(), l, userName);
        }
        return super.getDisplayableName();
    }
}
