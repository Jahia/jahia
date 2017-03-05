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
package org.jahia.services.cache;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.dummy.DummyCacheProvider;

import java.util.HashMap;
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

    /** logging. */
    private static final org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger (CacheFactory.class);

    /** caches table. */
    final private Map<String, Cache<?, ?>> caches = new ConcurrentHashMap<String, Cache<?, ?>> (53);

    private Map<String,CacheProvider> cacheProviders = new HashMap<String, CacheProvider>();
    private Map<String,String> cacheProviderForCache = new HashMap<String, String>();
    public static final String DEFAULT_CACHE = "default";

    /** Default constructor, creates a new <code>JahiaCacheFactory</code> instance.
     */
    private CacheFactory() {
        super();
    }

    public void start() throws JahiaInitializationException {
    	if (cacheProviders.isEmpty()) {
    		logger.warn("No cache provider are configured. Using no-cache provider as a default one.");
    		cacheProviders.put(DEFAULT_CACHE, new DummyCacheProvider());
    	}
    	for (CacheProvider cacheProvider : cacheProviders.values()) {
            cacheProvider.init(settingsBean,this);
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

        for (CacheProvider provider : cacheProviders.values()) {
	        try {
	        	provider.shutdown();
	        } catch (Exception e) {
	        	// ignore
	        }
        }
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final CacheFactory INSTANCE = new CacheFactory();
    }

    /**
     * Return the unique instance of this class.
     *
     * @return the class' unique instance.
     */
    public static CacheFactory getInstance() {
        return Holder.INSTANCE;
    }

    public <K, V> Cache<K, V> getCache(String name, boolean forceCreation)
            throws JahiaInitializationException {
    	Cache<K, V> cache = getCache(name);
    	if (cache != null || !forceCreation) {
    		return cache;
    	}
        String provider = cacheProviderForCache.get(name);
        if (provider == null) {
            provider = DEFAULT_CACHE;
        }
        return createCacheInstance(name, provider);
    }
    
    protected synchronized <K, V> Cache<K, V> createCacheInstance (String name, String cacheProvider)
            throws JahiaInitializationException {
        // validity check
        if (name == null)
            return null;

        // When the cache already exists in the factory, return the instance.
        Cache<K, V> cache = getCache (name);
        if (cache != null) {
            return cache;
        }

        // instantiate the new cache, can throw an JahiaInitialization exception
        cache = new Cache(name, cacheProviders.get(cacheProvider).newCacheImplementation(name));

        logger.info("Created cache instance [{}]", name);

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


    public void flushAllCaches () {

        Iterator<String> cacheNames = getNames().iterator();
        while (cacheNames.hasNext ()) {
            String curCacheName = cacheNames.next ();
            Cache<?, ?> cache = caches.get (curCacheName);

            cache.flush();
        }

        logger.info("Flushed all caches.");
    }


    public Map<String, CacheProvider> getCacheProviders() {
        return cacheProviders;
    }

    public void setCacheProviders(Map<String, CacheProvider> cacheProviders) {
    	if (cacheProviders != null) {
    		this.cacheProviders.putAll(cacheProviders);
    	}
    }

    public Map<String, String> getCacheProviderForCache() {
        return cacheProviderForCache;
    }

    public void setCacheProviderForCache(Map<String, String> cacheProviderForCache) {
    	if (cacheProviderForCache != null) {
    		this.cacheProviderForCache.putAll(cacheProviderForCache);
    	}
    }
}
