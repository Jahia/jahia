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

package org.jahia.modules.serversettings.flow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.modules.serversettings.users.admin.AdminProperties;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.utils.Url;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.i18n.LocaleContextHolder;

public class SiteBean implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(SiteBean.class);
    private static final long serialVersionUID = 2151226556427659305L;
    private AdminProperties adminProperties;
    private boolean createAdmin = false;
    private boolean defaultSite = false;
    private String description;

    private String language;

    private List<String> modules = new ArrayList<String>();
    private String serverName = "localhost";

    private String siteKey = "mySite";

    private String templateSet;

    private String title = "My Site";

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
            packs.add(templateManagerService.getTemplatePackageByFileName(module));
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
                .getTemplatePackageByFileName(templateSet);
    }

    public String getTitle() {
        return title;
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

    public void validateCreateSite(ValidationContext context) {
        // check validity...
        String title = (String) context.getUserValue("title");
        String serverName = (String) context.getUserValue("serverName");
        String siteKey = (String) context.getUserValue("siteKey");

        MessageContext messages = context.getMessageContext();

        JahiaSitesService sitesService = ServicesRegistry.getInstance().getJahiaSitesService();

        try {
            if (title != null && (title.length() > 0) && serverName != null && (serverName.length() > 0)
                    && siteKey != null && (siteKey.length() > 0)) {
                if (!sitesService.isSiteKeyValid(siteKey)) {
                    messages.addMessage(new MessageBuilder()
                            .error()
                            .source("siteKey")
                            .defaultText(
                                    Messages.get("resources.JahiaServerSettings",
                                            "serverSettings.manageWebProjects.warningMsg.onlyLettersDigitsUnderscore",
                                            LocaleContextHolder.getLocale())).build());
                } else if (!sitesService.isServerNameValid(serverName)) {
                    messages.addMessage(new MessageBuilder()
                            .error()
                            .source("serverName")
                            .defaultText(
                                    Messages.get("resources.JahiaServerSettings","serverSettings.manageWebProjects.warningMsg.invalidServerName",
                                            LocaleContextHolder.getLocale())).build());
                } else if (!Url.isLocalhost(serverName) && sitesService.getSiteByServerName(serverName) != null) {
                    messages.addMessage(new MessageBuilder()
                            .error()
                            .source("serverName")
                            .defaultText(
                                    Messages.get("resources.JahiaServerSettings","serverSettings.manageWebProjects.warningMsg.chooseAnotherServerName",
                                            LocaleContextHolder.getLocale())).build());
                } else if (sitesService.getSiteByKey(siteKey) != null) {
                    messages.addMessage(new MessageBuilder()
                            .error()
                            .source("siteKey")
                            .defaultText(
                                    Messages.get("resources.JahiaServerSettings","serverSettings.manageWebProjects.warningMsg.chooseAnotherSiteKey",
                                            LocaleContextHolder.getLocale())).build());
                }
            } else {
                messages.addMessage(new MessageBuilder()
                        .error()
                        .source("siteKey")
                        .defaultText(
                                Messages.get("resources.JahiaServerSettings","serverSettings.manageWebProjects.warningMsg.completeRequestInfo",
                                        LocaleContextHolder.getLocale())).build());
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void validateCreateSiteSelectModules(ValidationContext context) {
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
                            .defaultText(
                                    Messages.get("resources.JahiaServerSettings","serverSettings.manageWebProjects.warningMsg.invalidServerName",
                                            LocaleContextHolder.getLocale())).build());
                } else if (!Url.isLocalhost(serverName) && sitesService.getSiteByServerName(serverName) != null && !sitesService.getSiteByServerName(serverName).getSiteKey().equals(siteKey)) {
                    messages.addMessage(new MessageBuilder()
                            .error()
                            .source("serverName")
                            .defaultText(
                                    Messages.get("resources.JahiaServerSettings","serverSettings.manageWebProjects.warningMsg.chooseAnotherServerName",
                                            LocaleContextHolder.getLocale())).build());
                }
            } else {
                messages.addMessage(new MessageBuilder()
                        .error()
                        .source("siteKey")
                        .defaultText(
                                Messages.get("resources.JahiaServerSettings","serverSettings.manageWebProjects.warningMsg.completeRequestInfo",
                                        LocaleContextHolder.getLocale())).build());
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }
}