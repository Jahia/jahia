package org.jahia.modules.serversettings.users.management;

import org.apache.commons.lang.StringUtils;
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
 * @author rincevent
 */
public class UserProperties implements Serializable {
    private static final long serialVersionUID = -817961657416283232L;
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
            messages.addMessage(new MessageBuilder().error().source("username").defaultText(Messages.get(
                    "resources.JahiaServerSettings", "label.user.errors.username.mandatory",
                    LocaleContextHolder.getLocale())).build());
        } else if (!ServicesRegistry.getInstance().getJahiaUserManagerService().isUsernameSyntaxCorrect(username)) {
            messages.addMessage(new MessageBuilder().error().source("username").defaultText(Messages.get(
                    "resources.JahiaServerSettings",
                    "label.user.errors.username.syntax",
                    LocaleContextHolder.getLocale())).build());
        } else if (ServicesRegistry.getInstance().getJahiaUserManagerService().userExists(username)) {
            messages.addMessage(new MessageBuilder().error().source("username").defaultText(Messages.get(
                    "resources.JahiaServerSettings", "label.user.errors.username.exist", LocaleContextHolder.getLocale())).build());
        }
        validateEmail(messages);

        JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry.getInstance().getJahiaPasswordPolicyService();
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(passwordConfirm)) {
            messages.addMessage(new MessageBuilder().error().source("password").defaultText(Messages.get(
                    "resources.JahiaServerSettings", "label.user.errors.password.mandatory", LocaleContextHolder.getLocale())).build());
        } else {
            if (!passwordConfirm.equals(password)) {
                messages.addMessage(new MessageBuilder().error().source("passwordConfirm").defaultText(
                        Messages.get("resources.JahiaServerSettings", "label.user.errors.password.not.matching",
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

    private void validateEmail(MessageContext messages) {
        if (StringUtils.isNotEmpty(email) && !email.matches(
                "^$|^[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@([A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*|\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])$")) {
            messages.addMessage(new MessageBuilder().error().source("email").defaultText(Messages.get(
                    "resources.JahiaServerSettings", "label.user.errors.email", LocaleContextHolder.getLocale())).build());
        }
    }

    public void validateEditUser(ValidationContext context) {
        MessageContext messages = context.getMessageContext();
        validateEmail(messages);

        JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry.getInstance().getJahiaPasswordPolicyService();
        if (!StringUtils.isEmpty(password)) {
            if (!password.equals(passwordConfirm)) {
                messages.addMessage(new MessageBuilder().error().source("passwordConfirm").defaultText(
                        Messages.get("resources.JahiaServerSettings", "label.user.errors.password.not.matching",
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
