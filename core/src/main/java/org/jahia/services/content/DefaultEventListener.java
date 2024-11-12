/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
    protected Set<Integer> skipOnOperationTypes;

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
        return (operationTypes == null || operationTypes.contains(operationType)) &&
                (skipOnOperationTypes == null || !skipOnOperationTypes.contains(operationType));
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

    /**
     * Set of operation types that should be processed by the event listener
     * @param operationTypes set of operation types that should be processed by the event listener
     */
    public void setOperationTypes(Set<Integer> operationTypes) {
        this.operationTypes = operationTypes;
    }

    /**
     * @return Operation types that should be processed by the event listener or null if all operation types should be processed
     */
    public Set<Integer> getOperationTypes() {
        return operationTypes;
    }

    /**
     * @return Operation types that should be skipped by the event listener or null if all operation types should be processed
     */
    public Set<Integer> getSkipOnOperationTypes() {
        return skipOnOperationTypes;
    }

    /**
     * Set of operation types that should be skipped by the event listener
     * @param skipOnOperationTypes set of operation types that should be skipped by the event listener
     */
    public void setSkipOnOperationTypes(Set<Integer> skipOnOperationTypes) {
        this.skipOnOperationTypes = skipOnOperationTypes;
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

    public Class getListenerClass() {
        return this.getClass();
    }
}
