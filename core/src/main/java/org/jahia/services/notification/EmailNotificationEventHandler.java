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

import org.apache.log4j.Logger;
import org.jahia.services.mail.MailHelper;
import org.jahia.services.mail.MailService;
import org.jahia.services.notification.Subscription.Channel;
import org.jahia.services.notification.templates.SubscriberNotificationMessageBuilder;
import org.jahia.services.notification.templates.SubscriptionNotificationMessageBuilder;
import org.jahia.services.notification.templates.TemplateUtils;
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
                String emailAddress = MailHelper.getEmailAddress(user);
                if (emailAddress != null) {
                    if (!MailHelper.areEmailNotificationsDisabled(user)) {
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
                        subscription, user, MailHelper.getEmailAddress(user)));
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }
}