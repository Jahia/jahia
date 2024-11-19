/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.events;

import org.apache.jackrabbit.api.observation.JackrabbitEvent;
import org.apache.jackrabbit.commons.iterator.EventIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.FilteredRangeIterator;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.observation.EventState;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.AdditionalEventInfo;
import org.apache.jackrabbit.spi.commons.conversion.MalformedPathException;
import org.apache.jackrabbit.spi.commons.name.PathFactoryImpl;
import org.apache.jackrabbit.spi.commons.value.ValueFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Event iterator implementation that filters provided {@link EventState}s using required event types filter.
 *
 * @author Sergiy Shyrkov
 */
class FilteredEventIterator extends EventIteratorAdapter {

    /**
     * Implementation of the {@link Event} and the {@link JackrabbitEvent} interface.
     */
    static class JCREvent implements JackrabbitEvent, AdditionalEventInfo, Event {

        /**
         * Logger instance for this class
         */
        private static final Logger log = LoggerFactory.getLogger(JCREvent.class);

        /**
         * Returns <code>true</code> if the objects are equal or both are <code>null</code>; otherwise returns <code>false</code>.
         *
         * @param o1 an object.
         * @param o2 another object.
         * @return <code>true</code> if equal; <code>false</code> otherwise.
         */
        private static boolean equals(Object o1, Object o2) {
            if (o1 == null) {
                return o2 == null;
            } else {
                return o1.equals(o2);
            }
        }

        /**
         * The shared {@link EventState} object.
         */
        private final EventState eventState;

        /**
         * The session of the {@link javax.jcr.observation.EventListener} this event will be delivered to.
         */
        private final SessionImpl session;

        /**
         * Cached String value of this <code>Event</code> instance.
         */
        private String stringValue;

        /**
         * The timestamp of this event.
         */
        private final long timestamp;

        /**
         * The user data associated with this event.
         */
        private final String userData;

        // ---------------------------------------------------------------< Event >

        /**
         * Creates a new {@link Event} instance based on an {@link EventState eventState}.
         *
         * @param session the session of the registered <code>EventListener</code> where this <code>Event</code> will be delivered to.
         * @param eventState the underlying <code>EventState</code>.
         * @param timestamp the time when the change occurred that caused this event.
         * @param userData the user data associated with this event.
         */
        JCREvent(SessionImpl session, EventState eventState, long timestamp, String userData) {
            this.session = session;
            this.eventState = eventState;
            this.timestamp = timestamp;
            this.userData = userData;
        }

        /**
         * Returns <code>true</code> if this <code>Event</code> is equal to another object.
         * <p/>
         * Two <code>Event</code> instances are equal if their respective <code>EventState</code> instances are equal and both
         * <code>Event</code> instances are intended for the same <code>Session</code> that registerd the <code>EventListener</code>.
         *
         * @param obj the reference object with which to compare.
         * @return <code>true</code> if this <code>Event</code> is equal to another object.
         */
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof JCREvent) {
                JCREvent other = (JCREvent) obj;
                return this.eventState.equals(other.eventState) && this.session.equals(other.session)
                        && this.timestamp == other.timestamp && equals(this.userData, other.userData);
            }
            return false;
        }

        /**
         * Returns the id of a child node operation. If this <code>Event</code> was generated for a property operation this method returns
         * <code>null</code>.
         *
         * @return the id of a child node operation.
         */
        public NodeId getChildId() {
            return eventState.getChildId();
        }

        /**
         * {@inheritDoc}
         */
        public long getDate() {
            return timestamp;
        }

        /**
         * {@inheritDoc}
         */
        public String getIdentifier() throws RepositoryException {
            if (eventState.getType() == Event.PERSIST) {
                return null;
            } else {
                NodeId id = eventState.getChildId();

                if (id != null) {
                    return id.toString();
                } else {
                    // property event
                    return eventState.getParentId().toString();
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        public Map<String, String> getInfo() throws RepositoryException {
            Map<String, String> info = new HashMap<>();
            for (Map.Entry<String, InternalValue> entry : eventState.getInfo().entrySet()) {
                InternalValue value = entry.getValue();
                String strValue = null;
                if (value != null) {
                    strValue = ValueFormat.getJCRString(value, session);
                }
                info.put(entry.getKey(), strValue);
            }
            return info;
        }

        /**
         * @return the mixin node types of the node associated with the event
         * @see AdditionalEventInfo#getMixinTypeNames()
         */
        public Set<Name> getMixinTypeNames() {
            return eventState.getMixinNames();
        }

        // -----------------------------------------------------------< EventImpl >

        /**
         * Returns the uuid of the parent node.
         *
         * @return the uuid of the parent node.
         */
        public NodeId getParentId() {
            return eventState.getParentId();
        }

        /**
         * {@inheritDoc}
         */
        public String getPath() throws RepositoryException {
            Path p = getQPath();
            return p != null ? session.getJCRPath(p) : null;
        }

        /**
         * @return the primary node type of the node associated with the event
         * @see AdditionalEventInfo#getPrimaryNodeTypeName()
         */
        public Name getPrimaryNodeTypeName() {
            return eventState.getNodeType();
        }

        /**
         * Returns the <code>Path</code> of this event.
         *
         * @return path or <code>null</code> when no path is associated with the event
         * @throws RepositoryException if the path can't be constructed
         */
        public Path getQPath() throws RepositoryException {
            try {
                Path parent = eventState.getParentPath();
                Path child = eventState.getChildRelPath();

                if (parent == null || child == null) {
                    // an event without associated path information
                    return null;
                } else {
                    int index = child.getIndex();
                    if (index > 0) {
                        return PathFactoryImpl.getInstance().create(parent, child.getName(), index, false);
                    } else {
                        return PathFactoryImpl.getInstance().create(parent, child.getName(), false);
                    }
                }
            } catch (MalformedPathException e) {
                String msg = "internal error: malformed path for event";
                log.debug(msg);
                throw new RepositoryException(msg, e);
            }
        }

        /**
         * @return the specified session attribute
         */
        public Object getSessionAttribute(String name) {
            return session.getAttribute(name);
        }

        // ---------------------------------------------------------------< AdditionalEventInfo >

        /**
         * {@inheritDoc}
         */
        public int getType() {
            return eventState.getType();
        }

        /**
         * {@inheritDoc}
         */
        public String getUserData() {
            return userData;
        }

        /**
         * {@inheritDoc}
         */
        public String getUserID() {
            return eventState.getUserId();
        }

        /**
         * @see Object#hashCode()
         */
        public int hashCode() {
            int h = eventState.hashCode() ^ Long.hashCode(timestamp) ^ session.hashCode();
            if (userData != null) {
                h = h ^ userData.hashCode();
            }
            return h;
        }

        /**
         * Return a flag indicating whether this is an externally generated event.
         *
         * @return <code>true</code> if this is an external event; <code>false</code> otherwise
         * @see JackrabbitEvent#isExternal()
         */
        public boolean isExternal() {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns a flag indicating whether the child node of this event is a shareable node. Only applies to node added/removed events.
         *
         * @return <code>true</code> for a shareable child node, <code>false</code> otherwise.
         */
        public boolean isShareableChildNode() {
            return false;
        }

        /**
         * Returns a String representation of this <code>Event</code>.
         *
         * @return a String representation of this <code>Event</code>.
         */
        public String toString() {
            if (stringValue == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Event: Path: ");
                try {
                    sb.append(getPath());
                } catch (RepositoryException e) {
                    log.error("Exception retrieving path: " + e);
                    sb.append("[Error retrieving path]");
                }
                sb.append(", ").append(EventState.valueOf(getType())).append(": ");
                sb.append(", UserId: ").append(getUserID());
                sb.append(", Timestamp: ").append(timestamp);
                sb.append(", UserData: ").append(userData);
                sb.append(", Info: ").append(eventState.getInfo());
                stringValue = sb.toString();
            }
            return stringValue;
        }
    }

    /**
     * Target session
     */
    private final SessionImpl session;

    /**
     * The timestamp when the events occurred.
     */
    private final long timestamp;

    /**
     * The user data associated with these events.
     */
    private final String userData;

    FilteredEventIterator(SessionImpl session, Iterator<EventState> eventStates, long timestamp, String userData,
            final int eventTypes) {
        super(new FilteredRangeIterator(eventStates, state -> (eventTypes & ((EventState) state).getType()) != 0));
        this.session = session;
        this.timestamp = timestamp;
        this.userData = userData;
    }

    @Override
    public Object next() {
        return new JCREvent(session, (EventState) super.next(), timestamp, userData);
    }
}
