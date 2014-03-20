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
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
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
package org.jahia.modules.serversettings.flow;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.serversettings.users.admin.AdminProperties;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.taglibs.user.User;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;

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

        messages.addMessage(new MessageBuilder().info().code("label.changeSaved").build());
    }
    public List<JahiaGroup> getUserMembership() {
        return new LinkedList<JahiaGroup>(User.getUserMembership(JCRUserManagerProvider.getInstance().lookupRootUser().getUsername()).values());
    }

}