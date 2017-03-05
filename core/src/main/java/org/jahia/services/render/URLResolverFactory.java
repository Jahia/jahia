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
package org.jahia.services.render;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.expression.Criteria;

import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic factory for URL resolver, we will optimize it later, as it makes no sense to create these objects all the
 * time for the same URLs.
 */
public class URLResolverFactory {

    private static Logger logger = LoggerFactory.getLogger(URLResolverFactory.class);

    private Ehcache nodePathCache;
    private Ehcache siteInfoCache;

    private static final String NODE_PATH_CACHE = "urlResolverNodePath";
    private static final String SITE_INFO_CACHE = "urlResolverSiteInfo";

    public void setCacheService(EhCacheProvider cacheService) {
        nodePathCache = cacheService.getCacheManager().addCacheIfAbsent(NODE_PATH_CACHE);
        siteInfoCache = cacheService.getCacheManager().addCacheIfAbsent(SITE_INFO_CACHE);
    }

    public void setUrlResolverListener(URLResolverListener urlResolverListener) {
        urlResolverListener.setUrlResolverFactory(this); // we wire this manually to avoid loops.
    }

    public URLResolver createURLResolver(String urlPathInfo, String serverName, String workspace, HttpServletRequest request) {
        return new URLResolver(urlPathInfo, serverName, workspace, request, nodePathCache, siteInfoCache);
    }

    public URLResolver createURLResolver(String urlPathInfo, String serverName, HttpServletRequest request) {
        return new URLResolver(urlPathInfo, serverName, request, nodePathCache, siteInfoCache);
    }

    public URLResolver createURLResolver(String url, RenderContext context) {
        return new URLResolver(url, context, nodePathCache, siteInfoCache);
    }

    @SuppressWarnings("unchecked")
    public synchronized void flushCaches(String path) {
        flushCaches(Query.VALUE.eq(path));
    }

    @SuppressWarnings("unchecked")
    public synchronized void flushCaches(Collection<String> paths) {
        flushCaches(Query.VALUE.in(paths));
    }

    private void flushCaches(Criteria keySearchCriteria) {
        final List<Result> all = nodePathCache.createQuery().includeKeys().addCriteria(keySearchCriteria).execute().all();
        if (all.isEmpty()) {
            return;
        }
        List<Object> keys = new LinkedList<Object>();
        for (Result result : all) {
            keys.add(result.getKey());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Flushing {} keys from URLResolver caches: {}", keys.size(), keys.toArray());
        }
        nodePathCache.removeAll(keys);
        siteInfoCache.removeAll(keys);
    }
}
