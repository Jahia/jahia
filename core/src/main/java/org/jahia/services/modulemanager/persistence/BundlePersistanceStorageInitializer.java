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
package org.jahia.services.modulemanager.persistence;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;

/**
 * Initializer for the persistent storage of bundles.
 *
 * @author Ahmed Chaabni
 */
public class BundlePersistanceStorageInitializer implements JahiaAfterInitializationService {

    private static final Comparator<PersistedBundle> COMPARATOR_BY_KEY = new Comparator<PersistedBundle>() {
        @Override
        public int compare(PersistedBundle o1, PersistedBundle o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    };

    private static final Logger logger = LoggerFactory.getLogger(BundlePersistanceStorageInitializer.class);

    private BundlePersister persister;

    private boolean canHandle(Bundle bundle) {
        Dictionary<String, String> headers = bundle.getHeaders();
        if (canHandle(headers)) {
            return true;
        } else if (headers.get("Fragment-Host") != null) {
            // if the current bundle is a fragment -> check if its host can be handled and thus handle the fragment also
            Bundle hostBundle = BundleUtils.getBundleBySymbolicName(headers.get("Fragment-Host"), null);
            return hostBundle != null && canHandle(hostBundle);
        }

        return false;
    }

    private boolean canHandle(Dictionary<String, String> headers) {
        return headers.get("Jahia-Module-Type") != null || headers.get("Jahia-Cluster-Deployment") != null;
    }

    /**
     * Returns a list of installed bundles that we should persist.
     *
     * @return a list of installed bundles that we should persist
     */
    private List<PersistedBundle> getBundlesToBePersisted() {
        List<PersistedBundle> bundles = new LinkedList<>();

        for (Bundle bundle : FrameworkService.getBundleContext().getBundles()) {
            if (canHandle(bundle)) {

                String bundleLocation = bundle.getLocation();
                // Remove 'legacydepends:' from location if it exists
                if (bundleLocation.startsWith("legacydepends:")) {
                    bundleLocation = bundleLocation.substring("legacydepends:".length());
                }

                PersistedBundle bundleInfo = null;
                try {
                    UrlResource resource = new UrlResource(bundleLocation);

                    bundleInfo = PersistedBundleInfoBuilder.build(resource);
                    if (bundleInfo != null) {
                        bundles.add(bundleInfo);
                    }
                } catch (MalformedURLException e) {
                    logger.error("Unable to retrieve bundle from location: " + bundleLocation, e);
                } catch (Exception e) {
                    logger.error("Unable to extract information for bundle: " + bundle, e);
                }

            }

        }
        return bundles;
    }

    @Override
    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        try {
            if (!persister.isStorageInitialized()) {
                // need to populate the storage
                initializeStorage();
            }
        } catch (ModuleManagementException e) {
            logger.error("Unable to initialized persistent bundle storage. Cause: " + e.getMessage(), e);
            throw new JahiaInitializationException("Unable to initialized persistent bundle storage", e);
        }
    }

    private void initializeStorage() {
        long startTime = System.currentTimeMillis();
        logger.info("Start initializing bundle persistent storage...");

        List<PersistedBundle> installedBundles = getBundlesToBePersisted();
        Collections.sort(installedBundles, COMPARATOR_BY_KEY);

        logger.info("Detected {} bundles to be persisted", installedBundles.size());

        int persistedCount = 0;
        for (PersistedBundle bundle : installedBundles) {
            try {
                logger.info("Persisting bundle {}", bundle);
                persister.store(bundle);
                persistedCount++;
            } catch (Exception e) {
                logger.error("Unable to persist bundle: " + bundle, e);
            }
        }

        logger.info("Bundle persistent storage initialized with {} bundles in {} ms", persistedCount,
                System.currentTimeMillis() - startTime);
    }

    public void setPersister(BundlePersister persister) {
        this.persister = persister;
    }

}