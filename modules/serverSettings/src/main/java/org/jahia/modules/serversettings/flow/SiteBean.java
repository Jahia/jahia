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

import org.codehaus.plexus.util.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.modules.serversettings.users.admin.AdminProperties;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.utils.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SiteBean implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(SiteBean.class);
    private static final long serialVersionUID = 2151226556427659305L;
    private AdminProperties adminProperties;
    private boolean createAdmin = false;
    private boolean editModules = false;
    private boolean defaultSite = false;
    private String description;

    private String language;

    private List<String> modules = new ArrayList<String>();
    private String serverName = "localhost";

    private String siteKey = "mySite";

    private String templateSet;

    private String title = "My Site";

    private String templatePackageName;
    private String templateFolder;

    public AdminProperties getAdminProperties() {
        if (adminProperties == null) {
            adminProperties = new AdminProperties();
            adminProperties.setUsername(siteKey + "-admin");
        }
        return adminProperties;
    }

    public String getDescription() {
        return description;
    }

    public String getLanguage() {
        return language;
    }

    public List<JahiaTemplatesPackage> getModulePackages() {
        List<JahiaTemplatesPackage> packs = new LinkedList<JahiaTemplatesPackage>();
        JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance()
                .getJahiaTemplateManagerService();
        for (String module : modules) {
            packs.add(templateManagerService.getTemplatePackageById(module));
        }

        return packs;
    }

    public List<String> getModules() {
        return modules;
    }

    public String getServerName() {
        return serverName;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public String getTemplateSet() {
        return templateSet;
    }

    public JahiaTemplatesPackage getTemplateSetPackage() {
        return ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getTemplatePackageById(templateSet);
    }

    public String getTitle() {
        return title;
    }

    public String getTemplatePackageName() {
        return templatePackageName;
    }

    public String getTemplateFolder() {
        return templateFolder;
    }

    public boolean isCreateAdmin() {
        return createAdmin;
    }

    public boolean isDefaultSite() {
        return defaultSite;
    }

    public void setAdminProperties(AdminProperties adminProperties) {
        this.adminProperties = adminProperties;
    }

    public void setCreateAdmin(boolean createAdmin) {
        this.createAdmin = createAdmin;
    }

    public void setDefaultSite(boolean defaultSite) {
        this.defaultSite = defaultSite;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setModules(List<String> modules) {
        if (modules == null) {
            this.modules = new ArrayList<String>();
        } else {
            this.modules = modules;
        }
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public void setTemplateSet(String templateSet) {
        this.templateSet = templateSet;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTemplatePackageName(String templatePackageName) {
        this.templatePackageName = templatePackageName;
    }

    public void setTemplateFolder(String templateFolder) {
        this.templateFolder = templateFolder;
    }

    public boolean isEditModules() {
        return editModules;
    }

    public void setEditModules(boolean editModules) {
        this.editModules = editModules;
    }

    public void validateCreateSite(ValidationContext context) {
        // check validity...
        String title = (String) context.getUserValue("title");
        String serverName = (String) context.getUserValue("serverName");
        String siteKey = (String) context.getUserValue("siteKey");

        MessageContext messages = context.getMessageContext();

        JahiaSitesService sitesService = ServicesRegistry.getInstance().getJahiaSitesService();

        try {

            JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();

            if (templateManagerService.getNonSystemTemplateSetPackages().isEmpty()) {
                messages.addMessage(new MessageBuilder()
                        .error()
                        .code("serverSettings.manageWebProjects.warningMsg.noTemplateSets").build());
            }

            if (title != null && (title.length() > 0) && serverName != null && (serverName.length() > 0)
                    && siteKey != null && (siteKey.length() > 0)) {
                if (!sitesService.isSiteKeyValid(siteKey)) {
                    messages.addMessage(new MessageBuilder().error().source("siteKey")
                            .code("serverSettings.manageWebProjects.warningMsg.onlyLettersDigitsUnderscore").build());
                } else if (!sitesService.isServerNameValid(serverName)) {
                    messages.addMessage(new MessageBuilder()
                            .error()
                            .source("serverName")
                            .code("serverSettings.manageWebProjects.warningMsg.invalidServerName").build());
                } else if (!Url.isLocalhost(serverName) && sitesService.getSiteByServerName(serverName) != null) {
                    messages.addMessage(new MessageBuilder()
                            .error()
                            .source("serverName")
                            .code("serverSettings.manageWebProjects.warningMsg.chooseAnotherServerName").build());
                } else if (sitesService.getSiteByKey(siteKey) != null) {
                    messages.addMessage(new MessageBuilder()
                            .error()
                            .source("siteKey")
                            .code("serverSettings.manageWebProjects.warningMsg.chooseAnotherSiteKey").build());
                }
            } else {
                messages.addMessage(new MessageBuilder()
                        .error()
                        .source("siteKey")
                        .code("serverSettings.manageWebProjects.warningMsg.completeRequestInfo").build());
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void validateCreateSiteSelectModules(ValidationContext context) {
        MessageContext messages = context.getMessageContext();

        JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();

        if (templateManagerService.getNonSystemTemplateSetPackages().isEmpty()) {
            messages.addMessage(new MessageBuilder()
                    .error()
                    .source("templateSet")
                    .code("serverSettings.manageWebProjects.warningMsg.noTemplateSets").build());
        }
    }

    public void validateEditSite(ValidationContext context) {
        // check validity...
        MessageContext messages = context.getMessageContext();

        JahiaSitesService sitesService = ServicesRegistry.getInstance().getJahiaSitesService();

        try {
            if (StringUtils.isNotBlank(title) && StringUtils.isNotBlank(serverName)) {
                if (!sitesService.isServerNameValid(serverName)) {
                    messages.addMessage(new MessageBuilder()
                            .error()
                            .source("serverName")
                            .code("serverSettings.manageWebProjects.warningMsg.invalidServerName").build());
                } else if (!Url.isLocalhost(serverName) && sitesService.getSiteByServerName(serverName) != null && !sitesService.getSiteByServerName(serverName).getSiteKey().equals(siteKey)) {
                    messages.addMessage(new MessageBuilder()
                            .error()
                            .source("serverName")
                            .code("serverSettings.manageWebProjects.warningMsg.chooseAnotherServerName").build());
                }
            } else {
                messages.addMessage(new MessageBuilder()
                        .error()
                        .source("siteKey")
                        .code("serverSettings.manageWebProjects.warningMsg.completeRequestInfo").build());
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }
}