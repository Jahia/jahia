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

import org.apache.log4j.Logger;
import org.jahia.services.mail.MailService;
import org.jahia.services.notification.Subscription.Channel;
import org.jahia.services.notification.templates.SubscriberNotificationMessageBuilder;
import org.jahia.services.notification.templates.SubscriptionNotificationMessageBuilder;
import org.jahia.services.notification.templates.TemplateUtils;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Handles behavior for notification type events.
 * 
 * @author Sergiy Shyrkov
 */
public class EmailNotificationEventHandler extends BaseNotificationEventHandler {

    private static Logger logger = Logger
            .getLogger(EmailNotificationEventHandler.class);

    private MailService mailService;

    /**
     * Initializes an instance of this class.
     */
    public EmailNotificationEventHandler() {
        this(new Condition[] {});
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param conditions
     */
    public EmailNotificationEventHandler(Condition... conditions) {
        super(conditions);
        // add conditions: 1) e-mail service active 2) channel is e-mail 3) all
        // conditions specified as the constructor parameter
        insertCondition(new Condition() {
            private boolean isMailServiceEnabled() {
                boolean enabled = mailService.isEnabled();
                if (!enabled) {
                    logger
                            .info("Mail service is disabled. Skip sending notification.");
                }
                return enabled;
            }

            public boolean matches(Principal subscriber,
                    List<NotificationEvent> events) {
                return isMailServiceEnabled();
            }

            public boolean matches(Subscription subscription,
                    List<NotificationEvent> events) {
                return isMailServiceEnabled();
            }

        }, new Condition() {
            public boolean matches(Principal subscriber,
                    List<NotificationEvent> events) {
                return true;
            }

            public boolean matches(Subscription subscription,
                    List<NotificationEvent> events) {
                return subscription.getChannel() == Channel.EMAIL;
            }
        }, new Condition() {
            private boolean checkUser(JahiaUser user) {
                boolean matches = false;
                String emailAddress = UserPreferencesHelper.getEmailAddress(user);
                if (emailAddress != null) {
                    if (!UserPreferencesHelper.areEmailNotificationsDisabled(user)) {
                        matches = true;
                    } else if (logger.isDebugEnabled()) {
                        logger.debug("The user '" + user.getUsername()
                                + "' has disabled e-mail notifications."
                                + " Skip sending notification.");
                    }
                } else if (logger.isDebugEnabled()) {
                    logger.debug("The user '" + user.getUsername()
                            + "' has not provided an e-mail address."
                            + " Skip sending notification.");
                }

                return matches;
            }

            public boolean matches(Principal subscriber,
                    List<NotificationEvent> events) {
                boolean matches = false;
                if (subscriber instanceof JahiaUser) {
                    matches = checkUser((JahiaUser) subscriber);
                } else {
                    logger
                            .warn("Subscriber is not an instace of the JahiaUser. Do not know, how to deal with the type: "
                                    + subscriber
                                    + ". Skip sending notification.");
                }
                return matches;
            }

            public boolean matches(Subscription subscription,
                    List<NotificationEvent> events) {
                boolean matches = false;
                JahiaUser user = TemplateUtils.getSubscriber(subscription);
                if (user != null) {
                    matches = checkUser(user);
                } else {
                    logger.warn("Cannot find user for the subscription: "
                            + subscription + ". Skip sending notification.");
                }
                return matches;
            }
        });
    }

    protected MailService getMailService() {
        return mailService;
    }

    @Override
    protected void handleEvents(Principal subscriber,
            List<NotificationEvent> events) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handling " + events.size() + " notification events: "
                    + events + " for subscriber: " + subscriber);
        } else {
            logger.info("Handling " + events.size()
                    + " notification event(s) for subscriber "
                    + subscriber.getName());
        }

        getMailService().sendTemplateMessage(
                new SubscriberNotificationMessageBuilder(
                        (JahiaUser) subscriber, events));
    }

    @Override
    protected void handleEvents(Subscription subscription,
            List<NotificationEvent> events) {

        if (logger.isDebugEnabled()) {
            logger.debug("Handling  " + events.size()
                    + " notification event(s): " + events
                    + " for subscription: " + subscription);
        } else {
            logger.info("Handling " + events.size()
                    + " notification event(s) for subscription "
                    + subscription.getUsername());
        }

        JahiaUser user = TemplateUtils.getSubscriber(subscription);
        getMailService().sendTemplateMessage(
                new SubscriptionNotificationMessageBuilder(events,
                        subscription, user, UserPreferencesHelper.getEmailAddress(user)));
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }
}