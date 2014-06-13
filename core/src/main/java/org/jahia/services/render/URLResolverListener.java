/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.render;

import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.JahiaRepositoryImpl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.JCRObservationManager;
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
                    flushCaches(path);
                    return;
                }
            }
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

    private void flushCaches(String path) throws RepositoryException {
        urlResolverFactory.flushCaches(path);
        boolean clusterActivated = SettingsBean.getInstance().isClusterActivated();
        if (clusterActivated) {
            // Matching Permissions cache is not a selfPopulating Replicated cache so we need to send a command
            // to flush it across the cluster
            moduleCacheProvider.getSyncCache().put(new Element("FLUSH_URLRESOLVER-" + UUID.randomUUID(),
                            //Create an empty CacheClusterEvent to be executed after next Journal sync
                            new CacheClusterEvent(path, getClusterRevision())));
        }
        if (path.contains(VanityUrlManager.VANITYURLMAPPINGS_NODE)) {
            vanityUrlService.flushCaches();
            if (clusterActivated) {
                // Matching Permissions cache is not a selfPopulating Replicated cache so we need to send a command
                // to flush it across the cluster
                moduleCacheProvider.getSyncCache().put(new Element("FLUSH_VANITYURL-" + UUID.randomUUID(),
                                //Create an empty CacheClusterEvent to be executed after next Journal sync
                                new CacheClusterEvent("", getClusterRevision())));
            }
        }
    }

    private static long getClusterRevision() {
        return ((JahiaRepositoryImpl) ((SpringJackrabbitRepository) JCRSessionFactory.getInstance().getDefaultProvider().getRepository()).getRepository()).getContext().getClusterNode().getRevision();
    }

    public void setModuleCacheProvider(ModuleCacheProvider moduleCacheProvider) {
        this.moduleCacheProvider = moduleCacheProvider;
    }

}
