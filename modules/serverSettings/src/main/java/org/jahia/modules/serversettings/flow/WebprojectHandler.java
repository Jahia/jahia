package org.jahia.modules.serversettings.flow;

import org.jahia.exceptions.JahiaException;
import org.jahia.modules.serversettings.adminproperties.AdminProperties;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Url;
import org.jahia.utils.i18n.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Handle creation of webprojects in webflow
 */
public class WebprojectHandler implements Serializable {
    private List<JahiaSite> sites;

    @Autowired
    private transient JahiaSitesBaseService sitesService;

    @Autowired
    private transient JahiaUserManagerService userManagerService;

    @Autowired
    private transient JahiaGroupManagerService groupManagerService;

    public void setSitesService(JahiaSitesBaseService sitesService) {
        this.sitesService = sitesService;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    public SiteBean getNewSite() {
        return new SiteBean();
    }

    public List<JahiaSite> getSites() {
        return sites;
    }

    public void setSites(List<String> sites) {
        this.sites = new ArrayList<JahiaSite>();
        if (sites != null) {
            for (String site : sites) {
                try {
                    this.sites.add(sitesService.getSiteByKey(site));
                } catch (JahiaException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    public void createSite(SiteBean bean) {

        try {
            JahiaSite site = sitesService.addSite(JCRSessionFactory.getInstance().getCurrentUser(), bean.getTitle(), bean.getServerName(), bean.getSiteKey(), bean.getDescription(), LanguageCodeConverters.getLocaleFromCode(bean.getLanguage()),
                    bean.getTemplateSet(), bean.getModules().toArray(new String[bean.getModules().size()]), null, null, null, null, null, null);

            // set as default site
            if (bean.isDefaultSite()) {
                sitesService.setDefaultSite(site);
            }

            if (bean.createAdmin) {
                AdminProperties admin = bean.getAdminProperties();
                JahiaUser adminSiteUser = userManagerService.createUser(admin.getUserName(), admin.getPassword(), admin.getUserProperties());
                groupManagerService.getAdministratorGroup(site.getSiteKey()).addMember(adminSiteUser);
            }
        } catch (JahiaException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void deleteSites() {
        if (sites != null) {
            JahiaSite defSite = sitesService.getDefaultSite();

            for (JahiaSite site : sites) {
                try {
                    sitesService.removeSite(site);
                } catch (JahiaException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            if (sites.contains(defSite.getSiteKey())) {
                try {
                    Iterator<JahiaSite> siteIterator = sitesService.getSites();
                    if (siteIterator.hasNext()) {
                        sitesService.setDefaultSite(siteIterator.next());
                    } else {
                        sitesService.setDefaultSite(null);
                    }
                } catch (JahiaException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

    }


    public class SiteBean implements Serializable {
        private String siteKey = "mySite" ;
        private String title = "localhost";
        private String serverName = "My Site";
        private String description;

        private boolean defaultSite = false;

        private boolean createAdmin = false;
        private AdminProperties adminProperties;

        private String templateSet;
        private List<String> modules = new ArrayList<String>();
        private String language;

        public String getSiteKey() {
            return siteKey;
        }

        public void setSiteKey(String siteKey) {
            this.siteKey = siteKey;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isDefaultSite() {
            return defaultSite;
        }

        public void setDefaultSite(boolean defaultSite) {
            this.defaultSite = defaultSite;
        }

        public boolean isCreateAdmin() {
            return createAdmin;
        }

        public void setCreateAdmin(boolean createAdmin) {
            this.createAdmin = createAdmin;
        }

        public AdminProperties getAdminProperties() {
            if (adminProperties == null) {
                adminProperties = new AdminProperties(siteKey + "-admin");
            }
            return adminProperties;
        }

        public void setAdminProperties(AdminProperties adminProperties) {
            this.adminProperties = adminProperties;
        }

        public String getTemplateSet() {
            return templateSet;
        }

        public void setTemplateSet(String templateSet) {
            this.templateSet = templateSet;
        }

        public List<String> getModules() {
            return modules;
        }

        public void setModules(List<String> modules) {
            if (modules == null) {
                this.modules = new ArrayList<String>();
            } else {
                this.modules = modules;
            }
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public void validateCreateSite (ValidationContext context) {
            // check validity...
            String title = (String) context.getUserValue("title");
            String serverName = (String) context.getUserValue("serverName");
            String siteKey = (String) context.getUserValue("siteKey");

            MessageContext messages = context.getMessageContext();

            try {
                if (title != null && (title.length() > 0) && serverName != null &&
                        (serverName.length() > 0) && siteKey != null && (siteKey.length() > 0)) {
                    if (!sitesService.isSiteKeyValid(siteKey)) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey")
                                .defaultText(Messages.getInternal("org.jahia.admin.warningMsg.onlyLettersDigitsUnderscore.label", LocaleContextHolder.getLocale()))
                                .build());
                    } else if (siteKey.equals("site")) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey")
                                .defaultText(Messages.getInternal("org.jahia.admin.warningMsg.chooseAnotherSiteKey.label", LocaleContextHolder.getLocale()))
                                .build());
                    } else if (!sitesService.isServerNameValid(serverName)) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey")
                                .defaultText(Messages.getInternal("org.jahia.admin.warningMsg.invalidServerName.label", LocaleContextHolder.getLocale()))
                                .build());
                    } else if (serverName.equals("default")) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey")
                                .defaultText(Messages.getInternal("org.jahia.admin.warningMsg.chooseAnotherServerName.label", LocaleContextHolder.getLocale()))
                                .build());
                    } else if (!Url.isLocalhost(serverName) && sitesService.getSite(serverName) != null) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey")
                                .defaultText(Messages.getInternal("org.jahia.admin.warningMsg.chooseAnotherServerName.label", LocaleContextHolder.getLocale()))
                                .build());
                    } else if (sitesService.getSiteByKey(siteKey) != null) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey")
                                .defaultText(Messages.getInternal("org.jahia.admin.warningMsg.chooseAnotherSiteKey.label", LocaleContextHolder.getLocale()))
                                .build());
                    }
                } else {
                    messages.addMessage(new MessageBuilder().error().source("siteKey")
                            .defaultText(Messages.getInternal("org.jahia.admin.warningMsg.completeRequestInfo.label", LocaleContextHolder.getLocale()))
                            .build());
                }
            } catch (JahiaException e) {
                e.printStackTrace();
            }
        }

        public void validateCreateSiteSelectModules (ValidationContext context) {
        }
    }
}
