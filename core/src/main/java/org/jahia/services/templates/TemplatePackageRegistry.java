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

package org.jahia.services.templates;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Action;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRNodeDecoratorDefinition;
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
import org.jahia.services.visibility.VisibilityConditionRule;
import org.jahia.services.visibility.VisibilityService;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorklowTypeRegistration;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;

import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Template packages registry service.
 *
 * @author Sergiy Shyrkov
 */
class TemplatePackageRegistry {
// ------------------------------ FIELDS ------------------------------

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TemplatePackageRegistry.class);
    
    private final static String MODULES_ROOT_PATH = "modules.";

    private static final Comparator<JahiaTemplatesPackage> TEMPLATE_PACKAGE_COMPARATOR = new Comparator<JahiaTemplatesPackage>() {
        public int compare(JahiaTemplatesPackage o1, JahiaTemplatesPackage o2) {
            if (o1.isDefault()) return 99;
            if (o2.isDefault()) return -99;
            return o1.getName().compareTo(o2.getName());
        }
    };

    private Map<String, JahiaTemplatesPackage> registry = new TreeMap<String, JahiaTemplatesPackage>();
    private Map<String, JahiaTemplatesPackage> fileNameRegistry = new TreeMap<String, JahiaTemplatesPackage>();
    private List<JahiaTemplatesPackage> templatePackages;
    private Map<String, Map<ModuleVersion,JahiaTemplatesPackage>> packagesWithVersion = new TreeMap<String, Map<ModuleVersion, JahiaTemplatesPackage>>();
    private Map<String, Set<JahiaTemplatesPackage>> packagesPerModule = new HashMap<String, Set<JahiaTemplatesPackage>>();
    private List<RenderFilter> filters = new LinkedList<RenderFilter>();
    private List<ErrorHandler> errorHandlers = new LinkedList<ErrorHandler>();
    private Map<String,Action> actions;
    private Map<String, BackgroundAction> backgroundActions;
    private SettingsBean settingsBean;
    private JCRStoreService jcrStoreService;
    private TemplatePackageApplicationContextLoader templatePackageApplicationContextLoader;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Initializes an instance of this class.
     */
    @SuppressWarnings("unchecked")
    public TemplatePackageRegistry() {
	    super();
	    actions = new CaseInsensitiveMap();
	    backgroundActions = new CaseInsensitiveMap();
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public Map<String, Action> getActions() {
        return actions;
    }

    public Map<String, BackgroundAction> getBackgroundActions() {
        return backgroundActions;
    }

    /**
     * Returns a list of {@link ErrorHandler} instances
     *
     * @return a list of {@link ErrorHandler} instances
     */
    public List<ErrorHandler> getErrorHandlers() {
        return errorHandlers;
    }

    public Map<String, Set<JahiaTemplatesPackage>> getPackagesPerModule() {
        return packagesPerModule;
    }

    public void setJcrStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    public void setTemplatePackageApplicationContextLoader(TemplatePackageApplicationContextLoader templatePackageApplicationContextLoader) {
        this.templatePackageApplicationContextLoader = templatePackageApplicationContextLoader;
    }

// -------------------------- OTHER METHODS --------------------------

    public JahiaTemplatesPackage activateModuleVersion(String rootFolder, ModuleVersion version) {
        JahiaTemplatesPackage previousPack = fileNameRegistry.get(rootFolder);
        JahiaTemplatesPackage newPack = packagesWithVersion.get(rootFolder).get(version);

        previousPack.setActiveVersion(false);
        if (previousPack.getContext() != null) {
            previousPack.getContext().close();
            previousPack.setContext(null);
        }

        File rootFile = new File(newPack.getFilePath()).getParentFile();
        File activeVersionFile = new File(rootFile, "activeVersion");

        try {
            FileUtils.write(activeVersionFile, newPack.getVersion().toString(), "UTF-8");
        } catch (IOException e) {
            logger.error("Cannot store active version file "+activeVersionFile, e);
        }
        newPack.setActiveVersion(true);

        register(newPack);

        templatePackageApplicationContextLoader.createWebApplicationContext(newPack);
        afterInitializationForModule(newPack);

        return newPack;
    }

    private void computeDependencies(Set<JahiaTemplatesPackage> dependencies,  JahiaTemplatesPackage pack) {
        for (String depends : pack.getDepends()) {
            JahiaTemplatesPackage dependentPack = registry.get(depends);
            if (dependentPack == null) {
                dependentPack = fileNameRegistry.get(depends);
            }
            if (!dependencies.contains(dependentPack)) {
                dependencies.add(dependentPack);
                computeDependencies(dependencies, dependentPack);
            }
        }
    }

    public void afterInitializationForModule(JahiaTemplatesPackage pack) {
        Map map = pack.getContext().getBeansOfType(JahiaAfterInitializationService.class);
        for (Object o : map.values()) {
            JahiaAfterInitializationService initializationService = (JahiaAfterInitializationService) o;
            try {
                initializationService.initAfterAllServicesAreStarted();
            } catch (JahiaInitializationException e) {
                logger.error(e.getMessage(), e);
            }
        }
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

    public Set<ModuleVersion> getAvailableVersionsForModule(String rootFolder) {
        return Collections.unmodifiableSet(packagesWithVersion.get(rootFolder).keySet());
    }

    public Set<String> getPackageFileNames() {
        return fileNameRegistry.keySet();
    }

    public Set<String> getPackageNames() {
        return registry.keySet();
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
        return registry.get(packageName);
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
     * Returns the requested template package or <code>null</code> if the package with the specified JCR node name is not registered in the
     * repository.
     * 
     * @param nodeName
     *            the corresponding JCR node name to search for
     * @return the requested template package or <code>null</code> if the package with the specified JCR node name is not registered in the
     *         repository
     */
    public JahiaTemplatesPackage lookupByNodeName(String nodeName) {
        if (nodeName == null || registry == null)
            return null;
        for (JahiaTemplatesPackage pkg : registry.values()) {
            if (nodeName.equals(pkg.getRootFolder())) {
                return pkg;
            }
        }
        return null;
    }

    public void registerPackage(JahiaTemplatesPackage pack) {
        try {
            if (!packagesWithVersion.containsKey(pack.getRootFolder())) {
                packagesWithVersion.put(pack.getRootFolder(), new HashMap<ModuleVersion, JahiaTemplatesPackage>());
            }
            packagesWithVersion.get(pack.getRootFolder()).put(pack.getVersion(), pack);

            File rootFile = new File(pack.getFilePath()).getParentFile();
            File activeVersionFile = new File(rootFile, "activeVersion");
            if (!activeVersionFile.exists()) {
                FileUtils.write(activeVersionFile, pack.getVersion().toString(), "UTF-8");
                pack.setActiveVersion(true);
            } else {
                String activeVersion = FileUtils.readFileToString(activeVersionFile);
                if (pack.getVersion().toString().equals(activeVersion)) {
                    pack.setActiveVersion(true);
                }
            }

            File lastVersionFile = new File(rootFile, "lastVersion");
            if (!lastVersionFile.exists()) {
                FileUtils.write(lastVersionFile, pack.getVersion().toString(), "UTF-8");
                pack.setLastVersion(true);
            } else {
                String lastVersion = FileUtils.readFileToString(lastVersionFile, "UTF-8");
                if (new ModuleVersion(lastVersion).compareTo(pack.getVersion()) <= 0) {
                    FileUtils.write(lastVersionFile, pack.getVersion().toString(), "UTF-8");
                    pack.setLastVersion(true);
                } else if (pack.getVersion().toString().equals(lastVersion)) {
                    pack.setLastVersion(true);
                }
            }

            if (pack.isLastVersion()) {
                registerDefinitions(pack);
            }

            if (pack.isActiveVersion()) {
                register(pack);
            }
        } catch (IOException e) {
            logger.error("Cannot get active versions of module " + pack.getRootFolder(),e);
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
        fileNameRegistry.put(templatePackage.getRootFolder(), templatePackage);
        File rootFolder = new File(templatePackage.getFilePath());

        // handle dependencies
        for (JahiaTemplatesPackage pack : registry.values()) {
            computeDependencies(pack);
        }

        // handle resource bundles
//        for (JahiaTemplatesPackage sourcePack : registry.values()) {
        computeResourceBundleHierarchy(templatePackage);
//        }
        
        File[] files = rootFolder.listFiles();
        for (File file : files) {
            if (file.isDirectory() && file.getName().contains("_")) {
                String key = file.getName();
                if (!packagesPerModule.containsKey(key)) {
                    packagesPerModule.put(key, new TreeSet<JahiaTemplatesPackage>(TEMPLATE_PACKAGE_COMPARATOR));
                }
                packagesPerModule.get(key).remove(templatePackage);
                packagesPerModule.get(key).add(templatePackage);
            }
        }
        logger.info("Registered "+templatePackage.getName() + " version=" + templatePackage.getVersion());
    }

    private void computeResourceBundleHierarchy(JahiaTemplatesPackage templatePackage) {
        templatePackage.getResourceBundleHierarchy().clear();
        if (templatePackage.getResourceBundleName() != null) {
            templatePackage.getResourceBundleHierarchy().add(MODULES_ROOT_PATH + templatePackage.getRootFolder() + "." + templatePackage.getVersion().toString().replaceAll("\\.","___") + "." + templatePackage.getResourceBundleName());
        }
        for (JahiaTemplatesPackage dependency : templatePackage.getDependencies()) {
            if (!dependency.isDefault() && dependency.getResourceBundleName() != null) {
                templatePackage.getResourceBundleHierarchy().add(MODULES_ROOT_PATH + dependency.getRootFolder() + "." + dependency.getVersion().toString().replaceAll("\\.","___") + "." + dependency.getResourceBundleName());
            }
        }
        if (!templatePackage.isDefault() && fileNameRegistry.containsKey("default")) {
            	templatePackage.getResourceBundleHierarchy().add(MODULES_ROOT_PATH + fileNameRegistry.get("default").getVersion().toString().replaceAll("\\.","___") + "." + "default.resources.DefaultJahiaTemplates");
            templatePackage.getResourceBundleHierarchy().add("JahiaTypesResources");
            templatePackage.getResourceBundleHierarchy().add("JahiaInternalResources");
        } else {
            templatePackage.getResourceBundleHierarchy().add("JahiaTypesResources");
            templatePackage.getResourceBundleHierarchy().add("JahiaInternalResources");
        }
    }

    public void computeDependencies(JahiaTemplatesPackage pack) {
        pack.getDependencies().clear();
        computeDependencies(pack.getDependencies(), pack);
    }

    public void registerDefinitions(JahiaTemplatesPackage templatePackage) {
        File rootFolder = new File(templatePackage.getFilePath());
        if (!templatePackage.getDefinitionsFiles().isEmpty()) {
            try {
                for (String name : templatePackage.getDefinitionsFiles()) {
                    NodeTypeRegistry.getInstance().addDefinitionsFile(
                            new File(rootFolder, name),
                            templatePackage.getName());
                }
                jcrStoreService.deployDefinitions(templatePackage.getName());
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
                        List<String> filesAccepted = listener.getFilesAccepted();
                        if(filesAccepted.contains(StringUtils.substringAfterLast(name, "/"))) {
                            listener.addRules(new File(rootFolder, name));
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Cannot parse rules for "+templatePackage.getName(),e);
            }
        }
    }

    public void reset() {
        for (JahiaTemplatesPackage pkg : new HashSet<JahiaTemplatesPackage>(registry.values())) {
            unregister(pkg);
        }
        templatePackages = null;
    }

    public void unregister(JahiaTemplatesPackage templatePackage) {
        registry.remove(templatePackage.getName());
        fileNameRegistry.remove(templatePackage.getRootFolder());
        templatePackages = null;
        NodeTypeRegistry.getInstance().unregisterNodeTypes(templatePackage.getName());
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

        public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
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
                if(moduleGlobalObject.getGlobalRulesObject()!=null) {
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
                final DefaultEventListener eventListener = (DefaultEventListener) bean;
                if (eventListener.getEventTypes() > 0) {
	                try {
	                    JCRTemplate.getInstance().doExecuteWithSystemSession(null,eventListener.getWorkspace(),new JCRCallback<Object>() {
	                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
	                            final Workspace workspace = session.getWorkspace();

	                            ObservationManager observationManager = workspace.getObservationManager();
                                //first remove existing listener of same type
                                final EventListenerIterator registeredEventListeners = observationManager.getRegisteredEventListeners();
                                javax.jcr.observation.EventListener toBeRemoved = null;
                                while (registeredEventListeners.hasNext()) {
                                    javax.jcr.observation.EventListener next = registeredEventListeners.nextEventListener();
                                    if(next.getClass().equals(eventListener.getClass())) {
                                        toBeRemoved = next;
                                        break;
                                    }
                                }
                                observationManager.removeEventListener(toBeRemoved);
	                            return null;
	                        }
	                    });
	                    if (logger.isDebugEnabled()) {
	                        logger.debug("Unregistering event listener"+eventListener.getClass().getName()+" for workspace '" + eventListener.getWorkspace() + "'");
	                    }
	                } catch (RepositoryException e) {
	                    logger.error(e.getMessage(), e);
	                }
                }
            }
            if (bean instanceof BackgroundAction) {
                BackgroundAction backgroundAction = (BackgroundAction) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Unregistering Background Action '" + backgroundAction.getName() + "' (" + beanName + ")");
                }
                templatePackageRegistry.backgroundActions.remove(backgroundAction.getName());
            }

            if(bean instanceof WorklowTypeRegistration) {
                WorklowTypeRegistration registration = (WorklowTypeRegistration) bean;
                workflowService.unregisterWorkflowType(registration.getType(), registration.getDefinition());
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
                Map<String, String> decorators = jcrNodeDecoratorDefinition.getDecorators();
                if (decorators != null) {
                    for (Map.Entry<String, String> decorator : decorators.entrySet()) {
                        jcrStoreService.removeDecorator(decorator.getKey());
                    }
                }
            }
        }

        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof SharedService) {
                ((ConfigurableApplicationContext)SpringContextSingleton.getInstance().getContext()).getBeanFactory().registerSingleton(beanName, bean);
            }
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
                choiceListInitializers.getInitializers().put(moduleChoiceListInitializer.getKey(),moduleChoiceListInitializer);
            }
            
            if (bean instanceof ModuleChoiceListRenderer) {
                ModuleChoiceListRenderer choiceListRenderer = (ModuleChoiceListRenderer) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug("Registering ChoiceListRenderer '" + choiceListRenderer.getKey() + "' (" + beanName + ")");
                }
                choiceListRendererService.getRenderers().put(choiceListRenderer.getKey(),choiceListRenderer);
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
                if (eventListener.getEventTypes() > 0) {
	                try {
	                    JCRTemplate.getInstance().doExecuteWithSystemSession(null,eventListener.getWorkspace(),new JCRCallback<Object>() {
	                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
	                            final Workspace workspace = session.getWorkspace();
	
	                            ObservationManager observationManager = workspace.getObservationManager();
                                //first remove existing listener of same type
                                final EventListenerIterator registeredEventListeners = observationManager.getRegisteredEventListeners();
                                javax.jcr.observation.EventListener toBeRemoved = null;
                                while (registeredEventListeners.hasNext()) {
                                    javax.jcr.observation.EventListener next = registeredEventListeners.nextEventListener();
                                    if(next.getClass().equals(eventListener.getClass())) {
                                        toBeRemoved = next;
                                        break;
                                    }
                                }
                                observationManager.removeEventListener(toBeRemoved);
                                observationManager.addEventListener(eventListener, eventListener.getEventTypes(), eventListener.getPath(), eventListener.isDeep(), eventListener.getUuids(), eventListener.getNodeTypes(), false);
	                            return null;
	                        }
	                    });
	                    if (logger.isDebugEnabled()) {
	                        logger.debug("Registering event listener"+eventListener.getClass().getName()+" for workspace '" + eventListener.getWorkspace() + "'");
	                    }
	                } catch (RepositoryException e) {
	                    logger.error(e.getMessage(), e);
	                }
                } else {
                	logger.info("Skipping listener {} as it has no event types configured.",
					        eventListener.getClass().getName());                	
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

            if(bean instanceof WorklowTypeRegistration) {
                WorklowTypeRegistration registration = (WorklowTypeRegistration) bean;
                workflowService.registerWorkflowType(registration.getType(), registration.getDefinition(), registration.getPermissions());
            }

            if (bean instanceof VisibilityConditionRule) {
                VisibilityConditionRule conditionRule = (VisibilityConditionRule) bean;
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Registering Visibility Condition Rule '" + conditionRule.getClass().getName() + "' (" + beanName + ")");
                }
                visibilityService.addCondition(conditionRule.getAssociatedNodeType(),conditionRule);
            }

            if (bean instanceof JCRNodeDecoratorDefinition) {
                JCRNodeDecoratorDefinition jcrNodeDecoratorDefinition = (JCRNodeDecoratorDefinition) bean;
                Map<String, String> decorators = jcrNodeDecoratorDefinition.getDecorators();
                if (decorators != null) {
                    for (Map.Entry<String, String> decorator : decorators.entrySet()) {
                        jcrStoreService.addDecorator(decorator.getKey(), decorator.getValue());
                    }
                }
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
    }
}