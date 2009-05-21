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
package org.jahia.services.cache.reference;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.collections.OrderedMapIterator;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.CacheListener;
import org.jahia.services.cache.GroupCacheKey;

/**
 * We can configure the behavior of SoftReferences according to the following extract of the
 * Sun Hotspot FAQ : 
 * 
 * What determines when softly referenced objects are flushed?
 * 
 * Starting with 1.3.1, softly reachable objects will remain alive for some amount of time 
 * after the last time they were referenced. The default value is one second of lifetime 
 * per free megabyte in the heap. This value can be adjusted using the 
 * -XX:SoftRefLRUPolicyMSPerMB flag, which accepts integer values representing milliseconds. 
 * For example, to change the value from one second to 2.5 seconds, use this flag:
 * -XX:SoftRefLRUPolicyMSPerMB=2500
 * The Java HotSpot Server VM uses the maximum possible heap size (as set with the -Xmx 
 * option) to calculate free space remaining.
 * The Java Hotspot Client VM uses the current heap size to calculate the free space.
 * This means that the general tendency is for the Server VM to grow the heap rather 
 * than flush soft references, and -Xmx therefore has a significant effect on when 
 * soft references are garbage collected.
 * On the other hand, the Client VM will have a greater tendency to flush soft references 
 * rather than grow the heap.
 * The behavior described above is true for 1.3.1 through Java SE 6 versions of the Java 
 * HotSpot VMs. This behavior is not part of the VM specification, however, and is 
 * subject to change in future releases. Likewise the -XX:SoftRefLRUPolicyMSPerMB 
 * flag is not guaranteed to be present in any given release.
 * Prior to version 1.3.1, the Java HotSpot VMs cleared soft references whenever it 
 * found them.
 *
 * Groups use WeakHashMaps internally so that when a cache key is flushed, the group doesn't
 * retain a reference to the key either. If only a WeakSet class existed, this would have
 * been better, but here we just set null values in the WeakHashMap.
 * 
 * @author Serge Huber
 *
 *
 */
public class ReferenceCacheImpl<K, V> implements CacheImplementation<K, V> {

    final private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(ReferenceCacheImpl.class);

    class GroupLRUMap extends LRUMap {
        private static final long serialVersionUID = 2259981080159400501L;
        
        ReferenceCacheImpl<K, V> cacheImplementation;

        GroupLRUMap(ReferenceCacheImpl<K, V> cacheImpl, int maxGroups) {
            super(maxGroups);
            this.cacheImplementation = cacheImpl;
        }

        /**
         * We subclass this method because when a group is removed we
         * will flush all the cache entries in that group.
         * 
         * @param linkEntry
         * @return
         */
        protected boolean removeLRU(LinkEntry linkEntry) {
            boolean result = super.removeLRU(linkEntry);
            if (logger.isDebugEnabled()) {
                logger.debug("Flushing group " + (String) linkEntry.getKey());
            }
            Map<?, ?> groupKeys = (Map<?, ?>)linkEntry.getValue();
            if (groupKeys != null) {
                cacheImplementation.flushKeys(new HashSet<Object>(groupKeys
                        .keySet()));
            }
            return result;
        }

    }

    private final Map<K, V> cache = new ReferenceMap();
    private Map<String, Map<GroupCacheKey, Object>> groups;
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private Lock readLock = rwl.readLock();
    private Lock writeLock = rwl.writeLock();

    private String name;
    private long cacheLimit = -1;
    private long cacheGroupsLimit = -1;

    public ReferenceCacheImpl(String name, int maxGroups) {
        this.name = name;
        groups = new GroupLRUMap(this, maxGroups);
    }

    public boolean containsKey(K key) {
        try {
            readLock.lockInterruptibly();
            try {
                return cache.containsKey(key);
            } finally {
                readLock.unlock();
            }
        } catch (InterruptedException ie) {
            logger.error("Error acquiring read lock", ie);
            return false;
        }
    }

    public V get(K key) {
        try {
            readLock.lockInterruptibly();
            try {
                return cache.get(key);
            } finally {
                readLock.unlock();
            }
        } catch (InterruptedException ie) {
            logger.error("Error acquiring read lock", ie);
            return null;
        }
    }

    /**
     * Puts an entry in the cache, using optional groups
     * 
     * @param key
     * @param groups
     *            the groups this entry should be part of, or null if it's part of no group.
     * @param value
     */
    public void put(K key, String[] groups, V value) {
        if (getCacheLimit() == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("cache is deactivated. Aborting store.");
            }
            return;
        }

        try {
            writeLock.lockInterruptibly();
            try {
                V oldValue = cache.put(key, value);
                if (logger.isDebugEnabled()) {
                    logger.debug("cache put: " + key);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Stack trace",
                                new Exception("Stack trace"));
                    }
                }
                if (key instanceof GroupCacheKey) {
                    addToGroups((GroupCacheKey)key, oldValue == null ? true : false);
                }
            } finally {
                writeLock.unlock();
            }
        } catch (InterruptedException ie) {
            logger.error("Error acquiring write lock", ie);
        }
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public int size() {
        try {
            readLock.lockInterruptibly();
            try {
                return cache.size();
            } finally {
                readLock.unlock();
            }
        } catch (InterruptedException ie) {
            logger.error("Error acquiring read lock", ie);
            return 0;
        }
    }

    public long getGroupsSize() {
        return groups.size();
    }

    public long getGroupsKeysTotal() {
        long totalSize = 0;
        try {
            for (Map<GroupCacheKey, Object> keySet : groups.values()) {
                totalSize += keySet.size();
            }
        } catch (ConcurrentModificationException cme) {
            logger
                    .warn("Concurrent modification exception while calculating total groups keys for cache "
                            + getName() + ", returning -1");
            totalSize = -1;
        }
        return totalSize;
    }

    public void flushAll(boolean propagate) {
        writeLock.lock();
        try {
            cache.clear();
            groups.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public void flushGroup(String groupName) {
        writeLock.lock();
        try {
            Set<GroupCacheKey> keysToFlush = null;        
            Map<GroupCacheKey, Object> groupKeys = groups.get(groupName);
            if (groupKeys != null) {
                keysToFlush = groupKeys.keySet();
                if (keysToFlush != null) {
                    groups.remove(groupName);
                    flushKeys(new HashSet<Object>(keysToFlush));
                }
            }            
        } finally {
            writeLock.unlock();
        }
    }

    protected void flushKeys(Set<Object> keysToFlush) {
        writeLock.lock();
        try {
            for (Object curKeyToFlush : keysToFlush) {
                doRemove(curKeyToFlush);
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void addToGroups(GroupCacheKey key, boolean keyInstanceIsNew) {
        if (!(key instanceof GroupCacheKey)) {
            return;
        }
        try {
            writeLock.lockInterruptibly();
            try {
                for (String curGroup : ((GroupCacheKey) key).getGroups()) {
                    addToGroup(curGroup, key, keyInstanceIsNew);
                }
            } finally {
                writeLock.unlock();
            }
        } catch (InterruptedException ie) {
            logger.error("Error acquiring write lock", ie);
        }
    }

    private void addToGroup(String groupName, GroupCacheKey key, boolean keyInstanceIsNew) {
        try {
            writeLock.lockInterruptibly();
            try {
                Map<GroupCacheKey, Object> currentKeys = groups.get(groupName);
                if (currentKeys == null) {
                    currentKeys = new WeakHashMap<GroupCacheKey, Object>();
                } else if (keyInstanceIsNew && currentKeys.containsKey(key)) {
                    // this needs to be done as the key instance in the cache is a 
                    // new one, so we have to remove the reference to the old key instance
                    // as otherwise the entry will very soon be garbage collected
                    currentKeys.remove(key);
                }
                currentKeys.put(key, null);
                groups.put(groupName, currentKeys);
            } finally {
                writeLock.unlock();
            }
        } catch (InterruptedException ie) {
            logger.error("Error acquiring write lock", ie);
        }
    }

    private void removeFromGroup(String groupName, Object key) {
        try {
            writeLock.lockInterruptibly();
            try {
                Map<GroupCacheKey, Object> currentKeys = groups.get(groupName);
                if (currentKeys != null) {
                    currentKeys.remove(key);
                }
            } finally {
                writeLock.unlock();
            }
        } catch (InterruptedException ie) {
            logger.error("Error acquiring write lock", ie);
        }
    }

    private void removeFromAllGroups(Object key) {
        if (key.getClass() == GroupCacheKey.class) {
            try {
                writeLock.lockInterruptibly();
                try {
                    for (String curGroup : ((GroupCacheKey) key).getGroups()) {
                        removeFromGroup(curGroup, key);
                    }
                } finally {
                    writeLock.unlock();
                }
            } catch (InterruptedException ie) {
                logger.error("Error acquiring write lock", ie);
            }
        } else {
            // this is possible if the global LRU map is used to flush entries
        }
    }

    public void remove(Object key) {
        doRemove(key);
    }

    private boolean doRemove(Object key) {
        boolean removed = false;
        try {
            writeLock.lock();
            try {
                Object removedObject = cache.remove(key);
                if (logger.isDebugEnabled()) {
                    logger.debug("cache remove: " + key);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Stack trace",
                                new Exception("Stack trace"));
                    }
                }
                removeFromAllGroups(key);                
                if (removedObject != null) {
                    removed = true;
                }
            } finally {
                writeLock.unlock();
            }
        } catch (Exception e) {
            logger.warn("Cannot remove cache entry " + key + " from cache "
                    + toString(), e);
        }
        return removed;
    }

    public String getName() {
        return name;
    }

    public void addListener(CacheListener listener) {
    }

    public void removeListener(CacheListener listener) {
    }

    /**
     * <p>
     * Returns the maximum size allowed for the cache.
     * </p>
     * 
     * @return an integer representing the maximum cache size. Returns -1 if there is no limit set.
     */
    public long getCacheLimit() {
        return cacheLimit;
    }

    /**
     * <p>
     * Set the cache size limit. -1 will define an unlimited cache size.
     * </p>
     * 
     * @param limit
     *            the new size limit
     */
    public void setCacheLimit(long limit) {
        cacheLimit = limit;
    }

    public long getCacheGroupsLimit() {
        return cacheGroupsLimit;
    }

    /**
     * Value 0 is not supported (meaning you cannot really deactivate this groups).
     * 
     * @param groupsLimit
     */
    public void setCacheGroupsLimit(long groupsLimit) {
        try {
            writeLock.lockInterruptibly();
            try {
                // if we set to an unlimited value, we simply keep the old size.
                if (groupsLimit != -1) {
                    // now we must copy values to a new LRUMap with the correct size;
                    if (groupsLimit != 0) {
                        GroupLRUMap newGroups = new GroupLRUMap(this,
                                (int) groupsLimit);
                        OrderedMapIterator iterator = ((GroupLRUMap)groups)
                                .orderedMapIterator();
                        while (iterator.hasNext()) {
                            iterator.next();
                            newGroups.put(iterator.getKey(), iterator
                                    .getValue());
                        }
                        groups = newGroups;
                    } else {
                        // if we set 0, we create an LRU map with size 1, as a size 0 is
                        // not allowed.
                        // TODO this is not compliant with the specification !
                        groups = new GroupLRUMap(this, 1);
                    }
                    this.cacheGroupsLimit = groupsLimit;
                }
            } finally {
                writeLock.unlock();
            }
        } catch (InterruptedException ie) {
            logger.error("Error acquiring write lock", ie);
        }
    }

    public String toString() {
        return "ReferenceCache(" + getName() + ')';
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<GroupCacheKey> getGroupKeys(String groupName) {
        Map<GroupCacheKey, Object> map = groups.get(groupName);
        if (map != null) {
            return map.keySet();
        }
        return null;
    }

    public Collection<K> getKeys() {
        return cache.keySet();
    }
}
