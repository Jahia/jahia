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
package org.jahia.modules.serverSettings.flow;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.CacheHelper;
import org.jahia.services.cache.ehcache.EhCacheProvider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 08/03/13
 */
public class CacheFlushFlowHandler implements Serializable {
    private transient static Logger logger = Logger.getLogger(CacheFlushFlowHandler.class);

    public void flushAllCaches() {
        logger.info("Flushing all caches");
        CacheHelper.flushAllCaches();
    }

    public void flushOutputCaches() {
        logger.info("Flushing output caches");
        CacheHelper.flushOutputCaches();
    }

    public void flushHibernateCaches() {
        logger.info("Flushing hibernate caches");
        CacheHelper.flushHibernateCaches();
    }

    public void flushCache(String cacheName) {
        logger.info("Flushing cache " + cacheName);
        CacheHelper.flushEhcacheByName(cacheName);
    }

    public void toggleHibernateStats() {
        SessionFactory factory = (SessionFactory) SpringContextSingleton.getBean("sessionFactory");
        Statistics statistics = factory.getStatistics();
        statistics.setStatisticsEnabled(!statistics.isStatisticsEnabled());
    }

    public void enableAllEHCacheStats() {
        setAllEHCacheStats(true);
    }

    public void disableAllEHCacheStats() {
        setAllEHCacheStats(false);
    }

    private void setAllEHCacheStats(boolean status) {
        CacheManager ehcacheManager = ((EhCacheProvider) SpringContextSingleton.getBean(
                "ehCacheProvider")).getCacheManager();
        for (String cacheName : ehcacheManager.getCacheNames()) {
            ehcacheManager.getCache(cacheName).setStatisticsEnabled(status);
        }
    }

    public void enableEHCacheStats(String cacheName) {
        setEHCacheStatistics(cacheName, true);
    }

    public void disableEHCacheStats(String cacheName) {
        setEHCacheStatistics(cacheName, false);
    }

    private void setEHCacheStatistics(String cacheName, boolean status) {
        CacheManager ehcacheManager = ((EhCacheProvider) SpringContextSingleton.getBean(
                "ehCacheProvider")).getCacheManager();
        ehcacheManager.getCache(cacheName).setStatisticsEnabled(status);
    }

    public List<Cache> getEhCaches() {
        List<Cache> caches = new ArrayList<Cache>();
        CacheManager ehcacheManager = ((EhCacheProvider) SpringContextSingleton.getBean(
                "ehCacheProvider")).getCacheManager();
        for (String cacheName : ehcacheManager.getCacheNames()) {
            caches.add(ehcacheManager.getCache(cacheName));
        }
        Collections.sort(caches, new Comparator<Cache>() {
            @Override
            public int compare(Cache o1, Cache o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return caches;
    }

    public Statistics getHibernateStats() {
        SessionFactory factory = (SessionFactory) SpringContextSingleton.getBean("sessionFactory");
        return factory.getStatistics();
    }

    public List<SecondLevelCacheStatistics> getSecondLevelCacheStats() {
        Statistics hibernateStats = getHibernateStats();
        if(hibernateStats.isStatisticsEnabled()) {
            List<SecondLevelCacheStatistics> stats = new ArrayList<SecondLevelCacheStatistics>();
            for (String regionName : hibernateStats.getSecondLevelCacheRegionNames()) {
                stats.add(hibernateStats.getSecondLevelCacheStatistics(regionName));
            }
            return stats;
        } else {
            return Collections.emptyList();
        }
    }

    public List<EntityStatistics> getEntityStats() {
        Statistics hibernateStats = getHibernateStats();
        if(hibernateStats.isStatisticsEnabled()) {
            List<EntityStatistics> stats = new ArrayList<EntityStatistics>();
            for (String regionName : hibernateStats.getEntityNames()) {
                stats.add(hibernateStats.getEntityStatistics(regionName));
            }
            return stats;
        } else {
            return Collections.emptyList();
        }
    }
}
