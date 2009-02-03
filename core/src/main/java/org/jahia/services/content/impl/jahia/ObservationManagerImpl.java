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

package org.jahia.services.content.impl.jahia;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.log4j.Logger;
import org.jahia.services.workflow.WorkflowEvent;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.fields.ContentApplicationField;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.content.*;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.bin.Jahia;

import javax.jcr.observation.ObservationManager;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.Event;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Allows for the registration and deregistration of event listeners and converts Jahia events to JCR {@link Event} objects.
 * User: toto
 * Date: Jul 2, 2008
 * Time: 1:59:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ObservationManagerImpl implements ObservationManager {
    
    private static final transient Logger logger = Logger.getLogger(ObservationManagerImpl.class);
    
    private static Set observationManagers = new HashSet<ObservationManagerImpl>();

    public static void addObservationManager(ObservationManagerImpl o) {
        observationManagers.add(o);
    }

    public static void removeObservationManager(ObservationManagerImpl o) {
        observationManagers.remove(o);
    }

    private WorkspaceImpl workspace;
    private Map<EventListener,ListenerEntry> listeners;

    public ObservationManagerImpl(WorkspaceImpl workspace) {
        this.workspace = workspace;
        listeners = new HashMap<EventListener,ListenerEntry>();
    }

    public void addEventListener(EventListener listener, int eventTypes, String absPath, boolean isDeep, String[] uuid, String[] nodeTypeName, boolean noLocal) throws RepositoryException {
        listeners.put(listener, new ListenerEntry(listener, eventTypes, absPath, isDeep, uuid, nodeTypeName, noLocal));
    }

    public void removeEventListener(EventListener eventListener) throws RepositoryException {
        listeners.remove(listeners);
    }

    public EventListenerIterator getRegisteredEventListeners() throws RepositoryException {
        return new EventListenerIteratorImpl(new HashSet<EventListener>(listeners.keySet()).iterator(), listeners.size());                
    }


    public static void fireEvents(List events) {
        for (ObservationManagerImpl observationManager : (Iterable<ObservationManagerImpl>) observationManagers) {
            observationManager.fire(events);
        }
    }

    private void fire(List events) {
        List<Event> jcrEvents = new ArrayList<Event>();

        Set objectKeys = new HashSet();

        for (Iterator eventIterator = events.iterator(); eventIterator.hasNext();) {
            WorkflowEvent event = (WorkflowEvent) eventIterator.next();
            ContentObject object = (ContentObject) event.getObject();
            String path = null;
            try {
                if (object instanceof ContentContainerList) {
                    if (event.isNew() || object.isMarkedForDelete()) {
                        path = workspace.getSession().getJahiaContainerListNode((ContentContainerList) object).getPath();
                    }
                }
                if (object instanceof ContentContainer) {
                    if (event.isNew()|| object.isMarkedForDelete()) {
                        path = workspace.getSession().getJahiaContainerNode((ContentContainer) object).getPath();
                    }
                } else if (object instanceof ContentPage) {
                    if (((ContentPage)object).getPageType(null) == JahiaPage.TYPE_DIRECT) {
                        path = workspace.getSession().getJahiaPageNode((ContentPage) object).getPath();
                    }
                } else if (object instanceof ContentField) {
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            if (path != null) {
                int type = Event.NODE_ADDED;
                try {
                    if (object.isMarkedForDelete()) {
                        type = Event.NODE_REMOVED;
                    }
                } catch (JahiaException e) {
                    logger.warn(e.getMessage(), e);
                    // ??
                }
                Event e = new EventImpl(type, path, event.getUser().getUsername());

                objectKeys.add(object.getObjectKey());
                jcrEvents.add(e);
            }
        }

        for (Iterator eventIterator = events.iterator(); eventIterator.hasNext();) {
            WorkflowEvent event = (WorkflowEvent) eventIterator.next();
            ContentObject object = (ContentObject) event.getObject();
            String path = null;
            int type = Event.PROPERTY_CHANGED;
            try {
                if (object instanceof ContentField) {
                    try {
                        if (!object.isMarkedForDelete()) {
                            ContentObject parent = object.getParent(null,null,null);
                            if (parent != null && !objectKeys.contains(parent.getObjectKey())) {
                                if (parent instanceof ContentContainer) {
                                    path = workspace.getSession().getJahiaContainerNode((ContentContainer) parent).getPath();
                                } else if (parent instanceof ContentPage) {
                                    path = workspace.getSession().getJahiaPageNode((ContentPage) parent).getPath();
                                }
                                try {
                                    JahiaFieldDefinition def = (JahiaFieldDefinition) ContentDefinition.getContentDefinitionInstance(object.getDefinitionKey(null));
                                    path += "/" + def.getCtnType().split(" ")[1];
                                    String v = ((ContentField)object).getValue(Jahia.getThreadParamBean());
                                    if (object instanceof ContentPageField) {
                                        int pagetype = ((ContentPageField)object).getPage().getPageType(EntryLoadRequest.STAGED);
                                        if (v.equals("-1")) {
                                            if (event.isNew()) {
                                                path = null;
                                            } else {
                                                type = Event.NODE_REMOVED;
                                            }
                                        } else if (event.isNew()) {
                                            type = Event.NODE_ADDED;
                                        }else if (pagetype == JahiaPage.TYPE_URL) {
                                            path += "/j:url";
                                        } else if (pagetype == JahiaPage.TYPE_DIRECT || pagetype == JahiaPage.TYPE_LINK) {
                                            path += "/j:link";
                                        }
                                    } else if (object instanceof ContentApplicationField) {
                                        continue;
                                    }
                                    if (v == null || v.length() ==0) {
                                        type= Event.PROPERTY_REMOVED;
                                    }
                                } catch (ClassNotFoundException e) {
                                    logger.warn(e.getMessage(), e);
                                }
                            }
                        }
                    } catch (JahiaException e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
            if (path != null) {
                Event e = new EventImpl(type, path, event.getUser().getUsername());
                if (!jcrEvents.contains(e)) {
                    jcrEvents.add(e);
                }
            }
        }

        callListeners(jcrEvents);
    }

    public static void fireActivationEvents(List events) {
        for (ObservationManagerImpl observationManager : (Iterable<ObservationManagerImpl>) observationManagers) {
            observationManager.fireActivation(events);
        }
    }

    private void fireActivation(List events) {
        Set<Event> jcrEvents = new ListOrderedSet();

        for (Iterator iterator = events.iterator(); iterator.hasNext();) {
            ContentActivationEvent event = (ContentActivationEvent) iterator.next();
            ObjectKey key = (ObjectKey) event.getObject();
            try {
                String path = null;
                boolean deleted = false;
                if (key instanceof ContentContainerKey)  {
                    ContentContainer object = (ContentContainer) ContentContainer.getContentObjectInstance(key);
                    deleted = object.getLanguagesStates().isEmpty();
                    path = workspace.getSession().getJahiaContainerNode(object).getPath();

                } else if (key instanceof ContentPageKey) {
                    ContentPage object = (ContentPage) ContentPage.getContentObjectInstance(key);
                    deleted = object.getLanguagesStates().isEmpty();
                    if (object.getPageType(null) == JahiaPage.TYPE_DIRECT) {
                        path = workspace.getSession().getJahiaPageNode(object).getPath();
                    }
                } else if (key instanceof ContentFieldKey) {
                    ContentContainer object = (ContentContainer) ContentContainer.getContentObjectInstance(((ContentFieldKey)key).getParent(EntryLoadRequest.STAGED));
                    deleted = object.getLanguagesStates().isEmpty();
                    path = workspace.getSession().getJahiaContainerNode(object).getPath();
                }
                if (path != null) {
                    if (!deleted) {
                        int type = Event.PROPERTY_CHANGED;
                        Event e = new EventImpl(type, path+"/j:workflowState", event.getUser().getUsername());
                        jcrEvents.add(e);
                    } else {
                        int type = Event.NODE_REMOVED;
                        Event e = new EventImpl(type, path, event.getUser().getUsername());
                        jcrEvents.add(e);
                    }
                }

            } catch (ClassNotFoundException e) {
                logger.warn(e.getMessage(), e);
            } catch (RepositoryException e) {
                logger.warn(e.getMessage(), e);
            }
        }

        callListeners(new LinkedList(jcrEvents));
    }

    private void callListeners(List<Event> jcrEvents) {
        if (!jcrEvents.isEmpty()) {
            for (ListenerEntry listenerEntry : listeners.values()) {
                List<Event> jcrSelectedEvents = new ArrayList<Event>();

                for (Iterator<Event> eventIterator = jcrEvents.iterator(); eventIterator.hasNext();) {
                    Event event =  eventIterator.next();
                    if ((event.getType() & listenerEntry.eventTypes) == event.getType()) {
                        jcrSelectedEvents.add(event);
                    }
                }

                listenerEntry.eventListener.onEvent(new EventIteratorImpl(jcrSelectedEvents.iterator(), jcrSelectedEvents.size()));
            }
        }
    }


    class ListenerEntry {
        EventListener eventListener;
        int eventTypes;
        String absPath;
        boolean isDeep;
        String[] uuid;
        String[] nodeTypeName;
        boolean noLocal;

        ListenerEntry(EventListener eventListener, int eventTypes, String absPath, boolean deep, String[] uuid, String[] nodeTypeName, boolean noLocal) {
            this.eventListener = eventListener;
            this.eventTypes = eventTypes;
            this.absPath = absPath;
            isDeep = deep;
            this.uuid = uuid;
            this.nodeTypeName = nodeTypeName;
            this.noLocal = noLocal;
        }
    }

}
