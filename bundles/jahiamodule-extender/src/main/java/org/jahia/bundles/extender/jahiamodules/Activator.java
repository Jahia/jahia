/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.bundles.extender.jahiamodules.fileinstall.FileInstallConfigurer;
import org.jahia.bundles.extender.jahiamodules.jsp.JahiaJasperInitializer;
import org.jahia.bundles.extender.jahiamodules.logging.PaxLoggingConfigurer;
import org.jahia.bundles.extender.jahiamodules.mvn.MavenURLStreamHandler;
import org.jahia.bundles.extender.jahiamodules.transform.DxModuleURLStreamHandler;
import org.jahia.bundles.extender.jahiamodules.transform.ModuleDependencyURLStreamHandler;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.data.templates.ModuleState.State;
import org.jahia.osgi.BundleLifecycleUtils;
import org.jahia.osgi.BundleResource;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.ExtensionObserverRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.CacheHelper;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.persistence.PersistentBundle;
import org.jahia.services.modulemanager.persistence.jcr.BundleInfoJcrHelper;
import org.jahia.services.modulemanager.util.ModuleUtils;
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
import org.jahia.tools.patches.Patcher;
import org.jahia.utils.spring.http.converter.json.JahiaMappingJackson2HttpMessageConverter;
import org.json.JSONObject;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.ops4j.pax.swissbox.extender.BundleURLScanner;
import org.osgi.framework.*;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.namespace.extender.ExtenderNamespace;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import pl.touk.throwing.ThrowingBiConsumer;
import pl.touk.throwing.ThrowingPredicate;

import javax.jcr.RepositoryException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jahia.services.modulemanager.Constants.URL_PROTOCOL_DX;
import static org.jahia.services.modulemanager.Constants.URL_PROTOCOL_MODULE_DEPENDENCIES;
import static org.jahia.tools.patches.Patcher.KEEP;
import static org.jahia.tools.patches.Patcher.RESOURCE_COMPARATOR;

/**
 * Activator for DX Modules extender.
 */
public class Activator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(Activator.class);

    private static final BundleURLScanner CND_SCANNER = new BundleURLScanner("META-INF", "*.cnd", false);
    private static final BundleURLScanner CFG_SCANNER = new BundleURLScanner("META-INF/configurations", "*", false);
    private static final BundleURLScanner DSL_SCANNER = new BundleURLScanner("META-INF", "*.dsl", false);
    private static final BundleURLScanner DRL_SCANNER = new BundleURLScanner("META-INF", "*.drl", false);
    private static final BundleURLScanner URLREWRITE_SCANNER = new BundleURLScanner("META-INF", "*urlrewrite*.xml", false);
    private static final BundleURLScanner FLOW_SCANNER = new BundleURLScanner("/", "flow.xml", true);

    private static final Comparator<Resource> IMPORT_FILE_COMPARATOR = Comparator.comparing(o -> StringUtils.substringBeforeLast(o.getFilename(), "."));
    private static final String EXTENDER_CAPABILITY_NAME = "org.jahia.bundles.blueprint.extender.config";

    private static Activator instance;

    private static Map<Integer, String> status = new HashMap<>();

    static {
        status.put(BundleEvent.RESOLVED, "resolved");
        status.put(BundleEvent.STARTED, "started");
        status.put(BundleEvent.STOPPED, "stopped");
    }

    private CndBundleObserver cndBundleObserver;
    private List<ServiceRegistration<?>> serviceRegistrations = new ArrayList<>();
    private SynchronousBundleListener bundleListener;
    private Set<Bundle> installedBundles;
    private Set<Bundle> initializedBundles;
    private Map<Bundle, JahiaTemplatesPackage> registeredBundles;
    private Map<Bundle, ServiceTracker<HttpService, HttpService>> bundleHttpServiceTrackers = new HashMap<>();
    private JahiaTemplateManagerService templatesService;
    private TemplatePackageRegistry templatePackageRegistry;
    private TemplatePackageDeployer templatePackageDeployer;
    private ExtensionObserverRegistry startExtensionObservers;
    private RulesDSLBundleObserver rulesDSLBundleObserver;
    private BundleScriptEngineManager scriptEngineManager;
    private Map<String, List<Bundle>> toBeResolved;
    private Map<Bundle, ModuleState> moduleStates;
    private FileInstallConfigurer fileInstallConfigurer;
    private PaxLoggingConfigurer log4jConfigurer;

    private Boolean stopBundleWithErrorInRules;
    private ExecutorService executorService;
    private JahiaJasperInitializer jasperInitializer;

    public Activator() {
        instance = this;
    }

    public static Activator getInstance() {
        return instance;
    }

    /**
     * Persists the bundle content in DX and returns the new location URL which handles the transformed bundle content.
     *
     * @param bundle the source bundle
     * @return the location of the transformed bundle
     */
    private static String transform(Bundle bundle) throws ModuleManagementException {
        try {
            PersistentBundle persistentBundle = ModuleUtils.persist(bundle);
            String newLocation = persistentBundle.getLocation();
            logger.info("Transformed bundle {} with location {} to be handled by the DX protocol handler under new location {}", getDisplayName(bundle), bundle.getLocation(), newLocation);
            return newLocation;
        } catch (ModuleManagementException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Unable to transform bundle " + bundle + ". Cause: " + e.getMessage();
            logger.error(msg, e);
            throw new ModuleManagementException(msg, e);
        }
    }

    private static String getDisplayName(Bundle bundle) {
        return BundleUtils.getDisplayName(bundle);
    }

    private static boolean isInvalid(ModuleState.State moduleState) {
        return moduleState == ModuleState.State.ERROR_WITH_DEFINITIONS || moduleState == ModuleState.State.INCOMPATIBLE_VERSION;
    }

    private static boolean needsDefaultModuleDependency(final JahiaTemplatesPackage pkg) {
        return !pkg.getDepends().contains(JahiaTemplatesPackage.ID_DEFAULT)
                && !pkg.getDepends().contains(JahiaTemplatesPackage.NAME_DEFAULT)
                && !ServicesRegistry.getInstance().getJahiaTemplateManagerService().getModulesWithNoDefaultDependency()
                .contains(pkg.getId());
    }

    @Override
    public void start(final BundleContext context) throws Exception {

        logger.info("== Starting DX Extender ============================================================== ");
        long startTime = System.currentTimeMillis();

        registerJackson2ConverterDelegate(new MappingJackson2HttpMessageConverter());

        // obtain service instances
        templatesService = (JahiaTemplateManagerService) SpringContextSingleton.getBean("JahiaTemplateManagerService");
        templatePackageDeployer = templatesService.getTemplatePackageDeployer();
        templatePackageRegistry = templatesService.getTemplatePackageRegistry();

        BundleScriptResolver bundleScriptResolver = (BundleScriptResolver) SpringContextSingleton.getBean("BundleScriptResolver");
        scriptEngineManager = (BundleScriptEngineManager) SpringContextSingleton.getBean("scriptEngineManager");

        // extension observers used during start phase (unregistered is done in stop phase)
        startExtensionObservers = bundleScriptResolver.getObserverRegistry();

        // DSL are registered during resolve state because other dependants module can use .dsl rules instructions from other modules.
        rulesDSLBundleObserver = new RulesDSLBundleObserver();
        // DRL are registered and compile when module is started
        startExtensionObservers.put(DRL_SCANNER, new RulesBundleObserver());

        // Get all module state information from the service
        registeredBundles = templatesService.getRegisteredBundles();
        installedBundles = templatesService.getInstalledBundles();
        initializedBundles = templatesService.getInitializedBundles();
        toBeResolved = templatesService.getToBeResolved();
        moduleStates = templatesService.getModuleStates();

        // register view script observers
        bundleScriptResolver.registerObservers();
        final ScriptBundleObserver scriptBundleObserver = bundleScriptResolver.getBundleObserver();

        startExtensionObservers.put(FLOW_SCANNER, new BundleObserver<URL>() {

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
        startExtensionObservers.put(URLREWRITE_SCANNER, new UrlRewriteBundleObserver());

        // we won't register CND observer, but will rather call it manually
        cndBundleObserver = new CndBundleObserver();

        // add listener for other bundle life cycle events
        setupBundleListener(context);

        // Add transformers and URL handlers for jcr persistence and jahia-depends capabilities
        registerUrlTransformers(context);

        jasperInitializer = new JahiaJasperInitializer();

        checkExistingModules(context);

        JCRModuleListener l = (JCRModuleListener) SpringContextSingleton.getBean("org.jahia.services.templates.JCRModuleListener");
        l.setListener(pack -> {
            if (pack.getState().getState() == State.WAITING_TO_BE_IMPORTED) {
                start(pack.getBundle());
            }
        });

        stopBundleWithErrorInRules = Boolean.valueOf(SettingsBean.getInstance().getString("jahia.modules.stopBundleWithErrorInRules", "true"));

        // Wait for spring-bridge to be started (and so all other system bundles) before initializing persistent bundles and fileinstall
        new ServiceTracker<Object,Object>(context, "org.jahia.api.settings.SettingsBean", null) {
            @Override
            public Object addingService(ServiceReference<Object> reference) {
                close();
                new Thread(() -> {
                    logger.info("== Start initial modules provisioning");
                    checkPersistentStateMarker();
                    fileInstallConfigurer = new FileInstallConfigurer();
                    fileInstallConfigurer.start(context);
                }).start();
                return super.addingService(reference);
            }
        }.open(true);

        log4jConfigurer = new PaxLoggingConfigurer();
        log4jConfigurer.start(context);

        logger.info("== DX Extender started in {}ms ============================================================== ", System.currentTimeMillis() - startTime);
    }

    private void checkPersistentStateMarker() {
        File applyBundlesPersistentStatesMarker = new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "[persisted-bundles].dorestore");
        if (System.getProperty("applyBundlesPersistentStates") != null || applyBundlesPersistentStatesMarker.exists()) {
            logger.info("== Applying persistent states");
            ModuleUtils.getModuleManager().applyBundlesPersistentStates(null);
            logger.info("== Successfully applied persistent states");
            if (applyBundlesPersistentStatesMarker.exists()) {
                applyBundlesPersistentStatesMarker.delete();
            }
        }
    }

    private void checkExistingModules(BundleContext context) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, BundleException, IOException {
        List<Bundle> toStart = new ArrayList<>();

        // parse existing bundles
        for (Bundle bundle : context.getBundles()) {
            if (BundleUtils.isJahiaModuleBundle(bundle)) {
                checkExistingModule(toStart, bundle);
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

    private void checkExistingModule(List<Bundle> toStart, Bundle bundle) {
        int state = bundle.getState();
        boolean isRegistered = registeredBundles.containsKey(bundle);

        if (isRegistered && state == Bundle.ACTIVE) {
            // registering resources for already active bundles
            registerHttpResources(bundle);
        }

        if (!isRegistered && state >= Bundle.INSTALLED) {
            // Parse bundle if activator has not seen them before
            try {
                String bundleLocation = bundle.getLocation();
                String bundleDisplayName = BundleUtils.getDisplayName(bundle);
                logger.info("Found bundle {} which needs to be processed by a module extender. Location {}. State: {}", bundleDisplayName, bundleLocation, bundle.getState());
                if (state == Bundle.ACTIVE) {
                    bundle.stop(Bundle.STOP_TRANSIENT);
                    toStart.add(bundle);
                } else if (state == Bundle.INSTALLED) {
                    final JahiaTemplatesPackage pkg = BundleUtils.getModule(bundle);

                    if (pkg != null) {
                        // we register the bundle state
                        setModuleState(bundle, State.INSTALLED, null);
                        pkg.setState(getModuleState(bundle));
                        registeredBundles.put(bundle, pkg);
                    }
                }
                handleBundleMigration(bundle, bundleLocation, bundleDisplayName);
            } catch (Exception e) {
                logger.error("Unable to process the bundle " + bundle, e);
            }
        }

        if (state >= Bundle.RESOLVED) {
            resolve(bundle);
        }
    }

    private void handleBundleMigration(Bundle bundle, String bundleLocation, String bundleDisplayName) {
        try {
            if (!bundleLocation.startsWith(URL_PROTOCOL_DX)) {
                // transform the module
                String newLocation = transform(bundle);
                // overwrite bundle location
                ModuleUtils.updateBundleLocation(bundle, newLocation);

                // we check the start level for a module and adjust it
                BundleStartLevel bundleStartLevel = bundle.adapt(BundleStartLevel.class);
                int moduleStartLevel = SettingsBean.getInstance().getModuleStartLevel();
                if (bundleStartLevel.getStartLevel() != moduleStartLevel) {
                    // update start level
                    logger.info("Setting start level for bundle {} to {}", bundleDisplayName,
                            moduleStartLevel);
                    bundleStartLevel.setStartLevel(moduleStartLevel);
                }
                bundle.update();
            }
        } catch (BundleException | ModuleManagementException e) {
            logger.warn("Cannot update bundle : " + e.getMessage(), e);
        }
    }

    private synchronized void setupBundleListener(BundleContext context) {
        bundleListener = bundleEvent -> {
            Bundle bundle = bundleEvent.getBundle();

            int bundleEventType = bundleEvent.getType();
            if (bundle == null) {
                return;
            }

            // refresh host bundle in case of fragment operation
            if (BundleUtils.isFragment(bundle) && (bundleEventType == BundleEvent.INSTALLED || bundleEventType == BundleEvent.UNINSTALLED)) {
                BundleLifecycleUtils.refreshBundles(BundleLifecycleUtils.getHostsFragment(bundle), false, false);
                return;
            }

            if (!BundleUtils.isJahiaModuleBundle(bundle)) {
                return;
            }

            if (SettingsBean.getInstance().isProcessingServer()) {
                handlePatches(bundleEvent);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Received event {} for bundle {}", BundleUtils.bundleEventToString(bundleEventType), getDisplayName(bundleEvent.getBundle()));
            }
            try {
                switch (bundleEventType) {
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
                    default:
                        logger.debug("Unknown event : {}", bundleEventType);
                }
            } catch (Exception e) {
                logger.error("Error when handling event", e);
            }
        };
        context.addBundleListener(bundleListener);
    }

    @Override
    public void stop(BundleContext context) throws Exception {

        logger.info("== Stopping DX Extender ============================================================== ");
        long startTime = System.currentTimeMillis();

        if (JahiaContextLoaderListener.isRunning()) {
            registerJackson2ConverterDelegate(null);
        }

        shutdownExecutorService(10);

        if (fileInstallConfigurer != null) {
            fileInstallConfigurer.stop();
            fileInstallConfigurer = null;
        }

        if (log4jConfigurer != null) {
            log4jConfigurer.stop();
            log4jConfigurer = null;
        }

        context.removeBundleListener(bundleListener);

        bundleListener = null;

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
            installedBundles.add(bundle);
            setModuleState(bundle, ModuleState.State.INSTALLED, null);
        }
    }

    private synchronized void update(final Bundle bundle) {

        BundleUtils.unregisterModule(bundle);
        final JahiaTemplatesPackage pkg = BundleUtils.isJahiaModuleBundle(bundle) ? BundleUtils.getModule(bundle) : null;

        if (pkg != null) {
            pkg.setState(getModuleState(bundle));

            //Check required version
            if (!checkRequiredVersion(bundle)) {
                return;
            }

            logger.info("--- Updating DX OSGi bundle {} v{} --", pkg.getId(), pkg.getVersion());
            registeredBundles.put(bundle, pkg);
            installedBundles.add(bundle);
            setModuleState(bundle, ModuleState.State.UPDATED, null);
        }
    }

    private synchronized void uninstall(Bundle bundle) {

        logger.info("--- Uninstalling DX OSGi bundle {} --", getDisplayName(bundle));
        BundleUtils.unregisterModule(bundle);

        long startTime = System.currentTimeMillis();

        final JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (jahiaTemplatesPackage != null) {

            try {
                SettingsBean settingsBean = SettingsBean.getInstance();
                boolean isProcessingServer = settingsBean.isProcessingServer();
                if (isProcessingServer) {
                    clearModuleNodes(jahiaTemplatesPackage);
                }
                if (templatePackageRegistry.getAvailableVersionsForModule(jahiaTemplatesPackage.getId()).equals(Collections.singleton(jahiaTemplatesPackage.getVersion()))
                        && settingsBean.isDevelopmentMode()
                        && isProcessingServer
                        && !templatesService.checkExistingContent(bundle.getSymbolicName())) {
                    JCRStoreService jcrStoreService = (JCRStoreService) SpringContextSingleton.getBean("JCRStoreService");
                    jcrStoreService.undeployDefinitions(bundle.getSymbolicName());
                    NodeTypeRegistry.getInstance().unregisterNodeTypes(bundle.getSymbolicName());
                }
            } catch (IOException | RepositoryException e) {
                logger.error("Error while uninstalling module content for module " + jahiaTemplatesPackage, e);
            }
            templatePackageRegistry.unregisterPackageVersion(jahiaTemplatesPackage);
        }
        moduleStates.remove(bundle);
        installedBundles.remove(bundle);
        initializedBundles.remove(bundle);

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("--- Finished uninstalling DX OSGi bundle {} in {}ms --", getDisplayName(bundle), totalTime);
    }

    private void clearModuleNodes(JahiaTemplatesPackage jahiaTemplatesPackage) {
        try {
            templatePackageDeployer.clearModuleNodesAsync(jahiaTemplatesPackage);
        } catch (Exception e) {
            // We still want to perform the rest of operations in case module nodes cleanup fails for some reason.
            logger.error(e.getMessage(), e);
        }
    }

    private synchronized void resolve(final Bundle bundle) {

        final JahiaTemplatesPackage pkg = BundleUtils.isJahiaModuleBundle(bundle) ? BundleUtils.getModule(bundle) : null;

        if (null == pkg) {
            // is not a Jahia module -> skip
            moduleStates.remove(bundle);
            installedBundles.remove(bundle);
            return;
        }

        pkg.setState(getModuleState(bundle));

        // Check required version
        if (!checkRequiredVersion(bundle)) {
            return;
        }

        List<String> dependsList = pkg.getDepends();
        if (needsDefaultModuleDependency(pkg)) {
            dependsList.add(JahiaTemplatesPackage.ID_DEFAULT);
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

        checkConfigurations(bundle);

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

        // manually scan and perform observer for rules .dsl files
        scanBundleAndPerformObserver(bundle, pkg, DSL_SCANNER, rulesDSLBundleObserver, false);

        logger.info("--- Done resolving DX OSGi bundle {} v{} --", pkg.getId(), pkg.getVersion());

        checkInitialImport(bundle, pkg);

        resolveDependantBundles(pkg.getId());

        if (Bundle.ACTIVE == bundle.getState()) {
            // we've got an event for already started bundles
            // sometimes FileInstall sends the STARTED event before RESOLVED, so we have to handle this case
            logger.info("Got RESOLVED event for an already started bundle {} v{}. Proccesing started bundle.",
                    pkg.getId(), pkg.getVersion());
            start(bundle);
        }
    }

    private void checkInitialImport(Bundle bundle, JahiaTemplatesPackage pkg) {
        if (installedBundles.remove(bundle) || !checkImported(pkg)) {
            scanForImportFiles(bundle, pkg);

            if (SettingsBean.getInstance().isProcessingServer()) {
                try {
                    logger.info("--- Deploying content for DX OSGi bundle {} v{} --", pkg.getId(), pkg.getVersion());
                    JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser() != null ? JCRSessionFactory.getInstance().getCurrentUser() : JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser();

                    JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, null, null, session -> {
                        templatePackageDeployer.initializeModuleContent(pkg, session);
                        return null;
                    });
                    logger.info("--- Done deploying content for DX OSGi bundle {} v{} --", pkg.getId(), pkg.getVersion());
                } catch (RepositoryException e) {
                    logger.error("Error while initializing module content for module " + pkg, e);
                }
                initializedBundles.add(bundle);
            }
        }
    }

    private void checkConfigurations(Bundle bundle) {
        List<URL> foundURLs = CFG_SCANNER.scan(bundle);
        if (!foundURLs.isEmpty()) {
            for (URL url : foundURLs) {
                try (InputStream openStream = url.openStream()){
                    Path path = Paths.get(SettingsBean.getInstance().getJahiaVarDiskPath(), "karaf", "etc", StringUtils.substringAfterLast(url.getFile(), "/"));
                    List<String> lines = IOUtils.readLines(openStream, StandardCharsets.UTF_8);
                    boolean isEditable = lines.stream().map(String::toLowerCase).anyMatch(p -> p.startsWith("# default configuration"));
                    if (!path.toFile().exists() || !isEditable) {
                        if (!isEditable && Stream.of("cfg","config","yml","yaml").anyMatch(suffix -> path.toString().endsWith(suffix))) {
                            lines.add(0, "# Do not edit - Configuration file provided by module, any change will be lost");
                        }
                        try (Writer w = new FileWriter(path.toFile())) {
                            IOUtils.writeLines(lines, null, w);
                        }
                        logger.info("Copied configuration file of module {} into {}", getDisplayName(bundle), path);
                    }
                } catch (IOException e) {
                    logger.error("unable to copy configuration", e);
                }
            }
        }
    }

    private boolean checkRequiredVersion(Bundle bundle) {
        String jahiaRequiredVersion = bundle.getHeaders().get("Jahia-Required-Version");
        if (!StringUtils.isEmpty(jahiaRequiredVersion) && new org.jahia.commons.Version(jahiaRequiredVersion).compareTo(new org.jahia.commons.Version(Jahia.VERSION)) > 0) {
            logger.error("Error while reading module, required version ({}) is higher than your Jahia version ({})", jahiaRequiredVersion, Jahia.VERSION);
            setModuleState(bundle, ModuleState.State.INCOMPATIBLE_VERSION, jahiaRequiredVersion);
            return false;
        }
        return true;
    }

    private void addToBeResolved(Bundle bundle, String missingDependency) {
        List<Bundle> bundlesWaitingForDepend = toBeResolved.computeIfAbsent(missingDependency, k -> new CopyOnWriteArrayList<>());
        bundlesWaitingForDepend.add(bundle);
    }

    private void resolveDependantBundles(String key) {
        final List<Bundle> toBeResolvedForKey = toBeResolved.get(key);
        if (toBeResolvedForKey != null) {
            for (Bundle bundle : toBeResolvedForKey) {
                if (bundle.getState() != Bundle.UNINSTALLED) {
                    logger.debug("Parsing module {} since it is dependent on just resolved module {}", bundle.getSymbolicName(), key);
                    resolve(bundle);
                }
            }
            toBeResolved.remove(key);
        }
    }

    private synchronized void unresolve(Bundle bundle) {
        setModuleState(bundle, ModuleState.State.INSTALLED, null);

        // Unload .dsl rules files for bundle
        rulesDSLBundleObserver.removingEntries(bundle, null);

        JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (jahiaTemplatesPackage != null) {
            jahiaTemplatesPackage.setClassLoader(null);
        }
    }

    private synchronized void starting(Bundle bundle) {

        ModuleState.State moduleState = getModuleState(bundle).getState();
        if (isInvalid(moduleState)) {
            return;
        }

        setModuleState(bundle, ModuleState.State.STARTING, null);
        JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupById(bundle.getSymbolicName());
        if (jahiaTemplatesPackage != null && jahiaTemplatesPackage.getBundle() != null && jahiaTemplatesPackage.getBundle().getState() == Bundle.ACTIVE) {
            try {
                logger.info("Stopping module {} before activating {}...", getDisplayName(jahiaTemplatesPackage.getBundle()), getDisplayName(bundle));
                jahiaTemplatesPackage.getBundle().stop();
            } catch (BundleException e) {
                logger.info("--- Cannot stop module " + getDisplayName(jahiaTemplatesPackage.getBundle()), e);
            }
        }
        for (Map.Entry<Bundle, ModuleState> entry : moduleStates.entrySet()) {
            Bundle otherBundle = entry.getKey();
            if (otherBundle != bundle && otherBundle.getState() == Bundle.ACTIVE && otherBundle.getSymbolicName().equals(bundle.getSymbolicName())) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Stopping module {} before starting {}", getDisplayName(otherBundle), getDisplayName(bundle));
                    }
                    otherBundle.stop();
                } catch (BundleException e) {
                    logger.info("--- Cannot stop module " + getDisplayName(otherBundle) + " while starting " + getDisplayName(bundle), e);
                }
            }
        }
    }

    private synchronized void start(final Bundle bundle) {

        final JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (jahiaTemplatesPackage == null) {
            logger.info("--- Bundle {} is starting but has not yet been parsed. Delaying its startup.", bundle);
            return;
        }

        ModuleState.State state = getModuleState(bundle).getState();
        if (isInvalid(state)) {
            return;
        }

        if (!checkImported(jahiaTemplatesPackage)) {
            setModuleState(bundle, ModuleState.State.WAITING_TO_BE_IMPORTED, null);
            return;
        }


        logger.info("--- Start DX OSGi bundle {} --", getDisplayName(bundle));
        long startTime = System.currentTimeMillis();

        jasperInitializer.onBundleAdded(bundle);

        templatePackageRegistry.register(jahiaTemplatesPackage);
        jahiaTemplatesPackage.setActiveVersion(true);
        templatesService.fireTemplatePackageRedeployedEvent(jahiaTemplatesPackage);

        // scan for resource and call observers
        scanBundleAndPerformObservers(bundle, jahiaTemplatesPackage, startExtensionObservers);
        registerHttpResources(bundle);

        long totalTime = System.currentTimeMillis() - startTime;

        if (initializedBundles.remove(bundle)) {

            //auto deploy bundle according to bundle configuration
            try {

                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, null, null, session -> {
                    templatesService.autoInstallModulesToSites(jahiaTemplatesPackage, session);
                    session.save();
                    return null;
                });
            } catch (RepositoryException e) {
                logger.error("Error while initializing module content for module " + jahiaTemplatesPackage, e);
            }
        }

        // check for script engine factories
        String errorMessage = null;
        try {
            scriptEngineManager.addScriptEngineFactoriesIfNeeded(bundle);
        } catch (Exception e) {
            logger.error("Unable to add script engine factories", e);
            errorMessage = e.getMessage();
        }

        logger.info("--- Finished starting DX OSGi bundle {} in {}ms --", getDisplayName(bundle), totalTime);

        if (isBundleProcessedByBlueprintExtender(bundle)) {
            setModuleState(bundle, ModuleState.State.SPRING_STARTING, errorMessage);
            try {
                @SuppressWarnings("resource") final AbstractApplicationContext contextToStartForModule = BundleUtils
                        .getContextToStartForModule(bundle);
                if (contextToStartForModule != null) {
                    contextToStartForModule.refresh();
                }
            } catch (Exception e) {
                logger.error("Unable to create application context for [" + bundle.getSymbolicName() + "]", e);
            }
        } else {
            if (getModuleState(bundle).getState() != ModuleState.State.ERROR_WITH_RULES) {
                setModuleState(bundle, ModuleState.State.STARTED, errorMessage);
            }
        }

        flushOutputCachesForModule(jahiaTemplatesPackage);
    }

    private void scanBundleAndPerformObservers(Bundle bundle, JahiaTemplatesPackage jahiaTemplatesPackage, ExtensionObserverRegistry extensionObservers) {
        boolean hasSpringFile = hasSpringFile(bundle);
        for (final Map.Entry<BundleURLScanner, BundleObserver<URL>> scannerAndObserver : extensionObservers.entrySet()) {
            scanBundleAndPerformObserver(bundle, jahiaTemplatesPackage, scannerAndObserver.getKey(), scannerAndObserver.getValue(),
                    DRL_SCANNER.equals(scannerAndObserver.getKey()) && hasSpringFile);
        }
    }

    private void scanBundleAndPerformObserver(Bundle bundle, JahiaTemplatesPackage jahiaTemplatesPackage, BundleURLScanner scanner,
                                 BundleObserver<URL> observer, boolean postPoneObserverAfterSpringStart) {
        final List<URL> foundURLs = scanner.scan(bundle);
        if (!foundURLs.isEmpty()) {
            // rules may use Global objects from his own spring beans, so we delay the rules registration until the spring context is initialized
            // to insure that potential global objects are available before rules executions
            if (postPoneObserverAfterSpringStart) {
                logger.info("--- Rules registration for bundle {} has been delayed until its Spring context is initialized --", getDisplayName(bundle));
                jahiaTemplatesPackage.doExecuteAfterContextInitialized(context -> {
                    synchronized (Activator.this) {
                        performObserver(bundle, jahiaTemplatesPackage, observer, foundURLs);
                    }
                });
            } else {
                performObserver(bundle, jahiaTemplatesPackage, observer, foundURLs);
            }
        }
    }

    private void performObserver(final Bundle bundle, final JahiaTemplatesPackage jahiaTemplatesPackage, BundleObserver<URL> observer,
                                 final List<URL> foundURLs) {
        try {
            observer.addingEntries(bundle, foundURLs);
        } catch (Exception e) {
            String bundleDisplayName = jahiaTemplatesPackage.getId() + " v" + jahiaTemplatesPackage.getVersion();
            logger.error("--- Error parsing rules for DX OSGi bundle " + bundleDisplayName, e);
            setModuleState(bundle, ModuleState.State.ERROR_WITH_RULES, e);

            if (stopBundleWithErrorInRules) {
                // we execute bundle.stop() in another thread to prevent contention
                getExecutorService().submit(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        logger.info("--- The DX OSGi bundle {} v{} will be stopped", jahiaTemplatesPackage.getId(),
                                jahiaTemplatesPackage.getVersion());
                        try {
                            bundle.stop();
                            logger.info("...bundle {} stopped", bundleDisplayName);
                        } catch (BundleException be) {
                            logger.error("Unable to stop bundle " + bundleDisplayName, e);
                        }
                        return null;
                    }
                });
            }
        }
    }

    private boolean checkImported(final JahiaTemplatesPackage jahiaTemplatesPackage) {

        try {

            boolean imported = JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, null, null, new JCRCallback<Boolean>() {

                @Override
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

    private synchronized void stopping(Bundle bundle) {

        ModuleState.State moduleState = getModuleState(bundle).getState();
        if (isInvalid(moduleState)) {
            return;
        }

        logger.info("--- Stopping DX OSGi bundle {} --", getDisplayName(bundle));
        if (!keepPreviousStateOnStop(bundle)) {
            setModuleState(bundle, ModuleState.State.STOPPING, null);
        }

        long startTime = System.currentTimeMillis();

        JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (jahiaTemplatesPackage == null || !jahiaTemplatesPackage.isActiveVersion()) {
            return;
        }

        if (JahiaContextLoaderListener.isRunning()) {

            templatePackageRegistry.unregister(jahiaTemplatesPackage);

            jahiaTemplatesPackage.setActiveVersion(false);
            templatesService.fireTemplatePackageRedeployedEvent(jahiaTemplatesPackage);

            if (jahiaTemplatesPackage.getContext() != null) {
                jahiaTemplatesPackage.setContext(null);
            }

            // scan for resource and call observers
            for (Map.Entry<BundleURLScanner, BundleObserver<URL>> scannerAndObserver : startExtensionObservers.entrySet()) {
                List<URL> foundURLs = scannerAndObserver.getKey().scan(bundle);
                if (!foundURLs.isEmpty()) {
                    scannerAndObserver.getValue().removingEntries(bundle, foundURLs);
                }
            }

            // deal with script engine factories
            scriptEngineManager.removeScriptEngineFactoriesIfNeeded(bundle);

            flushOutputCachesForModule(jahiaTemplatesPackage);

            ServiceTracker<HttpService, HttpService> tracker = bundleHttpServiceTrackers.remove(bundle);

            if (tracker != null) {
                tracker.close();
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("--- Finished stopping DX OSGi bundle {} in {}ms --", getDisplayName(bundle), totalTime);
    }

    private boolean keepPreviousStateOnStop(Bundle bundle) {
        ModuleState moduleState = getModuleState(bundle);
        return moduleState != null && (moduleState.getState() == State.SPRING_NOT_STARTED || moduleState.getState() == State.ERROR_WITH_RULES);
    }

    private void flushOutputCachesForModule(final JahiaTemplatesPackage pkg) {
        if (hasImports(pkg) || hasViewFiles(pkg)) {
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                    @Override
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        List<JCRSiteNode> sitesNodeList = JahiaSitesService.getInstance().getSitesNodeList(session);
                        Set<String> pathsToFlush = new HashSet<>();
                        for (JCRSiteNode site : sitesNodeList) {
                            Set<String> installedModules = site.getInstalledModulesWithAllDependencies();
                            if (installedModules.contains(pkg.getId()) || installedModules.contains(pkg.getName())) {
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
    }

    private boolean hasImports(JahiaTemplatesPackage jahiaTemplatesPackage) {
        if (jahiaTemplatesPackage.getInitialImports().isEmpty()) {
            // check for initial imports
            Bundle bundle = jahiaTemplatesPackage.getBundle();
            Enumeration<URL> importXMLEntryEnum = bundle.findEntries("META-INF", "import*.xml", false);
            if (importXMLEntryEnum == null || !importXMLEntryEnum.hasMoreElements()) {
                importXMLEntryEnum = bundle.findEntries("META-INF", "import*.zip", false);
                if (importXMLEntryEnum == null || !importXMLEntryEnum.hasMoreElements()) {
                    // no templates -> no need to flush caches
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasViewFiles(JahiaTemplatesPackage jahiaTemplatesPackage) {
        for (Map.Entry<BundleURLScanner, BundleObserver<URL>> scannerAndObserver : startExtensionObservers.entrySet()) {
            List<URL> foundURLs = scannerAndObserver.getKey().scan(jahiaTemplatesPackage.getBundle());
            if (!foundURLs.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private synchronized void stopped(Bundle bundle) {
        jasperInitializer.onBundleRemoved(bundle);

        // Ensure context is reset
        BundleUtils.setContextToStartForModule(bundle, null);

        ModuleState.State moduleState = getModuleState(bundle).getState();
        if (isInvalid(moduleState)) {
            return;
        }

        if (!keepPreviousStateOnStop(bundle)) {
            setModuleState(bundle, ModuleState.State.RESOLVED, null);
        }
    }

    private void registerHttpResources(final Bundle bundle) {

        final String displayName = getDisplayName(bundle);

        if (!BundleHttpResourcesTracker.getStaticResources(bundle).isEmpty() || !BundleHttpResourcesTracker.getJsps(bundle).isEmpty()) {
            logger.debug("Found HTTP resources for bundle {}." + " Will launch service tracker for HttpService", displayName);
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
        List<Resource> importFiles = new ArrayList<>();
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
        Map<ModuleState.State, Set<Bundle>> modulesByState = new TreeMap<>();
        for (Map.Entry<Bundle, ModuleState> entry : moduleStates.entrySet()) {
            ModuleState.State moduleState = entry.getValue().getState();
            Set<Bundle> bundlesInState = modulesByState.get(moduleState);
            if (bundlesInState == null) {
                bundlesInState = new TreeSet<>();
            }
            bundlesInState.add(entry.getKey());
            modulesByState.put(moduleState, bundlesInState);
        }
        return modulesByState;
    }

    public ModuleState getModuleState(Bundle bundle) {
        return moduleStates.computeIfAbsent(bundle, bundle1 -> new ModuleState());
    }

    public void setModuleState(Bundle bundle, ModuleState.State state, Object details) {
        ModuleState moduleState = getModuleState(bundle);
        moduleState.setState(state);
        moduleState.setDetails(details);
    }

    /**
     * Registers the transformation services.
     *
     * @param context the OSGi bundle context object
     */
    private void registerUrlTransformers(BundleContext context) throws IOException {

        // register protocol handlers
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(URLConstants.URL_HANDLER_PROTOCOL, URL_PROTOCOL_DX);
        props.put(Constants.SERVICE_DESCRIPTION, "URL stream protocol handler for DX modules that handles bundle storage and dependencies between modules");
        props.put(Constants.SERVICE_VENDOR, Jahia.VENDOR_NAME);
        serviceRegistrations.add(context.registerService(URLStreamHandlerService.class, new DxModuleURLStreamHandler(), props));
        props = new Hashtable<>();
        props.put(URLConstants.URL_HANDLER_PROTOCOL, URL_PROTOCOL_MODULE_DEPENDENCIES);
        props.put(Constants.SERVICE_DESCRIPTION, "URL stream protocol handler for DX modules that handles dependencies between them using OSGi capabilities");
        props.put(Constants.SERVICE_VENDOR, Jahia.VENDOR_NAME);
        serviceRegistrations.add(context.registerService(URLStreamHandlerService.class, new ModuleDependencyURLStreamHandler(), props));

        // register mvn URL handler to verride the default from pax
        props.put(URLConstants.URL_HANDLER_PROTOCOL, "mvn");
        props.put(Constants.SERVICE_DESCRIPTION, "Override mvn URLs for Java 11");
        props.put(Constants.SERVICE_VENDOR, Jahia.VENDOR_NAME);
        props.put(Constants.SERVICE_RANKING, 1000);
        ServiceReference configAdminServiceRef = context.getServiceReference(ConfigurationAdmin.class.getName());
        ConfigurationAdmin configAdminService = null;
        if(configAdminServiceRef != null) {
            configAdminService = (ConfigurationAdmin) context.getService( configAdminServiceRef );
        }
        serviceRegistrations.add(context.registerService(URLStreamHandlerService.class, new MavenURLStreamHandler(configAdminService), props));
    }

    private ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }

        return executorService;
    }

    private void shutdownExecutorService(long maxSecondsToWait) {
        if (executorService == null) {
            return;
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(maxSecondsToWait, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            // Ignore
        }

        executorService = null;
    }

    private void registerJackson2ConverterDelegate(Object delegate) {
        String beanName = JahiaMappingJackson2HttpMessageConverter.class.getSimpleName();
        ApplicationContext ctx = SpringContextSingleton.getInstance().getContext();
        if (ctx.containsBean(beanName)) {
            ((JahiaMappingJackson2HttpMessageConverter) ctx.getBean(beanName)).setDelegate(delegate);
        }
    }


    private boolean isBundleProcessedByBlueprintExtender(Bundle bundle) {
        boolean hasSpringFile = hasSpringFile(bundle);
        if (!hasSpringFile) {
            return false;
        }
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        if (wiring != null) {
            for (BundleWire wire : wiring.getRequiredWires(ExtenderNamespace.EXTENDER_NAMESPACE)) {
                Object object = wire.getCapability().getAttributes().get(ExtenderNamespace.EXTENDER_NAMESPACE);
                if (EXTENDER_CAPABILITY_NAME.equals(object)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected synchronized void handlePatches(BundleEvent event) {
        String eventType = status.get(event.getType());

        if (eventType != null) {
            List<Resource> migrationScripts = getMigrationScripts(event.getBundle(), eventType);
            if (!migrationScripts.isEmpty()) {
                try {
                    JSONObject moduleScriptsStatus = BundleInfoJcrHelper.getModuleScriptsStatus(event.getBundle().getSymbolicName());

                    List<Resource> filteredMigrationScripts = migrationScripts.stream().filter(ThrowingPredicate.unchecked(resource ->
                            !moduleScriptsStatus.has(resource.getURI().getPath()) || moduleScriptsStatus.get(resource.getURI().getPath()).equals(KEEP)
                    )).collect(Collectors.toList());

                    if (!filteredMigrationScripts.isEmpty()) {
                        Patcher.getInstance().executeScripts(filteredMigrationScripts, eventType, ThrowingBiConsumer.unchecked((resource, result) -> {
                            moduleScriptsStatus.put(resource.getURI().getPath(), result);
                            logger.info("Execution result for {} : {}", resource, result);
                        }));

                        BundleInfoJcrHelper.storeModuleScriptStatus(event.getBundle().getSymbolicName(), moduleScriptsStatus);
                    }
                } catch (RepositoryException e) {
                    logger.error("Cannot execute scripts", e);
                }
            }
        }
    }

    private List<Resource> getMigrationScripts(Bundle bundle, String eventType) {
        Enumeration<URL> migrationScriptsURLs = bundle.findEntries("META-INF/patches", "*." + eventType + ".*", true);
        if (migrationScriptsURLs == null) {
            return Collections.emptyList();
        }
        return Collections.list(migrationScriptsURLs).stream().map(UrlResource::new)
                .sorted(RESOURCE_COMPARATOR)
                .collect(Collectors.toList());
    }
}
