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

import org.jahia.services.cache.clusterservice.ClusterCacheMessage;
import org.jahia.services.cache.clusterservice.batch.BatchingClusterServiceCacheImpl;
import org.jahia.services.cache.clusterservice.batch.ClusterCacheMessageBatcher;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.cache.CacheImplementation;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;

import java.util.Date;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 4 mai 2006
 * Time: 10:55:15
 * To change this template use File | Settings | File Templates.
 */
public class JahiaBatchingHibernateCache extends BatchingClusterServiceCacheImpl implements Cache {
    public JahiaBatchingHibernateCache(String name, ClusterCacheMessageBatcher batcher, CacheImplementation underlyingCacheImplementation) {
        super(name, batcher, underlyingCacheImplementation);
    }

    /**
     * Get an item from the cache
     *
     * @param key
     * @return the cached object or <tt>null</tt>
     * @throws org.hibernate.cache.CacheException
     *
     */
    public Object read(Object key) throws CacheException {
        // Try to get the cache entry out of the JCS cache
        CacheEntry entry = (CacheEntry) super.get(key);
        if (entry == null) {
            if (logger.isDebugEnabled()) {
                // log the result
                StringBuffer buffer = new StringBuffer("Entry [");
                buffer.append(key.toString());
                buffer.append("] could not be found in cache [");
                buffer.append(getName());
                buffer.append("]!");
                logger.debug(buffer.toString());
            }
            return null;
        }
        // get the entry expiration date
        Date date = entry.getExpirationDate();

        // check if the entry is expired
        if (date != null) {
            Date now = new Date();
            if (date.compareTo(now) < 0) {
                // entry has expired, we must remove it and then exit.
                logger.debug("Cache entry has expired, ignoring entry and removing...");
                remove(key);
                return null;

            } else {
                logger.debug("Cache entry has not expired, continuing...");
            }
        }
        // increase the entry hits
        entry.incrementHits();

        // update the last accessed time for the JCS cache entry.
        entry.setLastAccessedTimeNow();

        return entry.getObject();
    }

    public Object get(Object key) {
        return read(key);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Add an item to the cache, nontransactionally, with
     * failfast semantics
     *
     * @param key
     * @param value
     * @throws org.hibernate.cache.CacheException
     *
     */
    public void put(Object key, Object value) throws CacheException {
        super.put(key, null, new CacheEntry(value));
    }

    /**
     * Add an item to the cache
     *
     * @param key
     * @param value
     * @throws org.hibernate.cache.CacheException
     *
     */
    public void update(Object key, Object value) throws CacheException {
        super.put(key, null, new CacheEntry(value));
    }

    /**
     * Clear the cache
     */
    public void clear() throws CacheException {
        super.flushAll(false);
    }

    /**
     * Clean up
     */
    public void destroy() throws CacheException {
        new Thread() {
            public void run() {
                flushAll(false);
            }
        }.start();
    }

    /**
     * If this is a clustered cache, lock the item
     */
    public void lock(Object key) throws CacheException {
        throw new UnsupportedOperationException("JahiaHibernateCahce doesn't support locking");
    }

    /**
     * If this is a clustered cache, unlock the item
     */
    public void unlock(Object key) throws CacheException {
        throw new UnsupportedOperationException("JahiaHibernateCahce doesn't support locking");
    }

    /**
     * Generate a timestamp
     */
    public long nextTimestamp() {
        return System.currentTimeMillis() / 100;
    }

    /**
     * Get a reasonable "lock timeout"
     */
    public int getTimeout() {
        return 600;
    }

    /**
     * Get the name of the cache region
     */
    public String getRegionName() {
        return getName();
    }

    /**
     * The number of bytes is this cache region currently consuming in memory.
     *
     * @return The number of bytes consumed by this region; -1 if unknown or
     *         unsupported.
     */
    public long getSizeInMemory() {
        return -1;
    }

    /**
     * The count of entries currently contained in the regions in-memory store.
     *
     * @return The count of entries in memory; -1 if unknown or unsupported.
     */
    public long getElementCountInMemory() {
        return super.size();
    }

    /**
     * The count of entries currently contained in the regions disk store.
     *
     * @return The count of entries on disk; -1 if unknown or unsupported.
     */
    public long getElementCountOnDisk() {
        return -1;
    }

    /**
     * optional operation
     */
    public Map toMap() {
        return null;
    }

    public String toString() {
        return "JahiaHibernateCache(" + getName() + ')';
    }

    protected void onFlush(ClusterCacheMessage clusterCacheMessage) {
        super.onFlush(clusterCacheMessage);    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected void onFlushGroup(ClusterCacheMessage clusterCacheMessage) {
        super.onFlushGroup(clusterCacheMessage);    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected void onInvalidateEntry(ClusterCacheMessage clusterCacheMessage) {
        super.onInvalidateEntry(clusterCacheMessage);    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected void onRemove(ClusterCacheMessage clusterCacheMessage) {
        super.onRemove(clusterCacheMessage);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
