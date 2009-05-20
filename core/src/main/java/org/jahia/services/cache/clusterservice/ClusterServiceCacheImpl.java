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

import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.CacheListener;
import org.jahia.services.cluster.ClusterService;
import org.jahia.services.cluster.ClusterMessage;

import java.util.Collection;
import java.util.Set;

/**
 * User: Serge Huber
 * Date: Jul 26, 2005
 * Time: 6:44:28 PM
 * Copyright (C) Jahia Inc.
 * Now uses a underlyingCacheImplementation to implement the real caching, so that we can make it pluggeable.
 */
public class ClusterServiceCacheImpl implements CacheImplementation {

    protected final static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ClusterServiceCacheProvider.class);

    ClusterService clusterService;
    CacheImplementation underlyingCacheImplementation;

    public ClusterServiceCacheImpl(String name, ClusterService clusterService, CacheImplementation underlyingCacheImplementation) {
        this.clusterService = clusterService;
        this.underlyingCacheImplementation = underlyingCacheImplementation;
        this.underlyingCacheImplementation.setName(name);
    }

    /**
     * Puts an entry in the cache, using optional groups
     *
     * @param key
     * @param groups the groups this entry should be part of, or null if it's part of no group.
     * @param value
     */
    public void put(Object key, String[] groups, Object value) {
        boolean newEntry = true;
        if (underlyingCacheImplementation.get(key) != null) {
            newEntry = false;
        }
        underlyingCacheImplementation.put(key, groups, value);
        if (!newEntry) {
            clusterService.sendMessage(new ClusterMessage(new ClusterCacheMessage(getName(), ClusterCacheMessage.INVALIDATEENTRY_EVENT, key, null)));
        }
    }

    public void flushAll(boolean propagate) {
    	underlyingCacheImplementation.flushAll(propagate);
        if (propagate) {
            clusterService.sendMessage(new ClusterMessage(new ClusterCacheMessage(getName(), ClusterCacheMessage.FLUSH_EVENT, null, null)));
        }
    }

    public void flushGroup(String groupName) {
    	underlyingCacheImplementation.flushGroup(groupName);
        clusterService.sendMessage(new ClusterMessage(new ClusterCacheMessage(getName(), ClusterCacheMessage.FLUSHGROUP_EVENT, null, groupName)));
    }

    public void remove(Object key) {
    	underlyingCacheImplementation.remove(key);
        clusterService.sendMessage(new ClusterMessage(new ClusterCacheMessage(getName(), ClusterCacheMessage.REMOVE_EVENT, key, null)));
    }

    protected void onFlush(ClusterCacheMessage clusterCacheMessage) {
        if (logger.isDebugEnabled()) {
            logger.debug(getName() + " cache event received, flushing local cache");
        }
        underlyingCacheImplementation.flushAll(false);
    }

    protected void onFlushGroup(ClusterCacheMessage clusterCacheMessage) {
        if (logger.isDebugEnabled()) {
            logger.debug(getName() + " cache event received, flushing local cache group " + clusterCacheMessage.getGroupName());
        }
        underlyingCacheImplementation.flushGroup(clusterCacheMessage.getGroupName());
    }

    protected void onInvalidateEntry(ClusterCacheMessage clusterCacheMessage) {
        if (logger.isDebugEnabled()) {
            logger.debug(getName() + " cache event received, invalidating value for key " + clusterCacheMessage.getEntryKey());
        }
        underlyingCacheImplementation.remove(clusterCacheMessage.getEntryKey());
    }

    protected void onRemove(ClusterCacheMessage clusterCacheMessage) {
        if (logger.isDebugEnabled()) {
            logger.debug(getName() + " cache event received, removing key " + clusterCacheMessage.getEntryKey());
        }
        underlyingCacheImplementation.remove(clusterCacheMessage.getEntryKey());
    }

	public void addListener(CacheListener listener) {
		underlyingCacheImplementation.addListener(listener);
	}

	public boolean containsKey(Object key) {
		return underlyingCacheImplementation.containsKey(key);
	}

	public Object get(Object key) {
		return underlyingCacheImplementation.get(key);
	}

	public long getCacheLimit() {
		return underlyingCacheImplementation.getCacheLimit();
	}

	public String getName() {
		return underlyingCacheImplementation.getName();
	}

	public void removeListener(CacheListener listener) {
		underlyingCacheImplementation.removeListener(listener);
	}

	public void setCacheLimit(long limit) {
		underlyingCacheImplementation.setCacheLimit(limit);
	}

    public long getCacheGroupsLimit() {
        return underlyingCacheImplementation.getCacheGroupsLimit();
    }

    public void setCacheGroupsLimit(long groupsLimit) {
        underlyingCacheImplementation.setCacheGroupsLimit(groupsLimit);
    }

    public void setName(String name) {
		underlyingCacheImplementation.setName(name);
	}
	
    public boolean isEmpty() {
        return underlyingCacheImplementation.isEmpty();
    }   

	public int size() {
		return underlyingCacheImplementation.size();
	}

    public long getGroupsSize() {
        return underlyingCacheImplementation.getGroupsSize();
    }

    public long getGroupsKeysTotal() {
        return underlyingCacheImplementation.getGroupsKeysTotal();
    }

    public Set getGroupKeys(String groupName) {
        return underlyingCacheImplementation.getGroupKeys(groupName);
    }
    
    public Collection<Object> getKeys() {
        return underlyingCacheImplementation.getKeys();
    }
}
