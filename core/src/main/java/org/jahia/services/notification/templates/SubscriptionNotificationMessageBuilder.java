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
package org.jahia.services.notification.templates;

import groovy.lang.Binding;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jahia.services.notification.NotificationEvent;
import org.jahia.services.notification.Subscription;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Creates MIME notifications messages for sending to users based on Groovy
 * templates.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscriptionNotificationMessageBuilder extends
        SubscriptionConfirmationMessageBuilder {

    private List<NotificationEvent> events;

    /**
     * Initializes an instance of this class.
     * 
     * @param events
     *            the list of notification event objects
     * @param subscription
     *            the subscription object
     * @param subscriber
     *            subscriber information
     * @param subscriberEmail
     *            subscriber e-mail address
     */
    public SubscriptionNotificationMessageBuilder(List<NotificationEvent> events,
            Subscription subscription, JahiaUser subscriber,
            String subscriberEmail) {
        super(subscriber, subscriberEmail, subscription);
        this.events = events;
    }

    @Override
    protected String getTemplateHtmlPart() {
        String objectType = getObjectType();
        return lookupTemplate(objectType != null ? "notifications/events/"
                + subscription.getEventType() + "/" + objectType + "/body.html"
                : objectType, "notifications/events/"
                + subscription.getEventType() + "/body.html",
                "notifications/events/body.html");
    }

    @Override
    protected String getTemplateMailScript() {
        String objectType = getObjectType();
        return lookupTemplate(objectType != null ? "notifications/events/"
                + subscription.getEventType() + "/" + objectType
                + "/email.groovy" : null, "notifications/events/"
                + subscription.getEventType() + "/email.groovy",
                "notifications/events/email.groovy");
    }

    @Override
    protected String getTemplateTextPart() {
        String objectType = getObjectType();
        return lookupTemplate(objectType != null ? "notifications/events/"
                + subscription.getEventType() + "/" + objectType + "/body.txt"
                : null, "notifications/events/" + subscription.getEventType()
                + "/body.txt", "notifications/events/body.txt");
    }

    protected List<Link> getUpdatedPages() {
        List<Link> pages = new LinkedList<Link>();
        Set<Integer> pageIds = new HashSet<Integer>();
        for (NotificationEvent event : events) {
            if (event.getPageId() > 0 && !pageIds.contains(event.getPageId())) {
                pageIds.add(event.getPageId());
                String url = getPageUrl(event.getPageId());
                pages.add(new Link(event.getPageTitle() != null ? event
                        .getPageTitle() : "link", url, getServerUrl() + url));
            }
        }

        return pages;
    }

    @Override
    protected void populateBinding(Binding binding) {
        super.populateBinding(binding);
        binding.setVariable("events", events);
        List<Link> pages = getUpdatedPages();
        binding.setVariable("targetPages", pages);
        binding.setVariable("targetPage", !pages.isEmpty() ? pages.get(0) : null);
    }
}
