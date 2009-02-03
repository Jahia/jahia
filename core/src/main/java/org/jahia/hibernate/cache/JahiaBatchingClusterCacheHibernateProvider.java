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

 package org.jahia.hibernate.cache;

import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.jahia.services.cluster.ClusterListener;
import org.jahia.services.cluster.JGroupsClusterService;
import org.jahia.services.cluster.ClusterMessage;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.clusterservice.ClusterCacheMessage;
import org.jahia.services.cache.clusterservice.batch.ClusterMessageCacheBatch;
import org.jahia.services.cache.clusterservice.batch.ClusterCacheMessageBatcher;
import org.jahia.services.cache.reference.ReferenceCacheImpl;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaException;
import org.apache.log4j.Logger;
import org.jgroups.Address;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 4 mai 2006
 * Time: 10:22:05
 * To change this template use File | Settings | File Templates.
 */
public class JahiaBatchingClusterCacheHibernateProvider implements CacheProvider, ClusterListener, ClusterCacheMessageBatcher {
    private JGroupsClusterService clusterService;
    private Map caches = new ConcurrentHashMap(120);
    private Logger logger = Logger.getLogger(JahiaBatchingClusterCacheHibernateProvider.class);
    private long freeMemoryLimit;
    private int maxBatchSize = 100000;
    ThreadLocal threadClusterBatch = new ThreadLocal();
    private boolean clusterActivated = false;
    private String cacheClusterUnderlyingImplementationName;
    private int cacheMaxGroups = 20000;

    static private Set providerInstances = new HashSet();

    public JahiaBatchingClusterCacheHibernateProvider() {
        providerInstances.add(this);
    }

    public static void syncClusterNow() {
        Iterator providerInstanceIter = providerInstances.iterator();
        while (providerInstanceIter.hasNext()) {
            JahiaBatchingClusterCacheHibernateProvider curInstance = (JahiaBatchingClusterCacheHibernateProvider) providerInstanceIter.next();
            curInstance.sendBatch();
        }
    }

    public static void flushAllCaches() {
        Iterator providerInstanceIter = providerInstances.iterator();
        while (providerInstanceIter.hasNext()) {
            JahiaBatchingClusterCacheHibernateProvider curInstance = (JahiaBatchingClusterCacheHibernateProvider) providerInstanceIter.next();
            curInstance.flushCaches();
        }
    }

    /**
     * Configure the cache
     *
     * @param regionName the name of the cache region
     * @param properties configuration settings
     * @throws org.hibernate.cache.CacheException
     *
     */
    public Cache buildCache(String regionName, Properties properties) throws CacheException {
        if (caches.containsKey(regionName)) {
            return (Cache) caches.get(regionName);
        }
    	CacheImplementation underlyingCacheImplementation;
    	underlyingCacheImplementation = new ReferenceCacheImpl(regionName, cacheMaxGroups);
        JahiaBatchingHibernateCache jahiaBatchingHibernateCache = new JahiaBatchingHibernateCache(regionName, this, underlyingCacheImplementation);
        jahiaBatchingHibernateCache.setCacheLimit(freeMemoryLimit);
        caches.put(regionName,jahiaBatchingHibernateCache);
        return jahiaBatchingHibernateCache;
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
        clusterActivated = Boolean.valueOf((String) properties.get("activated")).booleanValue();
        if (clusterActivated) {
            clusterService = new JGroupsClusterService();
            clusterService.setActivated(Boolean.valueOf((String) properties.get("activated")).booleanValue());
            clusterService.setChannelGroupName((String) properties.get("channelGroupName"));
            clusterService.setChannelProperties((String) properties.get("channelProperties"));
            clusterService.setServerId((String) properties.get("serverId"));
            try {
                clusterService.start();
            } catch (JahiaInitializationException e) {
                logger.error("Error while initializing cluster service", e);
            }
            clusterService.addListener(this);
        }
        freeMemoryLimit = new Long(((String) properties.get("freeMemoryLimit")).split("MB")[0]).longValue()*(1024*1024);
        cacheClusterUnderlyingImplementationName = (String) properties.get("cacheClusterUnderlyingImplementation");
        cacheMaxGroups = new Integer(((String) properties.get("cacheMaxGroups"))).intValue();        
    }

    /**
     * Callback to perform any necessary cleanup of the underlying cache implementation
     * during SessionFactory.close().
     */
    public void stop() {
        if (clusterActivated) {
            try {
                clusterService.stop();
            } catch (JahiaException e) {
                logger.error("Error while shutting down cluster service", e);
            }
        }
    }

    public boolean isMinimalPutsEnabledByDefault() {
        return true;  
    }

    public void messageReceived(ClusterMessage message) {
        if (message.getObject().getClass().getName().equals(ClusterMessageCacheBatch.class.getName())) {
            ClusterMessageCacheBatch clusterMessageCacheBatch = (ClusterMessageCacheBatch) message.getObject();
            logger.debug("Received cache batch invalidation cluster message of size " + clusterMessageCacheBatch.size());
            Iterator cacheMessageIter = clusterMessageCacheBatch.iterator();
            while (cacheMessageIter.hasNext()) {
                ClusterCacheMessage clusterCacheMessage = (ClusterCacheMessage) cacheMessageIter.next();
                JahiaBatchingHibernateCache cacheImpl = (JahiaBatchingHibernateCache) caches.get(clusterCacheMessage.getCacheName());
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
    }

    public void memberJoined(Address address) {
        logger.info("member joined "+address);
    }

    public void memberLeft(Address address) {
        logger.info("member left "+address);
    }

    public void addMessageToBatch(ClusterCacheMessage clusterCacheMessage) {
        ClusterMessageCacheBatch cacheBatch = (ClusterMessageCacheBatch) threadClusterBatch.get();
        if (cacheBatch == null) {
            cacheBatch = new ClusterMessageCacheBatch();
        }
        if (!clusterActivated) {
            threadClusterBatch.set(cacheBatch);
            return;
        }
        cacheBatch.add(clusterCacheMessage);
        threadClusterBatch.set(cacheBatch);
        if (cacheBatch.size() > maxBatchSize) {
            if (logger.isDebugEnabled()) {
                logger.debug("Maximum batch size reached (" + maxBatchSize + "), sending cluster messages now...");
                syncClusterNow();
            }
        }
    }

    public void sendBatch() {
        ClusterMessageCacheBatch cacheBatch = (ClusterMessageCacheBatch) threadClusterBatch.get();
        if (cacheBatch == null) {
            cacheBatch = new ClusterMessageCacheBatch();
        }
        if (!cacheBatch.isEmpty()) {
            if (clusterActivated) {
                logger.debug("Sending cache invalidation batch cluster message of size " + cacheBatch.size());
                clusterService.sendMessage(new ClusterMessage(cacheBatch));
            }
            cacheBatch.clear();
        }
        threadClusterBatch.set(cacheBatch);
    }

    public void flushCaches() {
        Iterator cacheIter = caches.values().iterator();
        while (cacheIter.hasNext()) {
            JahiaBatchingHibernateCache curCache = (JahiaBatchingHibernateCache) cacheIter.next();
            curCache.flushAll(false);
        }
    }
}
