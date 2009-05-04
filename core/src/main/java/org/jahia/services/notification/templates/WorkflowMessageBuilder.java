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

import java.util.List;

import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.services.notification.NotificationEvent;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;

/**
 * Creates workflow operation report message for sending to users based on
 * Groovy templates.
 * 
 * @author Sergiy Shyrkov
 */
public class WorkflowMessageBuilder extends
        SubscriberNotificationMessageBuilder {

    private ProcessingContext ctx;

    private String jobGroup;

    private String jobName;

    public WorkflowMessageBuilder(JahiaUser subscriber,
            List<NotificationEvent> events, ProcessingContext ctx) {
        super(subscriber, events);
        this.ctx = ctx;
        jobName = (String) ctx.getAttribute(BackgroundJob.class.getName()
                + "_name");
        jobGroup = (String) ctx.getAttribute(BackgroundJob.class.getName()
                + "_group");
    }

    protected Link getJobReportLink() {
        String url = Jahia.getContextPath()
                + SettingsBean.getInstance().getJspContext()
                + "processing/jobreport.jsp?name=" + jobName + "&groupName="
                + jobGroup;
        return new Link("Job report", url, getServerUrl() + url);
    }

    @Override
    protected String getTemplateHtmlPart() {
        return lookupTemplate("notifications/events/"
                + getEvents().get(0).getEventType() + "/body.html",
                "notifications/events/workflow/body.html",
                "notifications/events/body.html");
    }

    @Override
    protected String getTemplateMailScript() {
        return lookupTemplate("notifications/events/"
                + getEvents().get(0).getEventType() + "/email.groovy",
                "notifications/events/workflow/email.groovy",
                "notifications/events/email.groovy");
    }

    @Override
    protected String getTemplateTextPart() {
        return lookupTemplate("notifications/events/"
                + getEvents().get(0).getEventType() + "/body.txt",
                "notifications/events/workflow/body.txt",
                "notifications/events/body.txt");
    }

    @Override
    protected void populateBinding(Binding binding) {
        super.populateBinding(binding);
        binding.setVariable("jobReportLink", getJobReportLink());
        binding.setVariable("from", getSenderEmailAddress(ctx));
        binding.setVariable("jobUser", new Subscriber(UserPreferencesHelper
                .getFirstName(ctx.getUser()), UserPreferencesHelper.getLastName(ctx
                .getUser()), UserPreferencesHelper.getFullName(ctx.getUser()), null, ctx
                .getUser()));
    }

}
