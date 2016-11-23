/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.services.templates.SourceControlManagement;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds Informations about a templates package
 *
 * @author Khue ng
 */
public class JahiaTemplatesPackage {

    /**
     * The ID of the default module.
     */
    public static final String ID_DEFAULT = "default";

    /**
     * The name of the default module.
     */
    public static final String NAME_DEFAULT = "Default Jahia Templates";

    private static final Resource[] NO_RESOURCES = new Resource[0];

    private static final String GIT_URI_END = ".git";

    private static URL NULL_URL;

    static {
        try {
            NULL_URL = new URL("http://");
        } catch (MalformedURLException e) {
            //
        }
    }

    private Bundle bundle = null;

    private String bundleKey;

    private ModuleState state;

    private ClassLoader classLoader;

    private ClassLoader chainedClassLoader;

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
     * The module id (= artifactId)
     */
    private String id;

    private String groupId;

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

    private LinkedHashMap<String, JahiaTemplatesPackage> dependencies;

    private int modulePriority = 0;

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

    private Map<String,URL> resourcesCache = new ConcurrentHashMap<String, URL>();

    /**
     * @deprecated with no replacement
     */
    @Deprecated
    private long buildNumber;

    private String autoDeployOnSite;

    private AbstractApplicationContext context;

    private String scmURI;

    private String scmTag;

    private File sourcesFolder;

    private SourceControlManagement sourceControl;

    private boolean isActiveVersion = false;

    private boolean isLastVersion = false;

    private boolean serviceInitialized;

    private boolean sourcesDownloadable;

    private String forgeUrl;

    private boolean editModeBlocked;

    /**
     * List of callbacks to execute when spring context is set
     */
    private final List<ContextInitializedCallback> contextInitializedCallbacks = new ArrayList<>();

    /**
     * Initializes an instance of this class.
     *
     * @param bundle
     *            the backing OSGi bundle for this module
     */
    public JahiaTemplatesPackage(Bundle bundle) {
        this.bundle = bundle;
        resetDependencies();
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
     * Return the module Id.
     *
     * @return (String) the module Id
     * @deprecated use {@link #getId()} instead
     */
    public String getRootFolder() {
        return getId();
    }

    /**
     * Return the module Id.
     *
     * @return (String) the module Id
     */
    public String getId() {
        return id;
    }

    /**
     * Return the module Id concatenated with it version
     *
     * @return the module Id concatenated with it version
     * @deprecated use {@link #getIdWithVersion()} instead
     */
    public String getRootFolderWithVersion() {
        return getIdWithVersion();
    }

    /**
     * Return the module Id concatenated with it version
     *
     * @return the module Id concatenated with it version
     */
    public String getIdWithVersion() {
        return id + "/" + version.toString();
    }

    /**
     * Set the module Id.
     *
     * @param moduleId the module Id
     * @deprecated use {@link #setId(String)} instead
     */
    public void setRootFolder(String moduleId) {
        setId(moduleId);
    }

    /**
     * Set the module Id.
     *
     * @param moduleId the module Id
     */
    public void setId(String moduleId) {
        if (StringUtils.isNotEmpty(moduleId)) {
            id = moduleId;
            SettingsBean conf = SettingsBean.getInstance();
            rootFolderPath = conf.getTemplatesContext() + (conf.getTemplatesContext().endsWith("/") ? "" : "/") + moduleId;
        } else {
            id = "";
            rootFolderPath = SettingsBean.getInstance().getTemplatesContext();
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
        // force recompute of bundle key
        this.bundleKey = null;
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
        return getId() != null && ID_DEFAULT.equals(getId());
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
        return getId();
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

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JahiaTemplatesPackage that = (JahiaTemplatesPackage) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
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
     * Returns a read-only list of modules which this module depends on.
     *
     * @return a read-only list of modules which this module depends on
     */
    public List<JahiaTemplatesPackage> getDependencies() {
        return Collections.unmodifiableList(new ArrayList<>(dependencies.values()));
    }

    /**
     * Add the provided module into the list of dependencies for this one.
     *
     * @param dep
     *            a module to add as a dependency for this module
     */
    public void addDependency(JahiaTemplatesPackage dep) {
        if (dep != null) {
            dependencies.put(dep.getId(), dep);
            // reset chained classloader
            chainedClassLoader = null;
        }
    }

    /**
     * Reset the module dependencies.
     */
    public void resetDependencies() {
        dependencies = new LinkedHashMap<>();
        // reset chained classloader
        chainedClassLoader = null;
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
     * Return the module priority, used for views and resource resolution.
     *
     * @return the module priority
     */
    public int getModulePriority() {
        return modulePriority;
    }

    /**
     * Set the module priority for this module
     * @param modulePriority
     */
    public void setModulePriority(int modulePriority) {
        this.modulePriority = modulePriority;
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

        if (this.context != null && state.getState().equals(ModuleState.State.SPRING_STARTING)) {
            state.setState(ModuleState.State.STARTED);
        }

        // executes callbacks if needed
        if(this.context != null && !contextInitializedCallbacks.isEmpty()) {
            for (ContextInitializedCallback contextInitializedCallback : contextInitializedCallbacks) {
                contextInitializedCallback.execute(context);
            }
            // clear callbacks
            contextInitializedCallbacks.clear();
        }
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
        if (scmURI != null) {
            int index = scmURI.lastIndexOf(GIT_URI_END);
            if (index > -1) {
                scmURI = scmURI.substring(0, index + GIT_URI_END.length());
            }
        }
        this.scmURI = scmURI;
    }

    /**
     * Returns the SCM tag of this module if it's released
     * @return the SCM tag of this module if it's released
     */
    public String getScmTag() {
        return scmTag;
    }

    /**
     * Set the SCM tag of this module if it's released
     */
    public void setScmTag(String scmTag) {
        this.scmTag = scmTag;
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
        if (getSourcesFolder() != null && getSourcesFolder().exists()) {
            try {
                File file = new File(getSourcesFolder(), "src/main/resources/" + relativePath);
                if (file.exists()) {
                    return new UrlResource(file.toURI());
                }
            } catch (MalformedURLException e) {
                // file.toURI cannot return malformed URL
            }
        }
        URL entryURL = getResourceFromCache(relativePath);
        if (entryURL != null) {
            return new BundleResource(entryURL, bundle);
        }
        return null;
    }

    /**
     * Checks if the specified resource is present in the module's bundle.
     *
     * @param relativePath
     *            a path of the bundle resource
     * @return <code>true</code> if the specified resource is present in the module's bundle; <code>false</code> otherwise
     * @see Bundle#getEntry(String)
     */
    public boolean resourceExists(String relativePath) {
        if (relativePath == null) {
            return false;
        }
        if (getSourcesFolder() != null && getSourcesFolder().exists()) {
            if ((new File(getSourcesFolder(), "src/main/resources/" + relativePath)).exists()) {
                return true;
            }
        }
        return getResourceFromCache(relativePath) != null;
    }

    private URL getResourceFromCache(String relativePath) {
        URL url = resourcesCache.get(relativePath);
        if (url == null) {
            url = bundle.getEntry(relativePath);
            resourcesCache.put(relativePath, url != null ? url : NULL_URL);
        }
        return url != NULL_URL ? url : null;
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
        List<Resource> resources = null;
        File sourceLocation = getSourcesFolder() != null ? new File(getSourcesFolder(), "src/main/resources/"
                + relativePath) : null;
        if (sourceLocation != null && sourceLocation.exists()) {
            File[] files = sourceLocation.listFiles();
            if (files == null || files.length == 0) {
                return NO_RESOURCES;
            }
            resources = new ArrayList<Resource>();
            for (File file : files) {
                try {
                    resources.add(new UrlResource(file.toURI()));
                } catch (MalformedURLException e) {
                    // file.toURI cannot return malformed URL
                }
            }
        } else {
            Enumeration<URL> resourceEnum = bundle.findEntries(relativePath, null, false);
            if (resourceEnum == null) {
                return NO_RESOURCES;
            } else {
                resources = new ArrayList<Resource>();
                while (resourceEnum.hasMoreElements()) {
                    resources.add(new BundleResource(resourceEnum.nextElement(), bundle));
                }
            }
        }
        return resources != null ? resources.toArray(new Resource[resources.size()]) : NO_RESOURCES;
    }

    /**
     * Returns the module bundle's class loader instance.
     *
     * @return the module bundle's class loader instance
     */
    public ClassLoader getClassLoader() {
        if (classLoader == null && bundle != null && state != null && state.getState() != null
                && state.getState() != ModuleState.State.INSTALLED) {
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

        // classloader updated, reset chainedClassloader
        // (will be reconstructed on next call to getChainedClassLoader())
        this.chainedClassLoader = null;
    }

    /**
     * Returns a class loader which is a chain of class loaders, starting from the Web application one, then this modules class loader and
     * at the end the list of class loaders of modules this module depends on.
     *
     * @return a class loader which is a chain of class loaders, starting from the Web application one, then this modules class loader and
     *         at the end the list of class loaders of modules this module depends on
     */
    public ClassLoader getChainedClassLoader() {

        if (chainedClassLoader != null) {
            return chainedClassLoader;
        }

        final List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();
        classLoaders.add(Jahia.class.getClassLoader());
        final ClassLoader classLoader = getClassLoader();
        if (classLoader != null) {
            classLoaders.add(classLoader);
        }
        for (JahiaTemplatesPackage dependentPack : getDependencies()) {
            if (dependentPack != null && dependentPack.getClassLoader() != null) {
                classLoaders.add(dependentPack.getClassLoader());
            }
        }

        chainedClassLoader = new ClassLoader() {

            @Override
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
            public Enumeration<URL> getResources(String name) throws IOException {

                final List<Enumeration<URL>> urlsEnums = new ArrayList<Enumeration<URL>>();
                for (ClassLoader loader : classLoaders) {
                    Enumeration<URL> urls = loader.getResources(name);
                    if (urls != null && urls.hasMoreElements()) {
                        // we only add enumerations that have elements, make things simpler
                        urlsEnums.add(urls);
                    }
                }

                if (urlsEnums.size() == 0) {
                    return java.util.Collections.emptyEnumeration();
                }

                return new Enumeration<URL>() {

                    int i=0;
                    Enumeration<URL> currentEnum = urlsEnums.get(i);

                    @Override
                    public boolean hasMoreElements() {
                        if (currentEnum.hasMoreElements()) {
                            return true;
                        }
                        int j=i;
                        do {
                            j++;
                        } while (j < (urlsEnums.size()-1) && !urlsEnums.get(j).hasMoreElements());
                        if (j <= (urlsEnums.size()-1)) {
                            return urlsEnums.get(j).hasMoreElements();
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public URL nextElement() {
                        if (currentEnum.hasMoreElements()) {
                            return currentEnum.nextElement();
                        }
                        do {
                            i++;
                            currentEnum = urlsEnums.get(i);
                        } while (!currentEnum.hasMoreElements() && i < (urlsEnums.size()-1));
                        if (currentEnum.hasMoreElements()) {
                            return currentEnum.nextElement();
                        } else {
                            throw new NoSuchElementException();
                        }
                    }
                };
            }

            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                for (ClassLoader classLoader : classLoaders) {
                    try {
                        Class<?> clazz = classLoader.loadClass(name);
                        if (resolve) {
                            resolveClass(clazz);
                        }
                        return clazz;
                    } catch (ClassNotFoundException e) {
                        // keep moving through the classloaders
                    }
                }
                throw new ClassNotFoundException(name);
            }
        };

        return chainedClassLoader;
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
     * Returns the URL of the corresponding Jahia Private App Store.
     *
     * @return the URL of the corresponding Jahia Private App Store
     */
    public String getForgeUrl() {
        return forgeUrl;
    }

    /**
     * Sets the URL of the corresponding Jahia Private App Store.
     *
     * @param forgeUrl
     *            the URL of the corresponding Jahia Private App Store
     */
    public void setForgeUrl(String forgeUrl) {
        this.forgeUrl = forgeUrl;
    }

    public boolean isEditModeBlocked() {
        return editModeBlocked;
    }

    public void setEditModeBlocked(boolean editModeBlocked) {
        this.editModeBlocked = editModeBlocked;
    }

    /**
     * Provide a callback that will be execute when the Spring context is ready for this bundle
     * if the context is already set, the callback is executed directly
     * if no context available, the callback is stored and will be execute when a context is set for the current JahiaTemplatePackage
     * the callback is removed when executed
     *
     * @param contextInitializedCallback the callback
     */
    public void doExecuteAfterContextInitialized(ContextInitializedCallback contextInitializedCallback) {
        if (context != null) {
            // context already available, execute now
            contextInitializedCallback.execute(context);
        } else {
            contextInitializedCallbacks.add(contextInitializedCallback);
        }
    }

    /**
     * Callback object to do operations just after the initialization of the spring context
     */
    public interface ContextInitializedCallback {
        /**
         * Do something after spring context is initialized
         *
         * @param context the spring context
         */
        void execute(AbstractApplicationContext context);
    }

    /**
     * Returns unique bundle key which corresponds to this module, including group ID, bundle symbolic name and version.
     *
     * @return unique bundle key which corresponds to this module, including group ID, bundle symbolic name and version
     * @see BundleInfo
     */
    public String getBundleKey() {
        if (bundleKey == null) {
            bundleKey = new BundleInfo(getGroupId(), bundle.getSymbolicName(), bundle.getVersion().toString()).getKey();
        }

        return bundleKey;
    }

    public Set<JahiaTemplatesPackage> getModuleDependenciesWithVersion() {
        Set<JahiaTemplatesPackage> deps = new HashSet<>();
        if (bundle != null && bundle.getState() >= Bundle.RESOLVED) {
            BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);

            List<BundleWire> requiredWires = bundleWiring.getRequiredWires(null);
            for (BundleWire wire : requiredWires) {
                Bundle bundle = wire.getProvider().getBundle();
                if (BundleUtils.isJahiaBundle(bundle)) {
                    deps.add(BundleUtils.getModule(bundle));
                }
            }
        }
        return deps;
    }

    public Set<JahiaTemplatesPackage> getDependentModulesWithVersion() {
        Set<JahiaTemplatesPackage> deps = new HashSet<>();
        if (bundle != null && bundle.getState() >= Bundle.RESOLVED) {
            BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);

            List<BundleWire> providedWires = bundleWiring.getProvidedWires(null);
            for (BundleWire wire : providedWires) {
                Bundle bundle = wire.getRequirer().getBundle();
                if (BundleUtils.isJahiaBundle(bundle)) {
                    deps.add(BundleUtils.getModule(bundle));
                }
            }
        }
        return deps;
    }
}