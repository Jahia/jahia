/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
