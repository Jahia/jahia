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
import org.jahia.services.cache.CacheProvider;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.PlaceholderUtils;
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
        try {
            try (InputStream is = configurationResource.getInputStream()) {
                try (InputStream interpolatedInputStream = PlaceholderUtils.resolvePlaceholders(is, settingsBean, true)) {
                    cacheManager = CacheManager.newInstance(interpolatedInputStream);
                }
            }
        } catch (IOException e) {
            throw new JahiaInitializationException(e.getMessage(), e);
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

    @Override
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
    @Override
    public synchronized SelfPopulatingCache registerSelfPopulatingCache(String cacheName, CacheEntryFactory factory) {
        return registerSelfPopulatingCache(cacheName, null, factory);
    }

    /**
     * This method register a SelfPopulatingCache in the CacheManager.
     * @param cacheName the name of the cache to be registered
     * @param searchable optional search configuration for the cache
     * @param factory the CacheFactory to be used to fill the CacheEntry
     * @return the instance of the registered cache
     */
    @Override
    public synchronized SelfPopulatingCache registerSelfPopulatingCache(String cacheName, Searchable searchable, CacheEntryFactory factory) {
        // Call getEhCache to be sure to have the decorated cache. We manipulate only EhCache not Cache object
        Ehcache cache = cacheManager.getEhcache(cacheName);
        if (cache != null) {
            if (cache instanceof SelfPopulatingCache) {
                return (SelfPopulatingCache) cache;
            }
        } else {
            // get the default configuration four the self populating Caches
            Configuration configuration = cacheManager.getConfiguration();
            Map<String,CacheConfiguration> cacheConfigurations = configuration.getCacheConfigurations();
            // Use config for cacheName, or org.jahia.selfPopulatingReplicatedCache by default
            CacheConfiguration cacheConfiguration = cacheConfigurations.get(cacheConfigurations.containsKey(cacheName) ? cacheName : "org.jahia.selfPopulatingReplicatedCache");
            if (searchable != null) {
                cacheConfiguration.addSearchable(searchable);
            }
            PinningConfiguration pinningConfiguration = new PinningConfiguration();
            pinningConfiguration.setStore("INCACHE");
            cacheConfiguration.addPinning(pinningConfiguration);
            // Create a new cache with the configuration
            cache = new Cache(cacheConfiguration);
            cache.setName(cacheName);
            // Cache name has been set now we can initialize it by putting it in the manager.
            // Only Cache manager is initializing caches.
            cache = cacheManager.addCacheIfAbsent(cache);
        }

        // Create a decorated cache from an initialized cache.
        SelfPopulatingCache selfPopulatingCache = new SelfPopulatingCache(cache, factory);
        // replace the cache in the manager to be sure that everybody is using the decorated instance.
        cacheManager.replaceCacheWithDecoratedCache(cache, selfPopulatingCache);
        return selfPopulatingCache;
    }
}
