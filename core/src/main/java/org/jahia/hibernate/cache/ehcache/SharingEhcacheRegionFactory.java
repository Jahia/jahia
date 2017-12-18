/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.hibernate.cache.ehcache;

import net.sf.ehcache.CacheManager;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.ehcache.EhCacheRegionFactory;
import org.hibernate.cfg.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * A sharing Hibernate Ehcache provider that can re-use a CacheManager provided by Jahia.
 * This is useful for using Hibernate inside modules.
 */
public class SharingEhcacheRegionFactory extends EhCacheRegionFactory {

    private static final String EXISTING_CACHE_MANAGER_NAME = "org.jahia.hibernate.ehcache.existingCacheManagerName";

    private static final Logger LOG = LoggerFactory.getLogger(SharingEhcacheRegionFactory.class.getName());

    private static final long serialVersionUID = 827915208448076343L;

    @Override
    public void start(Settings settings, Properties properties) throws CacheException {
        if (manager != null) {
            LOG.warn("Attempt to restart an already started EhCacheProvider. Use sessionFactory.close() "
                    + " between repeated calls to buildSessionFactory. Using previously created EhCacheProvider."
                    + " If this behaviour is required, consider using SingletonEhCacheProvider.");
            return;
        }
        try {
            String existingCacheManagerName = null;
            if (properties != null) {
                existingCacheManagerName = (String) properties.get(EXISTING_CACHE_MANAGER_NAME);
            }
            if (existingCacheManagerName == null || existingCacheManagerName.length() == 0) {
                throw new CacheException("No existing cache manager name specified in configuration, property "
                        + EXISTING_CACHE_MANAGER_NAME + " was not set properly.");
            } else {
                manager = CacheManager.getCacheManager(existingCacheManagerName);
            }
        } catch (net.sf.ehcache.CacheException e) {
            if (e.getMessage().startsWith(
                    "Cannot parseConfiguration CacheManager. Attempt to create a new instance of "
                            + "CacheManager using the diskStorePath")) {
                throw new CacheException(
                        "Attempt to restart an already started EhCacheProvider. Use sessionFactory.close() "
                                + " between repeated calls to buildSessionFactory. Consider using SingletonEhCacheProvider. Error from "
                                + " ehcache was: " + e.getMessage());
            } else {
                throw e;
            }
        }

    }

    @Override
    public void stop() {
        // Do nothing; we let the real creator of the cache manager do the shutdown.
    }
}
