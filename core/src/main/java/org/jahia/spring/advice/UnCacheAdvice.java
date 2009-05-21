/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 package org.jahia.spring.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 20 juil. 2005
 * Time: 17:59:19
 * To change this template use File | Settings | File Templates.
 */
public class UnCacheAdvice implements MethodInterceptor {
    private static final transient Logger log = Logger.getLogger(UnCacheAdvice.class);
    private String[] cacheNames;
    private String cacheName;

    public void setCacheNames(String[] cacheNames) {
        this.cacheNames = cacheNames;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object returnValue = null;
        returnValue = methodInvocation.proceed();
        CacheService cacheService = ServicesRegistry.getInstance().getCacheService();
        Object[] args = methodInvocation.getArguments();
        if (cacheNames != null && cacheNames.length > 0) {
            for (int i = 0; i < cacheNames.length; i++) {
                String name = cacheNames[i];
                Cache cache = cacheService.getCache(name);
                flushCache(args, cache);
            }
        } else if (cacheName != null) {
            Cache cache = cacheService.getCache(cacheName);
            flushCache(args, cache);
        }
        return returnValue;
    }

    private void flushCache(Object[] args, Cache cache) {
        if (cache != null) {
            for (int j = 0; j < args.length; j++) {
                Object arg = args[j];
                if (log.isDebugEnabled()) {
                    log.debug("Try to flush group " + CacheAdvice.ARGUMENTS_KEY + j + arg + " in cache " + cache);
                }
                cache.flushGroup(CacheAdvice.ARGUMENTS_KEY + j + arg);
            }
        }
    }
}
