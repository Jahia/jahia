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
package org.jahia.services.cache.ehcache;

import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.CacheListener;
import org.jahia.services.cache.GroupCacheKey;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 29 mars 2007
 * Time: 17:25:50
 * To change this template use File | Settings | File Templates.
 */
public class EhCacheImpl implements CacheImplementation {

    final private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger (EhCacheImpl.class);

    EhCacheProvider ehCacheProvider;
    String name;
    Cache ehCache;
    Cache ehCacheGroups;
    int groupsSizeLimit;

    protected EhCacheImpl(String name, CacheManager ehCacheManager, EhCacheProvider ehCacheProvider) {
        if (ehCacheManager.getCache(name) == null) {
            ehCacheManager.addCache(name);
        }
        if (ehCacheManager.getCache(name + "-groups") == null) {
            ehCacheManager.addCache(name + "-groups");
        }
        ehCache = ehCacheManager.getCache(name);
        ehCacheGroups = ehCacheManager.getCache(name + "-groups");
        this.name = name;
        this.ehCacheProvider = ehCacheProvider;
        groupsSizeLimit = ehCacheProvider.getGroupsSizeLimit();
    }

    public boolean containsKey(Object key) {
        // we cannot use EHCache's isKeyInCache because the element might have expired, so we use
        // an actual retrival to test for expiration.
        return get(key) != null;
    }

    public Object get(Object key) {
        Element element = ehCache.get(key);
        if (element != null) {    
            return element.getValue();
        } else {
            return null;
        }
    }

    public void put(Object key, String[] groups, Object value) {

        Element element = new Element(key, value);
        ehCache.put(element);

        if (key instanceof GroupCacheKey) {
            addToGroups(key);
        }
    }

    public int size() {
        return ehCache.getSize();
    }

    public long getGroupsSize() {
        return ehCacheGroups.getSize();
    }

    public long getGroupsKeysTotal() {
        long totalSize = 0;
        Iterator groupIterator = ehCacheGroups.getKeysWithExpiryCheck().iterator();
        while (groupIterator.hasNext()) {
            Object key = groupIterator.next();
            Element keySetElement = ehCacheGroups.get(key);
            if (keySetElement == null) continue;
            Set keySet = (Set) keySetElement.getValue();
            totalSize += keySet.size(); 
        }
        return totalSize;
    }

    public void flushAll(boolean propagate) {
        ehCache.removeAll();
        ehCacheGroups.removeAll();
    }

    public void remove(Object key) {
        doRemove(key);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addListener(CacheListener listener) {
    }

    public void removeListener(CacheListener listener) {
    }

    public long getCacheLimit() {
        return -1;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setCacheLimit(long limit) {
    }

    /**
     * Not supported.
     * @return
     */
    public long getCacheGroupsLimit() {
        return -1;
    }

    /**
     * Not supported.
     * @param groupsLimit
     */
    public void setCacheGroupsLimit(long groupsLimit) {
    }

    public void flushGroup(String groupName) {
        Set keysToFlush = null;
        Element element = ehCacheGroups.get(groupName);
        if (element == null) {
            return;
        }
        keysToFlush = (Set) element.getValue();
        if (keysToFlush == null) {
            return;
        } else {
            ehCacheGroups.remove(groupName);
            // we copy in order to avoid concurrent modifications
            keysToFlush = new HashSet(keysToFlush);
        }
        flushKeys(keysToFlush);
    }

    public Set getGroupKeys(String groupName) {
        Set keysToFlush = null;
        Element element = ehCacheGroups.get(groupName);
        if (element == null) {
            return null;
        }
        keysToFlush = (Set) element.getValue();
        if (keysToFlush == null) {
            return null;
        } else {
            // we copy in order to avoid concurrent modifications
            keysToFlush = new HashSet(keysToFlush);
        }
        return keysToFlush;
    }

    protected void flushKeys(Set keysToFlush) {
        if(keysToFlush.contains("ALL")) {
            flushAll(true);
            logger.warn("Due to presence of big groups we are flushing the whole cache "+ehCache.getName());
        } else {
            Iterator keyToFlushIter = keysToFlush.iterator();
            while (keyToFlushIter.hasNext()) {
                Object curKeyToFlush = keyToFlushIter.next();
                doRemove(curKeyToFlush);
            }
        }
    }

    private void addToGroups(Object key) {
        if (!(key instanceof GroupCacheKey)) {
            return;
        }
        GroupCacheKey groupCacheKey = (GroupCacheKey) key;
        synchronized (groupCacheKey) {
            Iterator groupIter = groupCacheKey.getGroups().iterator();
            while (groupIter.hasNext()) {
                String curGroup = (String) groupIter.next();
                addToGroup(curGroup, key);
            }
        }
    }

    private void addToGroup(String groupName, Object key) {
        Element element =  ehCacheGroups.get(groupName);
        Set currentKeys = null;
        if (element == null) {
            currentKeys = new HashSet();
        } else {
            currentKeys = (Set) element.getValue();
        }
        if (currentKeys == null) {
            currentKeys = new HashSet();
        }

        if(!currentKeys.contains("ALL") && currentKeys.size()<= groupsSizeLimit)
        currentKeys.add(key);
        if(currentKeys.size()> groupsSizeLimit) {
            currentKeys = new HashSet();
            currentKeys.add("ALL");
            logger.warn("Number of keys for group "+groupName+"inside cache "+ehCache.getName()+" is exceeding "+
                        groupsSizeLimit +" entries so we are putting only one entries to tell jahia to flush all this cache when needed");
        }
        element = new Element(groupName, currentKeys);
        ehCacheGroups.put(element);
    }

    private void removeFromGroup(String groupName, Object key) {
        Element element = ehCacheGroups.get(groupName);
        Set currentKeys = null;
        if (element != null) {
            currentKeys = (Set) element.getValue();            
        }
        if (currentKeys == null) {
            return;
        }
        currentKeys.remove(key);
    }

    private void removeFromAllGroups(Object key) {
        if (key.getClass() == GroupCacheKey.class) {
            GroupCacheKey curGroupCacheKey = (GroupCacheKey) key;
            synchronized(curGroupCacheKey) {
                Iterator groupIter = curGroupCacheKey.getGroups().iterator();
                while (groupIter.hasNext()) {
                    String curGroup = (String) groupIter.next();
                    removeFromGroup(curGroup, key);
                }
            }
        } else {
            // this is possible if the global LRU map is used to flush entries
        }
    }

    private boolean doRemove(Object key) {
        try {
            boolean removedObject = false;
            removedObject = ehCache.remove(key);
            if (removedObject) {
                removeFromAllGroups(key);
                return true;
            }
        } catch (Exception e) {
            logger.warn("Cannot remove cache entry " + key + " from cache " + toString() , e);
        }
        return false;
    }

    public boolean isEmpty() {
        return ehCache.getSize() == 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
    
    public Collection<Object> getKeys() {
        return ehCache.getKeys();
    }
}
