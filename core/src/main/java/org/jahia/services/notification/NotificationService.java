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

package org.jahia.services.notification;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.id.IdentifierUtils;
import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;

/**
 * Jahia service implementation for firing notification events and calling
 * appropriate event handlers.
 * 
 * @author Sergiy Shyrkov
 */
public class NotificationService extends JahiaService {

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
     * Register event handler.
     * 
     * @param handler
     *            event handler to be registered
     * @return the ID of the registered handler
     */
    public synchronized Long addHandler(NotificationEventHandler handler) {
        Long nextId = IdentifierUtils.nextLongIdentifier();
        handlers.put(nextId, handler);
        return nextId;
    }

    /**
     * Propagates the specified notification event to the handlers.
     * 
     * @param event
     *            the notification event occurred
     */
    public void fireEvent(NotificationEvent event) {
        List events = new LinkedList<NotificationEvent>();
        events.add(event);
        fireEvents(events);
    }

    /**
     * Propagates specified notification events to the handlers.
     * 
     * @param events
     *            the list of events to be fired
     */
    public void fireEvents(List<NotificationEvent> events) {
        if (!isStarted() || handlers.isEmpty()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Fired " + events.size() + " notification event(s): "
                    + events);
        } else {
            logger.info("Fired " + events.size() + " notification event(s)");
        }

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
                        handler.handle(eventsBySubscription.getKey(), eventsByEventType.getValue());
                    }
                }
            }
        }
    }

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

    /**
     * Removes the specified handler from the registry.
     * 
     * @param handlerId
     *            the handler to be removed
     * @return the removed handler or <code>null</code> if the handler was not
     *         in the registry
     */
    public synchronized NotificationEventHandler removeHandler(Long handlerId) {
        return (NotificationEventHandler) handlers.remove(handlerId);
    }

    /**
     * Removes the specified handler from the registry.
     * 
     * @param handler
     *            the handler to be removed
     * @return the removed handler or <code>null</code> if the handler was not
     *         in the registry
     */
    public synchronized NotificationEventHandler removeHandler(
            NotificationEventHandler handler) {
        return handlers.values().remove(handler) ? handler : null;
    }

    public void setHandlers(List<NotificationEventHandler> eventHandlers) {
        for (NotificationEventHandler handler : eventHandlers) {
            addHandler(handler);
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
