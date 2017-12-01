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
package org.jahia.services.modulemanager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.drools.core.util.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.BundleLifecycleUtils;
import org.jahia.osgi.BundleState;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.Constants;
import org.jahia.services.modulemanager.InvalidModuleException;
import org.jahia.services.modulemanager.InvalidModuleKeyException;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.ModuleNotFoundException;
import org.jahia.services.modulemanager.OperationResult;
import org.jahia.services.modulemanager.persistence.BundlePersister;
import org.jahia.services.modulemanager.persistence.PersistentBundle;
import org.jahia.services.modulemanager.persistence.PersistentBundleInfoBuilder;
import org.jahia.services.modulemanager.spi.BundleService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.settings.readonlymode.ReadOnlyModeCapable;
import org.jahia.settings.readonlymode.ReadOnlyModeException;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.FrameworkWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * The main entry point service for the module management service, providing functionality for module deployment, undeployment, start and
 * stop operations, which are performed in a seamless way on a standalone installation as well as across the platform cluster.
 *
 * @author Sergiy Shyrkov
 */
public class ModuleManagerImpl implements ModuleManager, ReadOnlyModeCapable {

    private static final Logger logger = LoggerFactory.getLogger(ModuleManagerImpl.class);

    private BundleService bundleService;
    private BundlePersister persister;
    private JahiaTemplateManagerService templateManagerService;
    private volatile boolean readOnly;

    private interface BundleOperation {

        String getName();
        boolean changesModuleState();
        void perform(BundleInfo info, String target) throws ModuleManagementException;
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

        return findTargetBundle(bundleKey, info != null ? info.getSymbolicName() : bundleKey,
                info != null ? info.getVersion() : null);
    }

    private static BundleInfo getBundleInfo(String bundleKey) {
        try {
            return BundleInfo.fromKey(bundleKey);
        } catch (IllegalArgumentException e) {
            throw new InvalidModuleKeyException(bundleKey);
        }
    }

    private OperationResult doInstall(Collection<PersistentBundle> infos, final String target, boolean start) throws ModuleManagementException {
        ArrayList<BundleInfo> bundleInfos = new ArrayList<>(infos.size());
        // phase #1: install bundles but do not start
        for (PersistentBundle info : infos) {
            // install but do not start bundle yet
            bundleService.install(info.getLocation(), target, false);
            bundleInfos.add(toBundleInfo(info));
        }
        // phase #2: if start requested, stop the previous versions of bundles, start requested versions and perform refresh
        if (start) {
            // phase #2.1 stop previous versions
            for (PersistentBundle info : infos) {
                stopPreviousVersions(info, target);
            }
            // phase #2.2 start new versions
            for (PersistentBundle info : infos) {
                bundleService.start(info, target);
            }
            // phase #2.3 refresh stopped versions
            refreshOtherVersions(bundleInfos, target);
        }
        return OperationResult.success(bundleInfos);
    }

    @Override
    public OperationResult install(Resource bundleResource, String target) throws ModuleManagementException {
        return install(bundleResource, target, false);
    }

    @Override
    public OperationResult install(Resource bundleResource, String target, boolean start) throws ModuleManagementException {
        return install(Collections.singleton(bundleResource), target, start);
    }

    @Override
    public OperationResult install(Collection<Resource> bundleResources, String target, boolean start) throws ModuleManagementException {
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
                if (requiresPersisting) {
                    persister.store(bundleInfo);
                }
                bundleInfos.add(bundleInfo);
            }
            result = doInstall(bundleInfos, target, start);
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
                        new Object[] { bundleResources, target, timeTaken, result });
            } else {
                logger.info("Installation failed for bundles {} on target {} (took {} ms). Operation error: {}",
                        new Object[] { bundleResources, target, timeTaken, error });
            }
        }
        return result;
    }

    private OperationResult performOperation(String bundleKey, String target, BundleOperation operation) {

        if (StringUtils.isEmpty(bundleKey)) {
            throw new IllegalArgumentException("Bundle '" + bundleKey + "' key in invalid");
        }

        long startTime = System.currentTimeMillis();
        logger.info("Performing {} operation for bundle {} on target {}",
                new Object[] { operation.getName(), bundleKey, target });

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
            error = e;
            throw e;
        } catch (Exception e) {
            error = e;
            throw new ModuleManagementException(e);
        } finally {
            if (error == null) {
                logger.info("{} operation completed for bundle {} on target {} in {} ms. Opearation result: {}",
                        new Object[] { operation.getName(), bundleKey, target, System.currentTimeMillis() - startTime,
                                result });
            } else {
                logger.info("{} operation failed for bundle {} on target {} (took {} ms). Opearation error: {}",
                        new Object[] { operation.getName(), bundleKey, target, System.currentTimeMillis() - startTime,
                                error });
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

        return performOperation(bundleKey, target, new BundleOperation() {

            @Override
            public String getName() {
                return "Start";
            }

            @Override
            public boolean changesModuleState() {
                return true;
            }

            @Override
            public void perform(BundleInfo info, String target) throws ModuleManagementException {
                stopPreviousVersions(info, target);

                bundleService.start(info, target);

                refreshOtherVersions(Collections.singleton(info), target);
            }
        });
    }

    @Override
    public OperationResult stop(String bundleKey, String target) {

        return performOperation(bundleKey, target, new BundleOperation() {

            @Override
            public String getName() {
                return "Stop";
            }

            @Override
            public boolean changesModuleState() {
                return true;
            }

            @Override
            public void perform(BundleInfo info, String target) throws ModuleManagementException {
                bundleService.stop(info, target);
            }
        });
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

        return performOperation(bundleKey, target, new BundleOperation() {

            @Override
            public String getName() {
                return "Uninstall";
            }

            @Override
            public boolean changesModuleState() {
                return true;
            }

            @Override
            public void perform(BundleInfo info, String target) throws ModuleManagementException {
                bundleService.uninstall(info, target);
            }
        });
    }

    @Override
    public OperationResult refresh(String bundleKey, String target) {

        return performOperation(bundleKey, target, new BundleOperation() {

            @Override
            public String getName() {
                return "Refresh";
            }

            @Override
            public boolean changesModuleState() {
                return false;
            }

            @Override
            public void perform(BundleInfo info, String target) throws ModuleManagementException {
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
    public Map<String, BundleService.BundleInformation> getInfo(String bundleKey, String target) throws ModuleManagementException {
        BundleInfo bundleInfo = getBundleInfo(bundleKey);
        return bundleService.getInfo(bundleInfo, target);
    }

    @Override
    public Map<String, Map<String, BundleService.BundleInformation>> getInfos(Collection<String> bundleKeys, String target) throws ModuleManagementException {
        LinkedHashSet<BundleInfo> bundleInfos = new LinkedHashSet<BundleInfo>(bundleKeys.size());
        for (String bundleKey : bundleKeys) {
            BundleInfo bundleInfo = getBundleInfo(bundleKey);
            bundleInfos.add(bundleInfo);
        }
        return bundleService.getInfos(bundleInfos, target);
    }

    @Override
    public BundleState getLocalState(String bundleKey) throws ModuleManagementException {
        BundleInfo bundleInfo = getBundleInfo(bundleKey);
        return bundleService.getLocalState(bundleInfo);
    }

    @Override
    public BundleService.BundleInformation getLocalInfo(String bundleKey) throws ModuleManagementException {
        BundleInfo bundleInfo = getBundleInfo(bundleKey);
        return bundleService.getLocalInfo(bundleInfo);
    }

    private List<Bundle> getOtherVersionsToRefresh(BundleInfo thisVersionInfo) {

        List<Bundle> result = new LinkedList<>();

        // collect active and resolved module bundles
        Map<ModuleVersion, JahiaTemplatesPackage> allModuleVersions = templateManagerService
                .getTemplatePackageRegistry().getAllModuleVersions().get(thisVersionInfo.getSymbolicName());

        if (allModuleVersions != null && allModuleVersions.size() > 1) {
            FrameworkWiring frameworkWiring = BundleLifecycleUtils.getFrameworkWiring();

            for (JahiaTemplatesPackage pkg : allModuleVersions.values()) {
                Bundle otherVersion = pkg.getBundle();
                if (otherVersion != null && otherVersion.getState() == Bundle.RESOLVED
                        && !otherVersion.getVersion().toString().equals(thisVersionInfo.getVersion())) {

                    // Sometimes a bundle depends on another version of the same bundle,
                    // doing a refresh in this case will cause an infinite loop of start/stop operations
                    Collection<Bundle> dependencies = frameworkWiring
                            .getDependencyClosure(Collections.singleton(otherVersion));
                    boolean doRefresh = true;
                    for (Bundle dependency : dependencies) {
                        if (dependency.getSymbolicName().equals(thisVersionInfo.getSymbolicName())
                                && dependency.getVersion().toString().equals(thisVersionInfo.getVersion())) {
                            // the active bundle depends on this one -> won't refresh it
                            doRefresh = false;
                            break;
                        }
                    }

                    if (doRefresh) {
                        result.add(otherVersion);
                    }
                }
            }
        }

        return result;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    @Override
    public void switchReadOnlyMode(boolean enable) {
        readOnly = enable;
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
}
