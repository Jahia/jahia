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
package org.jahia.services.render.filter.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.services.content.*;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

/**
 * Output cache invalidation listener.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 12 janv. 2010
 */
public class HtmlCacheEventListener extends DefaultEventListener {
    private transient static Logger logger = Logger.getLogger(HtmlCacheEventListener.class);

    private ModuleCacheProvider cacheProvider;
    private JCRTemplate jcrTemplate;

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED + Event.NODE_MOVED + Event.NODE_REMOVED;
    }

    @Override
    public String getPath() {
        return "/";
    }

    @Override
    public String[] getNodeTypes() {
        return null;
    }

    /**
     * This method is called when a bundle of events is dispatched.
     *
     * @param events The event set received.
     */
    public void onEvent(EventIterator events) {
        final Cache depCache = cacheProvider.getDependenciesCache();
//        final BlockingCache htmlCache = cacheProvider.getCache();
        final Set<String> flushed = new HashSet<String>();
        while (events.hasNext()) {
            Event event = (Event) events.next();
            try {
                String path = event.getPath();
                boolean flushParent = false;
                if(path.contains("j:template")) {
                    flushParent = true;
                }
                final int type = event.getType();
                if(type == Event.PROPERTY_ADDED || type == Event.PROPERTY_CHANGED || type == Event.PROPERTY_REMOVED) {
                    path = path.substring(0,path.lastIndexOf("/"));
                } else if (type==Event.NODE_ADDED || type == Event.NODE_MOVED || type == Event.NODE_REMOVED) {
                    flushParent = true;
                }
                if(path.contains("j:acl") || path.contains("jnt:group") || type == Event.NODE_MOVED) {
                    // Flushing cache of acl key for users as a group or an acl has been updated
                    CacheKeyGenerator cacheKeyGenerator = cacheProvider.getKeyGenerator();
                    if (cacheKeyGenerator instanceof DefaultCacheKeyGenerator) {
                        DefaultCacheKeyGenerator generator = (DefaultCacheKeyGenerator) cacheKeyGenerator;
                        generator.flushUsersGroupsKey();
                    }
                }
                path = StringUtils.substringBeforeLast(path, "/j:translation");
                flushDependenciesOfPath(depCache, flushed, path);
                flushSharedNode(depCache, flushed, path);
                if(flushParent) {
                    path = StringUtils.substringBeforeLast(path, "/");
                    flushDependenciesOfPath(depCache, flushed, path);
                    flushSharedNode(depCache, flushed, path);
                }


            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

    private void flushSharedNode(final Cache depCache, final Set<String> flushed, final String finalPath)
            throws RepositoryException {
        // Flushed shared node associated with this event
        jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    JCRNodeWrapper node = session.getNode(finalPath);
                    NodeIterator nodeIterator = node.getSharedSet();
                    while (nodeIterator.hasNext()) {
                        JCRNodeWrapper wrapper = (JCRNodeWrapper) nodeIterator.next();
                        flushDependenciesOfPath(depCache, flushed, wrapper.getPath());
                    }
                } catch (PathNotFoundException e) {
                    logger.trace(e.getMessage(), e);
                } catch (RepositoryException e) {
                    logger.debug(e.getMessage(), e);
                }
                return null;
            }
        });
    }

    private void flushDependenciesOfPath(Cache depCache, Set<String> flushed, String path) {
        Element element = !flushed.contains(path) ? depCache.get(path) : null;
        if(element!=null) {
            flushed.add(path);
            cacheProvider.invalidate(path);
            depCache.remove(element.getKey());
        }
    }

    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }
}
