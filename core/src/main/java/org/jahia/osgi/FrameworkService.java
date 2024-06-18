/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.osgi;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.karaf.main.Main;
import org.apache.karaf.util.config.PropertiesLoader;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.ScriptEngineUtils;
import org.osgi.framework.*;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PropertyPlaceholderHelper;

import javax.script.*;
import javax.servlet.ServletContext;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * OSGi framework service. This class is responsible for setting up and starting the embedded Karaf OSGi runtime.
 *
 * @author Serge Huber
 */
public final class FrameworkService implements FrameworkListener {
    private static final Logger logger = LoggerFactory.getLogger(FrameworkService.class);

    public static final String EVENT_TOPIC_LIFECYCLE = "org/jahia/dx/lifecycle";
    public static final String EVENT_TYPE_CLUSTERING_FEATURE_INSTALLED = "clusteringFeatureInstalled";
    public static final String EVENT_TYPE_INITIAL_START_LEVEL_REACHED = "initialStartLevelReached";
    public static final String EVENT_TYPE_FILEINSTALL_STARTED = "fileInstallStarted";
    public static final String EVENT_TYPE_SPRING_BRIDGE_STARTED = "springBridgeStarted";
    public static final String EVENT_TYPE_CLUSTER_STARTED = "clusterStarted";
    public static final String EVENT_TYPE_FINAL_START_LEVEL_REACHED = "finalStartLevelReached";

    private final ServletContext servletContext;
    private boolean firstStartup;
    private Main main;
    private long startTime;
    private int initialFrameworkStartLevel = 80;
    private int finalFrameworkStartLevel = 100;
    private long osgiStartupWaitTimeout;
    private BundleStarter bundleStarter;
    private Timer startLevelTimer = new Timer("OSGi-FrameworkService-Startup-Timer", true);

    private final CountDownLatch initialStartLevelReachedLatch = new CountDownLatch(1);
    private final CountDownLatch fileInstallStartedLatch = new CountDownLatch(1);
    private final CountDownLatch springBridgeStartedLatch = new CountDownLatch(1);
    private final CountDownLatch finalStartLevelReachedLatch = new CountDownLatch(1);
    private final CountDownLatch clusterStartedLatch = new CountDownLatch(1);

    private FrameworkService(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

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
     * Initiate synchronous or asynchronous delivery of an OSGi event using {@link EventAdmin} service. If synchronous delivery is
     * requested, this method does not return to the caller until delivery of the event is completed.
     *
     * @param topic        the topic of the event
     * @param properties   the event's properties (may be {@code null}). A property whose key is not of type {@code String} will be ignored.
     * @param asynchronous if <code>true</code> an event is delivered asynchronously; in case of <code>false</code> this method does not
     *                     return to the caller until delivery of the event is completed
     */
    public static void sendEvent(String topic, Map<String, ?> properties, boolean asynchronous) {
        BundleContext context = FrameworkService.getBundleContext();
        if (context != null) {
            Object service = BundleUtils.getOsgiService(EventAdmin.class.getName(), null);
            if (service != null) {
                try {
                    // have to use the class loader of the EventAdmin service
                    ClassLoader classLoader = service.getClass().getClassLoader();

                    Object evt = classLoader.loadClass("org.osgi.service.event.Event")
                            .getConstructor(String.class, Map.class).newInstance(topic, properties);

                    logger.info("Sending {} event with the properties {} to the topic {}...", asynchronous ? "asynchronous" : "synchronous", properties, topic);

                    MethodUtils.invokeExactMethod(service, asynchronous ? "postEvent" : "sendEvent", evt);

                    logger.info("Event sent to the topic {}", topic);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException
                        | InstantiationException | IllegalArgumentException | SecurityException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
    }

    /**
     * Returns <code>true</code> if the OSGi container is completely started.
     *
     * @return <code>true</code> if the OSGi container is completely started; <code>false</code> otherwise
     */
    public boolean isStarted() {
        return finalStartLevelReachedLatch.getCount() == 0;
    }

    /**
     * Returns <code>true</code> if this is the first startup of the OSGi container
     *
     * @return <code>true</code> if this is the first startup of the OSGi container
     */
    public boolean isFirstStartup() {
        return firstStartup;
    }

    private void setupStartupListener() {
        initialFrameworkStartLevel = Integer.parseInt(System.getProperty(Constants.FRAMEWORK_BEGINNING_STARTLEVEL));
        osgiStartupWaitTimeout = Long.getLong("org.jahia.osgi.startupWaitTimeout", 10 * 60 * 1000L);
        main.getFramework().getBundleContext().addFrameworkListener(this);

        startLevelTimer.schedule(new StartLevelChecker(), 1000L, 1000L);
    }

    private void setupSystemProperties() {

        @SuppressWarnings("unchecked")
        Map<String, String> unreplaced = (Map<String, String>) SpringContextSingleton.getBean("osgiProperties");
        Map<String, String> newSystemProperties = new TreeMap<>();

        PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("${", "}");
        Properties systemProps = System.getProperties();

        for (Map.Entry<String, String> entry : unreplaced.entrySet()) {
            newSystemProperties.put(entry.getKey(), placeholderHelper.replacePlaceholders(entry.getValue(), systemProps));
        }

        for (Map.Entry<String, String> property : newSystemProperties.entrySet()) {
            String propertyName = property.getKey();
            String oldPropertyValue = System.getProperty(propertyName);
            String newPropertyValue = property.getValue();
            boolean valueHasChanged = oldPropertyValue != null
                    && !StringUtils.equals(oldPropertyValue, newPropertyValue);
            if (valueHasChanged) {
                logger.warn("Overriding system property {}={} with new value={}", propertyName, oldPropertyValue, newPropertyValue);
            }
            if (oldPropertyValue == null || valueHasChanged) {
                JahiaContextLoaderListener.setSystemProperty(propertyName, newPropertyValue);
            }
        }

        File file = new File(System.getProperty("karaf.etc"), "config.properties");
        org.apache.felix.utils.properties.Properties karafConfigProperties = null;
        try {
            karafConfigProperties = PropertiesLoader.loadConfigProperties(file);
        } catch (Exception e) {
            logger.error("Unable to load properties from file {}. Cause: {}", file, e.getMessage(), e);
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

    /**
     * Start framework
     */
    public void start() {

        try {
            // Try to figure out if the installation folder has been moved/renamed, and update file references in configuration/auxiliary files correspondingly if so.
            updateFileReferencesIfNeeded();
        } catch (Exception e) {
            // In case the attempt fails for some reason, still give the application a chance to start.
            logger.error("Error updating file references", e);
        }

        startTime = System.currentTimeMillis();
        logger.info("Starting OSGi platform service");
        startKaraf();
        servletContext.setAttribute(BundleContext.class.getName(), main.getFramework().getBundleContext());
    }

    private void startKaraf() {
        try {
            setupSystemProperties();
            firstStartup = !new File(System.getProperty("org.osgi.framework.storage"), "bundle0").exists();

            if (!firstStartup) {
                checkJavaVersion();
            }

            bundleStarter = new BundleStarter();
            main = new Main(new String[0]);
            main.launch();
            setupStartupListener();
            bundleStarter.startInitialBundlesIfNeeded();
        } catch (Exception e) {
            main = null;
            logger.error("Error starting OSGi container");
            throw new JahiaRuntimeException("Error starting OSGi container", e);
        }
    }

    private void checkJavaVersion() throws IOException {
        String javaVersion = StringUtils.substringBefore(System.getProperty("java.version"), "_");
        if (javaVersion.startsWith("1.")) {
            javaVersion = StringUtils.substringBeforeLast(javaVersion, ".") + ".0";
        } else {
            javaVersion = StringUtils.substringBefore(javaVersion, ".") + ".0.0";
        }
        File wiring = new File(System.getProperty("org.osgi.framework.storage"), "bundle0/wiring");
        if (wiring.exists()) {
            for (File file : Objects.requireNonNull(wiring.listFiles())) {
                if (checkWiringFile(javaVersion, wiring, file)) {
                    return;
                }
            }
        }
    }

    private boolean checkWiringFile(String javaVersion, File wiring, File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file)) {
            Iterator<String> lines = IOUtils.readLines(input, StandardCharsets.UTF_8).iterator();
            while (lines.hasNext()) {
                String line = lines.next();
                if (line.startsWith("osgi.ee; (&(osgi.ee=JavaSE)")) {
                    String nextList = lines.next();
                    if (!nextList.contains(javaVersion)) {
                        FileUtils.deleteDirectory(wiring);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Shuts the OSGi container down.
     *
     * @throws BundleException in case of an error
     */
    public void stop() throws BundleException {
        if (this.main != null) {
            destroyTimer();
            servletContext.removeAttribute(BundleContext.class.getName());
            try {
                main.destroy();
            } catch (Exception e) {
                logger.error("Error shutting down Karaf framework", e);
            }
        }
        logger.info("OSGi framework stopped");
    }

    private void destroyTimer() {
        if (startLevelTimer != null) {
            try {
                startLevelTimer.cancel();
            } catch (Exception e) {
                logger.warn("Error terminating timer thread", e);
            } finally {
                startLevelTimer = null;
            }
        }
    }

    private void updateFileReferencesIfNeeded() {
        File script = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/scripts/groovy/updateFileReferences.groovy");
        if (!script.isFile()) {
            return;
        }

        ScriptEngine scriptEngine;
        try {
            scriptEngine = ScriptEngineUtils.getInstance().scriptEngine(FilenameUtils.getExtension(script.getName()));
        } catch (ScriptException e) {
            throw new JahiaRuntimeException(e);
        }
        if (scriptEngine == null) {
            throw new IllegalStateException("No script engine available");
        }

        ScriptContext scriptContext = new SimpleScriptContext();
        Bindings bindings = new SimpleBindings();
        bindings.put("log", logger);
        bindings.put("logger", logger);
        scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        try (FileInputStream scriptInputStream = new FileInputStream(script);
             InputStreamReader scriptReader = new InputStreamReader(scriptInputStream, StandardCharsets.UTF_8);
             StringWriter out = new StringWriter()) {
            scriptContext.setWriter(out);
            scriptEngine.eval(scriptReader, scriptContext);
        } catch (ScriptException | IOException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    // Startup lifecycle

    @Override
    public void frameworkEvent(FrameworkEvent event) {
        if (event.getType() == FrameworkEvent.STARTLEVEL_CHANGED && BundleLifecycleUtils.getFrameworkStartLevel() >= initialFrameworkStartLevel) {
            notifyInitialStartLevelReached();
        }
    }

    /**
     * Notifies the service that the Initial start leve has been reached
     */
    public void notifyInitialStartLevelReached() {
        logger.info("Inital start level reached {}", initialFrameworkStartLevel);
        initialStartLevelReachedLatch.countDown();
        sendEvent(EVENT_TOPIC_LIFECYCLE, Collections.singletonMap("type", EVENT_TYPE_INITIAL_START_LEVEL_REACHED), false);
    }

    /**
     * Wait for initial start level to be reached
     */
    public void waitForInitialStartLevelReached() {
        waitForLatch(initialStartLevelReachedLatch, "initial start level to be reached");
    }

    /**
     * Notifies the service that the FileInstall watcher has been started and processed the found modules.
     * @param createdOnStartup List of bundles that have installed by fileinstall
     */
    public void notifyFileInstallStarted(List<Long> createdOnStartup) {
        bundleStarter.afterFileInstallStarted(createdOnStartup);

        logger.info("FileInstall watcher started");
        fileInstallStartedLatch.countDown();
        sendEvent(EVENT_TOPIC_LIFECYCLE, Collections.singletonMap("type", EVENT_TYPE_FILEINSTALL_STARTED), false);
    }

    /**
     * Wait for fileinstall
     */
    public void waitForFileInstallStarted() {
        waitForLatch(fileInstallStartedLatch, "file-install");
    }

    /**
     * Notifies the service that the Spring bridge has been started
     */
    public void notifySpringBridgeStarted() {
        logger.info("Spring bridge started");
        springBridgeStartedLatch.countDown();
        sendEvent(EVENT_TOPIC_LIFECYCLE, Collections.singletonMap("type", EVENT_TYPE_SPRING_BRIDGE_STARTED), false);
    }

    /**
     * Wait for Spring bridge to be started
     */
    public void waitForSpringBridgeStarted() {
        waitForLatch(springBridgeStartedLatch, "Spring bridge to be started");
    }

    /**
     * Raise the start level to its final state
     */
    public void raiseStartLevel() {
        logger.info("Raising start level to {}", finalFrameworkStartLevel);
        FrameworkStartLevel frameworkStartLevel = main.getFramework().getBundleContext().getBundle(0).adapt(FrameworkStartLevel.class);
        frameworkStartLevel.setStartLevel(finalFrameworkStartLevel, event -> notifyFinalStartLevelReached());
    }

    /**
     * Notifies the service that the Spring bridge has been started
     */
    public void notifyFinalStartLevelReached() {
        logger.info("Final start level reached");
        logger.info("OSGi platform service initialized in {} ms", (System.currentTimeMillis() - startTime));
        finalStartLevelReachedLatch.countDown();
        sendEvent(EVENT_TOPIC_LIFECYCLE, Collections.singletonMap("type", EVENT_TYPE_FINAL_START_LEVEL_REACHED), false);
    }

    /**
     * Wait for final start level to be reached
     */
    public void waitForFinalStartLevelReached() {
        waitForLatch(finalStartLevelReachedLatch, "final start level to be reached");
    }

    /**
     * Notifies the service that the FileInstall watcher has been started and processed the found modules.
     */
    public void notifyClusterStarted() {
        logger.info("Cluster started");
        clusterStartedLatch.countDown();
        sendEvent(EVENT_TOPIC_LIFECYCLE, Collections.singletonMap("type", EVENT_TYPE_CLUSTER_STARTED), false);
    }

    /**
     * Wait for cluster sync to be finished
     */
    public void waitForClusterStarted() {
        if (SettingsBean.getInstance().isClusterActivated()) {
            waitForLatch(clusterStartedLatch, "cluster sync to be finished");
        }
    }

    private void waitForLatch(CountDownLatch latch, String name) {
        if (osgiStartupWaitTimeout > 0) {
            logger.info("Waiting for {} ...", name);
            try {
                if (!latch.await(osgiStartupWaitTimeout, TimeUnit.MILLISECONDS)) {
                    logger.warn("Timeout reached when waiting for {}", name);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JahiaRuntimeException(e);
            }
        }
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static final class Holder {
        static final FrameworkService INSTANCE = new FrameworkService(JahiaContextLoaderListener.getServletContext());

        private Holder() {
        }
    }

    /**
     * Timer task that checks if we have reached the OSGi framework beginning start level.
     *
     * @author Sergiy Shyrkov
     */
    private class StartLevelChecker extends TimerTask {

        @Override
        public void run() {
            if (initialStartLevelReachedLatch.getCount() == 0) {
                // we are done here: cancel the task
                cancel();
            } else if (BundleLifecycleUtils.getFrameworkStartLevel() >= initialFrameworkStartLevel) {
                logger.info("Framework start level reached {}", initialFrameworkStartLevel);
                initialStartLevelReachedLatch.countDown();
                // we are done here: cancel the task
                cancel();
            }
        }

        @Override
        public boolean cancel() {
            logger.info("Cancelling the start level checker task");
            boolean result = super.cancel();
            FrameworkService.this.destroyTimer();

            return result;
        }
    }
}
