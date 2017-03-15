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
package org.jahia.services.logging;

import org.slf4j.Logger;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.JCREventIterator;
import org.jahia.services.content.JCRObservationManager;
import org.jahia.services.content.JCRObservationManager.EventWrapper;
import org.json.JSONObject;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.List;
import java.util.Map;

/**
 * JCR listener that logs repository events to the metrics logging service.
 *
 * @author rincevent
 * @since JAHIA 6.5
 *        Created : 24 nov. 2009
 */
public class MetricsLoggingJCReventListener extends DefaultEventListener {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(MetricsLoggingJCReventListener.class);
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
        return loggingService.isEnabled() ? Event.NODE_ADDED + Event.NODE_MOVED + Event.NODE_REMOVED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED : 0;
    }

    public String[] getNodeTypes() {
        return nodeTypesList == null || nodeTypesList.isEmpty() ? null : nodeTypesList.toArray(new String[nodeTypesList.size()]);
    }

    /**
     * This method is called when a bundle of events is dispatched.
     *
     * @param events The event set received.
     */
    public void onEvent(EventIterator events) {
        if (events instanceof JCREventIterator && ((JCREventIterator)events).getOperationType() == JCRObservationManager.IMPORT) {
            return;
        }
        while (events.hasNext()) {
            try {
                Event event = events.nextEvent();
                switch (event.getType()) {
                    case Event.NODE_ADDED:
                        loggingService.logContentEvent(event.getUserID(), "", "", event.getIdentifier(), event.getPath(), getNodeType(event), "nodeCreated",
                                getInfoAsJson(event));
                        break;
                    case Event.NODE_MOVED:
                        /* From the JCR 2.0 Spec :
                           12.3.3 Event Information on Move and Order

                           On a NODE_MOVED event, the Map object returned by Event.getInfo() contains parameter
                           information from the method that caused the event. There are three JCR methods that cause
                           this event type: Session.move, Workspace.move and Node.orderBefore.

                           If the method that caused the NODE_MOVE event was a Session.move or Workspace.move then the
                           returned Map has keys srcAbsPath and destAbsPath with values corresponding to the parameters
                           passed to the move method, as specified in the Javadoc.

                           If the method that caused the NODE_MOVE event was a Node.orderBefore then the returned Map
                           has keys srcChildRelPath and destChildRelPath with values corresponding to the parameters
                           passed to the orderBefore method, as specified in the Javadoc.
                        */
                        loggingService.logContentEvent(event.getUserID(), "", "", event.getIdentifier(), event.getPath(), getNodeType(event), "nodeMoved",
                                getInfoAsJson(event));
                        break;
                    case Event.NODE_REMOVED:
                        loggingService.logContentEvent(event.getUserID(), "", "", event.getIdentifier(), event.getPath(), getNodeType(event), "nodeDeleted",
                                getInfoAsJson(event));
                        break;
                    case Event.PROPERTY_ADDED:
                        loggingService.logContentEvent(event.getUserID(), "", "", event.getIdentifier(), event.getPath(), getNodeType(event), "propertyAdded",
                                getInfoAsJson(event));
                        break;
                    case Event.PROPERTY_CHANGED:
                        // @todo we might want to add the new value if available, so that we can track updates more finely ?
                        loggingService.logContentEvent(event.getUserID(), "", "", event.getIdentifier(), event.getPath(), getNodeType(event), "propertyChanged",
                                getInfoAsJson(event));
                        break;
                    case Event.PROPERTY_REMOVED:
                        loggingService.logContentEvent(event.getUserID(), "", "", event.getIdentifier(), event.getPath(), getNodeType(event), "propertyRemoved",
                                getInfoAsJson(event));
                        break;
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

    private String getInfoAsJson(Event event) throws RepositoryException {
        @SuppressWarnings("rawtypes")
        Map info = event.getInfo();
        return info != null && info.size() > 0 ? new JSONObject(info).toString() : "{}";
    }

    private String getNodeType(Event event) {
        String nodeType = null;
        if (event instanceof EventWrapper) {
            List<String> nodeTypes = ((EventWrapper) event).getNodeTypes();
            if (nodeTypes != null && nodeTypes.size() > 0) {
                nodeType = nodeTypes.get(0);
            }
        }
        return nodeType != null ? nodeType : "";
    }

}