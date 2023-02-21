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
package org.jahia.services.modulemanager.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.BundleState;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.modulemanager.*;
import org.jahia.services.modulemanager.persistence.BundlePersister;
import org.jahia.services.modulemanager.persistence.PersistentBundle;
import org.jahia.services.modulemanager.persistence.PersistentBundleInfoBuilder;
import org.jahia.services.modulemanager.persistence.jcr.BundleInfoJcrHelper;
import org.jahia.services.modulemanager.spi.BundleService;
import org.jahia.services.modulemanager.spi.BundleService.BundleInformation;
import org.jahia.services.modulemanager.util.ModuleUtils;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.settings.SettingsBean;
import org.jahia.settings.readonlymode.ReadOnlyModeCapable;
import org.jahia.settings.readonlymode.ReadOnlyModeException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The main entry point service for the module management service, providing functionality for module deployment, undeployment, start and
 * stop operations, which are performed in a seamless way on a standalone installation as well as across the platform cluster.
 *
 * @author Sergiy Shyrkov
 */
public class ModuleManagerImpl implements ModuleManager, ReadOnlyModeCapable {

    private static final Logger logger = LoggerFactory.getLogger(ModuleManagerImpl.class);

    private final ReadWriteLock readOnlyModeLock = new ReentrantReadWriteLock();
    private BundleService bundleService;
    private BundlePersister persister;
    private JahiaTemplateManagerService templateManagerService;
    private boolean readOnly;

    private Collection<BundleChecker> bundleCheckers;

    private interface BundleOperation {

        String REFRESH = "Refresh";
        String STOP = "Stop";
        String START = "Start";
        String UNINSTALL = "Uninstall";

        String getName();

        boolean changesModuleState();

        void perform(BundleInfo info, String target);
    }

    private static BundleInfo toBundleInfo(PersistentBundle persistentBundle) {
        return new BundleInfo(persistentBundle.getGroupId(), persistentBundle.getSymbolicName(), persistentBundle.getVersion());
    }

    private BundleInfo findTargetBundle(String bundleKey, String symbolicName, String version) {
        BundleInfo targetInfo = null;
        List<Bundle> matches = new ArrayList<>();
        Bundle[] allBundles = FrameworkService.getBundleContext().getBundles();
        for (Bundle b : allBundles) {
            if (symbolicName.equals(b.getSymbolicName())
                    && (version == null || version.equals(b.getVersion().toString()))
                    && b.getState() != Bundle.UNINSTALLED) {
                matches.add(b);
            }
        }

        if (matches.size() > 1) {
            logger.warn("Found multiple bundles matching the key {}. Unable to uniquely identify target bundle",
                    bundleKey);
        } else if (!matches.isEmpty()) {
            Bundle target = matches.get(0);
            targetInfo = new BundleInfo(BundleUtils.getModuleGroupId(target), target.getSymbolicName(),
                    target.getVersion().toString());
        }

        return targetInfo;
    }

    private BundleInfo getBundleInfoGuessIfNeeded(String bundleKey) {

        BundleInfo info = null;

        // first check if we have the full key and all the info needed already
        if (bundleKey.indexOf('/') != -1) {
            info = BundleInfo.fromKey(bundleKey);
            if (info.getGroupId() != null && info.getSymbolicName() != null && info.getVersion() != null) {
                // we have everything
                return info;
            }
        }

        return findTargetBundle(
                bundleKey,
                info != null ? info.getSymbolicName() : bundleKey,
                info != null ? info.getVersion() : null
        );
    }

    private static BundleBucketInfo getBundleBucketInfo(String bundleBucketKey) {
        try {
            return BundleBucketInfo.fromKey(bundleBucketKey);
        } catch (IllegalArgumentException e) {
            throw new InvalidModuleKeyException(bundleBucketKey);
        }
    }

    private static BundleInfo getBundleInfo(String bundleKey) {
        try {
            return BundleInfo.fromKey(bundleKey);
        } catch (IllegalArgumentException e) {
            throw new InvalidModuleKeyException(bundleKey);
        }
    }

    private OperationResult doInstall(Collection<PersistentBundle> infos, final String target, boolean start, int startLevel) {
        for (PersistentBundle info : infos) {
            if (!info.isIgnoreChecks()) {
                for (BundleChecker bundleChecker : bundleCheckers) {
                    bundleChecker.check(info);
                }
            }
        }
        ArrayList<BundleInfo> bundleInfos = new ArrayList<>(infos.size());
        // phase #1: install bundles but do not start
        for (PersistentBundle info : infos) {
            // install but do not start bundle yet
            bundleService.install(info.getLocation(), target, false, startLevel);
            bundleInfos.add(toBundleInfo(info));
        }
        // phase #2: if start requested, stop the previous versions of bundles, start requested versions and perform refresh
        if (start) {
            // phase #2.1 resolve bundle to process imports
            for (PersistentBundle info : infos) {
                bundleService.resolve(info, target);
            }
            // phase #2.2 stop previous versions
            for (PersistentBundle info : infos) {
                stopPreviousVersions(info, target);
            }

            // phase #2.3 refresh stopped versions
            refreshOtherVersions(bundleInfos, target);

            // phase #2.4 start new versions
            for (PersistentBundle info : infos) {
                bundleService.start(info, target);
            }
        }
        return OperationResult.success(bundleInfos);
    }

    @Override
    public OperationResult install(Resource bundleResource, String target) {
        return install(bundleResource, target, false);
    }

    @Override
    public OperationResult install(Resource bundleResource, String target, boolean start) {
        return install(Collections.singleton(bundleResource), target, start);
    }

    @Override
    public OperationResult install(Collection<Resource> bundleResources, String target, boolean start) {
        return install(bundleResources, target, start, SettingsBean.getInstance().getModuleStartLevel());
    }

    public OperationResult install(Collection<Resource> bundleResources, String target, boolean start, boolean ignoreChecks) {
        return install(bundleResources, target, start, SettingsBean.getInstance().getModuleStartLevel(), ignoreChecks);
    }

    public OperationResult install(Collection<Resource> bundleResources, String target, boolean start, int startLevel) {
        return install(bundleResources, target, start, startLevel, false);
    }

    public OperationResult install(Collection<Resource> bundleResources, String target, boolean start, int startLevel, boolean ignoreChecks) {
        readOnlyModeLock.readLock().lock();
        try {
            long startTime = System.currentTimeMillis();
            logger.info("Performing installation of bundles {} on target {}", bundleResources, target);
            OperationResult result = null;
            Exception error = null;
            try {
                assertWritable();
                ArrayList<PersistentBundle> bundleInfos = new ArrayList<>(bundleResources.size());
                for (Resource bundleResource : bundleResources) {
                    boolean requiresPersisting = !((bundleResource instanceof UrlResource) && bundleResource.getURL().getProtocol().equals(Constants.URL_PROTOCOL_DX));
                    PersistentBundle bundleInfo = PersistentBundleInfoBuilder.build(bundleResource, requiresPersisting, requiresPersisting);
                    if (bundleInfo == null) {
                        throw new InvalidModuleException();
                    }
                    bundleInfo.setIgnoreChecks(ignoreChecks);
                    if (requiresPersisting) {
                        persister.store(bundleInfo);
                    }
                    bundleInfos.add(bundleInfo);
                }
                result = doInstall(bundleInfos, target, start, startLevel);
            } catch (ModuleManagementException e) {
                error = e;
                throw e;
            } catch (Exception e) {
                error = e;
                throw new ModuleManagementException(e);
            } finally {
                long timeTaken = System.currentTimeMillis() - startTime;
                if (error == null) {
                    logger.info("Installation completed for bundles {} on target {} in {} ms. Operation result: {}",
                            new Object[]{bundleResources, target, timeTaken, result});
                } else {
                    logger.info("Installation failed for bundles {} on target {} (took {} ms). Operation error: {}",
                            new Object[]{bundleResources, target, timeTaken, error});
                }
            }

            if (FrameworkService.getInstance().isStarted()) {
                try {
                    storeAllLocalPersistentStates();
                } catch (ModuleManagementException e) {
                    // Don't propagate this. The main operation did actually succeed.
                    logger.warn("Failed to persist modules state", e);
                }
            }

            return result;

        } finally {
            readOnlyModeLock.readLock().unlock();
        }
    }

    private OperationResult performOperation(String bundleKey, String target, BundleOperation operation) {

        if (StringUtils.isEmpty(bundleKey)) {
            throw new IllegalArgumentException("Bundle '" + bundleKey + "' key in invalid");
        }

        long startTime = System.currentTimeMillis();
        logger.info("Performing {} operation for bundle {} on target {}",
                new Object[]{operation.getName(), bundleKey, target});

        OperationResult result = null;
        BundleInfo info = null;
        Exception error = null;
        try {
            if (operation.changesModuleState()) {
                assertWritable();
            }
            info = getBundleInfoGuessIfNeeded(bundleKey);
            if (info == null) {
                throw new ModuleNotFoundException(bundleKey);
            }
            operation.perform(info, target);
            result = OperationResult.success(info);
        } catch (ModuleManagementException e) {
            checkForMissingDependency(e, info);
            error = e;
            throw e;
        } catch (Exception e) {
            error = e;
            throw new ModuleManagementException(e);
        } finally {
            if (error == null) {
                logger.info("{} operation completed for bundle {} on target {} in {} ms. Operation result: {}",
                        new Object[]{operation.getName(), bundleKey, target, System.currentTimeMillis() - startTime,
                                result});
            } else {
                logger.info("{} operation failed for bundle {} on target {} (took {} ms). Operation error: {}",
                        new Object[]{operation.getName(), bundleKey, target, System.currentTimeMillis() - startTime,
                                error});
            }
        }

        if (FrameworkService.getInstance().isStarted() && operation.changesModuleState()) {
            try {
                storeAllLocalPersistentStates();
            } catch (ModuleManagementException e) {
                // Don't propagate this. The main operation did actually succeed.
                logger.warn("Failed to persist modules state", e);
            }
        }

        return result;
    }

    /**
     * Injects an instance of the bundle service.
     *
     * @param bundleService an instance of the bundle service
     */
    public void setBundleService(BundleService bundleService) {
        this.bundleService = bundleService;
    }

    /**
     * Injects an instance of the bundle persister.
     *
     * @param persister an instance of the bundle persister
     */
    public void setPersister(BundlePersister persister) {
        this.persister = persister;
    }

    @Override
    public OperationResult start(String bundleKey, String target) {
        readOnlyModeLock.readLock().lock();
        try {
            return performOperation(bundleKey, target, new BundleOperation() {

                @Override
                public String getName() {
                    return BundleOperation.START;
                }

                @Override
                public boolean changesModuleState() {
                    return true;
                }

                @Override
                public void perform(BundleInfo info, String target) {
                    bundleService.stop(info, target);

                    stopPreviousVersions(info, target);

                    bundleService.start(info, target);

                    refreshOtherVersions(Collections.singleton(info), target);
                }
            });

        } finally {
            readOnlyModeLock.readLock().unlock();
        }
    }

    @Override
    public OperationResult stop(String bundleKey, String target) {
        readOnlyModeLock.readLock().lock();
        try {
            return performOperation(bundleKey, target, new BundleOperation() {

                @Override
                public String getName() {
                    return BundleOperation.STOP;
                }

                @Override
                public boolean changesModuleState() {
                    return true;
                }

                @Override
                public void perform(BundleInfo info, String target) {
                    bundleService.stop(info, target);
                }
            });

        } finally {
            readOnlyModeLock.readLock().unlock();
        }
    }

    private void stopPreviousVersions(BundleInfo info, String target) {
        JahiaTemplatesPackage activePackage = templateManagerService.getTemplatePackageRegistry()
                .lookupById(info.getSymbolicName());
        Bundle activeBundle = activePackage != null ? activePackage.getBundle() : null;
        if (activeBundle != null && activeBundle.getState() == Bundle.ACTIVE) {
            BundleInfo otherInfo = BundleInfo.fromBundle(activeBundle);
            stopExistingVersion(info, otherInfo, target);
        }

        for (Map.Entry<Bundle, ModuleState> entry : templateManagerService.getModuleStates().entrySet()) {
            Bundle bundle = entry.getKey();
            if (bundle != activeBundle && bundle.getState() == Bundle.ACTIVE
                    && bundle.getSymbolicName().equals(info.getSymbolicName())
                    && !bundle.getVersion().toString().equals(info.getVersion())) {
                BundleInfo otherInfo = BundleInfo.fromBundle(bundle);
                stopExistingVersion(info, otherInfo, target);
            }
        }
    }

    private void stopExistingVersion(BundleInfo info, BundleInfo otherInfo, String target) {
        String key = info.getKey();
        String otherKey = otherInfo.getKey();
        try {
            logger.info("Stopping existing version of the module {} before starting {}...", otherKey, key);
            bundleService.stop(otherInfo, target);
            logger.info("...done stopping existing version of the module {} before starting {}", otherKey, key);
        } catch (Exception e) {
            logger.warn("Unable to stop existing version of the module " + otherKey + " before starting " + key, e);
        }
    }

    @Override
    public OperationResult uninstall(String bundleKey, String target) {
        readOnlyModeLock.readLock().lock();
        try {
            return performOperation(bundleKey, target, new BundleOperation() {

                @Override
                public String getName() {
                    return BundleOperation.UNINSTALL;
                }

                @Override
                public boolean changesModuleState() {
                    return true;
                }

                @Override
                public void perform(BundleInfo info, String target) {
                    bundleService.uninstall(info, target);
                }
            });

        } finally {
            readOnlyModeLock.readLock().unlock();
        }
    }

    @Override
    public OperationResult refresh(String bundleKey, String target) {

        return performOperation(bundleKey, target, new BundleOperation() {

            @Override
            public String getName() {
                return BundleOperation.REFRESH;
            }

            @Override
            public boolean changesModuleState() {
                return false;
            }

            @Override
            public void perform(BundleInfo info, String target) {
                bundleService.refresh(info, target);
            }
        });
    }

    private void refreshOtherVersions(Collection<BundleInfo> theseVersionInfos, String target) {

        LinkedList<Bundle> otherVersions = new LinkedList<>();
        for (BundleInfo thisVersionInfo : theseVersionInfos) {
            otherVersions.addAll(getOtherVersionsToRefresh(thisVersionInfo));
        }
        if (otherVersions.isEmpty()) {
            return;
        }

        ArrayList<BundleInfo> otherVersionInfos = new ArrayList<>(otherVersions.size());
        for (Bundle otherVersion : otherVersions) {
            otherVersionInfos.add(BundleInfo.fromBundle(otherVersion));
        }

        logger.info("Refreshing bundles {} as their other versions are currently active...", otherVersionInfos);
        try {
            bundleService.refresh(otherVersionInfos, target);
        } catch (Exception e) {
            logger.error("Error refreshing bundles", e);
        }
    }

    @Override
    public OperationResult update(String bundleKey, String target) {
        readOnlyModeLock.readLock().lock();
        try {
            return performOperation(bundleKey, target, new BundleOperation() {

                @Override
                public String getName() {
                    return "Update";
                }

                @Override
                public boolean changesModuleState() {
                    return true;
                }

                @Override
                public void perform(BundleInfo info, String target) {
                    bundleService.update(info, target);
                }
            });

        } finally {
            readOnlyModeLock.readLock().unlock();
        }
    }

    @Override
    public Map<String, BundleService.BundleInformation> getInfo(String bundleKey, String target) {
        BundleInfo bundleInfo = getBundleInfo(bundleKey);
        return bundleService.getInfo(bundleInfo, target);
    }

    @Override
    public Map<String, Map<String, BundleService.BundleInformation>> getInfos(Collection<String> bundleKeys, String target) {
        LinkedHashSet<BundleInfo> bundleInfos = new LinkedHashSet<>(bundleKeys.size());
        for (String bundleKey : bundleKeys) {
            BundleInfo bundleInfo = getBundleInfo(bundleKey);
            bundleInfos.add(bundleInfo);
        }
        return bundleService.getInfos(bundleInfos, target);
    }

    @Override
    public Map<String, Map<String, BundleInformation>> getBucketInfos(String bundleBucketKey, String target) {
        BundleBucketInfo bundleBucketInfo = getBundleBucketInfo(bundleBucketKey);
        return bundleService.getInfos(bundleBucketInfo, target);
    }

    @Override
    public Map<String, Map<String, BundleService.BundleInformation>> getAllInfos(String target) {
        return bundleService.getAllInfos(target);
    }

    @Override
    public BundleState getLocalState(String bundleKey) {
        BundleInfo bundleInfo = getBundleInfo(bundleKey);
        return bundleService.getLocalState(bundleInfo);
    }

    @Override
    public BundleService.BundleInformation getLocalInfo(String bundleKey) {
        BundleInfo bundleInfo = getBundleInfo(bundleKey);
        return bundleService.getLocalInfo(bundleInfo);
    }

    @Override
    public Map<String, BundleInformation> getBucketLocalInfos(String bundleBucketKey) {
        BundleBucketInfo bundleBucketInfo = getBundleBucketInfo(bundleBucketKey);
        return bundleService.getLocalInfos(bundleBucketInfo);
    }

    @Override
    public Map<String, BundleInformation> getAllLocalInfos() {
        return bundleService.getAllLocalInfos();
    }

    private List<Bundle> getOtherVersionsToRefresh(BundleInfo thisVersionInfo) {

        List<Bundle> result = new LinkedList<>();

        // collect active and resolved module bundles
        Map<ModuleVersion, JahiaTemplatesPackage> allModuleVersions = templateManagerService
                .getTemplatePackageRegistry().getAllModuleVersions().get(thisVersionInfo.getSymbolicName());

        if (allModuleVersions != null && allModuleVersions.size() > 1) {
            for (JahiaTemplatesPackage pkg : allModuleVersions.values()) {
                Bundle otherVersion = pkg.getBundle();
                if (otherVersion != null && otherVersion.getState() == Bundle.RESOLVED
                        && !otherVersion.getVersion().toString().equals(thisVersionInfo.getVersion())) {
                    result.add(otherVersion);
                }
            }
        }

        return result;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    public void setBundleCheckers(Collection<BundleChecker> bundleCheckers) {
        this.bundleCheckers = bundleCheckers;
    }

    @Override
    public void switchReadOnlyMode(boolean enable) {
        readOnlyModeLock.writeLock().lock();
        try {
            readOnly = enable;
        } finally {
            readOnlyModeLock.writeLock().unlock();
        }
    }

    @Override
    public int getReadOnlyModePriority() {
        return 900;
    }

    private void assertWritable() {
        if (readOnly) {
            throw new ReadOnlyModeException("The Module Manager is in read only mode: no operations that change module state are available");
        }
    }

    @Override
    public Collection<BundlePersistentInfo> storeAllLocalPersistentStates() {
        readOnlyModeLock.readLock().lock();
        try {
            assertWritable();

            Collection<BundlePersistentInfo> bundleInfos = Arrays.stream(FrameworkService.getBundleContext().getBundles())
                    .map(BundlePersistentInfo::new)
                    .collect(Collectors.toSet());
            BundleInfoJcrHelper.storePersistentStates(bundleInfos);
            return bundleInfos;

        } catch (ModuleManagementException e) {
            throw e;
        } catch (Exception e) {
            throw new ModuleManagementException(e);
        } finally {
            readOnlyModeLock.readLock().unlock();
        }
    }

    @Override
    public OperationResult applyBundlesPersistentStates(String target) {
        // We only support this for bundles persisted in Jackrabbit and whose start level is higher than 0
        final Predicate<BundlePersistentInfo> persistentStateFilter = bundle ->
                bundle.getStartLevel() > 0 && Constants.URL_PROTOCOL_DX.equals(bundle.getLocationProtocol());

        readOnlyModeLock.readLock().lock();
        try {
            assertWritable();

            final Collection<BundlePersistentInfo> persistentStates = BundleInfoJcrHelper.getPersistentStates()
                    .stream()
                    .filter(persistentStateFilter)
                    .collect(Collectors.toList());
            List<BundleInfo> installedAndUpdatedBundles = installMissingBundlesFromPersistentStates(persistentStates, target);

            final Collection<BundlePersistentInfo> bundles = Arrays.stream(FrameworkService.getBundleContext().getBundles())
                    .map(BundlePersistentInfo::new)
                    .collect(Collectors.toSet());

            for (BundlePersistentInfo persistentState : persistentStates) {
                bundles.stream()
                        .filter(bundle -> bundle.isSameVersionAs(persistentState))
                        .findFirst().ifPresent(bundle -> {
                            try {
                                OperationResult result = applyPersistentState(
                                        bundle.getLocation(), bundle.getState(), persistentState.getState(), target
                                );
                                installedAndUpdatedBundles.addAll(result.getBundleInfos());
                            } catch (Exception e) {
                                logger.info("Cannot apply state for bundle {} reason: {}", bundle.getSymbolicName(), e.getMessage());
                                logger.debug(e.getMessage(), e);
                            }
                        }
                );
            }
            return OperationResult.success(Lists.newArrayList(Sets.newHashSet(installedAndUpdatedBundles)));

        } catch (ModuleManagementException e) {
            throw e;
        } catch (Exception e) {
            throw new ModuleManagementException(e);
        } finally {
            readOnlyModeLock.readLock().unlock();
        }
    }

    /*
     * Install missing modules referenced in persistent states.
     *
     * @param persistentStates the persistent module states
     * @param target the group of cluster nodes targeted
     */
    private List<BundleInfo> installMissingBundlesFromPersistentStates(Collection<BundlePersistentInfo> persistentStates, String target) {
        final Map<String, Set<String>> installedBundles = Arrays.stream(FrameworkService.getBundleContext().getBundles())
                .map(BundlePersistentInfo::new)
                .collect(Collectors.groupingBy(BundlePersistentInfo::getSymbolicName, Collectors.mapping(BundlePersistentInfo::getVersion, Collectors.toSet())));

        final List<BundleInfo> installedBundlesInfo = new ArrayList<>();
        for (BundlePersistentInfo persistentState : persistentStates) {
            Set<String> installedVersions = installedBundles.get(persistentState.getSymbolicName());

            if (installedVersions == null || !installedVersions.contains(persistentState.getVersion())) {
                try {
                    bundleService.install(persistentState.getLocation(), target, false, persistentState.getStartLevel());
                    installedBundlesInfo.add(BundleInfo.fromKey(persistentState.getLocation()));
                } catch (Exception e) {
                    logger.error("Cannot install {}", persistentState.getLocation(), e);
                }
            }
        }
        return installedBundlesInfo;
    }

    /*
     * Apply a persistent state to a specific bundle
     *
     * @param bundleLocation the location of the bundle to apply the persistent state to
     * @param currentState the current state of the bundle to apply the persistent state to
     * @param persistentState the state to apply
     * @param target the group of cluster nodes targeted
     * @return the result of this operation
     */
    private OperationResult applyPersistentState(String bundleLocation, int currentState, int persistentState, String target) {
        if (currentState != persistentState) {
            switch (persistentState) {
                case Bundle.ACTIVE:
                    return start(bundleLocation, target);
                case Bundle.INSTALLED:
                    return stop(bundleLocation, target);
                default:
                    return OperationResult.success(Collections.emptyList());
            }
        }

        return OperationResult.success(Collections.emptyList());
    }

    private Map<String, Object> missingDependency(BundleInfo info) {
        // Return first missing dependency
        for (Bundle b : templateManagerService.getInstalledBundles()) {
            if (b.getSymbolicName().equals(info.getSymbolicName())) {
                String jahiaDepends = b.getHeaders().get("Jahia-Depends");
                String[] dependencies = ModuleUtils.toDependsArray(jahiaDepends);
                List<String> missing = Arrays.stream(dependencies)
                        .filter(dep -> templateManagerService.getAnyDeployedTemplatePackage(dep) == null)
                        .collect(Collectors.toList());
                if (!missing.isEmpty()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("bundle", b);
                    map.put("dependency", missing.get(0));
                    return map;
                }
            }
        }
        return Collections.emptyMap();
    }

    private void checkForMissingDependency(Exception e, BundleInfo info) {
        // Missing dependency on initial install case
        if (e.getCause() instanceof BundleException && ((BundleException) e.getCause()).getType() == BundleException.RESOLVE_ERROR) {
            Map<String, Object> missing = missingDependency(info);
            if (!missing.isEmpty()) {
                throw new ModuleManagementException(
                        String.format("Bundle %s has unresolved dependency %s and won't be started", missing.get("bundle"), missing.get("dependency")));
            }
        }
    }
}
