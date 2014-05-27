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
package org.jahia.services.templates;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaPrivilegeRegistry;
import org.jahia.bin.Action;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.bin.filters.CompositeFilter;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.CacheHelper;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRNodeDecoratorDefinition;
import org.jahia.services.content.decorator.validation.JCRNodeValidator;
import org.jahia.services.content.decorator.validation.JCRNodeValidatorDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializerService;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.jahia.services.content.nodetypes.renderer.ChoiceListRendererService;
import org.jahia.services.content.nodetypes.renderer.ModuleChoiceListRenderer;
import org.jahia.services.content.rules.BackgroundAction;
import org.jahia.services.content.rules.ModuleGlobalObject;
import org.jahia.services.content.rules.RulesListener;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.StaticAssetMapping;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.render.filter.RenderServiceAware;
import org.jahia.services.render.filter.cache.CacheKeyPartGenerator;
import org.jahia.services.render.filter.cache.DefaultCacheKeyGenerator;
import org.jahia.services.render.webflow.BundleFlowRegistry;
import org.jahia.services.visibility.VisibilityConditionRule;
import org.jahia.services.visibility.VisibilityService;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorklowTypeRegistration;
import org.jahia.utils.i18n.ResourceBundles;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;
import javax.servlet.Filter;

import java.util.*;

/**
 * Template packages registry service.
 *
 * @author Sergiy Shyrkov
 */
public class TemplatePackageRegistry {
    private static Logger logger = LoggerFactory.getLogger(TemplatePackageRegistry.class);

    private static boolean hasEncounteredIssuesWithDefinitions = false;

    static final String TEMPLATES_SET = "templatesSet";
    public static final Comparator<JahiaTemplatesPackage> TEMPLATE_PACKAGE_COMPARATOR = new Comparator<JahiaTemplatesPackage>() {
        public int compare(JahiaTemplatesPackage o1, JahiaTemplatesPackage o2) {
            if (o2.getModulePriority() != o1.getModulePriority()) {
                return o2.getModulePriority() - o1.getModulePriority();
            }
            if (!o1.getModuleType().equals(o2.getModuleType())) {
                if (o1.getModuleType().equals(TEMPLATES_SET)) return -99;
                if (o2.getModuleType().equals(TEMPLATES_SET)) return 99;
            }

            return o1.getName().compareTo(o2.getName());
        }
    };

    private Map<String, JahiaTemplatesPackage> packagesByName = new TreeMap<String, JahiaTemplatesPackage>();
    private Map<String, JahiaTemplatesPackage> packagesById = new TreeMap<String, JahiaTemplatesPackage>();
    private List<JahiaTemplatesPackage> templatePackages;
    private Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> packagesWithVersionByName = new TreeMap<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>>();
    private Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> packagesWithVersionById = new TreeMap<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>>();
    private Map<String, Set<JahiaTemplatesPackage>> modulesWithViewsPerComponents = new HashMap<String, Set<JahiaTemplatesPackage>>();
    private List<RenderFilter> filters = new LinkedList<RenderFilter>();
    private List<Filter> servletFilters = new LinkedList<Filter>();
    private List<ErrorHandler> errorHandlers = new LinkedList<ErrorHandler>();
    private Map<String, Action> actions;
    private Map<String, BackgroundAction> backgroundActions;
    private List<HandlerMapping> springHandlerMappings = new ArrayList<HandlerMapping>();
    private Map<String, JahiaTemplatesPackage> packagesForResourceBundles = new HashMap<String, JahiaTemplatesPackage>();
    private boolean afterInitializeDone = false;

    /**
     * Initializes an instance of this class.
     */
    @SuppressWarnings("unchecked")
    public TemplatePackageRegistry() {
        super();
        actions = new CaseInsensitiveMap();
        backgroundActions = new CaseInsensitiveMap();
    }

    public Map<String, Action> getActions() {
        return actions;
    }

    public Map<String, BackgroundAction> getBackgroundActions() {
        return backgroundActions;
    }

    public List<HandlerMapping> getSpringHandlerMappings() {
        return springHandlerMappings;
    }

    /**
     * Returns a list of {@link ErrorHandler} instances
     *
     * @return a list of {@link ErrorHandler} instances
     */
    public List<ErrorHandler> getErrorHandlers() {
        return errorHandlers;
    }

    public Map<String, Set<JahiaTemplatesPackage>> getModulesWithViewsPerComponents() {
        return modulesWithViewsPerComponents;
    }

    public void addModuleWithViewsForComponent(String component, JahiaTemplatesPackage module) {
        Set<JahiaTemplatesPackage> jahiaTemplatesPackages;
        if (modulesWithViewsPerComponents.containsKey(component)) {
            jahiaTemplatesPackages = modulesWithViewsPerComponents.get(component);
        } else {
            jahiaTemplatesPackages = new TreeSet<JahiaTemplatesPackage>(TEMPLATE_PACKAGE_COMPARATOR);
            modulesWithViewsPerComponents.put(component, jahiaTemplatesPackages);
        }
        jahiaTemplatesPackages.add(module);
    }

    public void removeModuleWithViewsForComponent(String component, JahiaTemplatesPackage module) {
        if (modulesWithViewsPerComponents.containsKey(component)) {
            Set<JahiaTemplatesPackage> jahiaTemplatesPackages = modulesWithViewsPerComponents.get(component);
            jahiaTemplatesPackages.remove(module);
            if (jahiaTemplatesPackages.isEmpty()) {
                modulesWithViewsPerComponents.remove(component);
            }
        }
    }

    private boolean computeDependencies(JahiaTemplatesPackage pack, JahiaTemplatesPackage currentPack) {
        for (String depends : currentPack.getDepends()) {
            JahiaTemplatesPackage dependentPack = packagesByName.get(depends);
            if (dependentPack == null) {
                dependentPack = packagesById.get(depends);
            }
            if (dependentPack == null) {
                return false;
            }
            if (!pack.getDependencies().contains(dependentPack)) {
                if (!computeDependencies(pack, dependentPack)) {
                    return false;
                }
                pack.addDependency(dependentPack);
            }
        }
        return true;
    }

    public void afterInitializationForModules() {
        for (JahiaTemplatesPackage pack : packagesByName.values()) {
            afterInitializationForModule(pack);
        }
        afterInitializeDone = true;
    }

    public boolean isAfterInitializeDone() {
        return afterInitializeDone;
    }

    public synchronized void afterInitializationForModule(JahiaTemplatesPackage pack) {
        if (pack.getContext() != null && !pack.isServiceInitialized()) {
            int count = 0;
            Map<String, JahiaAfterInitializationService> map = pack.getContext().getBeansOfType(JahiaAfterInitializationService.class);
            for (JahiaAfterInitializationService initializationService : map.values()) {
                try {
                    initializationService.initAfterAllServicesAreStarted();
                    count++;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (count > 0) {
                logger.info("Post-initialized {} beans implementing JahiaAfterInitializationService for module {}",
                        count, pack.getId());
            }
            pack.setServiceInitialized(true);
        }
    }


    /**
     * Checks if the specified template set is present in the repository.
     *
     * @param moduleName the template package name to check for
     * @return <code>true</code>, if the specified template package already
     * exists in the repository
     */
    public boolean contains(String moduleName) {
        return packagesByName.containsKey(moduleName);
    }

    /**
     * Checks if specified template sets are all present in the repository.
     *
     * @param moduleNames the list of template package names to check for
     * @return <code>true</code>, if specified template packages are all present
     * in the repository
     */
    public boolean containsAll(List<String> moduleNames) {
        return packagesByName.keySet().containsAll(moduleNames);
    }

    public boolean containsId(String moduleId) {
        return packagesById.containsKey(moduleId);
    }

    /**
     * Returns a list of all available template packages.
     *
     * @return a list of all available template packages
     */
    public List<JahiaTemplatesPackage> getAvailablePackages() {
        if (null == templatePackages) {
            templatePackages = Collections
                    .unmodifiableList(new LinkedList<JahiaTemplatesPackage>(
                            packagesByName.values()));
        }
        return templatePackages;
    }

    /**
     * Returns the number of available template packages in the registry.
     *
     * @return the number of available template packages in the registry
     */
    public int getAvailablePackagesCount() {
        return packagesByName.size();
    }

    public Set<ModuleVersion> getAvailableVersionsForModule(String moduleNameOrId) {
        if (packagesWithVersionById.containsKey(moduleNameOrId)) {
            return Collections.unmodifiableSortedSet((SortedSet<ModuleVersion>) packagesWithVersionById.get(moduleNameOrId).keySet());
        }
        if (packagesWithVersionByName.containsKey(moduleNameOrId)) {
            return Collections.unmodifiableSortedSet((SortedSet<ModuleVersion>) packagesWithVersionByName.get(moduleNameOrId).keySet());
        }
        return Collections.emptySet();
    }

    public boolean areVersionsForModuleAvailable(String moduleNameOrId) {
        SortedMap<ModuleVersion, JahiaTemplatesPackage> m = packagesWithVersionById.get(moduleNameOrId);
        if (m == null) {
            m = packagesWithVersionByName.get(moduleNameOrId);
        }
        return m != null && !m.isEmpty();
    }

    public Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> getAllModuleVersions() {
        return packagesWithVersionById;
    }

    public String getModuleId(String moduleNameOrId) {
        if (packagesWithVersionById.containsKey(moduleNameOrId)) {
            return moduleNameOrId;
        }
        if (packagesWithVersionByName.containsKey(moduleNameOrId)) {
            for (JahiaTemplatesPackage aPackage : packagesWithVersionByName.get(moduleNameOrId).values()) {
                return aPackage.getId();
            }
        }
        return null;
    }

    public Set<String> getModuleIds() {
        return packagesById.keySet();
    }

    public Set<String> getModuleNames() {
        return packagesByName.keySet();
    }

    public Map<String, JahiaTemplatesPackage> getRegisteredModules() {
        return packagesById;
    }

    /**
     * Returns a list of {@link RenderFilter} instances, configured for the specified templates package.
     *
     * @return a list of {@link RenderFilter} instances, configured for the specified templates package
     */
    public List<RenderFilter> getRenderFilters() {
        return filters;
    }

    /**
     * Indicates if any issue related to the definitions has been encountered since the last startup. When this method
     * returns true, the only way to get back false as a return value is to restart Jahia.
     *
     * @return true if an issue with the def has been encountered, false otherwise.
     * @since 6.6.2.0
     */
    public final boolean hasEncounteredIssuesWithDefinitions() {
        return hasEncounteredIssuesWithDefinitions;
    }

    /**
     * Returns the requested template package or <code>null</code> if the
     * package with the specified name is not registered in the repository.
     *
     * @param packageName the template package name to search for
     * @return the requested template package or <code>null</code> if the
     * package with the specified name is not registered in the
     * repository
     */
    public JahiaTemplatesPackage lookup(String packageName) {
        if (packageName == null || packagesByName == null) return null;
        return packagesByName.get(packageName);
    }

    /**
     * Returns the template package that corresponds to the provided OSGi bundle or <code>null</code> if the package is not registered.
     *
     * @param osgiBundle the corresponding OSGi bundle
     * @return the template package that corresponds to the provided OSGi bundle or <code>null</code> if the package is not registered
     */
    public JahiaTemplatesPackage lookupByBundle(Bundle osgiBundle) {
        if (packagesByName == null) {
            return null;
        }
        if (osgiBundle == null) {
            throw new IllegalArgumentException("OSGi bundle is null");
        }

        String moduleId = osgiBundle.getSymbolicName();
        String version = StringUtils.defaultIfEmpty((String) osgiBundle.getHeaders().get("Implementation-Version"),
                osgiBundle.getVersion().toString());

        return lookupByIdAndVersion(moduleId, new ModuleVersion(version));
    }

    /**
     * Returns the requested template package or <code>null</code> if the
     * package with the specified file name is not registered in the repository.
     *
     * @param fileName the template package Id to search for
     * @return the requested template package or <code>null</code> if the
     * package with the specified file name is not registered in the
     * repository
     * @deprecated use {@link #lookupById(String)} instead
     */
    public JahiaTemplatesPackage lookupByFileName(String fileName) {
        return lookupById(fileName);
    }

    /**
     * Returns the requested template package or <code>null</code> if the
     * package with the specified Id is not registered in the repository.
     *
     * @param moduleId the template package Id to search for
     * @return the requested template package or <code>null</code> if the
     * package with the specified Id is not registered in the
     * repository
     */
    public JahiaTemplatesPackage lookupById(String moduleId) {
        if (moduleId == null || packagesById == null) return null;
        return packagesById.containsKey(moduleId) ? packagesById.get(moduleId)
                : null;
    }

    public JahiaTemplatesPackage lookupByNameAndVersion(String moduleName, ModuleVersion moduleVersion) {
        if (moduleName == null || packagesByName == null) return null;
        Map<ModuleVersion, JahiaTemplatesPackage> packageVersions = packagesWithVersionByName.get(moduleName);
        if (packageVersions != null) {
            return packageVersions.get(moduleVersion);
        } else {
            return null;
        }
    }

    /**
     * @deprecated use {@link #lookupByIdAndVersion(String, ModuleVersion)} instead
     */
    public JahiaTemplatesPackage lookupByFileNameAndVersion(String fileName, ModuleVersion moduleVersion) {
        return lookupByIdAndVersion(fileName, moduleVersion);
    }

    public JahiaTemplatesPackage lookupByIdAndVersion(String moduleId, ModuleVersion moduleVersion) {
        if (moduleId == null || packagesByName == null) return null;
        Map<ModuleVersion, JahiaTemplatesPackage> packageVersions = packagesWithVersionById.get(moduleId);
        if (packageVersions != null) {
            return packageVersions.get(moduleVersion);
        } else {
            return null;
        }
    }

    public void registerPackageVersion(JahiaTemplatesPackage pack) {
        if (!packagesWithVersionById.containsKey(pack.getId())) {
            packagesWithVersionById.put(pack.getId(), new TreeMap<ModuleVersion, JahiaTemplatesPackage>());
        }
        SortedMap<ModuleVersion, JahiaTemplatesPackage> map = packagesWithVersionById.get(pack.getId());
        if (!packagesWithVersionByName.containsKey(pack.getName())) {
            packagesWithVersionByName.put(pack.getName(), map);
        }
        JahiaTemplatesPackage jahiaTemplatesPackage = map.get(pack.getVersion());
        if (jahiaTemplatesPackage == null || jahiaTemplatesPackage.getClass().equals(pack.getClass()) || !(pack.getClass().equals(JahiaTemplatesPackage.class))) {
            map.put(pack.getVersion(), pack);
        }
    }

    public void unregisterPackageVersion(JahiaTemplatesPackage pack) {
        Map<ModuleVersion, JahiaTemplatesPackage> map = packagesWithVersionById.get(pack.getId());
        map.remove(pack.getVersion());
        if (map.isEmpty()) {
            packagesWithVersionById.remove(pack.getId());
            packagesWithVersionByName.remove(pack.getName());
        }
    }

    /**
     * Adds the template package to the repository.
     *
     * @param templatePackage the template package to add
     */
    public void register(final JahiaTemplatesPackage templatePackage) {
        templatePackages = null;
        if (packagesByName.get(templatePackage.getName()) != null) {
            JahiaTemplatesPackage previousPack = packagesByName.get(templatePackage.getName());
            previousPack.setActiveVersion(false);
        }

        packagesByName.put(templatePackage.getName(), templatePackage);
        packagesById.put(templatePackage.getId(), templatePackage);

        // handle dependencies
        computeDependencies(templatePackage);

        // handle resource bundles
//        for (JahiaTemplatesPackage sourcePack : registry.values()) {
//        computeResourceBundleHierarchy(templatePackage);
//        }

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    if (session.itemExists("/modules/" + templatePackage.getIdWithVersion() + "/permissions")) {
                        JahiaPrivilegeRegistry.addModulePrivileges(session, "/modules/" + templatePackage.getIdWithVersion());
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Cannot get permissions in module", e);
        }

        Resource[] rootResources = templatePackage.getResources("");
        for (Resource rootResource : rootResources) {
            if (templatePackage.getResources(rootResource.getFilename()).length > 0 && rootResource.getFilename().contains("_")) {
                String key = rootResource.getFilename();
                if (!modulesWithViewsPerComponents.containsKey(key)) {
                    modulesWithViewsPerComponents.put(key, new TreeSet<JahiaTemplatesPackage>(TEMPLATE_PACKAGE_COMPARATOR));
                }
                modulesWithViewsPerComponents.get(key).remove(templatePackage);
                modulesWithViewsPerComponents.get(key).add(templatePackage);
            }
        }

        logger.info("Registered '{}' [{}] version {}", new Object[]{templatePackage.getName(),
                templatePackage.getId(), templatePackage.getVersion()});
    }


    public JahiaTemplatesPackage getPackageForResourceBundle(String resourceBundle) {
        return packagesForResourceBundles.get(resourceBundle);
    }

    public void addPackageForResourceBundle(String bundle, JahiaTemplatesPackage module) {
        packagesForResourceBundles.put(bundle, module);
    }

    private void computeResourceBundleHierarchy(JahiaTemplatesPackage templatePackage) {
        templatePackage.getResourceBundleHierarchy().clear();
        if (templatePackage.getResourceBundleName() != null) {
            templatePackage.getResourceBundleHierarchy().add(templatePackage.getResourceBundleName());
        }
        for (JahiaTemplatesPackage dependency : templatePackage.getDependencies()) {
            if (!dependency.isDefault() && dependency.getResourceBundleName() != null) {
                templatePackage.getResourceBundleHierarchy().add(dependency.getResourceBundleName());
            }
        }
        if (!templatePackage.isDefault() && packagesById.containsKey("default")) {
            templatePackage.getResourceBundleHierarchy().add(packagesById.get("default").getResourceBundleName());
            templatePackage.getResourceBundleHierarchy().add(ResourceBundles.JAHIA_TYPES_RESOURCES);
            templatePackage.getResourceBundleHierarchy().add(ResourceBundles.JAHIA_INTERNAL_RESOURCES);
        } else {
            templatePackage.getResourceBundleHierarchy().add(ResourceBundles.JAHIA_TYPES_RESOURCES);
            templatePackage.getResourceBundleHierarchy().add(ResourceBundles.JAHIA_INTERNAL_RESOURCES);
        }
        if (templatePackage.getResourceBundleName() != null) {
            addPackageForResourceBundle(templatePackage.getResourceBundleName(), templatePackage);
        }
    }

    public boolean computeDependencies(JahiaTemplatesPackage pack) {
        pack.resetDependencies();
        if (computeDependencies(pack, pack)) {
            computeResourceBundleHierarchy(pack);
            return true;
        }
        return false;
    }

    public List<JahiaTemplatesPackage> getDependantModules(JahiaTemplatesPackage module) {
        List<JahiaTemplatesPackage> modules = new ArrayList<JahiaTemplatesPackage>();
        for (JahiaTemplatesPackage aPackage : packagesByName.values()) {
            if (aPackage.getDepends().contains(module.getId()) || aPackage.getDepends().contains(module.getName())) {
                modules.add(aPackage);
            }
        }
        return modules;
    }

    public void reset() {
        for (JahiaTemplatesPackage pkg : new HashSet<JahiaTemplatesPackage>(packagesByName.values())) {
            unregister(pkg);
        }
        templatePackages = null;
    }

    public void unregister(JahiaTemplatesPackage templatePackage) {
        if (templatePackage.isActiveVersion()) {
            packagesByName.remove(templatePackage.getName());
            packagesById.remove(templatePackage.getId());
            templatePackages = null;
        }

        for (Set<JahiaTemplatesPackage> packages : modulesWithViewsPerComponents.values()) {
            packages.remove(templatePackage);
        }
    }

    public void resetBeanModules() {
        filters.clear();
        errorHandlers.clear();
        actions.clear();
        backgroundActions.clear();
    }

// -------------------------- INNER CLASSES --------------------------

    static class ModuleRegistry implements DestructionAwareBeanPostProcessor {
        private TemplatePackageRegistry templatePackageRegistry;

        private ChoiceListInitializerService choiceListInitializers;

        private ChoiceListRendererService choiceListRendererService;

        private RenderService renderService;

        private WorkflowService workflowService;

        private VisibilityService visibilityService;

        private Map<String, String> staticAssetMapping;

        private JCRStoreService jcrStoreService;

        private CompositeFilter compositeFilter;

        public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
            if (!JahiaContextLoaderListener.isRunning()) {
                return;
            }

            if (bean instanceof RenderFilter) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Unregistering RenderFilter '" + beanName + "'");
                }
                templatePackageRegistry.filters.remove((RenderFilter) bean);
            }
            if (bean instanceof ErrorHandler) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Unregistering ErrorHandler '" + beanName + "'");
                }
                templatePackageRegistry.errorHandlers.remove((ErrorHandler) bean);
            }
            if (bean instanceof Action) {
                Action action = (Action) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug("Unregistering Action '" + action.getName() + "' (" + beanName + ")");
                }
                templatePackageRegistry.actions.remove(action.getName());
            }
            if (bean instanceof ModuleChoiceListInitializer) {
                ModuleChoiceListInitializer moduleChoiceListInitializer = (ModuleChoiceListInitializer) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug("Unregistering ModuleChoiceListInitializer '" + moduleChoiceListInitializer.getKey() + "' (" + beanName + ")");
                }
                choiceListInitializers.getInitializers().remove(moduleChoiceListInitializer.getKey());
            }

            if (bean instanceof ModuleChoiceListRenderer) {
                ModuleChoiceListRenderer choiceListRenderer = (ModuleChoiceListRenderer) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug("Unregistering ChoiceListRenderer '" + choiceListRenderer.getKey() + "' (" + beanName + ")");
                }
                choiceListRendererService.getRenderers().remove(choiceListRenderer.getKey());
            }
            if (bean instanceof ModuleGlobalObject) {
                ModuleGlobalObject moduleGlobalObject = (ModuleGlobalObject) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug("Unregistering ModuleGlobalObject '" + beanName + "'");
                }
                if (moduleGlobalObject.getGlobalRulesObject() != null) {
                    for (RulesListener listener : RulesListener.getInstances()) {
                        for (Map.Entry<String, Object> entry : moduleGlobalObject.getGlobalRulesObject().entrySet()) {
                            listener.removeGlobalObject(entry.getKey());
                        }
                    }
                }
            }
            if (bean instanceof StaticAssetMapping) {
                StaticAssetMapping mappings = (StaticAssetMapping) bean;
                staticAssetMapping.keySet().removeAll(mappings.getMapping().keySet());
                if (logger.isDebugEnabled()) {
                    logger.debug("Unregistering static asset mappings '" + mappings.getMapping() + "'");
                }
            }
            if (bean instanceof DefaultEventListener) {
                handleJCREventListener(bean, false);
            }
            if (bean instanceof BackgroundAction) {
                BackgroundAction backgroundAction = (BackgroundAction) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Unregistering Background Action '" + backgroundAction.getName() + "' (" + beanName + ")");
                }
                templatePackageRegistry.backgroundActions.remove(backgroundAction.getName());
            }

            if (bean instanceof WorklowTypeRegistration) {
                WorklowTypeRegistration registration = (WorklowTypeRegistration) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug("Unregistering workflow type {}", registration.getType());
                }
                workflowService.unregisterWorkflowType(registration);
            }

            if (bean instanceof VisibilityConditionRule) {
                VisibilityConditionRule conditionRule = (VisibilityConditionRule) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Unregistering Visibility Condition Rule '" + conditionRule.getClass().getName() + "' (" + beanName + ")");
                }
                visibilityService.removeCondition(conditionRule.getAssociatedNodeType());
            }

            if (bean instanceof JCRNodeDecoratorDefinition) {
                JCRNodeDecoratorDefinition jcrNodeDecoratorDefinition = (JCRNodeDecoratorDefinition) bean;
                @SuppressWarnings("rawtypes")
                Map<String, Class> decorators = jcrNodeDecoratorDefinition.getDecorators();
                if (decorators != null) {
                    for (@SuppressWarnings("rawtypes") Map.Entry<String, Class> decorator : decorators.entrySet()) {
                        jcrStoreService.removeDecorator(decorator.getKey());
                    }
                }
            }

            if (bean instanceof JCRNodeValidatorDefinition) {
                JCRNodeValidatorDefinition jcrNodeValidatorDefinition = (JCRNodeValidatorDefinition) bean;
                @SuppressWarnings("rawtypes")
                Map<String, Class> validators = jcrNodeValidatorDefinition.getValidators();
                if (validators != null) {
                    for (@SuppressWarnings("rawtypes") Map.Entry<String, Class> validatorEntry : validators.entrySet()) {
                        jcrStoreService.removeValidator(validatorEntry.getKey());
                    }
                }
            }

            if (bean instanceof CacheKeyPartGenerator) {
                final DefaultCacheKeyGenerator cacheKeyGenerator = (DefaultCacheKeyGenerator) SpringContextSingleton.getBean("cacheKeyGenerator");
                List<CacheKeyPartGenerator> l = new ArrayList<CacheKeyPartGenerator>(cacheKeyGenerator.getPartGenerators());
                l.remove(bean);
                cacheKeyGenerator.setPartGenerators(l);
                CacheHelper.flushOutputCaches();
            }

            if (bean instanceof HandlerMapping) {
                templatePackageRegistry.springHandlerMappings.remove((HandlerMapping) bean);
            }

            if (bean instanceof ProviderFactory) {
                jcrStoreService.removeProviderFactory(((ProviderFactory) bean).getNodeTypeName(), (ProviderFactory) bean);
            }

            if (bean instanceof FactoryBean && bean.getClass().getName().equals("org.springframework.webflow.config.FlowRegistryFactoryBean")) {
                try {
                    @SuppressWarnings("unchecked")
                    FlowDefinitionRegistry flowDefinitionRegistry = ((FactoryBean<FlowDefinitionRegistry>) bean).getObject();
                    ((BundleFlowRegistry) SpringContextSingleton.getBean("jahiaBundleFlowRegistry")).removeFlowRegistry(flowDefinitionRegistry);
                } catch (Exception e) {
                    logger.error("Cannot register webflow registry", e);
                }
            }

            if (bean instanceof AbstractServletFilter) {
                try {
                    compositeFilter.unregisterFilter((AbstractServletFilter) bean);
                } catch (Exception e) {
                    logger.error("Cannot register servlet filter", e);
                }
            }

        }

        @SuppressWarnings("unchecked")
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof RenderServiceAware) {
                ((RenderServiceAware) bean).setRenderService(renderService);
            }
            if (bean instanceof RenderFilter) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Registering RenderFilter '" + beanName + "'");
                }
                templatePackageRegistry.filters.add((RenderFilter) bean);
            }
            if (bean instanceof ErrorHandler) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Registering ErrorHandler '" + beanName + "'");
                }
                templatePackageRegistry.errorHandlers.add((ErrorHandler) bean);
            }
            if (bean instanceof Action) {
                Action action = (Action) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug("Registering Action '" + action.getName() + "' (" + beanName + ")");
                }
                templatePackageRegistry.actions.put(action.getName(), action);
            }
            if (bean instanceof ModuleChoiceListInitializer) {
                ModuleChoiceListInitializer moduleChoiceListInitializer = (ModuleChoiceListInitializer) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug("Registering ModuleChoiceListInitializer '" + moduleChoiceListInitializer.getKey() + "' (" + beanName + ")");
                }
                choiceListInitializers.getInitializers().put(moduleChoiceListInitializer.getKey(), moduleChoiceListInitializer);
            }

            if (bean instanceof ModuleChoiceListRenderer) {
                ModuleChoiceListRenderer choiceListRenderer = (ModuleChoiceListRenderer) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug("Registering ChoiceListRenderer '" + choiceListRenderer.getKey() + "' (" + beanName + ")");
                }
                choiceListRendererService.getRenderers().put(choiceListRenderer.getKey(), choiceListRenderer);
            }
            if (bean instanceof ModuleGlobalObject) {
                ModuleGlobalObject moduleGlobalObject = (ModuleGlobalObject) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug("Registering ModuleGlobalObject '" + beanName + "'");
                }
                if (moduleGlobalObject.getGlobalRulesObject() != null) {
                    for (RulesListener listener : RulesListener.getInstances()) {
                        for (Map.Entry<String, Object> entry : moduleGlobalObject.getGlobalRulesObject().entrySet()) {
                            listener.addGlobalObject(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
            if (bean instanceof StaticAssetMapping) {
                StaticAssetMapping mappings = (StaticAssetMapping) bean;
                staticAssetMapping.putAll(mappings.getMapping());
                if (logger.isDebugEnabled()) {
                    logger.debug("Registering static asset mappings '" + mappings.getMapping() + "'");
                }
            }
            if (bean instanceof DefaultEventListener) {
                handleJCREventListener(bean, true);
            }
            if (bean instanceof BackgroundAction) {
                BackgroundAction backgroundAction = (BackgroundAction) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Registering Background Action '" + backgroundAction.getName() + "' (" + beanName + ")");
                }
                templatePackageRegistry.backgroundActions.put(backgroundAction.getName(), backgroundAction);
            }

            if (bean instanceof WorklowTypeRegistration) {
                WorklowTypeRegistration registration = (WorklowTypeRegistration) bean;
                workflowService.registerWorkflowType(registration);
            }

            if (bean instanceof VisibilityConditionRule) {
                VisibilityConditionRule conditionRule = (VisibilityConditionRule) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Registering Visibility Condition Rule '" + conditionRule.getClass().getName() + "' (" + beanName + ")");
                }
                visibilityService.addCondition(conditionRule.getAssociatedNodeType(), conditionRule);
            }

            if (bean instanceof JCRNodeDecoratorDefinition) {
                JCRNodeDecoratorDefinition jcrNodeDecoratorDefinition = (JCRNodeDecoratorDefinition) bean;
                @SuppressWarnings("rawtypes")
                Map<String, Class> decorators = jcrNodeDecoratorDefinition.getDecorators();
                if (decorators != null) {
                    for (@SuppressWarnings("rawtypes") Map.Entry<String, Class> decorator : decorators.entrySet()) {
                        try {
                            if (!NodeTypeRegistry.getInstance().getNodeType(decorator.getKey()).isMixin()) {
                                jcrStoreService.addDecorator(decorator.getKey(), decorator.getValue());
                            } else {
                                logger.error("It is impossible to decorate a mixin, only primary node type can be decorated");
                            }
                        } catch (NoSuchNodeTypeException e) {
                            logger.error("Cannot register decorator for nodetype " + decorator.getKey() + " has it does not exist in the registry.", e);
                        }
                    }
                }
            }

            if (bean instanceof JCRNodeValidatorDefinition) {
                JCRNodeValidatorDefinition jcrNodeValidatorDefinition = (JCRNodeValidatorDefinition) bean;
                @SuppressWarnings("rawtypes")
                Map<String, Class> validators = jcrNodeValidatorDefinition.getValidators();
                if (validators != null) {
                    for (@SuppressWarnings("rawtypes") Map.Entry<String, Class> validatorEntry : validators.entrySet()) {
                        Class<?> validatorEntryValue = validatorEntry.getValue();
                        try {
                            if (validatorEntryValue.getConstructor(JCRNodeWrapper.class) != null && JCRNodeValidator.class.isAssignableFrom(validatorEntryValue)) {
                                jcrStoreService.addValidator(validatorEntry.getKey(), (Class<? extends JCRNodeValidator>) validatorEntryValue);
                            }
                        } catch (NoSuchMethodException e) {
                            logger.error("Validator must have a constructor taking only a JCRNodeWrapper has a parameter. " +
                                    "Please add " + validatorEntryValue.getSimpleName() + "(JCRNodeWrapper node) has a constructor", e);
                        }
                    }
                }
            }

            if (bean instanceof CacheKeyPartGenerator) {
                final DefaultCacheKeyGenerator cacheKeyGenerator = (DefaultCacheKeyGenerator) SpringContextSingleton.getBean("cacheKeyGenerator");
                List<CacheKeyPartGenerator> l = new ArrayList<CacheKeyPartGenerator>(cacheKeyGenerator.getPartGenerators());
                l.add((CacheKeyPartGenerator) bean);
                cacheKeyGenerator.setPartGenerators(l);
                CacheHelper.flushOutputCaches();
            }

            if (bean instanceof HandlerMapping) {
                templatePackageRegistry.springHandlerMappings.add((HandlerMapping) bean);
                try {
                    logger.info("Map {}", ((SimpleUrlHandlerMapping) bean).getUrlMap());
                } catch (Exception e) {

                }
            }

            if (bean instanceof ProviderFactory) {
                jcrStoreService.addProviderFactory(((ProviderFactory) bean).getNodeTypeName(), (ProviderFactory) bean);
            }

            if (bean instanceof FactoryBean && bean.getClass().getName().equals("org.springframework.webflow.config.FlowRegistryFactoryBean")) {
                try {
                    FlowDefinitionRegistry flowDefinitionRegistry = ((FactoryBean<FlowDefinitionRegistry>) bean).getObject();
                    ((BundleFlowRegistry) SpringContextSingleton.getBean("jahiaBundleFlowRegistry")).addFlowRegistry(flowDefinitionRegistry);
                } catch (Exception e) {
                    logger.error("Cannot register webflow registry", e);
                }
            }

            if (bean instanceof AbstractServletFilter) {
                try {
                    compositeFilter.registerFilter((AbstractServletFilter) bean);
                } catch (Exception e) {
                    logger.error("Cannot register servlet filter", e);
                }
            }

            return bean;
        }

        private void handleJCREventListener(Object bean, final boolean register) {
            final DefaultEventListener eventListener = (DefaultEventListener) bean;
            if (eventListener.getEventTypes() > 0) {
                try {
                    JCRTemplate.getInstance().doExecuteWithSystemSession(null, eventListener.getWorkspace(), new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            final Workspace workspace = session.getWorkspace();

                            ObservationManager observationManager = workspace.getObservationManager();
                            //first remove existing listener of same type
                            final EventListenerIterator registeredEventListeners = observationManager.getRegisteredEventListeners();
                            javax.jcr.observation.EventListener toBeRemoved = null;
                            while (registeredEventListeners.hasNext()) {
                                javax.jcr.observation.EventListener next = registeredEventListeners.nextEventListener();
                                if (next.getClass().equals(eventListener.getClass())) {
                                    toBeRemoved = next;
                                    break;
                                }
                            }
                            observationManager.removeEventListener(toBeRemoved);
                            if (register) {
                                observationManager.addEventListener(eventListener, eventListener.getEventTypes(), eventListener.getPath(), eventListener.isDeep(), eventListener.getUuids(), eventListener.getNodeTypes(), false);
                            }
                            return null;
                        }
                    });
                    if (logger.isDebugEnabled()) {
                        logger.debug((register ? "Registering" : "Unregistering") + " event listener"
                                + eventListener.getClass().getName() + " for workspace '"
                                + eventListener.getWorkspace() + "'");
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                logger.info("Skipping listener {} as it has no event types configured.",
                        eventListener.getClass().getName());
            }
        }

        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        public void setTemplatePackageRegistry(TemplatePackageRegistry templatePackageRegistry) {
            this.templatePackageRegistry = templatePackageRegistry;
        }

        /**
         * @param choiceListInitializers the choiceListInitializers to set
         */
        public void setChoiceListInitializers(ChoiceListInitializerService choiceListInitializers) {
            this.choiceListInitializers = choiceListInitializers;
        }

        public void setChoiceListRendererService(ChoiceListRendererService choiceListRendererService) {
            this.choiceListRendererService = choiceListRendererService;
        }

        /**
         * @param staticAssetMapping the staticAssetMapping to set
         */
        public void setStaticAssetMapping(Map<String, String> staticAssetMapping) {
            this.staticAssetMapping = staticAssetMapping;
        }

        public void setWorkflowService(WorkflowService workflowService) {
            this.workflowService = workflowService;
        }

        public void setVisibilityService(VisibilityService visibilityService) {
            this.visibilityService = visibilityService;
        }

        public void setRenderService(RenderService renderService) {
            this.renderService = renderService;
        }

        public void setJcrStoreService(JCRStoreService jcrStoreService) {
            this.jcrStoreService = jcrStoreService;
        }

        public void setCompositeFilter(CompositeFilter compositeFilter) {
            this.compositeFilter = compositeFilter;
        }
    }
}
