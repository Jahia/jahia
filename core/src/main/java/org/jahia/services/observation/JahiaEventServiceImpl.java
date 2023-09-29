/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;

import java.util.EventObject;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple Event service for sending synchronous events implementation
 */
public class JahiaEventServiceImpl implements JahiaEventService {
    private final static Logger logger = LoggerFactory.getLogger(JahiaEventServiceImpl.class);
    private final Set<EventListenerAdapter> applicationListeners = new LinkedHashSet<>();

    public void addEventListener(JahiaEventListener<? extends EventObject> listener) {
        applicationListeners.add(new EventListenerAdapter(listener));
    }

    public void removeEventListener(JahiaEventListener<? extends EventObject> listener) {
        applicationListeners.removeAll(applicationListeners.stream().filter(e -> e.getDelegate() == listener).collect(Collectors.toSet()));
    }

    Stream<JahiaEventListener<? extends EventObject>> getApplicationListeners(EventObject event) {
        return applicationListeners.stream().filter(a -> a.canApply(event)).map(EventListenerAdapter::getDelegate);
    }

    public void publishEvent(final EventObject event) {
        getApplicationListeners(event).forEach(l -> {
            try {
                l.onEvent(event);
            } catch (Exception e) {
                logger.error("Error thrown in listener", e);
            }
        });
    }

    private static class EventListenerAdapter {
        private JahiaEventListener<? extends EventObject> delegate;
        private Class<?> typeArg;

        public EventListenerAdapter(JahiaEventListener<? extends EventObject> delegate) {
            this.delegate = delegate;
            this.typeArg = GenericTypeResolver.resolveTypeArgument(delegate.getProxiedClass(), JahiaEventListener.class);
        }

        public JahiaEventListener<? extends EventObject> getDelegate() {
            return delegate;
        }

        public boolean canApply(EventObject event) {
            return typeArg.isInstance(event);
        }
    }
}
