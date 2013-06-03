/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.content;

import javax.jcr.RepositoryException;
import javax.jcr.observation.*;
import javax.jcr.observation.EventListener;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.api.observation.JackrabbitEvent;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * Observation manager implementation
 * <p/>
 * Execute listener synchronously after session.save()
 */
public class JCRObservationManager implements ObservationManager {
    public static final int SESSION_SAVE = 1;
    public static final int WORKSPACE_MOVE = 2;
    public static final int WORKSPACE_COPY = 3;
    public static final int WORKSPACE_CLONE = 4;
    public static final int WORKSPACE_CREATE_ACTIVITY = 5;
    public static final int NODE_CHECKIN = 6;
    public static final int NODE_CHECKOUT = 7;
    public static final int NODE_CHECKPOINT = 8;
    public static final int NODE_RESTORE = 9;
    public static final int NODE_UPDATE = 10;
    public static final int NODE_MERGE = 11;
    public static final int EXTERNAL_SYNC = 12;
    public static final int IMPORT = 13;

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRObservationManager.class);

    private static ThreadLocal<Boolean> eventsDisabled = new ThreadLocal<Boolean>();
    private static ThreadLocal<JCRSessionWrapper> currentSession = new ThreadLocal<JCRSessionWrapper>();
    private static ThreadLocal<Integer> lastOp = new ThreadLocal<Integer>();
    private static ThreadLocal<Map<JCRSessionWrapper, List<EventWrapper>>> events =
            new ThreadLocal<Map<JCRSessionWrapper, List<EventWrapper>>>();
    private static List<EventConsumer> listeners = new CopyOnWriteArrayList<EventConsumer>();

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
     * It is permissible to register a listener for a path where no node currently exists. The path can also be
     * a regular expression (as the parameter is an absolute path, Jahia will automatically add the begin/end line
     * character and if isDeep is true, Jahia appends to the regular expression, to include the path and subtree). 
     * If this parameter is <code>null</code> then no path-related restriction is placed on events received. 
     * </li>
     * <li>
     * <code>uuid</code>: Only events whose associated node has one of the UUIDs in this list will be
     * received. If his parameter is <code>null</code> then no UUID-related restriction is placed on events
     * received.
     * </li>
     * <li>
     * <code>nodeTypeName</code>: Only events whose associated node has one of the node types
     * (or a subtype of one of the node types) in this list will be received. If this parameter is
     * <code>null</code> then no node type-related restriction is placed on events received. 
     * WARNING: if a listener only filters on nodeTypeName, then this can slow down the system, as for all
     * events we need to determine the nodeType of the node. If possible you should use another filter like
     * the path to reduce the number of events, where nodetype needs to be determined. 
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
        listeners.add(new EventConsumer(ws.getSession(), listener, eventTypes, absPath, isDeep, nodeTypeName, uuid, listener instanceof ExternalEventListener));
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
                if (event.getType() == Event.NODE_ADDED && isExtensionNode(event.getPath()) && hasMatchingUuidBeenSet(event.getPath())) {
                    return;
                }
                Map<JCRSessionWrapper, List<EventWrapper>> map = events.get();
                if (map == null) {
                    events.set(new HashMap<JCRSessionWrapper, List<EventWrapper>>());
                }
                map = events.get();
                JCRSessionWrapper session = currentSession.get();
                if (session != null) {
                    if (!map.containsKey(session)) {
                        map.put(session, new ArrayList<EventWrapper>());
                    }
                    List<EventWrapper> list = map.get(session);
                    list.add(getEventWrapper(event, session));
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static EventWrapper getEventWrapper(Event event, JCRSessionWrapper session) {
        return new EventWrapper(event, event.getType() != Event.NODE_REMOVED ? null : Collections.<String>emptyList(), session);
    }

    private static void consume(JCRSessionWrapper session, int operationType) throws RepositoryException {
        operationType = lastOp.get();

        Map<JCRSessionWrapper, List<EventWrapper>> map = events.get();
        events.set(null);
        currentSession.set(null);
        if (map != null && map.containsKey(session)) {
            List<EventWrapper> list = map.get(session);
            consume(list, session, operationType);
        }
    }

    public static void consume(List<EventWrapper> list, JCRSessionWrapper session, int operationType) throws RepositoryException {
        for (EventConsumer consumer : listeners) {
            DefaultEventListener castListener = consumer.listener instanceof DefaultEventListener ? (DefaultEventListener) consumer.listener : null;
            // check if the required workspace condition is matched
            // check if the events are not disabled or the listener is still available during publication
            // check if the event is not eternal or consumer accepts external events
            if (consumer.session.getWorkspace().getName().equals(session.getWorkspace().getName()) &&
                (!Boolean.TRUE.equals(eventsDisabled.get()) || (castListener != null && castListener.isAvailableDuringPublish()))
                        && (consumer.useExternalEvents || operationType != EXTERNAL_SYNC)) {
                    List<EventWrapper> filteredEvents = new ArrayList<EventWrapper>();
                    for (EventWrapper event : list) {
                        if ((consumer.eventTypes & event.getType()) != 0 &&
                                (consumer.pathPattern == null || consumer.pathPattern.matcher(event.getPath()).matches()) &&
                                (consumer.uuid == null || checkUuids(event.getIdentifier(), consumer.uuid)) &&
                                (castListener == null || castListener.isSupportedOperationType(operationType)) &&
                                (consumer.nodeTypeName == null || checkNodeTypeNames(session, event, consumer.nodeTypeName))) {
                            filteredEvents.add(event);
                        }
                    }
                    try {
                        if (!filteredEvents.isEmpty()) {
                            consumer.listener.onEvent(new JCREventIterator(session, operationType, filteredEvents.iterator(),
                                    filteredEvents.size()));
                        }
                    } catch (Exception e) {
                        logger.warn("Error processing event by listener. Cause: " + e.getMessage(), e);
                    }
                
            }
        }
    }

    private static boolean checkNodeTypeNames(JCRSessionWrapper session, EventWrapper event, String[] requiredNodeTypes) throws RepositoryException {
        if (event.getNodeTypes() == null) {
            String nodePath = (event.getType() == Event.PROPERTY_REMOVED || event.getType() == Event.PROPERTY_CHANGED || event.getType() == Event.PROPERTY_ADDED ?
                    StringUtils.substringBeforeLast(event.getPath(), "/")
                    : event.getPath());
            try {
                JCRNodeWrapper node = session.getNode(nodePath);
                event.setNodeTypes(node.getNodeTypes());
            } catch (RepositoryException e) {
                logger.debug("Could not retrieve node (type)", e);
                event.setNodeTypes(Collections.<String>emptyList());
            }            
        }
        if (event.getNodeTypes() != null) {
            for (String requiredNodeType : requiredNodeTypes) {
                for (String nodeType : event.getNodeTypes()) {
                    if (NodeTypeRegistry.getInstance().getNodeType(nodeType).isNodeType(requiredNodeType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean checkUuids(String identifier, String[] uuids) throws RepositoryException {
        if (identifier != null) {
            for (String uuid : uuids) {
                if (identifier.equals(uuid)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static <X> X doWorkspaceWriteCall(JCRSessionWrapper session, int operationType, JCRCallback<X> callback)
            throws RepositoryException {
        currentSession.set(session);
        X res;
        try {
            res = callback.doInJCR(session);
        } finally {
            boolean x = lastOp.get() == null;
            try {
                if (x) {
                    lastOp.set(operationType);
                }
                consume(session, operationType);
            } finally {
                currentSession.set(null);
                if (x) {
                    lastOp.set(null);
                }
            }
        }
        return res;
    }

    public static <X> X doWithOperationType(JCRSessionWrapper session, int operationType, JCRCallback<X> callback)
            throws RepositoryException {
        boolean x = lastOp.get() == null;
        try {
            if (x) {
                lastOp.set(operationType);
            }
            return callback.doInJCR(session);
        } finally {
            currentSession.set(null);
            if (x) {
                lastOp.set(null);
            }
        }
    }

    public static Integer getCurrentOperationType() {
        return lastOp.get();
    }

    public static boolean isExtensionNode(String path) throws RepositoryException {
        for (Map.Entry<String, JCRStoreProvider> entry : JCRSessionFactory.getInstance().getMountPoints().entrySet()) {
            // Handle extension nodes, return visible uuid
            if (path.startsWith(entry.getKey()) && !entry.getKey().equals("/")) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasMatchingUuidBeenSet(String path) throws RepositoryException {
        if (events.get() != null && currentSession.get() != null) {
            List<EventWrapper> currentEvents = events.get().get(currentSession.get());
            for (EventWrapper previousEvent : currentEvents) {
                if (previousEvent.getPath().equals(path + "/j:externalNodeIdentifier")) {
                    return true;
                }
            }
        }
        return false;
    }


    class EventConsumer {
        private JCRSessionWrapper session;
        private EventListener listener;
        private int eventTypes;
        private String absPath;
        private boolean isDeep;
        private String[] nodeTypeName;
        private String[] uuid;
        private boolean useExternalEvents;
        private Pattern pathPattern;

        EventConsumer(JCRSessionWrapper session, EventListener listener, int eventTypes, String absPath, boolean isDeep, String[] nodeTypeName, String[] uuid, boolean useExternalEvents) {
            this.session = session;
            this.listener = listener;
            this.eventTypes = eventTypes;
            this.absPath = absPath;
            this.isDeep = isDeep;
            this.nodeTypeName = nodeTypeName;
            this.uuid = uuid;
            this.useExternalEvents = useExternalEvents;
            
            if (this.absPath != null) {
                pathPattern = Pattern.compile("^" + this.absPath + (this.isDeep ? (this.absPath.endsWith("/") ? "(.*)*" : "(/.*)*") : "") + "$");
            }
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

    static class EventWrapper implements Event {
        private Event event;
        private List<String> nodeTypes;
        private String identifier;
        private JCRSessionWrapper session;

        EventWrapper(Event event, List<String> nodeTypes, JCRSessionWrapper session) {
            this.event = event;
            this.nodeTypes = nodeTypes;
            this.session = session;
        }

        public int getType() {
            return event.getType();
        }

        public String getPath() throws RepositoryException {
            return event.getPath();
        }

        public String getUserID() {
            return event.getUserID();
        }

        public String getIdentifier() throws RepositoryException {
            if (identifier == null) {
                String path = event.getPath();
                if (event.getType() == PROPERTY_ADDED || event.getType() == PROPERTY_REMOVED || event.getType() == PROPERTY_CHANGED) {
                    path = StringUtils.substringBeforeLast(path,"/");
                }
                if (isExtensionNode(event.getPath())) {
                    try {
                        identifier = session.getNode(path).getIdentifier();
                    } catch (RepositoryException e) {
                        identifier = null;
                    }
                    return identifier;
                }
                identifier = event.getIdentifier();
            }
            return identifier;

        }

        @SuppressWarnings("rawtypes")
        public Map getInfo() throws RepositoryException {
            return event.getInfo();
        }

        public String getUserData() throws RepositoryException {
            return event.getUserData();
        }

        public long getDate() throws RepositoryException {
            return event.getDate();
        }

        public void setNodeTypes(List<String> nodeTypes) {
            this.nodeTypes = nodeTypes;
        }        
        
        public List<String> getNodeTypes() {
            return nodeTypes;
        }
        
        public boolean isExternal() {
            return event instanceof JackrabbitEvent ? ((JackrabbitEvent) event).isExternal()
                    : false;
        }
        
        /**
         * Returns <code>true</code> if this <code>Event</code> is equal to another
         * object.
         * <p/>
         * Two <code>Event</code> instances are equal if their respective
         * <code>EventState</code> instances are equal and both <code>Event</code>
         * instances are intended for the same <code>Session</code> that registerd
         * the <code>EventListener</code>.
         *
         * @param o the reference object with which to compare.
         * @return <code>true</code> if this <code>Event</code> is equal to another
         *         object.
         */
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            
            return event.equals(((EventWrapper)o).event);
        }
        
        @Override
        public String toString() {
            return event.toString();
        }
    }
}


