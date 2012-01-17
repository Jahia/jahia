/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/** <p>This is the root class for all the cache in Jahia.</p>
 *
 * <p>This cache can handle synchronization messages with other Jahia server instances
 * by using JMS messages. This synchronization is automatically initialized and used
 * when the JMS synchronization is activated in the Jahia configuration file.</p>
 *
 * <p>Each cache <b>must</b> has a distinct <code>name</code>, and the
 * associated <code>description</code> is only for debugging purpose or for
 * monitoring display. Each cache uses a MRU (Most Recent Used) list to determine which
 * elements will be removed from the cache when the cache limit has been reached. In this
 * case, the least used cache entry will be removed.</p>
 *
 * <p>Each object inserted in the cache will be wrapped into a
 * {@link org.jahia.services.cache.CacheEntry CacheEntry} instance, which contains
 * among other information, the entry's expiration date and last accessed date.</p>
 *
 * <p>Using the {@link org.jahia.services.cache.Cache#getCacheEntry getCacheEntry}
 * method will retrieve the cache entry instance and <b>not</b> the object stored into
 * the entry instance. To access the stored object instance, the
 * {@link org.jahia.services.cache.Cache#get get} method should be used instead or use
 * the getter methods of the {@link org.jahia.services.cache.CacheEntry CacheEntry}
 * class.</p>
 *
 * <p>Caches can only be retrieved and created through the
 * {@link org.jahia.services.cache.CacheFactory CacheFactory} class, which is responsible
 * for managing all the caches.</p>
 *
 * @author  Fulco Houkes, Copyright (c) 2003 by Jahia Ltd.
 * @version 1.0
 * @since   Jahia 4.0
 * @see     org.jahia.services.cache.CacheFactory CacheFactory
 * @see     org.jahia.services.cache.CacheEntry CacheEntry
 * @see     org.jahia.services.cache.CacheListener CacheListener
 */
public class Cache<K, V> {

    /** logging. */
    final private static org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger (Cache.class);

    /** cache map instance. */
    private CacheImplementation<K, CacheEntry<V>> cacheImplementation;

    /** Cache name. */
    private String name;

    /** number of cache hit that could successfully be served. */
    private long successHitCount = 0;

    /** total number of cache hits. */
    private long totalHitCount = 0;

    private List<CacheListener> listeners = null;


    /** <p>Creates a new <code>Cache</code> instance.</p>
     *
     * @param name          the cache name
     */
    protected Cache (final String name, final CacheImplementation<K, CacheEntry<V>> cacheImplementation) {
        this.cacheImplementation = cacheImplementation;
        init (name);
    }


    /** <p>Retrieve the cache entry associated to the <code>entryKey</code> argument or
     * <code>null</code> when the requested cache entry is not available, or when the
     * <code>entryKey</code> argument is <code>null</code>.</p>
     *
     * <p>The last accessed date of the returned cache entry will be updated to the current
     * date (& time) and it's set as the most recent used entry in the cache's MRU list.
     * Expired cache entries will automatically be removed from the cache and
     * <code>null</code> will be returned as result.</p>
     *
     * @param entryKey  the cache entry key. <code>null</code> keys will
     *                   return <code>null</code>.
     *
     * @return  the reference to the cache entry
     */
    public CacheEntry<V> getCacheEntry (K entryKey) {

        // don't know what to do with a null key, just return null in this case!
        if (entryKey == null)
            return null;

        // a new cache hit
        totalHitCount++;

        // Try to get the cache entry out of the JCS cache
        CacheEntry<V> entry = cacheImplementation.get (entryKey);
        if (entry == null) {
            if (logger.isDebugEnabled()) {
                // log the result
                StringBuffer buffer = new StringBuffer("Entry [");
                buffer.append(entryKey.toString());
                buffer.append("] could not be found in cache [");
                buffer.append(name);
                buffer.append("]!");
                logger.debug(buffer.toString());
            }
            return null;
        }

        // get the entry expiration date
        Date date = entry.getExpirationDate ();

        // check if the entry is expired
        if (date != null) {
            Date now = new Date ();
            if (date.compareTo (now) < 0) {
                // entry has expired, we must remove it and then exit.
                logger.debug ("Cache entry has expired, ignoring entry and removing...");
                remove (entryKey);
                return null;
            }
        }

        // at this point, the cache entry could be found -> another successful cache hit!
        successHitCount++;

        // increase the entry hits
        entry.incrementHits ();

        // update the last accessed time for the JCS cache entry.
        entry.setLastAccessedTimeNow ();

        return entry;
    }


    /** <p>Fetchs the cache entry associated to the <code>entryKey</code> and returns the
     * object stored in fetched cache entry. <code>null</code> is returned when the
     * <code>entryKey</code> is <code>null</code>, or when no cache entry could be found
     * for the specified <code>entryKey</code>.</p>
     *
     * @param entryKey  the key associated to the requested object. A <code>null</code>
     *                   key will return in a <code>null</code> result.
     *
     * @return  the object associated to the code <code>entryKey</code> cache entry.
     */
    public V get (K entryKey) {
        if (entryKey == null) {
            logger.debug ("Cannot fetch with an null entry key!!!");
            return null;
        }

        CacheEntry<V> entry = getCacheEntry (entryKey);
        if (entry != null) {
            return entry.getObject ();
        }
        return null;
    }


    /** <p>Add a new object into the cache. This method encapsulates automatically the specified
     * <code>entryObj</code> object into a new
     * {@link org.jahia.services.cache.CacheEntry CacheEntry} instance and associated the
     * new cache entry to the <code>entryKey</code> key. If there is already an entry
     * associated with the <code>entryKey</code> key in the cache, that entry will be removed
     * and replaced with the new specified entry.</p>
     *
     * @param entryKey  the object's associated key, <code>null</code> is not allowed
     * @param entryObj  the reference to the object to be cached.
     */
    public void put (K entryKey, V entryObj) {
        put(entryKey, entryObj, true);
    }

    /** <p>Add a new object into the cache. This method encapsulates automatically the specified
     * <code>entryObj</code> object into a new
     * {@link org.jahia.services.cache.CacheEntry CacheEntry} instance and associated the
     * new cache entry to the <code>entryKey</code> key. If there is already an entry
     * associated with the <code>entryKey</code> key in the cache, that entry will be removed
     * and replaced with the new specified entry.</p>
     *
     * @param entryKey  the object's associated key, <code>null</code> is not allowed
     * @param entryObj  the reference to the object to be cached.
     * @param propagate specifies whether the cache update should be sent
     * across the cluster. If you don't know what this means, set it to true.
     * Set it to false it you want to update the cache ONLY locally, for example
     * when implementing a cache listener method.
     */
    public void put (K entryKey, V entryObj, boolean propagate) {
        if (entryKey == null) {
            logger.debug ("Cannot add an object with an empty key!!");
            return;
        }

        CacheEntry<V> entry = new CacheEntry<V>(entryObj);
        putCacheEntry (entryKey, entry, propagate);
    }


    /** <p>Add a new entry into the cache.</p>
     *
     * <p>The process will be ignored when either <code>entryKey</code> and/or
     * <code>entry</code> is/are <code>null</code>. If there is already an entry
     * associated with the <code>entryKey</code> key in the cache, that entry will be removed
     * and replaced with the new specified entry.</p>
     *
     * @param entryKey  the cache entry key, <code>null</code> is not allowed.
     * @param entry     the reference to the entry to be cached, <code>null</code> is not
     *                   allowed.
     * @param propagate specifies whether the cache update should be sent
     * across the cluster. If you don't know what this means, set it to true.
     * Set it to false it you want to update the cache ONLY locally, for example
     * when implementing a cache listener method.
     */
    public void putCacheEntry (K entryKey, CacheEntry<V> entry, boolean propagate) {

        internalPut(entryKey, entry);

    }

    private boolean internalPut (K entryKey, CacheEntry<V> entry) {
        if ((entryKey == null) || (entry == null)) {
            logger.debug ("null cache entry key or entry object, cannot cache such an object!");
            return false;
        }

        if (logger.isDebugEnabled()) {
            CacheEntry<V> existingEntry = cacheImplementation.get (entryKey);
            if (existingEntry != null) {
                logger.debug("Updating cache entry " + entryKey + " for cache " + getName());
                if (entry.getObject().equals(existingEntry.getObject())) {
                    logger.debug("Updating cache "+getName()+" entry " + entryKey + " with same object value ("+entry.getObject()+")!");
                }
            }
        }

        if (entryKey.getClass() == GroupCacheKey.class) {
            GroupCacheKey groupKey = (GroupCacheKey) entryKey;
            cacheImplementation.put (entryKey, groupKey.getGroupArray(), entry);
        } else {
            cacheImplementation.put (entryKey, null, entry);
        }
        return true;
    }


    /** <p>Initialize the cache.</p>
     *
     * @param name          the cache name
     */
    private void init (final String name) {
        // set the cache name
        this.name = name;

    }


    /** <p>Removes the cache entry associate to the key <code>entryKey</code>. The removal
     * operation is canceled in case the <code>entryKey</code> is <code>null</code>; does
     * nothing when the <code>entryKey</code> is unknown in the cache.</p>
     *
     * @param entryKey  the cache entry key, <code>null</code> is not allowed
     */
    public void remove (K entryKey) {
        internalRemove (entryKey);

    }

    /** <p>Return true if there are no entries in the cache.</p>
     *
     *  @return  true when the cache is empty
     */
    final public boolean isEmpty () {
        return cacheImplementation.isEmpty ();
    }    
    

    /** <p>Return the current number of entries in the cache.</p>
     *
     * @return  the current cache size
     */
    final public int size () {
        return cacheImplementation.size ();
    }

    /**
     * Get the number of groups for this cache. This is an approximate value
     * as it is not synchronized.
     * @return a long representing the number of elements in the cache groups
     * LRUMap.
     */
    final public long getGroupsSize() {
        return cacheImplementation.getGroupsSize();
    }

    /**
     * Get the total number of keys in all the groups of this cache. This is
     * an approximate value as it is not cached.
     * @return a long representing the number of keys in all the groups.
     */
    final public long getGroupsKeysTotal() {
        return cacheImplementation.getGroupsKeysTotal();
    }


    /** <p>Retrieves the cache name.</p>
     *
     * @return  the cache region name
     */
    final public String getName () {
        return name;
    }


    /** <p>Flushs all the cache entries. By sending the flushing event to all the
     * cache's listeners.</p>
     */
    public final void flush () {
        flush (true);
    }

    /** <p>Flushs all the cache entries. When <code>sendToListeners</code> is
     * <code>true</code>, the event is send to the cache's listeners.</p>
     */
    public void flush(boolean propagate) {

		// clears the cache
		cacheImplementation.flushAll(propagate);

		// reset the cache statistics
		successHitCount = 0;
		totalHitCount = 0;

        logger.debug("Flushed all entries from cache [{}]", name);

        // when requested, propagate the flush event to the cache listeners and the JMS
        if (propagate) {

            // call the listeners if present
            if ((listeners != null) && (listeners.size() > 0)) {
                for (int i = 0; i < listeners.size(); i++) {
                    CacheListener listener = (CacheListener) listeners.get(i);
                    if (listener != null)
                        listener.onCacheFlush(name);
                }
            }

        } else {
            logger.debug("Got cache flush request without event propagation");
        }


    }

    final public void flushGroup(String groupName) {
        cacheImplementation.flushGroup(groupName);
    }


    /** <p>Retrieves the number of cache hits that could successfully be served.</p>
     *
     * @return  the number of cache hits that could successfully be served
     */
    final public long getSuccessHits () {
        return successHitCount;
    }


    /** <p>Retrieves the total number of cache hits.</p>
     *
     * @return  the number of cache hits
     */
    final public long getTotalHits () {
        return totalHitCount;
    }


    /** <p>Retrieves the percentage of cache hit that could successfully be served.
     * This efficiency is useful for tuning the maximum cache size. Cache size should
     * be changed increased for low efficiency percentages.</p>
     *
     * @return  the percentage of cache hit that could successfully be served
     */
    public double getCacheEfficiency () {
        return (successHitCount * 100.0) / totalHitCount;
    }

    /** Checks if the specified <code>entryKey</code> is present in the cache.
     *
     * @param entryKey  the entry key to be checked.
     *
     * @return  <code>true</code> when the entry key is present in the cache,
     *           otherwise return <code>false</code>
     */
    final public boolean containsKey (final K entryKey) {
        if (cacheImplementation.containsKey (entryKey)) {
            // we must now check that the object has not expired.
            if (getCacheEntry(entryKey) == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Entry " + entryKey + " has expired. containsKey will return false.");
                }
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
    
    /** Returns a Collection of all cache keys at the moment (may change in the next moment)
     * Warning, if expiration times were set, they are NOT checked by this method, so make
     * sure you perform a get and check that it is not null when using this method.
     * @return  Collection of key objects in the cache
     */
   final public Collection<K> getKeys () {
       return cacheImplementation.getKeys();
   }

    /** <p>Remove the cache entry associated to the <code>entryKey</code>. This method
     * <b>does not</b> take any synchronization actions.</p>
     *
     * @param entryKey  the cache entry's key to be removed
     */
    private void internalRemove (K entryKey) {
        if (entryKey == null)
            return;

        // remove the object from the cache
        cacheImplementation.remove (entryKey);

        if (logger.isDebugEnabled()) {
            logger.debug ("Removed the entry [" + entryKey.toString () +
                          "] from cache [" + name + "]!");
        }
    }

    /**
     * <p>Register a new cache listener.</p>
     * <p>When the specified <code>listener</code> is <code>null</code> or already present
     * within the listeners list, the registration process is ignored.</p>
     *
     * @param listener  the reference of the cache listener to register.
     * @since  Jahia 4.0.2
     */
    public synchronized void registerListener (CacheListener listener) {
        if (listener == null)
            return;

        if (listeners == null) {
            listeners = new ArrayList<CacheListener>();

        } else if (listeners.contains (listener)) {
            return;
        }

        listeners.add (listener);
        cacheImplementation.addListener(listener);
    }

    /**
     * <p>Unregister a cache listener.</p>
     * <p>When there is not cache listener registered, or the specified <code>listener</code>
     * is <code>null</code>, the unregistration process is ignored.</p>
     *
     * @param listener  the reference of the cache listener to register
     * @since  Jahia 4.0.2
     */
    public synchronized void unregisterListener (CacheListener listener) {
        if ((listeners == null) || (listener == null))
            return;

        listeners.remove (listener);
        cacheImplementation.removeListener(listener);
    }

    public CacheImplementation<K, CacheEntry<V>> getCacheImplementation() {
        return cacheImplementation;
    }
}
