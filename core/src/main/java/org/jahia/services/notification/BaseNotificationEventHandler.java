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
package org.jahia.services.notification;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;

/**
 * Base class for notification handlers that supports restrictions on channel,
 * object key, object path etc.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class BaseNotificationEventHandler implements
        NotificationEventHandler {

    public interface Condition {
        /**
         * Returns <code>true</code> if the condition is satisfied and
         * processing can be continued.
         * 
         * @param subscriber
         *            the subscriber information
         * @param events
         *            list of event matching the subscription
         * @return <code>true</code> if the condition is satisfied and
         *         processing can be continued
         */
        boolean matches(Principal subscriber, List<NotificationEvent> events);

        /**
         * Returns <code>true</code> if the condition is satisfied and
         * processing can be continued.
         * 
         * @param subscription
         *            the subscriber information
         * @param events
         *            list of event matching the subscription
         * @return <code>true</code> if the condition is satisfied and
         *         processing can be continued
         */
        boolean matches(Subscription subscription,
                List<NotificationEvent> events);
    }

    private Condition[] conditions = new Condition[] {};

    /**
     * Initializes an instance of this class.
     * 
     * @param conditions
     *            conditions to be matched for processing to continue
     */
    public BaseNotificationEventHandler(Condition... conditions) {
        super();
        this.conditions = conditions;
    }

    public void addCondition(Condition... conditions) {
        this.conditions = (Condition[]) ArrayUtils.addAll(this.conditions,
                conditions);
    }

    private boolean areConditionsMatched(Principal subscriber,
            List<NotificationEvent> events) {
        boolean matches = true;
        for (Condition condition : conditions) {
            if (!condition.matches(subscriber, events)) {
                matches = false;
                break;
            }
        }
        return matches;
    }

    private boolean areConditionsMatched(Subscription subscription,
            List<NotificationEvent> events) {
        boolean matches = true;
        for (Condition condition : conditions) {
            if (!condition.matches(subscription, events)) {
                matches = false;
                break;
            }
        }
        return matches;
    }

    public final void handle(Principal subscriber,
            List<NotificationEvent> events) {
        if (!areConditionsMatched(subscriber, events)) {
            return;
        }
        handleEvents(subscriber, events);
    }

    public final void handle(Subscription subscription,
            List<NotificationEvent> events) {
        if (!areConditionsMatched(subscription, events)) {
            return;
        }
        handleEvents(subscription, events);
    }

    protected abstract void handleEvents(Principal subscriber,
            List<NotificationEvent> events);

    protected abstract void handleEvents(Subscription subscription,
            List<NotificationEvent> events);

    public void insertCondition(Condition... conditions) {
        this.conditions = (Condition[]) ArrayUtils.addAll(conditions,
                this.conditions);
    }

    public void setIgnoreEventTypes(final Set<String> ignoreEventTypes) {
        if (ignoreEventTypes != null && !ignoreEventTypes.isEmpty()) {
            addCondition(new Condition() {
                public boolean matches(Principal subscriber,
                        List<NotificationEvent> events) {
                    return !ignoreEventTypes.contains(events.get(0)
                            .getEventType());
                }

                public boolean matches(Subscription subscription,
                        List<NotificationEvent> events) {
                    return !ignoreEventTypes.contains(events.get(0)
                            .getEventType());
                }
            });
        }
    }

    public void setIgnoreEventTypesPattern(String ignoreEventTypesPattern) {
        if (ignoreEventTypesPattern != null
                && ignoreEventTypesPattern.length() != 0) {
            final Pattern pattern = Pattern.compile(ignoreEventTypesPattern);
            addCondition(new Condition() {
                public boolean matches(Principal subscriber,
                        List<NotificationEvent> events) {
                    return !pattern.matcher(events.get(0).getEventType())
                            .matches();
                }

                public boolean matches(Subscription subscription,
                        List<NotificationEvent> events) {
                    return !pattern.matcher(events.get(0).getEventType())
                            .matches();
                }
            });
        }
    }

    public void setProcessEventTypes(final List<String> processEventTypes) {
        if (processEventTypes != null && !processEventTypes.isEmpty()) {
            addCondition(new Condition() {
                public boolean matches(Principal subscriber,
                        List<NotificationEvent> events) {
                    return processEventTypes.contains(events.get(0)
                            .getEventType());
                }

                public boolean matches(Subscription subscription,
                        List<NotificationEvent> events) {
                    return processEventTypes.contains(events.get(0)
                            .getEventType());
                }
            });
        }
    }

    public void setProcessEventTypesPattern(String processEventTypesPattern) {
        if (processEventTypesPattern != null
                && processEventTypesPattern.length() != 0) {
            final Pattern pattern = Pattern.compile(processEventTypesPattern);
            addCondition(new Condition() {
                public boolean matches(Principal subscriber,
                        List<NotificationEvent> events) {
                    return pattern.matcher(events.get(0).getEventType())
                            .matches();
                }

                public boolean matches(Subscription subscription,
                        List<NotificationEvent> events) {
                    return pattern.matcher(events.get(0).getEventType())
                            .matches();
                }
            });
        }
    }

}
