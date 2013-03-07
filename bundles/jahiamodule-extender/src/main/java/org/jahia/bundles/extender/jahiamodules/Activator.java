package org.jahia.bundles.extender.jahiamodules;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.fileinstall.ArtifactListener;
import org.apache.felix.fileinstall.ArtifactTransformer;
import org.apache.felix.service.command.CommandProcessor;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.BundleResource;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.scripting.bundle.BundleScriptResolver;
import org.jahia.services.templates.*;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.ops4j.pax.swissbox.extender.BundleURLScanner;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Activator for Jahia Modules extender
 */
public class Activator implements BundleActivator {

    public static enum ModuleState {
        UNINSTALLED, UNRESOLVED, RESOLVED, WAITING_TO_BE_PARSED, PARSED, INSTALLED, UPDATED, STOPPED, STOPPING, STARTING, WAITING_TO_BE_STARTED, ERROR_DURING_START, STARTED;
    }

    private static Logger logger = LoggerFactory.getLogger(Activator.class);

    private static final BundleURLScanner CND_SCANNER = new BundleURLScanner("META-INF", "*.cnd", false);
    
    private static final Comparator<Resource> IMPORT_FILE_COMPARATOR = new Comparator<Resource>() {
        public int compare(Resource o1, Resource o2) {
            return StringUtils.substringBeforeLast(o1.getFilename(), ".").compareTo(StringUtils.substringBeforeLast(o2.getFilename(), "."));
        }
    };
    
    private CndBundleObserver cndBundleObserver = null;
    private List<ServiceRegistration> serviceRegistrations = new ArrayList<ServiceRegistration>();
    private BundleListener bundleListener = null;
    private Set<Bundle> installedBundles = new HashSet<Bundle>();
    private Map<Bundle, JahiaTemplatesPackage> registeredBundles = new HashMap<Bundle, JahiaTemplatesPackage>();
    private Map<Bundle, ServiceTracker> bundleHttpServiceTrackers = new HashMap<Bundle, ServiceTracker>();
    private JahiaTemplateManagerService templatesService;
    private TemplatePackageRegistry templatePackageRegistry = null;
    private TemplatePackageDeployer templatePackageDeployer = null;

    private Map<BundleURLScanner, BundleObserver<URL>> extensionObservers = new LinkedHashMap<BundleURLScanner, BundleObserver<URL>>();
    private Map<String,List<Bundle>> toBeParsed = new HashMap<String, List<Bundle>>();
    private Map<String,List<Bundle>> toBeStarted = new HashMap<String, List<Bundle>>();

    private Map<Bundle, ModuleState> moduleStates = new TreeMap<Bundle, ModuleState>();

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("== Starting Jahia Extender ============================================================== ");
        long startTime = System.currentTimeMillis();

        // obtain service instances
        templatesService = (JahiaTemplateManagerService) SpringContextSingleton.getBean("JahiaTemplateManagerService");
        templatePackageDeployer = templatesService.getTemplatePackageDeployer();
        templatePackageRegistry = templatesService.getTemplatePackageRegistry();

        // register rule observers
        RulesBundleObserver rulesBundleObserver = new RulesBundleObserver();
        extensionObservers.put(new BundleURLScanner("META-INF", "*.dsl", false), rulesBundleObserver);
        extensionObservers.put(new BundleURLScanner("META-INF", "*.drl", false), rulesBundleObserver);

        BundleScriptResolver bundleScriptResolver = (BundleScriptResolver) SpringContextSingleton.getBean("BundleScriptResolver");

        // register view script observers 
        final ScriptBundleObserver scriptBundleObserver = new ScriptBundleObserver(bundleScriptResolver);
        // add scanners for all types of scripts of the views to register them in the BundleScriptResolver
        for (String scriptExtension : bundleScriptResolver.getScriptExtensionsOrdering()) {
            extensionObservers.put(new BundleURLScanner("/", "*." + scriptExtension, true), scriptBundleObserver);
        }

        extensionObservers.put(new BundleURLScanner("/", "flow.xml", true), new BundleObserver<URL>() {
            @Override
            public void addingEntries(Bundle bundle, List<URL> entries) {
                for (URL entry : entries) {
                    try {
                        URL parent = new URL(entry.getProtocol(), entry.getHost(), entry.getPort(), new File(entry.getFile()).getParent());
                        scriptBundleObserver.addingEntries(bundle, Arrays.asList(parent));
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
                        scriptBundleObserver.removingEntries(bundle, Arrays.asList(parent));
                    } catch (MalformedURLException e) {
                        //
                    }
                }
            }
        });


        // we won't register CND observer, but will rather call it manually
        cndBundleObserver = new CndBundleObserver();
        
        // register Jahia legacy module transformer
        serviceRegistrations.add(context.registerService(
                new String[]{ArtifactTransformer.class.getName(), ArtifactListener.class.getName()},
                new JahiaLegacyModuleTransformer(),
                new Hashtable<String, Object>()
        ));

        // add listener for other bundle life cycle events
        setupBundleListener(context);

        // parse existing bundles
        for (Bundle bundle : context.getBundles()) {
            parseBundle(bundle);
        }

        registerShellCommands(context);

        logger.info("== Jahia Extender started in {}ms ============================================================== ", System.currentTimeMillis() - startTime);

    }

    private void registerShellCommands(BundleContext context) {
        Dictionary<String, Object> dict = new Hashtable<String, Object>();
        dict.put(CommandProcessor.COMMAND_SCOPE, "jahia");
        dict.put(CommandProcessor.COMMAND_FUNCTION, new String[] {"modules"});
        ShellCommands shellCommands = new ShellCommands(this);
        serviceRegistrations.add(context.registerService(ShellCommands.class.getName(), shellCommands, dict));
    }

    private synchronized void setupBundleListener(BundleContext context) {
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
                            moduleStates.put(bundle, ModuleState.INSTALLED);
                            install(bundle);
                            break;
                        case BundleEvent.UPDATED:
                            BundleUtils.unregisterModule(bundle);
                            moduleStates.put(bundle, ModuleState.UPDATED);
                            update(bundle);
                            break;
                        case BundleEvent.RESOLVED:
                            moduleStates.put(bundle, ModuleState.RESOLVED);
                            resolve(bundle);
                            break;
                        case BundleEvent.STARTING:
                            moduleStates.put(bundle, ModuleState.STARTING);
                            starting(bundle);
                            break;
                        case BundleEvent.STARTED:
                            moduleStates.put(bundle, ModuleState.STARTED);
                            start(bundle);
                            break;
                        case BundleEvent.STOPPING:
                            moduleStates.put(bundle, ModuleState.STOPPING);
                            stopping(bundle);
                            break;
                        case BundleEvent.UNRESOLVED:
                            moduleStates.put(bundle, ModuleState.UNRESOLVED);
                            unresolve(bundle);
                            break;
                        case BundleEvent.UNINSTALLED:
                            BundleUtils.unregisterModule(bundle);
                            moduleStates.put(bundle, ModuleState.UNINSTALLED);
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

        logger.info("== Stopping Jahia Extender ============================================================== ");
        long startTime = System.currentTimeMillis();

        context.removeBundleListener(bundleListener);

        for (Bundle bundle : new HashSet<Bundle>(registeredBundles.keySet())) {
            unresolve(bundle);
        }

        bundleListener = null;
        registeredBundles.clear();

        for (ServiceRegistration serviceRegistration : serviceRegistrations) {
            try {
                serviceRegistration.unregister();
            } catch (IllegalStateException e) {
                logger.warn(e.getMessage());
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("== Jahia Extender stopped in {}ms ============================================================== ", totalTime);

    }

    private synchronized void install(final Bundle bundle) {
        installedBundles.add(bundle);
        parseBundle(bundle);
    }

    private synchronized void update(final Bundle bundle) {
        installedBundles.add(bundle);
        parseBundle(bundle);
    }

    private synchronized void uninstall(Bundle bundle) {
        logger.info("--- Uninstalling Jahia OSGi bundle {} --", getDisplayName(bundle));
        long startTime = System.currentTimeMillis();

        final JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (jahiaTemplatesPackage != null) {
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, null, new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        templatePackageDeployer.clearModuleNodes(jahiaTemplatesPackage, session);
                        return null;
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Error while initializing module content for module " + jahiaTemplatesPackage, e);
            }
        }
        installedBundles.remove(bundle);
        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("--- Finished uninstalling Jahia OSGi bundle {} in {}ms --", getDisplayName(bundle), totalTime);
    }

    private void parseBundle(Bundle bundle) {
        final JahiaTemplatesPackage pkg = BundleUtils.isJahiaModuleBundle(bundle) ? BundleUtils.getModule(bundle)
                : null;
        
        if (null == pkg) {
            // is not a Jahia module -> skip
            return;
        }

        List<String> dependsList = pkg.getDepends();
        if (!dependsList.contains("default") && !dependsList.contains("Default Jahia Templates")
                && !"assets".equals(pkg.getRootFolder()) && !"default".equals(pkg.getRootFolder())) {
            dependsList.add("default");
        }

        for (String depend : dependsList) {
            Set<ModuleVersion> m = templatePackageRegistry.getAvailableVersionsForModule(depend);
            if (m.isEmpty()) {
                if (!toBeParsed.containsKey(depend)) {
                    toBeParsed.put(depend, new ArrayList<Bundle>());
                }
                logger.debug("Delaying module {} parsing because it depends on module {} that is not yet parsed.",
                        bundle.getSymbolicName(), depend);
                moduleStates.put(bundle, ModuleState.WAITING_TO_BE_PARSED);
                toBeParsed.get(depend).add(bundle);
                return;
            }
        }

        logger.info("--- Parsing Jahia OSGi bundle {} v{} --", pkg.getRootFolder(), pkg.getVersion());
        
        registeredBundles.put(bundle, pkg);
        templatePackageRegistry.registerPackageVersion(pkg);

        boolean latestDefinitions = NodeTypeRegistry.getInstance().isLatestDefinitions(bundle.getSymbolicName(), pkg.getVersion());
        if (latestDefinitions) {
            List<URL> foundURLs = CND_SCANNER.scan(bundle);
            if (!foundURLs.isEmpty()) {
                cndBundleObserver.addingEntries(bundle, foundURLs);
            }
        }

        logger.info("--- Done parsing Jahia OSGi bundle {} v{} --", pkg.getRootFolder(), pkg.getVersion());

        moduleStates.put(bundle, ModuleState.PARSED);

        if (installedBundles.contains(bundle)) {
            logger.info("--- Installing Jahia OSGi bundle {} v{} --", pkg.getRootFolder(), pkg.getVersion());
            installedBundles.remove(bundle);
            scanForImportFiles(bundle, pkg);

            try {
                JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, null, new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        templatePackageDeployer.initializeModuleContent(pkg, session);
                        return null;
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Error while initializing module content for module " + pkg, e);
            }
            logger.info("--- Done installing Jahia OSGi bundle {} v{} --", pkg.getRootFolder(), pkg.getVersion());
            moduleStates.put(bundle, ModuleState.INSTALLED);

        }

        if (latestDefinitions) {
            parseDependantBundles(pkg.getRootFolder());
            parseDependantBundles(pkg.getName());
        }
    }

    private void parseDependantBundles(String key) {
        if (toBeParsed.get(key) != null) {
            for (Bundle bundle1 : toBeParsed.get(key)) {
                logger.debug("Parsing module " + bundle1.getSymbolicName() + " since it is dependent on just parsed module " + key);
                parseBundle(bundle1);
            }
            toBeParsed.remove(key);
        }
    }

    private void resolve(Bundle bundle) {
        // do nothing
    }

    private void unresolve(Bundle bundle) {
        // do nothing
    }

    private synchronized void starting(Bundle bundle) {
        JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByFileName(bundle.getSymbolicName());
        if (jahiaTemplatesPackage != null) {
            try {
                logger.info("Stopping module {} before activating new version...", getDisplayName(bundle));
                jahiaTemplatesPackage.getBundle().stop();
            } catch (BundleException e) {
                logger.info("--- Cannot stop previous version of module " + bundle.getSymbolicName(), e);
            }
        }
    }

    private synchronized void start(Bundle bundle) {
        final JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (jahiaTemplatesPackage == null) {
            for (Map.Entry<String, List<Bundle>> entry : toBeParsed.entrySet()) {
                if (entry.getValue().contains(bundle)) {
                    if (!toBeStarted.containsKey(entry.getKey())) {
                        toBeStarted.put(entry.getKey(), new ArrayList<Bundle>());
                    }
                    logger.debug("Delaying module {} startup because it depends on module {} that is not yet started.", bundle.getSymbolicName(), entry.getKey());
                    if (!toBeStarted.get(entry.getKey()).contains(bundle)) {
                        toBeStarted.get(entry.getKey()).add(bundle);
                    }
                    moduleStates.put(bundle, ModuleState.WAITING_TO_BE_STARTED);
                    return;
                }
            }
        }
        List<String> dependsList = jahiaTemplatesPackage.getDepends();
        if (!dependsList.contains("default") && !dependsList.contains("Default Jahia Templates") && !bundle.getSymbolicName().equals("assets")&& !bundle.getSymbolicName().equals("default")) {
            dependsList.add("default");
        }

        for (String depend : dependsList) {
            JahiaTemplatesPackage pack = templatePackageRegistry.lookupByFileName(depend);
            if (pack == null) {
                pack = templatePackageRegistry.lookup(depend);
            }
            if (pack == null) {
                if (!toBeStarted.containsKey(depend)) {
                    toBeStarted.put(depend, new ArrayList<Bundle>());
                }
                logger.debug("Delaying module {} startup because it depends on module {} that is not yet started.", bundle.getSymbolicName(), depend);
                toBeStarted.get(depend).add(bundle);
                moduleStates.put(bundle, ModuleState.WAITING_TO_BE_STARTED);
                return;
            }
        }

        logger.info("--- Start Jahia OSGi bundle {} --", getDisplayName(bundle));
        long startTime = System.currentTimeMillis();

        templatePackageRegistry.register(jahiaTemplatesPackage);

        jahiaTemplatesPackage.setActiveVersion(true);

        // scan for resource and call observers
        for (Map.Entry<BundleURLScanner, BundleObserver<URL>> scannerAndObserver : extensionObservers.entrySet()) {
            List<URL> foundURLs = scannerAndObserver.getKey().scan(bundle);
            if (!foundURLs.isEmpty()) {
                scannerAndObserver.getValue().addingEntries(bundle, foundURLs);
            }
        }

        registerHttpResources(bundle);

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("--- Finished starting Jahia OSGi bundle {} in {}ms --", getDisplayName(bundle), totalTime);
        moduleStates.put(bundle, ModuleState.STARTED);

        startDependantBundles(jahiaTemplatesPackage.getRootFolder());
        startDependantBundles(jahiaTemplatesPackage.getName());

        //auto deploy bundle according to bundle configuration
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, null, new JCRCallback<Boolean>() {
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

    private String getDisplayName(Bundle bundle) {
        return BundleUtils.getDisplayName(bundle);
    }

    private void startDependantBundles(String key) {
        if (toBeStarted.get(key) != null) {
            List<Bundle> startedBundles = new ArrayList<Bundle>();
            for (Bundle bundle : toBeStarted.get(key)) {
                logger.debug("Starting module " + bundle.getSymbolicName() + " since it is dependent on just started module " + key);
                moduleStates.put(bundle, ModuleState.STARTING);
                try {
                    start(bundle);
                    startedBundles.add(bundle);
                } catch (Throwable t) {
                    logger.error("Error during startup of dependent module " + bundle.getSymbolicName() + ", module is not started !", t);
                    moduleStates.put(bundle, ModuleState.ERROR_DURING_START);
                }
            }
            toBeStarted.get(key).removeAll(startedBundles);
            if (toBeStarted.get(key).size() == 0) {
                toBeStarted.remove(key);
            }
        }
    }

    private synchronized void stopping(Bundle bundle) {
        logger.info("--- Stopping Jahia OSGi bundle {} --", getDisplayName(bundle));
        long startTime = System.currentTimeMillis();

        JahiaTemplatesPackage JahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (JahiaTemplatesPackage == null) {
            return;
        }

        templatePackageRegistry.unregister(JahiaTemplatesPackage);
        JahiaTemplatesPackage.setActiveVersion(false);

        if (JahiaTemplatesPackage.getContext() != null) {
            JahiaTemplatesPackage.setContext(null);
        }

        // scan for resource and call observers
        for (Map.Entry<BundleURLScanner, BundleObserver<URL>> scannerAndObserver : extensionObservers.entrySet()) {
            List<URL> foundURLs = scannerAndObserver.getKey().scan(bundle);
            if (!foundURLs.isEmpty()) {
                scannerAndObserver.getValue().removingEntries(bundle, foundURLs);
            }
        }

        if (bundleHttpServiceTrackers.containsKey(bundle)) {
            bundleHttpServiceTrackers.remove(bundle).close();
        }

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("--- Finished stopping Jahia OSGi bundle {} in {}ms --", getDisplayName(bundle), totalTime);
    }

    private void registerHttpResources(final Bundle bundle) {
        final String displayName = getDisplayName(bundle);

        if (!BundleHttpResourcesTracker.getStaticResources(bundle).isEmpty()
                || !BundleHttpResourcesTracker.getJsps(bundle).isEmpty()) {
            logger.debug("Found HTTP resources for bundle {}." + " Will launch service tracker for HttpService",
                    displayName);
            if (bundleHttpServiceTrackers.containsKey(bundle)) {
                bundleHttpServiceTrackers.remove(bundle).close();
            }
            ServiceTracker bundleServiceTracker = new BundleHttpResourcesTracker(bundle);
            bundleServiceTracker.open();
            bundleHttpServiceTrackers.put(bundle, bundleServiceTracker);
        } else {
            logger.debug("No HTTP resources found for bundle {}", displayName);
        }
    }

    private void scanForImportFiles(Bundle bundle, JahiaTemplatesPackage jahiaTemplatesPackage) {
        List<Resource> importFiles = new ArrayList<Resource>();
        @SuppressWarnings("unchecked")
        Enumeration<URL> importXMLEntryEnum = bundle.findEntries("META-INF", "import*.xml", false);
        if (importXMLEntryEnum != null) {
            while (importXMLEntryEnum.hasMoreElements()) {
                importFiles.add(new BundleResource(importXMLEntryEnum.nextElement(), bundle));
            }
        }
        @SuppressWarnings("unchecked")
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

    public Map<Bundle, JahiaTemplatesPackage> getRegisteredBundles() {
        return registeredBundles;
    }

    public Map<Bundle, ModuleState> getModuleStates() {
        return moduleStates;
    }

    public Map<ModuleState, Set<Bundle>> getModulesByState() {
        Map<Activator.ModuleState, Set<Bundle>> modulesByState = new TreeMap<Activator.ModuleState, Set<Bundle>>();
        for (Bundle bundle : moduleStates.keySet()) {
            Activator.ModuleState moduleState = moduleStates.get(bundle);
            Set<Bundle> bundlesInState = modulesByState.get(moduleState);
            if (bundlesInState == null) {
                bundlesInState = new TreeSet<Bundle>();
            }
            bundlesInState.add(bundle);
            modulesByState.put(moduleState, bundlesInState);
        }
        return modulesByState;
    }

    public Map<String, List<Bundle>> getToBeParsed() {
        return toBeParsed;
    }

    public Map<String, List<Bundle>> getToBeStarted() {
        return toBeStarted;
    }
}
