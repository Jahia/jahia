/**
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms & Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.services.content;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jahia.services.content.decorator.JCRMountPointNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Laprun
 */
public class JCRStoreProviderChecker implements Serializable{

    private static final long serialVersionUID = 1L;

    private static Logger logger = LoggerFactory.getLogger(JCRStoreProvider.class);

    private final Map<String, JCRStoreProvider> toPeriodicallyCheck = new HashMap<String, JCRStoreProvider>();

    private static final transient Object lock = new Object();

    private static final transient ThreadLocal<Boolean> isFromChecker = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    public static boolean isFromChecker() {
        return isFromChecker.get();
    }


    public void run() {
        final Map<String, JCRStoreProvider> toCheck;

        synchronized (lock) {
            toCheck = new HashMap<String, JCRStoreProvider>(toPeriodicallyCheck);
        }


        try {
            isFromChecker.set(true);

            for (Map.Entry<String, JCRStoreProvider> entry : toCheck.entrySet()) {
                final String key = entry.getKey();

                // if we managed to open the stream, this means the remote server is back up
                // so attempt to mount the associated provider
                final JCRStoreProvider provider = entry.getValue();
                if (provider.isAvailable(true)) {
                    logger.info("Remote server " + getProviderName(provider, key) + " came back online. Attempting to re-mount provider.");

                    provider.setMountStatus(JCRMountPointNode.MountStatus.mounted);

                    synchronized (lock) {
                        // remove the URL from the list of servers to check
                        toPeriodicallyCheck.remove(key);
                    }
                    logger.info("Re-mount successful!");
                }
            }
        } finally {
            isFromChecker.set(false);
        }
    }

    public void checkPeriodically(JCRStoreProvider provider) {
        final String key = provider.getKey();
        synchronized (lock) {
            toPeriodicallyCheck.put(key, provider);
        }

        String providerName = getProviderName(provider, key);
        logger.info("Added provider " + providerName + " to list of servers to periodically check");
    }

    private String getProviderName(JCRStoreProvider provider, String key) {
        String providerName = provider.getUrl();
        providerName = providerName != null ? providerName : key;
        return providerName;
    }


    public void remove(String key) {
        synchronized (lock) {
            toPeriodicallyCheck.remove(key);
        }
    }
}
