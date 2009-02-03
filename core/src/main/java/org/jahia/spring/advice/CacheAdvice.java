/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
