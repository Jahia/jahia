/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
