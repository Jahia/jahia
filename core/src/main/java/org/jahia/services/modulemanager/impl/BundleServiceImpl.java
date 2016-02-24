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
package org.jahia.services.modulemanager.impl;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
// import org.apache.felix.framework.cache.BundleArchive;
// import org.apache.felix.framework.cache.BundleCache;
import org.apache.poi.util.IOUtils;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.modulemanager.model.*;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Bundle Service Implementation to manage the cluster nodes bundles.
 * 
 * @author achaabni
 */
public class BundleServiceImpl{

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(BundleServiceImpl.class);

    
//    private Properties felixProperties;

    /**
     * Get local bundles from Context
     *
     * @return local bundles with states
     * @throws RepositoryException
     */
    private Map<BundleDTO, String> getLocalBundles() throws RepositoryException {
        Map<BundleDTO, String> result = new HashMap<>();
        /* BundleArchive[] archives = null;
        // Get Felix Bundle Archives.
        try {
            archives = getBundleArchives();
        } catch (Exception e) {
            logger.error(e.getMessage());
            // Continue
        }
        */

        for (org.osgi.framework.Bundle contextBundle : FrameworkService.getBundleContext().getBundles()) {
            logger.info("putting " + contextBundle + " in JCR");
            if (contextBundle.getHeaders().get("Jahia-Module-Type") != null || contextBundle.getHeaders().get("Jahia-Cluster-Deployment") != null) {
                BundleDTO bundleToAdd = new BundleDTO();
                String bundleLocation = contextBundle.getLocation();
                // Remove 'legacydepends:' from location if it exists
                if(bundleLocation.startsWith("legacydepends:"))
                {
                    bundleLocation = bundleLocation.substring("legacydepends:".length());
                }
                try {
                    bundleToAdd.setFileName(FilenameUtils.getName(new URL(bundleLocation).getPath()));
                    File jarFile = new File(new URL(bundleLocation).getPath());
                    if (jarFile == null) {
                        logger.warn("Unable to find the location of the bundle.jar for bunlde {}", bundleToAdd.getName());
                        continue;
                    }
                    bundleToAdd.setJarFile(jarFile);
                    bundleToAdd.setChecksum(calculateDigest(jarFile));
                } catch (MalformedURLException e) {
                    logger.error(e.getMessage());
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
                try {
                    bundleToAdd.setSymbolicName(contextBundle.getHeaders().get("Bundle-SymbolicName"));
                    bundleToAdd.setDisplayName(contextBundle.getHeaders().get("Bundle-Name"));
                    String version = contextBundle.getHeaders().get("Implementation-Version");
                    if (version == null) {
                        version = contextBundle.getHeaders().get("Bundle-Version");
                    }
                    bundleToAdd.setVersion(version);
                    bundleToAdd.setName(bundleToAdd.getSymbolicName() + "-" + bundleToAdd.getVersion());
                    bundleToAdd.setGroupId(contextBundle.getHeaders().get("Jahia-GroupId"));
                    result.put(bundleToAdd, "test");
                } catch (Exception e) {
                    logger.error("Error finding bundle file from history " + contextBundle , e);
                }
            }

        }
        return result;
    }

    /*
    private static File getBundleJar(long bundleId, BundleArchive[] archives) {
        BundleArchive arch = findBundleArchiveById(bundleId, archives);

        if (arch == null) {
            return null;
        }

        File jar = new File(arch.getCurrentRevision().getRevisionRootDir(), "bundle.jar");

        return jar.exists() ? jar : null;
    }
    */

    private static String calculateDigest(File jarFile) throws IOException {
        byte[] b = new byte[1024 * 8];
        DigestInputStream digestInputStream = null;
        try {
            digestInputStream = ModuleManagerImpl
                    .toDigestInputStream(new BufferedInputStream(new FileInputStream(jarFile)));
            int read = 0;
            while (read != -1) {
                read = digestInputStream.read(b);
            }

            return Hex.encodeHexString(digestInputStream.getMessageDigest().digest());
        } finally {
            IOUtils.closeQuietly(digestInputStream);
        }
    }

    /**
     * Find bundle archive by its ID from a list of bundle archives.
     * 
     * @param bundleId
     *            the ID of the bundle
     * @param archives
     *            an array of bundle archives
     * @return the found bundle archive or null if no bundle archive with this ID could be found
     */
    /*
    private static BundleArchive findBundleArchiveById(long bundleId, BundleArchive[] archives) {
        BundleArchive result = null;
        try {
            for (BundleArchive archive : archives) {
                if (archive.getId() == bundleId) {
                    result = archive;
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return result;
    }
    */

    /**
     * Get Bundle Archives from Bundle Cache.
     *
     * @return list of archives
     */
    /*
    private BundleArchive[] getBundleArchives() throws Exception {
        BundleArchive[] result = null;
        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put(Constants.FRAMEWORK_STORAGE, felixProperties.getProperty(Constants.FRAMEWORK_STORAGE));
        configMap.put(BundleCache.CACHE_LOCKING_PROP, "false");
        BundleCache bundleCache = new BundleCache(new org.apache.felix.framework.Logger(), configMap);
        result = bundleCache.getArchives();
        return result;
    }
    */

    /**
     * populate the list of bundles in module management entity
     * @param bundles bundles
     * @return map of the bundle list states
     */
    public Map<String, String> populateBundles(TreeMap<String, BundleDTO> bundles) {
        Map<String, String> states = new HashMap<>();
        try {
            for (Map.Entry<BundleDTO, String> entry : getLocalBundles().entrySet()) {
                BundleDTO bundle = entry.getKey();
                bundles.put(bundle.getName(), bundle);
                states.put(bundle.getName(), entry.getValue());
            }
        } catch (RepositoryException e) {
            logger.error("Error initializing and verifying cluster JCR structures", e);
        }

        return states;
    }

    /**
     * Populate the list of bundles in the cluster node
     * 
     * @param clusterNode
     *            cluster node to update its bundles
     * @param bundleSources
     *            bundles sources
     * @param bundleStates the map bundle-to-state 
     */
    public void populateNodeBundles(ClusterNode clusterNode, TreeMap<String, Bundle> bundleSources, Map<String, String> bundleStates) {
        for (Map.Entry<String, Bundle> entry : bundleSources.entrySet()) {
            NodeBundle nodeBundle = new NodeBundle(entry.getKey());
            nodeBundle.setBundle(entry.getValue());
            if (bundleStates != null) {
                String state = bundleStates.get(entry.getKey());
                if (state != null) {
                    nodeBundle.setState(state);
                }
            }
            clusterNode.getBundles().put(nodeBundle.getName(), nodeBundle);
        }
    }


//    public void setFelixProperties(Properties felixProperties) {
//        this.felixProperties = felixProperties;
//    }
}