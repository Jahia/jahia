package org.jahia.modules.serversettings.flow;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.serversettings.users.admin.AdminProperties;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.utils.i18n.Messages;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.i18n.LocaleContextHolder;

public class AdminPropertiesHandler implements Serializable {
    private static final long serialVersionUID = -1665000223980422529L;

    private AdminProperties adminProperties;

    public AdminProperties getAdminProperties() {
        return adminProperties;
    }

    /**
     * first method call in the flow. It instantiates and populates the AdminProperties bean
     */
    public void init() {
        JCRUser rootNode = JCRUserManagerProvider.getInstance().lookupRootUser();
        adminProperties = new AdminProperties();
        UsersFlowHandler.populateUser(rootNode.getUserKey(), adminProperties);
    }

    /**
     * save the bean in the JCR
     */
    public void save(MessageContext messages) {
        JCRUser rootNode = JCRUserManagerProvider.getInstance().lookupRootUser();
        if (!StringUtils.isEmpty(adminProperties.getPassword())) {
            rootNode.setPassword(adminProperties.getPassword());
        }
        if (!StringUtils.equals(rootNode.getProperty("j:lastName"), adminProperties.getLastName())) {
            rootNode.setProperty("j:lastName", adminProperties.getLastName());
        }
        if (!StringUtils.equals(rootNode.getProperty("j:firstName"), adminProperties.getFirstName())) {
            rootNode.setProperty("j:firstName", adminProperties.getFirstName());
        }
        if (!StringUtils.equals(rootNode.getProperty("j:organization"), adminProperties.getOrganization())) {
            rootNode.setProperty("j:organization", adminProperties.getOrganization());
        }
        if (!StringUtils.equals(rootNode.getProperty("emailNotificationsDisabled"), adminProperties
                .getEmailNotificationsDisabled().toString())) {
            rootNode.setProperty("emailNotificationsDisabled",
                    Boolean.toString(adminProperties.getEmailNotificationsDisabled()));
        }
        if (!StringUtils.equals(rootNode.getProperty("j:email"), adminProperties.getEmail())) {
            rootNode.setProperty("j:email", adminProperties.getEmail());
        }
        String lang = adminProperties.getPreferredLanguage().toString();
        if (!StringUtils.equals(rootNode.getProperty("preferredLanguage"), lang)) {
            rootNode.setProperty("preferredLanguage", lang);
        }

        messages.addMessage(new MessageBuilder()
                .info()
                .defaultText(
                        Messages.get("resources.JahiaServerSettings", "label.user.edit.successful",
                                LocaleContextHolder.getLocale())).build());
    }
}