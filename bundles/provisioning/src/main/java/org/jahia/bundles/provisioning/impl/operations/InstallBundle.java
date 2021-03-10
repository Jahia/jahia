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
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.OperationResult;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Install operation
 */
@Component(service = Operation.class, property = "type=installBundle")
public class InstallBundle implements Operation {
    public static final String AUTO_START = "autoStart";
    public static final String INSTALL_BUNDLE = "installBundle";
    public static final String INSTALL_BUNDLE_AND_START = "installAndStartBundle";
    public static final String START_LEVEL = "startLevel";
    public static final String UNINSTALL_PREVIOUS_VERSION = "uninstallPreviousVersion";
    public static final String TARGET = "target";
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
        return entry.get(INSTALL_BUNDLE) instanceof String || entry.get(INSTALL_BUNDLE_AND_START) instanceof String;
    }

    @Override
    public void init(ExecutionContext executionContext) {
        Map<String, Set<String>> installedBundles = Arrays.stream(bundleContext.getBundles())
                .collect(Collectors.groupingBy(Bundle::getSymbolicName, Collectors.mapping(b -> b.getVersion().toString(), Collectors.toSet())));
        executionContext.getContext().put("installedBundles", installedBundles);
        executionContext.getContext().put("toStart", new ArrayList<BundleInfo>());
        executionContext.getContext().put("toUninstall", new ArrayList<BundleInfo>());
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        try {
            Map<String, Set<String>> installedBundles = (Map<String, Set<String>>) executionContext.getContext().get("installedBundles");

            String bundleKey = (String) Optional.ofNullable(entry.get(INSTALL_BUNDLE_AND_START)).orElse(entry.get(INSTALL_BUNDLE));
            OperationResult result = moduleManager.install(
                    Collections.singleton(new UrlResource(bundleKey)), (String) entry.get(TARGET),
                    false,
                    Optional.ofNullable((Integer) entry.get(START_LEVEL)).orElse(SettingsBean.getInstance().getModuleStartLevel())
            );
            if (result.getBundleInfos().size() == 1) {
                BundleInfo bundleInfo = result.getBundleInfos().get(0);

                // Should this (autostart / uninstall) be done immediately ?

                if (entry.get(AUTO_START) == Boolean.TRUE || entry.get(INSTALL_BUNDLE_AND_START) != null) {
                    getToStart(executionContext).add(bundleInfo);
                }

                Set<String> installedVersions = installedBundles.get(bundleInfo.getSymbolicName());
                if (entry.get(UNINSTALL_PREVIOUS_VERSION) == Boolean.TRUE && installedVersions != null) {
                    for (String installedVersion : installedVersions) {
                        if (!installedVersion.equals(bundleInfo.getVersion())) {
                            getToUninstall(executionContext).add(BundleInfo.fromModuleInfo(bundleInfo.getSymbolicName(), installedVersion));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Cannot install {}", entry.get(INSTALL_BUNDLE), e);
        }
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
