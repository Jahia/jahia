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
