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

package org.jahia.services.content;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.observation.*;
import javax.jcr.observation.EventListener;

import org.slf4j.Logger;

import java.util.*;

/**
 * Observation manager implementation
 * <p/>
 * Execute listener synchronously after session.save()
 */
public class JCRObservationManager implements ObservationManager {
    public static final int SESSION_SAVE = 1;
    public static final int WORKSPACE_MOVE = 1 << 1;
    public static final int WORKSPACE_COPY = 1 << 2;
    public static final int WORKSPACE_CLONE = 1 << 3;
    public static final int WORKSPACE_CREATE_ACTIVITY = 1 << 6;
    public static final int NODE_CHECKIN = 1 << 7;
    public static final int NODE_CHECKOUT = 1 << 8;
    public static final int NODE_CHECKPOINT = 1 << 9;
    public static final int NODE_RESTORE = 1 << 10;
    public static final int NODE_UPDATE = 1 << 11;
    public static final int NODE_MERGE = 1 << 12;

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRObservationManager.class);

    private static ThreadLocal<Boolean> eventsDisabled = new ThreadLocal<Boolean>();
    private static ThreadLocal<JCRSessionWrapper> currentSession = new ThreadLocal<JCRSessionWrapper>();
    private static ThreadLocal<Map<JCRSessionWrapper, List<Event>>> events =
            new ThreadLocal<Map<JCRSessionWrapper, List<Event>>>();
    private static List<EventConsumer> listeners = new ArrayList<EventConsumer>();

    private JCRWorkspaceWrapper ws;

    public JCRObservationManager(JCRWorkspaceWrapper ws) {
        this.ws = ws;
    }

    /**
     * Adds an event listener that listens for the specified <code>eventTypes</code> (a combination of one or more
     * event types encoded as a bit mask value).
     * <p/>
     * The set of events can be filtered by specifying restrictions based on characteristics of the node associated
     * with the event. In the case of  event types <code>NODE_ADDED</code> and <code>NODE_REMOVED</code>, the node
     * associated with an event is the node at (or formerly at) the path returned by <code>Event.getPath</code>.
     * In the case of  event types <code>PROPERTY_ADDED</code>,  <code>PROPERTY_REMOVED</code> and
     * <code>PROPERTY_CHANGED</code>, the node associated with an event is the parent node of the property at
     * (or formerly at) the path returned by <code>Event.getPath</code>:
     * <ul>
     * <li>
     * <code>absPath</code>, <code>isDeep</code>: Only events whose associated node is at
     * <code>absPath</code> (or within its subtree, if <code>isDeep</code> is <code>true</code>) will be received.
     * It is permissible to register a listener for a path where no node currently exists.
     * </li>
     * <li>
     * <code>uuid</code>: Only events whose associated node has one of the UUIDs in this list will be
     * received. If his parameter is <code>null</code> then no UUID-related restriction is placed on events
     * received.
     * </li>
     * <li>
     * <code>nodeTypeName</code>: Only events whose associated node has one of the node types
     * (or a subtype of one of the node types) in this list will be received. If his parameter is
     * <code>null</code> then no node type-related restriction is placed on events received.
     * </li>
     * </ul>
     * The restrictions are "ANDed" together. In other words, for a particular node to be "listened to" it must meet all the restrictions.
     * <p/>
     * Additionally, if <code>noLocal</code> is <code>true</code>, then events generated by the session through which
     * the listener was registered are ignored. Otherwise, they are not ignored.
     * <p/>
     * The filters of an already-registered <code>EventListener</code> can be changed at runtime by re-registering the
     * same <code>EventListener</code> object (i.e. the same actual Java object) with a new set of filter arguments.
     * The implementation must ensure that no events are lost during the changeover.
     *
     * @param listener     an {@link javax.jcr.observation.EventListener} object.
     * @param eventTypes   A combination of one or more event type constants encoded as a bitmask.
     * @param absPath      an absolute path.
     * @param isDeep       a <code>boolean</code>.
     * @param uuid         array of UUIDs.
     * @param nodeTypeName array of node type names.
     * @param noLocal      a <code>boolean</code>.
     * @throws javax.jcr.RepositoryException If an error occurs.
     */
    public void addEventListener(EventListener listener, int eventTypes, String absPath, boolean isDeep, String[] uuid,
                                 String[] nodeTypeName, boolean noLocal) throws RepositoryException {
        listeners.add(new EventConsumer(ws.getSession(), listener, eventTypes));
    }

    /**
     * Deregisters an event listener.
     * <p/>
     * A listener may be deregistered while it is being executed. The
     * deregistration method will block until the listener has completed
     * executing. An exception to this rule is a listener which deregisters
     * itself from within the <code>onEvent</code> method. In this case, the
     * deregistration method returns immediately, but deregistration will
     * effectively be delayed until the listener completes.
     *
     * @param listener The listener to deregister.
     * @throws javax.jcr.RepositoryException If an error occurs.
     */
    public void removeEventListener(EventListener listener) throws RepositoryException {
        EventConsumer e = null;
        for (EventConsumer eventConsumer : listeners) {
            if (eventConsumer.listener == listener) {
                e = eventConsumer;
                break;
            }
        }
        if (e != null) {
            listeners.remove(e);
        }
    }

    /**
     * Returns all event listeners that have been registered through this session.
     * If no listeners have been registered, an empty iterator is returned.
     *
     * @return an <code>EventListenerIterator</code>.
     * @throws javax.jcr.RepositoryException
     */
    public EventListenerIterator getRegisteredEventListeners() throws RepositoryException {
        return new EventListenerIteratorImpl(listeners.iterator(), listeners.size());
    }

    public void setUserData(String userData) throws RepositoryException {

    }

    public EventJournal getEventJournal() throws RepositoryException {
        return null;
    }

    public EventJournal getEventJournal(int i, String s, boolean b, String[] strings, String[] strings1)
            throws RepositoryException {
        return null;
    }

    public static void setEventsDisabled(Boolean eventsDisabled) {
        JCRObservationManager.eventsDisabled.set(eventsDisabled);
    }

    public static void addEvent(Event event) {
        try {
            if (!event.getPath().startsWith("/jcr:system")) {
                Map<JCRSessionWrapper, List<Event>> map = events.get();
                if (map == null) {
                    events.set(new HashMap<JCRSessionWrapper, List<Event>>());
                }
                map = events.get();
                JCRSessionWrapper session = currentSession.get();
                if (session != null) {
                    if (!map.containsKey(session)) {
                        map.put(session, new ArrayList<Event>());
                    }
                    List<Event> list = map.get(session);

                    list.add(event);
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static void setCurrentSession(JCRSessionWrapper session) {
        currentSession.set(session);
    }

    private static void consume(JCRSessionWrapper session, int operationType) {
        Map<JCRSessionWrapper, List<Event>> map = events.get();
        events.set(null);
        currentSession.set(null);
        if (map != null && map.containsKey(session)) {
            List<Event> list = map.get(session);
            for (EventConsumer consumer : listeners) {
                if (consumer.session.getWorkspace().getName().equals(session.getWorkspace().getName())) {
                    if (!Boolean.TRUE.equals(eventsDisabled.get()) || ((DefaultEventListener) consumer.listener).isAvailableDuringPublish()) {
                    List<Event> filteredEvents = new ArrayList<Event>();
                    for (Event event : list) {
                        if ((consumer.eventTypes & event.getType()) != 0) {
                            filteredEvents.add(event);
                        }
                    }
                    try {
                    consumer.listener.onEvent(new JCREventIterator(session, operationType, filteredEvents.iterator(),
                            filteredEvents.size()));
                    } catch (Exception e) {
                        logger.warn("Error processing event by listener. Cause: " + e.getMessage(), e);
                    }
                    }
                }
            }
        }
    }

    public static <X> X doWorkspaceWriteCall(JCRSessionWrapper session, int operationType, JCRCallback<X> callback)
            throws RepositoryException {
        setCurrentSession(session);
        X res;
        try {
            res = callback.doInJCR(session);
        } finally {
            consume(session, operationType);
            setCurrentSession(null);
        }
        return res;
    }


    class EventConsumer {
        private JCRSessionWrapper session;
        private EventListener listener;
        private int eventTypes;

        EventConsumer(JCRSessionWrapper session, EventListener listener, int eventTypes) {
            this.session = session;
            this.listener = listener;
            this.eventTypes = eventTypes;
        }
    }

    class EventListenerIteratorImpl extends RangeIteratorImpl implements EventListenerIterator {
        EventListenerIteratorImpl(Iterator<EventConsumer> iterator, long size) {
            super(iterator, size);
        }

        /**
         * Returns the next <code>EventListener</code> in the iteration.
         *
         * @return the next <code>EventListener</code> in the iteration.
         * @throws java.util.NoSuchElementException
         *          if iteration has no more <code>EventListener</code>s.
         */
        public EventListener nextEventListener() {
            return ((EventConsumer) next()).listener;
        }
    }

    private static class WeakReferenceUpdateEvent implements Event {
        private final Property property;
        private final Event event;

        public WeakReferenceUpdateEvent(Property property, Event event) {
            this.property = property;
            this.event = event;
        }

        public int getType() {
            return Event.PROPERTY_CHANGED;
        }

        public String getPath() throws RepositoryException {
            return property.getPath();
        }

        public String getUserID() {
            return event.getUserID();
        }

        public String getIdentifier() throws RepositoryException {
            return property.getParent().getIdentifier();
        }

        public Map getInfo() throws RepositoryException {
            return new HashMap<String, String>();
        }

        public String getUserData() throws RepositoryException {
            return event.getUserData();
        }

        public long getDate() throws RepositoryException {
            return event.getDate();
        }
    }
}


