/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.templates;

import org.slf4j.Logger;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializerService;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.jahia.services.content.rules.BackgroundAction;
import org.jahia.services.content.rules.ModuleGlobalObject;
import org.jahia.services.content.rules.RulesListener;
import org.jahia.services.render.StaticAssetMapping;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.settings.SettingsBean;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.bin.Action;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.observation.ObservationManager;
import java.io.File;
import java.util.*;

/**
 * Template packages registry service.
 *
 * @author Sergiy Shyrkov
 */
class TemplatePackageRegistry {

	private static Logger logger = org.slf4j.LoggerFactory.getLogger(TemplatePackageRegistry.class);
    
    private final static String MODULES_ROOT_PATH = "modules.";

    static class ModuleRegistry implements BeanPostProcessor {

        private TemplatePackageRegistry templatePackageRegistry;
        
        private ChoiceListInitializerService choiceListInitializers;
        
        private Map<String, String> staticAssetMapping;

        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
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
                choiceListInitializers.getInitializers().put(moduleChoiceListInitializer.getKey(),moduleChoiceListInitializer);
            }
            if (bean instanceof ModuleGlobalObject) {
                ModuleGlobalObject moduleGlobalObject = (ModuleGlobalObject) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug("Registering ModuleGlobalObject '" + beanName + "'");
                }
                if(moduleGlobalObject.getGlobalRulesObject()!=null) {
                    for (RulesListener listener : RulesListener.getInstances()) {
                        for (Map.Entry<String, Object> entry : moduleGlobalObject.getGlobalRulesObject().entrySet()) {
                            listener.addGlobalObject(entry.getKey(),entry.getValue());
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
                final DefaultEventListener eventListener = (DefaultEventListener) bean;
                try {
                    JCRTemplate.getInstance().doExecuteWithSystemSession(null,eventListener.getWorkspace(),new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            final Workspace workspace = session.getWorkspace();

                            ObservationManager observationManager = workspace.getObservationManager();
                            observationManager.addEventListener(eventListener, eventListener.getEventTypes(), eventListener.getPath(), true, null, eventListener.getNodeTypes(), false);
                            return null;
                        }
                    });
                    if (logger.isDebugEnabled()) {
                        logger.debug("Registering event listener"+eventListener.getClass().getName()+" for workspace '" + eventListener.getWorkspace() + "'");
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (bean instanceof BackgroundAction) {
                BackgroundAction backgroundAction = (BackgroundAction) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Registering Background Action '" + backgroundAction.getName() + "' (" + beanName + ")");
                }
                templatePackageRegistry.backgroundActions.put(backgroundAction.getName(), backgroundAction);
            }
            return bean;
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

        /**
         * @param staticAssetMapping the staticAssetMapping to set
         */
        public void setStaticAssetMapping(Map<String, String> staticAssetMapping) {
            this.staticAssetMapping = staticAssetMapping;
        }
    }

    private Map<String, JahiaTemplatesPackage> registry = new TreeMap<String, JahiaTemplatesPackage>();
    private Map<String, JahiaTemplatesPackage> fileNameRegistry = new TreeMap<String, JahiaTemplatesPackage>();
    private Map<String, List<JahiaTemplatesPackage>> packagesPerModule = new HashMap<String, List<JahiaTemplatesPackage>>();
    private List<RenderFilter> filters = new LinkedList<RenderFilter>();
    private List<ErrorHandler> errorHandlers = new LinkedList<ErrorHandler>();
    private Map<String,Action> actions;
    private Map<String, BackgroundAction> backgroundActions = new HashMap<String,BackgroundAction>();
    private SettingsBean settingsBean;

    private List<JahiaTemplatesPackage> templatePackages;

    /**
     * Initializes an instance of this class.
     */
    @SuppressWarnings("unchecked")
    public TemplatePackageRegistry() {
	    super();
	    actions = new CaseInsensitiveMap();
    }

    /**
     * Checks if the specified template set is present in the repository.
     *
     * @param packageName the template package name to check for
     * @return <code>true</code>, if the specified template package already
     *         exists in the repository
     */
    public boolean contains(String packageName) {
        return registry.containsKey(packageName);
    }

    /**
     * Checks if specified template sets are all present in the repository.
     *
     * @param packageNames the list of template package names to check for
     * @return <code>true</code>, if specified template packages are all present
     * 		   in the repository
     */
    public boolean containsAll(List<String> packageNames) {
    	return registry.keySet().containsAll(packageNames);
    }

    public boolean containsFileName(String fileName) {
        return fileNameRegistry.containsKey(fileName);
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
                    registry.values()));
        }
        return templatePackages;
    }

    /**
     * Returns the number of available template packages in the registry.
     *
     * @return the number of available template packages in the registry
     */
    public int getAvailablePackagesCount() {
        return registry.size();
    }

    public Map<String, List<JahiaTemplatesPackage>> getPackagesPerModule() {
        return packagesPerModule;
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
     * Returns a list of {@link ErrorHandler} instances
     *
     * @return a list of {@link ErrorHandler} instances
     */
    public List<ErrorHandler> getErrorHandlers() {
        return errorHandlers;
    }

    public Map<String, Action> getActions() {
        return actions;
    }

    /**
     * Returns the requested template package or <code>null</code> if the
     * package with the specified name is not registered in the repository.
     *
     * @param packageName the template package name to search for
     * @return the requested template package or <code>null</code> if the
     *         package with the specified name is not registered in the
     *         repository
     */
    public JahiaTemplatesPackage lookup(String packageName) {
        if (packageName == null || registry == null) return null;
        return registry.containsKey(packageName) ? registry.get(packageName)
                : null;
    }

    /**
     * Returns the requested template package or <code>null</code> if the
     * package with the specified file name is not registered in the repository.
     *
     * @param fileName the template package fileName to search for
     * @return the requested template package or <code>null</code> if the
     *         package with the specified name is not registered in the
     *         repository
     */
    public JahiaTemplatesPackage lookupByFileName(String fileName) {
        if (fileName == null || registry == null) return null;
        return fileNameRegistry.containsKey(fileName) ? fileNameRegistry.get(fileName)
                : null;
    }

    /**
     * Adds a collection of template packages to the repository.
     *
     * @param templatePackages a collection of packages to add
     */
    public void register(Collection<JahiaTemplatesPackage> templatePackages) {
        for (JahiaTemplatesPackage pack : templatePackages) {
            register(pack);
        }
    }
    
    /**
     * Adds the template package to the repository.
     *
     * @param templatePackage the template package to add
     */
    public void register(JahiaTemplatesPackage templatePackage) {
        templatePackages = null;
        registry.put(templatePackage.getName(), templatePackage);
        fileNameRegistry.put(templatePackage.getFileName(), templatePackage);
        File rootFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), templatePackage.getRootFolder());
        if (!rootFolder.exists()) {
            rootFolder = new File(templatePackage.getFilePath());
        }

        // register content definitions
        if (!templatePackage.getDefinitionsFiles().isEmpty()) {
            try {
                for (String name : templatePackage.getDefinitionsFiles()) {
                    NodeTypeRegistry.getInstance().addDefinitionsFile(
                            new File(rootFolder, name),
                            templatePackage.getName());
                }
                JCRStoreService.getInstance().deployDefinitions(templatePackage.getName());
            } catch (Exception e) {
                logger.warn("Cannot parse definitions for "+templatePackage.getName(),e);
            }
        }
        // add rules descriptor
        if (!templatePackage.getRulesDescriptorFiles().isEmpty()) {
            try {
                for (String name : templatePackage.getRulesDescriptorFiles()) {
                    for (RulesListener listener : RulesListener.getInstances()) {
                        listener.addRulesDescriptor(new File(rootFolder, name));
                    }
                }
            } catch (Exception e) {
                logger.warn("Cannot parse rules for "+templatePackage.getName(),e);
            }
        }
        // add rules
        if (!templatePackage.getRulesFiles().isEmpty()) {
            try {
                for (String name : templatePackage.getRulesFiles()) {
                    for (RulesListener listener : RulesListener.getInstances()) {
                        listener.addRules(new File(rootFolder, name));
                    }
                }
            } catch (Exception e) {
                logger.warn("Cannot parse rules for "+templatePackage.getName(),e);
            }
        }

        // handle resource bundles
        for (JahiaTemplatesPackage sourcePack : registry.values()) {
        	sourcePack.getResourceBundleHierarchy().clear();
            sourcePack.getResourceBundleHierarchy().add(MODULES_ROOT_PATH + sourcePack.getRootFolder() + "." + sourcePack.getResourceBundleName());
            for (String s : sourcePack.getDepends()) {
                sourcePack.getResourceBundleHierarchy().add(MODULES_ROOT_PATH + lookup(s).getRootFolder() + "." + sourcePack.getResourceBundleName());
            }
            if (!sourcePack.isDefault()) {
            	sourcePack.getResourceBundleHierarchy().add(MODULES_ROOT_PATH + "default.resources.DefaultJahiaTemplates");
            	sourcePack.getResourceBundleHierarchy().add("JahiaTypesResources");
                sourcePack.getResourceBundleHierarchy().add("JahiaInternalResources");
            } else {
                sourcePack.getResourceBundleHierarchy().add("JahiaTypesResources");
                sourcePack.getResourceBundleHierarchy().add("JahiaInternalResources");
            }
        }
        
        // handle dependencies
        for (JahiaTemplatesPackage pack : registry.values()) {
            pack.getDependencies().clear();
            computeDependencies(pack.getDependencies(), pack);
        }        
        
        File[] files = rootFolder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                String key = file.getName();
                if (!packagesPerModule.containsKey(key)) {
                    packagesPerModule.put(key, new ArrayList<JahiaTemplatesPackage>());
                }
                if (!packagesPerModule.get(key).contains(templatePackage)) {
                    packagesPerModule.get(key).add(templatePackage);
                }
            }
        }
        logger.info("Registered "+templatePackage.getName());
    }

	public void unregister(JahiaTemplatesPackage templatePackage) {
        registry.remove(templatePackage.getName());
        fileNameRegistry.remove(templatePackage.getFileName());
        templatePackages = null;
        NodeTypeRegistry.getInstance().unregisterNodeTypes(templatePackage.getName());
    }

    public void reset() {
        for (JahiaTemplatesPackage pkg : new HashSet<JahiaTemplatesPackage>(registry.values())) {
            unregister(pkg);
        }
        templatePackages = null;
    }

    public void resetBeanModules() {
        filters.clear();
        errorHandlers.clear();
        actions.clear();
        backgroundActions.clear();
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    /**
     * Performs a set of validation tests for deployed template packages.
     */
    public void validate() {
        if (getAvailablePackagesCount() == 0) {
            logger.warn("No available template packages found."
                    + " That will prevent creation of a virtual site.");
        }
        // TODO implement dependency validation for template sets
    }

    public Map<String, BackgroundAction> getBackgroundActions() {
        return backgroundActions;
    }

    private void computeDependencies(Set<JahiaTemplatesPackage> dependencies,  JahiaTemplatesPackage pack) {
        for (String depends : pack.getDepends()) {
            JahiaTemplatesPackage dependentPack = registry.get(depends);
            if (!dependencies.contains(dependentPack)) {
                dependencies.add(dependentPack);
                computeDependencies(dependencies, dependentPack);
            }
        }
    }
}