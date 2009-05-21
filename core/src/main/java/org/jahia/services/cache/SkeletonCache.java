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
package org.jahia.services.cache;

import org.apache.log4j.Category;
import static org.apache.log4j.Logger.getLogger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.params.ProcessingContext;
import org.jahia.content.ObjectKey;
import org.jahia.settings.SettingsBean;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by IntelliJ IDEA. User: Serge Huber Date: 26 mars 2007 Time: 12:09:55 To change this template use File |
 * Settings | File Templates.
 */
public class SkeletonCache<K, V> extends Cache<GroupCacheKey, SkeletonCacheEntry> {
// ------------------------------ FIELDS ------------------------------

    // the Container HTML cache name.
    public static final String SKELETON_CACHE = "SkeletonCache";

    private static final Category logger = getLogger(ContainerHTMLCache.class);

    private static final int INITIAL_CAPACITY = 1024;

    private final CacheKeyGeneratorService cacheKeyGeneratorService;

    private ConcurrentMap<GroupCacheKey, String> concurrentHashMap =
            new ConcurrentHashMap<GroupCacheKey, String>(INITIAL_CAPACITY);

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * <p>Creates a new <code>ContainerHTMLCache</code> instance.</p>
     */
    protected SkeletonCache(CacheImplementation<GroupCacheKey, CacheEntry<SkeletonCacheEntry>> cacheImplementation) {
        super(SKELETON_CACHE, cacheImplementation);
        cacheKeyGeneratorService = ServicesRegistry.getInstance().getCacheKeyGeneratorService();
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public ConcurrentMap<GroupCacheKey, String> getConcurrentHashMap() {
        return concurrentHashMap;
    }

// -------------------------- OTHER METHODS --------------------------

    public void flushSkeletonsForSite(int siteID) {
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            flushGroup(CacheKeyGeneratorService.SITE_PREFIX+siteID);
        }else {
            flushGroup(Integer.toString((CacheKeyGeneratorService.SITE_PREFIX+siteID).hashCode()));
        }
    }

    public void invalidateSkeletonEntries(String pageKey, String mode, String locale) {
        if (pageKey == null) {
            logger.debug("Cannot remove a null page ID from the cache!");
            return;
        }
        if(logger.isDebugEnabled())
        logger.debug("Removing cache entries for skeleton [" +
                     pageKey +
                     "], mode [" +
                     mode +
                     "], lang [" +
                     locale +
                     "]");

        String s = cacheKeyGeneratorService.getPageKey(pageKey, mode, locale);
        Set<GroupCacheKey> keys = getCacheImplementation().getGroupKeys(s);
        if (keys != null) {
            if(keys.contains("ALL")){
                flush();
                logger.warn("We are flushing all skeletons");
            }
            else {
                for (Iterator<GroupCacheKey> it = keys.iterator(); it.hasNext();) {
                    concurrentHashMap.putIfAbsent(it.next(), "");
                }
            }
        }
    }

    public void invalidateSkeletonEntriesInAllModes(String key, String locale){
        invalidateSkeletonEntries(key, ProcessingContext.NORMAL, locale);
        invalidateSkeletonEntries(key, ProcessingContext.EDIT, locale);
        invalidateSkeletonEntries(key, ProcessingContext.COMPARE, locale);
        invalidateSkeletonEntries(key, ProcessingContext.PREVIEW, locale);
    }

    public void flushPage(ObjectKey objectKey, List<Locale> languageSettingsAsLocales) {
        for (int i = 0; i < languageSettingsAsLocales.size(); i++) {
            Locale locale = languageSettingsAsLocales.get(i);
            invalidateSkeletonEntriesInAllModes(objectKey.toString(),locale.toString());
        }
    }
}