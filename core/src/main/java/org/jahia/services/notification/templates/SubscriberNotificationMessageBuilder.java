/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
