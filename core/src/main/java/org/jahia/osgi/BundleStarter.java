/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.karaf.main.ConfigProperties;
import org.apache.karaf.main.Main;
import org.apache.karaf.main.util.ArtifactResolver;
import org.apache.karaf.main.util.SimpleMavenResolver;
import org.apache.karaf.util.config.PropertiesLoader;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.util.dag.DAG;
import org.codehaus.plexus.util.dag.TopologicalSorter;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.util.ModuleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class, responsible for starting bundles on demand.
 *
 * @author Sergiy Shyrkov
 */
class BundleStarter {

    private static final Logger logger = LoggerFactory.getLogger(BundleStarter.class);

    private static final String MARKER_INITIAL_BUNDLES = "[initial-bundles].dostart";

    private static final String MARKER_MIGRATE_BUNDLES = "[migrate-bundles].dostart";
    
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

    private static void startBundles(Map<Bundle, JahiaTemplatesPackage> toBeStarted, boolean useModuleManagerApi) {
        logger.info("Will start {} bundle(s)", toBeStarted.size());
        Collection<Bundle> sortedBundles = getSortedModules(toBeStarted);

        try {
            for (Bundle bundle : sortedBundles) {
                try {
                    logger.info("Triggering start for bundle {}/{}", bundle.getSymbolicName(), bundle.getVersion());
                    if (useModuleManagerApi) {
                        ModuleUtils.getModuleManager().start(BundleInfo.fromBundle(bundle).getKey(), null);
                    } else {
                        bundle.start();
                    }
                } catch (Exception e) {
                    BundleException be = null;
                    if (e instanceof BundleException) {
                        be = (BundleException) e;
                    } else if (e instanceof ModuleManagementException && e.getCause() instanceof BundleException) {
                        be = (BundleException) e.getCause();
                    }
                    if (be != null && BundleException.RESOLVE_ERROR == be.getType()) {
                        // log warning for the resolution (dependencies) error
                        logger.warn(be.getMessage());
                    } else {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        } finally {
            logger.info("Finished starting {} bundle(s)", toBeStarted.size());
        }
    }

    /**
     * Start the specified module bundles in the order which tries to consider the dependencies between them.
     * 
     * @param moduleBundles the bundles to be started
     * @param useModuleManagerApi should we use {@link ModuleManager} or call OSGi API directly?
     */
    static void startModules(List<Bundle> moduleBundles, boolean useModuleManagerApi) {
        Map<Bundle, JahiaTemplatesPackage> toBeStarted = new HashMap<>();
        for (Bundle bundle : moduleBundles) {
            JahiaTemplatesPackage pkg = BundleUtils.getModule(bundle);
            if (pkg != null) {
                toBeStarted.put(bundle, pkg);
            } else {
                logger.warn("Unable to retrieve module package for bundle {}/{}. Skip starting it.",
                        bundle.getSymbolicName(), bundle.getVersion());
            }
        }

        startBundles(toBeStarted, useModuleManagerApi);
    }

    private BundleContext bundleContext;

    private File deployedBundlesDir;
    
    private boolean firstStartup;
    
    BundleStarter() {
        deployedBundlesDir = new File(System.getProperty("org.osgi.framework.storage"));
        firstStartup = !new File(deployedBundlesDir, "bundle0").exists();
    }

    /**
     * Notifies the service that the FileInstall watcher has been started and processed the found modules.
     */
    void afterFileInstallStarted() {
        if (firstStartup) {
            // this is a first framework startup
            firstStartup = false;
            if (!isFileinstallStartsNewBundles()) {
                // as the bundles are not started automatically by Fileinstall we need to start them manually
                startAllModules();
            }
        }
        startMigrateBundlesIfNeeded();
    }

    private BundleContext getBundleContext() {
        if (bundleContext == null) {
            bundleContext = FrameworkService.getBundleContext();
        }
        
        return bundleContext;
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

    private boolean isFileinstallStartsNewBundles() {
        return Boolean.valueOf(((Properties) SpringContextSingleton.getBean("felixFileInstallConfig"))
                .getProperty("felix.fileinstall.bundles.new.start", "true"));
    }

    private void startAllModules() {
        List<Bundle> toBeStarted = new LinkedList<>();
        for (Bundle bundle : getBundleContext().getBundles()) {
            if (bundle.getState() != Bundle.ACTIVE && bundle.getState() != Bundle.UNINSTALLED
                    && !BundleUtils.isFragment(bundle) && BundleUtils.isJahiaModuleBundle(bundle)) {
                toBeStarted.add(bundle);
            }
        }

        startModules(toBeStarted, false);
    }

    void startInitialBundlesIfNeeded() {
        File marker = new File(deployedBundlesDir, MARKER_INITIAL_BUNDLES);
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

    private void startMigrateBundlesIfNeeded() {
        // as the bundles are not started automatically after a migration whe should do it manually
        File marker = new File(deployedBundlesDir, MARKER_MIGRATE_BUNDLES);
        if (!marker.exists()) {
            return;
        }

        logger.info("Starting migrated bundles");
        
        try {
            List<Bundle> toBeStarted = new LinkedList<>();
            List<String> lines = FileUtils.readLines(marker);
            for (String line : lines) {
                String[] lineParts = line.split(",");
                String bundleSymbolicName = lineParts[0].trim();
                String bundleVersion = lineParts[1].trim();
                logger.info("Found entry for bundle {}/{}", bundleSymbolicName, bundleVersion);
                Bundle bundle = BundleUtils.getBundleBySymbolicName(bundleSymbolicName, bundleVersion);
                if (bundle != null) {
                    if (bundle.getState() != Bundle.ACTIVE && bundle.getState() != Bundle.UNINSTALLED
                            && !BundleUtils.isFragment(bundle)
                            && !bundle.adapt(BundleStartLevel.class).isPersistentlyStarted()) {
                        toBeStarted.add(bundle);
                    } else {
                        logger.info("No need to start bundle {}/{}. Skipping it.", bundleSymbolicName, bundleVersion);
                    }
                } else {
                    logger.warn("Cannot find bundle {}/{}. Skip starting it.", bundleSymbolicName, bundleVersion);
                }
            }
            if (!toBeStarted.isEmpty()) {
                startModules(toBeStarted, false);
            }
            logger.info("Finished starting migrated bundles");
            FileUtils.deleteQuietly(marker);
        } catch (IOException e) {
            logger.error("Error reading [migrate-bundles].dostart Cause: " + e.getMessage(), e);
        }
    }

}
