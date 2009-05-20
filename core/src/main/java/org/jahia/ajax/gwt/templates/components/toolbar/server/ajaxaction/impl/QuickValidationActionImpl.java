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
package org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.impl;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.workflow.AbstractActivationJob;
import org.jahia.services.workflow.QuickActivationJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import java.util.Map;

/**
 * User: jahia
 * Date: 8 juil. 2008
 * Time: 14:50:27
 */
public class QuickValidationActionImpl extends AjaxAction {
    private static final transient Logger logger = Logger.getLogger(QuickValidationActionImpl.class);


    public GWTJahiaAjaxActionResult execute(JahiaData jahiaData, String action, Map gwtPropertiesMap) {

        final ProcessingContext processingContext = jahiaData.getProcessingContext();
        long startTime = System.currentTimeMillis();
        GWTJahiaAjaxActionResult result = new GWTJahiaAjaxActionResult();
        try {
            // create a job key
            String key = "ContentPage_" + processingContext.getPageID();

            //String comment
            String comment = null;
            GWTJahiaProperty gwtProperty = (GWTJahiaProperty) gwtPropertiesMap.get("comment");
            if (gwtProperty != null) {
                comment = gwtProperty.getValue();
            }

            //schedule job
            scheduleJob(processingContext, key, comment);


        } catch (Exception e) {
            logger.error("Unable to process the request !", e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("QuickValidationActionImpl.perform took: " + (System.currentTimeMillis() - startTime));
        }
        return result;
    }

    /**
     * Schedule job now
     *
     * @param processingContext
     * @param objectKey
     * @throws JahiaException
     */
    private void scheduleJob(ProcessingContext processingContext, String objectKey, String comment) throws JahiaException {
        // create a job
        final JobDetail jobDetail = BackgroundJob.createJahiaJob("QuickValidation", QuickActivationJob.class, processingContext);
        final JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put(BackgroundJob.JOB_DESTINATION_SITE, processingContext.getSite().getSiteKey());
        jobDataMap.put(BackgroundJob.JOB_TYPE, AbstractActivationJob.WORKFLOW_TYPE);
        jobDataMap.put(QuickActivationJob.CONTENT_OBJECT_KEY, objectKey);
        if (comment != null) {
            jobDataMap.put(AbstractActivationJob.COMMENTS_INPUT, comment);
        }

        // Schedule Job
        ServicesRegistry.getInstance().getSchedulerService().scheduleJobNow(jobDetail);
    }
}
