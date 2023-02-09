/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
