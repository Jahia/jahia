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
