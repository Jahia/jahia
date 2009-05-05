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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.notification.NotificationEvent;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.AbstractActivationJob;
import org.jahia.settings.SettingsBean;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

/**
 * Creates workflow operation report message for sending to users based on
 * Groovy templates.
 * 
 * @author Sergiy Shyrkov
 */
public class WorkflowMessageBuilder extends
        SubscriberNotificationMessageBuilder {

    private static Logger logger = Logger
            .getLogger(WorkflowMessageBuilder.class);

    private ProcessingContext ctx;

    private String jobComment;

    private String jobGroup;

    private String jobName;

    private String jobTitle;

    public WorkflowMessageBuilder(JahiaUser subscriber,
            List<NotificationEvent> events, ProcessingContext ctx) {
        super(subscriber, events);
        this.ctx = ctx;
        retrieveActivtionJobData();
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
                .getFirstName(ctx.getUser()), UserPreferencesHelper
                .getLastName(ctx.getUser()), UserPreferencesHelper
                .getFullName(ctx.getUser()), null, ctx.getUser()));
        binding.setVariable("jobTitle", jobTitle);
        binding.setVariable("jobComment", jobComment);
    }

    private void retrieveActivtionJobData() {
        jobName = (String) ctx.getAttribute(BackgroundJob.class.getName()
                + "_name");
        jobGroup = (String) ctx.getAttribute(BackgroundJob.class.getName()
                + "_group");
        try {
            JobDetail jobDetails = ServicesRegistry.getInstance()
                    .getSchedulerService().getJobDetail(jobName, jobGroup);
            JobDataMap dataMap = jobDetails.getJobDataMap();
            jobTitle = (String) dataMap.get(BackgroundJob.JOB_TITLE);
            jobComment = (String) dataMap
                    .get(AbstractActivationJob.COMMENTS_INPUT);
            if (StringUtils.isBlank(jobTitle)
                    && StringUtils.isNotBlank(jobComment)) {
                jobTitle = StringUtils.left(jobComment, 80);
                if (jobTitle.length() < jobComment.length()) {
                    jobTitle = jobTitle + "...";
                }
            }
            jobTitle = StringUtils.isNotBlank(jobTitle) ? jobTitle : null;
            jobComment = StringUtils.isNotBlank(jobComment) ? jobComment : null;
        } catch (Exception e) {
            logger.warn("Unable to retrieve activation job details for job '"
                    + jobName + "' and group '" + jobGroup + "'. Cause: "
                    + e.getMessage(), e);
        }
    }

}
