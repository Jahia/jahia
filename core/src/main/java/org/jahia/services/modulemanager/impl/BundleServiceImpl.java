/*
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
import org.apache.poi.util.IOUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.modulemanager.model.BundleDTO;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.jcr.RepositoryException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Bundle Service Implementation to manage the cluster nodes bundles.
 *
 * @author achaabni
 */
public class BundleServiceImpl {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(BundleServiceImpl.class);

    /**
     * Get local bundles from Context
     *
     * @return local bundles with states
     * @throws RepositoryException
     */
    private Map<BundleDTO, String> getLocalBundles() throws RepositoryException {
        Map<BundleDTO, String> result = new HashMap<>();


        for (Bundle contextBundle : FrameworkService.getBundleContext().getBundles()) {
            logger.info("putting " + contextBundle + " in JCR");
            if (contextBundle.getHeaders().get("Jahia-Module-Type") != null || contextBundle.getHeaders().get("Jahia-Cluster-Deployment") != null) {
                BundleDTO bundleToAdd = new BundleDTO();
                String bundleLocation = contextBundle.getLocation();
                // Remove 'legacydepends:' from location if it exists
                if (bundleLocation.startsWith("legacydepends:")) {
                    bundleLocation = bundleLocation.substring("legacydepends:".length());
                }
                try {
                    bundleToAdd.setFileName(FilenameUtils.getName(new URL(bundleLocation).getPath()));
                    FileSystemResource jarFile = new FileSystemResource(new URL(bundleLocation).getPath());
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
                    bundleToAdd.setBundleKey(bundleToAdd.getSymbolicName() + "-" + bundleToAdd.getVersion());
                    bundleToAdd.setGroupId(contextBundle.getHeaders().get("Jahia-GroupId"));
                    result.put(bundleToAdd, "test");
                } catch (Exception e) {
                    logger.error("Error finding bundle file from history " + contextBundle, e);
                }
            }

        }
        return result;
    }

    private static String calculateDigest(Resource jarFile) throws IOException {
        byte[] b = new byte[1024 * 8];
        DigestInputStream digestInputStream = null;
        try {
            digestInputStream = ModuleManagerImpl
                    .toDigestInputStream(new BufferedInputStream(jarFile.getInputStream()));
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
     * populate the list of bundles in module management entity
     *
     * @param bundles bundles
     * @return map of the bundle list states
     */
    public void populateBundles(TreeMap<String, BundleDTO> bundles, Map<String, String> states) {
        try {
            for (Map.Entry<BundleDTO, String> entry : getLocalBundles().entrySet()) {
                bundles.put(entry.getKey().getBundleKey(), entry.getKey());
                states.put(entry.getKey().getBundleKey(), entry.getValue());
            }
        } catch (RepositoryException e) {
            logger.error("Error initializing and verifying cluster JCR structures", e);
        }
    }

}