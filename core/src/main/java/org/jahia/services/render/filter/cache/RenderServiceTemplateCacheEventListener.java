/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render.filter.cache;

import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.render.RenderService;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

/**
 * Output cache invalidation listener.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 12 janv. 2010
 */
public class RenderServiceTemplateCacheEventListener extends DefaultEventListener {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(
            RenderServiceTemplateCacheEventListener.class);

    private RenderService renderService;
    private CacheKeyGenerator cacheKeyGenerator;

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED +
               Event.NODE_MOVED + Event.NODE_REMOVED;
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

        while (events.hasNext()) {
            Event event = (Event) events.next();
            try {
                String path = event.getPath();
                if (!path.startsWith("/jcr:system")) {
                    boolean flushRoles = false;
                    final int type = event.getType();
                    if (path.contains("/templates") || path.startsWith("/templateSets")) {
                        renderService.flushCache();
                    }
                    if (path.contains("j:templateNode")) {
                        renderService.flushCache();
                    }
                    if (path.endsWith("j:roles")) {
                        flushRoles = true;
                    }
                    if (path.contains("j:acl") || path.contains("jnt:group") || flushRoles ||
                        type == Event.NODE_MOVED) {
                        // Flushing cache of acl key for users as a group or an acl has been updated
                        if (cacheKeyGenerator instanceof DefaultCacheKeyGenerator) {
                            DefaultCacheKeyGenerator generator = (DefaultCacheKeyGenerator) cacheKeyGenerator;
                            generator.flushUsersGroupsKey();
                        }
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

    public void setCacheKeyGenerator(CacheKeyGenerator cacheKeyGenerator) {
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    public void setRenderService(RenderService renderService) {
        this.renderService = renderService;
    }
}
