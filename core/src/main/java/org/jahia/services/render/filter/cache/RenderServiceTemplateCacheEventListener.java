/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.filter.cache;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.ExternalEventListener;
import org.jahia.services.render.RenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.HashSet;
import java.util.Set;

/**
 * Output cache invalidation listener.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 12 janv. 2010
 */
public class RenderServiceTemplateCacheEventListener extends DefaultEventListener implements ExternalEventListener {
    private static final int MODULES_LENGTH = "/modules/".length();

    private static final Logger logger = LoggerFactory.getLogger(RenderServiceTemplateCacheEventListener.class);

    private RenderService renderService;

    @Override
    public String getPath() {
        return "/modules";
    }

    @Override
    public String[] getNodeTypes() {
        return new String[] { "jnt:template" };
    }

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED +
               Event.NODE_MOVED + Event.NODE_REMOVED;
    }

    /**
     * This method is called when a bundle of events is dispatched.
     *
     * @param events The event set received.
     */
    @Override
    public void onEvent(EventIterator events) {
        Set<String> modulesToFlush = new HashSet<String>();
        while (events.hasNext()) {
            Event event = (Event) events.next();
            try {
                String path = event.getPath();
                if (renderService == null) {
                    renderService = (RenderService) SpringContextSingleton.getBean("RenderService");
                }
                if (renderService != null) {
                    int index = path.indexOf("/", MODULES_LENGTH);
                    index = path.indexOf("/", index + 1);
                    if (index > -1) {
                        modulesToFlush.add(path.substring(0,index));
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }

        }
        for (String module : modulesToFlush) {
            logger.debug("Flushing {} entries for {}", RenderService.RENDER_SERVICE_TEMPLATES_CACHE, module);
            renderService.flushCache(module);
        }
    }

    public void setRenderService(RenderService renderService) {
        this.renderService = renderService;
    }
}
