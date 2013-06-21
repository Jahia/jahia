/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.modules.serversettings.users.management;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.i18n.Messages;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * @author rincevent
 */
public class UserProperties implements Serializable {
    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("^$|^[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@([A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*|\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])$");
    private static final long serialVersionUID = -817961657416283232L;

    public static void validateCreateUser(String username, String password, String passwordConfirm, String email,
            MessageContext messages) {
        if (username == null || username.isEmpty()) {
            messages.addMessage(new MessageBuilder()
                    .error()
                    .source("username")
                    .defaultText(
                            Messages.get("resources.JahiaServerSettings", "serverSettings.user.errors.username.mandatory",
                                    LocaleContextHolder.getLocale())).build());
        } else if (!ServicesRegistry.getInstance().getJahiaUserManagerService().isUsernameSyntaxCorrect(username)) {
            messages.addMessage(new MessageBuilder()
                    .error()
                    .source("username")
                    .defaultText(
                            Messages.get("resources.JahiaServerSettings", "serverSettings.user.errors.username.syntax",
                                    LocaleContextHolder.getLocale())).build());
        } else if (ServicesRegistry.getInstance().getJahiaUserManagerService().userExists(username)) {
            messages.addMessage(new MessageBuilder()
                    .error()
                    .source("username")
                    .defaultText(Messages.getInternal("label.username", LocaleContextHolder.getLocale()) + " '" + username + "' " + 
                            Messages.get("resources.JahiaServerSettings", "serverSettings.user.errors.username.exist",
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
                    .defaultText(
                            Messages.get("resources.JahiaServerSettings", "serverSettings.user.errors.email",
                                    LocaleContextHolder.getLocale())).build());
        }
    }

    public static void validatePassword(String username, String password, String passwordConfirm,
            MessageContext messages) {
        JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry.getInstance().getJahiaPasswordPolicyService();
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(passwordConfirm)) {
            messages.addMessage(new MessageBuilder()
                    .error()
                    .source("password")
                    .defaultText(
                            Messages.get("resources.JahiaServerSettings", "serverSettings.user.errors.password.mandatory",
                                    LocaleContextHolder.getLocale())).build());
        } else {
            if (!passwordConfirm.equals(password)) {
                messages.addMessage(new MessageBuilder()
                        .error()
                        .source("passwordConfirm")
                        .defaultText(
                                Messages.get("resources.JahiaServerSettings",
                                        "serverSettings.user.errors.password.not.matching", LocaleContextHolder.getLocale()))
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
                        .defaultText(
                                Messages.get("resources.JahiaServerSettings",
                                        "serverSettings.user.errors.password.not.matching", LocaleContextHolder.getLocale()))
                        .build());
            } else {
                JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService()
                        .lookupUserByKey(userKey);
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
}
