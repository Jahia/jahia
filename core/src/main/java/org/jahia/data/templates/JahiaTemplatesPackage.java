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
import org.jahia.bin.Jahia;
import org.jahia.osgi.BundleResource;
import org.jahia.osgi.BundleUtils;
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
    private boolean sourcesDownloadable;

    private String forgeUrl;

    /**
     * Initializes an instance of this class.
     * 
     * @param bundle
     *            the backing OSGi bundle for this module
     */
    public JahiaTemplatesPackage(Bundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Returns the backing OSGi bundle for this module.
     * 
     * @return the backing OSGi bundle for this module
     */
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Retrieves the module state information.
     * 
     * @return the module state information
     */
    public ModuleState getState() {
        return state;
    }

    /**
     * Sets the module state information.
     * 
     * @param state
     *            the module state information
     */
    public void setState(ModuleState state) {
        this.state = state;
    }

    /**
     * Return the template name.
     *
     * @return (String) the name of the template
     */
    public String getName() {

        return m_Name;
    }


    /**
     * Set the name.
     *
     * @param name the name of the template
     */
    public void setName(String name) {

        m_Name = name;
    }


    /**
     * Return the Root Folder.
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
     * Set the Root Folder.
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
     * Return the provider name.
     *
     * @return (String) the name of the Provider
     */
    public String getProvider() {

        return m_Provider;
    }


    /**
     * Set the Provider.
     *
     * @param provider the name of the Provider
     */
    public void setProvider(String provider) {

        m_Provider = provider;
    }


    /**
     * Return the thumbnail file name.
     *
     * @return (String) the thumbnail file name
     */
    public String getThumbnail() {

        return m_Thumbnail;
    }


    /**
     * Set the thumbnail file name.
     *
     * @param val the file name
     */
    public void setThumbnail(String val) {

        m_Thumbnail = val;
    }

    /**
     * Get the file path.
     */
    public String getFilePath() {
        return this.m_FilePath;
    }


    /**
     * Set the file path.
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

    @Override
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


    /**
     * Returns the description of this module is available.
     * 
     * @return the description of this module is available
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description for this module.
     * 
     * @param description
     *            the description text
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the name (path) of the resource bundle for this module.
     * 
     * @return the name (path) of the resource bundle for this module
     */
    public String getResourceBundleName() {
        return resourceBundleName;
    }

    /**
     * Sets the resource bundle name (path) for this module.
     * 
     * @param resourceBundleName
     *            the resource bundle name (path) for this module
     */
    public void setResourceBundleName(String resourceBundleName) {
        this.resourceBundleName = resourceBundleName;
    }

    /**
     * Returns a list of content node definition files (CND) available in the module.
     * 
     * @return a list of content node definition files (CND) available in the module
     */
    public List<String> getDefinitionsFiles() {
        return definitionsFile;
    }

    /**
     * Sets the list of content node definition files (CND) available in the module.
     * 
     * @param definitionFile
     *            the list of content node definition files (CND) available in the module
     */
    public void setDefinitionsFile(String definitionFile) {
        definitionsFile.add(definitionFile);
    }

    /**
     * Returns a list of rule files available in the module.
     * 
     * @return a list of rule files available in the module
     */
    public List<String> getRulesFiles() {
        return rulesFiles;
    }

    /**
     * Sets a list of rule files available in the module.
     * 
     * @param rulesFile
     *            a list of rule files available in the module
     */
    public void setRulesFile(String rulesFile) {
        rulesFiles.add(rulesFile);
    }

    /**
     * Returns a list with the resource bundle lookup chain for this module.
     * 
     * @return a list with the resource bundle lookup chain for this module
     */
    public List<String> getResourceBundleHierarchy() {
        return resourceBundleHierarchy;
    }

    /**
     * Resets the resource bundle lookup hierarchy.
     */
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

    /**
     * Returns a list with the rule DSL files for this module.
     * 
     * @return a list with the rule DSL files for this module
     */
    public List<String> getRulesDescriptorFiles() {
        return rulesDescriptorFiles;
    }

    /**
     * Sets the list with the rule DSL files for this module.
     * 
     * @param rulesDescriptorFiles
     *            a list with the rule DSL files for this module
     */
    public void setRulesDescriptorFile(String rulesDescriptorFiles) {
        this.rulesDescriptorFiles.add(rulesDescriptorFiles);
    }


    /**
     * Returns a list of modules which this module depends on.
     * 
     * @return a list of modules which this module depends on
     */
    public List<JahiaTemplatesPackage> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    /**
     * Add the provided module into the list of dependencies for this one.
     * 
     * @param dep
     *            a module to add as a dependency for this module
     */
    public void addDependency(JahiaTemplatesPackage dep) {
        if (!dependencies.contains(dep)) {
            dependencies.add(dep);
        }
    }

    /**
     * Reset the module dependencies.
     */
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

    /**
     * Returns the version information for this module.
     * 
     * @return the version information for this module
     */
    public ModuleVersion getVersion() {
        return version;
    }

    /**
     * Sets the version information for this module.
     * 
     * @param version
     *            the version information for this module
     */
    public void setVersion(ModuleVersion version) {
        this.version = version;
    }

    /**
     * Returns the type of this module.
     * 
     * @return the type of this module
     */
    public String getModuleType() {
        return moduleType;
    }

    /**
     * Sets the type of this module.
     * 
     * @param moduleType
     *            the type of this module
     */
    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    /**
     * Sets the value for the <code>autoDeployOnSite</code> directive which defines on which sites this module should be automatically
     * installed.
     * 
     * @param autoDeployOnSite
     *            the value for the <code>autoDeployOnSite</code> directive which defines on which sites this module should be automatically
     *            installed
     */
    public void setAutoDeployOnSite(String autoDeployOnSite) {
        this.autoDeployOnSite = autoDeployOnSite;
    }

    /**
     * Returns the value for the <code>autoDeployOnSite</code> directive which defines on which sites this module should be automatically
     * installed.
     * 
     * @return the value for the <code>autoDeployOnSite</code> directive which defines on which sites this module should be automatically
     *         installed
     */
    public String getAutoDeployOnSite() {
        return autoDeployOnSite;
    }

    /**
     * Returns the Spring application context instance for this module. <code>null</code> in case this module has no application context
     * provided.
     * 
     * @return the Spring application context instance for this module. <code>null</code> in case this module has no application context
     *         provided
     */
    public AbstractApplicationContext getContext() {
        return context;
    }

    /**
     * Sets the application context instance for this module.
     * 
     * @param context
     *            the application context instance for this module
     */
    public void setContext(AbstractApplicationContext context) {
        this.context = context;
        // reset services state
        serviceInitialized = false;
    }

    /**
     * Checks if this is the version of the module which is currently active.
     * 
     * @return <code>true</code> if this version of the module is currently active.
     */
    public boolean isActiveVersion() {
        return isActiveVersion;
    }

    /**
     * Sets the active state of this module version.
     * 
     * @param activeVersion
     *            the active state of this module version
     */
    public void setActiveVersion(boolean activeVersion) {
        isActiveVersion = activeVersion;
    }

    /**
     * Checks if this is the latest available version of this module.
     * 
     * @return <code>true</code> if this is the latest version of this module
     */
    public boolean isLastVersion() {
        return isLastVersion;
    }

    /**
     * Sets the flag for the latest available module version.
     * 
     * @param lastVersion
     *            the flag for the latest available module version
     */
    public void setLastVersion(boolean lastVersion) {
        isLastVersion = lastVersion;
    }

    /**
     * Returns an SCM URI for this module if the source control is available for it.
     * 
     * @return an SCM URI for this module if the source control is available for it
     */
    public String getScmURI() {
        return scmURI;
    }

    /**
     * Sets the SCM URI for this module if the source control is available for it.
     * 
     * @param scmURI the SCM URI for this module if the source control is available for it
     */
    public void setScmURI(String scmURI) {
        this.scmURI = scmURI;
    }

    /**
     * Returns the file descriptor, representing the folder with the sources for this module if available.
     * 
     * @return the file descriptor, representing the folder with the sources for this module if available
     */
    public File getSourcesFolder() {
        return sourcesFolder;
    }

    /**
     * Sets the file descriptor, representing the folder with the sources for this module if available.
     * 
     * @param sourcesFolder
     *            the file descriptor, representing the folder with the sources for this module if available
     */
    public void setSourcesFolder(File sourcesFolder) {
        this.sourcesFolder = sourcesFolder;
    }

    /**
     * Returns an instance of the {@link SourceControlManagement} for this module.
     * 
     * @return an instance of the {@link SourceControlManagement} for this module
     */
    public SourceControlManagement getSourceControl() {
        return sourceControl;
    }

    /**
     * Sets an instance of the source control management for this module.
     * 
     * @param sourceControl
     *            an instance of the source control management for this module
     * @throws IOException
     *             in case of an exception when retrieving SCM URL
     */
    public void setSourceControl(SourceControlManagement sourceControl) throws IOException {
        this.sourceControl = sourceControl;
        if (sourceControl != null) {
            this.scmURI = sourceControl.getURI();
        }
    }

    /**
     * Returns a resource from the module's bundle for the specified path.
     * 
     * @param relativePath
     *            a path of the bundle resource
     * @return a resource from the module's bundle for the specified path
     * @see Bundle#getEntry(String)
     */
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

    /**
     * Returns resources from the module's bundle and its attached fragments for the specified path.
     * 
     * @param relativePath
     *            a path of the bundle resource
     * @return resources from the module's bundle and its attached fragments for the specified path
     * @see Bundle#findEntries(String, String, boolean)
     */
    public Resource[] getResources(String relativePath) {
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

    /**
     * Returns the module bundle's class loader instance.
     * 
     * @return the module bundle's class loader instance
     */
    public ClassLoader getClassLoader() {
        if (classLoader == null && bundle != null) {
            classLoader = BundleUtils.createBundleClassLoader(bundle);
        }
        return classLoader;
    }

    /**
     * Sets the class loader instance for this module
     * 
     * @param classLoader
     *            a class loader instance for this module
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Returns a class loader which is a chain of class loaders, starting from the Web application one, than this modules class loader and
     * at the end the list of class loaders of modules, this module depends on.
     * 
     * @return a class loader which is a chain of class loaders, starting from the Web application one, than this modules class loader and
     *         at the end the list of class loaders of modules, this module depends on
     */
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
    
    /**
     * Sets the root path for the module folder.
     * 
     * @param rootFolderPath
     *            the root path for the module folder
     */
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

    /**
     * Sets the flag for downloadable sources.
     * 
     * @param sourcesDownloadable
     *            value of the flag for downloadable sources
     */
    public void setSourcesDownloadable(boolean sourcesDownloadable) {
        this.sourcesDownloadable = sourcesDownloadable;
    }

    /**
     * Checks if the sources for this module can be downloaded.
     * 
     * @return <code>true</code> if the sources for this module can be downloaded; <code>false</code> otherwise
     */
    public boolean isSourcesDownloadable() {
        return sourcesDownloadable;
    }

    /**
     * Returns the URL of the corresponding Jahia forge.
     * 
     * @return the URL of the corresponding Jahia forge
     */
    public String getForgeUrl() {
        return forgeUrl;
    }

    /**
     * Sets the URL of the corresponding Jahia forge.
     * 
     * @param forgeUrl
     *            the URL of the corresponding Jahia forge
     */
    public void setForgeUrl(String forgeUrl) {
        this.forgeUrl = forgeUrl;
    }
}