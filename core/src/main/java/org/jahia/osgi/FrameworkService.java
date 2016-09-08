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
package org.jahia.osgi;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.karaf.main.ConfigProperties;
import org.apache.karaf.main.Main;
import org.apache.karaf.main.util.ArtifactResolver;
import org.apache.karaf.main.util.SimpleMavenResolver;
import org.apache.karaf.util.config.PropertiesLoader;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.util.dag.DAG;
import org.codehaus.plexus.util.dag.TopologicalSorter;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PropertyPlaceholderHelper;

/**
 * OSGi framework service. This class is responsible for setting up and starting the embedded Karaf OSGi runtime.
 *
 * @author Serge Huber
 */
public class FrameworkService implements FrameworkListener {

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final FrameworkService INSTANCE = new FrameworkService(JahiaContextLoaderListener.getServletContext());

        private Holder() {
        }
    }
    
    public static final String EVENT_TOPIC_LIFECYCLE = "org/jahia/dx/lifecycle";
    
    public static final String EVENT_TYPE_OSGI_STARTED = "osgiContainerStarted";

    private static final Logger logger = LoggerFactory.getLogger(FrameworkService.class);

    /**
     * Returns bundle context.
     *
     * @return bundle context or <code>null</code> in case the OSGi container has not been started yet
     */
    public static BundleContext getBundleContext() {
        final FrameworkService instance = getInstance();
        if (instance != null && instance.main != null) {
            return instance.main.getFramework().getBundleContext();
        } else {
            return null;
        }
    }

    /**
     * Returns a singleton instance of this class.
     *
     * @return a singleton instance of this class
     */
    public static FrameworkService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Notifies the service that the FileInstall watcher has been started and processed the found modules.
     */
    public static void notifyFileInstallStarted() {
        final FrameworkService instance = getInstance();

        Properties felixProperties = (Properties) SpringContextSingleton.getBean("felixFileInstallConfig");
        if (instance.firstStartup) {
            // this is a first framework startup
            instance.firstStartup = false;
            if (!Boolean.valueOf(felixProperties.getProperty("felix.fileinstall.bundles.new.start", "true"))) {
                // as the bundles are not started automatically by Fileinstall we need to start them manually
                startAllModules();
            }
        } else if (!SettingsBean.getInstance().isDevelopmentMode() &&
                !Boolean.valueOf(felixProperties.getProperty("felix.fileinstall.bundles.new.start", "true"))) {
            startMigrateBundlesIfNeeded();
        }

        synchronized (instance) {
            instance.fileInstallStarted = true;
            logger.info("FileInstall watcher started");
            notifyStarted(instance);
        }
    }

    /**
     * Notify this service that the container has actually started.
     */
    private static void notifyStarted(FrameworkService instance) {
        if (instance.frameworkStartLevelChanged && instance.fileInstallStarted) {
            // send synchronous event about startup
            sendEvent(EVENT_TOPIC_LIFECYCLE, Collections.singletonMap("type", EVENT_TYPE_OSGI_STARTED), false);
            
            // both pre-requisites for stratup are fulfilled
            // notify any threads waiting
            instance.notifyAll();
            logger.info("OSGi platform service initialized in {} ms", (System.currentTimeMillis() - instance.startTime));
        }
    }

    /**
     * Initiate synchronous or asynchronous delivery of an OSGi event using {@link EventAdmin} service. If synchronous delivery is
     * requested, this method does not return to the caller until delivery of the event is completed.
     * 
     * @param topic the topic of the event
     * @param properties the event's properties (may be {@code null}). A property whose key is not of type {@code String} will be ignored.
     * @param asynchronous if <code>true</code> an event is delivered asynchronously; in case of <code>false</code> this method does not
     *            return to the caller until delivery of the event is completed
     */
    public static void sendEvent(String topic, Map<String, ?> properties, boolean asynchronous) {
        BundleContext context = FrameworkService.getBundleContext();
        ServiceReference<?> ref = context.getServiceReference(EventAdmin.class.getName());
        if (ref != null) {
            Object service = context.getService(ref);
            try {
                // have to use the class loader of the EventAdmin service
                ClassLoader classLoader = service.getClass().getClassLoader();

                Object evt = classLoader.loadClass("org.osgi.service.event.Event")
                        .getConstructor(String.class, Map.class).newInstance(topic, properties);

                logger.info("Sending {} event with the properties {} to the topic {}...",
                        new Object[] { asynchronous ? "asynchronous" : "synchronous", properties, topic });

                MethodUtils.invokeExactMethod(service, asynchronous ? "postEvent" : "sendEvent", evt);

                logger.info("Event sent to the topic {}", topic);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException
                    | InstantiationException | IllegalArgumentException | SecurityException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private static void startAllModules() {

        long startTime = System.currentTimeMillis();
        Map<Bundle, JahiaTemplatesPackage> toBeStarted = collectBundlesToBeStarted();

        logger.info("Will start {} bundles", toBeStarted.size());
        Collection<Bundle> sortedBundles = getSortedModules(toBeStarted);

        try {
            for (Bundle bundle : sortedBundles) {
                try {
                    logger.info("Triggering start for bundle {}/{}", bundle.getSymbolicName(), bundle.getVersion());
                    bundle.start();
                } catch (BundleException e) {
                    if (BundleException.RESOLVE_ERROR == e.getType()) {
                        // log warning for the resolution (dependencies) error
                        logger.warn(e.getMessage());
                    } else {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        } finally {
            logger.info("Finished starting {} bundles in {} ms", toBeStarted.size(),
                    System.currentTimeMillis() - startTime);
            toBeStarted = null;
        }
    }

    private static Map<Bundle, JahiaTemplatesPackage> collectBundlesToBeStarted() {
        Map<Bundle, JahiaTemplatesPackage> toBeStarted = new HashMap<>();
        for (Bundle bundle : getBundleContext().getBundles()) {

            if (bundle.getState() != Bundle.ACTIVE && bundle.getState() != Bundle.UNINSTALLED
                    && !BundleUtils.isFragment(bundle) && BundleUtils.isJahiaModuleBundle(bundle)) {
                JahiaTemplatesPackage pkg = BundleUtils.getModule(bundle);
                if (pkg != null) {
                    toBeStarted.put(bundle, pkg);
                }
            }
        }

        return toBeStarted;
    }

    private static Collection<Bundle> getSortedModules(Map<Bundle, JahiaTemplatesPackage> modulesByBundle) {

        long startTime = System.currentTimeMillis();
        try {

            // we build a Directed Acyclic Graph of dependencies (only those, which are present in the package)
            DAG dag = new DAG();

            Map<String, Bundle> bundlesByModuleId = new HashMap<>();
            for (Map.Entry<Bundle, JahiaTemplatesPackage> entry : modulesByBundle.entrySet()) {

                JahiaTemplatesPackage pkg = entry.getValue();
                String pkgId = pkg.getId();
                bundlesByModuleId.put(pkgId, entry.getKey());

                dag.addVertex(pkgId);
                for (String depPkg : pkg.getDepends()) {
                    dag.addEdge(pkgId, depPkg);
                }
                if (!pkg.getDepends().contains(JahiaTemplatesPackage.ID_DEFAULT)
                        && !pkg.getDepends().contains(JahiaTemplatesPackage.NAME_DEFAULT)
                        && !ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                                .getModulesWithNoDefaultDependency().contains(pkg.getId())) {
                    dag.addEdge(pkgId, JahiaTemplatesPackage.ID_DEFAULT);
                }
            }

            List<Bundle> sortedBundles = new LinkedList<>();

            // use topological sort (Depth First Search) on the created graph
            @SuppressWarnings("unchecked")
            List<String> vertexes = TopologicalSorter.sort(dag);
            for (String v : vertexes) {
                Bundle b = bundlesByModuleId.get(v);
                if (b != null) {
                    sortedBundles.add(b);
                }
            }

            logger.info("Sorted bundles in {} ms", System.currentTimeMillis() - startTime);
            return sortedBundles;
        } catch (CycleDetectedException e) {
            logger.error("A cyclic dependency detected in the modules to be started", e);
            // will start bundles in non-sorted order; the OSGi framework will handle the startup correctly if all the dependencies are available
            return modulesByBundle.keySet();
        }
    }
    
    private boolean fileInstallStarted;
    private boolean firstStartup;
    private boolean frameworkStartLevelChanged;
    private Main main;
    private final ServletContext servletContext;
    private long startTime;

    private File deployedBundlesDir;

    private FrameworkService(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    private void checkFirstStartup() throws IOException {
        firstStartup = !new File(deployedBundlesDir, "bundle0").exists();
    }

    private Map<String, String> filterOutSystemProperties() {

        if (!"was".equals(SettingsBean.getInstance().getServer())) {
            // we skip filtering out system properties on any server except WebSphere, which sets OSGi-related properties for its internal
            // container
            return null;
        }

        Map<String, String> filteredOutSystemProperties = new HashMap<>();
        Properties sysProps = System.getProperties();
        for (String prop : sysProps.stringPropertyNames()) {
            if (prop.startsWith("org.osgi.framework.")) {
                logger.info("Filtering out system property {}", prop);
                filteredOutSystemProperties.put(prop, sysProps.getProperty(prop));
                sysProps.remove(prop);
            }
        }
        return filteredOutSystemProperties;
    }

    /**
     * For the bundles in INSTALLED state we force the resolution so they are moved to RESOLVED state.
     */
    private void forceBundleResolution() {
        logger.info("Trigger resolution of bundles");
        BundleLifecycleUtils.resolveBundles(null);
    }

    @Override
    public void frameworkEvent(FrameworkEvent event) {
        if (event.getType() == FrameworkEvent.STARTLEVEL_CHANGED) {
            forceBundleResolution();
            final FrameworkService instance = getInstance();
            synchronized (instance) {
                instance.frameworkStartLevelChanged = true;
                logger.info("Framework start level changed");
                notifyStarted(instance);
            }
        }
    }

    /**
     * Returns <code>true</code> if the OSGi container is completely started.
     *
     * @return <code>true</code> if the OSGi container is completely started; <code>false</code> otherwise
     */
    public boolean isStarted() {
        return frameworkStartLevelChanged && fileInstallStarted;
    }

    private void restoreSystemProperties(Map<String, String> systemPropertiesToRestore) {

        if (systemPropertiesToRestore == null || systemPropertiesToRestore.isEmpty()) {
            // nothing to restore
            return;
        }

        for (Map.Entry<String, String> prop : systemPropertiesToRestore.entrySet()) {
            logger.info("Restoring system property {}", prop.getKey());
            System.setProperty(prop.getKey(), prop.getValue());
        }
    }

    private void setupStartupListener() {
        main.getFramework().getBundleContext().addFrameworkListener(this);
    }

    private void setupSystemProperties() {

        @SuppressWarnings("unchecked")
        Map<String,String> unreplaced = (Map<String,String>) SpringContextSingleton.getBean("osgiProperties");
        Map<String,String> newSystemProperties = new TreeMap<>();

        PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("${", "}");
        Properties systemProps = System.getProperties();

        for (Map.Entry<String, String> entry : unreplaced.entrySet()) {
            newSystemProperties.put(entry.getKey(), placeholderHelper.replacePlaceholders(entry.getValue(), systemProps));
        }

        for (Map.Entry<String,String> property : newSystemProperties.entrySet()) {
            String propertyName = property.getKey();
            String oldPropertyValue = System.getProperty(propertyName);
            if (oldPropertyValue != null) {
                logger.warn("Overriding system property " + propertyName + "=" + oldPropertyValue + " with new value=" + property.getValue());
            }
            JahiaContextLoaderListener.setSystemProperty(propertyName, property.getValue());
        }

        File file = new File(System.getProperty("karaf.etc"), "config.properties");
        org.apache.felix.utils.properties.Properties karafConfigProperties = null;
        try {
            karafConfigProperties = PropertiesLoader.loadConfigProperties(file);
        } catch (Exception e) {
            logger.error("Unable to load properties from file " + file + ". Cause: " + e.getMessage(), e);
            karafConfigProperties = new org.apache.felix.utils.properties.Properties();
        }

        StringBuilder extraSystemPackages = new StringBuilder(karafConfigProperties.getProperty("org.osgi.framework.system.packages.extra"));
        boolean modifiedExtraSystemPackages = false;
        for (Map.Entry<String, String> entry : newSystemProperties.entrySet()) {
            if (entry.getKey().startsWith("org.osgi.framework.system.packages.extra.")) {
                extraSystemPackages.append(',').append(entry.getValue());
                modifiedExtraSystemPackages = true;
            }
        }
        if (modifiedExtraSystemPackages) {
            JahiaContextLoaderListener.setSystemProperty("org.osgi.framework.system.packages.extra", extraSystemPackages.toString());
        }

    }

    public void start() {
        startTime = System.currentTimeMillis();
        logger.info("Starting OSGi platform service");
        startKaraf();
        servletContext.setAttribute(BundleContext.class.getName(), main.getFramework().getBundleContext());
    }

    private void startKaraf() {
        Map<String, String> filteredOutSystemProperties = filterOutSystemProperties();
        try {
            setupSystemProperties();
            deployedBundlesDir = new File(System.getProperty("org.osgi.framework.storage"));
            checkFirstStartup();
            main = new Main(new String[0]);
            main.launch();
            setupStartupListener();
            startInitialBundlesIfNeeded();
        } catch (Exception e) {
            main = null;
            logger.error("Error starting OSGi container", e);
            throw new JahiaRuntimeException("Error starting OSGi container", e);
        } finally {
            restoreSystemProperties(filteredOutSystemProperties);
        }
    }

    private void startInitialBundlesIfNeeded() {
        File marker = new File(deployedBundlesDir, "[initial-bundles].dostart");
        if (!marker.exists()) {
            return;
        }
        logger.info("Installing and starting initial bundles");
        
        // there is a timing issue somewhere in the Karaf code, sleep for 5 seconds
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        
        BundleContext ctx = getBundleContext();
        File startupPropsFile = new File(System.getProperty(ConfigProperties.PROP_KARAF_ETC),
                Main.STARTUP_PROPERTIES_FILE_NAME);
        org.apache.felix.utils.properties.Properties startupProps = PropertiesLoader
                .loadPropertiesOrFail(startupPropsFile);

        List<File> bundleDirs = getBundleRepos();
        ArtifactResolver resolver = new SimpleMavenResolver(bundleDirs);

        List<Bundle> bundlesToStart = new LinkedList<>();
        for (String key : startupProps.keySet()) {
            Integer startLevel = new Integer(startupProps.getProperty(key).trim());
            try {
                URI resolvedURI = resolver.resolve(new URI(key));
                Bundle b = ctx.installBundle(key, resolvedURI.toURL().openStream());
                b.adapt(BundleStartLevel.class).setStartLevel(startLevel);
                if (!BundleUtils.isFragment(b)) {
                    bundlesToStart.add(b);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error installing bundle listed in " + startupPropsFile + " with url: " + key
                        + " and startlevel: " + startLevel, e);
            }
        }
        for (Bundle b : bundlesToStart) {
            try {
                b.start();
            } catch (Exception e) {
                throw new RuntimeException("Error starting bundle " + b.getSymbolicName() + "/" + b.getVersion(), e);
            }
        }
        logger.info("All initial bundles installed and set to start");
        FileUtils.deleteQuietly(marker);
    }

    private static void startMigrateBundlesIfNeeded() {
        // as the bundles are not started automatically after a migration whe should do it manually
        File deployedBundlesDir = new File(System.getProperty("org.osgi.framework.storage"));
        File marker = new File(deployedBundlesDir, "[migrate-bundles].dostart");
        if (!marker.exists()) {
            return;
        }
        logger.info("Starting migrate bundles");

        try {
            BundleContext ctx = getBundleContext();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(marker));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] lineParts = line.split(",");
                String bundleSymbolicName = lineParts[0].trim();
                String bundleVersion = lineParts[1].trim();
                logger.info("Re-starting module " + bundleSymbolicName + " v" + bundleVersion + " purged by FixApplier ...");
                try {
                    Bundle b = null;
                    for (Bundle bundle : ctx.getBundles()) {
                        String n = BundleUtils.getModuleId(bundle);
                        if (StringUtils.equals(n, bundleSymbolicName)) {
                            if (StringUtils.equals(bundle.getVersion().toString(), bundleVersion)) {
                                b = bundle;
                                break;
                            }
                        }
                    }
                    if (b != null) {
                        b.start();
                    } else {
                        logger.info("Cannot find module " + bundleSymbolicName + " v" + bundleVersion + ". Skip starting it.");
                    }

                } catch (Exception e) {
                    logger.error("Error starting module " + bundleSymbolicName + " v" + bundleVersion + ". Cause: " + e.getMessage(), e);
                }
            }
            IOUtils.closeQuietly(bufferedReader);
            logger.info("All migrate bundles installed and set to start");
            FileUtils.deleteQuietly(marker);
        } catch (IOException e) {
            logger.error("Error reading [migrate-bundles].dostart Cause: " + e.getMessage(), e);
        }
    }

    private List<File> getBundleRepos() {
        // currently we consider only karaf/system repo
        List<File> bundleDirs = new ArrayList<File>();
        File baseSystemRepo = new File(System.getProperty(ConfigProperties.PROP_KARAF_HOME),
                System.getProperty("karaf.default.repository", "system"));
        if (!baseSystemRepo.exists() && baseSystemRepo.isDirectory()) {
            throw new RuntimeException("system repo folder not found: " + baseSystemRepo.getAbsolutePath());
        }
        bundleDirs.add(baseSystemRepo);

        return bundleDirs;
    }

    /**
     * Shuts the OSGi container down.
     *
     * @throws BundleException in case of an error
     */
    public void stop() throws BundleException {
        if (this.main != null) {
            servletContext.removeAttribute(BundleContext.class.getName());
            try {
                main.destroy();
            } catch (Exception e) {
                logger.error("Error shutting down Karaf framework", e);
            }
        }
        logger.info("OSGi framework stopped");
    }
}
