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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.content.ContentObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.SubscriptionManager;
import org.jahia.services.JahiaService;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.mail.MailService;
import org.jahia.services.notification.templates.SubscriptionConfirmationMessageBuilder;
import org.jahia.services.notification.templates.TemplateUtils;
import org.jahia.services.notification.templates.UnsubscribeConfirmationMessageBuilder;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Jahia service for managing user subscriptions to different event types.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscriptionService extends JahiaService {

    public enum ConfirmationResult {
        ALREADY_CONFIRMED, CONFIRMATION_KEY_MISMATCH, OK, SUBSCRIPTION_NOT_FOUND;
    }

    private static Logger logger = Logger.getLogger(SubscriptionService.class);

    private static SubscriptionService service;

    /**
     * Returns an instance of this service.
     * 
     * @return an instance of this service
     */
    public static SubscriptionService getInstance() {
        if (service == null) {
            synchronized (SubscriptionService.class) {
                if (service == null) {
                    service = new SubscriptionService();
                }
            }
        }

        return service;
    }

    private MailService mailService;

    private SubscriptionManager subscriptionManager;

    /**
     * Cancels the user subscription.
     * 
     * @param subscriptionId
     *            the id of the subscription to cancel
     * @param confirmationKey
     *            the confirmation key
     */
    public ConfirmationResult cancelSubscription(int subscriptionId,
            String confirmationKey) {

        ConfirmationResult status = ConfirmationResult.OK;

        Subscription subscription = subscriptionManager
                .getSubscription(subscriptionId);
        if (subscription != null) {
            if (subscription.getConfirmationKey() != null
                    && subscription.getConfirmationKey()
                            .equals(confirmationKey)) {
                subscriptionManager.delete(subscription);
            } else {
                status = ConfirmationResult.CONFIRMATION_KEY_MISMATCH;
            }
        } else {
            status = ConfirmationResult.SUBSCRIPTION_NOT_FOUND;
        }

        return status;
    }

    /**
     * Confirms the user subscription as the result of the identity validation
     * process.
     * 
     * @param subscriptionId
     *            the id of the subscription to confirm
     * @param confirmationKey
     *            the confirmation key
     */
    public ConfirmationResult confirmSubscription(int subscriptionId,
            String confirmationKey) {

        ConfirmationResult status = ConfirmationResult.OK;

        Subscription subscription = subscriptionManager
                .getSubscription(subscriptionId);
        if (subscription != null) {
            if (!subscription.isEnabled()) {
                if (subscription.getConfirmationKey() != null
                        && subscription.getConfirmationKey().equals(
                                confirmationKey)) {
                    subscription.setEnabled(true);
                    subscription.setConfirmationKey(null);
                    subscription.setConfirmationRequestTimestamp(0);
                    subscriptionManager.update(subscription);
                } else {
                    status = ConfirmationResult.CONFIRMATION_KEY_MISMATCH;
                }
            } else {
                status = ConfirmationResult.ALREADY_CONFIRMED;
            }
        } else {
            status = ConfirmationResult.SUBSCRIPTION_NOT_FOUND;
        }

        return status;
    }

    /**
     * Generates and stores in the database the key to be used for subscription
     * confirmation.
     * 
     * @param subscription
     *            the subscription to be confirmed
     * @return the confirmation key to be sent to the user
     */
    private String generateConfirmationKey(Subscription subscription) {

        String key = DigestUtils.md5Hex(subscription.getId()
                + subscription.getSiteId() + subscription.getObjectKey()
                + subscription.getEventType() + subscription.getUsername()
                + subscription.getChannel());

        subscription.setConfirmationKey(key);
        subscription
                .setConfirmationRequestTimestamp(System.currentTimeMillis());

        subscriptionManager.update(subscription);

        return key;
    }

    /**
     * Returns a list of active subscriptions for the occurred event.
     * 
     * @param event
     *            the occurred notification event
     * @return a list of active subscriptions for the occurred event
     */
    public List<Subscription> getActiveSubscriptions(NotificationEvent event) {
        Subscription criteria = new Subscription(event.getObjectKey(), true,
                event.getEventType(), null, event.getSiteId());
        criteria.setEnabled(true);
        criteria.setSuspended(false);

        List<Subscription> subscriptions = new LinkedList<Subscription>();
        subscriptions.addAll(subscriptionManager.getSubscriptions(criteria,
                "includeChildren", "userRegistered"));

        ContentObjectKey contentObjectKey = null;
        try {
            contentObjectKey = (ContentObjectKey) ContentObjectKey
                    .getInstance(event.getObjectKey());
        } catch (ClassNotFoundException e) {
            // not a content object key
        }
        String[] objectKeys = contentObjectKey != null ? JCRContentUtils
                .getNodeTypeNamesWithSuperTypes(contentObjectKey)
                : new String[] { event.getObjectKey() };

        objectKeys = (String[]) ArrayUtils.addAll(StringUtils.split(event
                .getObjectPath(), "/"), objectKeys);
        subscriptions.addAll(subscriptionManager.getParentSubscriptions(
                criteria, objectKeys));

        return subscriptions;
    }

    /**
     * Returns the requested user subscription by ID or <code>null</code> if
     * there is no such subscription available.
     * 
     * @param subscriptionId
     *            the ID of the subscription
     * @return the requested user subscription by ID or <code>null</code> if
     *         there is no such subscription available
     */
    public Subscription getSubscription(int subscriptionId) {
        return subscriptionManager.getSubscription(subscriptionId);
    }

    /**
     * Returns the requested user subscription or <code>null</code> if there is
     * no such subscription available.
     * 
     * @param objectKey
     *            the key of the content object that is the source of events
     * @param eventType
     *            the type of an event to be notified about
     * @param username
     *            the user to be notified
     * @param siteId
     *            the ID of the site owning the content object in question
     * @return the requested user subscription or <code>null</code> if there is
     *         no such subscription available
     */
    public Subscription getSubscription(String objectKey, String eventType,
            String username, int siteId) {

        List<Subscription> subscriptions = subscriptionManager
                .getSubscriptions(new Subscription(objectKey, false, eventType,
                        username, siteId), "includeChildren", "enabled",
                        "suspended", "confirmationKey",
                        "confirmationRequestTimestamp");
        return !subscriptions.isEmpty() ? subscriptions.get(0) : null;
    }

    /**
     * Find subscriptions "by example".
     * 
     * @param criteria
     *            the {@link Subscription} object template with the available
     *            search criteria
     * @param excludedProperties
     *            properties to ignore in the criteria
     * @return list of {@link Subscription} objects matching the criteria
     */
    public List<Subscription> getSubscriptionsByCriteria(Subscription criteria,
            String... excludedProperties) {

        return subscriptionManager.getSubscriptions(criteria,
                excludedProperties);
    }

    /**
     * Returns <code>true</code> if the subscriptions entry with the specified
     * data exists.
     * 
     * @param objectKey
     *            the key of the content object that is the source of events
     * @param eventType
     *            the type of an event to be notified about
     * @param username
     *            the user to be notified
     * @param siteId
     *            the ID of the site owning the content object in question
     * @return <code>true</code> if the subscriptions entry with the specified
     *         data exists
     */
    public boolean isSubscribed(String objectKey, String eventType,
            String username, int siteId) {

        return !subscriptionManager
                .getSubscriptions(
                        new Subscription(objectKey, false, eventType, username,
                                siteId), "includeChildren", "enabled",
                        "suspended", "confirmationKey",
                        "confirmationRequestTimestamp").isEmpty();
    }

    /**
     * Sends an e-mail requesting for user confirmation.
     * 
     * @param subscription
     *            the subscription object
     * @param subscriptionRequest
     *            if this is a subscription request (contrary to unsubscribe
     *            request)
     */
    private void sendConfirmationRequest(Subscription subscription,
            boolean subscriptionRequest) {
        if (!mailService.isEnabled()) {
            logger
                    .warn("Cannot send a subscription confirmation request"
                            + " as the mail server service is either disabled or not configured yet");
            return;
        }

        JahiaUser user = TemplateUtils.getSubscriber(subscription);
        if (user != null) {
            final String emailAddress = UserPreferencesHelper.getEmailAddress(user);
            if (emailAddress != null) {
                // generate and store confirmation key
                generateConfirmationKey(subscription);

                mailService
                        .sendTemplateMessage(subscriptionRequest ? new SubscriptionConfirmationMessageBuilder(
                                user, emailAddress, subscription)
                                : new UnsubscribeConfirmationMessageBuilder(
                                        user, emailAddress, subscription));
            } else {
                logger.debug("The user '" + user.getUsername()
                        + "' has not provided an e-mail address."
                        + " Skip sending confirmation.");
            }
        } else {
            logger.warn("Cannot find user for the subscription: "
                    + subscription + ". Skip sending confirmation.");
        }

    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public void setSubscriptionManager(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.JahiaService#start()
     */
    public void start() throws JahiaInitializationException {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.JahiaService#stop()
     */
    public void stop() throws JahiaException {
        // do nothing
    }

    /**
     * Subscribes the current user to the events of the specified type.
     * 
     * @param objectKey
     *            the key of the content object that is the source of events
     * @param includeChildren
     *            do we also capture event on the child objects?
     * @param eventType
     *            the type of an event to be notified about
     * @param username
     *            the user to be notified
     * @param userRegistered
     *            is user registered in Jahia?
     * @param siteId
     *            the ID of the site owning the content object in question
     * @param enabled
     *            if this subscription is immediately enabled
     * @param properties
     *            any custom user data
     * @return the created subscription object
     */
    public Subscription subscribe(String objectKey, boolean includeChildren,
            String eventType, String username, boolean userRegistered,
            int siteId, boolean enabled, Map<String, String> properties) {
        Subscription template = new Subscription(objectKey, includeChildren,
                eventType, username, siteId, enabled);
        template.setUserRegistered(userRegistered);
        template.setProperties(properties);

        List<Subscription> existingSubscriptions = subscriptionManager
                .getSubscriptions(template, "includeChildren", "enabled",
                        "suspended", "confirmationKey",
                        "confirmationRequestTimestamp", "properties");
        if (!existingSubscriptions.isEmpty()) {
            // found subscription
            Subscription found = existingSubscriptions.get(0);
            if (found.isIncludeChildren() != template.isIncludeChildren()
                    || found.isEnabled() != template.isEnabled()
                    || found.isSuspended() != template.isSuspended()) {
                found.setIncludeChildren(template.isIncludeChildren());
                found.setSuspended(template.isSuspended());
                found.setEnabled(template.isEnabled());
                subscriptionManager.update(found);
            }
            template = found;
        } else {
            return subscriptionManager.update(template);
        }

        return template;
    }

    /**
     * Subscribes the current user to the events of the specified type.
     * 
     * @param objectKey
     *            the key of the content object that is the source of events
     * @param includeChildren
     *            do we also capture event on the child objects?
     * @param eventType
     *            the type of an event to be notified about
     * @param username
     *            the user to be notified
     * @param siteId
     *            the ID of the site owning the content object in question
     * @return the created subscription object
     */
    public Subscription subscribe(String objectKey, boolean includeChildren,
            String eventType, String username, int siteId) {
        return subscribe(objectKey, includeChildren, eventType, username, true,
                siteId, true, null);
    }

    /**
     * Subscribes the current user to the events of the specified type and send
     * an e-mail asking for confirmation.
     * 
     * @param objectKey
     *            the key of the content object that is the source of events
     * @param includeChildren
     *            do we also capture event on the child objects?
     * @param eventType
     *            the type of an event to be notified about
     * @param username
     *            the user to be notified
     * @param userRegistered
     *            is user registered in Jahia?
     * @param siteId
     *            the ID of the site owning the content object in question
     * @param properties
     *            any custom user data
     * @return the created subscription object
     */
    public Subscription subscribeAndAskForConfirmation(String objectKey,
            boolean includeChildren, String eventType, String username,
            boolean userRegistered, int siteId, Map<String, String> properties) {
        Subscription subscription = subscribe(objectKey, includeChildren,
                eventType, username, userRegistered, siteId, false, properties);

        sendConfirmationRequest(subscription, true);

        return subscription;
    }

    /**
     * Subscribes the current user to the events of the specified type and send
     * an e-mail asking for confirmation.
     * 
     * @param objectKey
     *            the key of the content object that is the source of events
     * @param includeChildren
     *            do we also capture event on the child objects?
     * @param eventType
     *            the type of an event to be notified about
     * @param username
     *            the user to be notified
     * @param siteId
     *            the ID of the site owning the content object in question
     * @return the created subscription object
     */
    public Subscription subscribeAndAskForConfirmation(String objectKey,
            boolean includeChildren, String eventType, String username,
            int siteId) {
        return subscribeAndAskForConfirmation(objectKey, includeChildren,
                eventType, username, true, siteId, null);
    }

    /**
     * Suspends the specified subscription.
     * 
     * @param subscriptionId
     *            the id of the subscription to suspend
     * @return the updated subscription object
     */
    public Subscription suspendSubscription(int subscriptionId) {
        Subscription subscription = subscriptionManager.suspend(subscriptionId, true);
        if (null == subscription) {
            logger
                    .warn("Unable to suspend a subscription. The subscription is not found for ID: "
                            + subscriptionId);
        }
        return subscription;
    }

    /**
     * Resumes the specified subscription.
     * 
     * @param subscriptionId
     *            the id of the subscription to resume
     * @return the updated subscription object
     */
    public Subscription resumeSubscription(int subscriptionId) {
        Subscription subscription = subscriptionManager.suspend(subscriptionId, false);
        if (null == subscription) {
            logger
                    .warn("Unable to resume a subscription. The subscription is not found for ID: "
                            + subscriptionId);
        }
        return subscription;
    }

    /**
     * Deletes the specified subscription.
     * 
     * @param subscriptionId
     *            the id of the subscription to delete
     * @return returns {@link ConfirmationResult#OK} in case the subscription
     *         entry exists, {@link ConfirmationResult#SUBSCRIPTION_NOT_FOUND}
     *         otherwise
     */
    public ConfirmationResult unsubscribe(int subscriptionId) {
        Subscription subscription = subscriptionManager
                .getSubscription(subscriptionId);
        if (subscription != null) {
            subscriptionManager.delete(subscriptionId);
        }
        return subscription != null ? ConfirmationResult.OK
                : ConfirmationResult.SUBSCRIPTION_NOT_FOUND;
    }

    /**
     * Unsubscribes the current user to the events of the specified type.
     * 
     * @param objectKey
     *            the key of the content object that is the source of events
     * @param eventType
     *            the type of an event to be notified about
     * @param username
     *            the user to be notified
     * @param siteId
     *            the ID of the site owning the content object in question
     */
    public void unsubscribe(String objectKey, String eventType,
            String username, int siteId) {
        subscriptionManager.deleteAll(new Subscription(objectKey, true,
                eventType, username, siteId));
    }

    /**
     * Sends a request to the subscriber to confirm unsubscribe request.
     * 
     * @param subscriptionId
     *            the subscription ID
     * @param username
     *            the user to be notified
     * @param siteId
     *            the ID of the site owning the content object in question
     * @return the corresponding subscription object
     */
    public Subscription unsubscribeWithConfirmation(int subscriptionId,
            String username, int siteId) {

        Subscription subscription = subscriptionManager
                .getSubscription(subscriptionId);
        if (subscription != null && subscription.getSiteId() == siteId
                && subscription.getUsername().equals(username)) {
            generateConfirmationKey(subscription);
            sendConfirmationRequest(subscription, false);
        }

        return subscription;
    }

}
