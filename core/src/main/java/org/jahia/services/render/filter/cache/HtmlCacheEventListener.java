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

package org.jahia.services.render.filter.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.observation.EventImpl;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.ExternalEventListener;
import org.jahia.services.content.JCREventIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Output cache invalidation listener.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 * Created : 12 janv. 2010
 */
public class HtmlCacheEventListener extends DefaultEventListener implements ExternalEventListener {
    private static Logger logger = LoggerFactory.getLogger(HtmlCacheEventListener.class);

    private ModuleCacheProvider cacheProvider;
    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED + Event.NODE_MOVED + Event.NODE_REMOVED;
    }

    /**
     * This method is called when a bundle of events is dispatched.
     *
     * @param events The event set received.
     */
    public void onEvent(EventIterator events) {
        final Cache depCache = cacheProvider.getDependenciesCache();
        final Cache regexpDepCache = cacheProvider.getRegexpDependenciesCache();
        final Set<String> flushed = new HashSet<String>();
        while (events.hasNext()) {
            Event event = (Event) events.next();
            boolean propageToOtherClusterNodes = !isExternal(event);
            try {
                String path = event.getPath();
                if (!path.startsWith("/jcr:system")) {
                    boolean flushParent = false;
                    boolean flushRoles = false;
                    if (path.contains("j:view")) {
                        flushParent = true;
                    }
                    final int type = event.getType();
                    if (type == Event.PROPERTY_ADDED || type == Event.PROPERTY_CHANGED || type == Event.PROPERTY_REMOVED) {
                        if (path.endsWith("/j:published")) {
                            flushParent = true;
                        }
                        if(path.endsWith("j:roles")) {
                            flushRoles = true;
                        }
                        path = path.substring(0, path.lastIndexOf("/"));
                    } else if (type == Event.NODE_ADDED || type == Event.NODE_MOVED || type == Event.NODE_REMOVED) {
                        flushParent = true;
                    }
                    if(path.contains("vanityUrlMapping")) {
                        flushParent=true;
                    }
                    if (path.contains("j:acl") || path.contains("jnt:group") || flushRoles || type == Event.NODE_MOVED) {
                        // Flushing cache of acl key for users as a group or an acl has been updated
                        CacheKeyGenerator cacheKeyGenerator = cacheProvider.getKeyGenerator();
                        if (cacheKeyGenerator instanceof DefaultCacheKeyGenerator) {
                            DefaultCacheKeyGenerator generator = (DefaultCacheKeyGenerator) cacheKeyGenerator;
                            generator.flushUsersGroupsKey(propageToOtherClusterNodes);
                        }
                        flushParent = true;
                    }
                    path = StringUtils.substringBeforeLast(StringUtils.substringBeforeLast(path, "/j:translation"), "/j:acl");
                    flushDependenciesOfPath(depCache, flushed, path, propageToOtherClusterNodes);
                    try {
                        flushDependenciesOfPath(depCache, flushed,((JCREventIterator)events).getSession().getNode(path).getIdentifier(), propageToOtherClusterNodes);
                    } catch (PathNotFoundException e) {
                        if(event instanceof EventImpl && (((EventImpl) event).getChildId() != null)) {
                            flushDependenciesOfPath(depCache, flushed,((EventImpl)event).getChildId().toString(), propageToOtherClusterNodes);
                        }
                    }
                    flushRegexpDependenciesOfPath(regexpDepCache, path, propageToOtherClusterNodes);
                    if (flushParent) {
                        path = StringUtils.substringBeforeLast(path, "/");
                        flushDependenciesOfPath(depCache, flushed, path, propageToOtherClusterNodes);
                        try {
                            flushDependenciesOfPath(depCache, flushed,((JCREventIterator)events).getSession().getNode(path).getIdentifier(), propageToOtherClusterNodes);
                        } catch (PathNotFoundException e) {
                            if (event instanceof EventImpl  && (((EventImpl) event).getParentId() != null)) {
                                flushDependenciesOfPath(depCache, flushed, ((EventImpl) event).getParentId().toString(),
                                        propageToOtherClusterNodes);
                            }
                        }
                        flushRegexpDependenciesOfPath(regexpDepCache,path, propageToOtherClusterNodes);
                    }
                }

            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

    private void flushDependenciesOfPath(Cache depCache, Set<String> flushed, String path, boolean propageToOtherClusterNodes) {
        if (logger.isDebugEnabled()) {
            logger.debug("Flushing dependencies for path : " + path);
        }
        Element element = !flushed.contains(path) ? depCache.get(path) : null;
        if (element != null) {
            flushed.add(path);
            if (logger.isDebugEnabled()) {
                logger.debug("Flushing path : " + path);
            }
            cacheProvider.invalidate(path, propageToOtherClusterNodes);
            depCache.remove(element.getKey());
        }
    }

    private void flushRegexpDependenciesOfPath(Cache depCache, String path, boolean propageToOtherClusterNodes) {
        if (logger.isDebugEnabled()) {
            logger.debug("Flushing dependencies for path : " + path);
        }
        @SuppressWarnings("unchecked")
        List<String> keys = depCache.getKeys();
        for (String key : keys) {
            if(path.matches(key)) {
                cacheProvider.invalidateRegexp(key, propageToOtherClusterNodes);
            }
        }

    }

    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }
}
