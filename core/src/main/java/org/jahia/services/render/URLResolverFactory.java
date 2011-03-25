package org.jahia.services.render;

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * Basic factory for URL resolver, we will optimize it later, as it makes no sense to create these objects all the
 * time for the same URLs.
 */
public class URLResolverFactory {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(URLResolverFactory.class);

    private Cache nodePathCache;
    private Cache siteInfoCache;

    private CacheService cacheService;
    private static final String NODE_PATH_CACHE = "urlResolverNodePath";
    private static final String SITE_INFO_CACHE = "urlResolverSiteInfo";
    private URLResolverListener urlResolverListener;

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
        try {
            nodePathCache = cacheService.getCache(NODE_PATH_CACHE, true);
            siteInfoCache = cacheService.getCache(SITE_INFO_CACHE, true);
        } catch (JahiaInitializationException jie) {
            logger.error("Error while creating URL resolver caches", jie);
        }
    }

    public void setUrlResolverListener(URLResolverListener urlResolverListener) {
        this.urlResolverListener = urlResolverListener;
        urlResolverListener.setUrlResolverFactory(this); // we wire this manually to avoid loops.
    }

    public URLResolver createURLResolver(String urlPathInfo, String serverName, HttpServletRequest request) {
        return new URLResolver(urlPathInfo, serverName, request, nodePathCache, siteInfoCache);
    }

    public URLResolver createURLResolver(String url, RenderContext context) {
        return new URLResolver(url, context, nodePathCache, siteInfoCache);
    }

    public void flushCaches() {
        nodePathCache.flush();
        siteInfoCache.flush();
    }
}
