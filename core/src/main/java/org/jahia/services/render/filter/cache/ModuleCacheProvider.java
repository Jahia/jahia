/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render.filter.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.constructs.blocking.BlockingCache;

import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.springframework.beans.factory.InitializingBean;

/**
 * Instantiates and provides access to the module output and dependency caches.
 * 
 * @author Sergiy Shyrkov
 * 
 */
public class ModuleCacheProvider implements InitializingBean {

    public static final String CACHE_NAME = "CJHTMLCache";
    public static final String DEPS_CACHE_NAME = CACHE_NAME + "dependencies";
    
    private BlockingCache blockingCache;
    private int blockingTimeout = 5000;
    private EhCacheProvider cacheProvider;
    private Cache dependenciesCache;

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        CacheManager cacheManager = cacheProvider.getCacheManager();
        if (!cacheManager.cacheExists(CACHE_NAME)) {
            cacheManager.addCache(CACHE_NAME);
        }
        if (!cacheManager.cacheExists(DEPS_CACHE_NAME)) {
            cacheManager.addCache(DEPS_CACHE_NAME);
        }
        blockingCache = new BlockingCache(cacheManager.getCache(CACHE_NAME));
        blockingCache.setTimeoutMillis(blockingTimeout);
        dependenciesCache = cacheManager.getCache(DEPS_CACHE_NAME);
    }

    public BlockingCache getCache() {
        return blockingCache;
    }

    public Cache getDependenciesCache() {
        return dependenciesCache;
    }

    public void setBlockingTimeout(int blockingTimeout) {
        this.blockingTimeout = blockingTimeout;
    }

    public void setCacheProvider(EhCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

}
