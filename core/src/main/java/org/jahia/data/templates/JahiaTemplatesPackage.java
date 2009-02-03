/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

//
//
//  JahiaTemplatesPackage
//
//  NK      16.01.2001
//
//

package org.jahia.data.templates;

import java.io.File;
import java.util.*;

import org.apache.commons.collections.list.UnmodifiableList;
import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Holds Informations about a templates package
 *
 * @author Khue ng
 * @version 1.0
 */
public class JahiaTemplatesPackage {

    /**
     * the file or directory name from which data are loaded *
     */
    private String m_FileName;
    /**
     * the full path to the source file or directory *
     */
    private String m_FilePath;
    /**
     * the package type *
     */
    private int m_Type;    // 1=jar,2=directory
    /**
     * jar package *
     */
    private static final int JAR = 1;
    /**
     * directory *
     */
    private static final int DIR = 2;
    private static final int WAR = 3;

    /**
     * Name of the package *
     */
    private String m_Name;
    /**
     * Name of the parent package *
     */
    private String parent;
    /**
     * The Folder Name where to extract package contents *
     */
    private String m_RootFolder;
    /**
     * The name of the jar file containing classes used by this package  *
     */
    private String m_ClassesFile;
    /**
     * The root folder where the classes are deployed  *
     */
    private String m_ClassesRoot;
    /**
     * The initial import file *
     */
    private String m_InitialImport;
    /**
     * The Package Provider Name *
     */
    private String m_Provider;
    /**
     * The Package thumbnail image file Name entry *
     */
    private String m_Thumbnail;

    private String description;

    private List<JahiaTemplateDef> templateListReadOnly = Collections.emptyList();

    private Map<String, JahiaTemplateDef> templatesReadOnly = Collections.emptyMap();

    private Map<String, JahiaTemplateDef> templates = new TreeMap();

    private boolean changesMade = false;

    private String rootFolderPath;

    private JahiaTemplateDef homePageTemplate;

    private String homePageName;

    private JahiaTemplateDef defaultPageTemplate;

    private String defaultPageName;

    private String mySettingsPageName;

    private String mySettingsSuccessPageName;

    private String searchResultsPageName;

    private String resourceBundleName;

    private List<String> definitionsFile = new ArrayList<String>();

    private List<String> rulesFiles = new ArrayList<String>();

    /**
     * Contains names of the template sets starting from this one, then the direct parent and so on.
     */
    private List<String> hierarchy = new LinkedList();

    /**
     * Contains names of the resource bundles for template sets starting from this one, then the direct parent and so on.
     */
    private List<String> resourceBundleHierarchy = new LinkedList();

    /**
     * Based on hierarchy path, contains root folder paths for each of the
     * template sets in the hierarchy path.
     */
    private List<String> lookupPath = new LinkedList();

    private Map<String, String> properties = new HashMap<String, String>();

    /**
     * Initializes an instance of this class.
     */
    public JahiaTemplatesPackage() {
        super();
    }

    /**
     * Constructor
     */
    public JahiaTemplatesPackage(
            String name,
            String rootFolder,
            String classesFile,
            String classesRoot,
            String initialImport,
            String providerName,
            String thumbnail
    ) {
        this();
        m_Name = name;
        m_ClassesFile = classesFile;
        m_ClassesRoot = classesRoot;
        m_InitialImport = initialImport;
        m_Provider = providerName;
        m_Thumbnail = thumbnail;
        setRootFolder(rootFolder);
    }

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
            StringBuffer path = new StringBuffer(64).append(org.jahia.settings.SettingsBean.getInstance()
                    .getTemplatesContext());
            if (org.jahia.settings.SettingsBean.getInstance().getTemplatesContext().charAt(
                    org.jahia.settings.SettingsBean.getInstance().getTemplatesContext().length() - 1) != '/') {
                path.append("/");
            }
            path.append(folder);
            rootFolderPath = path.toString();
        } else {
            m_RootFolder = "";
            rootFolderPath = org.jahia.settings.SettingsBean.getInstance().getTemplatesContext();
        }

        // need to recalculate paths
        for (Iterator iterator = templates.values().iterator(); iterator
                .hasNext();) {
            JahiaTemplateDef tempDef = (JahiaTemplateDef) iterator.next();
            if (tempDef.getParent() == this) {
                tempDef.setFilePath(new StringBuffer(64).append(
                        getRootFolderPath()).append('/').append(
                        tempDef.getFileName()).toString());
            }
        }
        changesMade = true;
    }

    /**
     * Return the Classes File name
     *
     * @return (String) the Classes File name
     */
    public String getClassesFile() {

        return m_ClassesFile;
    }


    /**
     * Set the Classes file
     *
     * @param classesFile the Classes file name
     */
    public void setClassesFile(String classesFile) {

        m_ClassesFile = classesFile;
    }

    public String getClassesRoot() {
        return m_ClassesRoot;
    }

    public void setClassesRoot(String classesRoot) {
        this.m_ClassesRoot = classesRoot;
    }

    /**
     * Return true if the classesFile is not null and length>0
     *
     * @return (boolean) true if m_ClassesFile != null && length>0
     */
    public boolean hasClasses() {

        return (m_ClassesFile != null && m_ClassesFile.length() > 0);
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
     * Return the home page template.
     *
     * @return the Home page template or null if no defined home page template
     */
    public JahiaTemplateDef getHomePageTemplate() {
        return homePageTemplate;
    }

    public JahiaTemplateDef getDefaultPageTemplate() {
        return defaultPageTemplate;
    }

    /**
     * Returns unmodifiable list of available templates.
     *
     * @return unmodifiable list of available templates
     */
    public List<JahiaTemplateDef> getTemplates() {
        checkForCahnges();
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
    public void addTemplateDefAll(List templateList,
                                  boolean inheritedFromParentPackage) {
        for (Iterator iterator = templateList.iterator(); iterator.hasNext();) {
            addTemplateDef((JahiaTemplateDef) iterator.next(),
                    inheritedFromParentPackage);
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

        if (name.endsWith(".jar")) {
            m_Type = JAR;
        } else if (name.endsWith(".war")) {
            m_Type = WAR;
        } else {
            m_Type = DIR;
        }
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
     * if the source is a file
     */
    public boolean isFile() {
        return (m_Type == JAR);
    }


    /**
     * if the source is a directory
     */
    public boolean isDirectory() {
        return (m_Type == DIR);
    }

    public boolean isWar() {
        return (m_Type == WAR);
    }


    public String getInitialImport() {
        return m_InitialImport;
    }

    public void setInitialImport(String initIport) {
        m_InitialImport = initIport;
    }

    /**
     * Returns the name of the parent template package.
     *
     * @return the name of the parent template package
     */
    public String getExtends() {
        return parent;
    }

    /**
     * Sets the name of the parent template package.
     *
     * @param ext name of the parent template package
     */
    public void setExtends(String ext) {
        parent = ext;
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
        checkForCahnges();
        return templatesReadOnly;
    }

    private void checkForCahnges() {
        if (changesMade) {
            List tmpList = new LinkedList();
            tmpList.addAll(templates.values());
            templateListReadOnly = UnmodifiableList.decorate(tmpList);
            Map<String, JahiaTemplateDef> tmpMap = new TreeMap();
            for (JahiaTemplateDef def : templateListReadOnly) {
                tmpMap.put(def.getName(), def);
            }
            templatesReadOnly = UnmodifiableMap.decorate(tmpMap);

            homePageTemplate = homePageName != null ? templates
                    .get(homePageName) : null;
            defaultPageTemplate = defaultPageName != null ? templates
                    .get(defaultPageName) : null;

            if (templateListReadOnly.size() > 0) {
                if (homePageTemplate == null) homePageTemplate = templateListReadOnly.get(0);
                if (defaultPageTemplate == null) defaultPageTemplate = templateListReadOnly.get(0);
            }

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

    /**
     * Returns names of the template sets starting from this one, then the
     * direct parent and so on.
     *
     * @return names of the template sets starting from this one, then the
     *         direct parent and so on
     */
    public List<String> getHierarchy() {
        return hierarchy;
    }

    /**
     * Returns names of the template sets starting from highest parent
     *
     * @return names of the template sets starting from highest parent
     */
    public List<String> getInvertedHierarchy() {
        if (hierarchy != null && hierarchy.size() > 1) {
            final List<String> result = new ArrayList<String>(hierarchy.size());
            for (int i = hierarchy.size() - 1; i > -1; i--) {
                result.add(hierarchy.get(i));
            }
            return result;
        } else {
            return hierarchy;
        }
    }

    /**
     * Returns a list of root folders (based on hierarchy path), to lookup pages
     * starting from the current root folder, than direct parent's root folder and
     * so on.
     *
     * @return a list of root folders (based on hierarchy path), to lookup pages
     *         starting from the current root folder, than direct parent's root
     *         folder and so on
     */
    public List<String> getLookupPath() {
        return lookupPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHomePageName() {
        return homePageName;
    }

    public void setHomePageName(String homePageName) {
        this.homePageName = homePageName;
        changesMade = true;
    }

    public String getDefaultPageName() {
        return defaultPageName;
    }

    public void setDefaultPageName(String defaultPageName) {
        this.defaultPageName = defaultPageName;
        changesMade = true;
    }

    public String getMySettingsPageName() {
        return mySettingsPageName;
    }

    public void setMySettingsPageName(String mySettingsPageName) {
        this.mySettingsPageName = mySettingsPageName;
    }

    public String getSearchResultsPageName() {
        return searchResultsPageName;
    }

    public void setSearchResultsPageName(String searchResultsPageName) {
        this.searchResultsPageName = searchResultsPageName;
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

    public String getMySettingsSuccessPageName() {
        return mySettingsSuccessPageName;
    }

    public void setMySettingsSuccessPageName(String mySettingsSuccessPageName) {
        this.mySettingsSuccessPageName = mySettingsSuccessPageName;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public String addProperty(String key, String value) {
        return properties.put(key, value);
    }

    public List<String> getResourceBundleHierarchy() {
        return resourceBundleHierarchy;
    }

   public void clearHierarchy() {
        getHierarchy().clear();
        getLookupPath().clear();
        getResourceBundleHierarchy().clear();
    }
}