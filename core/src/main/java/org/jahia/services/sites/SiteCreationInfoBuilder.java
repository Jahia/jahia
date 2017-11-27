/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 * <p>
 * http://www.jahia.com
 * <p>
 * Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 * <p>
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 * <p>
 * 1/ GPL
 * ==================================================================================
 * <p>
 * IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ===================================================================================
 * <p>
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 * <p>
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 * <p>
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.sites;

import org.jahia.services.usermanager.JahiaUser;
import org.springframework.core.io.Resource;

/**
 * SiteCreationInfo Builder
 *
 * @author David Griffon
 */
public class SiteCreationInfoBuilder {
    private String siteKey;
    private String serverName;
    private String serverNameAliasesAsString;
    private String title;
    private String description;
    private String templateSet;
    private String[] modulesToDeploy;
    private String locale;
    private JahiaUser siteAdmin;
    private String firstImport = null;
    private Resource fileImport = null;
    private String fileImportName = null;
    private String originatingJahiaRelease = null;
    private Resource legacyMappingFilePath = null;
    private Resource legacyDefinitionsFilePath = null;

    public SiteCreationInfoBuilder setSiteKey(String siteKey) {
        this.siteKey = siteKey;
        return this;
    }

    public SiteCreationInfoBuilder setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public SiteCreationInfoBuilder setServerNameAliasesAsString(String serverNameAliasesAsString) {
        this.serverNameAliasesAsString = serverNameAliasesAsString;
        return this;
    }

    public SiteCreationInfoBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public SiteCreationInfoBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public SiteCreationInfoBuilder setTemplateSet(String templateSet) {
        this.templateSet = templateSet;
        return this;
    }

    public SiteCreationInfoBuilder setModulesToDeploy(String[] modulesToDeploy) {
        this.modulesToDeploy = modulesToDeploy;
        return this;
    }

    public SiteCreationInfoBuilder setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    public SiteCreationInfoBuilder setSiteAdmin(JahiaUser siteAdmin) {
        this.siteAdmin = siteAdmin;
        return this;
    }

    public SiteCreationInfoBuilder setFirstImport(String firstImport) {
        this.firstImport = firstImport;
        return this;
    }

    public SiteCreationInfoBuilder setFileImport(Resource fileImport) {
        this.fileImport = fileImport;
        return this;
    }

    public SiteCreationInfoBuilder setFileImportName(String fileImportName) {
        this.fileImportName = fileImportName;
        return this;
    }

    public SiteCreationInfoBuilder setOriginatingJahiaRelease(String originatingJahiaRelease) {
        this.originatingJahiaRelease = originatingJahiaRelease;
        return this;
    }

    public SiteCreationInfoBuilder setLegacyMappingFilePath(Resource legacyMappingFilePath) {
        this.legacyMappingFilePath = legacyMappingFilePath;
        return this;
    }

    public SiteCreationInfoBuilder setLegacyDefinitionsFilePath(Resource legacyDefinitionsFilePath) {
        this.legacyDefinitionsFilePath = legacyDefinitionsFilePath;
        return this;
    }

    public SiteCreationInfo createSiteCreationInfo() {
        return new SiteCreationInfo(siteKey, serverName, serverNameAliasesAsString, title, description, templateSet, modulesToDeploy, locale, siteAdmin, firstImport, fileImport, fileImportName, originatingJahiaRelease, legacyMappingFilePath, legacyDefinitionsFilePath);
    }
}