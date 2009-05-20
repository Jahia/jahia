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


/**
 * This interface specifies the methods a Cache Listeners has to implement in order to be registered
 * as a listener into a Cache instance.
 *
 * @author Fulco Houkes
 * @since  Jahia 4.0.2
 */
public interface CacheListener {

    /**
     * This method is called each time the cache flushes its items.
     * Warning : no calls to flush should be done in this method or this will
     * result in recursive calls !
     *
     * @param cacheName     the name of the cache which flushed its items.
     */
    public void onCacheFlush (String cacheName);

}
