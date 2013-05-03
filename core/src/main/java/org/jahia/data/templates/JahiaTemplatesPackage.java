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
import org.jahia.bin.Jahia;
import org.jahia.osgi.BundleResource;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.services.templates.SourceControlManagement;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Holds Informations about a templates package
 *
 * @author Khue ng
 */
public class JahiaTemplatesPackage {

    private static final Resource[] NO_RESOURCES = new Resource[0];
    
    private Bundle bundle = null;

    private ModuleState state;

    private ClassLoader classLoader;
    
    /**
     * the full path to the source file or directory
     */
    private String m_FilePath;

    /**
     * Name of the package
     */
    private String m_Name;

    private ModuleVersion version;
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

    private List<JahiaTemplatesPackage> dependencies = new ArrayList<JahiaTemplatesPackage>();

    private List<JahiaTemplatesPackage> dependantModules = new ArrayList<JahiaTemplatesPackage>();

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

    /**
     * @deprecated with no replacement
     */
    @Deprecated
    private long buildNumber;

    private String autoDeployOnSite;

    private AbstractApplicationContext context;

    private String scmURI;

    private File sourcesFolder;

    private SourceControlManagement sourceControl;

    private boolean isActiveVersion = false;

    private boolean isLastVersion = false;
    
    private boolean serviceInitialized;

    public JahiaTemplatesPackage(Bundle bundle) {
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public ModuleState getState() {
        return state;
    }

    public void setState(ModuleState state) {
        this.state = state;
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
    
    public String getRootFolderWithVersion() {
        return m_RootFolder + "/" + version.toString();
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
        return getRootFolder();
    }

    /**
     * Returns the source path of the root folder for the deployed template set.
     *
     * @return the source path of the root folder for the deployed template set
     */
    public String getRootFolderPath() {
        return rootFolderPath;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JahiaTemplatesPackage that = (JahiaTemplatesPackage) o;

        if (m_RootFolder != null ? !m_RootFolder.equals(that.m_RootFolder) : that.m_RootFolder != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (m_RootFolder != null ? m_RootFolder.hashCode() : 0);
        return result;
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
    public List<JahiaTemplatesPackage> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    public void addDependency(JahiaTemplatesPackage dep) {
        if (!dependencies.contains(dep)) {
            dependencies.add(dep);
        }
    }

    public void resetDependencies() {
        dependencies.clear();
    }

    /**
     * @deprecated with no replacement
     */
    @Deprecated
    public long getBuildNumber() {
        return buildNumber;
    }

    /**
     * @deprecated with no replacement
     */
    @Deprecated
    public void setBuildNumber(long buildNumber) {
        this.buildNumber = buildNumber;
    }

    public ModuleVersion getVersion() {
        return version;
    }

    public void setVersion(ModuleVersion version) {
        this.version = version;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public void setAutoDeployOnSite(String autoDeployOnSite) {
        this.autoDeployOnSite = autoDeployOnSite;
    }

    public String getAutoDeployOnSite() {
        return autoDeployOnSite;
    }

    public AbstractApplicationContext getContext() {
        return context;
    }

    public void setContext(AbstractApplicationContext context) {
        this.context = context;
        // reset services state
        serviceInitialized = false;
    }

    public boolean isActiveVersion() {
        return isActiveVersion;
    }

    public void setActiveVersion(boolean activeVersion) {
        isActiveVersion = activeVersion;
    }

    public boolean isLastVersion() {
        return isLastVersion;
    }

    public void setLastVersion(boolean lastVersion) {
        isLastVersion = lastVersion;
    }

    public String getScmURI() {
        return scmURI;
    }

    public void setScmURI(String scmURI) {
        this.scmURI = scmURI;
    }

    public File getSourcesFolder() {
        return sourcesFolder;
    }

    public void setSourcesFolder(File sourcesFolder) {
        this.sourcesFolder = sourcesFolder;
    }

    public SourceControlManagement getSourceControl() {
        return sourceControl;
    }

    public void setSourceControl(SourceControlManagement sourceControl) throws IOException {
        this.sourceControl = sourceControl;
        if (sourceControl != null) {
            this.scmURI = sourceControl.getURI();
        }
    }

    public Resource getResource(String relativePath) {
        if (relativePath == null) {
            return null;
        }
        URL entryURL = bundle.getEntry(relativePath);
        if (entryURL != null) {
            return new BundleResource(entryURL, bundle);
        } else {
            return null;
        }
    }

    public Resource[] getResources(String relativePath) {
        @SuppressWarnings("unchecked")
        Enumeration<URL> resourceEnum = bundle.findEntries(relativePath, null, false);
        List<Resource> resources = new ArrayList<Resource>();
        if (resourceEnum == null) {
            return NO_RESOURCES;
        } else {
            while (resourceEnum.hasMoreElements()) {
                resources.add(new BundleResource(resourceEnum.nextElement(), bundle));
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getChainedClassLoader() {
        final List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();
        classLoaders.add(Jahia.class.getClassLoader());
        if (getClassLoader() != null) {
            classLoaders.add(getClassLoader());
        }
        for (JahiaTemplatesPackage dependentPack : getDependencies()) {
            if (dependentPack != null && dependentPack.getClassLoader() != null) {
                classLoaders.add(dependentPack.getClassLoader());
            }
        }
        return new ClassLoader() {
            public URL getResource(String name) {
                URL url = null;
                for (ClassLoader loader : classLoaders) {
                    url = loader.getResource(name);
                    if (url != null)
                        return url;
                }
                return url;
            }

            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                for (ClassLoader loader : classLoaders) {
                    try {
                        return loader.loadClass(name);
                    }
                    catch (ClassNotFoundException e) {
                        // keep moving through the classloaders
                    }
                }
                throw new ClassNotFoundException(name);
            }
        };
    }
    
    public void setRootFolderPath(String rootFolderPath) {
        this.rootFolderPath = rootFolderPath;
    }

    /**
     * Returns <code>true</code> if the Spring beans, implementing {@link JahiaAfterInitializationService}, were already initialized.
     * 
     * @return the serviceInitialized
     */
    public boolean isServiceInitialized() {
        return serviceInitialized;
    }

    /**
     * Set this to <code>true</code> to indicate that the Spring beans, implementing {@link JahiaAfterInitializationService}, were already
     * initialized.
     * 
     * @param serviceInitialized
     *            the state of the service initialization
     */
    public void setServiceInitialized(boolean serviceInitialized) {
        this.serviceInitialized = serviceInitialized;
    }
}