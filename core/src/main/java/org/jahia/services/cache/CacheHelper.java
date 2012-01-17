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

package org.jahia.services.cache;

import java.lang.management.ManagementFactory;
import java.util.Iterator;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.sampled.SampledEhcacheMBeans;

import org.hibernate.SessionFactory;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache manager utility.
 * 
 * @author Sergiy Shyrkov
 */
public final class CacheHelper {

    private static final Logger logger = LoggerFactory.getLogger(CacheHelper.class);

    /**
     * Returns <code>true</code> if the an MBean instance is registered for the Hibernate Ehcache Manager.
     * 
     * @return <code>true</code> if the an MBean instance is registered for the Hibernate Ehcache Manager
     */
    public static boolean canFlushHibernateCaches() {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName cacheManagerObjectName = SampledEhcacheMBeans.getCacheManagerObjectName(
                    null, "org.jahia.hibernate.ehcachemanager");
            return mBeanServer.isRegistered(cacheManagerObjectName);
        } catch (Exception e) {
            logger.warn("Unable to flush an instance of the Hibernate Ehcache manager MBean", e);
        }

        return false;
    }

    /**
     * Flushes all back-end and front-end Jahia caches, including Hibernate second level cache, Ehcaches etc. on the current cluster node
     * only.
     */
    public static void flushAllCaches() {
        flushAllCaches(false);
    }

    /**
     * Flushes all back-end and front-end Jahia caches, including Hibernate second level cache, Ehcaches etc. If
     * <code>propagateInCluster</code> is set to true also propagates the flush to other cluster nodes.
     * 
     * @param propagateInCluster
     *            if set to true the flush is propagated to other cluster nodes
     */
    public static void flushAllCaches(boolean propagateInCluster) {
        logger.info("Flushing all caches");
        CacheManager ehcacheManager = getEhcacheManager();
        CacheService cacheService = ServicesRegistry.getInstance().getCacheService();

        // legacy caches
        Iterator<String> cacheNames = cacheService.getNames().iterator();
        while (cacheNames.hasNext()) {
            String curCacheName = cacheNames.next();
            org.jahia.services.cache.Cache<Object, Object> cache = cacheService
                    .getCache(curCacheName);
            if (cache != null) {
                cache.flush(propagateInCluster);
            }
        }

        // Ehcaches
        for (String cacheName : ehcacheManager.getCacheNames()) {
            Cache cache = ehcacheManager.getCache(cacheName);
            if (cache != null) {
                // flush
                cache.removeAll(!propagateInCluster);
                // reset statistics
                cache.clearStatistics();
            }
        }

        // Hiberante caches
        flushHibernateCaches();

        logger.info("...done flushing all caches.");
    }

    /**
     * Flushes the specified Ehcache on the current cluster node only.
     * 
     * @param cacheName
     *            the name of the cache to flush
     */
    public static void flushEhcacheByName(String cacheName) {
        flushEhcacheByName(cacheName, false);
    }

    /**
     * Flushes the specified Ehcache. If <code>propagateInCluster</code> is set to true also propagates the flush to other cluster nodes.
     * 
     * @param cacheName
     *            the name of the cache to flush
     * @param propagateInCluster
     *            if set to true the flush is propagated to other cluster nodes
     */
    public static void flushEhcacheByName(String cacheName, boolean propagateInCluster) {
        logger.info("Flushing {}", cacheName);
        CacheManager ehcacheManager = getEhcacheManager();
        Cache cache = ehcacheManager.getCache(cacheName);
        if (cache != null) {
            // flush
            cache.removeAll(!propagateInCluster);
            // reset statistics
            cache.clearStatistics();
            logger.info("...done flushing {}", cacheName);
        } else {
            logger.warn("Cache with the name {} not found. Skip flushing.", cacheName);
        }
    }

    /**
     * Flushes Hibernate second level cache and propagates the flush to all cluster nodes.
     */
    public static void flushHibernateCaches() {
        logger.info("Flushing Hibernate second level caches");
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName cacheManagerObjectName = SampledEhcacheMBeans.getCacheManagerObjectName(
                    null, "org.jahia.hibernate.ehcachemanager");
            if (mBeanServer.isRegistered(cacheManagerObjectName)) {
                mBeanServer.invoke(cacheManagerObjectName, "clearAll", new Object[] {},
                        new String[] {});
                ((SessionFactory) SpringContextSingleton.getBean("sessionFactory")).getStatistics()
                        .clear();
                logger.info("...done flushing Hibernate second level caches");
            } else {
                logger.warn("Hibernate Ehcache manager MBean is not registered under the name {}",
                        cacheManagerObjectName);
            }
        } catch (Exception e) {
            logger.warn("Unable to flush an instance of the Hibernate Ehcache manager MBean", e);
        }
    }

    /**
     * Flushes front-end Jahia caches (module HTML output caches) on the current cluster node only.
     */
    public static void flushOutputCaches() {
        flushOutputCaches(false);
    }

    /**
     * Flushes front-end Jahia caches (module HTML output caches). If <code>propagateInCluster</code> is set to true also propagates the
     * flush to other cluster nodes.
     * 
     * @param propagateInCluster
     *            if set to true the flush is propagated to other cluster nodes
     */
    public static void flushOutputCaches(boolean propagateInCluster) {
        logger.info("Flushing HTML output caches");
        CacheManager ehcacheManager = getEhcacheManager();
        for (String cacheName : ehcacheManager.getCacheNames()) {
            if (!cacheName.startsWith("HTML")) {
                continue;
            }
            Cache cache = ehcacheManager.getCache(cacheName);
            if (cache != null) {
                // flush
                cache.removeAll(!propagateInCluster);
                // reset statistics
                cache.clearStatistics();
                logger.info("...done flushing {}", cacheName);
            }
        }
    }

    private static CacheManager getEhcacheManager() {
        return ((EhCacheProvider) SpringContextSingleton.getBean("ehCacheProvider"))
                .getCacheManager();
    }

}
