package org.jahia.bundles.extender.jahiamodules;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.fileinstall.ArtifactListener;
import org.apache.felix.fileinstall.ArtifactUrlTransformer;
import org.jahia.bundles.extender.jahiamodules.render.BundleDispatcherServlet;
import org.jahia.bundles.extender.jahiamodules.render.BundleJSR223ScriptFactory;
import org.jahia.bundles.extender.jahiamodules.render.BundleRequestDispatcherScriptFactory;
import org.jahia.bundles.extender.jahiamodules.render.BundleScriptResolver;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.scripting.ScriptFactory;
import org.jahia.services.render.scripting.ScriptResolver;
import org.jahia.services.templates.*;
import org.jahia.utils.Patterns;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.ops4j.pax.swissbox.extender.BundleURLScanner;
import org.osgi.framework.*;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Activator for Jahia Modules extender
 */
public class Activator implements BundleActivator {

    private static Logger logger = LoggerFactory.getLogger(Activator.class);

    public static final String STATIC_RESOURCES_HEADERNAME = "Jahia-Static-Resources";

    CndBundleObserver cndBundleObserver = null;
    JspBundleObserver jspBundleObserver = null;
    ScriptBundleObserver scriptBundleObserver = null;
    boolean scriptResolverAlreadyInstalled = false;
    BundleScriptResolver bundleScriptResolver = null;
    RenderService renderService = null;
    JCRStoreService jcrStoreService = null;
    private List<ServiceRegistration> serviceRegistrations = new ArrayList<ServiceRegistration>();
    BundleListener bundleListener = null;
    Set<Bundle> installedBundles = new HashSet<Bundle>();
    Map<Bundle, JahiaBundleTemplatesPackage> registeredBundles = new HashMap<Bundle, JahiaBundleTemplatesPackage>();
    Map<Bundle, ServiceTracker> bundleHttpServiceTrackers = new HashMap<Bundle, ServiceTracker>();
    JahiaTemplateManagerService templatesService = null;
    TemplatePackageRegistry templatePackageRegistry = null;
    TemplatePackageDeployer templatePackageDeployer = null;
    WebApplicationContext parentWebApplicationContext = null;

    Map<Bundle, List<URL>> bundleResources = new HashMap<Bundle, List<URL>>();
    Map<BundleURLScanner, BundleObserver> extensionObservers = new HashMap<BundleURLScanner, BundleObserver>();
    private BundleURLScanner cndScanner;
    private Map<String,List<Bundle>> toBeParsed = new HashMap<String, List<Bundle>>();
    private Set<Bundle> toBeStarted = new HashSet<Bundle>();

    @Override
    public void start(BundleContext context) throws Exception {
        parentWebApplicationContext = (WebApplicationContext) SpringContextSingleton.getInstance().getContext();
        renderService = (RenderService) parentWebApplicationContext.getBean("RenderService");
        jcrStoreService = (JCRStoreService) parentWebApplicationContext.getBean("JCRStoreService");
        templatesService = (JahiaTemplateManagerService) parentWebApplicationContext.getBean("JahiaTemplateManagerService");
        templatePackageDeployer = templatesService.getTemplatePackageDeployer();
        templatePackageRegistry = templatesService.getTemplatePackageRegistry();

        cndBundleObserver = new CndBundleObserver();
        cndBundleObserver.setJcrStoreService(jcrStoreService);
        cndBundleObserver.setTemplatePackageRegistry(templatePackageRegistry);
        cndScanner = new BundleURLScanner("META-INF", "*.cnd", false);
        extensionObservers.put(cndScanner, cndBundleObserver);

        Collection<ScriptResolver> scriptResolvers = renderService.getScriptResolvers();
        for (ScriptResolver scriptResolver : scriptResolvers) {
            if (scriptResolver instanceof BundleScriptResolver) {
                scriptResolverAlreadyInstalled = true;
                bundleScriptResolver = (BundleScriptResolver) scriptResolver;
            }
        }
        BundleDispatcherServlet bundleDispatcherServlet = new BundleDispatcherServlet();
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put("alias", "/dispatcher");
        serviceRegistrations.add(context.registerService(Servlet.class.getName(), bundleDispatcherServlet, props));

        if (!scriptResolverAlreadyInstalled) {
            bundleScriptResolver = new BundleScriptResolver();
            Map<String, ScriptFactory> scriptFactoryMap = new HashMap<String, ScriptFactory>();
            BundleJSR223ScriptFactory bundleJSR223ScriptFactory = new BundleJSR223ScriptFactory();
            BundleRequestDispatcherScriptFactory bundleRequestDispatcherScriptFactory = new BundleRequestDispatcherScriptFactory(bundleDispatcherServlet);
            // @ todo remove these hardcoded lists
            scriptFactoryMap.put("jsp", bundleRequestDispatcherScriptFactory);
            scriptFactoryMap.put("groovy", bundleJSR223ScriptFactory);
            scriptFactoryMap.put("js", bundleJSR223ScriptFactory);
            scriptFactoryMap.put("php", bundleRequestDispatcherScriptFactory);
            scriptFactoryMap.put("vm", bundleJSR223ScriptFactory);
            scriptFactoryMap.put("fm", bundleJSR223ScriptFactory);
            bundleScriptResolver.setScriptFactoryMap(scriptFactoryMap);
            bundleScriptResolver.setTemplateManagerService(templatesService);
            List<String> scriptExtensionsOrdering = new ArrayList<String>();
            // @ todo remove these hardcoded lists
            scriptExtensionsOrdering.add("jsp");
            scriptExtensionsOrdering.add("groovy");
            scriptExtensionsOrdering.add("js");
            scriptExtensionsOrdering.add("php");
            scriptExtensionsOrdering.add("vm");
            scriptExtensionsOrdering.add("fm");
            bundleScriptResolver.setScriptExtensionsOrdering(scriptExtensionsOrdering);
            scriptResolvers.add(bundleScriptResolver);
        }

        jspBundleObserver = new JspBundleObserver(bundleScriptResolver, bundleDispatcherServlet);
        extensionObservers.put(new BundleURLScanner("/", "*.jsp", true), jspBundleObserver);

        scriptBundleObserver = new ScriptBundleObserver(bundleScriptResolver);
        List<String> scriptExtensions = new ArrayList<String>();
        scriptExtensions.add("groovy");
        scriptExtensions.add("js");
        scriptExtensions.add("php");
        scriptExtensions.add("vm");
        scriptExtensions.add("fm");
        for (String scriptExtension : scriptExtensions) {
            extensionObservers.put(new BundleURLScanner("/", "*." + scriptExtension, true), scriptBundleObserver);
        }

        setupBundleListener(context);

        // now let's register artifact transformer to legacy Jahia module
        serviceRegistrations.add(context.registerService(
            new String[] {ArtifactUrlTransformer.class.getName(), ArtifactListener.class.getName()},
            new JahiaLegacyModuleTransformer(),
            new Hashtable()
        ));

    }

    private synchronized void setupBundleListener(BundleContext context) {
        context.addBundleListener(bundleListener = new SynchronousBundleListener() {

            public void bundleChanged(final BundleEvent bundleEvent) {
                Bundle bundle = bundleEvent.getBundle();
                if (bundle == null || bundle.getHeaders().get("module-type") == null) {
                    return;
                }

                try {
                    switch (bundleEvent.getType()) {
                        case BundleEvent.INSTALLED:
                        case BundleEvent.UPDATED:
                            install(bundle);
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
                        case BundleEvent.UNRESOLVED:
                            unnresolve(bundle);
                            break;
                        case BundleEvent.UNINSTALLED:
                            uninstall(bundle);
                            break;
                    }
                } catch (Exception e) {
                    logger.error("Error when handling event",e);
                }
            }

        }
        );
        for (Bundle bundle : context.getBundles()) {
            parseBundle(bundle);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        context.removeBundleListener(bundleListener);

        for (Bundle bundle : new HashSet<Bundle>(registeredBundles.keySet())) {
            unnresolve(bundle);
        }

        bundleListener = null;
        registeredBundles.clear();

        if (!scriptResolverAlreadyInstalled) {
            if (renderService != null) {
                renderService.getScriptResolvers().remove(bundleScriptResolver);
            }
        }
        for (ServiceRegistration serviceRegistration : serviceRegistrations) {
            serviceRegistration.unregister();
        }

    }

    private synchronized void install(Bundle bundle) {
        installedBundles.add(bundle);
        parseBundle(bundle);
    }

    private synchronized void uninstall(Bundle bundle) {
        logger.info("--- Uninstalling Jahia OSGi bundle " + bundle.getSymbolicName() + " v" + bundle.getVersion() + " --");
        long startTime = System.currentTimeMillis();

        final JahiaBundleTemplatesPackage jahiaBundleTemplatesPackage = (JahiaBundleTemplatesPackage) templatePackageRegistry.lookupByFileNameAndVersion(bundle.getSymbolicName(), new ModuleVersion(bundle.getVersion().toString()));

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, null, new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    templatePackageDeployer.clearModuleNodes(jahiaBundleTemplatesPackage, session);
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while initializing module content for module " + jahiaBundleTemplatesPackage, e);
        }
        installedBundles.remove(bundle);
        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("--- Finished uninstalling Jahia OSGi bundle " + bundle.getSymbolicName() + " v" + bundle.getVersion() + " in " + totalTime + "ms --");
    }

    private void parseBundle(Bundle bundle) {
        if (bundle == null || bundle.getHeaders().get("module-type") == null) {
            return;
        }

        final JahiaBundleTemplatesPackage jahiaBundleTemplatesPackage = new JahiaBundleTemplatesPackage(bundle);
        if (bundle.getHeaders().get("Implementation-Title") != null) {
            jahiaBundleTemplatesPackage.setName((String) bundle.getHeaders().get("Implementation-Title"));
        } else {
            jahiaBundleTemplatesPackage.setName(bundle.getSymbolicName());
        }
        // @todo the list of definition files should come either from the header or from a bundle watcher
        jahiaBundleTemplatesPackage.setDefinitionsFile("META-INF/definitions.cnd");
        jahiaBundleTemplatesPackage.setModuleType((String) bundle.getHeaders().get("module-type"));
        jahiaBundleTemplatesPackage.setVersion(new ModuleVersion(bundle.getVersion().toString()));
        jahiaBundleTemplatesPackage.setRootFolder((String) bundle.getHeaders().get("root-folder"));
        jahiaBundleTemplatesPackage.setRootFolderPath("/osgi/" + jahiaBundleTemplatesPackage.getRootFolder());
        if (bundle.getHeaders().get("Source-Folders") != null) {
            templatesService.setSourcesFolderInPackage(jahiaBundleTemplatesPackage, new File((String) bundle.getHeaders().get("Source-Folders")));
        }

//        String rootFolderPrefix = jahiaBundleTemplatesPackage.getRootFolder().replaceAll("[ -]", "") + "/";
         if (bundle.getEntry("/") != null) {
            jahiaBundleTemplatesPackage.setFilePath(bundle.getEntry("/").getPath());
         }
//        if (bundle.getEntry(rootFolderPrefix) != null) {
//            jahiaBundleTemplatesPackage.setFilePath(bundle.getEntry(rootFolderPrefix).getPath());
//        }
        String depends = (String) bundle.getHeaders().get("depends");
        if (depends != null && !StringUtils.isEmpty(depends.trim())) {
            String[] dependencies = Patterns.COMMA.split(depends);
            for (String dependency : dependencies) {
                jahiaBundleTemplatesPackage.setDepends(dependency.trim());
            }
        }

        List<String> dependsList = jahiaBundleTemplatesPackage.getDepends();
        if (!dependsList.contains("default") && !dependsList.contains("Default Jahia Templates") && !bundle.getSymbolicName().equals("assets")&& !bundle.getSymbolicName().equals("default")) {
            dependsList.add("default");
        }

        for (String depend : dependsList) {
            Set<ModuleVersion> m = templatePackageRegistry.getAvailableVersionsForModule(depend);
            if (m.isEmpty()) {
                if (!toBeParsed.containsKey(depend)) {
                    toBeParsed.put(depend, new ArrayList<Bundle>());
                }
                toBeParsed.get(depend).add(bundle);
                return;
            }
        }

        logger.info("--- Parsing Jahia OSGi bundle " + bundle.getSymbolicName() + " v" + bundle.getVersion() + " --");
        registeredBundles.put(bundle, jahiaBundleTemplatesPackage);
        templatePackageRegistry.registerPackageVersion(jahiaBundleTemplatesPackage);

        boolean latestDefinitions = NodeTypeRegistry.getInstance().isLatestDefinitions(bundle.getSymbolicName(), jahiaBundleTemplatesPackage.getVersion());
        if (latestDefinitions) {
            List<URL> foundURLs = cndScanner.scan(bundle);
            addResources(bundle, foundURLs, cndBundleObserver);
        }

        logger.info("--- Done parsing Jahia OSGi bundle " + bundle.getSymbolicName() + " v" + bundle.getVersion() + " --");

        if (installedBundles.contains(bundle)) {
            logger.info("--- Installing Jahia OSGi bundle " + bundle.getSymbolicName() + " v" + bundle.getVersion() + " --");
            installedBundles.remove(bundle);
            scanForImportFiles(bundle, jahiaBundleTemplatesPackage);

            try {
                JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, null, new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        templatePackageDeployer.initializeModuleContent(jahiaBundleTemplatesPackage, session);
                        return null;
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Error while initializing module content for module " + jahiaBundleTemplatesPackage, e);
            }
            logger.info("--- Done installing Jahia OSGi bundle " + bundle.getSymbolicName() + " v" + bundle.getVersion() + " --");
        }

        if (latestDefinitions) {
            parseDependantBundles(jahiaBundleTemplatesPackage.getRootFolder());
            parseDependantBundles(jahiaBundleTemplatesPackage.getName());
        }
        if (toBeStarted.contains(bundle)) {
            toBeStarted.remove(bundle);
            try {
                bundle.start();
            } catch (BundleException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private void parseDependantBundles(String key) {
        if (toBeParsed.get(key) != null) {
            for (Bundle bundle1 : toBeParsed.get(key)) {
                parseBundle(bundle1);
            }
            toBeParsed.remove(key);
        }
    }

    private synchronized void resolve(Bundle bundle) {
    }

    private synchronized void unnresolve(Bundle bundle) {
    }

    private synchronized void starting(Bundle bundle) {
        if (templatePackageRegistry.lookupByFileName(bundle.getSymbolicName()) != null) {
            try {
                JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByFileName(bundle.getSymbolicName());
                if (!(jahiaTemplatesPackage instanceof JahiaBundleTemplatesPackage)) {
                    logger.warn("Error, a non OSGi module conflicts with an OSGi module, please fix this for module name :" + bundle.getSymbolicName());
                } else {
                    ((JahiaBundleTemplatesPackage) jahiaTemplatesPackage).getBundle().stop();
                }
            } catch (BundleException e) {
                logger.info("--- Cannot stop previous version of module " + bundle.getSymbolicName(), e);
            }
        }
    }

    private synchronized void start(Bundle bundle) {
        JahiaBundleTemplatesPackage jahiaBundleTemplatesPackage = (JahiaBundleTemplatesPackage) templatePackageRegistry.lookupByFileNameAndVersion(bundle.getSymbolicName(), new ModuleVersion(bundle.getVersion().toString()));
        if (jahiaBundleTemplatesPackage == null) {
            toBeStarted.add(bundle);
            return;
        }

        logger.info("--- Start Jahia OSGi bundle " + bundle.getSymbolicName() + " v" + bundle.getVersion() + " --");
        long startTime = System.currentTimeMillis();

        templatePackageRegistry.register(jahiaBundleTemplatesPackage);
        jahiaBundleTemplatesPackage.setActiveVersion(true);

        // initialize spring context
        createBundleApplicationContext(jahiaBundleTemplatesPackage, parentWebApplicationContext.getServletContext());

        // scan for resource and call observers
        for (BundleURLScanner bundleURLScanner : extensionObservers.keySet()) {
            List<URL> foundURLs = bundleURLScanner.scan(bundle);
            BundleObserver<URL> urlObserver = extensionObservers.get(bundleURLScanner);
            addResources(bundle, foundURLs, urlObserver);
        }

        registerStaticResources(bundle);

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("--- Finished starting Jahia OSGi bundle " + bundle.getSymbolicName() + " v" + bundle.getVersion() + " in "+totalTime+"ms --");
    }

    private synchronized void stopping(Bundle bundle) {
        logger.info("--- Stop Jahia OSGi bundle " + bundle.getSymbolicName() + " v" + bundle.getVersion() + " --");
        long startTime = System.currentTimeMillis();

        JahiaBundleTemplatesPackage jahiaBundleTemplatesPackage = (JahiaBundleTemplatesPackage) templatePackageRegistry.lookupByFileNameAndVersion(bundle.getSymbolicName(), new ModuleVersion(bundle.getVersion().toString()));
        if (jahiaBundleTemplatesPackage == null) {
            return;
        }

        templatePackageRegistry.unregister(jahiaBundleTemplatesPackage);
        jahiaBundleTemplatesPackage.setActiveVersion(false);

        // stop spring context
        if (jahiaBundleTemplatesPackage.getContext() != null) {
            if (jahiaBundleTemplatesPackage.getContext().isActive()) {
                jahiaBundleTemplatesPackage.getContext().close();
                jahiaBundleTemplatesPackage.setContext(null);
            }
        }

        // scan for resource and call observers
        for (BundleURLScanner bundleURLScanner : extensionObservers.keySet()) {
            List<URL> foundURLs = bundleURLScanner.scan(bundle);
            BundleObserver<URL> urlObserver = extensionObservers.get(bundleURLScanner);
            removeResources(bundle, foundURLs, urlObserver);
        }

        if (bundleHttpServiceTrackers.containsKey(bundle)) {
            bundleHttpServiceTrackers.remove(bundle).close();
        }

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("--- Finished stopping Jahia OSGi bundle " + bundle.getSymbolicName() + " v" + bundle.getVersion() + " in "+totalTime+"ms --");
    }

    private void addResources(Bundle bundle, List<URL> foundURLs, BundleObserver<URL> urlObserver) {
        bundleResources.put(bundle, foundURLs);
        urlObserver.addingEntries(bundle, foundURLs);
    }

    private void registerStaticResources(final Bundle bundle) {
        final Hashtable<String, String> staticResources = new Hashtable<String, String>();
        String staticResourcesStr = (String) bundle.getHeaders().get(STATIC_RESOURCES_HEADERNAME);
        if (staticResourcesStr != null) {
            String[] staticResourcesArray = staticResourcesStr.split(",");
            for (String curMappingStr : staticResourcesArray) {
                if (curMappingStr.contains("=")) {
                    String[] mapping = curMappingStr.split("=");
                    staticResources.put(mapping[0], mapping[1]);
                } else {
                    staticResources.put("/" + bundle.getSymbolicName() + curMappingStr, curMappingStr);
                }
            }
        }

        if (bundleHttpServiceTrackers.containsKey(bundle)) {
            bundleHttpServiceTrackers.remove(bundle).close();
        }
        ServiceTracker bundleServiceTracker = new ServiceTracker(bundle.getBundleContext(), HttpService.class.getName(), null) {

            HttpContext httpContext;

            @Override
            public Object addingService(ServiceReference reference) {
                HttpService httpService = (HttpService) super.addingService(reference);
                httpContext = new FileHttpContext(FileHttpContext.getSourceURLs(bundle), httpService.createDefaultHttpContext());
                for (Map.Entry<String, String> curEntry : staticResources.entrySet()) {
                    try {
                        logger.info("Registering static resource " + curEntry.getKey());
                        httpService.registerResources(curEntry.getKey(), curEntry.getValue(), httpContext);
                    } catch (NamespaceException e) {
                        e.printStackTrace();
                    }
                }
                jspBundleObserver.setBundleHttpService(bundle, httpService);
                return httpService;
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                HttpService httpService = (HttpService) service;
                for (Map.Entry<String, String> curEntry : staticResources.entrySet()) {
                    logger.info("Unregistering static resource " + curEntry.getKey());
                    httpService.unregister(curEntry.getKey());
                }
                jspBundleObserver.setBundleHttpService(bundle, null);
                super.removedService(reference, service);
            }
        };

        bundleServiceTracker.open();
        bundleHttpServiceTrackers.put(bundle, bundleServiceTracker);
    }

    private void scanForImportFiles(Bundle bundle, JahiaBundleTemplatesPackage jahiaBundleTemplatesPackage) {
        Comparator<Resource> c = new Comparator<Resource>() {
            public int compare(Resource o1, Resource o2) {
                return StringUtils.substringBeforeLast(o1.getFilename(), ".").compareTo(StringUtils.substringBeforeLast(o2.getFilename(), "."));
            }
        };
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
        Collections.sort(importFiles, c);
        for (Resource importFile : importFiles) {
            try {
                jahiaBundleTemplatesPackage.addInitialImport(importFile.getURL().getPath());
            } catch (IOException e) {
                logger.error("Error retrieving URL for resource " + importFile, e);
            }
        }
    }

    private void removeResources(Bundle bundle, List<URL> foundURLs, BundleObserver<URL> urlObserver) {
        bundleResources.remove(bundle);
        urlObserver.removingEntries(bundle, foundURLs);
    }

    public void createBundleApplicationContext(JahiaBundleTemplatesPackage aPackage, ServletContext servletContext) throws BeansException {
        Bundle bundle = aPackage.getBundle();
        if (aPackage.getResource("/META-INF/spring/") != null && aPackage.getResource("/META-INF/spring/").exists()) {
            logger.debug("Start initializing context for module {}", aPackage.getName());
            long startTime = System.currentTimeMillis();

            String configLocation = "classpath:org/jahia/defaults/config/spring/modules-applicationcontext-registry.xml";
            Enumeration<URL> springFilesURLs = bundle.findEntries("META-INF/spring", "*.xml", true);
            if (springFilesURLs == null) {
                logger.info("Empty Spring directory, will not initialize any beans");
                return;
            }
            while (springFilesURLs.hasMoreElements()) {
                configLocation += ",classpath:" + springFilesURLs.nextElement().getPath();
            }
            logger.info("Loading bean definitions from configLocation=" + configLocation);
            XmlWebApplicationContext bundleApplicationContext = new XmlWebApplicationContext();
            bundleApplicationContext.setParent(SpringContextSingleton.getInstance().getContext());
            bundleApplicationContext.setClassLoader(BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle, SpringContextSingleton.getInstance().getContext().getClassLoader()));
            bundleApplicationContext.setServletContext(servletContext);
            servletContext.setAttribute(XmlWebApplicationContext.class.getName() + ".jahiaModule." + aPackage.getRootFolder(), bundleApplicationContext);
            bundleApplicationContext.setConfigLocation(configLocation);
            aPackage.setContext(bundleApplicationContext);
            bundleApplicationContext.refresh();

            logger.info(
                    "'{}' [{}] context initialized in {} ms, registered {} beans",
                    new Object[]{aPackage.getName(), aPackage.getRootFolder(),
                            System.currentTimeMillis() - startTime, bundleApplicationContext.getBeanNamesForType(null).length});
        }
    }

}
