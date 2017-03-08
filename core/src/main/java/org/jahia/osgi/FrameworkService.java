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
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.felix.cm.file.ConfigurationHandler;
import org.apache.karaf.main.Main;
import org.apache.karaf.util.config.PropertiesLoader;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
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

    private static final FileReferenceUpdater FILE_REFERENCE_UPDATER_FSPATH = new FileReferenceUpdaterFsPath();

    // NOTE: Order of updaters matters.
    // Especially, it is critical that the FileReferenceUpdaterFsPath is configured before the FileReferenceUpdaterRootedWindowsFsPath.
    private static final FileReferenceUpdater[] FILE_REFERENCE_UPDATERS = {
        new FileReferenceUpdaterUri(),
        FILE_REFERENCE_UPDATER_FSPATH,
        new FileReferenceUpdaterRootedWindowsFsPath(),
        new FileReferenceUpdaterMavenRepositories(FILE_REFERENCE_UPDATER_FSPATH, ".mvn.defaultRepositories", ".mvn.repositories")
    };

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

    private boolean fileInstallStarted;
    private boolean frameworkStartLevelChanged;
    private Main main;
    private final ServletContext servletContext;
    private long startTime;

    private BundleStarter bundleStarter;

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
        if (event.getType() == FrameworkEvent.STARTLEVEL_CHANGED) {
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
            servletContext.removeAttribute(BundleContext.class.getName());
            try {
                main.destroy();
            } catch (Exception e) {
                logger.error("Error shutting down Karaf framework", e);
            }
        }
        logger.info("OSGi framework stopped");
    }

    private void updateFileReferencesIfNeeded() throws IOException {

        File varDir = new File(SettingsBean.getInstance().getJahiaVarDiskPath());

        File instancePropertiesFile = getKarafInstancePropertiesFile();
        if (!instancePropertiesFile.exists()) {
            logger.info("'{}' file not found. Assuming the instance is starting for the first time, so no file reference updates needed.", instancePropertiesFile);
            return;
        }

        Properties instanceProperties = new Properties();
        try (FileInputStream instancePropertiesIn = new FileInputStream(instancePropertiesFile)) {
            instanceProperties.load(instancePropertiesIn);
        }

        File oldKarafDir = new File(instanceProperties.getProperty("item.0.loc"));
        File oldVarDir = oldKarafDir.getParentFile();

        if (varDir.equals(oldVarDir)) {
            logger.info("The var dir hasn't been changed since last start, so no file path updates needed.");
            return;
        }

        logger.info("The var dir changed from '{}' to '{}', so updating file references correspondingly", oldVarDir, varDir);
        updateFileReferences(oldVarDir, varDir);
    }

    private void updateFileReferences(File oldVarDir, File varDir) throws IOException {

        FileHandler propertiesFileHandler = new FileHandler() {

            @Override
            public Map<Object, Object> readProperties(File propertiesFile) throws IOException {
                Properties properties = new Properties();
                try (FileInputStream propertiesFileIn = new FileInputStream(propertiesFile)) {
                    properties.load(propertiesFileIn);
                }
                return properties;
            }

            @Override
            public void writeProperties(File propertiesFile, Map<Object, Object> properties) throws IOException {
                Properties props = new Properties();
                props.putAll(properties);
                try (FileOutputStream propertiesFileOut = new FileOutputStream(propertiesFile)) {
                    props.store(propertiesFileOut, null);
                }
            }
        };

        updateFileReferencesInFile(getKarafInstancePropertiesFile(), oldVarDir, varDir, propertiesFileHandler);

        String moduleBundleLocationMapPath = getFileInstallConfig().getProperty("felix.fileinstall.bundleLocationMapFile");
        File moduleBundleLocationMapFile = new File(moduleBundleLocationMapPath);
        if (moduleBundleLocationMapFile.exists()) {
            updateFileReferencesInFile(moduleBundleLocationMapFile, oldVarDir, varDir, propertiesFileHandler);
        }

        String frameworkStoragePath = getOsgiConfig().getProperty("org.osgi.framework.storage");
        File frameworkStorageDir = new File(frameworkStoragePath);

        if (frameworkStorageDir.isDirectory()) {

            updateFileReferencesInConfigFiles(frameworkStorageDir, oldVarDir, varDir, new FileHandler() {

                @Override
                public Map<Object, Object> readProperties(File configFile) throws IOException {
                    Dictionary<?, ?> props;
                    try (FileInputStream configFileIn = new FileInputStream(configFile)) {
                        props = ConfigurationHandler.read(configFileIn);
                    }
                    HashMap<Object, Object> properties = new HashMap<>(props.size());
                    for (Enumeration<?> propertyKeys = props.keys(); propertyKeys.hasMoreElements(); ) {
                        Object propertyKey = propertyKeys.nextElement();
                        Object propertyValue = props.get(propertyKey);
                        properties.put(propertyKey, propertyValue);
                    }
                    return properties;
                }

                @Override
                public void writeProperties(File configFile, Map<Object, Object> properties) throws IOException {
                    Hashtable<Object, Object> props = new Hashtable<>(properties.size());
                    props.putAll(properties);
                    try (FileOutputStream configFileOut = new FileOutputStream(configFile)) {
                        ConfigurationHandler.write(configFileOut, props);
                    }
                }
            });
        }
    }

    private void updateFileReferencesInConfigFiles(File baseDir, File oldVarDir, File newVarDir, FileHandler configFileHandler) throws IOException {
        for (File file : baseDir.listFiles()) {
            if (file.isDirectory()) {
                updateFileReferencesInConfigFiles(file, oldVarDir, newVarDir, configFileHandler);
            } else if (file.getName().endsWith(".config")) {
                updateFileReferencesInFile(file, oldVarDir, newVarDir, configFileHandler);
            }
        }
    }

    private void updateFileReferencesInFile(File file, File oldVarDir, File newVarDir, FileHandler fileHandler) throws IOException {

        Path oldVarPath = Paths.get(oldVarDir.getAbsolutePath());
        Path newVarPath = Paths.get(newVarDir.getAbsolutePath());
        Map<Object, Object> properties = fileHandler.readProperties(file);
        boolean changed = false;

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {

            Object propertyKey = entry.getKey();
            Object propertyValue = entry.getValue();

            // Chain of responsibility.
            for (FileReferenceUpdater updater : FILE_REFERENCE_UPDATERS) {
                Object newPropertyValue = updater.updateIfFamiliar(propertyKey, propertyValue, oldVarPath, newVarPath);
                if (newPropertyValue != null) {
                    properties.put(propertyKey, newPropertyValue);
                    changed = true;
                    logger.debug("Changed '{}' property value from '{}' to '{}' in '{}'", new Object[] {propertyKey, propertyValue, newPropertyValue, file});
                    break;
                }
            }
        }
        if (changed) {
            fileHandler.writeProperties(file, properties);
            logger.debug("Saved changes to '{}'", file);
        }
    }

    private static Properties getOsgiConfig() {
        return (Properties) SpringContextSingleton.getBean("combinedOsgiProperties");
    }

    private static Properties getFileInstallConfig() {
        return (Properties) SpringContextSingleton.getBean("felixFileInstallConfig");
    }

    private static File getKarafInstancePropertiesFile() {
        String karafInstancesPath = getOsgiConfig().getProperty("karaf.instances");
        return FileUtils.getFile(karafInstancesPath, "instance.properties");
    }

    private static Path canonizeIfPossible(Path path) {
        try {
            return path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            // This path does not exist in reality.
            return path;
        }
    }

    private interface FileHandler {

        Map<Object, Object> readProperties(File file) throws IOException;
        void writeProperties(File file, Map<Object, Object> properties) throws IOException;
    }

    private interface FileReferenceUpdater {

        Object updateIfFamiliar(Object propertyKey, Object propertyValue, Path oldVarPath, Path newVarPath);
    }

    private static abstract class FileReferenceUpdaterSimpleBase implements FileReferenceUpdater {

        @Override
        public Object updateIfFamiliar(Object propertyKey, Object propertyValue, Path oldVarPath, Path newVarPath) {

            if (!(propertyValue instanceof String)) {
                // File reference may only be a String property.
                return null;
            }

            Path fileReference = toPath((String) propertyValue);
            if (fileReference == null) {
                // Not a file reference format the updater is able to handle (maybe not a file reference at all).
                return null;
            }

            // Update the file reference.
            Path relativePath;
            try {
                relativePath = oldVarPath.relativize(fileReference);
            } catch (IllegalArgumentException e) {
                return null;
            }
            fileReference = newVarPath.resolve(relativePath);

            return toString(fileReference);
        }

        protected abstract Path toPath(String fileReferenceString);
        protected abstract String toString(Path fileReference);
    }

    /*
     * Handles values like
     * file:/C:/Program%20Files/apache-tomcat-8.0.30/digital-factory-data/karaf/etc/jmx.acl.java.lang.Memory.cfg
     * or
     * file:/home/jahia/install/QA-9314/digital-factory-data/karaf/etc/jmx.acl.java.lang.Memory.cfg
     */
    private static class FileReferenceUpdaterUri extends FileReferenceUpdaterSimpleBase {

        @Override
        protected Path toPath(String fileReferenceString) {
            URI uri;
            try {
                uri = new URI(fileReferenceString);
            } catch (URISyntaxException e) {
                // Not a file URI the updater is able to handle.
                return null;
            }
            try {
                return Paths.get(uri);
            } catch (IllegalArgumentException | FileSystemNotFoundException e) {
                // Not a file URI the updater is able to handle.
                return null;
            }
        }

        @Override
        protected String toString(Path fileReference) {
            String fileReferenceString = canonizeIfPossible(fileReference).toString();
            fileReferenceString = StringUtils.replace(fileReferenceString, "\\", "/");
            URI uri;
            try {
                uri = new URI("file", "/" + fileReferenceString, null);
            } catch (URISyntaxException e) {
                throw new JahiaRuntimeException(e);
            }
            return uri.toASCIIString();
        }
    }

    /*
     * Handles values like
     * C:\\Program Files\\apache-tomcat-8.0.30\\digital-factory-data\\karaf/deploy
     * or
     * /home/jahia/install/QA-9314/digital-factory-data/karaf/deploy
     */
    private static class FileReferenceUpdaterFsPath extends FileReferenceUpdaterSimpleBase {

        @Override
        protected Path toPath(String fileReferenceString) {
            Path path;
            try {
                path = Paths.get(fileReferenceString);
            } catch (InvalidPathException e) {
                // Not a file path the updater is able to handle.
                return null;
            }
            if (!path.isAbsolute()) {
                // We only update absolute paths.
                return null;
            }
            return path;
        }

        @Override
        protected String toString(Path fileReference) {
            return canonizeIfPossible(fileReference).toString();
        }
    }

    /*
     * Handles specific values where Windows file path is prefixed with slash like
     * /C:/Program Files/apache-tomcat-8.0.30/digital-factory-data/modules/advanced-visibility-7.1.1.jar
     */
    private static class FileReferenceUpdaterRootedWindowsFsPath extends FileReferenceUpdaterFsPath {

        @Override
        protected Path toPath(String fileReferenceString) {
            if (!fileReferenceString.startsWith("/")) {
                return null;
            }
            return super.toPath(fileReferenceString.substring(1));
        }

        @Override
        protected String toString(Path fileReference) {
            return ("/" + super.toString(fileReference));
        }
    }

    /*
     * Handles Karaf specific values that represent a comma separated Maven repository list like
     * file:C:\\Program Files\\apache-tomcat-8.0.30\\webapps\\ROOT\\WEB-INF\\karaf/system@id\=system.repository@snapshots, file:C:\\Program Files\\apache-tomcat-8.0.30\\digital-factory-data\\karaf\\data/kar@id\=kar.repository@multi@snapshots
     */
    private static class FileReferenceUpdaterMavenRepositories implements FileReferenceUpdater {

        private FileReferenceUpdater fileReferenceUpdaterFsPath;
        private String[] propertyKeyEndings;

        public FileReferenceUpdaterMavenRepositories(FileReferenceUpdater fileReferenceUpdaterFsPath, String... propertyKeyEndings) {
            this.propertyKeyEndings = propertyKeyEndings;
            this.fileReferenceUpdaterFsPath = fileReferenceUpdaterFsPath;
        }

        @Override
        public Object updateIfFamiliar(Object propertyKey, Object propertyValue, Path oldVarPath, Path newVarPath) {

            if (!(propertyKey instanceof String && propertyValue instanceof String)) {
                // File reference may only be a part of a String property.
                return null;
            }
            if (!StringUtils.endsWithAny((String) propertyKey, propertyKeyEndings)) {
                // Not a property this updater is familiar with.
                return null;
            }

            String[] values = StringUtils.split((String) propertyValue, ',');
            String[] newValues = new String[values.length];
            boolean changed = false;
            for (int i = 0; i < values.length; i++) {
                String value = values[i].trim();
                if (!StringUtils.startsWithIgnoreCase(value, "file:")) {
                    // Not a file reference.
                    newValues[i] = value;
                    continue;
                }
                int atIndex = value.indexOf('@');
                if (atIndex < 0) {
                    // Not a format this updater is familiar with.
                    newValues[i] = value;
                    continue;
                }
                String path = value.substring("file:".length(), atIndex);
                String rest = value.substring(atIndex);
                Object newPath = fileReferenceUpdaterFsPath.updateIfFamiliar(null, path, oldVarPath, newVarPath);
                if (newPath == null) {
                    // Not a file path this updater is able to handle.
                    newValues[i] = value;
                    continue;
                }
                newValues[i] = "file:" + newPath + rest;
                changed = true;
            }

            if (changed) {
                return StringUtils.join(newValues, ',');
            } else {
                return null;
            }
        }
    }
}
