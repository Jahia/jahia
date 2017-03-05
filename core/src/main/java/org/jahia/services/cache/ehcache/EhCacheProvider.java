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
package org.jahia.services.cache.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.PinningConfiguration;
import net.sf.ehcache.config.Searchable;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.apache.tika.io.IOUtils;
import org.jahia.services.cache.CacheProvider;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.settings.SettingsBean;
import org.jahia.exceptions.JahiaInitializationException;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Map;

/**
 * EHCache based cache provider implementation.
 * @author Serge Huber
 */
public class EhCacheProvider implements CacheProvider {

    final private static Logger logger = LoggerFactory.getLogger(EhCacheProvider.class);

    private CacheManager cacheManager = null;
    private int groupsSizeLimit = 100;
    private Resource configurationResource;
    private boolean statisticsEnabled;
    private boolean jmxActivated = true;
    private boolean initialized = false;

    public void init(SettingsBean settingsBean, CacheService cacheService) throws JahiaInitializationException {
        if (initialized) {
            return;
        }
        InputStream is = null;
        try {
            is = configurationResource.getInputStream();
            cacheManager = CacheManager.newInstance(is);
        } catch (IOException e) {
            throw new JahiaInitializationException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        if (jmxActivated) {
            ManagementService.registerMBeans(cacheManager, ManagementFactory.getPlatformMBeanServer(), true, true,
                    true, true, true);
        }
        initialized = true;
    }

    public void shutdown() {
        if (initialized) {
            logger.info("Shutting down cache provider, serializing to disk if active. Please wait...");
            long startTime = System.currentTimeMillis();
            cacheManager.shutdown();
            logger.info("Cache provider shutdown completed in {} ms", System.currentTimeMillis() - startTime);
            initialized = false;
        }
    }

    public CacheImplementation<?, ?> newCacheImplementation(String name) {
        return new EhCacheImpl(name, cacheManager, this);
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public int getGroupsSizeLimit() {
        return groupsSizeLimit;
    }

    public void setGroupsSizeLimit(int groupsSizeLimit) {
        this.groupsSizeLimit = groupsSizeLimit;
    }

    public void setConfigurationResource(Resource configurationResource) {
        this.configurationResource = configurationResource;
    }

    public void setJmxActivated(boolean jmxActivated) {
        this.jmxActivated = jmxActivated;
    }

    public void setStatisticsEnabled(boolean statisticsEnabled) {
        this.statisticsEnabled = statisticsEnabled;
    }

    public boolean isStatisticsEnabled() {
        return statisticsEnabled;
    }

    /**
     * This method register a SelfPopulatingCache in the CacheManager.
     * @param cacheName the name of the cache to be registered
     * @param factory the CacheFactory to be used to fill the CacheEntry
     * @return teh instance of the registered cache
     */
    public synchronized SelfPopulatingCache registerSelfPopulatingCache(String cacheName, CacheEntryFactory factory) {
        return registerSelfPopulatingCache(cacheName, null, factory);
    }

    /**
     * This method register a SelfPopulatingCache in the CacheManager.
     * @param cacheName the name of the cache to be registered
     * @param factory the CacheFactory to be used to fill the CacheEntry
     * @return teh instance of the registered cache
     */
    public synchronized SelfPopulatingCache registerSelfPopulatingCache(String cacheName, Searchable searchable, CacheEntryFactory factory) {
        // Call getEhCache to be sure to have the decorated cache. We manipulate only EhCache not Cache object
        if (cacheManager.getEhcache(cacheName) == null) {
            // get the default configuration four the self populating Caches
            Configuration configuration = cacheManager.getConfiguration();
            Map<String,CacheConfiguration> cacheConfigurations = configuration.getCacheConfigurations();
            CacheConfiguration cacheConfiguration = cacheConfigurations.get("org.jahia.selfPopulatingReplicatedCache");
            if (searchable != null) {
                cacheConfiguration.addSearchable(searchable);
            }
            PinningConfiguration pinningConfiguration = new PinningConfiguration();
            pinningConfiguration.setStore("INCACHE");
            cacheConfiguration.addPinning(pinningConfiguration);
            // Create a new cache with the configuration
            Ehcache cache = new Cache(cacheConfiguration);
            cache.setName(cacheName);
            // Cache name has been set now we can initialize it by putting it in the manager.
            // Only Cache manager is initializing caches.
            cache = cacheManager.addCacheIfAbsent(cache);
            // Create a decorated cache from an initialized cache.
            SelfPopulatingCache selfPopulatingCache = new SelfPopulatingCache(cache, factory);
            // replace the cache in the manager to be sure that everybody is using the decorated instance.
            cacheManager.replaceCacheWithDecoratedCache(cache, selfPopulatingCache);
            return selfPopulatingCache;
        } else {
            return (SelfPopulatingCache) cacheManager.getEhcache(cacheName);
        }
    }
}