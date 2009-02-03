/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

//
//  JahiaSearchService
//  NK		25.01.2002 Implementation based on Lucene engine.
//
//
package org.jahia.services.search;


import java.util.*;

import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.cache.CacheListener;

/**
 * Support load balancing using Jahia's cache system
 *
 */
public class SearchHub extends SearchEventListenerImpl implements CacheListener {

    public static final String SEARCH_CACHE_PREFIX = "SEARCH_CACHE";

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (SearchHub.class);

    private Date startingDate;

    // Cache used as synchronized Hub in a Load Balanced environment
    private Cache<String, CacheableDocumentProxy> cache;

    private String cacheName;

    private List<String> toRemoveDocs = new ArrayList<String>();

    public SearchHub (Cache<String, CacheableDocumentProxy> cache , String cacheName) {
        this(cache,cacheName,null);
    }

    public SearchHub (Cache<String, CacheableDocumentProxy> cache , String cacheName, SearchHandler searchHandler) {
        super(searchHandler);
        this.cache = cache;
        this.cacheName = cacheName;
        if ( this.cache != null ){
            this.cache.registerListener(this);
        }
        this.startingDate = new Date();
    }

    public Cache<String, CacheableDocumentProxy> getCache() {
        return cache;
    }

    public void setCache(Cache<String, CacheableDocumentProxy> cache) {
        this.cache = cache;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    /**
     * Cache Listener implementation
     *
     * @param cacheName String
     */
    public void onCacheFlush (String cacheName){

    }

    /**
     * Cache Listener implementation
     *
     * On cache put, new cache entry in the local indexOrders List.
     *
     * @param cacheName
     * @param entryKey
     * @param entryValue
     */
    public synchronized void onCachePut (String cacheName,
                                         Object entryKey,
                                         Object entryValue){
        // TODO : Convert this so that it uses the cluster service instead, since we no
        // longer offer a replicating cache service.
        logger.debug("Search Cache listener :" + cacheName + entryKey.toString());
        if ( this.getCacheName().equals(cacheName)
             && entryKey != null && entryValue != null ){
            CacheEntry<?> cacheEntry = (CacheEntry<?>)entryValue;
            CacheableDocumentProxy doc =(CacheableDocumentProxy)cacheEntry.getObject();
            SearchHandler searchHandler = this.getSearchHandler();
            if ( searchHandler != null ){
                this.toRemoveDocs.add(doc.getCacheKey());
                if ( doc.getDoc() instanceof RemovableDocument ){
                   searchHandler.removeDocument((RemovableDocument)doc);
                } else {
                    searchHandler.addDocument((IndexableDocument)doc);
                }
            }
        }
    }

    /** Search Event Listener **/
    public void addDocument( SearchEvent ev ){
        this.removeDocument(ev);
    }

    public void removeDocument( SearchEvent ev ){
        if ( this.cache != null && ev != null ){
            IndexableDocument doc =(IndexableDocument)ev.getData();
            if ( doc instanceof CacheableDocumentProxy ){
                String cacheKey = ((CacheableDocumentProxy)doc).getCacheKey();
                if ( this.toRemoveDocs.contains(cacheKey) ){
                    this.cache.remove(cacheKey);
                    this.toRemoveDocs.remove(cacheKey);
                }
            } else {
                CacheableDocumentProxy docProxy =
                    (CacheableDocumentProxy)CacheableDocumentProxy.newInstance(doc);
                docProxy.setDate(new Date());
                docProxy.setServerDate(this.startingDate);
                this.cache.put(docProxy.getCacheKey(),docProxy);
            }
        }
    }

    public void search( SearchEvent ev ){
    }


}
