/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.modules.serversettings.flow;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Statistics;
import org.apache.commons.lang.StringUtils;
import org.jahia.modules.serversettings.cache.CacheManagement;
import org.jahia.services.cache.CacheHelper;
import org.jahia.utils.FileUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Init class for cache management
 * @author david
 */
public class CacheManagerHandler implements Serializable {
    private static final long serialVersionUID = 8547213229629335665L;

    private Map<String, List<SerializedCacheManager>> managersMap;
    private String sizeMemory;
    private String sizeDisk;
    private CacheManagement cacheManagement;

    /**
     * init class call during flow initialization
     */
    public void init() {
        managersMap = new TreeMap<String, List<SerializedCacheManager>>();
        computeCacheMap(false);
        cacheManagement = new CacheManagement();

    }

    /**
     * Method called when the form is submitted
     * @return must return a string to process the form
     */
    public String performAction() {
        boolean propagate = StringUtils.equals(cacheManagement.getPropagate(), "true");
        if (StringUtils.equals(cacheManagement.getAction(), "flushOutputCaches")) {
            CacheHelper.flushOutputCaches(propagate);
        }
        if (StringUtils.equals(cacheManagement.getAction(), "flushHibernateCaches")) {
            CacheHelper.flushHibernateCaches(propagate);
        }
        if (StringUtils.equals(cacheManagement.getAction(), "flushOutputCaches")) {
            CacheHelper.flushOutputCaches(propagate);
        }
        if (StringUtils.equals(cacheManagement.getAction(), "flushCaches")) {
            CacheHelper.flushCachesForManager(cacheManagement.getName(), propagate);
        }
        if (StringUtils.equals(cacheManagement.getAction(), "flush")) {
            CacheHelper.flushEhcacheByName(cacheManagement.getName(), propagate);
        }
        if (StringUtils.equals(cacheManagement.getAction(), "flushAllCaches")) {
            CacheHelper.flushAllCaches(propagate);
        }

        if (StringUtils.equals(cacheManagement.getAction(), "enableStats") || StringUtils.equals(cacheManagement.getAction(), "disableStats")) {
            boolean enable = StringUtils.equals(cacheManagement.getAction(), "enableStats");
            CacheManager mgr = CacheHelper.getCacheManager(cacheManagement.getName());
            for (String cacheName : mgr.getCacheNames()) {
                Cache cache = mgr.getCache(cacheName);
                if (cache == null) {
                    continue;
                }
                cache.setStatisticsEnabled(enable);
                cache.clearStatistics();
            }
        }
        computeCacheMap(cacheManagement.isShowBytes());
        return "";
    }

    public Map<String, List<SerializedCacheManager>> getManagersMap() {
        return managersMap;
    }

    public boolean getClusterActivated() {
        return Boolean.getBoolean("cluster.activated");
    }

    public String getSizeMemory() {
        return sizeMemory;
    }

    public String getSizeDisk() {
        return sizeDisk;
    }

    public CacheManagement getCacheManagement() {
        return cacheManagement;
    }

    private void computeCacheMap(boolean doDiskSize) {
        int sizeMemory = 0;
        int sizeDisk = 0;
        for (CacheManager manager : CacheManager.ALL_CACHE_MANAGERS) {
            List<SerializedCacheManager> cacheList = new ArrayList<SerializedCacheManager>();
            String[] names = manager.getCacheNames();
            Arrays.sort(names);

            for (String name : names) {
                Cache cache = manager.getCache(name);
                Statistics stats = cache.getStatistics();
                SerializedCacheManager serializedCacheManager = new SerializedCacheManager();
                serializedCacheManager.setConfig(manager.getActiveConfigurationText(name));
                serializedCacheManager.setName(name);
                serializedCacheManager.setCacheHits(stats.getCacheHits());
                serializedCacheManager.setCacheMisses(stats.getCacheMisses());
                if (doDiskSize) {
                    serializedCacheManager.setCalculateInMemorySize(cache.getMemoryStoreSize());
                    serializedCacheManager.setCalculateOnDiskSize(cache.calculateOnDiskSize());
                }
                serializedCacheManager.setMemoryStoreObjectCount(stats.getMemoryStoreObjectCount());
                serializedCacheManager.setObjectCount(stats.getObjectCount());
                serializedCacheManager.setDiskStoreObjectCount(stats.getDiskStoreObjectCount());
                serializedCacheManager.setOverflowToDisk(cache.getCacheConfiguration().isOverflowToDisk());
                serializedCacheManager.setStatisticsEnabled(cache.isStatisticsEnabled());
                cacheList.add(serializedCacheManager);
                sizeDisk += manager.getCache(name).calculateOnDiskSize();
                sizeMemory += manager.getCache(name).calculateInMemorySize();
            }
            managersMap.put(manager.getName(), cacheList);
        }
        this.sizeDisk = FileUtils.humanReadableByteCount(sizeDisk);
        this.sizeMemory = FileUtils.humanReadableByteCount(sizeMemory);
    }

    /**
     * Serializable implementation of the cache manager to be able to read it from the flow
     */
    public class SerializedCacheManager implements Serializable {
        private static final long serialVersionUID = -5349727732290619923L;
        private String config;
        private boolean overflowToDisk;
        private String calculateInMemorySize;
        private String calculateOnDiskSize;
        private boolean statisticsEnabled;
        private long objectCount;
        private long memoryStoreObjectCount;
        private long diskStoreObjectCount;
        private long cacheHits;
        private long cacheMisses;
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        public boolean isOverflowToDisk() {
            return overflowToDisk;
        }

        public void setOverflowToDisk(boolean overflowToDisk) {
            this.overflowToDisk = overflowToDisk;
        }

        public String getCalculateInMemorySize() {
            return calculateInMemorySize;
        }

        public void setCalculateInMemorySize(long calculateInMemorySize) {
            this.calculateInMemorySize = FileUtils.humanReadableByteCount(calculateInMemorySize);
        }

        public String getCalculateOnDiskSize() {
            return calculateOnDiskSize;
        }

        public void setCalculateOnDiskSize(long calculateOnDiskSize) {
            this.calculateOnDiskSize = FileUtils.humanReadableByteCount(calculateOnDiskSize);
        }

        public boolean isStatisticsEnabled() {
            return statisticsEnabled;
        }

        public void setStatisticsEnabled(boolean statisticsEnabled) {
            this.statisticsEnabled = statisticsEnabled;
        }

        public long getObjectCount() {
            return objectCount;
        }

        public void setObjectCount(long objectCount) {
            this.objectCount = objectCount;
        }

        public long getMemoryStoreObjectCount() {
            return memoryStoreObjectCount;
        }

        public void setMemoryStoreObjectCount(long memoryStoreObjectCount) {
            this.memoryStoreObjectCount = memoryStoreObjectCount;
        }

        public long getDiskStoreObjectCount() {
            return diskStoreObjectCount;
        }

        public void setDiskStoreObjectCount(long diskStoreObjectCount) {
            this.diskStoreObjectCount = diskStoreObjectCount;
        }

        public long getCacheHits() {
            return cacheHits;
        }

        public void setCacheHits(long cacheHits) {
            this.cacheHits = cacheHits;
        }

        public long getCacheMisses() {
            return cacheMisses;
        }

        public void setCacheMisses(long cacheMisses) {
            this.cacheMisses = cacheMisses;
        }
    }
}
