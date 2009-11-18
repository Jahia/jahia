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

    public abstract void enableClusterSync() throws JahiaInitializationException;

    public abstract void stopClusterSync();

    public abstract void syncClusterNow();

    public abstract SkeletonCache<GroupCacheKey, SkeletonCacheEntry> getSkeletonCacheInstance() throws JahiaInitializationException;
    public abstract Map<String, CacheProvider> getCacheProviders();

    public abstract void setCacheProviders(Map<String, CacheProvider> cacheProviders);
}
