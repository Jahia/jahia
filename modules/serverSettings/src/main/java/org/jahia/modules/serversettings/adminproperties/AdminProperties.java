package org.jahia.modules.serversettings.adminproperties;

import org.apache.commons.lang.StringUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.utils.i18n.Messages;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Serializable;
import java.util.Properties;

/**
 * Bean to handle Admin properties flow.
 * User: david
 * Date: 3/12/13
 * Time: 2:15 PM
 */
public class AdminProperties implements Serializable {

    private String firstName;
    private String lastName;
    private String email;
    private boolean emailNotificationsDisabled;
    private String organization;
    private String password;
    private String confirmPassword;
    private String userName;

    public AdminProperties(String userName) {
        this.userName = userName;
    }

    /**
     * Perform a validation on email and password
     * @param context
     */
    public void validateAdminPropertiesForm (ValidationContext context) {
        //Validate email
        MessageContext messages = context.getMessageContext();
        email = email != null ? email.trim() : null;
        if(StringUtils.isNotEmpty(email) && !email.matches("^$|^[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@([A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*|\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])$")){
            messages.addMessage(new MessageBuilder().error().source("email").defaultText(Messages.getInternal("org.jahia.admin.userMessage.emailFormatIsIncorrect.label", LocaleContextHolder.getLocale())).build());
        }
        if (!StringUtils.isEmpty(password)) {
            if (StringUtils.equals(password,confirmPassword)) {
                JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry.getInstance().getJahiaPasswordPolicyService();
                PolicyEnforcementResult evalResult = null;
                evalResult = pwdPolicyService.enforcePolicyOnUserCreate(userName, password);
                if (!evalResult.isSuccess()) {
                    for (String message : evalResult.getTextMessages()) {
                        messages.addMessage(new MessageBuilder().error().source("password").defaultText(message).build());
                    }
                }

            } else {
                messages.addMessage(new MessageBuilder().error().source("password").defaultText(Messages.getInternal("org.jahia.admin.userMessage.passwdNotMatch.label",LocaleContextHolder.getLocale())).build());
            }
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailNotificationsDisabled() {
        return emailNotificationsDisabled;
    }

    public void setEmailNotificationsDisabled(boolean emailNotificationsDisabled) {
        this.emailNotificationsDisabled = emailNotificationsDisabled;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Properties getUserProperties() {
        Properties properties = new Properties();
        properties.setProperty("j:lastName", getLastName());
        properties.setProperty("j:firstName", getFirstName());
        properties.setProperty("j:organization", getOrganization());
        properties.setProperty("j:email", getEmail());
        return properties;
    }
}
