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
package org.jahia.services.observation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple Event service for sending synchronous events implementation
 */
public class JahiaEventServiceImpl implements JahiaEventService {
    private final static Logger logger = LoggerFactory.getLogger(JahiaEventServiceImpl.class);

    private final Set<JahiaEventListener<? extends EventObject>> allEventListeners = ConcurrentHashMap.newKeySet();
    // We store the listeners by type to avoid iterating over all listeners for each event
    private final Map<Class<? extends EventObject>, Set<JahiaEventListener<? extends EventObject>>> listenersByTypeCache = new ConcurrentHashMap<>();

    public void addEventListener(JahiaEventListener<? extends EventObject> listener) {
        if (listener == null) {
            return;
        }
        allEventListeners.add(listener);
        listenersByTypeCache.clear();
    }

    public void removeEventListener(JahiaEventListener<? extends EventObject> listener) {
        if (listener == null) {
            // This can happen when the list is created and no service are registered yet, we still get a call to this method
            return;
        }
        allEventListeners.remove(listener);
        listenersByTypeCache.clear();
    }

    Set<JahiaEventListener<? extends EventObject>> getApplicationListeners(EventObject event) {
        return listenersByTypeCache.computeIfAbsent(event.getClass(), this::resolveTypeListeners);
    }

    private Set<JahiaEventListener<? extends EventObject>> resolveTypeListeners(Class<? extends EventObject> eventClass) {
        // Here we resolve the listeners that are interested in the event type by also testing if the event class is
        // a subclass of any of the listener's accepted event types
        return allEventListeners.stream()
                .filter(listener -> {
                    Class<? extends EventObject>[] acceptedEventTypes = listener.getEventTypes();
                    return Stream.of(acceptedEventTypes).anyMatch(eventType -> eventType.isAssignableFrom(eventClass));
                })
                .collect(Collectors.toSet());
    }

    public void publishEvent(final EventObject event) {
        getApplicationListeners(event).forEach(listener -> {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                logger.error("Error thrown in listener", e);
            }
        });
    }

}
