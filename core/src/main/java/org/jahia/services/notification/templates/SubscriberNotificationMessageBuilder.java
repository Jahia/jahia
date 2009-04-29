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
import org.jahia.services.usermanager.JahiaUser;

/**
 * Creates messages for sending to users based on Groovy templates.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscriberNotificationMessageBuilder extends MessageBuilder {

    private List<NotificationEvent> events;

    public SubscriberNotificationMessageBuilder(JahiaUser subscriber,
            List<NotificationEvent> events) {
        super(subscriber, events.get(0).getSiteId());
        this.events = events;
    }

    protected List<NotificationEvent> getEvents() {
        return events;
    }

    @Override
    protected String getTemplateHtmlPart() {
        return lookupTemplate("notifications/events/"
                + events.get(0).getEventType() + "/body.html",
                "notifications/events/body.html");
    }

    @Override
    protected String getTemplateMailScript() {
        return lookupTemplate("notifications/events/"
                + events.get(0).getEventType() + "/email.groovy",
                "notifications/events/email.groovy");
    }

    @Override
    protected String getTemplateTextPart() {
        return lookupTemplate("notifications/events/"
                + events.get(0).getEventType() + "/body.txt",
                "notifications/events/body.txt");
    }

    @Override
    protected Link getUnsubscribeLink() {
        return null;
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
        binding.setVariable("eventType", events.get(0).getEventType());
        List<Link> pages = getUpdatedPages();
        binding.setVariable("targetPages", pages);
        binding.setVariable("targetPage", !pages.isEmpty() ? pages.get(0)
                : null);
    }

}
