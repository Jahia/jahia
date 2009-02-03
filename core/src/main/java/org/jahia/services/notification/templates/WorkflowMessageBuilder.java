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

import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.services.mail.MailHelper;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;

/**
 * Creates workflow operation report message for sending to users based on
 * Groovy templates.
 * 
 * @author Sergiy Shyrkov
 */
public class WorkflowMessageBuilder extends MessageBuilder {

    private ProcessingContext ctx;

    private String jobGroup;

    private String jobName;

    public WorkflowMessageBuilder(JahiaUser subscriber, int siteId,
            ProcessingContext ctx) {
        super(subscriber, siteId);
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
        return lookupTemplate("notifications/workflow/body.html");
    }

    @Override
    protected String getTemplateMailScript() {
        return lookupTemplate("notifications/workflow/email.groovy");
    }

    @Override
    protected String getTemplateTextPart() {
        return lookupTemplate("notifications/workflow/body.txt");
    }

    @Override
    protected void populateBinding(Binding binding) {
        super.populateBinding(binding);
        binding.setVariable("jobReportLink", getJobReportLink());
        binding.setVariable("from", MailHelper.getSenderEmailAddress(ctx));
        binding.setVariable("jobUser", new Subscriber(MailHelper
                .getFirstName(ctx.getUser()), MailHelper.getLastName(ctx
                .getUser()), MailHelper.getFullName(ctx.getUser()), null, ctx
                .getUser()));
    }
}
