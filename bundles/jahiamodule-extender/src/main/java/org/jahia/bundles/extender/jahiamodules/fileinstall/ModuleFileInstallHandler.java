/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.extender.jahiamodules.fileinstall;

import org.apache.felix.fileinstall.*;
import org.jahia.osgi.BundleLifecycleUtils;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.OperationResult;
import org.jahia.services.modulemanager.persistence.PersistentBundle;
import org.jahia.services.modulemanager.persistence.PersistentBundleInfoBuilder;
import org.jahia.services.modulemanager.util.ModuleUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handler class for DX modules which processes artifacts from the FileInstall service.
 *
 * @author Sergiy Shyrkov
 */
public class ModuleFileInstallHandler implements CustomHandler {

    private class LocationMappingProperties extends Properties {
        private static final long serialVersionUID = -4051934369037923034L;

        LocationMappingProperties() throws IOException {
            super();
            load();
        }

        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<Object>(super.keySet()));
        }

        synchronized void load() throws IOException {
            try (InputStream is = new FileInputStream(bundleLocationMapFile)) {
                super.load(is);
            }
        }

        synchronized void store() throws FileNotFoundException, IOException {
            try (OutputStream os = new FileOutputStream(bundleLocationMapFile)) {
                store(os, null);
            }
        }
    }

    private static final String BUNDLE_LOCATION_MAP_FILE = "felix.fileinstall.bundleLocationMapFile";

    private static final Logger logger = LoggerFactory.getLogger(ModuleFileInstallHandler.class);

    private static final String START_NEW_BUNDLES = "felix.fileinstall.bundles.new.start";

    private static final String TARGET_GROUP = null;

    private static final String UNINSTALL_REMOVE = "felix.fileinstall.bundles.uninstall.remove";

    private boolean autoStartBundles;

    private File bundleLocationMapFile;

    private Boolean removedDataOnUninstall;

    private List<Long> createdOnStartup = new ArrayList<>();

    /**
     * Initializes an instance of this class.
     *
     * @param cfg file install configuration
     */
    public ModuleFileInstallHandler(Properties cfg) {
        super();
        this.autoStartBundles = Boolean.valueOf(cfg.getProperty(START_NEW_BUNDLES));
        // by default, bundles are not removed from file system when uninstalled
        this.removedDataOnUninstall = Boolean.valueOf(cfg.getProperty(UNINSTALL_REMOVE));
        bundleLocationMapFile = initBundleLocationMapFile(cfg.getProperty(BUNDLE_LOCATION_MAP_FILE));
    }

    private void addLocationMapping(String location, File path) {
        if (bundleLocationMapFile == null) {
            return;
        }
        try {
            LocationMappingProperties props = new LocationMappingProperties();
            String normalizedPath = path.toURI().normalize().getPath();
            if (!props.containsKey(location) || !props.getProperty(location).equals(normalizedPath)) {
                props.put(location, normalizedPath);
                props.store();
            }
        } catch (IOException e) {
            logger.warn("Error adding bundle location mapping for " + location, e);
        }
    }

    @Override
    public Properties getBundleLocationMapping() {
        if (bundleLocationMapFile != null) {
            try {
                return new LocationMappingProperties();
            } catch (IOException e) {
                logger.warn("Unable to load bundle location mapping from file " + bundleLocationMapFile, e);
            }
        }

        return null;
    }

    private ModuleManager getModuleManager() {
        return ModuleUtils.getModuleManager();
    }

    private File initBundleLocationMapFile(String path) {
        File target = null;
        if (path != null) {
            try {
                target = new File(path).getCanonicalFile();
                if (!target.exists()) {
                    target.createNewFile();
                }
            } catch (IOException e) {
                throw new RuntimeException("Cannot read/create file for bundle location map at: " + path, e);
            }
        }

        return target;
    }

    private void install(Artifact artifact) throws Exception {
        File path = artifact.getPath();

        // If the listener is an installer, ask for an installer
        if (artifact.getListener() instanceof ArtifactInstaller) {
            logger.info("Installing artifact {}", path);
            ((ArtifactInstaller) artifact.getListener()).install(path);
        } else if (artifact.getListener() instanceof ArtifactUrlTransformer) {
            logger.info("Installing url transformed artifact {}", path);
            // if the listener is an url transformer
            install(artifact, new UrlResource(artifact.getTransformedUrl()));
        } else if (artifact.getListener() instanceof ArtifactTransformer) {
            logger.info("Installing transformed artifact {}", path);
            // if the listener is an artifact transformer
            install(artifact, new FileSystemResource(artifact.getTransformed()));
        }
    }

    private void install(Artifact artifact, Resource bundleResource) {
        OperationResult result = getModuleManager().install(bundleResource, TARGET_GROUP);
        for (BundleInfo bundleInfo : result.getBundleInfos()) {
            Bundle b = BundleUtils.getBundle(bundleInfo.getSymbolicName(), bundleInfo.getVersion());
            if (b != null) {
                artifact.setBundleId(b.getBundleId());
                addLocationMapping(b.getLocation(), artifact.getPath());
            }
        }
    }

    private void reconcile(Artifact artifact) {
        File path = artifact.getPath();
        logger.info("Reconciling {}", path);

        URL transformed = artifact.getTransformedUrl();
        String location = transformed.toString();

        Bundle b = BundleUtils.getBundle(location);
        if (b != null) {
            artifact.setBundleId(b.getBundleId());
        }

        addLocationMapping(location, path);
    }

    @Override
    public void process(List<Artifact> created, List<Artifact> modified, List<Artifact> deleted) {
        logger.info("Processing FileInstall artifacts: {} created, {} modified, {} deleted",
                new Object[] { created.size(), modified.size(), deleted.size() });

        deleted.sort(Comparator.comparing(o -> o.getPath().getName()));
        for (Artifact artifact : deleted) {
            try {
                uninstall(artifact);
            } catch (Exception e) {
                logger.error("Error uninstalling artifact " + artifact.getPath(), e);
            }
        }

        modified.sort(Comparator.comparing(o -> o.getPath().getName()));
        for (Artifact artifact : modified) {
            try {
                update(artifact);
            } catch (Exception e) {
                logger.error("Error updating artifact " + artifact.getPath(), e);
            }
        }

        List<Artifact> added = created;
        List<Artifact> restored = Collections.emptyList();
        if (!FrameworkService.getInstance().isStarted() && FrameworkService.getInstance().isFirstStartup()) {
            // When the framework is starting for the first time, we avoid calling install if the bundle corresponding
            // to a created artifact is already installed to avoid excessive and non-necessary refreshes.
            // This would occur when bundles' states are restored on startup. In this situation the restored
            // bundles have actually been re-installed before FileInstall kicks off. Thus, we just need to
            // reconcile the artifact with the restored bundle to preserve FileInstall consistency.
            Map<Boolean, List<Artifact>> alreadyInstalled = created.stream().collect(Collectors.partitioningBy(a -> isAlreadyInstalled(a)));
            added = alreadyInstalled.get(false);
            restored = alreadyInstalled.get(true);
            logger.info("Processing FileInstall artifacts: {} to be upgraded", added.size());
            if(logger.isInfoEnabled()) {
                StringJoiner joiner = new StringJoiner(",");
                for (Artifact bundle : added) {
                    try {
                        joiner.add(bundle.getPath().getCanonicalPath());
                    } catch (IOException e) {
                        logger.error("Error getting bundle path", e);
                    }
                }
                logger.info("Upgrading  artifacts : {}", joiner);
            }
        }

        restored.sort(Comparator.comparing(o -> o.getPath().getName()));
        for (Artifact artifact : restored) {
            try {
                reconcile(artifact);
            } catch (Exception e) {
                logger.error("Error reconciling artifact " + artifact.getPath(), e);
            }
        }

        added.sort(Comparator.comparing(o -> o.getPath().getName()));
        for (Artifact artifact : added) {
            try {
                install(artifact);
            } catch (Exception e) {
                logger.error("Error installing artifact " + artifact.getPath(), e);
            }
        }

        if (!FrameworkService.getInstance().isStarted() && FrameworkService.getInstance().isFirstStartup()) {
            createdOnStartup.addAll(added.stream().map(Artifact::getBundleId).collect(Collectors.toList()));
        } else if (autoStartBundles) {
            startBundles(created);
        }

        logger.info("Done processing FileInstall artifacts");
    }

    private void removeLocationMapping(File path) {
        if (bundleLocationMapFile == null) {
            return;
        }
        try {
            LocationMappingProperties props = new LocationMappingProperties();
            String normalizedPath = path.toURI().normalize().getPath();
            boolean updated = false;
            for (Iterator<Map.Entry<Object, Object>> it = props.entrySet().iterator(); it.hasNext();) {
                Map.Entry<Object, Object> entry = it.next();
                String filePath = (String) entry.getValue();
                if (normalizedPath.equals(filePath)) {
                    it.remove();
                    updated = true;
                }
            }
            if (updated) {
                props.store();
            }
        } catch (IOException e) {
            logger.warn("Error removing bundle location mapping for " + path, e);
        }
    }

    @Override
    public void removeUninstalledBundleData(Artifact artifact) {
        if (!removedDataOnUninstall) {
            return;
        }

        removeLocationMapping(artifact.getPath());

        File transformed = artifact.getTransformed();
        if (transformed != null && !transformed.equals(artifact.getPath()) && !transformed.delete()) {
            logger.warn("Unable to delete transformed artifact: {}", transformed.getAbsolutePath());
        }

        File jaredDirectory = artifact.getJaredDirectory();
        if (jaredDirectory != null && !jaredDirectory.equals(artifact.getPath()) && !jaredDirectory.delete()) {
            logger.warn("Unable to delete jared artifact: {}", jaredDirectory.getAbsolutePath());
        }

        if (artifact.getPath().exists()) {
            if (!artifact.getPath().delete()) {
                logger.warn("Unable to delete file {} for uninstalled bundle {}", artifact.getPath(),
                        artifact.getBundleId());
            } else {
                logger.info("File {} for bundle {} has been removed from file system", artifact.getPath(),
                        artifact.getBundleId());
            }
        }
        // The file can be deleted at shutdown of the JVM on Windows, check if the file still exists after removal
        if (artifact.getPath().exists()) {
            logger.warn("File {} still exists on file system, it will be removed at JVM shutdown", artifact.getPath());
            artifact.getPath().deleteOnExit();
        }

    }

    private void startBundles(List<Artifact> installed) {
        List<Bundle> toBeStarted = new LinkedList<>();
        int frameworkStartLevel = BundleLifecycleUtils.getFrameworkStartLevel();
        for (Artifact artifact : installed) {
            Bundle bundle = artifact.getBundleId() > 0 ? BundleUtils.getBundle(artifact.getBundleId()) : null;
            if (bundle != null && bundle.getState() != Bundle.UNINSTALLED && !BundleUtils.isFragment(bundle)
                    && frameworkStartLevel >= BundleLifecycleUtils.getBundleStartLevel(bundle)) {
                toBeStarted.add(bundle);
            }
        }

        if (!toBeStarted.isEmpty()) {
            BundleLifecycleUtils.startModules(toBeStarted, true);
        }
    }

    private void uninstall(Artifact artifact) throws Exception {
        File path = artifact.getPath();
        logger.info("Uninstalling {}", path);

        removeLocationMapping(path);

        // if the listener is an installer, uninstall the artifact
        if (artifact.getListener() instanceof ArtifactInstaller) {
            ((ArtifactInstaller) artifact.getListener()).uninstall(path);
        }
        // else we need uninstall the bundle
        else if (artifact.getBundleId() != 0) {
            // old can't be null because of the way we calculate deleted list.
            Bundle bundle = BundleUtils.getBundle(artifact.getBundleId());
            if (bundle == null) {
                logger.warn("Failed to uninstall bundle {} with id {}. The bundle has already been uninstalled", path,
                        artifact.getBundleId());
            } else {
                getModuleManager().uninstall(BundleInfo.fromBundle(bundle).getKey(), TARGET_GROUP);
            }
        }
    }

    private void update(Artifact artifact) throws Exception {
        File path = artifact.getPath();
        logger.info("Updating {}", path);

        // If the listener is an installer, ask for an update
        if (artifact.getListener() instanceof ArtifactInstaller) {
            ((ArtifactInstaller) artifact.getListener()).update(path);
        }
        // if the listener is an url transformer
        else if (artifact.getListener() instanceof ArtifactUrlTransformer) {
            URL transformed = artifact.getTransformedUrl();
            getModuleManager().install(new UrlResource(transformed), TARGET_GROUP);
        }
        // else we need to ask for an update on the bundle
        else if (artifact.getListener() instanceof ArtifactTransformer) {
            File transformed = artifact.getTransformed();
            getModuleManager().install(new FileSystemResource(transformed), TARGET_GROUP);
        }
    }

    @Override
    public void watcherStarted() {
        // notify the framework that the file install watcher has started and processed found modules
        FrameworkService.getInstance().notifyFileInstallStarted(createdOnStartup);
    }

    /*
     * Checks whether or not a given artifact is already persisted and installed.
     *
     * @param artifact the artifact to check
     * @return {@code true} if a corresponding bundle is installed and has the same checksum
     *         than {@code artifact}, {@code false} otherwise
     */
    private boolean isAlreadyInstalled(Artifact artifact) {
        if (artifact.getListener() instanceof ArtifactUrlTransformer) {
            String location = artifact.getTransformedUrl().toString();
            Bundle bundle = BundleUtils.getBundle(location);
            if ((bundle != null) && (bundle.getState() != Bundle.UNINSTALLED)) {
                String bundleKey = BundleInfo.fromBundle(bundle).getKey();
                try {
                    PersistentBundle persistentBundle = ModuleUtils.loadPersistentBundle(bundleKey);
                    String artifactChecksum = computeChecksum(artifact);
                    return Objects.equals(persistentBundle.getChecksum(), artifactChecksum);
                } catch (ModuleManagementException e) {
                    logger.debug("Could not find bundle " + bundleKey + " in persistent storage", e);
                } catch (IOException e) {
                    logger.error("Failed to compute checksum of " + bundleKey, e);
                }
            }
        }
        return false;
    }

    /*
     * Computes the checksum of the given artifact using the same algorithm used to compute the one
     * stored with persisted bundles.
     *
     * @param artifact the artifact to compute the checksum from
     * @return the computed checksum
     * @throws IOException if an error occurs while computing the checksum
     */
    private String computeChecksum(Artifact artifact) throws IOException {
        Resource resource = new UrlResource(artifact.getJaredUrl());
        return PersistentBundleInfoBuilder.build(resource, true, false).getChecksum();
    }

}
