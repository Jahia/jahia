/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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

    /** 
     * <p>Returns the requested cache or creates it if it does not exist yet and <code>forceCreation</code> is set to <code>true</code>.</p>
     * <p>When the name is <code>null</code> the cache creation is canceled and a
     * <code>null</code> instance will be returned.</p>
     *
     * @param name        the cache region
     * @param forceCreation do we need to create a new cache if it does not exist yet? 
     *
     * @return  the new cache instance
     *
     * @exception org.jahia.exceptions.JahiaInitializationException
     *      when the cache could not be initialized
     */
    public abstract <K,V> Cache<K, V> getCache(String name, boolean forceCreation)
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

    public abstract Map<String, CacheProvider> getCacheProviders();

    public abstract void setCacheProviders(Map<String, CacheProvider> cacheProviders);
}
