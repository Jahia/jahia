/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

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
