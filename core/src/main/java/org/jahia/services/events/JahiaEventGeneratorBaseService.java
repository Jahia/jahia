/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
//
//  JahiaEventGeneratorBaseService
//  EV      12.01.2001
//
//
//  fireUpdateField( theEvent )
//  fireAddContainer( theEvent )
//  fireUpdateContainer( theEvent )
//  fireDeleteContainer( theEvent )
//  fireAddPage( theEvent )
//  fireSetRights( theEvent )
//  fireLogin( theEvent )
//  fireLogout( theEvent )
//

package org.jahia.services.events;

import org.apache.log4j.Logger;
import org.jahia.data.events.JahiaEvent;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.JahiaListenersRegistry;
import org.jahia.services.notification.NotificationEvent;
import org.springframework.util.StringUtils;

import java.util.*;

public class JahiaEventGeneratorBaseService extends JahiaEventGeneratorService {

    private static JahiaEventGeneratorBaseService theObject;

    private static Logger logger = Logger.getLogger (JahiaEventGeneratorBaseService.class);

    private ThreadLocal<List<MethodWithEvent>> tlevents = new ThreadLocal<List<MethodWithEvent>>();

    private Map<String, Object> aggregatedEventWeigth = new HashMap<String, Object>();
    private static final int AGGREGATED_EVENT_DEFAULT_WEIGHT = 1;
    
    private JahiaListenersRegistry listenersRegistry;

    public void setListenersRegistry(JahiaListenersRegistry listenersRegistry) {
        this.listenersRegistry = listenersRegistry;
    }


    /**
     * constructor
     */
    protected JahiaEventGeneratorBaseService () {
        super();
    }


    /**
     * returns an instance of the JahiaEventGeneratorBaseService
     */
    public static synchronized JahiaEventGeneratorBaseService getInstance () {
        if (theObject == null) {
            theObject = new JahiaEventGeneratorBaseService ();
        }
        return theObject;
    }

    public void start() {}

    public void stop() {}

    private void addAggregableEvent(String method, JahiaEvent event) {
        List<MethodWithEvent> events = tlevents.get();
        if (events == null) {
            events = new ArrayList<MethodWithEvent>();
            tlevents.set(events);
        }
        MethodWithEvent methodWithEvent = new MethodWithEvent(method, event);
        events.add(methodWithEvent);

        if (events.size() > org.jahia.settings.SettingsBean.getInstance().getMaxAggregatedEvents()) {
            fireAggregatedEvents();
        }
    }

    public void fireAggregatedEvents() {
        logger.debug("Firing aggregated events ..");
        List<MethodWithEvent> events = tlevents.get();
        if (events != null) {
            for (; !events.isEmpty(); ) {
                if (logger.isDebugEnabled())
                	logger.debug("Fire aggregate for events : "+events);
                Map<String, AggregatedEvents> aggregateEvents = new HashMap<String, AggregatedEvents>();
                for (; !events.isEmpty(); ) {
                    MethodWithEvent methodWithEvent = (MethodWithEvent) events.remove(0);
                    AggregatedEvents  aggEvent = (AggregatedEvents)aggregateEvents.get(methodWithEvent.name);
                    if ( aggEvent == null ){
                        aggEvent = new AggregatedEvents(methodWithEvent.name);
                        aggregateEvents.put(methodWithEvent.name,aggEvent);
                    }
                    aggEvent.getEvents().add(methodWithEvent.event);
                }
                SortedSet<AggregatedEvents> orderedEvents = new TreeSet<AggregatedEvents>(new AggregateEventComparator());
                orderedEvents.addAll(aggregateEvents.values());
                for (AggregatedEvents aggEvent : orderedEvents) {
                    JahiaEvent event = new JahiaEvent(this,null,aggEvent.getEvents());
                    listenersRegistry.wakeupListeners ("aggregated"
                            +StringUtils.capitalize(aggEvent.getName()),event);
                }
            }
        }
        logger.debug("Flushing events ..");
        if ( events != null ){
            JahiaEvent event = new JahiaEvent(this,null,events);
            fireAggregatedEventsFlush(event);
        }    
    }

    public void fireAggregatedEventsFlush(JahiaEvent theEvent) {
        listenersRegistry.wakeupListeners ("aggregatedEventsFlush", theEvent);
    }

    public void fireSiteAdded (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("siteAdded", theEvent);
    }

    public void fireSiteDeleted (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("siteDeleted", theEvent);
    }

    public void fireBeforeFieldActivation (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("beforeFieldActivation", theEvent);
    }

    public void fireAddField (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("fieldAdded", theEvent);
    }

    public void fireUpdateField (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("fieldUpdated", theEvent);
    }

    public void fireDeleteField (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("fieldDeleted", theEvent);
    }

    public void fireBeforeContainerActivation (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("beforeContainerActivation", theEvent);
    }

    public void fireAddContainerEngineBeforeSave (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("addContainerEngineBeforeSave", theEvent);
    }

    public void fireContainerValidation (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("containerValidation", theEvent);
    }    
      
    public void fireAddContainerEngineAfterSave (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("addContainerEngineAfterSave", theEvent);
    }

    public void fireAddContainerEngineAfterInit (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("addContainerEngineAfterInit", theEvent);
    }

    public void fireAddContainer (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("containerAdded", theEvent);
    }

    public void fireUpdateContainerEngineBeforeSave (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("updateContainerEngineBeforeSave", theEvent);
    }

    public void fireUpdateContainerEngineAfterInit (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("updateContainerEngineAfterInit", theEvent);
    }

    public void fireUpdateContainer (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("containerUpdated", theEvent);
    }

    public void fireDeleteContainer (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("containerDeleted", theEvent);
    }


    public void fireSetPageProperties (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("pagePropertiesSet", theEvent);
    }

    /**
     * Event fired when a user property is set
     */
    public void fireUserPropertiesSet (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("userPropertiesSet", theEvent);
    }

    public void fireSetContainerListProperties (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("containerListPropertiesSet", theEvent);
    }


    public void fireSetRights (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("rightsSet", theEvent);
    }


    public void fireAddPage (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("pageAdded", theEvent);
    }


    public void fireLoadPage (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("pageLoaded", theEvent);
    }

    /**
     * Triggers the <code>pageLoadedFromCache</code> event
     * @param theEvent the event object
     * @throws JahiaException in case an error occurs invoking listeners
     */
    public void fireLoadPageFromCache (JahiaEvent theEvent) {
        listenersRegistry.wakeupListeners ("pageLoadedFromCache", theEvent);
    }


    public void fireLogin (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("userLoggedIn", theEvent);
    }

    public void fireLogout (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("userLoggedOut", theEvent);
    }

    public void fireUpdateTemplate (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("templateUpdated", theEvent);
    }

    public void fireUpdateCategory (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("categoryUpdated", theEvent);
    }

    public void fireObjectChanged (JahiaEvent theEvent) throws JahiaException {
        addAggregableEvent("objectChanged", theEvent);
        listenersRegistry.wakeupListeners ("objectChanged", theEvent);
    }

    public void fireBeforeStagingContentIsDeleted (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("beforeStagingContentIsDeleted", theEvent);
    }

    /**
     * Event fired after the Metadata_Engine has been initialized ( engineMap init )
     * and before processing last and current engine request.
     * The Event source object is the calling Metadata_Engine, the event object is a
     * ContentMetadataFacade instance.
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public void fireMetadataEngineAfterInit (JahiaEvent theEvent)
            throws JahiaException {
        addAggregableEvent("metadataEngineAfterInit", theEvent);
        listenersRegistry.wakeupListeners ("metadataEngineAfterInit", theEvent);
    }

    /**
     * Event fired before the Metadata_Engine start to save the metadata fields for
     * the current content metadata facade
     * The Event source object is the calling Metadata_Engine, the event object is a
     * ContentMetadataFacade instance.
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public void fireMetadataEngineBeforeSave (JahiaEvent theEvent) throws JahiaException {
        addAggregableEvent("metadataEngineBeforeSave", theEvent);
        listenersRegistry.wakeupListeners ("metadataEngineBeforeSave", theEvent);
    }

    /**
     * Event fired after the Metadata_Engine has saved the metadata fields for
     * the current content object
     * The Event source object is the calling Metadata_Engine, the event object is
     * the ObjectKey instance of the content object.
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public void fireMetadataEngineAfterSave (JahiaEvent theEvent) throws JahiaException {
        addAggregableEvent("metadataEngineAfterSave", theEvent);
        listenersRegistry.wakeupListeners ("metadataEngineAfterSave", theEvent);
    }

    /**
     * Event fired before WorkflowService.activate(...)
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public void fireAfterGroupActivation (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("afterGroupActivation", theEvent);
    }

    /**
     * Event fired after ContentObject.activate(...)
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public void fireContentActivation (JahiaEvent theEvent) throws JahiaException {
        addAggregableEvent("contentActivation", theEvent);
        listenersRegistry.wakeupListeners ("contentActivation", theEvent);
    }

    public void fireContentWorkflowStatusChanged (JahiaEvent theEvent) throws JahiaException {
        addAggregableEvent("contentWorkflowStatusChanged", theEvent);
        listenersRegistry.wakeupListeners ("contentWorkflowStatusChanged", theEvent);
    }

    /**
     * Should be fired once a content object has been first created ( stored in persistence )
     * The Event source object is the JahiaUser
     * The Object is the ContentObject created.
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public void fireContentObjectCreated (JahiaEvent theEvent)
    throws JahiaException{
        addAggregableEvent("contentObjectCreated", theEvent);
        listenersRegistry.wakeupListeners ("contentObjectCreated", theEvent);
    }

    /**
     * Should be fired once a content object has been updated ( changes stored in persistence )
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public void fireContentObjectUpdated (JahiaEvent theEvent)
    throws JahiaException{
        listenersRegistry.wakeupListeners ("contentObjectUpdated", theEvent);
    }

    /**
     * fired by ContentObject.undoStaging()
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public void fireContentObjectUndoStaging (JahiaEvent theEvent)
    throws JahiaException {
        listenersRegistry.wakeupListeners ("contentObjectUndoStaging", theEvent);
    }

    /**
     * fire on content object delete
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public void fireContentObjectDelete (JahiaEvent theEvent)
    throws JahiaException {
        listenersRegistry.wakeupListeners ("contentObjectDelete", theEvent);
    }

    public void fireFileManagerAclChanged (JahiaEvent theEvent)
    throws JahiaException{
        listenersRegistry.wakeupListeners ("fileManagerAclChanged", theEvent);
    }

    public void fireTimeBasedPublishingStateChange(JahiaEvent theEvent)
    throws JahiaException {
        listenersRegistry.wakeupListeners ("timeBasedPublishingEvent", theEvent);
    }

    public void fireContentObjectRestoreVersion(JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("contentObjectRestoreVersion", theEvent);
    }

    // ==================================================================
    // Nicol�s Charczewski - Neoris Argentina - added 28/03/2006 - begin
    public void fireDeletePage (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("pageDeleted", theEvent);
    }

    public void fireAcceptPage (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("pageAccepted", theEvent);
    }

    public void fireRejectPage (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("pageRejected", theEvent);
    }

    public void fireAddUser(JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("userAdded", theEvent);
    }

    public void fireDeleteUser(JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("userDeleted", theEvent);
    }
    
    public void fireUpdateUser(JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("userUpdated", theEvent);
    }
    
    public void fireAddGroup(JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("groupAdded", theEvent);
    }

    public void fireDeleteGroup(JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("groupDeleted", theEvent);
    }

    public void fireUpdateGroup(JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("groupUpdated", theEvent);
    }

    public void fireAddTemplate (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("templateAdded", theEvent);
    }

    public void fireDeleteTemplate (JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("templateDeleted", theEvent);
    }
    
    public Map<String, Object> getAggregatedEventWeigth() {
        return aggregatedEventWeigth;
    }

    public void setAggregatedEventWeigth(Map<String, Object> aggregatedEventWeigth) {
        this.aggregatedEventWeigth = aggregatedEventWeigth;
    }

    // Nicol�s Charczewski - Neoris Argentina - added 28/03/2006 - end
    
    public void fireErrorOccurred(JahiaEvent je) {
        listenersRegistry.wakeupListeners ("errorOccurred", je);
    }
    
    // ==================================================================

    class MethodWithEvent {
        String name;
        JahiaEvent event;

        public MethodWithEvent(String aName, JahiaEvent anEvent) {
            this.name = aName;
            this.event = anEvent;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final MethodWithEvent that = (MethodWithEvent) o;

            if (!event.equals(that.event)) return false;
            if (!name.equals(that.name)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = name.hashCode();
            result = 29 * result + event.hashCode();
            return result;
        }


        public String toString() {
            return "MethodWithEvent{" +
                   "name='" + name + '\'' +
                   ", event=" + event +
                   '}';
        }
    }

    class AggregatedEvents {

        String name;
        List<JahiaEvent> events;
        int weight = AGGREGATED_EVENT_DEFAULT_WEIGHT;

        public AggregatedEvents(String aName) {
            this.name = aName;
            Object weightVal = getAggregatedEventWeigth().get(aName);
            if ( weightVal != null ){
                try {
                    weight = Integer.parseInt(weightVal.toString());
                } catch (Exception t){
                }
            }
            this.events = new ArrayList<JahiaEvent>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<JahiaEvent> getEvents() {
            return events;
        }

        public void setEvents(List<JahiaEvent> events) {
            this.events = events;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }
    }

    class AggregateEventComparator implements Comparator<AggregatedEvents> {
        public int compare(AggregatedEvents agEv1, AggregatedEvents agEv2){
            if ( agEv2.getWeight()>agEv1.getWeight() ){
                return 1;
            }
            return -1;
        }
    }

    @Override
    public void fireNotification(NotificationEvent theEvent) {
        addAggregableEvent("notification", theEvent);
        listenersRegistry.wakeupListeners("notification", theEvent);
    }


    @Override
    public void fireBeforeContentCopy(JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("beforeContentCopy", theEvent);
    }


    @Override
    public void fireBeforeFormHandling(JahiaEvent theEvent) throws JahiaException {
        listenersRegistry.wakeupListeners ("beforeFormHandling", theEvent);
    }
}
