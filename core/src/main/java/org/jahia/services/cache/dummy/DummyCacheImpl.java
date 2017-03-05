/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.cache.dummy;

import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.GroupCacheKey;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * An implementation of the {@link CacheImplementation} that disables caching.
 * 
 * @author rincevent
 */
public class DummyCacheImpl<K, V> implements CacheImplementation<K, V> {

    private String name;

    public DummyCacheImpl(String name) {
        setName(name);
    }

    public boolean containsKey(Object key) {
        return false;  
    }

    public V get(Object key) {
        return null;  
    }

    public void put(Object key, String[] groups, Object value) {
        
    }

    public boolean isEmpty() {
        return true;  
    }

    public int size() {
        return 0;  
    }

    public Collection<K> getKeys() {
        return null;  
    }

    public long getGroupsSize() {
        return 0;  
    }

    public long getGroupsKeysTotal() {
        return 0;  
    }

    public void flushAll(boolean propagate) {
        
    }

    public void flushGroup(String groupName) {
        
    }

    public void remove(Object key) {
        
    }

    public String getName() {
        return name;  
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<GroupCacheKey> getGroupKeys(String groupName) {
        return Collections.emptySet(); 
    }
}
