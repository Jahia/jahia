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
package org.jahia.services.cache.jboss.cache;

import org.jahia.services.cache.CacheListener;
import org.jboss.cache.Fqn;
import org.jboss.cache.TreeCache;
import org.jboss.cache.TreeCacheListener;
import org.jgroups.View;

public class TreeCacheListenerWrapper implements TreeCacheListener {
    private CacheListener cacheListener = null;

    /**
     * @return Returns the cacheListener.
     */
    private CacheListener getCacheListener() {
        return cacheListener;
    }

    public TreeCacheListenerWrapper(CacheListener listener) {
        super();

        cacheListener = listener;
    }

    public void nodeCreated(Fqn arg0) {
        // cacheListener.onCachePut((String) arg0.get(0), arg0.size() > 1 ?
        // arg0.get(1) : null, null);
    }

    public void nodeRemoved(Fqn arg0) {
        // cacheListener.onCacheFlush((String) arg0.get(0));
    }

    public void nodeLoaded(Fqn arg0) {

    }

    public void nodeEvicted(Fqn arg0) {
    }

    public void nodeModified(Fqn arg0) {
/*        String cacheName = (String) arg0.get(0);
        Object entryKey = arg0.size() > 1 ? arg0.get(1) : null;
        Object entryValue = null;
        if (entryKey != null) {
            Cache cache = TreeCacheProvider.getCache(TreeCacheProvider.getInstance(), cacheName);
            if (cache != null) {
                entryValue = cache.getCacheEntry(entryKey);
            }
        }
        cacheListener.onCachePut(cacheName, entryKey, entryValue);
*/
    }

    public void nodeVisited(Fqn arg0) {
    }

    public void cacheStarted(TreeCache arg0) {
    }

    public void cacheStopped(TreeCache arg0) {
    }

    public void viewChange(View arg0) {
    }

    public int hashCode() {
        return cacheListener.hashCode();
    }

    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (obj instanceof TreeCacheListenerWrapper) {
            isEqual = cacheListener.equals(((TreeCacheListenerWrapper) obj).getCacheListener());
        }
        return isEqual;
    }
}
