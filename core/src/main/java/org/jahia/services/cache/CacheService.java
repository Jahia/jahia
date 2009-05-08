/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.cache;

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;

import java.util.Set;
import java.util.Map;

/**
 * User: Serge Huber
 * Date: Jul 13, 2005
 * Time: 1:17:31 PM
 * Copyright (C) Jahia Inc.
 */
public abstract class CacheService extends JahiaService {

    /** <p>Creates a new instance of type <code>Cache</code>.</p>
     * <p>When the region is <code>null</code> the cache creation is canceled and a
     * <code>null</code> instance will be returned.</p>
     *
     * @param name        the cache region
     *
     * @return  the new cache instance
     *
     * @exception org.jahia.exceptions.JahiaInitializationException
     *      when the cache could not be initialized
     */
    public abstract <K,V> Cache<K, V> createCacheInstance (String name)
            throws JahiaInitializationException;

    public abstract <K,V> Cache<K, V> getCache(String name);

    /** <p>Returns an iterator of all the cache names.</p>
     *
     * @return an iterator of all the cache names.
     */
    public abstract Set<String> getNames ();

    /** <p>Flush all the cache entries of all the registered caches.</p>
     *
     * <p>Use this method with caution as it may take a lot of CPU time, because
     * the method is synchronized and each accessed cache has to be synchronized too.</p>
     */
    public abstract void flushAllCaches ();

    /** <p>Checks if the cache synchronization is enabled.</p>
     *
     * @return  <code>true</code> when the cache synchronization is enabled
     */
    public abstract boolean isClusterCache();

    public abstract boolean isJMXEnabled();

    public abstract void setJMXEnabled(boolean JMXEnabled);

    public abstract void enableClusterSync() throws JahiaInitializationException;

    public abstract void stopClusterSync();

    public abstract void syncClusterNow();

    /**
     * <p>Retrieves the Container HTML cache instance.</p>
     *
     * <p>When the Container HTML cache is not present, a new instance is created
     * and inserted into the factory.<p>
     *
     * @return  the Container HTML cache instance
     *
     * @exception org.jahia.exceptions.JahiaInitializationException
     *      when the Container HTML cache could not be instanciated and properly initialized
     */
    public abstract ContainerHTMLCache<GroupCacheKey, ContainerHTMLCacheEntry> getContainerHTMLCacheInstance() throws JahiaInitializationException;

    public abstract SkeletonCache<GroupCacheKey, SkeletonCacheEntry> getSkeletonCacheInstance() throws JahiaInitializationException;
    public abstract Map<String, CacheProvider> getCacheProviders();

    public abstract void setCacheProviders(Map<String, CacheProvider> cacheProviders);
}
