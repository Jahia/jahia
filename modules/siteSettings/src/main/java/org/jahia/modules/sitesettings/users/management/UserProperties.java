/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
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
 */
package org.jahia.modules.sitesettings.users.management;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.jcr.RepositoryException;

/**
 * @author rincevent
 */
public class UserProperties implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(UserProperties.class);
    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("^$|^[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@([A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*|\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])$");
    private static final long serialVersionUID = -817961657416283232L;
    private static final String[] BASIC_USER_PROPERTIES = new String[]{"j:firstName", "j:lastName", "j:email", "j:organization", "emailNotificationsDisabled", "j:accountLocked", "preferredLanguage"};

    public static void validateCreateUser(String username, String password, String passwordConfirm, String email,
                                          MessageContext messages) {
        if (username == null || username.isEmpty()) {
            messages.addMessage(new MessageBuilder()
                    .error()
                    .source("username")
                    .code("siteSettings.user.errors.username.mandatory").build());
        } else if (!ServicesRegistry.getInstance().getJahiaUserManagerService().isUsernameSyntaxCorrect(username)) {
            messages.addMessage(new MessageBuilder()
                    .error()
                    .source("username")
                    .code("siteSettings.user.errors.username.syntax").build());
        } else if (ServicesRegistry.getInstance().getJahiaUserManagerService().userExists(username)) {
            messages.addMessage(new MessageBuilder()
                    .error()
                    .source("username")
                    .defaultText(Messages.getInternal("label.username", LocaleContextHolder.getLocale()) + " '" + username + "' " +
                            Messages.get("resources.JahiaSiteSettings", "siteSettings.user.errors.username.exist",
                                    LocaleContextHolder.getLocale())).build());
        }
        validateEmail(email, messages);

        validatePassword(username, password, passwordConfirm, messages);
    }

    public static void validateEmail(String email, MessageContext messages) {
        if (StringUtils.isNotEmpty(email) && !EMAIL_PATTERN.matcher(email).matches()) {
            messages.addMessage(new MessageBuilder()
                    .error()
                    .source("email")
                    .code("siteSettings.user.errors.email").build());
        }
    }

    public static void validatePassword(String username, String password, String passwordConfirm,
                                        MessageContext messages) {
        JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry.getInstance().getJahiaPasswordPolicyService();
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(passwordConfirm)) {
            messages.addMessage(new MessageBuilder()
                    .error()
                    .source("password")
                    .code("siteSettings.user.errors.password.mandatory").build());
        } else {
            if (!passwordConfirm.equals(password)) {
                messages.addMessage(new MessageBuilder()
                        .error()
                        .source("passwordConfirm")
                        .code("siteSettings.user.errors.password.not.matching")
                        .build());
            } else {
                PolicyEnforcementResult evalResult = pwdPolicyService.enforcePolicyOnUserCreate(username, password);
                if (!evalResult.isSuccess()) {
                    List<String> textMessages = evalResult.getTextMessages();
                    for (String textMessage : textMessages) {
                        messages.addMessage(new MessageBuilder().error().source("password").defaultText(textMessage)
                                .build());
                    }
                }
            }
        }
    }

    public static void validatePasswordExistingUser(String userKey, String password, String passwordConfirm,
                                                    MessageContext messages) {
        JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry.getInstance().getJahiaPasswordPolicyService();
        if (!StringUtils.isEmpty(password)) {
            if (!password.equals(passwordConfirm)) {
                messages.addMessage(new MessageBuilder()
                        .error()
                        .source("passwordConfirm")
                        .code("siteSettings.user.errors.password.not.matching")
                        .build());
            } else {
                JCRUserNode jahiaUser = JahiaUserManagerService.getInstance().lookupUserByPath(userKey);
                PolicyEnforcementResult evalResult = pwdPolicyService.enforcePolicyOnPasswordChange(jahiaUser,
                        password, true);
                if (!evalResult.isSuccess()) {
                    List<String> textMessages = evalResult.getTextMessages();
                    for (String textMessage : textMessages) {
                        messages.addMessage(new MessageBuilder().error().source("password").defaultText(textMessage)
                                .build());
                    }
                }
            }
        }
    }

    public static void validateUpdateUser(String userKey, String password, String passwordConfirm, String email,
                                          MessageContext messages) {
        validatePasswordExistingUser(userKey, password, passwordConfirm, messages);
        validateEmail(email, messages);
    }

    private Boolean accountLocked = Boolean.FALSE;

    private String displayName;
    private String email;
    private Boolean emailNotificationsDisabled = Boolean.FALSE;
    private String firstName;
    private String lastName;
    private String localPath;
    private String organization;
    private String password;
    private String passwordConfirm;
    private Locale preferredLanguage;

    private boolean readOnly;
    private boolean external;

    private Set<String> readOnlyProperties = new HashSet<String>();

    private String userKey;

    private String username;

    public UserProperties() {
        super();
    }

    public UserProperties(String userKey) {
        this();
        setUserKey(userKey);
    }

    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getEmailNotificationsDisabled() {
        return emailNotificationsDisabled;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getOrganization() {
        return organization;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public Locale getPreferredLanguage() {
        return preferredLanguage;
    }

    public Set<String> getReadOnlyProperties() {
        return readOnlyProperties;
    }

    public void setReadOnlyProperties(Set<String> readOnlyProperties) {
        this.readOnlyProperties = readOnlyProperties;
    }

    public String getUserKey() {
        return userKey;
    }

    public String getUsername() {
        return username;
    }

    public Properties getUserProperties() {
        Properties properties = new Properties();
        properties.setProperty("j:lastName", getLastName());
        properties.setProperty("j:firstName", getFirstName());
        properties.setProperty("j:organization", getOrganization());
        properties.setProperty("j:email", getEmail());
        return properties;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailNotificationsDisabled(Boolean emailNotifications) {
        this.emailNotificationsDisabled = emailNotifications;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public void setPreferredLanguage(Locale preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void validateCreateUser(ValidationContext context) {
        validateCreateUser(username, password, passwordConfirm, email, context.getMessageContext());
    }

    public void validateEditUser(ValidationContext context) {
        validateUpdateUser(userKey, password, passwordConfirm, email, context.getMessageContext());
    }

    public void populate(JCRUserNode jahiaUser) {
        setFirstName(jahiaUser.getPropertyAsString("j:firstName"));
        setLastName(jahiaUser.getPropertyAsString("j:lastName"));
        setUsername(jahiaUser.getName());
        setUserKey(jahiaUser.getPath());
        setEmail(jahiaUser.getPropertyAsString("j:email"));
        setOrganization(jahiaUser.getPropertyAsString("j:organization"));

        try {
            if (jahiaUser.hasProperty("emailNotificationsDisabled")) {
                setEmailNotificationsDisabled(jahiaUser.getProperty("emailNotificationsDisabled").getBoolean());
            }

            if (jahiaUser.hasProperty("j:accountLocked")) {
                setAccountLocked(jahiaUser.getProperty("j:accountLocked").getBoolean());
            }

            if (jahiaUser.hasProperty("j:external")) {
                setExternal(jahiaUser.getProperty("j:external").getBoolean());
            }
        } catch (RepositoryException e) {
            logger.debug(e.getMessage(), e);
        }

        setPreferredLanguage(UserPreferencesHelper.getPreferredLocale(jahiaUser));
        setDisplayName(PrincipalViewHelper.getDisplayName(jahiaUser, LocaleContextHolder.getLocale()));
        setLocalPath(jahiaUser.getPath());

        Set<String> readOnlyProperties = new HashSet<String>();
        for (String p : BASIC_USER_PROPERTIES) {
            if (!jahiaUser.isPropertyEditable(p)) {
                readOnlyProperties.add(p);
            }
        }
        setReadOnlyProperties(readOnlyProperties);
    }

}
