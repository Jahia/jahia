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

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.importexport.SiteImportDefaults;
import org.jahia.services.importexport.validation.ValidationResults;

public class ImportInfo implements Serializable {

    private static final long serialVersionUID = 1156948970758806329L;
    
    private String defaultLanguage;
    private Boolean defaultSite;
    private String description;
    private File fileToBeImported;
    private String importFileName;
    private Collection<File> legacyDefinitions;
    private boolean legacyImport;
    private Collection<File> legacyMappings;
    private Boolean mixLanguage;
    private String oldSiteKey;
    private String originatingJahiaRelease;
    private boolean selected;
    private String selectedLegacyDefinitions;
    private String selectedLegacyMapping;
    private boolean site;
    private String siteKey;
    private Properties siteProperties;
    private String siteServername;
    private String siteTitle;
    private boolean siteTitleInvalid;
    private String templatePackageName;
    private String templates;
    private String type;
    private ValidationResults validationResult;

    public Map<Object, Object> asMap() {
        Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        if (siteProperties != null) {
            map.putAll(siteProperties);
            map.put("sitekey", siteKey);
            map.put("sitetitle", siteTitle);
            map.put("siteservername", siteServername);
            map.put("templates", templates);
        }
        return map;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public Boolean getDefaultSite() {
        return defaultSite;
    }

    public String getDescription() {
        return description;
    }

    public File getImportFile() {
        return fileToBeImported;
    }

    public String getImportFileName() {
        return importFileName;
    }

    public Collection<File> getLegacyDefinitions() {
        return legacyDefinitions;
    }

    public Collection<File> getLegacyMappings() {
        return legacyMappings;
    }

    public Boolean getMixLanguage() {
        return mixLanguage;
    }

    public String getOldSiteKey() {
        return oldSiteKey;
    }

    public String getOriginatingJahiaRelease() {
        return originatingJahiaRelease;
    }

    public String getSelectedLegacyDefinitions() {
        return selectedLegacyDefinitions;
    }

    public String getSelectedLegacyMapping() {
        return selectedLegacyMapping;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public Properties getSiteProperties() {
        return siteProperties;
    }

    public String getSiteServername() {
        return siteServername;
    }

    public String getSiteTitle() {
        return siteTitle;
    }

    public String getTemplatePackageName() {
        return templatePackageName;
    }

    public String getTemplates() {
        return templates;
    }

    public String getType() {
        return type;
    }

    public ValidationResults getValidationResult() {
        return validationResult;
    }

    public boolean isLegacyImport() {
        return legacyImport;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isSite() {
        return site;
    }

    public boolean isSiteTitleInvalid() {
        return siteTitleInvalid;
    }

    public void loadSiteProperties(Properties siteProperties) {
        this.siteProperties = siteProperties;
        siteKey = siteProperties.getProperty("sitekey");
        siteTitle = siteProperties.getProperty("sitetitle");
        siteServername = siteProperties.getProperty("siteservername");
        description = siteProperties.getProperty("description");
        templatePackageName = siteProperties.getProperty("templatePackageName");
        mixLanguage = Boolean.valueOf(siteProperties.getProperty("mixLanguage", "false"));
        defaultLanguage = siteProperties.getProperty("defaultLanguage");
        defaultSite = Boolean.valueOf(siteProperties.getProperty("defaultSite", "false"));
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public void setDefaultSite(Boolean defaultSite) {
        this.defaultSite = defaultSite;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImportFile(File fileToBeImported) {
        this.fileToBeImported = fileToBeImported;
    }

    public void setImportFileName(String importFileName) {
        this.importFileName = importFileName;
    }

    public void setLegacyDefinitions(Collection<File> legacyDefinitions) {
        this.legacyDefinitions = legacyDefinitions;
    }

    public void setLegacyImport(boolean legacyImport) {
        this.legacyImport = legacyImport;
        if (legacyImport) {
            Map<String, SiteImportDefaults> siteImportDefaultsMap = SpringContextSingleton.getInstance().getContext()
                    .getBeansOfType(SiteImportDefaults.class);
            if (siteImportDefaultsMap != null && siteImportDefaultsMap.size() > 0) {
                if (siteImportDefaultsMap.size() > 1) {
                    WebprojectHandler.logger
                            .error("Found several beans of type org.jahia.services.importexport.SiteImportDefaults whereas only one is allowed, skipping");
                } else {
                    SiteImportDefaults siteImportDefaults = siteImportDefaultsMap.values().iterator().next();
                    templates = siteImportDefaults.getDefaultTemplateSet(siteKey);
                    selectedLegacyDefinitions = siteImportDefaults.getDefaultSourceDefinitionsFile(siteKey);
                    selectedLegacyMapping = siteImportDefaults.getDefaultMappingFile(siteKey);
                }
            }
        }
    }

    public void setLegacyMappings(Collection<File> legacyMappings) {
        this.legacyMappings = legacyMappings;
    }

    public void setMixLanguage(Boolean mixLanguage) {
        this.mixLanguage = mixLanguage;
    }

    public void setOldSiteKey(String oldSiteKey) {
        this.oldSiteKey = oldSiteKey;
    }

    public void setOriginatingJahiaRelease(String originatingJahiaRelease) {
        this.originatingJahiaRelease = originatingJahiaRelease;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setSelectedLegacyDefinitions(String selectedLegacyDefinitions) {
        this.selectedLegacyDefinitions = selectedLegacyDefinitions;
    }

    public void setSelectedLegacyMapping(String selectedLegacyMapping) {
        this.selectedLegacyMapping = selectedLegacyMapping;
    }

    public void setSite(boolean site) {
        this.site = site;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public void setSiteProperties(Properties siteProperties) {
        this.siteProperties = siteProperties;
    }

    public void setSiteServername(String siteServername) {
        this.siteServername = siteServername;
    }

    public void setSiteTitle(String siteTitle) {
        this.siteTitle = siteTitle;
    }

    public void setSiteTitleInvalid(boolean invalid) {
        siteTitleInvalid = invalid;
    }

    public void setTemplatePackageName(String templatePackageName) {
        this.templatePackageName = templatePackageName;
    }

    public void setTemplates(String templates) {
        this.templates = templates;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValidationResult(ValidationResults validationResult) {
        this.validationResult = validationResult;
    }
}