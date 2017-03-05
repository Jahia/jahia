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
        removeDependentElements(cache, element);
    }

    @Override
    /**
     * A dependency has been expired, flush related entries.
     */
    public void notifyElementExpired(Ehcache cache, Element element) {
        if (logger.isDebugEnabled()) {
            logger.debug("EHCache has expired: " + element.getObjectKey() + " from cache " + cache.getName());
        }
        removeDependentElements(cache, element);
    }

    private void removeDependentElements(Ehcache cache, Element element) {
        // Element is not present in the cache anymore
        ModuleCacheProvider cacheProvider = ModuleCacheProvider.getInstance();
        Cache htmlCache = cacheProvider.getCache();
        String cacheName = cache.getName();
        if(cacheName.equals(cacheProvider.getDependenciesCache().getName()) || cacheName.equals(cacheProvider.getRegexpDependenciesCache().getName())) {
            // This is a dependency path that has been evicted
            @SuppressWarnings("unchecked")
            Set<String> deps = (Set<String>) element.getObjectValue();
            if (logger.isDebugEnabled()) {
                logger.debug("Evicting/Expiring "+deps.size()+" dependencies related to "+element.getObjectKey()+".");
            }
            if (deps.contains(DependenciesCacheEvictionPolicy.ALL)) {
                // do not propagate
                htmlCache.removeAll(true);
            } else {
                invalidateDependencies(deps, htmlCache);
            }
        }
    }

    private void invalidateDependencies(Set<String> deps, Cache cache) {
        cache.removeAll(deps);
    }
}
