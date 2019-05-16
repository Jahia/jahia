/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group SA. All rights reserved.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Helper class, responsible for starting bundles on demand.
 *
 * @author Sergiy Shyrkov
 */
class BundleStarter {

    private static final Logger logger = LoggerFactory.getLogger(BundleStarter.class);

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
            List<Bundle> toBeStarted = new LinkedList<>();
            List<Bundle> toBeUninstaled = new LinkedList<>();

            // as the bundles are not started automatically by Fileinstall we need to start them manually
            for (Long bundleId : createdOnStartup) {
                Bundle bundle = getBundleContext().getBundle(bundleId);
                if (bundle.getState() != Bundle.ACTIVE && bundle.getState() != Bundle.UNINSTALLED && !BundleUtils.isFragment(bundle) && BundleUtils.isJahiaModuleBundle(bundle)) {
                    toBeStarted.add(bundle);
                }

                // Check if another version was previously installed. Uninstall previous version and start only if previous was started
                for (Bundle otherBundle : getBundleContext().getBundles()) {
                    if (bundleId != otherBundle.getBundleId() && bundle.getSymbolicName().equals(otherBundle.getSymbolicName())) {
                        if (otherBundle.getState() != Bundle.ACTIVE) {
                            // Previous bundle was not active, do not start the new one
                            toBeStarted.remove(bundle);
                        }
                        if (bundle.getState() != Bundle.UNINSTALLED) {
                            // Uninstall older version
                            toBeUninstaled.add(otherBundle);
                        }
                    }
                }
            }
            for (Bundle bundle : toBeUninstaled) {
                try {
                    bundle.uninstall();
                } catch (BundleException e) {
                    logger.error("Cannot uninstall bundle", e);
                }
            }

            if (!toBeStarted.isEmpty()) {
                startModules(toBeStarted, false);
            }
        }
    }

    private BundleContext getBundleContext() {
        if (bundleContext == null) {
            bundleContext = FrameworkService.getBundleContext();
        }

        return bundleContext;
    }

}
