/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.provisioning.impl.operations;

import org.jahia.osgi.BundleUtils;
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.InvalidModuleException;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.OperationResult;
import org.jahia.services.modulemanager.persistence.PersistentBundle;
import org.jahia.services.modulemanager.persistence.PersistentBundleInfoBuilder;
import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Install operation
 */
@Component(service = Operation.class, property = "type=installBundle")
public class InstallBundle implements Operation {
    public static final String AUTO_START = "autoStart";
    public static final String INSTALL_BUNDLE = "installBundle";
    public static final String INSTALL_AND_START_BUNDLE = "installAndStartBundle";
    public static final String INSTALL_OR_UPGRADE_BUNDLE = "installOrUpgradeBundle";
    public static final String START_LEVEL = "startLevel";
    public static final String FORCE_UPDATE = "forceUpdate";
    public static final String UNINSTALL_PREVIOUS_VERSION = "uninstallPreviousVersion";
    public static final String TARGET = "target";
    private static final String[] SUPPORTED_KEYS = { INSTALL_BUNDLE, INSTALL_AND_START_BUNDLE, INSTALL_OR_UPGRADE_BUNDLE };
    private static final Logger logger = LoggerFactory.getLogger(InstallBundle.class);
    private BundleContext bundleContext;
    private ModuleManager moduleManager;

    /**
     * Activate
     * @param bundleContext bundle context
     */
    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Reference
    public void setModuleManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    @Override
    public boolean canHandle(Map<String, Object> entry) {
        return Arrays.stream(SUPPORTED_KEYS).anyMatch(k -> entry.get(k) instanceof String);
    }

    @Override
    public void init(ExecutionContext executionContext) {
        Map<String, Set<Bundle>> installedBundles = Arrays.stream(bundleContext.getBundles())
                .collect(Collectors.groupingBy(Bundle::getSymbolicName, Collectors.toSet()));
        executionContext.getContext().put("installedBundles", installedBundles);
        executionContext.getContext().put("toStart", new ArrayList<BundleInfo>());
        executionContext.getContext().put("toUninstall", new ArrayList<BundleInfo>());
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        try {
            Map<String, Set<Bundle>> installedBundles = (Map<String, Set<Bundle>>) executionContext.getContext().get("installedBundles");
            Optional<String> bundleKeyOptional = Arrays.stream(SUPPORTED_KEYS).map(k -> (String) entry.get(k)).filter(Objects::nonNull).findFirst();
            if (bundleKeyOptional.isPresent()) {
                String bundleKey = bundleKeyOptional.get();
                UrlResource resource = new UrlResource(bundleKey);
                if (entry.get(FORCE_UPDATE) != Boolean.TRUE && checkAlreadyInstalled(bundleKey, resource)) {
                    return;
                }
                OperationResult result = moduleManager.install(
                        Collections.singleton(resource), (String) entry.get(TARGET),
                        false,
                        Optional.ofNullable((Integer) entry.get(START_LEVEL)).orElse(SettingsBean.getInstance().getModuleStartLevel())
                );
                if (result.getBundleInfos().size() == 1) {
                    BundleInfo bundleInfo = result.getBundleInfos().get(0);
                    Set<Bundle> installedVersions = installedBundles.get(bundleInfo.getSymbolicName());

                    setupAutoStart(entry, executionContext, bundleInfo, installedVersions);
                    setupUninstall(entry, executionContext, bundleInfo, installedVersions);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot install {}", entry.get(INSTALL_BUNDLE), e);
        }
    }

    private void setupAutoStart(Map<String, Object> entry, ExecutionContext executionContext, BundleInfo bundleInfo, Set<Bundle> installedVersions) {
        // Should this (autostart / uninstall) be done immediately ?

        boolean autoStart = entry.get(AUTO_START) == Boolean.TRUE || entry.get(INSTALL_AND_START_BUNDLE) != null;

        if (entry.get(AUTO_START) != Boolean.FALSE && entry.get(INSTALL_OR_UPGRADE_BUNDLE) != null) {
            // In case of upgrade, get the previous version state, or auto-start by default
            autoStart = installedVersions == null || installedVersions.stream().anyMatch(b -> b.getState() == Bundle.ACTIVE);
        }

        if (autoStart) {
            getToStart(executionContext).add(bundleInfo);
        }
    }

    private void setupUninstall(Map<String, Object> entry, ExecutionContext executionContext, BundleInfo bundleInfo, Set<Bundle> installedVersions) {
        boolean uninstallPreviousVersions = installedVersions != null &&
                (entry.get(UNINSTALL_PREVIOUS_VERSION) == Boolean.TRUE || entry.get(INSTALL_OR_UPGRADE_BUNDLE) != null);

        if (uninstallPreviousVersions) {
            for (Bundle installedVersion : installedVersions) {
                if (!installedVersion.getVersion().toString().equals(bundleInfo.getVersion())) {
                    getToUninstall(executionContext).add(BundleInfo.fromBundle(installedVersion));
                }
            }
        }
    }

    private boolean checkAlreadyInstalled(String bundleKey, UrlResource resource) throws IOException {
        PersistentBundle bundleInfo = PersistentBundleInfoBuilder.build(resource, false, false);
        if (bundleInfo == null) {
            throw new InvalidModuleException();
        }
        Bundle bundle = bundleContext.getBundle(bundleInfo.getLocation());
        if (bundle != null) {
            logger.info("Bundle {} already installed, skip", bundleKey);
            return true;
        }
        return false;
    }

    @Override
    public void cleanup(ExecutionContext executionContext) {
        for (BundleInfo bundleInfo : getToStart(executionContext)) {
            try {
                Bundle bundle = BundleUtils.getBundle(bundleInfo.getSymbolicName(), bundleInfo.getVersion());
                if (bundle != null && !BundleUtils.isFragment(bundle)) {
                    moduleManager.start(bundleInfo.getKey(), null);
                }
            } catch (Exception e) {
                logger.error("Cannot start {}", bundleInfo.getKey(), e);
            }
        }

        for (BundleInfo bundleInfo : getToUninstall(executionContext)) {
            try {
                moduleManager.uninstall(bundleInfo.getKey(), null);
            } catch (Exception e) {
                logger.error("Cannot uninstall {}", bundleInfo.getKey(), e);
            }
        }
    }

    private List<BundleInfo> getToUninstall(ExecutionContext executionContext) {
        return (List<BundleInfo>) executionContext.getContext().get("toUninstall");
    }

    private List<BundleInfo> getToStart(ExecutionContext executionContext) {
        return (List<BundleInfo>) executionContext.getContext().get("toStart");
    }

}
