/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.karaf.main.Main;
import org.apache.karaf.util.config.PropertiesLoader;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.ScriptEngineUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PropertyPlaceholderHelper;

import com.google.common.base.Charsets;

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

    /**
     * Timer task that checks if we have reached the OSGi framework beginning start level.
     *
     * @author Sergiy Shyrkov
     */
    private class StartLevelChecker extends TimerTask {

        private int frameworkBeginningStartLevel;

        public StartLevelChecker(int frameworkBeginningStartLevel) {
            this.frameworkBeginningStartLevel = frameworkBeginningStartLevel;
        }

        @Override
        public void run() {
            synchronized (FrameworkService.this) {
                if (frameworkStartLevelReached) {
                    // we are done here: cancel the task
                    cancel();
                } else if (BundleLifecycleUtils.getFrameworkStartLevel() >= frameworkBeginningStartLevel) {
                    frameworkStartLevelReached = true;
                    logger.info("Framework start level reached " + frameworkBeginningStartLevel);
                    notifyStarted();
                    // we are done here: cancel the task
                    cancel();
                }
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

    public static final String EVENT_TOPIC_LIFECYCLE = "org/jahia/dx/lifecycle";

    public static final String EVENT_TYPE_CLUSTERING_FEATURE_INSTALLED = "clusteringFeatureInstalled";

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

        instance.bundleStarter.afterFileInstallStarted();

        synchronized (instance) {
            instance.fileInstallStarted = true;
            logger.info("FileInstall watcher started");
            instance.notifyStarted();
        }
    }

    /**
     * Notify this service that the container has actually started.
     */
    private void notifyStarted() {
        if (frameworkStartLevelReached && fileInstallStarted) {
            // send synchronous event about startup
            sendEvent(EVENT_TOPIC_LIFECYCLE, Collections.singletonMap("type", EVENT_TYPE_OSGI_STARTED), false);

            // both pre-requisites for stratup are fulfilled
            // notify any threads waiting
            notifyAll();
            logger.info("OSGi platform service initialized in {} ms", (System.currentTimeMillis() - startTime));
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

    private boolean fileInstallStarted;
    private boolean frameworkStartLevelReached;
    private Main main;
    private final ServletContext servletContext;
    private long startTime;
    private int frameworkBeginningStartLevel = 100;

    private BundleStarter bundleStarter;

    private Timer startLevelTimer = new Timer("OSGi-FrameworkService-Startup-Timer", true);

    private FrameworkService(ServletContext servletContext) {
        this.servletContext = servletContext;
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

    @Override
    public void frameworkEvent(FrameworkEvent event) {
        if (event.getType() == FrameworkEvent.STARTLEVEL_CHANGED && BundleLifecycleUtils.getFrameworkStartLevel() >= frameworkBeginningStartLevel) {
            synchronized (this) {
                if (!frameworkStartLevelReached) {
                    frameworkStartLevelReached = true;
                    logger.info("Framework start level reached " + frameworkBeginningStartLevel);
                    notifyStarted();
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
        return frameworkStartLevelReached && fileInstallStarted;
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
        frameworkBeginningStartLevel = Integer.parseInt(System.getProperty(Constants.FRAMEWORK_BEGINNING_STARTLEVEL));
        main.getFramework().getBundleContext().addFrameworkListener(this);

        startLevelTimer.schedule(new StartLevelChecker(frameworkBeginningStartLevel), 1000L, 1000L);
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
        Map<String, String> filteredOutSystemProperties = filterOutSystemProperties();
        try {
            setupSystemProperties();
            bundleStarter = new BundleStarter();
            main = new Main(new String[0]);
            main.launch();
            setupStartupListener();
            bundleStarter.startInitialBundlesIfNeeded();
        } catch (Exception e) {
            main = null;
            logger.error("Error starting OSGi container", e);
            throw new JahiaRuntimeException("Error starting OSGi container", e);
        } finally {
            restoreSystemProperties(filteredOutSystemProperties);
        }
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

    protected void destroyTimer() {
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
                InputStreamReader scriptReader = new InputStreamReader(scriptInputStream, Charsets.UTF_8);
                StringWriter out = new StringWriter()) {
            scriptContext.setWriter(out);
            scriptEngine.eval(scriptReader, scriptContext);
        } catch (ScriptException | IOException e) {
            throw new JahiaRuntimeException(e);
        }
    }
}