package org.jahia.modules.serversettings.flow;

import org.jahia.exceptions.JahiaException;
import org.jahia.modules.serversettings.adminproperties.AdminProperties;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSiteTools;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.LanguageCodeConverters;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class WebprojectHandler implements Serializable {

    @Autowired
    private transient JahiaSitesService sitesService;

    @Autowired
    private transient JahiaUserManagerService userManagerService;

    @Autowired
    private transient JahiaGroupManagerService groupManagerService;

    public void setSitesService(JahiaSitesService sitesService) {
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


    public class SiteBean implements Serializable {
        private String siteKey;
        private String title;
        private String serverName;
        private String description;

        private boolean defaultSite;

        private boolean createAdmin;
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
    }
}
