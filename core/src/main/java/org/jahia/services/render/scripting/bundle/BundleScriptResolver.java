/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 * http://www.jahia.com
 *
 * Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 *
 * 1/ GPL
 * ==================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ===================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render.scripting.bundle;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.ExtensionObserverRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.*;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.render.scripting.ScriptFactory;
import org.jahia.services.render.scripting.ScriptResolver;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.JahiaTemplateManagerService.ModuleDependenciesEvent;
import org.jahia.services.templates.JahiaTemplateManagerService.ModuleDeployedOnSiteEvent;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.script.ScriptEngineFactory;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BundleScriptResolver implements ScriptResolver, ApplicationListener<ApplicationEvent> {

    private static final int PRIORITY_STAGGER_FACTOR = 100;
    private static final String EXTENSION_PATTERN_PREFIX = "*.";
    private static Logger logger = LoggerFactory.getLogger(BundleScriptResolver.class);

    private static Map<String, SortedSet<View>> viewSetCache = new ConcurrentHashMap<>(512);

    private Map<String, SortedMap<String, ViewResourceInfo>> availableScripts = new HashMap<>(64);

    private LinkedHashMap<String, ScriptFactory> scriptFactoryMap;
    private Set<String> preRegisteredExtensions;

    private HashMap<String, Integer> extensionPriorities;
    private JahiaTemplateManagerService templateManagerService;
    private final Comparator<ViewResourceInfo> scriptExtensionComparator = new Comparator<ViewResourceInfo>() {
        public int compare(ViewResourceInfo o1, ViewResourceInfo o2) {
            if (Objects.equals(o1, o2)) {
                return 1;
            }

            final Integer o1Priority = extensionPriorities.get(o1.extension);
            final Integer o2Priority = extensionPriorities.get(o2.extension);
            if (o1Priority == null) {
                throw new IllegalArgumentException("Unknown extension " + o1.extension);
            }
            if (o2Priority == null) {
                throw new IllegalArgumentException("Unknown extension " + o2.extension);
            }
            final int i = o1Priority - o2Priority;
            return i != 0 ? i : 1; // not sure why returning 1 is needed when i == 0 here but not doing so seems to break :(
        }
    };
    private BundleJSR223ScriptFactory bundleScriptFactory;
    private ExtensionObserverRegistry observerRegistry;
    private final ScriptBundleObserver scriptBundleObserver = new ScriptBundleObserver(this);
    /**
     * Prefixes for bundles that are excluded from bundle scanning for views since they contain lots of files with registered extensions that would be considered as views.
     */
    private Set<String> ignoredBundlePrefixes = new HashSet<>(7);

    public void setIgnoredBundlePrefixes(Set<String> ignoredBundlePrefixes) {
        this.ignoredBundlePrefixes = ignoredBundlePrefixes;
    }

    public void setExtensionObserverRegistry(ExtensionObserverRegistry extensionObserverRegistry) {
        this.observerRegistry = extensionObserverRegistry;
    }

    public ExtensionObserverRegistry getObserverRegistry() {
        return observerRegistry;
    }

    public void registerObservers() {
        // add scanners for all types of scripts of the views to register them in the BundleScriptResolver
        registerObservers(scriptFactoryMap.keySet());
    }

    private void registerObserver(String extension) {
        observerRegistry.put(new ScriptBundleURLScanner("/", extension, true), scriptBundleObserver);
    }

    public void registerObservers(Iterable<String> extensions) {
        // add scanners for all types of scripts of the views to register them in the BundleScriptResolver
        for (String scriptExtension : extensions) {
            registerObserver(scriptExtension);
        }
    }

    public ScriptBundleObserver getBundleObserver() {
        return scriptBundleObserver;
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final BundleScriptResolver INSTANCE = new BundleScriptResolver();

        private Holder() {
        }
    }

    public static BundleScriptResolver getInstance() {
        return Holder.INSTANCE;
    }

    public void setScriptFactoryMap(Map<String, ScriptFactory> scriptFactoryMap) {
        if (!(scriptFactoryMap instanceof LinkedHashMap)) {
            throw new IllegalArgumentException("Error instantiating BundleScriptResolver: Spring is supposed to create a SortedMap when using a <map> property but didn't. Was: "
                    + scriptFactoryMap.getClass().getName());
        }
        this.scriptFactoryMap = (LinkedHashMap<String, ScriptFactory>) scriptFactoryMap;

        // record pre-registered extensions
        preRegisteredExtensions = new HashSet<>(scriptFactoryMap.size());
        preRegisteredExtensions.addAll(scriptFactoryMap.keySet());

        // record priorities of extensions, each pre-registered extension is assigned a priority of its registration index times 100
        extensionPriorities = new HashMap<>(scriptFactoryMap.size());
        int i = 0;
        for (String extension : scriptFactoryMap.keySet()) {
            extensionPriorities.put(extension, PRIORITY_STAGGER_FACTOR * i);
            i++;
        }
    }

    public void register(ScriptEngineFactory scriptEngineFactory, Bundle bundle) {
        final List<String> extensions = scriptEngineFactory.getExtensions();

        if (extensions.isEmpty()) {
            return;
        }

        // determine if the bundle provided any scripting-related information
        final BundleScriptingContext context;
        if (scriptEngineFactory instanceof BundleScriptEngineFactory) {
            BundleScriptEngineFactory engineFactory = (BundleScriptEngineFactory) scriptEngineFactory;
            context = engineFactory.getContext();
        } else {
            context = null;
        }

        for (String extension : extensions) {
            // first check that we don't already have a script factory assigned to that extension
            final ScriptFactory scriptFactory = scriptFactoryMap.get(extension);
            if (scriptFactory != null) {
                // todo: do something different here?
                throw new IllegalArgumentException("Extension " + extension + " is already associated with ScriptEngineFactory " + scriptEngineFactory);
            }

            scriptFactoryMap.put(extension, bundleScriptFactory);

            // compute or retrieve the extensions priority and record it
            final int priority = getPriorityFor(extension, context);
            extensionPriorities.put(extension, priority);

            logger.info("ScriptEngineFactory {} registered extension {} with priority {}", new Object[]{scriptEngineFactory, extension, priority});

            // now we need to activate the bundle script scanner inside of newly deployed or existing bundles
            // register view script observers
            addBundleScripts(bundle, extension, scriptEngineFactory);

            // check existing bundles to see if they provide views for the newly deployed scripting language
            final BundleContext bundleContext = bundle.getBundleContext();
            if (bundleContext != null) {
                for (Bundle otherBundle : bundleContext.getBundles()) {
                    if (otherBundle.getState() == Bundle.ACTIVE) {
                        addBundleScripts(otherBundle, extension, scriptEngineFactory);
                    }
                }
            }
        }

        // deal with extension priorities if needed
        if (context != null && context.specifiesExtensionPriorities()) {
            final Map<String, Integer> specifiedPriorities = context.getExtensionPriorities();
            final SortedMap<Integer, String> orderedPriorities = new TreeMap<>();

            for (Map.Entry<String, Integer> entry : extensionPriorities.entrySet()) {
                final String extension = entry.getKey();
                Integer priority = entry.getValue();

                final Integer newPriority = specifiedPriorities.get(extension);
                if (newPriority != null) {
                    extensionPriorities.put(extension, newPriority);
                    priority = newPriority;
                }
                orderedPriorities.put(priority, extension);
            }

            //check if we specified unknown extensions
            final Set<String> specifiedExtensions = specifiedPriorities.keySet();
            specifiedExtensions.removeAll(extensionPriorities.keySet());
            if (!specifiedExtensions.isEmpty()) {
                logger.warn("Module {} specified priorities for unknown extensions {}", bundle.getSymbolicName(), specifiedExtensions);
            }

            logger.info("Extension priorities got re-ordered by module {} to {}", bundle.getSymbolicName(), orderedPriorities);
        }

        // add observers for the extensions
        registerObservers(extensions);
    }

    private int getPriorityFor(String extension, BundleScriptingContext context) {
        final int defaultPriority = extensionPriorities.size() * PRIORITY_STAGGER_FACTOR;
        if (context == null) {
            return defaultPriority;
        } else {
            return context.getPriorityFor(extension, defaultPriority);
        }
    }

    public void remove(ScriptEngineFactory factory, Bundle bundle) {
        for (String extension : factory.getExtensions()) {
            // we need to remove the views associated with our bundle
            availableScripts.remove(bundle.getSymbolicName());

            // remove all the bundle scripts for all the deployed bundles.
            for (Bundle otherBundle : bundle.getBundleContext().getBundles()) {
                if (otherBundle.getState() == Bundle.ACTIVE) {
                    removeBundleScripts(otherBundle, extension);
                }
            }

            scriptFactoryMap.remove(extension);
            extensionPriorities.remove(extension);
        }
    }

    /**
     * Whether or not to scan the specified bundle for views with the specified extension.
     *
     * @param bundle the bundle to possibly scan
     * @param viewExtension the extension for views we're looking for in bundles
     * @return {@code true} if the specified bundle should be scanned for views with the specified extension, {@code false} otherwise
     */
    static boolean shouldNotBeScannedForViews(Bundle bundle, String viewExtension) {
        if (isIgnoredBundle(bundle)) {
            return true;
        } else if (isPreRegisteredExtension(viewExtension)) {
            // if the extension is one of the pre-registered ones (via Spring configuration), we should scan the bundle
            return false;
        } else {
            final ScriptEngineFactory scriptFactory = BundleScriptEngineManager.getInstance().getFactoryForExtension(viewExtension);
            if (scriptFactory == null) {
                // we don't have a ScriptEngineFactory associated with this extension so no need to scan
                return true;
            } else {
                // check headers for view markers
                final Dictionary<String, String> headers = bundle.getHeaders();
                final String hasViews = headers.get("Jahia-Module-Has-Views");
                if ("no".equalsIgnoreCase(StringUtils.trim(hasViews))) {
                    // if the bundle indicated that it doesn't provide views, no need to scan
                    return true;
                } else {
                    // check if the bundle provided a list of of comma-separated scripting language names for the views it provides
                    final String commaSeparatedScriptNames = headers.get("Jahia-Module-Scripting-Views");
                    final String[] split = StringUtils.split(commaSeparatedScriptNames, ',');
                    if (split != null) {
                        List<String> result = new ArrayList<>(split.length);
                        for (String name : split) {
                            result.add(name.trim().toLowerCase());
                        }

                        // the bundle should only be scanned if it defined the header and the header contains the name or language of the factory associated with the extension
                        return !doesFactorySupport(scriptFactory, result);
                    } else {
                        return true;
                    }
                }

            }
        }
    }

    private static boolean doesFactorySupport(ScriptEngineFactory scriptFactory, List<String> scriptNames) {
        final boolean nameOrLanguage = scriptNames.contains(scriptFactory.getEngineName().toLowerCase()) ||
                scriptNames.contains(scriptFactory.getLanguageName().toLowerCase());
        if (!nameOrLanguage) {
            // check extensions
            final List<String> extensions = scriptFactory.getExtensions();
            for (String scriptName : scriptNames) {
                if (extensions.contains(scriptName)) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    private static boolean isIgnoredBundle(Bundle bundle) {
        final String symbolicName = bundle.getSymbolicName();
        for (String ignoredBundlePrefix : getInstance().ignoredBundlePrefixes) {
            if (symbolicName.startsWith(ignoredBundlePrefix)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isPreRegisteredExtension(String viewExtension) {
        return getInstance().preRegisteredExtensions.contains(viewExtension);
    }

    private void addBundleScripts(Bundle bundle, String extension, ScriptEngineFactory scriptEngineFactory) {
        // only add views if we need to
        if (!shouldNotBeScannedForViews(bundle, extension)) {
            final String extensionPattern = getExtensionPattern(extension);
            final Enumeration<URL> entries = bundle.findEntries("/", extensionPattern, true);
            if (entries != null) {
                final List<URL> scripts = new LinkedList<>();
                while (entries.hasMoreElements()) {
                    scripts.add(entries.nextElement());
                }
                addBundleScripts(bundle, scripts);
            }
        }
    }

    static String getExtensionPattern(String extension) {
        return EXTENSION_PATTERN_PREFIX + extension;
    }

    private void removeBundleScripts(Bundle bundle, String extension) {
        if (!shouldNotBeScannedForViews(bundle, extension)) {
            final String extensionPattern = getExtensionPattern(extension);
            final Enumeration<URL> entries = bundle.findEntries("/", extensionPattern, true);
            if (entries != null) {
                final List<URL> scripts = new LinkedList<>();
                while (entries.hasMoreElements()) {
                    scripts.add(entries.nextElement());
                }
                removeBundleScripts(bundle, scripts);
            }
        }
    }

    /**
     * Callback for registering new resource views for a bundle.
     * @param bundle the bundle to register views for
     * @param scripts the URLs of the views to register
     */
    public void addBundleScripts(Bundle bundle, List<URL> scripts) {
        // TODO consider versions of modules/bundles
        if (!scripts.isEmpty()) {
            for (URL script : scripts) {
                addBundleScript(bundle, script.getPath());
            }
            logger.info("Bundle {} registered {} views", bundle, logger.isDebugEnabled() ? scripts : scripts.size());
        }
    }

    /**
     * Method for registering a new resource view for a bundle.
     * @param bundle the bundle to register views for
     * @param path the path of the view to register
     */
    public void addBundleScript(Bundle bundle, String path) {
        if (path.split("/").length != 4) {
            return;
        }
        ViewResourceInfo scriptResource = new ViewResourceInfo(path);
        final String symbolicName = bundle.getSymbolicName();
        SortedMap<String, ViewResourceInfo> existingBundleScripts = availableScripts.get(symbolicName);
        if (existingBundleScripts == null) {
            existingBundleScripts = new TreeMap<>();
            availableScripts.put(symbolicName, existingBundleScripts);
            existingBundleScripts.put(scriptResource.path, scriptResource);
        } else if (!existingBundleScripts.containsKey(scriptResource.path)) {
            existingBundleScripts.put(scriptResource.path, scriptResource);
        } else {
            // if we already have a script resource available, retrieve it to make sure we update it with new properties
            // this is required because it is possible that the properties file is not found when the view is first processed due to
            // file ordering processing in ModulesDataSource.start.process method.
            scriptResource = existingBundleScripts.get(scriptResource.path);
        }

        String properties = StringUtils.substringBeforeLast(path, ".") + ".properties";
        final URL propertiesResource = bundle.getResource(properties);
        if (propertiesResource != null) {
            Properties p = new Properties();
            try {
                p.load(propertiesResource.openStream());
            } catch (IOException e) {
                logger.error("Cannot read properties", e);
            }
            scriptResource.setProperties(p);
        } else {
            scriptResource.setProperties(new Properties());
        }
        clearCaches();
    }

    /**
     * Callback for unregistering resource views for a bundle.
     * @param bundle the bundle to unregister views for
     * @param scripts the URLs of the views to unregister
     */
    public void removeBundleScripts(Bundle bundle, List<URL> scripts) {
        final String bundleName = bundle.getSymbolicName();
        final SortedMap<String, ViewResourceInfo> existingBundleScripts = availableScripts.get(bundleName);
        if (existingBundleScripts == null) {
            return;
        }
        if (!scripts.isEmpty()) {
            boolean didRemove = false;
            for (URL script : scripts) {
                didRemove = existingBundleScripts.remove(script.getPath()) != null;
            }

            if (didRemove) {
                // remove entry if we don't have any scripts anymore for this bundle
                if (existingBundleScripts.isEmpty()) {
                    availableScripts.remove(bundleName);
                }

                logger.info("Bundle {} unregistered {} views", bundle, scripts);
                clearCaches();
            }
        }
    }

    /**
     * Method for unregistering a resource view for a bundle.
     * @param bundle the bundle to unregister views for
     * @param path the path of the view to unregister
     */
    public void removeBundleScript(Bundle bundle, String path) {
        final SortedMap<String, ViewResourceInfo> existingBundleScripts = availableScripts.get(bundle.getSymbolicName());
        if (existingBundleScripts == null) {
            return;
        }
        existingBundleScripts.remove(path);
        clearCaches();
    }

    @Override
    public Script resolveScript(Resource resource, RenderContext renderContext) throws TemplateNotFoundException {
        try {
            View resolvedView = resolveView(resource, renderContext);
            if (resolvedView == null) {
                throw new TemplateNotFoundException("Unable to find the view for resource " + resource);
            }

            if (scriptFactoryMap.containsKey(resolvedView.getFileExtension())) {
                return scriptFactoryMap.get(resolvedView.getFileExtension()).createScript(resolvedView);
            }
            throw new TemplateNotFoundException("Unable to script factory map extension handler for the resolved view "
                    + resolvedView.getInfo());
        } catch (RepositoryException e) {
            throw new TemplateNotFoundException(e);
        }
    }

    private View resolveView(Resource resource, RenderContext renderContext) throws RepositoryException {
        ExtendedNodeType nt = resource.getNode().getPrimaryNodeType();
        List<ExtendedNodeType> nodeTypeList = getNodeTypeList(nt);
        for (ExtendedNodeType type : resource.getNode().getMixinNodeTypes()) {
            nodeTypeList.addAll(0, type.getSupertypeSet());
            nodeTypeList.add(0, type);
        }

        if (resource.getResourceNodeType() != null) {
            nodeTypeList.addAll(0, getNodeTypeList(resource.getResourceNodeType()));
        }

        return resolveView(resource, nodeTypeList, renderContext);
    }

    private View resolveView(Resource resource, List<ExtendedNodeType> nodeTypeList, RenderContext renderContext) {
        String template = resource.getResolvedTemplate();
        JCRSiteNode site = renderContext.getSite();

        List<String> templateTypeMappings = null;
        Channel channel = renderContext.getChannel();
        if (channel != null && !channel.getFallBack().equals("root")) {
            templateTypeMappings = new LinkedList<String>();
            while (!channel.getFallBack().equals("root")) {
                if (channel.getCapability("template-type-mapping") != null) {
                    templateTypeMappings.add(resource.getTemplateType() + "-" + channel.getCapability("template-type-mapping"));
                }
                channel = ChannelService.getInstance().getChannel(channel.getFallBack());
            }
            templateTypeMappings.add(resource.getTemplateType());
        }
        Set<View> s = getViewsSet(nodeTypeList, site,
                templateTypeMappings != null ? templateTypeMappings : Collections.singletonList(resource.getTemplateType()));
        View selected;
        selected = getView(template, s);
        if (selected == null && !"default".equals(template)) {
            selected = getView("default", s);
        }
        return selected;
    }

    private View getView(String template, Set<View> s) {
        for (View view : s) {
            if (view.getKey().equals(template)) {
                return view;
            }
        }
        return null;
    }

    @Override
    public boolean hasView(ExtendedNodeType nt, String key, JCRSiteNode site, String templateType) {
        for (View view : getViewsSet(nt, site, templateType)) {
            if (view.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SortedSet<View> getViewsSet(ExtendedNodeType nt, JCRSiteNode site, String templateType) {
        try {
            return getViewsSet(getNodeTypeList(nt), site, Collections.singletonList(templateType));
        } catch (NoSuchNodeTypeException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * @param nt
     * @return
     * @throws NoSuchNodeTypeException
     */
    private List<ExtendedNodeType> getNodeTypeList(ExtendedNodeType nt) throws NoSuchNodeTypeException {
        List<ExtendedNodeType> nodeTypeList = new LinkedList<ExtendedNodeType>();
        nodeTypeList.add(nt);
        nodeTypeList.addAll(nt.getSupertypeSet());
        ExtendedNodeType base = NodeTypeRegistry.getInstance().getNodeType("nt:base");
        if (nodeTypeList.remove(base)) {
            nodeTypeList.add(base);
        }
        return nodeTypeList;
    }

    private SortedSet<View> getViewsSet(List<ExtendedNodeType> nodeTypeList, JCRSiteNode site,
                                        List<String> templateTypes) {

        StringBuilder cacheKey = new StringBuilder();
        for (ExtendedNodeType type : nodeTypeList) {
            cacheKey.append(type.getName()).append("_");
        }
        cacheKey.append("_").append((site != null ? site.getPath() : "")).append("__");
        for (String type : templateTypes) {
            cacheKey.append(type).append("_");
        }
        final String s = cacheKey.toString();

        if (viewSetCache.containsKey(s)) {
            return viewSetCache.get(s);
        } else {
            Map<String, View> views = new HashMap<String, View>();

            Set<String> installedModules = getInstalledModules(site);

            for (ExtendedNodeType type : nodeTypeList) {
                boolean defaultModuleProcessed = false;
                Set<JahiaTemplatesPackage> packages = templateManagerService
                        .getModulesWithViewsForComponent(JCRContentUtils.replaceColon(type.getName()));
                for (JahiaTemplatesPackage aPackage : packages) {
                    String packageName = aPackage.getId();
                    if (installedModules == null || installedModules.contains(packageName)) {
                        if (aPackage.isDefault()) {
                            defaultModuleProcessed = true;
                        }
                        for (String templateType : templateTypes) {
                            getViewsSet(type, views, templateType, aPackage);
                        }
                    }
                }
                if (type.getTemplatePackage() != null && installedModules != null && !installedModules.contains(type.getSystemId())) {
                    for (String templateType : templateTypes) {
                        getViewsSet(type, views, templateType, type.getTemplatePackage());
                    }
                }
                if (!defaultModuleProcessed) {
                    JahiaTemplatesPackage defaultModule = templateManagerService.getTemplatePackageById(JahiaTemplatesPackage.ID_DEFAULT);
                    if (defaultModule != null) {
                        for (String templateType : templateTypes) {
                            getViewsSet(type, views, templateType, defaultModule);
                        }
                    }
                }
            }
            SortedSet<View> t = new TreeSet<View>(views.values());
            viewSetCache.put(s, t);
            return t;
        }
    }

    private Set<String> getInstalledModules(JCRSiteNode site) {
        if (site == null) {
            return null;
        }
        Set<String> installedModules = null;
        String sitePath = site.getPath();
        if (sitePath.startsWith("/sites/")) {
            installedModules = site.getInstalledModulesWithAllDependencies();
        } else if (sitePath.startsWith("/modules/")) {
            JahiaTemplatesPackage aPackage = templateManagerService.getTemplatePackageById(site.getName());
            if (aPackage != null) {
                installedModules = new LinkedHashSet<String>();
                installedModules.add(aPackage.getId());
                for (JahiaTemplatesPackage depend : aPackage.getDependencies()) {
                    if (!installedModules.contains(depend.getId())) {
                        installedModules.add(depend.getId());
                    }
                }
            }
            if (installedModules != null) {
                installedModules.add("templates-system");
                for (JahiaTemplatesPackage depend : templateManagerService.getTemplatePackageById("templates-system").getDependencies()) {
                    if (!installedModules.contains(depend.getId())) {
                        installedModules.add(depend.getId());
                    }
                }
            }
        }

        return installedModules;
    }

    private void getViewsSet(ExtendedNodeType nt, Map<String, View> views, String templateType,
                             JahiaTemplatesPackage tplPackage) {
        StringBuilder pathBuilder = new StringBuilder(64);
        pathBuilder.append("/").append(JCRContentUtils.replaceColon(nt.getAlias())).append("/").append(templateType)
                .append("/");

        // append node type name (without namespace prefix) + "."
        pathBuilder.append(nt.getName().contains(":") ? StringUtils.substringAfter(nt.getName(), ":") : nt.getName())
                .append(".");

        // find scripts in the module bundle, matching that path prefix
        Set<ViewResourceInfo> sortedScripts = findBundleScripts(tplPackage.getId(), pathBuilder.toString());
        Properties defaultProperties = null;
        if (!sortedScripts.isEmpty()) {
            defaultProperties = new Properties();
            JahiaTemplatesPackage aPackage = nt.getTemplatePackage();
            if (aPackage == null) {
                aPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(JahiaTemplatesPackage.ID_DEFAULT);
            }
            if (!aPackage.getId().equals(tplPackage.getId())) {
                Set<ViewResourceInfo> defaultScripts = findBundleScripts(aPackage.getId(), pathBuilder.toString());
                for (ViewResourceInfo defaultScript : defaultScripts) {
                    if (defaultScript.viewKey.equals(View.DEFAULT_VIEW_KEY)) {
                        defaultProperties.putAll(defaultScript.getProperties());
                        break;
                    }
                }
            }
            for (ViewResourceInfo defaultScript : sortedScripts) {
                if (defaultScript.viewKey.equals(View.DEFAULT_VIEW_KEY)) {
                    defaultProperties.putAll(defaultScript.getProperties());
                    break;
                }
            }
        }
        for (ViewResourceInfo res : sortedScripts) {
            if (!views.containsKey(res.viewKey)) {
                if (!scriptFactoryMap.containsKey(res.extension)) {
                    logger.error("Script extension " + res.extension + " can not be handled by this system.");
                    break;
                }
                BundleView view = new BundleView(res.path, res.viewKey, tplPackage, res.filename);
                view.setProperties(res.getProperties());
                view.setDefaultProperties(defaultProperties);
                views.put(res.viewKey, view);
                scriptFactoryMap.get(res.extension).initView(view);
            }
        }
    }

    /**
     * Returns view scripts for the specified module bundle which match the specified path.
     *
     * @param module
     *            the module bundle to perform lookup in
     * @param pathPrefix
     *            the resource path prefix to match
     * @return a set of matching view scripts ordered by the extension (script type)
     */
    private Set<ViewResourceInfo> findBundleScripts(String module, String pathPrefix) {
        final SortedMap<String, ViewResourceInfo> allBundleScripts = availableScripts.get(module);
        if (allBundleScripts == null || allBundleScripts.isEmpty()) {
            return Collections.emptySet();
        }

        // get all the ViewResourceInfos which path is greater than or equal to the given prefix
        final SortedMap<String, ViewResourceInfo> viewInfosWithPathGTEThanPrefix = allBundleScripts.tailMap(pathPrefix);

        // if the tail map is empty, we won't find the path prefix in the available scripts so return an empty set
        if (viewInfosWithPathGTEThanPrefix.isEmpty()) {
            return Collections.emptySet();
        }

        // check if the first key contains the prefix. If not, the prefix will not match any entries so return an empty set
        if (!viewInfosWithPathGTEThanPrefix.firstKey().startsWith(pathPrefix)) {
            return Collections.emptySet();
        } else {
            SortedSet<ViewResourceInfo> sortedScripts = new TreeSet<ViewResourceInfo>(scriptExtensionComparator);
            for (String path : viewInfosWithPathGTEThanPrefix.keySet()) {
                // we should have only few values to look at
                if (path.startsWith(pathPrefix)) {
                    sortedScripts.add(viewInfosWithPathGTEThanPrefix.get(path));
                } else {
                    // as soon as the path doesn't start with the given prefix anymore, we won't have a match in the remaining so return
                    return sortedScripts;
                }
            }
            return sortedScripts;
        }
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof TemplatePackageRedeployedEvent || event instanceof ModuleDeployedOnSiteEvent
                || event instanceof ModuleDependenciesEvent) {
            clearCaches();
        }
    }

    public static void clearCaches() {
        viewSetCache.clear();
    }

    public void setBundleScriptFactory(BundleJSR223ScriptFactory bundleScriptFactory) {
        this.bundleScriptFactory = bundleScriptFactory;
    }
}
