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

import org.apache.karaf.main.Main;
import org.apache.karaf.util.config.PropertiesLoader;
import org.jahia.bin.Jahia;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.ModuleState;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.*;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PropertyPlaceholderHelper;

import javax.servlet.ServletContext;

import java.io.File;
import java.util.*;

/**
 * OSGi framework service. This class is responsible for setting up and starting the embedded Karaf OSGi runtime.
 *
 * @author Serge Huber
 */
public class FrameworkService implements BundleListener, FrameworkListener {

    private static final org.springframework.core.Constants FRAMEWORK_EVENTS = new org.springframework.core.Constants(FrameworkEvent.class);

    private static final Logger logger = LoggerFactory.getLogger(FrameworkService.class);

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final FrameworkService INSTANCE = new FrameworkService(JahiaContextLoaderListener.getServletContext());

        private Holder() {
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

    private final ServletContext servletContext;
    
    private Main main;

    private boolean started;

    private long lastBundleEventTime = -1;
    private long timeoutBetweenBundleEvents = 30 * 1000;
    private long startupEventCount = 0;
    private long totalIntervalTime = 0;
    Set<String> modulesInDirectory = new HashSet<>();
    private boolean countModules = true;
    private Timer startupBundleEventTimer = new Timer("startup-bundle-event-timer", true);

    private FrameworkService(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void start() throws BundleException {
        startKaraf();
        servletContext.setAttribute(BundleContext.class.getName(), main.getFramework().getBundleContext());
        started = true;
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

    /**
     * Returns <code>true</code> if the OSGi container is completely started.
     *
     * @return <code>true</code> if the OSGi container is completely started; <code>false</code> otherwise
     */
    public boolean isStarted() {
        return started;
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
     * Notify this service that the container has actually started.
     */
    public static void notifyStarted() {
        logger.info("Got started event");
        final FrameworkService instance = getInstance();
        synchronized (instance) {
            logger.info("Started event arrived");
            instance.started = true;
            instance.notifyAll();
            logger.info("Notified all about OSGi container started event");
        }
    }

    private void startKaraf() {
        try {
            setupSystemProperties();
            main = new Main(new String[0]);
            main.launch();
            setupStartupListener();
        } catch (Exception e) {
            main = null;
            logger.error("Error starting Karaf container", e);
            throw new JahiaRuntimeException("Error starting Karaf container", e);
        }

    }

    private void setupSystemProperties() {

        String varDiskPath = SettingsBean.getInstance().getJahiaVarDiskPath();

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
            if (System.getProperty(propertyName) != null) {
                logger.warn("Overriding system property " + propertyName + "=" + System.getProperty(propertyName) + " with new value=" + property.getValue());
            }
            System.setProperty(propertyName, property.getValue());
        }

        File file = new File(varDiskPath + "/etc", "config.properties");
        org.apache.felix.utils.properties.Properties karafConfigProperties = null;
        try {
            karafConfigProperties = PropertiesLoader.loadConfigProperties(file);
        } catch (Exception e) {
            e.printStackTrace();
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
            System.setProperty("org.osgi.framework.system.packages.extra", extraSystemPackages.toString());
        }

    }


    private void setupStartupListener() {
        if (countModules) {
            File modulesDiskPath = new File(SettingsBean.getInstance().getJahiaModulesDiskPath());
            modulesInDirectory.clear();
            if (modulesDiskPath.exists()) {
                File[] moduleFiles = modulesDiskPath.listFiles();
                for (File moduleFile : moduleFiles) {
                    if (moduleFile.isFile() && moduleFile.getName().endsWith(".jar")) {
                        modulesInDirectory.add(moduleFile.getAbsolutePath());
                    }
                }
            }
        }
        main.getFramework().getBundleContext().addBundleListener(this);
        main.getFramework().getBundleContext().addFrameworkListener(this);
        startupBundleEventTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (lastBundleEventTime != -1 &&
                        (System.currentTimeMillis() - lastBundleEventTime) > timeoutBetweenBundleEvents) {
                    shutdownStartupListener();
                }
            }
        }, 1000, 1000);
    }

    private void shutdownStartupListener() {
        startupBundleEventTimer.cancel();
        startupBundleEventTimer = null;
        main.getFramework().getBundleContext().removeBundleListener(this);
        main.getFramework().getBundleContext().removeFrameworkListener(this);

        double averageIntervalTime = totalIntervalTime / startupEventCount;
        logger.info("Average interval between bundle events=" + Double.toString(averageIntervalTime));

        long initializationTime = System.currentTimeMillis() - JahiaContextLoaderListener.getStartupTime() ;
        StringBuilder out = new StringBuilder(256);
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            out.append("\n--------------------------------------------------------------------------------------------------" +
                    "\n  D E V E L O P M E N T   M O D E   A C T I V E" +
                    "\n" +
                    "\n  In development mode, Digital Experience Manager will allow JSPs to be modified, modules to be" +
                    "\n  re-deployed and other modifications to happen immediately, but these DO have a performance impact." +
                    "\n  It is strongly recommended to switch to production mode when running performance tests or going live." +
                    "\n  The setting to change modes is called operatingMode in the jahia.properties configuration file.");
        } else if (SettingsBean.getInstance().isDistantPublicationServerMode()) {
            out.append("\n--------------------------------------------------------------------------------------------------" +
                    "\n  D I S T A N T  P U B L I C A T I O N  S E R V E R  M O D E   A C T I V E");
        } else {
            out.append("\n--------------------------------------------------------------------------------------------------" +
                    "\n  P R O D U C T I O N   M O D E   A C T I V E");
        }
        out.append("\n--------------------------------------------------------------------------------------------------\n");
        appendModulesInfo(out);
        out.append("\n--------------------------------------------------------------------------------------------------"+
                "\n  ").append(Jahia.getFullProductVersion()).append(" is now ready. Initialization completed in ").append((initializationTime/1000)).append(" seconds");
        out.append("\n--------------------------------------------------------------------------------------------------");
        logger.info(out.toString());

    }

    private void appendModulesInfo(StringBuilder out) {
        JahiaTemplateManagerService templateService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        out.append("  Modules:");
        for (ModuleState.State state : ModuleState.State.values()) {
            List<Bundle> modules = templateService.getModulesByState(state);
            if (modules.isEmpty()) {
                continue;
            }
            out.append("\n      ").append(state).append(": ").append(modules.size());
        }
    }


    public BundleStats getBundleStats() {
        Bundle[] bundles = main.getFramework().getBundleContext().getBundles();
        int numActive = 0;
        int numBundles = bundles.length;
        int numJahiaModules = 0;
        int numFragments = 0;
        for (Bundle bundle : bundles) {
            if (bundle.getHeaders().get(Constants.FRAGMENT_HOST) != null) {
                numBundles--;
                numFragments++;
                if (bundle.getState() == Bundle.RESOLVED) {
                    numJahiaModules += countNumJahiaModules(bundle);
                }
            } else {
                if (bundle.getState() == Bundle.ACTIVE) {
                    numActive++;
                    numJahiaModules += countNumJahiaModules(bundle);
                }
            }
        }
        BundleStats stats = new BundleStats();
        stats.numActive = numActive;
        stats.numTotal = numBundles;
        stats.numJahiaModules = numJahiaModules;
        stats.numFragments = numFragments;
        return stats;
    }

    private int countNumJahiaModules(Bundle bundle) {
        String moduleLocation = bundle.getLocation();
        if (moduleLocation.startsWith("legacydepends:")) {
            moduleLocation = moduleLocation.substring("legacydepends:".length());
        }
        if (moduleLocation.startsWith("file:")) {
            moduleLocation = moduleLocation.substring("file:".length());
        }
        if (modulesInDirectory.contains(moduleLocation)) {
            return 1;
        }
        return 0;
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        startupEventCount++;
        if (lastBundleEventTime > 0) {
            totalIntervalTime += System.currentTimeMillis() - lastBundleEventTime;
        }
        lastBundleEventTime = System.currentTimeMillis();
        BundleStats stats = getBundleStats();
        if (countModules) {
            if (stats.numJahiaModules >= modulesInDirectory.size()) {
                // become more impatient if the standard modules have all been deployed.
                timeoutBetweenBundleEvents = 10*1000;
            }
        }
    }

    @Override
    public void frameworkEvent(FrameworkEvent event) {
        logger.info("Framework event type=" + FRAMEWORK_EVENTS.toCode(event.getType(), "") + " bundle=" + event.getBundle());
    }

    class BundleStats {
        int numJahiaModules = 0;
        int numActive = 0;
        int numTotal = 0;
        int numFragments = 0;
    }

}
