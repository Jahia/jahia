/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.files;

import java.util.Map;

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File cache accessor and controller.
 * 
 * @author Sergiy Shyrkov
 */
public class FileCacheManager {

    public static final String CONTENT_CACHE_NAME = "FileContentCache";

    private static FileCacheManager instance;

    public static final String LAST_MODIFIED_CACHE_NAME = "FileLastModifiedCache";
    
    private static Logger logger = LoggerFactory.getLogger(FileCacheManager.class);

    public static FileCacheManager getInstance() {
        if (instance == null) {
            synchronized (FileCacheManager.class) {
                if (instance == null) {
                    instance = new FileCacheManager();
                }
            }
        }

        return instance;
    }

    private Cache<String, Map<String, FileCacheEntry>> contentCache;

    private Cache<String, FileLastModifiedCacheEntry> lastModifiedCache;
    
    private FileCacheManager() {
        super();
    }
    

    public Cache<String, Map<String, FileCacheEntry>> getContentCache() {
        if (contentCache == null) {
            try {
                contentCache = CacheFactory.getInstance().getCache(CONTENT_CACHE_NAME, true);
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
                lastModifiedCache = CacheFactory.getInstance().getCache(LAST_MODIFIED_CACHE_NAME, true);
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
}
