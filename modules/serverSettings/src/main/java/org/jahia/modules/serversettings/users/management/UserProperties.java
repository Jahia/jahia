package org.jahia.modules.serversettings.users.management;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;

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
                            Messages.get("resources.JahiaServerSettings", "label.user.errors.username.mandatory",
                                    LocaleContextHolder.getLocale())).build());
        } else if (!ServicesRegistry.getInstance().getJahiaUserManagerService().isUsernameSyntaxCorrect(username)) {
            messages.addMessage(new MessageBuilder()
                    .error()
                    .source("username")
                    .defaultText(
                            Messages.get("resources.JahiaServerSettings", "label.user.errors.username.syntax",
                                    LocaleContextHolder.getLocale())).build());
        } else if (ServicesRegistry.getInstance().getJahiaUserManagerService().userExists(username)) {
            messages.addMessage(new MessageBuilder()
                    .error()
                    .source("username")
                    .defaultText(
                            Messages.get("resources.JahiaServerSettings", "label.user.errors.username.exist",
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
                            Messages.get("resources.JahiaServerSettings", "label.user.errors.email",
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
                            Messages.get("resources.JahiaServerSettings", "label.user.errors.password.mandatory",
                                    LocaleContextHolder.getLocale())).build());
        } else {
            if (!passwordConfirm.equals(password)) {
                messages.addMessage(new MessageBuilder()
                        .error()
                        .source("passwordConfirm")
                        .defaultText(
                                Messages.get("resources.JahiaServerSettings",
                                        "label.user.errors.password.not.matching", LocaleContextHolder.getLocale()))
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
                                        "label.user.errors.password.not.matching", LocaleContextHolder.getLocale()))
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
    private List<JahiaGroup> groups;
    private String lastName;
    private String localPath;
    private String organization;
    private String password;
    private String passwordConfirm;
    private Locale preferredLanguage;

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

    public List<JahiaGroup> getGroups() {
        return groups;
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

    public void setGroups(List<JahiaGroup> groups) {
        this.groups = groups;
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
