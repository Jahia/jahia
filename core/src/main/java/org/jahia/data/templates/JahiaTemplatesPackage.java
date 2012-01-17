/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

//
//
//  JahiaTemplatesPackage
//
//  NK      16.01.2001
//
//

package org.jahia.data.templates;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Version;

import java.io.File;
import java.util.*;

/**
 * Holds Informations about a templates package
 *
 * @author Khue ng
 * @version 1.0
 */
public class JahiaTemplatesPackage {

    /**
     * the file or directory name from which data are loaded
     */
    private String m_FileName;
    /**
     * the full path to the source file or directory
     */
    private String m_FilePath;

    /**
     * Name of the package
     */
    private String m_Name;
    /**
     * Name of the dependent package
     */
    private List<String> depends = new LinkedList<String>();
    /**
     * The Folder Name where to extract package contents
     */
    private String m_RootFolder;
    /**
     * The initial import file
     */
    private List<String> initialImports = new LinkedList<String>();
    /**
     * The Package Provider Name
     */
    private String m_Provider;
    /**
     * The Package thumbnail image file Name entry
     */
    private String m_Thumbnail;

    private String description;

    private Set<JahiaTemplatesPackage> dependencies = new LinkedHashSet<JahiaTemplatesPackage>();
    
    private List<JahiaTemplateDef> templateListReadOnly = Collections.emptyList();

    private Map<String, JahiaTemplateDef> templatesReadOnly = Collections.emptyMap();

    private Map<String, JahiaTemplateDef> templates = new TreeMap<String, JahiaTemplateDef>();

    private boolean changesMade = false;

    // set the module-type property from the manifest

    private String moduleType;

    private String rootFolderPath;

    private String resourceBundleName;

    private List<String> definitionsFile = new LinkedList<String>();

    private List<String> rulesFiles = new LinkedList<String>();

    /**
     * Contains names of the resource bundles for template sets starting from this one, then the direct parent and so on.
     */
    private List<String> resourceBundleHierarchy = new LinkedList<String>();
    private List<String> rulesDescriptorFiles = new LinkedList<String>();

    private long buildNumber;

    private Version version;

    /**
     * Return the template name
     *
     * @return (String) the name of the template
     */
    public String getName() {

        return m_Name;
    }


    /**
     * Set the name
     *
     * @param name the name of the template
     */
    public void setName(String name) {

        m_Name = name;
    }


    /**
     * Return the Root Folder
     *
     * @return (String) the Root Folder of the templates
     */
    public String getRootFolder() {
        return m_RootFolder;
    }


    /**
     * Set the Root Folder
     *
     * @param folder the Root Folder of the templates
     */
    public void setRootFolder(String folder) {
        if (StringUtils.isNotEmpty(folder)) {
            m_RootFolder = folder;
            SettingsBean conf = SettingsBean.getInstance(); 
            rootFolderPath = conf.getTemplatesContext() + (conf.getTemplatesContext().endsWith("/") ? "" : "/") + folder;
        } else {
            m_RootFolder = "";
            rootFolderPath = SettingsBean.getInstance().getTemplatesContext();
        }

        // need to recalculate paths
        for (JahiaTemplateDef tempDef : templates.values()) {
            if (tempDef.getParent() == this) {
                tempDef.setFilePath(new StringBuilder(64).append(
                        getRootFolderPath()).append('/').append(
                        tempDef.getFileName()).toString());
            }
        }
        changesMade = true;
    }

    /**
     * Return the provider name
     *
     * @return (String) the name of the Provider
     */
    public String getProvider() {

        return m_Provider;
    }


    /**
     * Set the Provider
     *
     * @param provider the name of the Provider
     */
    public void setProvider(String provider) {

        m_Provider = provider;
    }


    /**
     * Return the thumbnail file name
     *
     * @return (String) the thumbnail file name
     */
    public String getThumbnail() {

        return m_Thumbnail;
    }


    /**
     * Set the thumbnail file name
     *
     * @param val the file name
     */
    public void setThumbnail(String val) {

        m_Thumbnail = val;
    }

    /**
     * Returns unmodifiable list of available templates.
     *
     * @return unmodifiable list of available templates
     */
    public List<JahiaTemplateDef> getTemplates() {
        checkForChanges();
        return templateListReadOnly;
    }


    /**
     * Add a Template Definition in the Templates list
     *
     * @param tempDef
     */
    public void addTemplateDef(JahiaTemplateDef tempDef) {
        addTemplateDef(tempDef, false);
    }

    /**
     * Add a Template Definition in the Templates list
     *
     * @param tempDef                    tempDef
     * @param inheritedFromParentPackage if the template is inherited from parent package
     */
    public void addTemplateDef(JahiaTemplateDef tempDef, boolean inheritedFromParentPackage) {
        if (!inheritedFromParentPackage) {
            tempDef.setParent(this);
            tempDef.setFilePath(new StringBuffer(64)
                    .append(getRootFolderPath()).append('/').append(
                            tempDef.getFileName()).toString());
        }
        templates.put(tempDef.getName(), tempDef);
        changesMade = true;
    }

    /**
     * Add a list of Template Definitions into the Templates list
     *
     * @param templateList               a list of template definitions
     * @param inheritedFromParentPackage if templates are inherited from parent package
     */
    public void addTemplateDefAll(List<JahiaTemplateDef> templateList,
                                  boolean inheritedFromParentPackage) {
        for (JahiaTemplateDef templateDef : templateList) {
            addTemplateDef(templateDef, inheritedFromParentPackage);
        }
    }

    /**
     * Clears the list with the contained templates.
     */
    public void removeTemplates() {
        templates.clear();
        changesMade = true;
    }


    /**
     * get the source filename
     */
    public String getFileName() {
        return this.m_FileName;
    }


    /**
     * set the source filename
     */
    public void setFileName(String name) {
        this.m_FileName = name;
    }


    /**
     * get the file path
     */
    public String getFilePath() {
        return this.m_FilePath;
    }


    /**
     * set the file path
     */
    public void setFilePath(String path) {
        this.m_FilePath = path;
        File f = new File(path);
        this.setFileName(f.getName());
    }


	/**
	 * Returns <code>true</code> if this package is the default template set.
	 * 
	 * @return <code>true</code> if this package is the default template set
	 */
	public boolean isDefault() {
		return getRootFolder() != null && "default".equals(getRootFolder());
	}

    public List<String> getInitialImports() {
        return initialImports;
    }

    public void addInitialImport(String initImport) {
        initialImports.add(initImport);
    }

    /**
     * Returns the name of the parent template package.
     *
     * @return the name of the parent template package
     */
    public List<String> getDepends() {
        return depends;
    }

    /**
     * Sets the name of the parent template package.
     *
     * @param dep name of the parent template package
     */
    public void setDepends(String dep) {
        depends.add(dep);
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    /**
     * Returns unmodified map with the all templates available in the package,
     * keyed by the template name.
     *
     * @return unmodified map with the all templates available in the package,
     *         keyed by the template name
     */
    public Map<String, JahiaTemplateDef> getTemplateMap() {
        checkForChanges();
        return templatesReadOnly;
    }

    private void checkForChanges() {
        if (changesMade) {
            List<JahiaTemplateDef> tmpList = new LinkedList<JahiaTemplateDef>();
            tmpList.addAll(templates.values());
            templateListReadOnly = Collections.unmodifiableList(tmpList);
            Map<String, JahiaTemplateDef> tmpMap = new TreeMap<String, JahiaTemplateDef>();
            for (JahiaTemplateDef def : templateListReadOnly) {
                tmpMap.put(def.getName(), def);
            }
            templatesReadOnly = Collections.unmodifiableMap(tmpMap);

            changesMade = false;
        }
    }

    /**
     * Returns the source path of the root folder for the deployed template set.
     *
     * @return the source path of the root folder for the deployed template set
     */
    public String getRootFolderPath() {
        return rootFolderPath;
    }


    /**
     * Returns the requested template definition by its name.
     *
     * @param name the template name
     * @return the requested template definition by its name
     */
    public JahiaTemplateDef lookupTemplate(String name) {
        return getTemplateMap().get(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResourceBundleName() {
        return resourceBundleName;
    }

    public void setResourceBundleName(String resourceBundleName) {
        this.resourceBundleName = resourceBundleName;
    }

    public List<String> getDefinitionsFiles() {
        return definitionsFile;
    }

    public void setDefinitionsFile(String definitionFile) {
        definitionsFile.add(definitionFile);
    }

    public List<String> getRulesFiles() {
        return rulesFiles;
    }

    public void setRulesFile(String rulesFile) {
        rulesFiles.add(rulesFile);
    }

    public List<String> getResourceBundleHierarchy() {
        return resourceBundleHierarchy;
    }

   public void clearHierarchy() {
        getResourceBundleHierarchy().clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JahiaTemplatesPackage that = (JahiaTemplatesPackage) o;

        if (m_Name != null ? !m_Name.equals(that.m_Name) : that.m_Name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return m_Name != null ? m_Name.hashCode() : 0;
    }

    public List<String> getRulesDescriptorFiles() {
        return rulesDescriptorFiles;
    }

    public void setRulesDescriptorFile(String rulesDescriptorFiles) {
        this.rulesDescriptorFiles.add(rulesDescriptorFiles);
    }


    /**
     * @return the dependencies
     */
    public Set<JahiaTemplatesPackage> getDependencies() {
        return dependencies;
    }

    public long getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(long buildNumber) {
        this.buildNumber = buildNumber;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }
}