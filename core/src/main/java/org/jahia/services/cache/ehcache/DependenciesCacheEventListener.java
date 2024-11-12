/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * This Listener is flushing HTMLCache entries upon eviction/expiration of entries on the HTML dependencies cache.
 *
 * @author cedric mailleux at jahia dot com
 * @since JAHIA 7.0.5
 */
public class DependenciesCacheEventListener extends CacheEventListenerAdapter {
    private static Logger logger = LoggerFactory.getLogger(DependenciesCacheEventListener.class);

    @Override
    /**
     * A dependency has been evicted, flush related entries.
     */
    public void notifyElementEvicted(Ehcache cache, Element element) {
        if (logger.isDebugEnabled()) {
            logger.debug("EHCache has evicted: " + element.getObjectKey() + " from cache " + cache.getName());
        }
        removeDependentElements(cache, element, false);
    }

    @Override
    /**
     * A dependency has been expired, flush related entries.
     */
    public void notifyElementExpired(Ehcache cache, Element element) {
        if (logger.isDebugEnabled()) {
            logger.debug("EHCache has expired: " + element.getObjectKey() + " from cache " + cache.getName());
        }
        removeDependentElements(cache, element, true);
    }

    private void removeDependentElements(Ehcache cache, Element element, boolean expired) {
        // Element is not present in the cache anymore
        ModuleCacheProvider cacheProvider = ModuleCacheProvider.getInstance();
        Cache htmlCache = cacheProvider.getCache();
        Cache dependenciesCache = cacheProvider.getDependenciesCache();
        String cacheName = cache.getName();
        if(cacheName.equals(dependenciesCache.getName()) || cacheName.equals(cacheProvider.getRegexpDependenciesCache().getName())) {
            // This is a dependency path that has been evicted
            @SuppressWarnings("unchecked")
            Set<String> deps = (Set<String>) element.getObjectValue();
            if (logger.isDebugEnabled()) {
                logger.debug("Evicting/Expiring "+deps.size()+" dependencies related to "+element.getObjectKey()+".");
            }
            if (deps.contains(DependenciesCacheEvictionPolicy.ALL)) {
                // do not propagate
                logger.warn("Due to the " + (expired ? "expiration" : "eviction") + " of a big entry in cache: '" + cacheName +
                        "', we are flushing the whole html cache and dependencies cache for key: " + element.getObjectKey());
                htmlCache.removeAll(true);
                dependenciesCache.removeAll(true);
            } else {
                invalidateDependencies(deps, htmlCache);
            }
        }
    }

    private void invalidateDependencies(Set<String> deps, Cache cache) {
        cache.removeAll(deps);
    }
}
