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
package org.jahia.services.cache.clusterservice.batch;

import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.CacheListener;
import org.jahia.services.cache.clusterservice.ClusterServiceCacheProvider;
import org.jahia.services.cache.clusterservice.ClusterCacheMessage;

import java.util.Collection;
import java.util.Set;

/**
 * User: Serge Huber
 * Date: 8 mai 2006
 * Time: 11:30:48
 * Copyright (C) Jahia Inc.
 */
public class BatchingClusterServiceCacheImpl implements CacheImplementation {

    protected final static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ClusterServiceCacheProvider.class);

    ClusterCacheMessageBatcher batcher;
    CacheImplementation underlyingCacheImplementation;

    public BatchingClusterServiceCacheImpl(String name, ClusterCacheMessageBatcher batcher, CacheImplementation underlyingCacheImplementation) {
        this.batcher = batcher;
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
            batcher.addMessageToBatch(new ClusterCacheMessage(getName(), ClusterCacheMessage.INVALIDATEENTRY_EVENT, key, null));
        }
    }

    public void flushAll(boolean propagate) {
    	underlyingCacheImplementation.flushAll(propagate);
        if (propagate) {
            batcher.addMessageToBatch(new ClusterCacheMessage(getName(), ClusterCacheMessage.FLUSH_EVENT, null, null));
        }
    }

    public void flushGroup(String groupName) {
    	underlyingCacheImplementation.flushGroup(groupName);
        batcher.addMessageToBatch(new ClusterCacheMessage(getName(), ClusterCacheMessage.FLUSHGROUP_EVENT, null, groupName));
    }

    public void remove(Object key) {
    	underlyingCacheImplementation.remove(key);
        batcher.addMessageToBatch(new ClusterCacheMessage(getName(), ClusterCacheMessage.REMOVE_EVENT, key, null));
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

    public long getCacheGroupsLimit() {
        return underlyingCacheImplementation.getCacheGroupsLimit();
    }

    public void setCacheGroupsLimit(long groupsLimit) {
        underlyingCacheImplementation.setCacheGroupsLimit(groupsLimit);
    }

    public Set getGroupKeys(String groupName) {
        return underlyingCacheImplementation.getGroupKeys(groupName);
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
    
    public Collection<Object> getKeys() {
        return underlyingCacheImplementation.getKeys();
    }

}
