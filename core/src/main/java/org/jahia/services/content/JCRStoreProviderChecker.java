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
package org.jahia.services.content;

import java.util.HashMap;
import java.util.Map;

import org.jahia.services.content.decorator.JCRMountPointNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Laprun
 */
public class JCRStoreProviderChecker {
    private static Logger logger = LoggerFactory.getLogger(JCRStoreProvider.class);

    private final Map<String, JCRStoreProvider> toPeriodicallyCheck = new HashMap<String, JCRStoreProvider>();

    private static final Object lock = new Object();

    private static final ThreadLocal<Boolean> isFromChecker = new ThreadLocal<Boolean>() {
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
