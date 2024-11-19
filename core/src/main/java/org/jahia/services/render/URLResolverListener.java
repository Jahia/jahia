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
package org.jahia.services.render;

import static org.jahia.services.cache.CacheHelper.*;

import org.jahia.services.content.ApiEventListener;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.jahia.services.seo.jcr.VanityUrlManager;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * JCR listener to invalidate URL resolver caches
 */
public class URLResolverListener extends DefaultEventListener implements ApiEventListener {

    private static final Logger logger = LoggerFactory.getLogger(URLResolverListener.class);

    private URLResolverFactory urlResolverFactory;
    private VanityUrlService vanityUrlService;

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.NODE_MOVED + Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED + Event.PROPERTY_REMOVED;
    }

    @Override
    public void onEvent(final EventIterator events) {
        if (urlResolverFactory == null) {
            return;
        }
        try {
            Set<String> pathsToFlush = null;
            boolean flushVanityUrlCache = false;
            while (events.hasNext()) {
                Event event = events.nextEvent();

                String path = event.getPath();
                if (event.getType() == Event.NODE_ADDED || event.getType() == Event.NODE_REMOVED || event.getType() == Event.NODE_MOVED ||
                        path.endsWith("/j:published") || path.contains("/vanityUrlMapping/")) {
                    if (event.getType() == Event.PROPERTY_CHANGED || event.getType() == Event.PROPERTY_ADDED || event.getType() == Event.PROPERTY_REMOVED) {
                        path = path.substring(0, path.lastIndexOf("/"));
                        int pos = path.lastIndexOf("/");
                        if (pos != -1 && path.substring(pos, path.length()).startsWith("/j:translation_")) {
                            path = path.substring(0, pos);
                        }
                    }
                    flushVanityUrlCache = flushVanityUrlCache || path.contains(VanityUrlManager.VANITYURLMAPPINGS_NODE);
                    if (pathsToFlush == null) {
                        pathsToFlush = new LinkedHashSet<String>();
                    }
                    pathsToFlush.add(path);
                }
            }
            flushCaches(pathsToFlush, flushVanityUrlCache);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    public void setUrlResolverFactory(URLResolverFactory urlResolverFactory) {
        this.urlResolverFactory = urlResolverFactory;
    }

    public void setVanityUrlService(VanityUrlService vanityUrlService) {
        this.vanityUrlService = vanityUrlService;
    }

    private void flushCaches(Set<String> pathsToFlush, boolean flushVanityUrlCache) {
        if (pathsToFlush != null) {
            urlResolverFactory.flushCaches(pathsToFlush);
        }
        if (flushVanityUrlCache) {
            vanityUrlService.flushCaches();
        }
        if ((pathsToFlush != null || flushVanityUrlCache) && SettingsBean.getInstance().isClusterActivated()) {
            // Matching Permissions cache is not a selfPopulating Replicated cache so we need to send a command
            // to flush it across the cluster
            if (pathsToFlush != null) {
                sendMultipleCacheFlushCommandsToCluster(CMD_FLUSH_URLRESOLVER, pathsToFlush);
            }
            if (flushVanityUrlCache) {
                sendCacheFlushCommandToCluster(CMD_FLUSH_VANITYURL);
            }
        }
    }

    @Deprecated
    public void setModuleCacheProvider(ModuleCacheProvider moduleCacheProvider) {
     // deprecated since 7.2.3.1 as not used
    }
}
