/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.provisioning.impl.operations;

import org.apache.felix.fileinstall.ArtifactUrlTransformer;
import org.apache.felix.utils.version.VersionCleaner;
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
import org.jahia.utils.comparator.VersionComparator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
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
    public static final String IGNORE_CHECKS = "ignoreChecks";
    public static final String FORCE_UPDATE = "forceUpdate";
    public static final String UNINSTALL_PREVIOUS_VERSION = "uninstallPreviousVersion";
    public static final String TARGET = "target";
    public static final String IF = "if";
    private static final String[] SUPPORTED_KEYS = {INSTALL_BUNDLE, INSTALL_AND_START_BUNDLE, INSTALL_OR_UPGRADE_BUNDLE};
    private static final Logger logger = LoggerFactory.getLogger(InstallBundle.class);
    private BundleContext bundleContext;
    private ModuleManager moduleManager;

    private Collection<ArtifactUrlTransformer> transformers = new HashSet<>();

    /**
     * Activate
     *
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

    /**
     * Register ArtifactUrlTransformer service
     *
     * @param transformer transformer
     */
    @Reference(service = ArtifactUrlTransformer.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void addOperation(ArtifactUrlTransformer transformer) {
        this.transformers.add(transformer);
    }

    /**
     * Unregister ArtifactUrlTransformer service
     *
     * @param transformer transformer
     */
    public void removeOperation(ArtifactUrlTransformer transformer) {
        this.transformers.remove(transformer);
    }


    @Override
    public boolean canHandle(Map<String, Object> entry) {
        return Arrays.stream(SUPPORTED_KEYS).anyMatch(entry::containsKey);
    }

    @Override
    public void init(ExecutionContext executionContext) {
        Map<String, Set<Bundle>> installedBundles = Arrays.stream(bundleContext.getBundles())
                .collect(Collectors.groupingBy(Bundle::getSymbolicName, Collectors.toSet()));
        executionContext.getContext().put("installedBundles", installedBundles);
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        Map<String, Set<Bundle>> installedBundles = (Map<String, Set<Bundle>>) executionContext.getContext().get("installedBundles");
        Optional<String> keyOptional = Arrays.stream(SUPPORTED_KEYS).filter(entry::containsKey).findFirst();
        List<OperationResult> installResults = new ArrayList<>();
        List<OperationResult> startResults = new ArrayList<>();
        List<OperationResult> uninstallResults = new ArrayList<>();
        if (keyOptional.isPresent()) {
            String key = keyOptional.get();
            List<Map<String, Object>> entries = ProvisioningScriptUtil.convertToList(entry, key, "url");

            LinkedHashMap<BundleInfo, String> toStart = new LinkedHashMap<>();
            LinkedHashMap<BundleInfo, String> toUninstall = new LinkedHashMap<>();

            for (Map<String, Object> subEntry : entries) {
                String condition = (String) subEntry.get(IF);
                if (condition == null || ProvisioningScriptUtil.evalCondition(condition)) {
                    doInstall(subEntry, key, executionContext, installedBundles, toStart, toUninstall, installResults);
                }
            }

            processAutoStart(toStart, startResults);
            processUninstall(toUninstall, uninstallResults);

            if (executionContext.getContext().get("result") instanceof Collection) {
                Map<String, List<OperationResult>> all = new HashMap<>();
                all.put("install", installResults);
                all.put("start", startResults);
                all.put("uninstall", uninstallResults);
                ((Collection) executionContext.getContext().get("result")).add(all);
            }
        }
    }

    private void processUninstall(LinkedHashMap<BundleInfo, String> toUninstall, List<OperationResult> results) {
        for (Map.Entry<BundleInfo, String> bundleInfoStringEntry : toUninstall.entrySet()) {
            BundleInfo bundleInfo = bundleInfoStringEntry.getKey();
            try {
                results.add(moduleManager.uninstall(bundleInfo.getKey(), bundleInfoStringEntry.getValue()));
            } catch (Exception e) {
                logger.error("Cannot uninstall {}", bundleInfo.getKey(), e);
            }
        }
    }

    private void processAutoStart(LinkedHashMap<BundleInfo, String> toStart, List<OperationResult> results) {
        for (Map.Entry<BundleInfo, String> bundleInfoStringEntry : toStart.entrySet()) {
            BundleInfo bundleInfo = bundleInfoStringEntry.getKey();
            try {
                Bundle bundle = BundleUtils.getBundle(bundleInfo.getSymbolicName(), bundleInfo.getVersion());
                if (bundle != null && !BundleUtils.isFragment(bundle)) {
                    results.add(moduleManager.start(bundleInfo.getKey(), bundleInfoStringEntry.getValue()));
                }
            } catch (Exception e) {
                logger.error("Cannot start {}", bundleInfo.getKey(), e);
            }
        }
    }

    private void doInstall(Map<String, Object> entry, String key, ExecutionContext executionContext, Map<String, Set<Bundle>> installedBundles,
                           LinkedHashMap<BundleInfo, String> toStart, LinkedHashMap<BundleInfo, String> toUninstall, List<OperationResult> results) {
        String bundleKey = (String) entry.get(key);
        try {
            Resource resource = ProvisioningScriptUtil.getResource(bundleKey, executionContext);
            logger.info("Installing resource {}", resource);
            resource = transformURL(resource);

            if (entry.get(FORCE_UPDATE) != Boolean.TRUE && checkAlreadyInstalled(bundleKey, resource)) {
                return;
            }

            if (entry.get(INSTALL_OR_UPGRADE_BUNDLE) != null && checkMoreRecentVersion(resource, bundleKey, installedBundles)) {
                return;
            }

            String target = (String) entry.get(TARGET);
            OperationResult result = moduleManager.install(
                    Collections.singleton(resource), target,
                    false,
                    Optional.ofNullable((Integer) entry.get(START_LEVEL)).orElse(SettingsBean.getInstance().getModuleStartLevel()),
                    entry.get(IGNORE_CHECKS) == Boolean.TRUE
            );
            if (result.getBundleInfos().size() == 1) {
                BundleInfo bundleInfo = result.getBundleInfos().get(0);
                Set<Bundle> installedVersions = installedBundles.get(bundleInfo.getSymbolicName());

                setupAutoStart(entry, bundleInfo, target, installedVersions, toStart);
                setupUninstall(entry, bundleInfo, target, installedVersions, toUninstall);
            }
            results.add(result);
        } catch (Throwable e) {
            logger.error("Cannot install {} = {}. Please make sure the artifact is reachable within the registered maven repositories (network, credentials ...)", bundleKey, e.getClass().getName());
        }
    }

    private boolean checkMoreRecentVersion(Resource resource, String bundleKey, Map<String, Set<Bundle>> installedBundles) {
        // Check if a more recent version is already installed
        try (InputStream is = resource.getInputStream()) {
            JarInputStream zip = new JarInputStream(is);
            Manifest mf = zip.getManifest();
            String name = mf.getMainAttributes().getValue("Bundle-SymbolicName");
            String versionString = mf.getMainAttributes().getValue("Implementation-Version");
            if (versionString == null) {
                versionString = mf.getMainAttributes().getValue("Bundle-Version");
            }
            if (name == null || versionString == null) {
                logger.warn("Cannot read manifest from {}", bundleKey);
                return false;
            }
            String osgiVersion = VersionCleaner.clean(versionString);
            Version thisVersion = new Version(osgiVersion);
            Set<Bundle> installedVersions = installedBundles.get(name);
            if (installedVersions != null && installedVersions.stream().anyMatch(b ->
                VersionComparator.compare(b.getVersion(), thisVersion) > 0)) {
                logger.info("Skipping installation of {}, a more recent version is already installed", bundleKey);
                return true;
            }
        } catch (IOException e) {
            logger.error("Cannot read bundle information for {}", bundleKey, e);
        }
        return false;
    }

    private Resource transformURL(Resource resource) throws Exception {
        if (resource instanceof FileSystemResource) {
            ArtifactUrlTransformer transformer = findTransformer(resource.getFile());
            if (transformer != null) {
                URL transformedURL = transformer.transform(resource.getURL());
                resource = new UrlResource(transformedURL);
                logger.info("Resource has been transformed to {}", resource);
            }
        }
        return resource;
    }

    private ArtifactUrlTransformer findTransformer(File artifact) {
        for (ArtifactUrlTransformer transformer : transformers) {
            if (transformer.canHandle(artifact)) {
                return transformer;
            }
        }
        return null;
    }

    private void setupAutoStart(Map<String, Object> entry, BundleInfo bundleInfo, String target, Set<Bundle> installedVersions, LinkedHashMap<BundleInfo, String> toStart) {
        boolean autoStart = entry.get(AUTO_START) == Boolean.TRUE || entry.get(INSTALL_AND_START_BUNDLE) != null;

        if (entry.get(AUTO_START) != Boolean.FALSE && entry.get(INSTALL_OR_UPGRADE_BUNDLE) != null) {
            logger.info("Setup autostart {} {}", bundleInfo.getSymbolicName(), bundleInfo.getVersion());
            // In case of upgrade, get the previous version state, or auto-start by default
            Version thisVersion = new Version(bundleInfo.getVersion());
            if (installedVersions != null && logger.isDebugEnabled()) {
                logger.debug("Installed versions : {}", installedVersions.stream().map(Bundle::getVersion).collect(Collectors.toList()));
                logger.debug("Any more recent match : {}", installedVersions.stream().filter(b -> VersionComparator.compare(b.getVersion(), thisVersion) > 0).findAny());
                logger.debug("Previous versions started : {}", installedVersions.stream().map(b -> b.getVersion() + "=" + (b.getState() == Bundle.ACTIVE || b.adapt(BundleStartLevel.class).isPersistentlyStarted())).collect(Collectors.toList()));
            } else {
                logger.debug("No previously installed versions, autostart module");
            }
            autoStart = installedVersions == null || (
                    installedVersions.stream().noneMatch(b -> VersionComparator.compare(b.getVersion(), thisVersion) > 0) &&
                            installedVersions.stream().anyMatch(b -> b.getState() == Bundle.ACTIVE || b.adapt(BundleStartLevel.class).isPersistentlyStarted())
            );
        }

        if (autoStart) {
            toStart.put(bundleInfo, target);
        }
    }

    private void setupUninstall(Map<String, Object> entry, BundleInfo bundleInfo, String target, Set<Bundle> installedVersions, LinkedHashMap<BundleInfo, String> toUninstall) {
        boolean uninstallPreviousVersions = installedVersions != null &&
                (entry.get(UNINSTALL_PREVIOUS_VERSION) == Boolean.TRUE || entry.get(INSTALL_OR_UPGRADE_BUNDLE) != null);

        if (uninstallPreviousVersions) {
            for (Bundle installedVersion : installedVersions) {
                if (VersionComparator.compare(installedVersion.getVersion(), new Version(bundleInfo.getVersion())) < 0) {
                    toUninstall.put(BundleInfo.fromBundle(installedVersion), target);
                }
            }
        }
    }

    private boolean checkAlreadyInstalled(String bundleKey, Resource resource) throws IOException {
        PersistentBundle bundleInfo = PersistentBundleInfoBuilder.build(resource, false, false);
        if (bundleInfo == null) {
            throw new InvalidModuleException();
        }
        Bundle bundle = bundleContext.getBundle(bundleInfo.getLocation());
        String qualifier = (bundle != null) ? bundle.getVersion().getQualifier() : "";
        if (qualifier != null && !qualifier.endsWith("SNAPSHOT")) {
            logger.info("Bundle {} already installed, skip", bundleKey);
            return true;
        }
        return false;
    }

}
