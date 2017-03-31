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

import org.apache.felix.utils.manifest.Clause;
import org.apache.felix.utils.manifest.Parser;
import org.apache.felix.utils.version.VersionRange;
import org.apache.felix.utils.version.VersionTable;
import org.jahia.services.modulemanager.ModuleManager;
import org.osgi.framework.*;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.framework.wiring.FrameworkWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Convenient utilities for OSGi bundle lifecycle and framework wiring.
 *
 * @author Sergiy Shyrkov
 */
public final class BundleLifecycleUtils {

    private static final Logger logger = LoggerFactory.getLogger(BundleLifecycleUtils.class);

    /**
     * Collects a set of bundles, which have optional package dependencies to one of the bundles in the provided list. Inspired by the
     * DirectoryWatcher from Felix FileInstall.
     *
     * @param bundles a collection of bundles to collect bundles with optional packages for
     * @return a set of bundles, which have optional package dependencies to one of the bundles in the provided list. Inspired by the
     *         DirectoryWatcher from Felix FileInstall
     */
    private static Set<Bundle> findBundlesWithOptionalPackagesToRefresh(Collection<Bundle> bundles) {

        Set<Bundle> targets = new HashSet<Bundle>(Arrays.asList(getAllBundles()));
        targets.removeAll(bundles);

        if (targets.isEmpty()) {
            return Collections.emptySet();
        }

        // Second pass: for each bundle, check if there is any unresolved optional package that could be resolved
        Map<Bundle, List<Clause>> imports = new HashMap<Bundle, List<Clause>>();
        for (Iterator<Bundle> it = targets.iterator(); it.hasNext();) {
            Bundle b = it.next();
            String importsStr = b.getHeaders().get(Constants.IMPORT_PACKAGE);
            List<Clause> importsList = getOptionalImports(importsStr);
            if (importsList.isEmpty()) {
                it.remove();
            } else {
                imports.put(b, importsList);
            }
        }
        if (targets.isEmpty()) {
            return Collections.emptySet();
        }

        // Third pass: compute a list of packages that are exported by our bundles and see if
        // some exported packages can be wired to the optional imports
        List<Clause> exports = new ArrayList<Clause>();
        for (Bundle b : bundles) {
            if (b.getState() != Bundle.UNINSTALLED) {
                String exportsStr = b.getHeaders().get(Constants.EXPORT_PACKAGE);
                if (exportsStr != null) {
                    Clause[] exportsList = Parser.parseHeader(exportsStr);
                    exports.addAll(Arrays.asList(exportsList));
                }
            }
        }
        for (Iterator<Bundle> it = targets.iterator(); it.hasNext();) {
            Bundle b = it.next();
            List<Clause> importsList = imports.get(b);
            for (Iterator<Clause> itpi = importsList.iterator(); itpi.hasNext();) {
                Clause pi = itpi.next();
                boolean matching = false;
                for (Clause pe : exports) {
                    if (pi.getName().equals(pe.getName())) {
                        String evStr = pe.getAttribute(Constants.VERSION_ATTRIBUTE);
                        String ivStr = pi.getAttribute(Constants.VERSION_ATTRIBUTE);
                        Version exported = evStr != null ? Version.parseVersion(evStr) : Version.emptyVersion;
                        VersionRange imported = ivStr != null ? VersionRange.parseVersionRange(ivStr)
                                : VersionRange.ANY_VERSION;
                        if (imported.contains(exported)) {
                            matching = true;
                            break;
                        }
                    }
                }
                if (!matching) {
                    itpi.remove();
                }
            }
            if (importsList.isEmpty()) {
                it.remove();
            }
        }

        return targets;
    }

    /**
     * Collects a set of fragment bundles, which are related to the provided list of bundles, i.e. provided bundles are hosts for the
     * collected fragments. Inspired by the DirectoryWatcher from Felix FileInstall.
     *
     * @param bundles a collection of bundles to collect fragments for
     * @return a set of fragment bundles, which are related to the provided list of bundles, i.e. provided bundles are hosts for the
     *         collected fragments
     */
    private static Set<Bundle> findFragmentsForBundles(Collection<Bundle> bundles) {
        Set<Bundle> fragments = new HashSet<>();
        for (Bundle b : getAllBundles()) {
            if (b.getState() != Bundle.UNINSTALLED) {
                fragments.addAll(findBundlesForClause(b, bundles));
            }
        }
        return fragments;
    }

    /**
     * Collect a set of hosts bundles for a fragment
     * @param fragment is the fragment to collect hosts bundles for
     * @return a set of all bundles that match fragment's hosts
     */
    public static Set<Bundle> getHostsFragment(Bundle fragment) {
        HashSet<Bundle> hosts = new HashSet<>();
        hosts.addAll(findBundlesForClause(fragment, Arrays.asList(getAllBundles())));
        return hosts;
    }

    private static List<Bundle> findBundlesForClause(Bundle fragment, Collection<Bundle> bundles) {
        List<Bundle> foundBundles = new ArrayList<>();
        String hostHeader = fragment.getHeaders().get(Constants.FRAGMENT_HOST);
        if (hostHeader != null) {
            Clause[] clauses = Parser.parseHeader(hostHeader);
            if (clauses != null && clauses.length > 0) {
                Clause path = clauses[0];
                for (Bundle bundle : bundles) {
                    if (bundle.getSymbolicName() != null
                            && bundle.getSymbolicName().equals(path.getName())) {
                        String ver = path.getAttribute(Constants.BUNDLE_VERSION_ATTRIBUTE);
                        if (ver != null) {
                            VersionRange v = VersionRange.parseVersionRange(ver);
                            if (v.contains(VersionTable
                                    .getVersion(bundle.getHeaders().get(Constants.BUNDLE_VERSION)))) {
                                foundBundles.add(bundle);
                            }
                        } else {
                            foundBundles.add(bundle);
                        }
                    }
                }
            }
        }
        return foundBundles;
    }

    private static Bundle[] getAllBundles() {
        return FrameworkService.getBundleContext().getBundles();
    }

    /**
     * Returns the start level of the provided bundle.
     *
     * @param bundle the bundle to get start level for
     * @return the start level of the provided bundle
     */
    public static int getBundleStartLevel(Bundle bundle) {
        return bundle.adapt(BundleStartLevel.class).getStartLevel();
    }

    /**
     * Returns the start level of the OSGi framework.
     *
     * @return the start level of the OSGi framework
     */
    public static int getFrameworkStartLevel() {
        return getSystemBundle().adapt(FrameworkStartLevel.class).getStartLevel();
    }

    /**
     * Obtains the {@link FrameworkWiring}.
     *
     * @return the {@link FrameworkWiring}
     */
    public static FrameworkWiring getFrameworkWiring() {
        return getSystemBundle().adapt(FrameworkWiring.class);
    }

    private static List<Clause> getOptionalImports(String importsStr) {
        Clause[] imports = Parser.parseHeader(importsStr);
        List<Clause> result = new LinkedList<Clause>();
        for (Clause anImport : imports) {
            String resolution = anImport.getDirective(Constants.RESOLUTION_DIRECTIVE);
            if (Constants.RESOLUTION_OPTIONAL.equals(resolution)) {
                result.add(anImport);
            }
        }
        return result;
    }

    /**
     * Returns the system bundle of the OSGi framework.
     *
     * @return the system bundle of the OSGi framework
     */
    private static Bundle getSystemBundle() {
        return FrameworkService.getBundleContext().getBundle(0);
    }

    /**
     * Check if a bundle is a fragment.
     *
     * @return <code>true</code> if the supplied bundle is a fragment; <code>false</code> otherwise
     */
    private static boolean isFragment(Bundle bundle) {
        BundleRevision rev = bundle.adapt(BundleRevision.class);
        return (rev.getTypes() & BundleRevision.TYPE_FRAGMENT) != 0;
    }

    /**
     * Refreshes the specified bundle and its "dependencies".
     *
     * @param bundle the bundle to refresh
     */
    public static void refreshBundle(Bundle bundle) {
        refreshBundles(Collections.singleton(bundle), true, true);
    }

    /**
     * Refreshes the specified bundles.
     *
     * @param bundlesToRefresh the bundles to refresh
     */
    private static void refreshBundles(Collection<Bundle> bundlesToRefresh) {

        final CountDownLatch latch = new CountDownLatch(1);

        FrameworkWiring wiring = getFrameworkWiring();
        wiring.refreshBundles(bundlesToRefresh, new FrameworkListener() {

            @Override
            public void frameworkEvent(FrameworkEvent event) {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.warn("Waiting for refresh of bundles was interrupted", e);
        }
    }

    /**
     * Refreshes the specified bundles and optionally their "dependencies".
     *
     * @param bundlesToRefresh the bundles to refresh
     * @param considerFragments should we also refresh the related fragment bundles?
     * @param considerBundlesWithOptionalPackages should we consider bundles with optional package dependencies?
     */
    public static void refreshBundles(Collection<Bundle> bundlesToRefresh, boolean considerFragments,
            boolean considerBundlesWithOptionalPackages) {

        logger.info("Requested refresh for the following {} bundle(s): {}", bundlesToRefresh.size(), bundlesToRefresh);
        Collection<Bundle> fullBundleList = bundlesToRefresh;
        if (considerFragments) {
            Set<Bundle> fragments = findFragmentsForBundles(bundlesToRefresh);
            if (!fragments.isEmpty()) {
                fullBundleList = new HashSet<>(bundlesToRefresh);
                fullBundleList.addAll(fragments);
            }
        }
        if (considerBundlesWithOptionalPackages) {
            Set<Bundle> bundlesWithOptionalPackages = findBundlesWithOptionalPackagesToRefresh(bundlesToRefresh);
            if (!bundlesWithOptionalPackages.isEmpty()) {
                fullBundleList = fullBundleList == bundlesToRefresh ? new HashSet<>(bundlesToRefresh) : fullBundleList;
                fullBundleList.addAll(bundlesWithOptionalPackages);
            }
        }

        if (fullBundleList != bundlesToRefresh) {
            // we collected some "dependencies"
            logger.info("Triggering refresh for the following {} bundle(s): {}", fullBundleList.size(), fullBundleList);
        }

        // perform the refresh
        refreshBundles(fullBundleList);
    }

    /**
     * Resolves the specified bundle.
     *
     * @param bundle the bundle to be resolved
     * @return {@code true} if the specified bundle is resolved; {@code false} otherwise.
     */
    public static boolean resolveBundle(Bundle bundle) {
        return resolveBundles(Collections.singleton(bundle));
    }

    /**
     * Resolves the specified bundles.
     *
     * @param bundlesToResolve the bundles to resolve or {@code null} to resolve all unresolved bundles installed in the Framework
     * @return {@code true} if all specified bundles are resolved; {@code false}
     *         otherwise.
     */
    public static boolean resolveBundles(Collection<Bundle> bundlesToResolve) {
        if (bundlesToResolve != null) {
            logger.info("Requested resolve for the following {} bundle(s): {}", bundlesToResolve.size(), bundlesToResolve);
        } else {
            logger.info("Requested resolve for all bundles");
        }
        return getFrameworkWiring().resolveBundles(bundlesToResolve);
    }

    /**
     * Starts provided bundle.
     *
     * @param bundlesToStart a collection of bundles to be started
     */
    private static void startBundles(Collection<Bundle> bundlesToStart) {
        for (Bundle bundle : bundlesToStart) {
            int frameworkStartLevel = getFrameworkStartLevel();
            // Fragments can never be started.
            // Bundles can only be started transient when the start level of the framework is high
            // enough. Persistent (i.e. non-transient) starts will simply make the framework start the
            // bundle when the start level is high enough.
            if (bundle.getState() != Bundle.UNINSTALLED && !isFragment(bundle)
                    && frameworkStartLevel >= getBundleStartLevel(bundle)) {
                try {
                    logger.info("Starting bundle: {}", bundle);
                    bundle.start(Bundle.START_ACTIVATION_POLICY);
                } catch (BundleException e) {
                    if (BundleException.RESOLVE_ERROR == e.getType()) {
                        // we log unresolved dependencies in DEBUG
                        logger.debug("Error while starting bundle " + bundle + ". Cause: " + e.getMessage(), e);
                        logger.info("Bundle {} has unresolved dependencies and won't be started", bundle);
                    } else {
                        logger.warn("Error while starting bundle " + bundle + ". Cause: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Tries to start all the bundles which somehow got stopped mainly due to a missing dependencies. Inspired by the DirectoryWatcher from
     * Felix FileInstall.
     */
    public static void startBundlesPendingDependencies() {
        int frameworkStartLevel = getFrameworkStartLevel();
        List<Bundle> bundlesToStart = new ArrayList<Bundle>();
        Bundle[] allBundles = getAllBundles();
        for (Bundle bundle : allBundles) {
            if (bundle != null) {
                if (bundle.getState() != Bundle.STARTING && bundle.getState() != Bundle.ACTIVE
                        && bundle.adapt(BundleStartLevel.class).isPersistentlyStarted()
                        && frameworkStartLevel >= getBundleStartLevel(bundle)) {
                    bundlesToStart.add(bundle);
                }
            }
        }
        startBundles(bundlesToStart);
    }

    /**
     * Start the specified module bundles in the order which tries to consider the dependencies between them.
     *
     * @param moduleBundles the bundles to be started
     * @param useModuleManagerApi should we use {@link ModuleManager} or call OSGi API directly?
     */
    public static void startModules(List<Bundle> moduleBundles, boolean useModuleManagerApi) {
        BundleStarter.startModules(moduleBundles, useModuleManagerApi);
    }

    /**
     * Will update the bundle, calling stop/start on it.
     * Sometimes a refresh is necessary in case the bundle is providing wires to other bundles.
     * The refresh will actualize the wires to the updated bundle revision.
     *
     * @param bundle the bundle to update
     */
    public static void updateBundle(Bundle bundle) throws BundleException {

        if (bundle.getState() == Bundle.ACTIVE) {

            // if the bundle is Active and provides wires to other bundle(s) we need to refresh these wires after the update

            BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
            List<BundleWire> bundleWires = bundleWiring.getProvidedWires(null);

            if (bundleWires != null && bundleWires.size() > 0) {

                // stop manually the bundle
                bundle.stop();

                // do the update
                bundle.update();

                // refresh the wirings
                refreshBundle(bundle);

                // start manually
                bundle.start();
            } else {
                // no wires, just update the bundle directly
                bundle.update();
            }
        } else {
            // not active, just update the bundle directly
            bundle.update();
        }
    }
}