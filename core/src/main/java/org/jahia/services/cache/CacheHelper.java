/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.management.ManagementService;
import net.sf.ehcache.statistics.StatisticsGateway;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.ehcache.CacheInfo;
import org.jahia.services.cache.ehcache.CacheManagerInfo;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.impl.jackrabbit.SpringJackrabbitRepository;
import org.jahia.services.render.filter.cache.CacheClusterEvent;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.jahia.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * Cache manager utility.
 *
 * @author Sergiy Shyrkov
 */
public final class CacheHelper {

    public static final String CMD_FLUSH_ALL_CACHES = "FLUSH_ALL_CACHES";
    public static final String CMD_FLUSH_CHILDREN = "FLUSH_CHILDREN";
    /**
     * @deprecated in favour of {@link #CMD_FLUSH_CHILDREN}
     */
    @Deprecated
    public static final String CMD_FLUSH_CHILDS = "FLUSH_CHILDS";
    public static final String CMD_FLUSH_MATCHINGPERMISSIONS = "FLUSH_MATCHINGPERMISSIONS";
    public static final String CMD_FLUSH_OUTPUT_CACHES = "FLUSH_OUTPUT_CACHES";
    public static final String CMD_FLUSH_PATH = "FLUSH_PATH";
    public static final String CMD_FLUSH_REGEXP = "FLUSH_REGEXP";
    public static final String CMD_FLUSH_REGEXPDEP = "FLUSH_REGEXPDEP";
    public static final String CMD_FLUSH_URLRESOLVER = "FLUSH_URLRESOLVER";
    public static final String CMD_FLUSH_VANITYURL = "FLUSH_VANITYURL";

    private static final Logger logger = LoggerFactory.getLogger(CacheHelper.class);

    /**
     * Flushes all back-end and front-end Jahia caches on the current cluster node
     * only.
     */
    public static void flushAllCaches() {
        flushAllCaches(false);
    }

    /**
     * Flushes all back-end and front-end Jahia caches. If
     * <code>propagateInCluster</code> is set to true also propagates the flush to other cluster nodes.
     *
     * @param propagateInCluster if set to true the flush is propagated to other cluster nodes
     */
    public static void flushAllCaches(boolean propagateInCluster) {
        logger.info("Flushing all caches{}",
                propagateInCluster ? " also propagating it to all cluster members" : "");

        CacheService cacheService = ServicesRegistry.getInstance().getCacheService();

        // legacy caches
        for (String curCacheName : cacheService.getNames()) {
            org.jahia.services.cache.Cache<Object, Object> cache = cacheService
                    .getCache(curCacheName);
            if (cache != null) {
                cache.flush(propagateInCluster);
            }
        }

        // Ehcaches
        for (CacheManager mgr : CacheManager.ALL_CACHE_MANAGERS) {
            for (String cacheName : mgr.getCacheNames()) {
                Ehcache cache = mgr.getEhcache(cacheName);
                if (cache != null) {
                    // flush
                    cache.removeAll(!propagateInCluster);
                }
            }
        }

        if (propagateInCluster) {
            sendCacheFlushCommandToClusterImmediately(CMD_FLUSH_ALL_CACHES);
        }

        logger.info("...done flushing all caches.");
    }

    /**
     * Flushes the caches of the specified cache manager.
     *
     * @param cacheManagerName   the cache manager name to flush caches for
     * @param propagateInCluster if set to true the flush is propagated to other cluster nodes
     */
    public static void flushCachesForManager(String cacheManagerName, boolean propagateInCluster) {
        logger.info("Flushing caches for the cache manager '{}' {}", cacheManagerName, propagateInCluster ? " also propagating it to all cluster members" : "");
        CacheManager ehcacheManager = getCacheManager(cacheManagerName);
        if (ehcacheManager == null) {
            return;
        }
        for (String cacheName : ehcacheManager.getCacheNames()) {
            Ehcache cache = ehcacheManager.getEhcache(cacheName);
            if (cache != null) {
                // flush
                cache.removeAll(!propagateInCluster);
            }
        }
        logger.info("...done flushing caches for manager {}", cacheManagerName);
    }

    /**
     * Flushes the specified Ehcache on the current cluster node only.
     *
     * @param cacheName the name of the cache to flush
     */
    public static void flushEhcacheByName(String cacheName) {
        flushEhcacheByName(cacheName, false);
    }

    /**
     * Flushes the specified Ehcache. If <code>propagateInCluster</code> is set to true also propagates the flush to other cluster nodes.
     *
     * @param cacheName          the name of the cache to flush
     * @param propagateInCluster if set to true the flush is propagated to other cluster nodes
     */
    public static void flushEhcacheByName(String cacheName, boolean propagateInCluster) {
        logger.info("Flushing {}", cacheName);
        Ehcache cache = getEhcache(cacheName);
        if (cache != null) {
            // flush
            cache.removeAll(!propagateInCluster);
            logger.info("...done flushing {}", cacheName);
        } else {
            logger.warn("Cache with the name {} not found. Skip flushing.", cacheName);
        }
    }

    private static Ehcache getEhcache(String cacheName) {
        Ehcache ehcache = getJahiaCacheManager().getEhcache(cacheName);
        return ehcache == null ? getBigCacheManager().getEhcache(cacheName) : ehcache;
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
     * @param propagateInCluster if set to true the flush is propagated to other cluster nodes
     */
    public static void flushOutputCaches(boolean propagateInCluster) {
        logger.info("Flushing HTML output caches{}",
                propagateInCluster ? " also propagating it to all cluster members" : "");
        flushOutputCachesInManager(propagateInCluster, getJahiaCacheManager());
        flushOutputCachesInManager(propagateInCluster, getBigCacheManager());
        if (propagateInCluster) {
            flushOutputCachesCluster();
        }
    }

    private static void flushOutputCachesInManager(boolean propagateInCluster, CacheManager ehcacheManager) {
        for (String cacheName : ehcacheManager.getCacheNames()) {
            if (!cacheName.startsWith("HTML")) {
                continue;
            }
            Ehcache cache = ehcacheManager.getEhcache(cacheName);
            if (cache != null) {
                // flush
                cache.removeAll(!propagateInCluster);
                logger.info("...done flushing {}", cacheName);
            }
        }
    }

    private static void flushOutputCachesCluster() {
        sendCacheFlushCommandToClusterImmediately(CMD_FLUSH_OUTPUT_CACHES);
    }

    public static void flushOutputCachesForPath(String path, boolean flushSubtree) {
        flushOutputCachesForPaths(Sets.newHashSet(path), flushSubtree);
    }

    public static void flushOutputCachesForPaths(Set<String> paths, boolean flushSubtree) {
        logger.debug("Flushing dependencies for paths: {}", paths);
        ModuleCacheProvider cacheProvider = ModuleCacheProvider.getInstance();
        Cache cache = cacheProvider.getDependenciesCache();
        Set<Object> cacheKeys = new HashSet<>();
        for (String path : paths) {
            Element element = cache.get(path);
            if (element != null) {
                logger.debug("Flushing path: {}", path);
                cacheKeys.add(element.getObjectKey());
            }
        }
        if (flushSubtree) {
            Set<String> pathsWithSubTree = new HashSet<>(paths);

            @SuppressWarnings("rawtypes")
            List keys = cache.getKeys();
            for (Object key : keys) {
                String stringKey = key.toString();
                if (isKeyMatched(stringKey, paths)) {
                    pathsWithSubTree.add(stringKey);
                    cacheKeys.add(key);
                }
            }
            paths = pathsWithSubTree;
        }
        cacheProvider.invalidate(paths, true);
        cache.removeAll(cacheKeys);
    }

    private static boolean isKeyMatched(String stringKey, Set<String> paths) {
        for (String path : paths) {
            if (stringKey.equals(path) || stringKey.startsWith(path + '/')) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private static CacheInfo getCacheInfo(Ehcache cache, boolean withConfig, boolean withSizeInBytes) {
        CacheInfo info = new CacheInfo(cache);
        info.setName(cache.getName());
        if (withConfig) {
            info.setConfig(cache.getCacheManager().getActiveConfigurationText(info.getName()));
        }
        StatisticsGateway stats = cache.getStatistics();
        info.setHitCount(stats.cacheHitCount());
        info.setMissCount(stats.cacheMissCount());
        info.setHitRatio(info.getAccessCount() > 0 ? info.getHitCount() * 100 / (double) info.getAccessCount() : 0);

        info.setSize(stats.getSize());

        info.setLocalHeapSize(stats.getLocalHeapSize());

        info.setOverflowToDisk(cache.getCacheConfiguration().isOverflowToDisk());
        if (info.isOverflowToDisk()) {
            info.setLocalDiskSize(stats.getLocalDiskSize());
        }

        info.setOverflowToOffHeap(cache.getCacheConfiguration().isOverflowToOffHeap());
        if (info.isOverflowToOffHeap()) {
            info.setLocalOffHeapSize(stats.getLocalOffHeapSize());
        }

        if (withSizeInBytes) {
            info.setLocalHeapSizeInBytes(stats.getLocalHeapSizeInBytes());
            info.setLocalHeapSizeInBytesHumanReadable(FileUtils.humanReadableByteCount(info.getLocalHeapSizeInBytes()));
            if (info.isOverflowToDisk()) {
                info.setLocalDiskSizeInBytes(stats.getLocalDiskSizeInBytes());
                info.setLocalDiskSizeInBytesHumanReadable(FileUtils.humanReadableByteCount(info
                        .getLocalDiskSizeInBytes()));
            }
            if (info.isOverflowToOffHeap()) {
                info.setLocalOffHeapSizeInBytes(stats.getLocalOffHeapSizeInBytes());
                info.setLocalOffHeapSizeInBytesHumanReadable(FileUtils.humanReadableByteCount(info
                        .getLocalOffHeapSizeInBytes()));
            }
        }

        return info;
    }

    public static CacheManager getCacheManager(String cacheManagerName) {
        for (CacheManager mgr : CacheManager.ALL_CACHE_MANAGERS) {
            if (cacheManagerName.equals(mgr.getName())) {
                return mgr;
            }
        }
        return null;
    }

    private static CacheManagerInfo getCacheManagerInfo(CacheManager manager, boolean withConfig,
            boolean withSizeInBytes) {
        CacheManagerInfo info = new CacheManagerInfo(manager);
        info.setName(manager.getName());
        if (withConfig) {
            info.setConfig(manager.getActiveConfigurationText());
        }

        for (String name : manager.getCacheNames()) {
            Ehcache cache = manager.getEhcache(name);
            if (cache != null) {
                CacheInfo cacheInfo = getCacheInfo(cache, withConfig, withSizeInBytes);
                info.getCaches().put(name, cacheInfo);
                info.setHitCount(info.getHitCount() + cacheInfo.getHitCount());
                info.setMissCount(info.getMissCount() + cacheInfo.getMissCount());

                info.setOverflowToDisk(info.isOverflowToDisk() || cacheInfo.isOverflowToDisk());
                info.setOverflowToOffHeap(info.isOverflowToOffHeap() || cacheInfo.isOverflowToOffHeap());

                info.setSize(info.getSize() + cacheInfo.getSize());
                info.setLocalHeapSize(info.getLocalHeapSize() + cacheInfo.getLocalHeapSize());
                if (info.isOverflowToDisk()) {
                    info.setLocalDiskSize(info.getLocalDiskSize() + cacheInfo.getLocalDiskSize());
                }
                if (info.isOverflowToOffHeap()) {
                    info.setLocalOffHeapSize(info.getLocalOffHeapSize() + cacheInfo.getLocalOffHeapSize());
                }
                if (withSizeInBytes) {
                    info.setLocalDiskSizeInBytes(info.getLocalDiskSizeInBytes() + cacheInfo.getLocalDiskSizeInBytes());
                    info.setLocalHeapSizeInBytes(info.getLocalHeapSizeInBytes() + cacheInfo.getLocalHeapSizeInBytes());
                    info.setLocalOffHeapSizeInBytes(info.getLocalOffHeapSizeInBytes()
                            + cacheInfo.getLocalOffHeapSizeInBytes());
                }
            }
        }

        if (withSizeInBytes) {
            info.setLocalDiskSizeInBytesHumanReadable(FileUtils.humanReadableByteCount(info.getLocalDiskSizeInBytes()));
            info.setLocalHeapSizeInBytesHumanReadable(FileUtils.humanReadableByteCount(info.getLocalHeapSizeInBytes()));
            if (info.isOverflowToOffHeap()) {
                info.setLocalOffHeapSizeInBytesHumanReadable(FileUtils.humanReadableByteCount(info
                        .getLocalOffHeapSizeInBytes()));
            }
        }

        return info;
    }

    /**
     * Returns the configuration and statistics information for all the available cache managers.
     *
     * @param withConfig
     *            if <code>true</code> the configuration information will be also available
     * @param withSizeInBytes
     *            if <code>true</code> the calculation of the cache sizes (bytes) will be also available
     * @return the configuration and statistics information for all the available cache managers
     */
    public static Map<String, CacheManagerInfo> getCacheManagerInfos(boolean withConfig, boolean withSizeInBytes) {
        Map<String, CacheManagerInfo> infos = new TreeMap<String, CacheManagerInfo>();
        for (CacheManager manager : CacheManager.ALL_CACHE_MANAGERS) {
            infos.put(manager.getName(), getCacheManagerInfo(manager, withConfig, withSizeInBytes));
        }

        return infos;
    }

    private static CacheManager getJahiaCacheManager() {
        return ((EhCacheProvider) SpringContextSingleton.getBean("ehCacheProvider"))
                .getCacheManager();
    }

    private static CacheManager getBigCacheManager() {
        return ((EhCacheProvider) SpringContextSingleton.getBean("bigEhCacheProvider"))
                .getCacheManager();
    }

    /**
     * Returns a value of the specified cache element (by key), considering also classloader-aware deserialization if required.
     *
     * @param cache
     *            the target cache
     * @param key
     *            the element key
     * @return a value of the specified cache element (by key), considering also classloader-aware deserialization if required
     */
    public static Object getObjectValue(Ehcache cache, String key) {
        return getObjectValue(cache.get(key));
    }

    /**
     * Returns a value of the specified cache element, considering also classloader-aware deserialization if required.
     *
     * @param cacheElement
     *            the cache element
     * @return a value of the specified cache element (by key), considering also classloader-aware deserialization if required
     */
    public static Object getObjectValue(Element cacheElement) {
        Object value = null;
        if (cacheElement != null) {
            value = cacheElement.getObjectValue();
            if (value != null && value instanceof ClassLoaderAwareCacheEntry) {
                value = ((ClassLoaderAwareCacheEntry) value).getValue();
            }
        }

        return value;
    }

    public static void registerMBeans(String cacheManagerName) {
        CacheManager mgr = getCacheManager(cacheManagerName);
        if (mgr == null) {
            logger.warn("Cannot find Ehcache manager for name {}. Skip registering managed beans in JMX", cacheManagerName);
            return;
        }
        ManagementService.registerMBeans(mgr, ManagementFactory.getPlatformMBeanServer(), true,
                true, true, true, true);
    }

    private static long getClusterRevision() {
        return SpringJackrabbitRepository.getInstance().getClusterRevision();
    }

    private static Cache getSyncCache() {
        return ModuleCacheProvider.getInstance().getSyncCache();
    }

    /**
     * Sends the specified cache flush command to other cluster nodes (if clustering is activated). The event will be processed immediately
     * upon receipt.
     *
     * @param command the command to be sent
     */
    public static void sendCacheFlushCommandToClusterImmediately(String command) {
        sendCacheFlushCommandToCluster(command, false, null, false);
    }

    /**
     * Sends the specified cache flush command to other cluster nodes (if clustering is activated). The event will be processed considering
     * current cluster revision.
     *
     * @param command the command to be sent
     */
    public static void sendCacheFlushCommandToCluster(String command) {
        sendCacheFlushCommandToCluster(command, null);
    }

    /**
     * Sends the specified cache flush command with a message to other cluster nodes (if clustering is activated). The event will be
     * processed considering current cluster revision.
     *
     * @param command the command to be sent
     * @param eventMessage custom event message
     */
    public static void sendCacheFlushCommandToCluster(String command, String eventMessage) {
        sendCacheFlushCommandToCluster(command, true, eventMessage, true);
    }

    /**
     * Sends the specified cache flush command to other cluster nodes (if clustering is activated).
     *
     * @param command the command to be sent
     * @param appendRandomSufixToCommand if true a random prefix (generated UUID) will be appended to the command name
     * @param eventMessage custom event message
     * @param executeConsideringCurrentClusterRevision if <code>true</code> the event will be processed considering current cluster
     *            revision; if <code>false</code>, the event will be processed immediately upon receipt by the other cluster node.
     */
    private static void sendCacheFlushCommandToCluster(String command, boolean appendRandomSufixToCommand,
            String eventMessage, boolean executeConsideringCurrentClusterRevision) {
        Cache syncCache = getSyncCache();
        if (syncCache != null) {
            // we use special sync cache here to let other cluster nodes know that they need to flush caches
            // this "event" is handled in org.jahia.services.cache.ehcache.FlushCacheEventListener
            CacheClusterEvent cacheEvent = new CacheClusterEvent(eventMessage,
                    executeConsideringCurrentClusterRevision ? getClusterRevision() : Long.MIN_VALUE);
            String commandString = appendRandomSufixToCommand ? (command + '-' + UUID.randomUUID()) : command;
            Element element = new Element(commandString, cacheEvent);
            if (logger.isDebugEnabled()) {
                logger.debug("Sending cache flush command {} to cluster with messsage {} and cluster revision {}",
                        new Object[] { commandString, cacheEvent.getEvent(), cacheEvent.getClusterRevision() });
            }
            syncCache.put(element);
        }
    }

    /**
     * Sends the specified cache flush commands to other cluster nodes (if clustering is activated). The events will be processed
     * considering current cluster revision.
     *
     * @param command the command to be sent
     * @param eventMessages a collection of event messages to send
     */
    public static void sendMultipleCacheFlushCommandsToCluster(String command, Collection<String> eventMessages) {
        sendMultipleCacheFlushCommandsToCluster(command, true, eventMessages, true);
    }

    /**
     * Sends the specified cache flush commands to other cluster nodes (if clustering is activated).
     *
     * @param command the command to be sent
     * @param appendRandomSufixToCommand if true a random prefix (generated UUID) will be appended to the command name
     * @param eventMessages a collection of event messages to send
     * @param executeConsideringCurrentClusterRevision if <code>true</code> the event will be processed considering current cluster
     *            revision; if <code>false</code>, the event will be processed immediately upon receipt by the other cluster node.
     */
    private static void sendMultipleCacheFlushCommandsToCluster(String command, boolean appendRandomSufixToCommand,
            Collection<String> eventMessages, boolean executeConsideringCurrentClusterRevision) {
        Cache syncCache = getSyncCache();
        if (syncCache != null && eventMessages != null && !eventMessages.isEmpty()) {
            // we use special sync cache here to let other cluster nodes know that they need to flush caches
            // this "event" is handled in org.jahia.services.cache.ehcache.FlushCacheEventListener
            List<Element> cacheElements = new ArrayList<>(eventMessages.size());
            long clusterRevision = executeConsideringCurrentClusterRevision ? getClusterRevision() : Long.MIN_VALUE;
            boolean debugEnabled = logger.isDebugEnabled();
            for (String msg : eventMessages) {
                CacheClusterEvent cacheEvent = new CacheClusterEvent(msg, clusterRevision);
                String commandString = appendRandomSufixToCommand ? (command + '-' + UUID.randomUUID()) : command;
                Element element = new Element(commandString, cacheEvent);
                cacheElements.add(element);
                if (debugEnabled) {
                    logger.debug("Sending cache flush command {} to cluster with messsage {} and cluster revision {}",
                            new Object[] { commandString, cacheEvent.getEvent(), cacheEvent.getClusterRevision() });
                }
            }
            syncCache.putAll(cacheElements);
        }
    }
}
