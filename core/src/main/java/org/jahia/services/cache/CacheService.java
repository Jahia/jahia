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
