/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.cache;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** <p>Here are the methods that should be called to create new instances of caches.</p>
 *
 * @author  Fulco Houkes, Copyright (c) 2003 by Jahia Ltd.
 * @version 1.0
 * @since   Jahia 4.0
 *
 * @see     org.jahia.services.cache.Cache Cache
 */
public class CacheFactory extends CacheService {

    /** class unique instance. */
    private static CacheFactory instance;

    /** logging. */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (CacheFactory.class);

    /** caches table. */
    final private Map<String, Cache<?, ?>> caches = new ConcurrentHashMap<String, Cache<?, ?>> (53);

    private Map<String,CacheProvider> cacheProviders = null;
    private Map<String,String> cacheProviderForCache = null;
    // hashmap containing the cache size limits specified in Jahia config.
    private Map<String, Long> cacheLimits = null;
    private Map<String, Long> cacheGroupsLimits = null;
    private long freeMemoryLimit;
    public static final String DEFAULT_CACHE = "DEFAULT_CACHE";

    /** Default constructor, creates a new <code>JahiaCacheFactory</code> instance.
     */
    protected CacheFactory () {
    }

    public void start()
        throws JahiaInitializationException
    {
        // do nothing when settings are missing
        if (settingsBean == null) {
            cacheLimits = new ConcurrentHashMap<String, Long>(2503);
            return;
        }
        Iterator<CacheProvider> providerIterator = cacheProviders.values().iterator();
        while (providerIterator.hasNext()) {
            CacheProvider cacheProvider = providerIterator.next();
            cacheProvider.init(settingsBean,this);
        }

        cacheLimits = settingsBean.getMaxCachedValues();
        cacheGroupsLimits = settingsBean.getMaxCachedGroupsValues();
        freeMemoryLimit = new Long(settingsBean.getFreeMemoryLimit().split("MB")[0]).longValue()*(1024*1024);

        // now we set the limits for caches that have already been created, because this could happen before
        // the init of this service.
        for (Cache<?, ?> cache : caches.values()) {
            if (cacheLimits.containsKey(cache.getName())) {
                Long cacheLimit = (Long) cacheLimits.get(cache.getName());
                cache.setCacheLimit(cacheLimit.longValue());
            } else {
                cache.setCacheLimit(freeMemoryLimit);
            }
            if (cacheGroupsLimits.containsKey(cache.getName())) {
                Long cacheGroupsLimit = (Long) cacheGroupsLimits.get(cache.getName());
                cache.setCacheGroupsLimit(cacheGroupsLimit.longValue());
            }
        }
    }

    // Javadoc inherited from parent
    public synchronized void stop ()
            throws JahiaException {

        // flush the caches
        // we deactivated this because on a cluster we don't want to flush
        // on all nodes.
        // flushAllCaches();
        caches.clear();

        Iterator<CacheProvider> providerIterator = cacheProviders.values().iterator();
        while (providerIterator.hasNext()) {
            CacheProvider cacheProvider = providerIterator.next();
            cacheProvider.shutdown();
        }
    }

    /** Return the unique instance of this class.
     *
     * @return  the class' unique instance.
     */
    public static synchronized CacheFactory getInstance () {
        if (instance == null) {
            instance = new CacheFactory ();
        }
        return instance;
    }
    public synchronized <K, V> Cache<K, V> createCacheInstance (String name)
            throws JahiaInitializationException{
        String provider = cacheProviderForCache.get(name);
        if(provider == null)
        provider = DEFAULT_CACHE;
        return createCacheInstance(name, provider);
    }
    public synchronized <K, V> Cache<K, V> createCacheInstance (String name, String cacheProvider)
            throws JahiaInitializationException
    {
        // validity check
        if (name == null)
            return null;

        // When the cache already exists in the factory, return the instance.
        Cache<K, V> cache = getCache (name);
        if (cache != null) {
            return cache;
        }

        if (cacheLimits == null) {
            cacheLimits = new ConcurrentHashMap<String, Long>(2503);
        }
        if (cacheGroupsLimits == null) {
            cacheGroupsLimits = new ConcurrentHashMap<String, Long>(2503);
        }

        // instanciate the new cache, can throw an JahiaInitialization exception
        cache = new Cache(name, cacheProviders.get(cacheProvider).newCacheImplementation(name));

        if (cacheLimits.containsKey(name)) {
            Long cacheLimit = (Long) cacheLimits.get(name);
            cache.setCacheLimit(cacheLimit.longValue());
        } else {
            cache.setCacheLimit(freeMemoryLimit);
        }
        if (cacheGroupsLimits.containsKey(cache.getName())) {
            Long cacheGroupsLimit = (Long) cacheGroupsLimits.get(cache.getName());
            cache.setCacheGroupsLimit(cacheGroupsLimit.longValue());
        }
        logger.debug ("Created cache instance [" + name + "]");

        if (registerCache (cache)) {
            return cache;
        }

        cache = null;
        return null;
    }

    private boolean registerCache (Cache<?, ?> cache) {
        // Add the cache to the table
        caches.put (cache.getName (), cache);

        return true;
    }



    public SkeletonCache<GroupCacheKey, SkeletonCacheEntry> getSkeletonCacheInstance() throws JahiaInitializationException {
        // if the Html cache already exists, then return the instance
        SkeletonCache<GroupCacheKey, SkeletonCacheEntry> cache = (SkeletonCache<GroupCacheKey, SkeletonCacheEntry>) getCache (SkeletonCache.SKELETON_CACHE);
        if (cache != null)
            return cache;

        // At this point, the HTML cache does not exist, create it
        String providerName = cacheProviderForCache.get(SkeletonCache.SKELETON_CACHE);
        if(providerName == null)
            providerName = DEFAULT_CACHE;
        CacheProvider provider = cacheProviders.get(providerName);
        cache = new SkeletonCache<GroupCacheKey, SkeletonCacheEntry> (provider.newCacheImplementation(SkeletonCache.SKELETON_CACHE));
        if (cacheLimits.containsKey(cache.getName())) {
            Long cacheLimit = (Long) cacheLimits.get(cache.getName());
            cache.setCacheLimit(cacheLimit.longValue());
        } else {
            cache.setCacheLimit(freeMemoryLimit);
        }
        caches.put (cache.getName(), cache);
        return cache;
    }

    /** <p>Retrieves the specified <code>region</code> cache.</p>
     *
     * @param name the cache region name, <code>null</code> is not allowed
     *
     * @return  the cache instance
     */
    public Cache getCache(String name) {
        if (name == null) {
            return null;
        }
        return caches.get(name);
    }

    public Set<String> getNames () {
        return caches.keySet();
    }


    public synchronized void flushAllCaches () {

        Iterator<String> cacheNames = getNames().iterator();
        while (cacheNames.hasNext ()) {
            String curCacheName = cacheNames.next ();
            Cache<?, ?> cache = caches.get (curCacheName);

            cache.flush();
        }

        logger.info ("Flushed all caches.");
    }


    public boolean isClusterCache() {
        return cacheProviders.get(DEFAULT_CACHE).isClusterCache();
    }

    public void enableClusterSync() throws JahiaInitializationException {
        Iterator<CacheProvider> providerIterator = cacheProviders.values().iterator();
        while (providerIterator.hasNext()) {
            CacheProvider cacheProvider = providerIterator.next();
            cacheProvider.enableClusterSync();
        }
    }

    public void stopClusterSync() {
        Iterator<CacheProvider> providerIterator = cacheProviders.values().iterator();
        while (providerIterator.hasNext()) {
            CacheProvider cacheProvider = providerIterator.next();
            cacheProvider.stopClusterSync();
        }
    }

    public void syncClusterNow() {
        Iterator<CacheProvider> providerIterator = cacheProviders.values().iterator();
        while (providerIterator.hasNext()) {
            CacheProvider cacheProvider = providerIterator.next();
            cacheProvider.syncClusterNow();
        }
    }

    public Map<String, CacheProvider> getCacheProviders() {
        return cacheProviders;
    }

    public void setCacheProviders(Map<String, CacheProvider> cacheProviders) {
        this.cacheProviders = cacheProviders;
    }

    public Map<String, String> getCacheProviderForCache() {
        return cacheProviderForCache;
    }

    public void setCacheProviderForCache(Map<String, String> cacheProviderForCache) {
        this.cacheProviderForCache = cacheProviderForCache;
    }
}
