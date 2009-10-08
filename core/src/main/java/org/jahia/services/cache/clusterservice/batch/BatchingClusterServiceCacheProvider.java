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
package org.jahia.services.cache.clusterservice.batch;

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.CacheProvider;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.clusterservice.ClusterCacheMessage;
import org.jahia.services.cache.reference.ReferenceCacheImpl;
import org.jahia.services.cluster.ClusterListener;
import org.jahia.services.cluster.ClusterMessage;
import org.jahia.services.cluster.ClusterService;
import org.jahia.settings.SettingsBean;
import org.jgroups.Address;

import java.util.Iterator;

/**
 * User: Serge Huber
 * Date: 8 mai 2006
 * Time: 11:46:12
 * Copyright (C) Jahia Inc.
 */
public class BatchingClusterServiceCacheProvider implements CacheProvider, ClusterListener, ClusterCacheMessageBatcher {

    final private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (BatchingClusterServiceCacheProvider.class);

    private ClusterService clusterService;
    private CacheService cacheService;
    private int maxBatchSize = 100000;
    private String cacheClusterUnderlyingImplementationName;
    ThreadLocal<ClusterMessageCacheBatch> threadClusterBatch = new ThreadLocal<ClusterMessageCacheBatch>();
    private int cacheMaxGroups = 10000;

    public BatchingClusterServiceCacheProvider() {
    }

    public void init(SettingsBean settingsBean, CacheService cacheService) throws JahiaInitializationException {
        clusterService.addListener(this);
        this.cacheService = cacheService;
        maxBatchSize = settingsBean.getClusterCacheMaxBatchSize();
        this.cacheClusterUnderlyingImplementationName = settingsBean.getCacheClusterUnderlyingImplementation();
        this.cacheMaxGroups = settingsBean.getCacheMaxGroups();
    }

    public void shutdown() {
        clusterService.removeListener(this);
    }

    public void enableClusterSync() throws JahiaInitializationException {
    }

    public void stopClusterSync() {
    }

    public void syncClusterNow() {
        ClusterMessageCacheBatch cacheBatch = (ClusterMessageCacheBatch) threadClusterBatch.get();
        if (cacheBatch == null) {
            cacheBatch = new ClusterMessageCacheBatch();
        }
        if (!cacheBatch.isEmpty()) {
            if (clusterService.isActivated()) {
                logger.debug("Sending cache invalidation batch cluster message of size " + cacheBatch.size());
                if(logger.isDebugEnabled()) {
                    for (Object o : cacheBatch) {
                        logger.debug("Invalidation message "+o.toString());
                    }
                }
                clusterService.sendMessage(new ClusterMessage(cacheBatch));
            }
            cacheBatch.clear();
        }
        threadClusterBatch.set(cacheBatch);
    }

    public boolean isClusterCache() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public CacheImplementation newCacheImplementation(String name) {
    	CacheImplementation underlyingCacheImplementation;
    	underlyingCacheImplementation = new ReferenceCacheImpl(name, cacheMaxGroups);
        return new BatchingClusterServiceCacheImpl(name, this, underlyingCacheImplementation);
    }

    public ClusterService getClusterService() {
        return clusterService;
    }

    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    public void messageReceived(ClusterMessage message) {
        if (message.getObject().getClass().getName().equals(
                ClusterMessageCacheBatch.class.getName())) {
            ClusterMessageCacheBatch clusterMessageCacheBatch = (ClusterMessageCacheBatch) message
                    .getObject();
            logger.debug("Received cache batch invalidation cluster message of size "
                            + clusterMessageCacheBatch.size());
            Iterator cacheMessageIter = clusterMessageCacheBatch.iterator();
            while (cacheMessageIter.hasNext()) {
                ClusterCacheMessage clusterCacheMessage = (ClusterCacheMessage) cacheMessageIter
                        .next();
                String cacheName = clusterCacheMessage.getCacheName();
                if (cacheName != null) {
                    Cache targetCache = cacheService.getCache(cacheName);
                    if (targetCache == null) {
                        logger.debug("Target cache " + cacheName
                                + " not found, ignoring message");
                        continue;
                    }
                    BatchingClusterServiceCacheImpl cacheImpl = (BatchingClusterServiceCacheImpl) targetCache
                            .getCacheImplementation();
                    if (clusterCacheMessage.isFlush()) {
                        cacheImpl.onFlush(clusterCacheMessage);
                    } else if (clusterCacheMessage.isFlushGroup()) {
                        cacheImpl.onFlushGroup(clusterCacheMessage);
                    } else if (clusterCacheMessage.isInvalidateEntry()) {
                        cacheImpl.onInvalidateEntry(clusterCacheMessage);
                    } else if (clusterCacheMessage.isRemove()) {
                        cacheImpl.onRemove(clusterCacheMessage);
                    }
                } else if (clusterCacheMessage.isKeyGeneratorRestart()) {
                    try {
                        ServicesRegistry.getInstance()
                                .getCacheKeyGeneratorService().start();
                    } catch (JahiaInitializationException e) {
                        logger.error("Error starting cache key generator service", e);
                    }
                } else if (clusterCacheMessage.isKeyGeneratorAclUpdate()) {
                    try {
                        ServicesRegistry.getInstance()
                                .getCacheKeyGeneratorService().rightsUpdated();
                    } catch (JahiaInitializationException e) {
                        logger.error("Error updating rights in cache key generator service", e);
                    }
                }
            }
        } else if (message.getObject().getClass().getName().equals(
                ClusterCacheMessage.class.getName())) {
            ClusterCacheMessage clusterCacheMessage = (ClusterCacheMessage) message
                    .getObject();
            String cacheName = clusterCacheMessage.getCacheName();
            if (cacheName == null) {
                if (clusterCacheMessage.isKeyGeneratorRestart()) {
                    try {
                        ServicesRegistry.getInstance()
                                .getCacheKeyGeneratorService().start();
                    } catch (JahiaInitializationException e) {
                        logger.error("Error starting cache key generator service", e);
                    }
                } else if (clusterCacheMessage.isKeyGeneratorAclUpdate()) {
                    try {
                        ServicesRegistry.getInstance()
                                .getCacheKeyGeneratorService().rightsUpdated();
                    } catch (JahiaInitializationException e) {
                        logger.error("Error updating rights in cache key generator service", e);
                    }
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

        if (!clusterService.isActivated()) {
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
}
