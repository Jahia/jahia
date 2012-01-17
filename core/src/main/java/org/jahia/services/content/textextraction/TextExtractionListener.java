/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.textextraction;

import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.services.content.rules.ExtractionService;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.io.IOException;
import java.util.Calendar;

/**
 * JCR event listener to trigger text extracting for binary content.
 *
 * @author Thomas Draier
 * @author Sergiy Shyrkov
 */
public class TextExtractionListener extends DefaultEventListener {

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(TextExtractionListener.class);

    private ExtractionService extractionService;

    private SchedulerService schedulerService;

    protected void doHandle(Node node, Event event, JCRSessionWrapper s) throws AccessDeniedException,
            ItemNotFoundException, RepositoryException, SchedulerException {
        String mimeType = null;
        try {
            mimeType = node.getProperty(Constants.JCR_MIMETYPE).getString();
        } catch (PathNotFoundException e) {
            // ignore
        }
        // no mime type detected -> skip it
        if (mimeType == null) {
            return;
        }

        Calendar extractionDate = null;
        if (node.hasProperty(Constants.EXTRACTION_DATE)) {
            try {
                extractionDate = node.getProperty(Constants.EXTRACTION_DATE).getDate();
            } catch (PathNotFoundException e) {
                // ignore
            }
        }

        // extraction date property found?
        if (extractionDate != null) {
            // check if it is greater than last modified date
            Calendar lastModified = node.getProperty(Constants.JCR_LASTMODIFIED).getDate();
            if (!extractionDate.before(lastModified)) {
                // no updates were done -> do not need to extract content
                return;
            }
        }

        boolean canHanlde = false;
        try {
            canHanlde = extractionService.canHandle((JCRNodeWrapper) node);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
        if (canHanlde) {
            // we got so far to the background task
            scheduleBackgroundExtraction((JCRNodeWrapper) node.getParent(), event.getUserID());
        }
    }

    public int getEventTypes() {
        // if the extraction service is not active, do not enable this listener
        return extractionService.isEnabled() ? Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED
                : 0;
    }

    public String[] getNodeTypes() {
        return new String[]{Constants.JAHIANT_RESOURCE};
    }

    public void onEvent(final EventIterator eventIterator) {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper s) throws RepositoryException {
                    try {
                        while (eventIterator.hasNext()) {
                            Event event = eventIterator.nextEvent();
                            if (isExternal(event)) {
                                continue;
                            }

                            // skip /jcr:system path
                            if (event.getPath().startsWith("/jcr:system")) {
                                continue;
                            }

                            // is it a binary property?
                            Property p = (Property) s.getItem(event.getPath());
                            if (p.getType() != PropertyType.BINARY) {
                                continue;
                            }

                            doHandle(p.getParent(), event, s);
                        }
                    } catch (ConstraintViolationException e) {
                        logger.debug(e.getMessage(), e);
                    } catch (PathNotFoundException e) {
                        logger.debug(e.getMessage(), e);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected void scheduleBackgroundExtraction(JCRNodeWrapper fileNode, String user) throws SchedulerException {
        JobDetail jobDetail = BackgroundJob.createJahiaJob("Text extraction for " + fileNode.getName(),
                TextExtractorJob.class);
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put(TextExtractorJob.JOB_PROVIDER, fileNode.getProvider().getMountPoint());
        jobDataMap.put(TextExtractorJob.JOB_PATH, fileNode.getPath());

        if (logger.isDebugEnabled()) {
            logger.debug("Scheduling text extraction background job for file " + fileNode.getPath());
        }

        schedulerService.scheduleJobNow(jobDetail);
    }

    /**
     * @param extractionService the extractionService to set
     */
    public void setExtractionService(ExtractionService extractionService) {
        this.extractionService = extractionService;
    }

    /**
     * @param schedulerService the schedulerService to set
     */
    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }
}
