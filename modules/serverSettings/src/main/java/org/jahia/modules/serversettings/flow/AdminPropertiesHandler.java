package org.jahia.modules.serversettings.flow;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.serversettings.adminproperties.AdminProperties;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;

import java.io.Serializable;

public class AdminPropertiesHandler implements Serializable {

    private AdminProperties adminProperties;

    /**
     * first method call in the flow. It instantiates and populates the AdminProperties bean
     */
    public void init() {
        JCRUser rootNode = JCRUserManagerProvider.getInstance().lookupRootUser();
        adminProperties = new AdminProperties(rootNode.getName());
        adminProperties.setFirstName(rootNode.getProperty("j:firstName"));
        adminProperties.setLastName(rootNode.getProperty("j:lastName"));
        adminProperties.setEmail(rootNode.getProperty("j:email"));
        adminProperties.setOrganization(rootNode.getProperty("j:organization"));
        adminProperties.setEmailNotificationsDisabled(rootNode.getProperty("emailNotificationsDisabled") != null);
    }

    /**
     * save the bean in the JCR
     */
    public void save() {
        JCRUser rootNode = JCRUserManagerProvider.getInstance().lookupRootUser();
        if (!StringUtils.isEmpty(adminProperties.getPassword())) {
            rootNode.setPassword(adminProperties.getPassword());
        }
        rootNode.setProperty("j:lastName",adminProperties.getLastName());
        rootNode.setProperty("j:firstName",adminProperties.getFirstName());
        rootNode.setProperty("j:organization",adminProperties.getOrganization());
        rootNode.setProperty("emailNotificationsDisabled",Boolean.toString(adminProperties.isEmailNotificationsDisabled()));
        rootNode.setProperty("j:email",adminProperties.getEmail());
        if (adminProperties.isEmailNotificationsDisabled()) {
            rootNode.setProperty("emailNotificationsDisabled","on");
        } else {
            rootNode.removeProperty("emailNotificationsDisabled");
        }

    }

    public AdminProperties getAdminProperties() {
        return adminProperties;
    }
}