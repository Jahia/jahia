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
 package org.jahia.services.cache;

import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: Jul 12, 2005
 * Time: 3:31:00 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CacheImplementation<K, V> {
    boolean containsKey(K key);

    V get(K key);

    /**
     * Puts an entry in the cache, using optional groups
     * @param key
     * @param groups the groups this entry should be part of, or null if it's part of no group.
     * @param value
     */
    void put(K key, String[] groups, V value);

    boolean isEmpty();    
    
    int size();
    
    Collection<K> getKeys();    

    /**
     * Get the number of groups for this cache. This is an approximate value
     * as it is not synchronized.
     * @return a long representing the number of elements in the cache groups
     * LRUMap.
     */
    public long getGroupsSize();

    /**
     * Get the total number of keys in all the groups of this cache. This is
     * an approximate value as it is not cached.
     * @return a long representing the number of keys in all the groups.
     */    
    public long getGroupsKeysTotal();

    void flushAll(boolean propagate);

    void flushGroup(String groupName);

    void remove(K key);

    public String getName();
    
    public void setName(String name);

    void addListener(CacheListener listener);
    void removeListener(CacheListener listener);

    /** <p>Returns the maximum size allowed for the cache.</p>
     *
     * @return  an integer representing the maximum cache size. Returns -1 if
     *           there is no limit set.
     */
    long getCacheLimit ();

    /** <p>Set the cache size limit. -1 will define an unlimited cache size.</p>
     *
     * @param limit     the new size limit
     */
    void setCacheLimit (long limit);

    /** <p>Returns the maximum size allowed for the cache groups.</p>
     *
     * @return  an integer representing the maximum cache groups size. Returns -1 if
     *           there is no limit set.
     */
    long getCacheGroupsLimit ();


    /** <p>Set the cache groups size limit. -1 will define an unlimited cache size.</p>
     *
     * @param groupsLimit the new size limit
     */
    void setCacheGroupsLimit (long groupsLimit);

    Set<GroupCacheKey> getGroupKeys(String groupName);
}
