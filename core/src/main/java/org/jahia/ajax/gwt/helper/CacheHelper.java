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

package org.jahia.ajax.gwt.helper;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.slf4j.Logger;
import org.jahia.services.cache.CacheFactory;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.List;

/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 16 sept. 2010
 */
public class CacheHelper implements ApplicationListener<ApplicationEvent> {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(CacheHelper.class);
    private ModuleCacheProvider cacheProvider;
    private CacheFactory cacheFactory;
    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public void setCacheFactory(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
    }

    public void flush(String path, boolean flushSubtree) {
        if (logger.isDebugEnabled()) {
            logger.debug("Flushing dependencies for path : " + path);
        }
        Cache cache = cacheProvider.getDependenciesCache();
        Element element = cache.get(path);
        if (element != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Flushing path : " + path);
            }
            cacheProvider.invalidate(path);
            cache.remove(element.getKey());
        }
        if (flushSubtree) {
            List keys = cache.getKeys();
            for (Object key : keys) {
                if (key.toString().startsWith(path)) {
                    cacheProvider.invalidate(key.toString());
                    cache.remove(key);
                }
            }
        }
    }

    public void flushAll() {
        if (logger.isDebugEnabled()) {
            logger.debug("Flushing all caches");
        }
        cacheProvider.flushCaches();
        cacheFactory.flushAllCaches();
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof JahiaTemplateManagerService.ModuleDeployedOnSiteEvent) {
            flush(((JahiaTemplateManagerService.ModuleDeployedOnSiteEvent)event).getTargetSitePath(), true);
        }
    }
}
