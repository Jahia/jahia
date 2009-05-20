/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
