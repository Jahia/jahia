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

import java.util.Collection;
import java.util.Set;

/**
 * 
 * User: Serge Huber
 * Date: Jul 12, 2005
 * Time: 3:31:00 PM
 * 
 */
public interface CacheImplementation<K, V> {
    @Deprecated
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

    Set<GroupCacheKey> getGroupKeys(String groupName);
}
