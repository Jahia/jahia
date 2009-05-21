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
