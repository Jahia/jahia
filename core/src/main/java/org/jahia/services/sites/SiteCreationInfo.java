/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
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
package org.jahia.services.sites;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jahia.services.usermanager.JahiaUser;
import org.springframework.core.io.Resource;

/**
 * Data object to provide site creation data for the {@link JahiaSitesService}.
 * 
 * @author Sergiy Shyrkov
 */
public class SiteCreationInfo {

    /**
     * Helper class used to build the {@link SiteCreationInfo} object.
     */
    public static class Builder {

        private SiteCreationInfo site;

        private Builder() {
            site = new SiteCreationInfo();
        }

        public SiteCreationInfo build() {
            return site;
        }

        public Builder description(String description) {
            site.setDescription(description);
            return this;
        }

        public Builder fileImport(Resource fileImport) {
            site.setFileImport(fileImport);
            return this;
        }

        public Builder fileImportName(String fileImportName) {
            site.setFileImportName(fileImportName);
            return this;
        }

        public Builder firstImport(String firstImport) {
            site.setFirstImport(firstImport);
            return this;
        }

        public Builder legacyDefinitionsFilePath(Resource legacyDefinitionsFilePath) {
            site.setLegacyDefinitionsFilePath(legacyDefinitionsFilePath);
            return this;
        }

        public Builder legacyMappingFilePath(Resource legacyMappingFilePath) {
            site.setLegacyMappingFilePath(legacyMappingFilePath);
            return this;
        }

        public Builder locale(String locale) {
            site.setLocale(locale);
            return this;
        }

        public Builder modulesToDeploy(String[] modulesToDeploy) {
            site.setModulesToDeploy(modulesToDeploy);
            return this;
        }

        public Builder originatingJahiaRelease(String originatingJahiaRelease) {
            site.setOriginatingJahiaRelease(originatingJahiaRelease);
            return this;
        }

        public Builder serverName(String serverName) {
            site.setServerName(serverName);
            return this;
        }

        public Builder serverNameAliases(String... serverNameAliases) {
            site.setServerNameAliases(serverNameAliases);
            return this;
        }

        public Builder siteAdmin(JahiaUser siteAdmin) {
            site.setSiteAdmin(siteAdmin);
            return this;
        }

        public Builder siteKey(String siteKey) {
            site.setSiteKey(siteKey);
            return this;
        }

        public Builder templateSet(String templateSet) {
            site.setTemplateSet(templateSet);
            return this;
        }

        public Builder title(String title) {
            site.setTitle(title);
            return this;
        }
    }

    /**
     * Returns a helper object, used to build an instance of the {@link SiteCreationInfo}.
     * 
     * @return a helper object, used to build an instance of the {@link SiteCreationInfo}
     */
    public static Builder builder() {
        return new Builder();
    }

    private String description;

    private Resource fileImport;

    private String fileImportName;

    private String firstImport;

    private Resource legacyDefinitionsFilePath;

    private Resource legacyMappingFilePath;

    private String locale;

    private String[] modulesToDeploy;

    private String originatingJahiaRelease;

    private String serverName;

    private String[] serverNameAliases;

    private JahiaUser siteAdmin;

    private String siteKey;

    private String templateSet;

    private String title;

    /**
     * Initializes an instance of this class.
     */
    public SiteCreationInfo() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param siteKey
     * @param serverName
     * @param serverNameAliasesAsString
     * @param title
     * @param description
     * @param templateSet
     * @param modulesToDeploy
     * @param locale
     * @param siteAdmin
     */
    public SiteCreationInfo(String siteKey, String serverName, String serverNameAliasesAsString, String title,
            String description, String templateSet, String[] modulesToDeploy, String locale, JahiaUser siteAdmin) {
        this(siteKey, serverName, serverNameAliasesAsString, title, description, templateSet, modulesToDeploy, locale,
                siteAdmin, null, null, null, null);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param siteKey
     * @param serverName
     * @param serverNameAliasesAsString
     * @param title
     * @param description
     * @param templateSet
     * @param modulesToDeploy
     * @param locale
     * @param siteAdmin
     * @param firstImport
     * @param fileImport
     * @param fileImportName
     * @param originatingJahiaRelease
     */
    public SiteCreationInfo(String siteKey, String serverName, String serverNameAliasesAsString, String title,
            String description, String templateSet, String[] modulesToDeploy, String locale, JahiaUser siteAdmin,
            String firstImport, Resource fileImport, String fileImportName, String originatingJahiaRelease) {
        this(siteKey, serverName, serverNameAliasesAsString, title, description, templateSet, modulesToDeploy, locale,
                siteAdmin, firstImport, fileImport, fileImportName, originatingJahiaRelease, null, null);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param siteKey
     * @param serverName
     * @param serverNameAliasesAsString
     * @param title
     * @param description
     * @param templateSet
     * @param modulesToDeploy
     * @param locale
     * @param siteAdmin
     * @param firstImport
     * @param fileImport
     * @param fileImportName
     * @param originatingJahiaRelease
     * @param legacyMappingFilePath
     * @param legacyDefinitionsFilePath
     */
    public SiteCreationInfo(String siteKey, String serverName, String serverNameAliasesAsString, String title,
            String description, String templateSet, String[] modulesToDeploy, String locale, JahiaUser siteAdmin,
            String firstImport, Resource fileImport, String fileImportName, String originatingJahiaRelease,
            Resource legacyMappingFilePath, Resource legacyDefinitionsFilePath) {
        this();
        this.siteKey = siteKey;
        this.serverName = serverName;
        setServerNameAliasesAsString(serverNameAliasesAsString);
        this.title = title;
        this.description = description;
        this.templateSet = templateSet;
        this.modulesToDeploy = modulesToDeploy;
        this.locale = locale;
        this.siteAdmin = siteAdmin;
        this.fileImport = fileImport;
        this.fileImportName = fileImportName;
        this.firstImport = firstImport;
        this.originatingJahiaRelease = originatingJahiaRelease;
        this.legacyDefinitionsFilePath = legacyDefinitionsFilePath;
        this.legacyMappingFilePath = legacyMappingFilePath;
    }

    public String getDescription() {
        return description;
    }

    public Resource getFileImport() {
        return fileImport;
    }

    public String getFileImportName() {
        return fileImportName;
    }

    public String getFirstImport() {
        return firstImport;
    }

    public Resource getLegacyDefinitionsFilePath() {
        return legacyDefinitionsFilePath;
    }

    public Resource getLegacyMappingFilePath() {
        return legacyMappingFilePath;
    }

    public String getLocale() {
        return locale;
    }

    public String[] getModulesToDeploy() {
        return modulesToDeploy;
    }

    public String getOriginatingJahiaRelease() {
        return originatingJahiaRelease;
    }

    public String getServerName() {
        return serverName;
    }

    public String[] getServerNameAliases() {
        return serverNameAliases;
    }

    public JahiaUser getSiteAdmin() {
        return siteAdmin;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public String getTemplateSet() {
        return templateSet;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFileImport(Resource fileImport) {
        this.fileImport = fileImport;
    }

    public void setFileImportName(String fileImportName) {
        this.fileImportName = fileImportName;
    }

    public void setFirstImport(String firstImport) {
        this.firstImport = firstImport;
    }

    public void setLegacyDefinitionsFilePath(Resource legacyDefinitionsFilePath) {
        this.legacyDefinitionsFilePath = legacyDefinitionsFilePath;
    }

    public void setLegacyMappingFilePath(Resource legacyMappingFilePath) {
        this.legacyMappingFilePath = legacyMappingFilePath;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setModulesToDeploy(String[] modulesToDeploy) {
        this.modulesToDeploy = modulesToDeploy;
    }

    public void setOriginatingJahiaRelease(String originatingJahiaRelease) {
        this.originatingJahiaRelease = originatingJahiaRelease;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setServerNameAliases(String[] serverNameAliases) {
        this.serverNameAliases = serverNameAliases;
    }

    public void setServerNameAliasesAsString(String serverNameAliases) {
        this.serverNameAliases = StringUtils.isNotEmpty(serverNameAliases) ? StringUtils.split(serverNameAliases, ", ")
                : null;
    }

    public void setSiteAdmin(JahiaUser siteAdmin) {
        this.siteAdmin = siteAdmin;
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
