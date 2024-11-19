/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.services.content.files;

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * File cache accessor and controller.
 *
 * @author Sergiy Shyrkov
 */
public class FileCacheManager {

    public static final String CONTENT_CACHE_NAME = "FileContentCache";

    public static final String LAST_MODIFIED_CACHE_NAME = "FileLastModifiedCache";

    private static Logger logger = LoggerFactory.getLogger(FileCacheManager.class);
    private CacheFactory cacheFactory;


    public static FileCacheManager getInstance() {
        return (FileCacheManager) SpringContextSingleton.getBean("FileCacheManager");
    }

    private Cache<String, Map<String, FileCacheEntry>> contentCache;

    private Cache<String, FileLastModifiedCacheEntry> lastModifiedCache;

    private FileCacheManager() {
        super();
    }


    public Cache<String, Map<String, FileCacheEntry>> getContentCache() {
        if (contentCache == null) {
            try {
                contentCache = cacheFactory.getCache(CONTENT_CACHE_NAME, true);
            } catch (JahiaInitializationException e) {
                logger.error(e.getMessage(), e);
                throw new JahiaRuntimeException(e);
            }
        }

        return contentCache;

    }

    public Cache<String, FileLastModifiedCacheEntry> getLastModifiedCache() {
        if (lastModifiedCache == null) {
            try {
                lastModifiedCache = cacheFactory.getCache(LAST_MODIFIED_CACHE_NAME, true);
            } catch (JahiaInitializationException e) {
                logger.error(e.getMessage(), e);
                throw new JahiaRuntimeException(e);
            }
        }

        return lastModifiedCache;
    }

    public void invalidate(FileKey key) {
        getContentCache().remove(key.getCacheKey());
        getLastModifiedCache().remove(key.getCacheKey());
    }

    public void invalidate(String workspace, String nodePath) {
        invalidate(new FileKey(workspace, nodePath));
    }

    public void setCacheFactory(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
    }
}
