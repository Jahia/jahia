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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.notification;

import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.id.IdentifierUtils;
import org.apache.log4j.Logger;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaGroup;

/**
 * Jahia service implementation for firing notification events and calling
 * appropriate event handlers.
 * 
 * @author Sergiy Shyrkov
 */
public class NotificationService extends JahiaService {

    /**
     * Handler for the notification events.
     * 
     * @author Sergiy Shyrkov
     */
    public static class NotificationEventListener extends JahiaEventListener {

        @Override
        public void aggregatedNotification(JahiaEvent evt) {
            getInstance().handleEvents(
                    (List<NotificationEvent>) evt.getObject());
        }

        @Override
        protected void log(String eventType, JahiaEvent evt) {
            // do nothing
        }

        @Override
        public void notification(NotificationEvent evt) {
            // ignore single events --> handle aggregated
        }
    }

    private static final OrderedMap handlers = ListOrderedMap
            .decorate(new HashMap<Long, NotificationEventHandler>());

    private static NotificationService instance;

    private static Logger logger = Logger.getLogger(NotificationService.class);

    /**
     * Returns an instance of this service.
     * 
     * @return an instance of this service
     */
    public static NotificationService getInstance() {
        if (null == instance) {
            instance = new NotificationService();
        }

        return instance;
    }

    private boolean started;

    private SubscriptionService subscriptionService;

    /**
     * Returns a list of active subscriptions for the specified event.
     * 
     * @param event
     *            the notification event to be handled
     * @return a list of active subscriptions for the specified event
     */
    private List<Subscription> getSubscriptions(NotificationEvent event) {
        return subscriptionService.getActiveSubscriptions(event);
    }

    private Map<Principal, Map<String, List<NotificationEvent>>> groupByPrincipalAndEventType(
            Collection<NotificationEvent> events) {
        Map<Principal, Map<String, List<NotificationEvent>>> groupedEvents = LazyMap
                .decorate(
                        new HashMap<Principal, Map<String, List<NotificationEvent>>>(),
                        new Factory() {
                            public Object create() {
                                return LazyMap
                                        .decorate(
                                                new HashMap<String, List<NotificationEvent>>(),
                                                new Factory() {
                                                    public Object create() {
                                                        return new LinkedList<NotificationEvent>();
                                                    }
                                                });
                            }
                        });

        for (NotificationEvent evt : events) {
            for (Principal principal : evt.getSubscribers()) {
                groupedEvents.get(principal).get(evt.getEventType()).add(evt);
            }
        }

        return groupedEvents;
    }

    /**
     * Propagates specified notification events to the handlers.
     * 
     * @param events
     *            the list of events to be fired
     */
    public void handleEvents(List<NotificationEvent> events) {
        if (!isStarted() || handlers.isEmpty()) {
            return;
        }

        // let's split events which have predefined subscribers and not
        List<NotificationEvent> eventsWithPredefinedSubscribers = new LinkedList<NotificationEvent>(
                events);
        List<NotificationEvent> eventsForSubscriptions = new LinkedList<NotificationEvent>();
        for (Iterator<NotificationEvent> iterator = eventsWithPredefinedSubscribers
                .iterator(); iterator.hasNext();) {
            NotificationEvent evt = iterator.next();
            if (evt.getSubscribers().isEmpty()) {
                iterator.remove();
                eventsForSubscriptions.add(evt);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Found " + events.size() + " notification event(s). "
                    + eventsWithPredefinedSubscribers.size()
                    + " notification event(s) with predefined subscribers: "
                    + eventsWithPredefinedSubscribers + " and "
                    + eventsForSubscriptions.size()
                    + " notification event(s) for subscriptions: "
                    + eventsForSubscriptions);
        } else {
            logger.info("Found " + events.size() + " notification event(s). "
                    + eventsWithPredefinedSubscribers.size()
                    + " notification event(s) with predefined subscribers and "
                    + eventsForSubscriptions.size()
                    + " notification event(s) for subscriptions.");
        }

        handleEventsWithPredefinedSubscribers(eventsWithPredefinedSubscribers);
        handleEventsForSubscriptions(eventsForSubscriptions);
    }

    private void handleEventsForSubscriptions(List<NotificationEvent> events) {
        Map<Subscription, Map<String, List<NotificationEvent>>> subscriptionEvents = LazyMap
                .decorate(
                        new HashMap<Subscription, Map<String, List<NotificationEvent>>>(),
                        new Factory() {
                            public Object create() {
                                return LazyMap
                                        .decorate(
                                                new HashMap<String, List<NotificationEvent>>(),
                                                new Factory() {
                                                    public Object create() {
                                                        return new LinkedList<NotificationEvent>();
                                                    }
                                                });
                            }
                        });

        // group by subscription and event type
        for (NotificationEvent event : events) {
            // get list of subscribers
            for (Subscription evtSubscription : getSubscriptions(event)) {
                subscriptionEvents.get(evtSubscription).get(
                        event.getEventType()).add(event);
            }
        }

        // notify handlers
        for (Map.Entry<Subscription, Map<String, List<NotificationEvent>>> eventsBySubscription : subscriptionEvents
                .entrySet()) {
            for (Map.Entry<String, List<NotificationEvent>> eventsByEventType : eventsBySubscription
                    .getValue().entrySet()) {
                if (!eventsByEventType.getValue().isEmpty()) {
                    for (NotificationEventHandler handler : (Collection<NotificationEventHandler>) handlers
                            .values()) {
                        handler.handle(eventsBySubscription.getKey(),
                                eventsByEventType.getValue());
                    }
                }
            }
        }
    }

    private void handleEventsWithPredefinedSubscribers(
            List<NotificationEvent> events) {

        // 1) aggregate subscribers by event type and object key
        Map<String, NotificationEvent> aggregatedEvents = new HashMap<String, NotificationEvent>();
        for (NotificationEvent notificationEvent : events) {
            String key = notificationEvent.getEventType()
                    + notificationEvent.getObjectKey();
            if (!aggregatedEvents.containsKey(key)) {
                aggregatedEvents.put(key, notificationEvent);
            } else {
                aggregatedEvents.get(key).getSubscribers().addAll(
                        notificationEvent.getSubscribers());
            }
        }
        // 2) resolve principals
        resolvePrincipals(aggregatedEvents.values());
        // 3) group by principal and event type
        Map<Principal, Map<String, List<NotificationEvent>>> eventsByPrincipalAndEventType = groupByPrincipalAndEventType(aggregatedEvents
                .values());

        // notify handlers
        for (Map.Entry<Principal, Map<String, List<NotificationEvent>>> eventsByPrincipal : eventsByPrincipalAndEventType
                .entrySet()) {
            for (Map.Entry<String, List<NotificationEvent>> eventsByEventType : eventsByPrincipal
                    .getValue().entrySet()) {
                if (!eventsByEventType.getValue().isEmpty()) {
                    for (NotificationEventHandler handler : (Collection<NotificationEventHandler>) handlers
                            .values()) {
                        handler.handle(eventsByPrincipal.getKey(),
                                eventsByEventType.getValue());
                    }
                }
            }
        }
    }

    /**
     * Returns <code>true</code> if the notification service is started.
     * Otherwise returns <code>false</code> (this is normally the case or a
     * non-processing cluster node or if the service was explicitly disabled).
     * 
     * @return <code>true</code> if the notification service is started.
     *         Otherwise returns <code>false</code> (this is normally the case
     *         or a non-processing cluster node or if the service was explicitly
     *         disabled)
     */
    public boolean isStarted() {
        return started;
    }

    private void resolvePrincipals(Collection<NotificationEvent> events) {
        for (NotificationEvent evt : events) {
            Set<Principal> subscribers = evt.getSubscribers();
            Set<Principal> resolvedSubscribers = new HashSet<Principal>();
            for (Principal principal : subscribers) {
                if (principal instanceof JahiaGroup) {
                    resolvedSubscribers.addAll(((JahiaGroup) principal)
                            .getRecursiveUserMembers());
                } else {
                    resolvedSubscribers.add(principal);
                }
            }
            evt.getSubscribers().clear();
            evt.getSubscribers().addAll(resolvedSubscribers);
        }
    }

    public void setHandlers(List<NotificationEventHandler> eventHandlers) {
        for (NotificationEventHandler handler : eventHandlers) {
            handlers.put(IdentifierUtils.nextLongIdentifier(), handler);
        }
    }

    public void setSubscriptionService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public void start() throws JahiaInitializationException {
        if (!settingsBean.isProcessingServer()) {
            logger.info("Notification service will not be started"
                    + " as this node is not a processing server");
            return;
        }

        started = true;
    }

    @Override
    public void stop() throws JahiaException {
        started = false;
    }
}
