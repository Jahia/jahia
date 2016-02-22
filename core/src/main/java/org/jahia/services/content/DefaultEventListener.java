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
package org.jahia.services.content;

import static org.jahia.api.Constants.*;

import org.apache.jackrabbit.api.observation.JackrabbitEvent;
import org.jahia.services.content.JCRObservationManager.EventWrapper;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: toto
 * Date: 14 janv. 2008
 * Time: 15:56:43
 * 
 */
public abstract class DefaultEventListener implements EventListener {

    protected Set<String> propertiesToIgnore;
    protected String workspace;
    protected boolean availableDuringPublish = false;
    protected Set<Integer> operationTypes;

    protected DefaultEventListener() {
        propertiesToIgnore = new HashSet<String>();
        propertiesToIgnore.add(JCR_PRIMARYTYPE);
        propertiesToIgnore.add(JCR_UUID);
        propertiesToIgnore.add(JCR_CREATED);
        propertiesToIgnore.add(JCR_CREATEDBY);
        propertiesToIgnore.add(JCR_LASTMODIFIED);
        propertiesToIgnore.add(JCR_LASTMODIFIEDBY);
        propertiesToIgnore.add(LASTPUBLISHED);
        propertiesToIgnore.add(LASTPUBLISHEDBY);        
        propertiesToIgnore.add(PUBLISHED);
        propertiesToIgnore.add(JCR_LOCKOWNER);
        propertiesToIgnore.add(JCR_LOCKISDEEP);
        propertiesToIgnore.add("j:locktoken");
        propertiesToIgnore.add("j:lockTypes");
        propertiesToIgnore.add(JCR_ISCHECKEDOUT);
        propertiesToIgnore.add(JCR_VERSIONHISTORY);
        propertiesToIgnore.add(JCR_PREDECESSORS);
        propertiesToIgnore.add(JCR_SUCCESSORS);
        propertiesToIgnore.add(JCR_BASEVERSION);
        propertiesToIgnore.add(JCR_FROZENUUID);
        propertiesToIgnore.add(FULLPATH);
        propertiesToIgnore.add(NODENAME);
        propertiesToIgnore.add(PROCESSID);
        propertiesToIgnore.add(JCR_MERGEFAILED);
        propertiesToIgnore.add(REVISION_NUMBER);
        propertiesToIgnore.add(CHECKIN_DATE);
        propertiesToIgnore.add(JCR_LASTLOGINDATE);
        propertiesToIgnore.add(ORIGIN_WORKSPACE);
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    protected boolean isExternal(Event event) {
        // Jackrabbit / cluster workaround
        if (event instanceof JackrabbitEvent) {
            return ((JackrabbitEvent) event).isExternal();
        } else if (event instanceof EventWrapper) {
            return ((EventWrapper) event).isExternal();
        }
        return false;
    }
    
    protected boolean isSupportedOperationType(int operationType) {
        return operationTypes == null || operationTypes.contains(operationType);
    }

    public abstract int getEventTypes();

    public String getPath() {
        return null;
    }

    public boolean isDeep() {
        return true;
    }

    public String[] getNodeTypes() {
        return null;
    }

    public String[] getUuids() {
        return null;
    }

    public boolean isAvailableDuringPublish() {
        return availableDuringPublish;
    }

    public void setAvailableDuringPublish(boolean availableDuringPublish) {
        this.availableDuringPublish = availableDuringPublish;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setOperationTypes(Set<Integer> operationTypes) {
        this.operationTypes = operationTypes;
    }

    public Set<Integer> getOperationTypes() {
        return operationTypes;
    }

    /**
     * This method is a utility method to filter out the REMOVE and ADDED node events that are generated by Jackrabbit
     * before a MOVE event (either a real move or a child reorder)
     * @param eventIterator an event iterator (usually provided by JCR observation API calls)
     * @return a filtered list of events, where the REMOVE and ADDED node events were removed if they
     * were just before the MOVED event for the same not.
     * @throws javax.jcr.RepositoryException in case the was an error accessing the event identifiers
     */
    public List<Event> getMoveFilteredEvents(EventIterator eventIterator) throws RepositoryException {
        List<Event> eventList = new ArrayList<Event>();
        Event previousPreviousEvent = null;
        Event previousEvent = null;
        while (eventIterator.hasNext()) {
            Event event = eventIterator.nextEvent();

            String identifier = null;
            identifier = event.getIdentifier();
            String previousIdentifier = null;
            if (previousEvent != null) {
                previousIdentifier = previousEvent.getIdentifier();
            }
            String previousPreviousIdentifier = null;
            if (previousPreviousEvent != null) {
                previousPreviousIdentifier = previousPreviousEvent.getIdentifier();
            }

            if (Event.NODE_MOVED == event.getType()) {
                if (previousEvent != null && previousPreviousEvent != null &&
                        Event.NODE_REMOVED == previousPreviousEvent.getType() &&
                        Event.NODE_ADDED == previousEvent.getType() &&
                        identifier != null &&
                        identifier.equals(previousIdentifier) &&
                        identifier.equals(previousPreviousIdentifier)) {
                    // we are in the case of added remove and add events due to Jackrabbit event migration compatibility
                    eventList.remove(eventList.size());
                }
            }

            eventList.add(event);

            // upgrade previous events
            previousPreviousEvent = previousEvent;
            previousEvent = event;
        }
        return eventList;
    }
}
