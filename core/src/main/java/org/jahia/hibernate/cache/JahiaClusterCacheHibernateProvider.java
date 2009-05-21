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
 package org.jahia.hibernate.cache;

import org.apache.log4j.Logger;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.clusterservice.ClusterCacheMessage;
import org.jahia.services.cache.reference.ReferenceCacheImpl;
import org.jahia.services.cluster.ClusterListener;
import org.jahia.services.cluster.ClusterMessage;
import org.jahia.services.cluster.JGroupsClusterService;
import org.jgroups.Address;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 4 mai 2006
 * Time: 10:22:05
 * To change this template use File | Settings | File Templates.
 */
public class JahiaClusterCacheHibernateProvider implements CacheProvider, ClusterListener {
    private JGroupsClusterService clusterService;
    private Map caches = new ConcurrentHashMap(120);
    private Logger logger = Logger.getLogger(JahiaClusterCacheHibernateProvider.class);
    private long freeMemoryLimit;
    private String cacheClusterUnderlyingImplementationName;
    private int cacheMaxGroups = 10000;

    /**
     * Configure the cache
     *
     * @param regionName the name of the cache region
     * @param properties configuration settings
     * @throws org.hibernate.cache.CacheException
     *
     */
    public Cache buildCache(String regionName, Properties properties) throws CacheException {
        if(caches.containsKey(regionName))
        return (Cache) caches.get(regionName);
    	CacheImplementation underlyingCacheImplementation = new ReferenceCacheImpl(regionName, cacheMaxGroups);
        JahiaHibernateCache jahiaHibernateCache = new JahiaHibernateCache(regionName, clusterService, underlyingCacheImplementation);
        jahiaHibernateCache.setCacheLimit(freeMemoryLimit);
        caches.put(regionName,jahiaHibernateCache);
        return jahiaHibernateCache;
    }

    /**
     * Generate a timestamp
     */
    public long nextTimestamp() {
        return System.currentTimeMillis()/100;
    }

    /**
     * Callback to perform any necessary initialization of the underlying cache implementation
     * during SessionFactory construction.
     *
     * @param properties current configuration settings.
     */
    public void start(Properties properties) throws CacheException {
        clusterService = new JGroupsClusterService();
        clusterService.setActivated(Boolean.valueOf((String) properties.get("activated")).booleanValue());
        clusterService.setChannelGroupName((String) properties.get("channelGroupName"));
        clusterService.setChannelProperties((String) properties.get("channelProperties"));
        clusterService.setServerId((String) properties.get("serverId"));
        try {
            clusterService.start();
        } catch (JahiaInitializationException e) {
            logger.error(e.getMessage(), e);
        }
        clusterService.addListener(this);
        freeMemoryLimit = new Long(((String) properties.get("freeMemoryLimit")).split("MB")[0]).longValue()*(1024*1024);
        cacheClusterUnderlyingImplementationName = (String) properties.get("cacheClusterUnderlyingImplementation");
        cacheMaxGroups = new Integer(((String) properties.get("cacheMaxGroups"))).intValue();
    }

    /**
     * Callback to perform any necessary cleanup of the underlying cache implementation
     * during SessionFactory.close().
     */
    public void stop() {
        try {
            clusterService.stop();
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean isMinimalPutsEnabledByDefault() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void messageReceived(ClusterMessage message) {
        if (message.getObject().getClass().getName().equals(ClusterCacheMessage.class.getName())) {
            ClusterCacheMessage clusterCacheMessage = (ClusterCacheMessage) message.getObject();
            JahiaHibernateCache cacheImpl = (JahiaHibernateCache) caches.get(clusterCacheMessage.getCacheName());
            if (cacheImpl == null) {
                logger.debug("Target cache " + clusterCacheMessage.getCacheName() + " not found, ignoring message" );
                return;
            }
            if (clusterCacheMessage.isFlush()) {
                cacheImpl.onFlush(clusterCacheMessage);
            } else if (clusterCacheMessage.isFlushGroup()) {
                cacheImpl.onFlushGroup(clusterCacheMessage);
            } else if (clusterCacheMessage.isInvalidateEntry()) {
                cacheImpl.onInvalidateEntry(clusterCacheMessage);
            } else if (clusterCacheMessage.isRemove()) {
                cacheImpl.onRemove(clusterCacheMessage);
            }
        }
    }

    public void memberJoined(Address address) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void memberLeft(Address address) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
