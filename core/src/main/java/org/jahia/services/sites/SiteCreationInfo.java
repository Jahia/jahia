/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
import org.jahia.api.io.IOResource;
import org.jahia.services.io.adapter.SpringResourceAdapter;
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

        public Builder fileImportResource(IOResource fileImport) {
            site.setFileImportResource(fileImport);
            return this;
        }

        /**
         * @deprecated use {@link #fileImportResource(IOResource)} instead, this method is not used anymore and should be removed in future versions
         */
        @Deprecated(since = "8.2.4.0", forRemoval = true)
        public Builder fileImport(Resource fileImport) {
            return fileImportResource(SpringResourceAdapter.fromSpring(fileImport));
        }

        public Builder fileImportName(String fileImportName) {
            site.setFileImportName(fileImportName);
            return this;
        }

        public Builder firstImport(String firstImport) {
            site.setFirstImport(firstImport);
            return this;
        }

        public Builder legacyDefinitionsFilePathResource(IOResource legacyDefinitionsFilePath) {
            site.setLegacyDefinitionsFilePathResource(legacyDefinitionsFilePath);
            return this;
        }

        /**
         * @deprecated use {@link #legacyDefinitionsFilePathResource(IOResource)} instead, this method is not used anymore and should be removed in future versions
         */
        @Deprecated(since = "8.2.4.0", forRemoval = true)
        public Builder legacyDefinitionsFilePath(Resource legacyDefinitionsFilePath) {
           return legacyDefinitionsFilePathResource(SpringResourceAdapter.fromSpring(legacyDefinitionsFilePath));
        }

        public Builder legacyMappingFilePathResource(IOResource legacyMappingFilePath) {
            site.setLegacyMappingFilePathResource(legacyMappingFilePath);
            return this;
        }

        /**
         * @deprecated use {@link #legacyMappingFilePathResource(IOResource)} instead, this method is not used anymore and should be removed in future versions
         */
        @Deprecated(since = "8.2.4.0", forRemoval = true)
        public Builder legacyMappingFilePath(Resource legacyMappingFilePath) {
           return legacyMappingFilePathResource(SpringResourceAdapter.fromSpring(legacyMappingFilePath));
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

        public Builder serverNameAliases(String serverNameAliases) {
            site.setServerNameAliasesAsString(serverNameAliases);
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

    private IOResource fileImport;

    private String fileImportName;

    private String firstImport;

    private IOResource legacyDefinitionsFilePath;

    private IOResource legacyMappingFilePath;

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
     * @param firstImport
     * @param fileImport
     * @param fileImportName
     * @param originatingJahiaRelease
     * @param legacyMappingFilePath
     * @param legacyDefinitionsFilePath
     */
    public SiteCreationInfo(String siteKey, String serverName, String serverNameAliasesAsString, String title,
                            String description, String templateSet, String[] modulesToDeploy, String locale, JahiaUser siteAdmin,
                            String firstImport, IOResource fileImport, String fileImportName, String originatingJahiaRelease,
                            IOResource legacyMappingFilePath, IOResource legacyDefinitionsFilePath) {
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

    /**
     * @deprecated use {@link #SiteCreationInfo(String, String, String, String, String, String, String[], String, JahiaUser, String, IOResource, String, String, IOResource, IOResource)} instead, this constructor is not used anymore and should be removed in future versions
     */
    @Deprecated(since = "8.2.4.0", forRemoval = true)
    public SiteCreationInfo(String siteKey, String serverName, String serverNameAliasesAsString, String title,
                            String description, String templateSet, String[] modulesToDeploy, String locale, JahiaUser siteAdmin,
                            String firstImport,Resource fileImport, String fileImportName, String originatingJahiaRelease,
                            Resource legacyMappingFilePath, Resource legacyDefinitionsFilePath) {
        this(siteKey, serverName, serverNameAliasesAsString, title,
                description, templateSet, modulesToDeploy, locale, siteAdmin,
                firstImport, SpringResourceAdapter.fromSpring(fileImport), fileImportName, originatingJahiaRelease,
                SpringResourceAdapter.fromSpring(legacyDefinitionsFilePath), SpringResourceAdapter.fromSpring(legacyMappingFilePath));
    }

    public String getDescription() {
        return description;
    }

    public IOResource getFileImportResource() {
        return fileImport;
    }

    /**
     * @deprecated use {@link #getFileImportResource()} instead, this method is not used anymore and should be removed in future versions
     */
    @Deprecated(since = "8.2.4.0", forRemoval = true)
    public Resource getFileImport() {
        return SpringResourceAdapter.toSpring(fileImport);
    }

    public String getFileImportName() {
        return fileImportName;
    }

    public String getFirstImport() {
        return firstImport;
    }

    public IOResource getLegacyDefinitionsFilePathResource() {
        return legacyDefinitionsFilePath;
    }

    /**
     * @deprecated use {@link #getLegacyDefinitionsFilePathResource()} instead, this method is not used anymore and should be removed in future versions
     */
    @Deprecated(since = "8.2.4.0", forRemoval = true)
    public Resource getLegacyDefinitionsFilePath() {
        return SpringResourceAdapter.toSpring(legacyDefinitionsFilePath);
    }

    public IOResource getLegacyMappingFilePathResource() {
        return legacyMappingFilePath;
    }

    /**
     * @deprecated use {@link #getLegacyMappingFilePathResource()} instead, this method is not used anymore and should be removed in future versions
     */
    @Deprecated(since = "8.2.4.0", forRemoval = true)
    public Resource getLegacyMappingFilePath() {
        return SpringResourceAdapter.toSpring(legacyMappingFilePath);
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

    public void setFileImportResource(IOResource fileImport) {
        this.fileImport = fileImport;
    }

    /**
     * @deprecated use {@link #setFileImportResource(IOResource)} instead, this method is not used anymore and should be removed in future versions
     */
    @Deprecated(since = "8.2.4.0", forRemoval = true)
    public void setFileImport(Resource fileImport) {
        this.fileImport = SpringResourceAdapter.fromSpring(fileImport);
    }

    public void setFileImportName(String fileImportName) {
        this.fileImportName = fileImportName;
    }

    public void setFirstImport(String firstImport) {
        this.firstImport = firstImport;
    }

    public void setLegacyDefinitionsFilePathResource(IOResource legacyDefinitionsFilePath) {
        this.legacyDefinitionsFilePath = legacyDefinitionsFilePath;
    }

    /**
     * @deprecated use {@link #setLegacyDefinitionsFilePathResource(IOResource)} instead, this method is not used anymore and should be removed in future versions
     */
    @Deprecated(since = "8.2.4.0", forRemoval = true)
    public void setLegacyDefinitionsFilePath(Resource legacyDefinitionsFilePath) {
        this.legacyDefinitionsFilePath = SpringResourceAdapter.fromSpring(legacyDefinitionsFilePath);
    }

    public void setLegacyMappingFilePathResource(IOResource legacyMappingFilePath) {
        this.legacyMappingFilePath = legacyMappingFilePath;
    }

    /**
     * @deprecated use {@link #setLegacyMappingFilePathResource(IOResource)} instead, this method is not used anymore and should be removed in future versions
     */
    @Deprecated(since = "8.2.4.0", forRemoval = true)
    public void setLegacyMappingFilePath(Resource legacyMappingFilePath) {
        this.legacyMappingFilePath = SpringResourceAdapter.fromSpring(legacyMappingFilePath);
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
