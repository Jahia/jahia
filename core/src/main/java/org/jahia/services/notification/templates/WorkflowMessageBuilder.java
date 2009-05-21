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
