package org.jahia.modules.serversettings.flow;

import java.io.Serializable;
import java.util.*;

public class WebprojectHandler implements Serializable {

    public SiteBean getNewSite() {
        return new SiteBean();
    }


    public class SiteBean implements Serializable {
        private String siteKey;
        private String title;
        private String serverName;
        private String description;

        private boolean defaultSite;

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
