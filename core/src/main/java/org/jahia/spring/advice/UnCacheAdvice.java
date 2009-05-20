/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
