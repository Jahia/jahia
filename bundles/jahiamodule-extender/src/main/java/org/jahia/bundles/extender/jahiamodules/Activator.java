/*
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
package org.jahia.bundles.extender.jahiamodules;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.fileinstall.ArtifactUrlTransformer;
import org.jahia.bin.Jahia;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.BundleResource;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.ExtensionObserverRegistry;
import org.jahia.osgi.FrameworkService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.CacheHelper;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.scripting.bundle.BundleScriptEngineManager;
import org.jahia.services.render.scripting.bundle.BundleScriptResolver;
import org.jahia.services.render.scripting.bundle.ScriptBundleObserver;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JCRModuleListener;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.TemplatePackageDeployer;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.ops4j.pax.swissbox.extender.BundleURLScanner;
import org.osgi.framework.*;
import org.osgi.service.http.HttpService;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.Resource;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.jahia.bundles.extender.jahiamodules.ModuleDependencyTransformer.HANDLER_PREFIX;

/**
 * Activator for DX Modules extender.
 */
public class Activator implements BundleActivator {

    static Logger logger = LoggerFactory.getLogger(Activator.class);

    private static final BundleURLScanner CND_SCANNER = new BundleURLScanner("META-INF", "*.cnd", false);
    private static final BundleURLScanner DSL_SCANNER = new BundleURLScanner("META-INF", "*.dsl", false);
    private static final BundleURLScanner DRL_SCANNER = new BundleURLScanner("META-INF", "*.drl", false);
    private static final BundleURLScanner URLREWRITE_SCANNER = new BundleURLScanner("META-INF", "*urlrewrite*.xml", false);
    private static final BundleURLScanner FLOW_SCANNER = new BundleURLScanner("/", "flow.xml", true);

    private static final Comparator<Resource> IMPORT_FILE_COMPARATOR = new Comparator<Resource>() {
        public int compare(Resource o1, Resource o2) {
            return StringUtils.substringBeforeLast(o1.getFilename(), ".").compareTo(StringUtils.substringBeforeLast(o2.getFilename(), "."));
        }
    };

    private CndBundleObserver cndBundleObserver = null;
    private List<ServiceRegistration<?>> serviceRegistrations = new ArrayList<ServiceRegistration<?>>();
    private BundleListener bundleListener = null;
    private Set<Bundle> installedBundles;
    private Set<Bundle> initializedBundles;
    private Map<Bundle, JahiaTemplatesPackage> registeredBundles;
    private Map<Bundle, ServiceTracker<HttpService, HttpService>> bundleHttpServiceTrackers = new HashMap<Bundle, ServiceTracker<HttpService, HttpService>>();
    private JahiaTemplateManagerService templatesService;
    private TemplatePackageRegistry templatePackageRegistry = null;
    private TemplatePackageDeployer templatePackageDeployer = null;

    private ExtensionObserverRegistry extensionObservers;
    private BundleScriptEngineManager scriptEngineManager;
    private Map<String, List<Bundle>> toBeResolved;

    private FrameworkStartedListener frameworkStartedListener;

    private Map<Bundle, ModuleState> moduleStates;

    private static Activator instance = null;

    private FileInstallConfigurer fileInstallConfigurer;

    public Activator() {
        instance = this;
    }

    public static Activator getInstance() {
        return instance;
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        logger.info("== Starting DX Extender ============================================================== ");
        long startTime = System.currentTimeMillis();

        // obtain service instances
        templatesService = (JahiaTemplateManagerService) SpringContextSingleton.getBean("JahiaTemplateManagerService");
        templatePackageDeployer = templatesService.getTemplatePackageDeployer();
        templatePackageRegistry = templatesService.getTemplatePackageRegistry();

        BundleScriptResolver bundleScriptResolver = (BundleScriptResolver) SpringContextSingleton.getBean("BundleScriptResolver");
        scriptEngineManager = (BundleScriptEngineManager) SpringContextSingleton.getBean("scriptEngineManager");

        extensionObservers = bundleScriptResolver.getObserverRegistry();

        // register rule observers
        RulesBundleObserver rulesBundleObserver = new RulesBundleObserver();
        extensionObservers.put(DSL_SCANNER, rulesBundleObserver);
        extensionObservers.put(DRL_SCANNER, rulesBundleObserver);

        // Get all module state information from the service
        registeredBundles = templatesService.getRegisteredBundles();
        installedBundles = templatesService.getInstalledBundles();
        initializedBundles = templatesService.getInitializedBundles();
        toBeResolved = templatesService.getToBeResolved();
        moduleStates = templatesService.getModuleStates();

        // register view script observers
        bundleScriptResolver.registerObservers();
        final ScriptBundleObserver scriptBundleObserver = bundleScriptResolver.getBundleObserver();

        frameworkStartedListener = new FrameworkStartedListener();

        extensionObservers.put(FLOW_SCANNER, new BundleObserver<URL>() {
            @Override
            public void addingEntries(Bundle bundle, List<URL> entries) {
                for (URL entry : entries) {
                    try {
                        URL parent = new URL(entry.getProtocol(), entry.getHost(), entry.getPort(), new File(entry.getFile()).getParent());
                        scriptBundleObserver.addingEntries(bundle, Collections.singletonList(parent));
                    } catch (MalformedURLException e) {
                        //
                    }
                }
            }

            @Override
            public void removingEntries(Bundle bundle, List<URL> entries) {
                for (URL entry : entries) {
                    try {
                        URL parent = new URL(entry.getProtocol(), entry.getHost(), entry.getPort(), new File(entry.getFile()).getParent());
                        scriptBundleObserver.removingEntries(bundle, Collections.singletonList(parent));
                    } catch (MalformedURLException e) {
                        //
                    }
                }
            }
        });

        // observer for URL rewrite rules
        extensionObservers.put(URLREWRITE_SCANNER, new UrlRewriteBundleObserver());

        // we won't register CND observer, but will rather call it manually
        cndBundleObserver = new CndBundleObserver();

        // add listener for other bundle life cycle events
        setupBundleListener(context);

        // Add transformer for jahia-depends capabilities
        registerLegacyTransformer(context);
        registerJcrUrlHandler(context);

        checkExistingModules(context);

        JCRModuleListener l = (JCRModuleListener) SpringContextSingleton.getBean("org.jahia.services.templates.JCRModuleListener");
        l.setListener(new JCRModuleListener.Listener() {
            @Override
            public void onModuleImported(JahiaTemplatesPackage pack) {
                if (pack.getState().getState() == ModuleState.State.WAITING_TO_BE_IMPORTED) {
                    start(pack.getBundle());
                }
            }
        });

        fileInstallConfigurer = new FileInstallConfigurer();
        fileInstallConfigurer.start(context);

        logger.info("== DX Extender started in {}ms ============================================================== ", System.currentTimeMillis() - startTime);

    }

    private void checkExistingModules(BundleContext context) throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, BundleException, IOException {
        List<Bundle> toStart = new ArrayList<>();
        // parse existing bundles
        for (Bundle bundle : context.getBundles()) {
            if (BundleUtils.isJahiaModuleBundle(bundle)) {
                int state = bundle.getState();
                boolean isRegistered = registeredBundles.containsKey(bundle);

                if (isRegistered && state == Bundle.ACTIVE) {
                    // registering resources for already active bundles
                    registerHttpResources(bundle);
                }

                // Parse bundle if activator has not seen them before
                if (!isRegistered && state > Bundle.INSTALLED) {
                    try {
                        String l = BeanUtils.getProperty(bundle, "location");
                        logger.info("Found bundle {} which needs to be processed by a module extender. Location {}",
                                BundleUtils.getDisplayName(bundle), l);
                        if (state == Bundle.ACTIVE) {
                            bundle.stop();
                            toStart.add(bundle);
                        }
                        try {
                            if (!l.startsWith(HANDLER_PREFIX) && !l.startsWith("inputstream:")
                                    && !StringUtils.contains((String) bundle.getHeaders().get("Provide-Capability"),
                                    "com.jahia.modules.dependencies")) {
                                // transform the module
                                bundle.update(new URL(HANDLER_PREFIX + ":" + l).openStream());
                            } else {
                                bundle.update();
                            }
                        } catch (BundleException e) {
                            logger.warn("Cannot update bundle : " + e.getMessage(), e);
                        }
                    } catch (Exception e) {
                        logger.error("Unable to process the bundle " + bundle, e);
                    }
                }
            }
        }
        for (Bundle bundle : toStart) {
            try {
                bundle.start();
            } catch (Exception e) {
                logger.error("Unable to start the bundle " + bundle, e);
            }
        }
    }

    private synchronized void setupBundleListener(BundleContext context) {
        context.addFrameworkListener(frameworkStartedListener);
        context.addBundleListener(bundleListener = new SynchronousBundleListener() {

                    public void bundleChanged(final BundleEvent bundleEvent) {
                        Bundle bundle = bundleEvent.getBundle();
                        if (bundle == null || !BundleUtils.isJahiaModuleBundle(bundle)) {
                            return;
                        }

                        if (logger.isDebugEnabled()) {
                            logger.debug("Received event {} for bundle {}", BundleUtils.bundleEventToString(bundleEvent.getType()),
                                    getDisplayName(bundleEvent.getBundle()));
                        }
                        try {
                            switch (bundleEvent.getType()) {
                                case BundleEvent.INSTALLED:
                                    install(bundle);
                                    break;
                                case BundleEvent.UPDATED:
                                    update(bundle);
                                    break;
                                case BundleEvent.RESOLVED:
                                    resolve(bundle);
                                    break;
                                case BundleEvent.STARTING:
                                    starting(bundle);
                                    break;
                                case BundleEvent.STARTED:
                                    start(bundle);
                                    break;
                                case BundleEvent.STOPPING:
                                    stopping(bundle);
                                    break;
                                case BundleEvent.STOPPED:
                                    stopped(bundle);
                                    break;
                                case BundleEvent.UNRESOLVED:
                                    unresolve(bundle);
                                    break;
                                case BundleEvent.UNINSTALLED:
                                    uninstall(bundle);
                                    break;
                            }
                        } catch (Exception e) {
                            logger.error("Error when handling event", e);
                        }
                    }

                }
        );
    }

    @Override
    public void stop(BundleContext context) throws Exception {

        logger.info("== Stopping DX Extender ============================================================== ");
        long startTime = System.currentTimeMillis();

        if (fileInstallConfigurer != null) {
            fileInstallConfigurer.stop();
            fileInstallConfigurer = null;
        }

        context.removeBundleListener(bundleListener);
        context.removeFrameworkListener(frameworkStartedListener);

        bundleListener = null;
        frameworkStartedListener = null;

        for (Iterator<ServiceRegistration<?>> iterator = serviceRegistrations.iterator(); iterator.hasNext(); ) {
            try {
                iterator.next().unregister();
            } catch (IllegalStateException e) {
                logger.warn(e.getMessage());
            } finally {
                iterator.remove();
            }
        }

        // Ensure all trackers are correctly closed - should be empty now
        for (Iterator<ServiceTracker<HttpService, HttpService>> iterator = bundleHttpServiceTrackers.values().iterator(); iterator.hasNext(); ) {
            iterator.next().close();
            iterator.remove();
        }

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("== DX Extender stopped in {}ms ============================================================== ", totalTime);

    }

    private synchronized void install(final Bundle bundle) {
        final JahiaTemplatesPackage pkg = BundleUtils.isJahiaModuleBundle(bundle) ? BundleUtils.getModule(bundle) : null;

        if (pkg != null) {
            pkg.setState(getModuleState(bundle));

            //Check required version
            if (!checkRequiredVersion(bundle)) {
                return;
            }

            logger.info("--- Installing DX OSGi bundle {} v{} --", pkg.getId(), pkg.getVersion());

            registeredBundles.put(bundle, pkg);
            templatePackageRegistry.registerPackageVersion(pkg);

            installedBundles.add(bundle);
            setModuleState(bundle, ModuleState.State.INSTALLED, null);
        }
    }

    private synchronized void update(final Bundle bundle) {
        setModuleState(bundle, ModuleState.State.UPDATED, null);
        BundleUtils.unregisterModule(bundle);
        installedBundles.add(bundle);
    }

    private synchronized void uninstall(Bundle bundle) {
        logger.info("--- Uninstalling DX OSGi bundle {} --", getDisplayName(bundle));
        BundleUtils.unregisterModule(bundle);

        long startTime = System.currentTimeMillis();

        final JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (jahiaTemplatesPackage != null) {
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, null, null, new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        templatePackageDeployer.clearModuleNodes(jahiaTemplatesPackage, session);
                        return null;
                    }
                });
                if (templatePackageRegistry.getAvailableVersionsForModule(jahiaTemplatesPackage.getId()).equals(Collections.singleton(jahiaTemplatesPackage.getVersion()))) {
                    if (SettingsBean.getInstance().isDevelopmentMode() && SettingsBean.getInstance().isProcessingServer()
                            && !templatesService.checkExistingContent(bundle.getSymbolicName())) {
                        JCRStoreService jcrStoreService = (JCRStoreService) SpringContextSingleton.getBean("JCRStoreService");
                        jcrStoreService.undeployDefinitions(bundle.getSymbolicName());
                        NodeTypeRegistry.getInstance().unregisterNodeTypes(bundle.getSymbolicName());
                    }
                }
            } catch (IOException | RepositoryException e) {
                logger.error("Error while uninstalling module content for module " + jahiaTemplatesPackage, e);
            }
            templatePackageRegistry.unregisterPackageVersion(jahiaTemplatesPackage);
        }
        moduleStates.remove(bundle);
        installedBundles.remove(bundle);
        initializedBundles.remove(bundle);

        deleteBundleFileIfNeeded(bundle);

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("--- Finished uninstalling DX OSGi bundle {} in {}ms --", getDisplayName(bundle), totalTime);
    }

    private void deleteBundleFileIfNeeded(Bundle bundle) {
        File bundleFile = null;
        try {
            URL bundleUrl = new URL(bundle.getLocation());
            if (bundleUrl.getProtocol().equals("file")) {
                bundleFile = new File(bundleUrl.getFile());
            }
        } catch (MalformedURLException e) {
            // not located in a file
        }
        if (bundleFile != null
                && bundleFile.getAbsolutePath().startsWith(
                SettingsBean.getInstance().getJahiaModulesDiskPath() + File.separatorChar)
                && bundleFile.exists()) {
            // remove bundle file from var/modules
            if (!bundleFile.delete()) {
                logger.warn("Unable to delete file for uninstalled bundle {}", bundleFile);
            }
        }
    }

    private void resolve(final Bundle bundle) {
        final JahiaTemplatesPackage pkg = BundleUtils.isJahiaModuleBundle(bundle) ? BundleUtils.getModule(bundle)
                : null;

        if (null == pkg) {
            // is not a Jahia module -> skip
            installedBundles.remove(bundle);
            moduleStates.remove(bundle);
            return;
        }

        pkg.setState(getModuleState(bundle));

        //Check required version
        if (!checkRequiredVersion(bundle)) {
            return;
        }

        List<String> dependsList = pkg.getDepends();
        if (!dependsList.contains("default")
                && !dependsList.contains("Default Jahia Templates")
                && !ServicesRegistry.getInstance().getJahiaTemplateManagerService().getModulesWithNoDefaultDependency()
                .contains(pkg.getId())) {
            dependsList.add("default");
        }

        for (String depend : dependsList) {
            if (!templatePackageRegistry.areVersionsForModuleAvailable(depend)) {
                logger.debug("Delaying module {} parsing because it depends on module {} that is not yet resolved.",
                        bundle.getSymbolicName(), depend);
                addToBeResolved(bundle, depend);
                return;
            }
        }

        logger.info("--- Resolving DX OSGi bundle {} v{} --", pkg.getId(), pkg.getVersion());

        setModuleState(bundle, ModuleState.State.RESOLVED, null);

        registeredBundles.put(bundle, pkg);
        templatePackageRegistry.registerPackageVersion(pkg);

        try {
            List<URL> foundURLs = CND_SCANNER.scan(bundle);
            if (!foundURLs.isEmpty()) {
                cndBundleObserver.addingEntries(bundle, foundURLs);
            }
        } catch (Exception e) {
            logger.error("--- Error parsing definitions for DX OSGi bundle " + pkg.getId() + " v" + pkg.getVersion(), e);
            setModuleState(bundle, ModuleState.State.ERROR_WITH_DEFINITIONS, e);
            return;
        }

        logger.info("--- Done resolving DX OSGi bundle {} v{} --", pkg.getId(), pkg.getVersion());

        if (installedBundles.remove(bundle) || !checkImported(pkg)) {
            scanForImportFiles(bundle, pkg);

            if (SettingsBean.getInstance().isProcessingServer()) {
                try {
                    logger.info("--- Deploying content for DX OSGi bundle {} v{} --", pkg.getId(), pkg.getVersion());
                    JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser() != null ? JCRSessionFactory.getInstance().getCurrentUser() : JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser();
                    JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, null, null, new JCRCallback<Boolean>() {
                        public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            templatePackageDeployer.initializeModuleContent(pkg, session);
                            return null;
                        }
                    });
                    logger.info("--- Done deploying content for DX OSGi bundle {} v{} --", pkg.getId(), pkg.getVersion());
                } catch (RepositoryException e) {
                    logger.error("Error while initializing module content for module " + pkg, e);
                }
                initializedBundles.add(bundle);
            }
        }

        resolveDependantBundles(pkg.getId());
    }

    private boolean checkRequiredVersion(Bundle bundle) {
        String jahiaRequiredVersion = bundle.getHeaders().get("Jahia-Required-Version");
        if (!StringUtils.isEmpty(jahiaRequiredVersion) && new org.jahia.commons.Version(jahiaRequiredVersion).compareTo(new org.jahia.commons.Version(Jahia.VERSION)) > 0) {
            logger.error("Error while reading module, required version (" + jahiaRequiredVersion + ") is higher than your Jahia version (" + Jahia.VERSION + ")");
            setModuleState(bundle, ModuleState.State.INCOMPATIBLE_VERSION, jahiaRequiredVersion);
            return false;
        }
        return true;
    }

    private void addToBeResolved(Bundle bundle, String missingDependency) {
        List<Bundle> bundlesWaitingForDepend = toBeResolved.get(missingDependency);
        if (bundlesWaitingForDepend == null) {
            bundlesWaitingForDepend = new ArrayList<Bundle>();
            toBeResolved.put(missingDependency, bundlesWaitingForDepend);
        }
        bundlesWaitingForDepend.add(bundle);
    }

    private void resolveDependantBundles(String key) {
        final List<Bundle> toBeResolvedForKey = toBeResolved.get(key);
        if (toBeResolvedForKey != null) {
            for (Bundle bundle : toBeResolvedForKey) {
                if (bundle.getState() != Bundle.UNINSTALLED) {
                    logger.debug("Parsing module " + bundle.getSymbolicName() + " since it is dependent on just resolved module " + key);
                    resolve(bundle);
                }
            }
            toBeResolved.remove(key);
        }
    }

    private void unresolve(Bundle bundle) {
        setModuleState(bundle, ModuleState.State.UNRESOLVED, null);
        // do nothing
    }

    private synchronized void starting(Bundle bundle) {
        if (getModuleState(bundle).getState() == ModuleState.State.ERROR_WITH_DEFINITIONS ||
                getModuleState(bundle).getState() == ModuleState.State.INCOMPATIBLE_VERSION) {
            return;
        }

        setModuleState(bundle, ModuleState.State.STARTING, null);
        JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupById(bundle.getSymbolicName());
        if (jahiaTemplatesPackage != null) {
            try {
                logger.info("Stopping module {} before activating new version...", getDisplayName(bundle));
                jahiaTemplatesPackage.getBundle().stop();
            } catch (BundleException e) {
                logger.info("--- Cannot stop previous version of module " + bundle.getSymbolicName(), e);
            }
        }
        for (Map.Entry<Bundle, ModuleState> entry : moduleStates.entrySet()) {
            if (entry.getKey().getSymbolicName().equals(bundle.getSymbolicName()) && entry.getKey() != bundle) {
                try {
                    entry.getKey().stop();
                } catch (BundleException e) {
                    logger.info("--- Cannot stop previous version of module " + bundle.getSymbolicName(), e);
                }
            }
        }
    }

    private synchronized void start(final Bundle bundle) {
        final JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (jahiaTemplatesPackage == null) {
            logger.error("--- Bundle " + bundle + " is starting but has not yet been parsed");
            return;
        }

        ModuleState.State state = getModuleState(bundle).getState();
        if (state == ModuleState.State.ERROR_WITH_DEFINITIONS ||
                state == ModuleState.State.INCOMPATIBLE_VERSION) {
            return;
        }

        if (!checkImported(jahiaTemplatesPackage)) {
            setModuleState(bundle, ModuleState.State.WAITING_TO_BE_IMPORTED, null);
            return;
        }


        logger.info("--- Start DX OSGi bundle {} --", getDisplayName(bundle));
        long startTime = System.currentTimeMillis();

        templatePackageRegistry.register(jahiaTemplatesPackage);
        jahiaTemplatesPackage.setActiveVersion(true);
        templatesService.fireTemplatePackageRedeployedEvent(jahiaTemplatesPackage);

        // scan for resource and call observers
        for (Map.Entry<BundleURLScanner, BundleObserver<URL>> scannerAndObserver : extensionObservers.entrySet()) {
            List<URL> foundURLs = scannerAndObserver.getKey().scan(bundle);
            if (!foundURLs.isEmpty()) {
                scannerAndObserver.getValue().addingEntries(bundle, foundURLs);
            }
        }

        registerHttpResources(bundle);

        long totalTime = System.currentTimeMillis() - startTime;

        if (initializedBundles.remove(bundle)) {
            //auto deploy bundle according to bundle configuration
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, null, null, new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        templatesService.autoInstallModulesToSites(jahiaTemplatesPackage, session);
                        session.save();
                        return null;
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Error while initializing module content for module " + jahiaTemplatesPackage, e);
            }
        }

        // check for script engine factories
        scriptEngineManager.addScriptEngineFactoriesIfNeeded(bundle);

        logger.info("--- Finished starting DX OSGi bundle {} in {}ms --", getDisplayName(bundle), totalTime);
        setModuleState(bundle, ModuleState.State.STARTED, null);

        if (hasSpringFile(bundle)) {
            try {
                final AbstractApplicationContext contextToStartForModule = BundleUtils.getContextToStartForModule(bundle);
                if (contextToStartForModule != null) {
                    contextToStartForModule.refresh();
                }
            } catch (Exception e) {
                logger.error("Unable to create application context for [" + bundle.getSymbolicName() + "]", e);
            }
        }
    }

    private boolean checkImported(final JahiaTemplatesPackage jahiaTemplatesPackage) {
        try {
            boolean imported = JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, null, null, new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    return session.itemExists("/modules/" + jahiaTemplatesPackage.getId() + "/" + jahiaTemplatesPackage.getVersion());
                }
            });
            if (!imported) {
                return false;
            }
        } catch (RepositoryException e) {
            logger.error("Error while reading module jcr content" + jahiaTemplatesPackage, e);
        }
        return true;
    }

    private boolean hasSpringFile(Bundle bundle) {
        Enumeration<String> entries = bundle.getEntryPaths("/META-INF/spring");
        if (entries != null) {
            while (entries.hasMoreElements()) {
                String s = entries.nextElement();
                if (s.toLowerCase().endsWith(".xml")) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getDisplayName(Bundle bundle) {
        return BundleUtils.getDisplayName(bundle);
    }

    private synchronized void stopping(Bundle bundle) {
        if (getModuleState(bundle).getState() == ModuleState.State.ERROR_WITH_DEFINITIONS ||
                getModuleState(bundle).getState() == ModuleState.State.INCOMPATIBLE_VERSION) {
            return;
        }

        logger.info("--- Stopping DX OSGi bundle {} --", getDisplayName(bundle));
        setModuleState(bundle, ModuleState.State.STOPPING, null);

        long startTime = System.currentTimeMillis();

        JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (jahiaTemplatesPackage == null || !jahiaTemplatesPackage.isActiveVersion()) {
            return;
        }

        if (JahiaContextLoaderListener.isRunning()) {
            final String pkgId = jahiaTemplatesPackage.getId();
            final String pkgName = jahiaTemplatesPackage.getName();

            templatePackageRegistry.unregister(jahiaTemplatesPackage);

            boolean cachesNeedFlushing = true;
            if (jahiaTemplatesPackage.getInitialImports().isEmpty()) {
                // check for initial imports
                Enumeration<URL> importXMLEntryEnum = bundle.findEntries("META-INF", "import*.xml", false);
                if (importXMLEntryEnum == null || !importXMLEntryEnum.hasMoreElements()) {
                    importXMLEntryEnum = bundle.findEntries("META-INF", "import*.zip", false);
                    if (importXMLEntryEnum == null || !importXMLEntryEnum.hasMoreElements()) {
                        // no templates -> no need to flush caches
                        cachesNeedFlushing = false;
                    }
                }
            }
            jahiaTemplatesPackage.setActiveVersion(false);
            templatesService.fireTemplatePackageRedeployedEvent(jahiaTemplatesPackage);

            if (jahiaTemplatesPackage.getContext() != null) {
                jahiaTemplatesPackage.setContext(null);
            }
            jahiaTemplatesPackage.setClassLoader(null);

            // scan for resource and call observers
            for (Map.Entry<BundleURLScanner, BundleObserver<URL>> scannerAndObserver : extensionObservers.entrySet()) {
                List<URL> foundURLs = scannerAndObserver.getKey().scan(bundle);
                if (!foundURLs.isEmpty()) {
                    scannerAndObserver.getValue().removingEntries(bundle, foundURLs);
                    cachesNeedFlushing = true;
                }
            }

            if (cachesNeedFlushing) {
                flushOutputCachesForModule(bundle, pkgId, pkgName);
            }

            // deal with script engine factories
            scriptEngineManager.removeScriptEngineFactoriesIfNeeded(bundle);

            if (cachesNeedFlushing) {
                flushOutputCachesForModule(bundle, pkgId, pkgName);
            }

            ServiceTracker<HttpService, HttpService> tracker = bundleHttpServiceTrackers.remove(bundle);
            if (tracker != null) {
                tracker.close();
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("--- Finished stopping DX OSGi bundle {} in {}ms --", getDisplayName(bundle), totalTime);
    }

    private void flushOutputCachesForModule(Bundle bundle, final String pkgId, final String pkgName) {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                @Override
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    List<JCRSiteNode> sitesNodeList = JahiaSitesService.getInstance().getSitesNodeList(session);
                    Set<String> pathsToFlush = new HashSet<String>();
                    for (JCRSiteNode site : sitesNodeList) {
                        Set<String> installedModules = site.getInstalledModulesWithAllDependencies();
                        if (installedModules.contains(pkgId) || installedModules.contains(pkgName)) {
                            pathsToFlush.add(site.getPath());
                        }
                    }
                    if (!pathsToFlush.isEmpty()) {
                        CacheHelper.flushOutputCachesForPaths(pathsToFlush, true);
                    }
                    return Boolean.TRUE;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private synchronized void stopped(Bundle bundle) {
        // Ensure context is reset
        BundleUtils.setContextToStartForModule(bundle, null);

        if (getModuleState(bundle).getState() == ModuleState.State.ERROR_WITH_DEFINITIONS ||
                getModuleState(bundle).getState() == ModuleState.State.INCOMPATIBLE_VERSION) {
            return;
        }

        setModuleState(bundle, ModuleState.State.STOPPED, null);
    }

    private void registerHttpResources(final Bundle bundle) {
        final String displayName = getDisplayName(bundle);

        if (!BundleHttpResourcesTracker.getStaticResources(bundle).isEmpty()
                || !BundleHttpResourcesTracker.getJsps(bundle).isEmpty()) {
            logger.debug("Found HTTP resources for bundle {}." + " Will launch service tracker for HttpService",
                    displayName);
            ServiceTracker<HttpService, HttpService> tracker = bundleHttpServiceTrackers.remove(bundle);
            if (tracker != null) {
                tracker.close();
            }
            if (bundle.getBundleContext() != null) {
                ServiceTracker<HttpService, HttpService> bundleServiceTracker = new BundleHttpResourcesTracker(bundle);
                bundleServiceTracker.open(true);
                bundleHttpServiceTrackers.put(bundle, bundleServiceTracker);
            }
        } else {
            logger.debug("No HTTP resources found for bundle {}", displayName);
        }
    }

    private void scanForImportFiles(Bundle bundle, JahiaTemplatesPackage jahiaTemplatesPackage) {
        List<Resource> importFiles = new ArrayList<Resource>();
        Enumeration<URL> importXMLEntryEnum = bundle.findEntries("META-INF", "import*.xml", false);
        if (importXMLEntryEnum != null) {
            while (importXMLEntryEnum.hasMoreElements()) {
                importFiles.add(new BundleResource(importXMLEntryEnum.nextElement(), bundle));
            }
        }
        Enumeration<URL> importZIPEntryEnum = bundle.findEntries("META-INF", "import*.zip", false);
        if (importZIPEntryEnum != null) {
            while (importZIPEntryEnum.hasMoreElements()) {
                importFiles.add(new BundleResource(importZIPEntryEnum.nextElement(), bundle));
            }
        }
        Collections.sort(importFiles, IMPORT_FILE_COMPARATOR);
        for (Resource importFile : importFiles) {
            try {
                jahiaTemplatesPackage.addInitialImport(importFile.getURL().getPath());
            } catch (IOException e) {
                logger.error("Error retrieving URL for resource " + importFile, e);
            }
        }
    }

    public Map<ModuleState.State, Set<Bundle>> getModulesByState() {
        Map<ModuleState.State, Set<Bundle>> modulesByState = new TreeMap<ModuleState.State, Set<Bundle>>();
        for (Bundle bundle : moduleStates.keySet()) {
            ModuleState.State moduleState = moduleStates.get(bundle).getState();
            Set<Bundle> bundlesInState = modulesByState.get(moduleState);
            if (bundlesInState == null) {
                bundlesInState = new TreeSet<Bundle>();
            }
            bundlesInState.add(bundle);
            modulesByState.put(moduleState, bundlesInState);
        }
        return modulesByState;
    }

    private class FrameworkStartedListener implements FrameworkListener {
        @Override
        public synchronized void frameworkEvent(FrameworkEvent event) {
            switch (event.getType()) {
                case FrameworkEvent.STARTED:
                    logger.info("Got started event from OSGi framework");
                    FrameworkService.notifyStarted();
                    break;
            }
        }
    }

    public ModuleState getModuleState(Bundle bundle) {
        if (!moduleStates.containsKey(bundle)) {
            moduleStates.put(bundle, new ModuleState());
        }
        return moduleStates.get(bundle);
    }

    public void setModuleState(Bundle bundle, ModuleState.State state, Object details) {
        ModuleState moduleState = getModuleState(bundle);
        moduleState.setState(state);
        moduleState.setDetails(details);
    }

    /**
     * Registers services for module dependency transformation.
     *
     * @param context the OSGi bundle context object.
     */
    private void registerLegacyTransformer(BundleContext context) {
        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put("url.handler.protocol", HANDLER_PREFIX);
        serviceRegistrations
                .add(context.registerService(URLStreamHandlerService.class, new ModuleDependencyTransformer(), props));

        serviceRegistrations.add(context.registerService(new String[]{ArtifactUrlTransformer.class.getName()},
                new ModuleDependencyTransformer(), null));

    }

    /**
     * Registers services for Jcr bundle protocol url resolution transformation.
     *
     * @param context the OSGi bundle context object.
     */
    private void registerJcrUrlHandler(BundleContext context) throws IOException {
        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put("url.handler.protocol", "jcr");
        serviceRegistrations
                .add(context.registerService(URLStreamHandlerService.class, new JcrBundleTransformer(), props));
    }


}
