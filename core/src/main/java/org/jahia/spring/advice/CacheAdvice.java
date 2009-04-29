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
import org.jahia.services.cache.GroupCacheKey;

import java.util.Set;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 20 juil. 2005
 * Time: 17:59:19
 * To change this template use File | Settings | File Templates.
 */
public class CacheAdvice implements MethodInterceptor {
    private static final transient Logger log = Logger.getLogger(CacheAdvice.class.getName());
    private String cacheName;
    public static final String ARGUMENTS_KEY = "arguments_";

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object returnValue = null;
        CacheService cacheService = ServicesRegistry.getInstance().getCacheService();
        Cache cache = cacheService.getCache(cacheName);
        if (cache == null) {
            cache = cacheService.createCacheInstance(cacheName);
        }
        GroupCacheKey entryKey = toGroupCacheKeyInternal(methodInvocation.getArguments());
        if (cache != null) {
            returnValue = cache.get(entryKey);
            if (log.isDebugEnabled()) {
                log.debug("Result of get from cache " + cacheName + " for entry key " + entryKey + " = " + returnValue + " in method " + methodInvocation.getMethod().getName());
            }
        }
        if (returnValue == null) {
            returnValue = methodInvocation.proceed();
            if (returnValue != null) {
                cache.put(entryKey, returnValue);
                if (log.isDebugEnabled()) {
                    log.debug("Put result in cache " + cacheName + " for entry key " + entryKey + " = " + returnValue + " in method " + methodInvocation.getMethod().getName());
                }
            }
        }
        return returnValue;
    }

    private GroupCacheKey toGroupCacheKeyInternal(Object[] array) {
        Set list = new HashSet(array.length);
        StringBuffer buffer = new StringBuffer(512);
        for (int i = 0; i < array.length; i++) {
            Object o = array[i];
            list.add(ARGUMENTS_KEY + i + o);
            buffer.append(o.toString());
        }
        return new GroupCacheKey(buffer.toString(),list);
    }

    public static GroupCacheKey toGroupCacheKey(Object[] array) {
        Set list = new HashSet(array.length);
        StringBuffer buffer = new StringBuffer(512);
        for (int i = array.length-1; i >=0 ; i--) {
            Object o = array[i];
            if(o!=null) {
                String s = o.toString();
                list.add(s);
                buffer.append(s);
            }
        }
        return new GroupCacheKey(buffer.toString(),list);
    }
}
