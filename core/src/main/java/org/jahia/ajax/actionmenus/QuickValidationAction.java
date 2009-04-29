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
