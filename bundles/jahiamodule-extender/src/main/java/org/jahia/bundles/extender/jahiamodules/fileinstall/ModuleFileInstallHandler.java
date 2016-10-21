/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.extender.jahiamodules.fileinstall;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.felix.fileinstall.Artifact;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.apache.felix.fileinstall.ArtifactTransformer;
import org.apache.felix.fileinstall.ArtifactUrlTransformer;
import org.apache.felix.fileinstall.CustomHandler;
import org.jahia.osgi.BundleLifecycleUtils;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.util.ModuleUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;

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

    private final static String BUNDLE_LOCATION_MAP_FILE = "felix.fileinstall.bundleLocationMapFile";

    private static final Logger logger = LoggerFactory.getLogger(ModuleFileInstallHandler.class);

    private static final String START_NEW_BUNDLES = "felix.fileinstall.bundles.new.start";

    private static final String TARGET_GROUP = null;

    private final static String UNINSTALL_REMOVE = "felix.fileinstall.bundles.uninstall.remove";

    private boolean autoStartBundles;

    private File bundleLocationMapFile;

    private Boolean removedDataOnUninstall;

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
        logger.info("Installing {}", path);

        // If the listener is an installer, ask for an install
        if (artifact.getListener() instanceof ArtifactInstaller) {
            ((ArtifactInstaller) artifact.getListener()).install(path);
        } else if (artifact.getListener() instanceof ArtifactUrlTransformer) {
            // if the listener is an url transformer
            URL transformed = artifact.getTransformedUrl();
            String location = transformed.toString();
            getModuleManager().install(new UrlResource(transformed), TARGET_GROUP);
            Bundle b = BundleUtils.getBundle(location);
            if (b != null) {
                artifact.setBundleId(b.getBundleId());
            }

            addLocationMapping(location, path);

        } else if (artifact.getListener() instanceof ArtifactTransformer) {
            // if the listener is an artifact transformer
            File transformed = artifact.getTransformed();
            String location = path.toURI().normalize().toString();
            getModuleManager().install(new FileSystemResource(transformed), TARGET_GROUP);
            Bundle b = BundleUtils.getBundle(location);
            if (b != null) {
                artifact.setBundleId(b.getBundleId());
            }
        }
    }

    @Override
    public void process(List<Artifact> created, List<Artifact> modified, List<Artifact> deleted) {
        logger.info("Processing FileInstall artifacts: {} created, {} modified, {} deleted",
                new Object[] { created.size(), modified.size(), deleted.size() });

        for (Artifact artifact : deleted) {
            try {
                uninstall(artifact);
            } catch (Exception e) {
                logger.error("Error uninstalling artifact " + artifact.getPath(), e);
            }
        }

        for (Artifact artifact : modified) {
            try {
                update(artifact);
            } catch (Exception e) {
                logger.error("Error updating artifact " + artifact.getPath(), e);
            }
        }

        for (Artifact artifact : created) {
            try {
                install(artifact);
            } catch (Exception e) {
                logger.error("Error installing artifact " + artifact.getPath(), e);
            }
        }
        if (autoStartBundles) {
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
        File transformed = artifact.getTransformed();
        if (transformed != null && !transformed.equals(artifact.getPath()) && !transformed.delete()) {
            logger.warn("Unable to delete transformed artifact: {}", transformed.getAbsolutePath());
        }

        File jaredDirectory = artifact.getJaredDirectory();
        if (jaredDirectory != null && !jaredDirectory.equals(artifact.getPath()) && !jaredDirectory.delete()) {
            logger.warn("Unable to delete jared artifact: {}" + jaredDirectory.getAbsolutePath());
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
            // boolean useModuleManagerApi = SettingsBean.getInstance().isClusterActivated();
            BundleLifecycleUtils.startModules(toBeStarted, true);
            // if (!useModuleManagerApi) {
            // BundleLifecycleUtils.startBundlesPendingDependencies();
            // }
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
        FrameworkService.notifyFileInstallStarted();
    }

}
