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

package org.jahia.services.render;

import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.jahia.services.content.*;
import org.jahia.services.seo.jcr.VanityUrlManager;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

/**
 * JCR listener to invalidate URL resolver caches
 * @todo This implementation is not optimal, we should try to perfom finer invalidations.
 */
public class URLResolverListener extends DefaultEventListener {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(URLResolverListener.class);

    private URLResolverFactory urlResolverFactory;
    private VanityUrlService vanityUrlService;

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.NODE_MOVED;
    }

    public void onEvent(final EventIterator events) {
        if (urlResolverFactory == null) {
            return;
        }
        try {
            String userId = ((JCREventIterator)events).getSession().getUserID();
            if (userId.startsWith(JahiaLoginModule.SYSTEM)) {
                userId = userId.substring(JahiaLoginModule.SYSTEM.length());
            }

            JCRTemplate.getInstance().doExecuteWithSystemSession(userId, workspace, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    while (events.hasNext()) {
                        Event event = events.nextEvent();

                        if (isExternal(event)) {
                            continue;
                        }

                        String path = event.getPath();
                        if (event.getType() == Event.NODE_ADDED) {
                            nodeAdded(session, path);
                        } else if (event.getType() == Event.NODE_REMOVED) {
                            nodeRemoved(session, path);
                        } else if (event.getType() == Event.NODE_MOVED) {
                            nodeMoved(session, path);
                        }
                    }
                    return null;  
                }
            });
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
    
    private void nodeAdded(JCRSessionWrapper session, String path) throws RepositoryException {
        urlResolverFactory.flushCaches();
        if (path.contains(VanityUrlManager.VANITYURLMAPPINGS_NODE)) {
            vanityUrlService.flushCaches();
        }
    }

    private void nodeRemoved(JCRSessionWrapper session, String path) throws RepositoryException {
        urlResolverFactory.flushCaches();
        if (path.contains(VanityUrlManager.VANITYURLMAPPINGS_NODE)) {
            vanityUrlService.flushCaches();
        }
    }

    private void nodeMoved(JCRSessionWrapper session, String path) {
        urlResolverFactory.flushCaches();
        if (path.contains(VanityUrlManager.VANITYURLMAPPINGS_NODE)) {
            vanityUrlService.flushCaches();
        }
    }
}
