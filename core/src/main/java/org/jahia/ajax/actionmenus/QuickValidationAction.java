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
package org.jahia.ajax.actionmenus;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.ajax.AjaxAction;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.workflow.AbstractActivationJob;
import org.jahia.services.workflow.QuickActivationJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

/**
 * Ajax-based action handler for retrieving the workflow state of the content
 * object.
 * 
 * @author Sergiy Shyrkov
 */
public class QuickValidationAction extends AjaxAction {

    private static final transient Logger logger = Logger
            .getLogger(QuickValidationAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        long startTime = System.currentTimeMillis();

        setNoCacheHeaders(response);

        try {
            String pid = getParameter(request, "pid");
            if (StringUtils.isEmpty(pid)) {
                throw new JahiaBadRequestException(
                        "Required parameter 'pid' is missing.");
            }
            String key = getParameter(request, "key");
            if (StringUtils.isEmpty(key)) {
                throw new JahiaBadRequestException(
                        "Required parameter 'key' is missing.");
            }
            ProcessingContext ctx = retrieveProcessingContext(request,
                    response, "/op/edit/pid/" + pid, false);

            scheduleJob(ctx, key);

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("QuickValidationAction.execute took: "
                    + (System.currentTimeMillis() - startTime));
        }
        return null;
    }

    private void scheduleJob(ProcessingContext ctx, String objectKey)
            throws JahiaException {
        final JobDetail jobDetail = BackgroundJob.createJahiaJob(
                "QuickValidation", QuickActivationJob.class, ctx);
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put(BackgroundJob.JOB_DESTINATION_SITE, ctx.getSite()
                .getSiteKey());
        jobDataMap.put(BackgroundJob.JOB_TYPE, AbstractActivationJob.WORKFLOW_TYPE);

        jobDataMap.put(QuickActivationJob.CONTENT_OBJECT_KEY, objectKey);
        jobDataMap.put(AbstractActivationJob.COMMENTS_INPUT, ctx
                .getParameter(AbstractActivationJob.COMMENTS_INPUT));

        ServicesRegistry.getInstance().getSchedulerService()
                .scheduleJobNow(jobDetail);
    }

}
