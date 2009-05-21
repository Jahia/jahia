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
