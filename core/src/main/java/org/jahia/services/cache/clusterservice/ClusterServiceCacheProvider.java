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
package org.jahia.services.cache.clusterservice;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.CacheProvider;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.reference.ReferenceCacheImpl;
import org.jahia.services.cluster.ClusterService;
import org.jahia.services.cluster.ClusterListener;
import org.jahia.services.cluster.ClusterMessage;
import org.jahia.settings.SettingsBean;
import org.jahia.exceptions.JahiaInitializationException;
import org.jgroups.Address;

/**
 * User: Serge Huber
 * Date: Jul 26, 2005
 * Time: 6:44:46 PM
 * Copyright (C) Jahia Inc.
 */
public class ClusterServiceCacheProvider implements CacheProvider, ClusterListener {

    final private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ClusterServiceCacheProvider.class);

    private ClusterService clusterService;
    private CacheService cacheService;
    private String cacheClusterUnderlyingImplementationName;
    private int cacheMaxGroups = 20000;

    public ClusterServiceCacheProvider() {

    }

    public void init(SettingsBean settingsBean, CacheService cacheService) throws JahiaInitializationException {
        clusterService.addListener(this);
        this.cacheService = cacheService;
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
    }

    public boolean isClusterCache() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public CacheImplementation newCacheImplementation(String name) {
    	CacheImplementation underlyingCacheImplementation;
        underlyingCacheImplementation = new ReferenceCacheImpl(name, cacheMaxGroups);
        return new ClusterServiceCacheImpl(name, clusterService, underlyingCacheImplementation);
    }

    public ClusterService getClusterService() {
        return clusterService;
    }

    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    public void messageReceived(ClusterMessage message) {
        if (message.getObject().getClass().getName().equals(
                ClusterCacheMessage.class.getName())) {
            ClusterCacheMessage clusterCacheMessage = (ClusterCacheMessage) message
                    .getObject();
            String cacheName = clusterCacheMessage.getCacheName();
            if (cacheName != null) {
                Cache targetCache = cacheService.getCache(cacheName);
                if (targetCache == null) {
                    logger.debug("Target cache "
                            + cacheName
                            + " not found, ignoring message");
                    return;
                }
                ClusterServiceCacheImpl cacheImpl = (ClusterServiceCacheImpl) targetCache
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
                    logger.error(e);
                }
            } else if (clusterCacheMessage.isKeyGeneratorAclUpdate()) {
                try {
                    ServicesRegistry.getInstance()
                            .getCacheKeyGeneratorService().rightsUpdated();
                } catch (JahiaInitializationException e) {
                    logger.error(e);
                }
            }
        }
    }

    public void memberJoined(Address address) {
    }

    public void memberLeft(Address address) {
    }
}
