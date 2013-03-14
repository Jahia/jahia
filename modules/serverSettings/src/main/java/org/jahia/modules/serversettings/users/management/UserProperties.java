/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.serversettings.users.management;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.i18n.Messages;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 13/03/13
 */
public class UserProperties implements Serializable {
    private transient static Logger logger = Logger.getLogger(UserProperties.class);
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String organization;
    private Boolean emailNotifications = Boolean.FALSE;
    private Locale preferredLanguage;
    private String password;
    private String passwordConfirm;
    private Boolean accountLocked = Boolean.FALSE;
    private List<JahiaGroup> groups;
    private String displayName;
    private String userKey;
    private String localPath;

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }


    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public void setPreferredLanguage(Locale preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public String getFirstName() {
        return firstName;
    }

    public List<JahiaGroup> getGroups() {
        return groups;
    }

    public String getLastName() {
        return lastName;
    }

    public String getOrganization() {
        return organization;
    }

    public String getPassword() {
        return password;
    }

    public Locale getPreferredLanguage() {
        return preferredLanguage;
    }

    public String getUsername() {
        return username;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getUserKey() {
        return userKey;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public void setGroups(List<JahiaGroup> groups) {
        this.groups = groups;
    }

    public void validateCreateUser(ValidationContext context) {
        MessageContext messages = context.getMessageContext();
        if (username == null || username.isEmpty()) {
            messages.addMessage(new MessageBuilder().error().source("username").defaultText(Messages.getInternal(
                    "org.jahia.admin.userMessage.specifyUserName.label", LocaleContextHolder.getLocale())).build());
        } else if (!ServicesRegistry.getInstance().getJahiaUserManagerService().isUsernameSyntaxCorrect(username)) {
            messages.addMessage(new MessageBuilder().error().source("username").defaultText(Messages.getInternal(
                    "org.jahia.admin.users.ManageUsers.onlyCharacters.label",
                    LocaleContextHolder.getLocale())).build());
        } else if (ServicesRegistry.getInstance().getJahiaUserManagerService().userExists(username)) {
            messages.addMessage(new MessageBuilder().error().source("username").defaultText(Messages.getInternal(
                    "org.jahia.admin.userMessage.alreadyExist.label", LocaleContextHolder.getLocale())).build());
        }
        if (StringUtils.isNotEmpty(email) && !email.matches(
                "^$|^[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@([A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*|\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])$")) {
            messages.addMessage(new MessageBuilder().error().source("email").defaultText(Messages.getInternal(
                    "org.jahia.admin.userMessage.emailFormatIsIncorrect.label",
                    LocaleContextHolder.getLocale())).build());
        }

        JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry.getInstance().getJahiaPasswordPolicyService();
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(passwordConfirm)) {
            messages.addMessage(new MessageBuilder().error().source("password").defaultText(Messages.getInternal(
                    "org.jahia.admin.userMessage.specifyPassword.label", LocaleContextHolder.getLocale())).build());
        } else {
            if (!passwordConfirm.equals(password)) {
                messages.addMessage(new MessageBuilder().error().source("passwordConfirm").defaultText(
                        Messages.getInternal("org.jahia.admin.userMessage.passwdNotMatch.label",
                                LocaleContextHolder.getLocale())).build());
            } else {
                PolicyEnforcementResult evalResult = pwdPolicyService.enforcePolicyOnUserCreate(username, password);
                if (!evalResult.isSuccess()) {
                    List<String> textMessages = evalResult.getTextMessages();
                    for (String textMessage : textMessages) {
                        messages.addMessage(new MessageBuilder().error().source("password").defaultText(
                                textMessage).build());
                    }
                }
            }
        }
    }

    public void validateEditUser(ValidationContext context) {
        MessageContext messages = context.getMessageContext();
        if (StringUtils.isNotEmpty(email) && !email.matches(
                "^$|^[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@([A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*|\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])$")) {
            messages.addMessage(new MessageBuilder().error().source("email").defaultText(Messages.getInternal(
                    "org.jahia.admin.userMessage.emailFormatIsIncorrect.label",
                    LocaleContextHolder.getLocale())).build());
        }

        JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry.getInstance().getJahiaPasswordPolicyService();
        if (!StringUtils.isEmpty(password)) {
            if (!password.equals(passwordConfirm)) {
                messages.addMessage(new MessageBuilder().error().source("passwordConfirm").defaultText(
                        Messages.getInternal("org.jahia.admin.userMessage.passwdNotMatch.label",
                                LocaleContextHolder.getLocale())).build());
            } else {
                JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(
                        userKey);
                PolicyEnforcementResult evalResult = pwdPolicyService.enforcePolicyOnPasswordChange(jahiaUser, password,
                        true);
                if (!evalResult.isSuccess()) {
                    List<String> textMessages = evalResult.getTextMessages();
                    for (String textMessage : textMessages) {
                        messages.addMessage(new MessageBuilder().error().source("password").defaultText(
                                textMessage).build());
                    }
                }
            }
        }
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getLocalPath() {
        return localPath;
    }
}
