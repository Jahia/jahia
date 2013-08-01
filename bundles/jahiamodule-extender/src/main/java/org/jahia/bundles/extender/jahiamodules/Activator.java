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

package org.jahia.bundles.extender.jahiamodules;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.fileinstall.ArtifactListener;
import org.apache.felix.fileinstall.ArtifactTransformer;
import org.apache.felix.service.command.CommandProcessor;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.BundleResource;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.scripting.bundle.BundleScriptResolver;
import org.jahia.services.templates.*;
import org.jahia.settings.SettingsBean;
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

    private BundleStarter bundleStarter;

    private Map<Bundle, ModuleState> moduleStates = new TreeMap<Bundle, ModuleState>();

    @Override
    public void start(final BundleContext context) throws Exception {
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

        bundleStarter = new BundleStarter();

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
            parseBundle(bundle, false);
        }

        registerShellCommands(context);
        templatesService.setModuleStates(moduleStates);
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
        context.addFrameworkListener(bundleStarter);
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
                            setModuleState(bundle,ModuleState.State.INSTALLED, null);
                            boolean fromFileInstall = bundleEvent.getOrigin().getSymbolicName().equals("org.apache.felix.fileinstall");
                            install(bundle, fromFileInstall);
                            break;
                        case BundleEvent.UPDATED:
                            BundleUtils.unregisterModule(bundle);
                            setModuleState(bundle,ModuleState.State.UPDATED, null);
                            update(bundle);
                            break;
                        case BundleEvent.RESOLVED:
                            setModuleState(bundle,ModuleState.State.RESOLVED, null);
                            resolve(bundle);
                            break;
                        case BundleEvent.STARTING:
                            setModuleState(bundle,ModuleState.State.STARTING, null);
                            starting(bundle);
                            break;
                        case BundleEvent.STARTED:
                            setModuleState(bundle,ModuleState.State.STARTED, null);
                            start(bundle);
                            break;
                        case BundleEvent.STOPPING:
                            setModuleState(bundle,ModuleState.State.STOPPING, null);
                            stopping(bundle);
                            break;
                        case BundleEvent.STOPPED:
                            setModuleState(bundle,ModuleState.State.STOPPED, null);
                            stopped(bundle);
                            break;
                        case BundleEvent.UNRESOLVED:
                            setModuleState(bundle,ModuleState.State.UNRESOLVED, null);
                            unresolve(bundle);
                            break;
                        case BundleEvent.UNINSTALLED:
                            BundleUtils.unregisterModule(bundle);
                            moduleStates.remove(bundle);
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

    private synchronized void install(final Bundle bundle, boolean fromFileInstall) {
        installedBundles.add(bundle);
        parseBundle(bundle, fromFileInstall);
    }

    private synchronized void update(final Bundle bundle) {
        installedBundles.add(bundle);
        parseBundle(bundle, false);
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
            templatePackageRegistry.unregisterPackageVersion(jahiaTemplatesPackage);
        }
        installedBundles.remove(bundle);

        deleteBundleFileIfNeeded(bundle);
        
        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("--- Finished uninstalling Jahia OSGi bundle {} in {}ms --", getDisplayName(bundle), totalTime);
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

    private void parseBundle(Bundle bundle, boolean shouldAutoStart) {
        final JahiaTemplatesPackage pkg = BundleUtils.isJahiaModuleBundle(bundle) ? BundleUtils.getModule(bundle)
                : null;
        
        if (null == pkg) {
            // is not a Jahia module -> skip
            return;
        }

        pkg.setState(getModuleState(bundle));

        List<String> dependsList = pkg.getDepends();
        if (!dependsList.contains("default") && !dependsList.contains("Default Jahia Templates")
                && !"assets".equals(pkg.getRootFolder()) && !"default".equals(pkg.getRootFolder()) && !"jquery".equals(pkg.getRootFolder())) {
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
                setModuleState(bundle, ModuleState.State.WAITING_TO_BE_PARSED, depend);
                toBeParsed.get(depend).add(bundle);
                if (templatePackageRegistry.lookupByFileNameAndVersion(pkg.getName(), pkg.getVersion()) != null) {
                    templatePackageRegistry.unregisterPackageVersion(pkg);
                }
                return;
            }
        }

        logger.info("--- Parsing Jahia OSGi bundle {} v{} --", pkg.getRootFolder(), pkg.getVersion());

        registeredBundles.put(bundle, pkg);
        boolean newModuleDeployment = templatePackageRegistry.getAvailableVersionsForModule(bundle.getSymbolicName()).isEmpty();
        templatePackageRegistry.registerPackageVersion(pkg);

        boolean latestDefinitions = NodeTypeRegistry.getInstance().isLatestDefinitions(bundle.getSymbolicName(), pkg.getVersion());
        if (latestDefinitions) {
            List<URL> foundURLs = CND_SCANNER.scan(bundle);
            if (!foundURLs.isEmpty()) {
                cndBundleObserver.addingEntries(bundle, foundURLs);
            }
        }

        logger.info("--- Done parsing Jahia OSGi bundle {} v{} --", pkg.getRootFolder(), pkg.getVersion());

        setModuleState(bundle, ModuleState.State.PARSED, null);

        if (installedBundles.contains(bundle)) {
            logger.info("--- Installing Jahia OSGi bundle {} v{} --", pkg.getRootFolder(), pkg.getVersion());
            installedBundles.remove(bundle);

            scanForImportFiles(bundle, pkg);

            if (SettingsBean.getInstance().isProcessingServer()) {
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
            }
            logger.info("--- Done installing Jahia OSGi bundle {} v{} --", pkg.getRootFolder(), pkg.getVersion());
            setModuleState(bundle, ModuleState.State.INSTALLED, null);
            if (shouldAutoStart && newModuleDeployment) {
                bundleStarter.startBundle(bundle);
            }
        }

        if (latestDefinitions) {
            parseDependantBundles(pkg.getRootFolder(), shouldAutoStart);
            parseDependantBundles(pkg.getName(), shouldAutoStart);
        }
    }

    private void parseDependantBundles(String key, boolean shouldAutoStart) {
        if (toBeParsed.get(key) != null) {
            for (Bundle bundle1 : toBeParsed.get(key)) {
                if(bundle1.getState()!=Bundle.UNINSTALLED) {
                    logger.debug("Parsing module " + bundle1.getSymbolicName() + " since it is dependent on just parsed module " + key);
                    parseBundle(bundle1, shouldAutoStart);
                }
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
        for (Map.Entry<Bundle, ModuleState> entry : moduleStates.entrySet()) {
            if (entry.getKey().getSymbolicName().equals(bundle.getSymbolicName()) && entry.getKey() != bundle && entry.getValue().getState() == ModuleState.State.WAITING_TO_BE_STARTED) {
                try {
                    entry.getKey().stop();
                } catch (BundleException e) {
                    logger.info("--- Cannot stop previous version of module " + bundle.getSymbolicName(), e);
                }
            }
        }
    }

    private synchronized void start(Bundle bundle) {
        final JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (jahiaTemplatesPackage == null) {
            logger.error("--- Bundle "+bundle+" is starting but has not yet been parsed");
            bundleStarter.stopBundle(bundle);
            return;
        }
        List<String> dependsList = jahiaTemplatesPackage.getDepends();
        if (!dependsList.contains("default") && !dependsList.contains("Default Jahia Templates") && !bundle.getSymbolicName().equals("assets")&& !bundle.getSymbolicName().equals("default") && !bundle.getSymbolicName().equals("jquery")) {
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
                setModuleState(bundle, ModuleState.State.WAITING_TO_BE_STARTED, depend);
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
        setModuleState(bundle, ModuleState.State.STARTED, null);

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
                setModuleState(bundle, ModuleState.State.STARTING, key);
                try {
                    start(bundle);
                    startedBundles.add(bundle);
                } catch (Throwable t) {
                    logger.error("Error during startup of dependent module " + bundle.getSymbolicName() + ", module is not started !", t);
                    setModuleState(bundle, ModuleState.State.ERROR_DURING_START, t);
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

        JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (jahiaTemplatesPackage == null) {
            return;
        }

        for (JahiaTemplatesPackage dependant : templatePackageRegistry.getDependantModules(jahiaTemplatesPackage)) {
            if (!toBeStarted.containsKey(bundle.getSymbolicName())) {
                toBeStarted.put(bundle.getSymbolicName(), new ArrayList<Bundle>());
            }
            toBeStarted.get(bundle.getSymbolicName()).add(dependant.getBundle());
            setModuleState(dependant.getBundle(), ModuleState.State.WAITING_TO_BE_STARTED, bundle.getSymbolicName());
            stopping(dependant.getBundle());
        }

        templatePackageRegistry.unregister(jahiaTemplatesPackage);
        jahiaTemplatesPackage.setActiveVersion(false);

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

    private synchronized void stopped(Bundle bundle) {
        JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (jahiaTemplatesPackage.getContext() != null) {
            jahiaTemplatesPackage.setContext(null);
        }
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

    public Map<Bundle, JahiaTemplatesPackage> getRegisteredBundles() {
        return registeredBundles;
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

    public Map<String, List<Bundle>> getToBeParsed() {
        return toBeParsed;
    }

    public Map<String, List<Bundle>> getToBeStarted() {
        return toBeStarted;
    }

    private class BundleStarter implements FrameworkListener {

        private List<Bundle> toStart = Collections.synchronizedList(new ArrayList<Bundle>());
        private List<Bundle> toStop = Collections.synchronizedList(new ArrayList<Bundle>());

        @Override
        public synchronized void frameworkEvent(FrameworkEvent event) {
            switch (event.getType()) {
                case FrameworkEvent.PACKAGES_REFRESHED:
                    startAllBundles();
                    stopAllBundles();
            }
        }

        public void startBundle(Bundle bundle) {
            toStart.add(bundle);
        }

        public void stopBundle(Bundle bundle) {
            toStop.add(bundle);
        }

        public void startAllBundles() {
            List<Bundle> toStart = new ArrayList<Bundle>(this.toStart);
            this.toStart.removeAll(toStart);
            for (Bundle bundle : toStart) {
                try {
                    bundle.start();
                } catch (BundleException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopAllBundles() {
            List<Bundle> toStop = new ArrayList<Bundle>(this.toStop);
            this.toStop.removeAll(toStop);
            for (Bundle bundle : toStop) {
                try {
                    if (bundle.getState() != Bundle.UNINSTALLED) {
                        bundle.stop();
                    }
                } catch (BundleException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    ModuleState getModuleState(Bundle bundle) {
        if (!moduleStates.containsKey(bundle)) {
            moduleStates.put(bundle, new ModuleState());
        }
        return moduleStates.get(bundle);
    }

    void setModuleState(Bundle bundle, ModuleState.State state, Object details) {
        ModuleState moduleState = getModuleState(bundle);
        moduleState.setState(state);
        moduleState.setDetails(details);
    }

}
