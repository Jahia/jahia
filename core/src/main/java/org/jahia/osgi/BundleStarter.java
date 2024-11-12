/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class, responsible for starting bundles on demand.
 *
 * @author Sergiy Shyrkov
 */
class BundleStarter {

    private static final Logger logger = LoggerFactory.getLogger(BundleStarter.class);

    @Deprecated
    private static final String MARKER_INITIAL_BUNDLES = "[initial-bundles].dostart";

    @Deprecated
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
        Collection<Bundle> sortedBundles = getSortedModules(toBeStarted);
        if(logger.isInfoEnabled()) {
            StringJoiner joiner = new StringJoiner(",");
            for (Bundle bundle : sortedBundles) {
                joiner.add(bundle.getSymbolicName());
            }
            logger.info("Will start {} bundle(s) : {}", toBeStarted.size(), joiner);
        }

        try {
            for (Bundle bundle : sortedBundles) {
                try {
                    logger.info("Triggering start for bundle {}/{}, state: {}", bundle.getSymbolicName(), bundle.getVersion(), BundleState.fromInt(bundle.getState()));
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
     * @param moduleBundles       the bundles to be started
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

    /**
     * Notifies the service that the FileInstall watcher has been started and processed the found modules.
     */
    void afterFileInstallStarted(List<Long> createdOnStartup) {
        if (!createdOnStartup.isEmpty()) {
            logger.info("Handling bundles created on fileinstall startup");
            List<Bundle> toBeStarted = new LinkedList<>();
            List<Bundle> toBeUninstalled = new LinkedList<>();

            // as the bundles are not started automatically by Fileinstall we need to start them manually
            for (Long bundleId : createdOnStartup) {
                Bundle bundle = getBundleContext().getBundle(bundleId);

                // This was added to address potential NPE, see QA-12181 for details
                if (bundle == null) {
                    logger.error("Could not find bundle with id {}, will skip to next bundle.", bundleId);
                    continue;
                }

                // list other existing versions of this bundle if any
                List<Bundle> otherVersions = Arrays.stream(getBundleContext().getBundles())
                        .filter(b -> bundleId != b.getBundleId() && bundle.getSymbolicName().equals(b.getSymbolicName()))
                        .collect(Collectors.toList());

                Collection<Bundle> otherVersionsNotCreatedOnStartup = otherVersions.stream()
                        .filter(otherBundle -> !createdOnStartup.contains(otherBundle.getBundleId()))
                        .collect(Collectors.toList());


                if (bundle.getState() != Bundle.ACTIVE && bundle.getState() != Bundle.UNINSTALLED && !BundleUtils.isFragment(bundle) && BundleUtils.isJahiaModuleBundle(bundle) &&
                        (otherVersionsNotCreatedOnStartup.isEmpty() || otherVersionsNotCreatedOnStartup.stream().anyMatch(otherBundle -> BundleUtils.getPersistentState(otherBundle) == Bundle.ACTIVE)) &&
                        (otherVersions.stream().noneMatch(otherBundle -> otherBundle.getVersion().compareTo(bundle.getVersion()) > 0))) {
                    toBeStarted.add(bundle);
                }

                // if older versions were previously installed, then the previous ones has to be uninstalled
                if (bundle.getState() != Bundle.UNINSTALLED) {
                    otherVersionsNotCreatedOnStartup.stream()
                            .filter(otherBundle -> otherBundle.getVersion().compareTo(bundle.getVersion()) < 0)
                            .forEach(otherBundle -> {
                        if (!toBeUninstalled.contains(otherBundle)) {
                            toBeUninstalled.add(otherBundle);
                        }
                    });
                }
            }

            Collection<Bundle> toBeRefreshed = BundleLifecycleUtils.getBundlesDependencies(new HashSet<>(toBeUninstalled), true, true);
            logger.info("Full list of to be refreshed bundles : {}", toBeRefreshed);
            toBeRefreshed.removeAll(toBeUninstalled);
            toBeUninstalled.sort((b1,b2) -> Boolean.compare(BundleUtils.isFragment(b1),BundleUtils.isFragment(b2)));

            logger.info("Uninstall bundles : {}", toBeUninstalled);

            for (Bundle bundle : toBeUninstalled) {
                try {
                    bundle.uninstall();
                } catch (BundleException e) {
                    logger.error("Cannot uninstall bundle", e);
                }
            }

            logger.info("Refreshing bundles : {}", toBeRefreshed);

            if (!toBeRefreshed.isEmpty()) {
                BundleLifecycleUtils.refreshBundles(toBeRefreshed);
            }

            logger.info("Start bundles : {}", toBeStarted);

            if (!toBeStarted.isEmpty()) {
                startModules(toBeStarted, false);
            }

            logger.info("Done processing fileinstall bundles");
        }

        // Migrations from versions < 7.3.1.1
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

    /**
     * Used only for migrations from versions < 7.3.1.1
     *
     * @deprecated
     */
    @Deprecated
    void startInitialBundlesIfNeeded() {
        File marker = new File(System.getProperty("org.osgi.framework.storage"), MARKER_INITIAL_BUNDLES);
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

    /**
     * Used only for migrations from versions < 7.3.1.1
     *
     * @deprecated
     */
    @Deprecated
    private void startMigrateBundlesIfNeeded() {
        // as the bundles are not started automatically after a migration whe should do it manually
        File marker = new File(System.getProperty("org.osgi.framework.storage"), MARKER_MIGRATE_BUNDLES);
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
