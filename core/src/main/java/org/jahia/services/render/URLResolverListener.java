/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import net.sf.ehcache.Element;

import org.apache.jackrabbit.core.JahiaRepositoryImpl;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.impl.jackrabbit.SpringJackrabbitRepository;
import org.jahia.services.render.filter.cache.CacheClusterEvent;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * JCR listener to invalidate URL resolver caches
 */
public class URLResolverListener extends DefaultEventListener {

    private static Logger logger = LoggerFactory.getLogger(URLResolverListener.class);

    private URLResolverFactory urlResolverFactory;
    private VanityUrlService vanityUrlService;
    private ModuleCacheProvider moduleCacheProvider;

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.NODE_MOVED + Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED + Event.PROPERTY_REMOVED;
    }

    public void onEvent(final EventIterator events) {
        if (urlResolverFactory == null) {
            return;
        }
        try {
            Set<String> pathsToFlush = null;
            boolean flushVanityUrlCache = false;
            while (events.hasNext()) {
                Event event = events.nextEvent();

                if (isExternal(event)) {
                    continue;
                }

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
            List<Element> syncEvents = new LinkedList<Element>();
            // Matching Permissions cache is not a selfPopulating Replicated cache so we need to send a command
            // to flush it across the cluster
            if (pathsToFlush != null) {
                for (String path : pathsToFlush) {
                    syncEvents.add(new Element("FLUSH_URLRESOLVER-" + UUID.randomUUID(),
                    // Create an empty CacheClusterEvent to be executed after next Journal sync
                            new CacheClusterEvent(path, getClusterRevision())));
                }
            }
            if (flushVanityUrlCache) {
                syncEvents.add(new Element("FLUSH_VANITYURL-" + UUID.randomUUID(),
                // Create an empty CacheClusterEvent to be executed after next Journal sync
                        new CacheClusterEvent("", getClusterRevision())));
            }
            moduleCacheProvider.getSyncCache().putAll(syncEvents);
        }
    }

    private static long getClusterRevision() {
        return ((JahiaRepositoryImpl) ((SpringJackrabbitRepository) JCRSessionFactory.getInstance().getDefaultProvider().getRepository()).getRepository()).getContext().getClusterNode().getRevision();
    }

    public void setModuleCacheProvider(ModuleCacheProvider moduleCacheProvider) {
        this.moduleCacheProvider = moduleCacheProvider;
    }

}
