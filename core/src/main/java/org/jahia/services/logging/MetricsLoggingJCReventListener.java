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
package org.jahia.services.logging;

import org.apache.log4j.Logger;
import org.jahia.services.content.DefaultEventListener;
import org.json.JSONObject;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 24 nov. 2009
 */
public class MetricsLoggingJCReventListener extends DefaultEventListener {
    private transient static Logger logger = Logger.getLogger(MetricsLoggingJCReventListener.class);
    private MetricsLoggingService loggingService;
    private List<String> nodeTypesList = null;


    public void setLoggingService(MetricsLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    public void setNodeTypesList(List<String> nodeTypes) {
        if (nodeTypes != null && nodeTypes.size() > 0) {
            this.nodeTypesList = nodeTypes;
        }
    }

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_MOVED + Event.NODE_REMOVED;
    }

    public String getPath() {
        return "/";
    }

    public String[] getNodeTypes() {
        return nodeTypesList == null ? null : nodeTypesList.toArray(new String[nodeTypesList.size()]);
    }

    /**
     * This method is called when a bundle of events is dispatched.
     *
     * @param events The event set received.
     */
    public void onEvent(EventIterator events) {
        while (events.hasNext()) {
            try {
                Event event = events.nextEvent();
                switch (event.getType()) {
                    case Event.NODE_ADDED:
                        loggingService.logContentEvent(event.getUserID(), "", "", event.getPath(), "", "nodeCreated",
                                                       new JSONObject(event.getInfo()).toString());
                        break;
                    case Event.NODE_MOVED:
                        loggingService.logContentEvent(event.getUserID(), "", "", event.getPath(), "", "nodeUpdated",
                                                       new JSONObject(event.getInfo()).toString());
                        break;
                    case Event.NODE_REMOVED:
                        loggingService.logContentEvent(event.getUserID(), "", "", event.getPath(), "", "nodeDeleted",
                                                       new JSONObject(event.getInfo()).toString());
                        break;
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }

        }
    }
}
