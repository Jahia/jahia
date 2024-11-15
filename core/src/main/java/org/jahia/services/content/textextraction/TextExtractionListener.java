/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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

    protected void doHandle(Node node, Event event, JCRSessionWrapper s, boolean immediateExtraction) throws AccessDeniedException,
            ItemNotFoundException, IOException, RepositoryException, SchedulerException {
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
        if (node.hasProperty(Constants.EXTRACTION_DATE) && node.hasProperty(Constants.EXTRACTED_TEXT)) {
            try {
                extractionDate = node.getProperty(Constants.EXTRACTION_DATE).getDate();
            } catch (PathNotFoundException e) {
                // ignore
            }
        }

        // extraction date property found?
        if (extractionDate != null) {
            // check if last modified date is at least more than 1 second after extraction date
            Calendar lastModified = node.getProperty(Constants.JCR_LASTMODIFIED).getDate();
            if (!extractionDate.before(lastModified) || lastModified.getTimeInMillis() - extractionDate.getTimeInMillis() < 1000L) {
                // no updates were done -> do not need to extract content
                return;
            }
        }

        boolean canHandle = false;
        try {
            canHandle = extractionService.canHandle((JCRNodeWrapper) node);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
        if (canHandle) {
            JCRNodeWrapper fileNode = (JCRNodeWrapper)node.getParent();
            if (immediateExtraction) {
                ExtractionService.getInstance().extractText(fileNode.getProvider(), fileNode.getPath(), null, getWorkspace());
            } else {
                scheduleBackgroundExtraction(fileNode, event.getUserID());
            }
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
        final boolean isImport = eventIterator instanceof JCREventIterator
                && ((JCREventIterator) eventIterator).getOperationType() == JCRObservationManager.IMPORT;

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, getWorkspace(), new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper s) throws RepositoryException {
                    try {
                        while (eventIterator.hasNext()) {
                            Event event = eventIterator.nextEvent();
                            String eventPath = event.getPath();

                            // skip /jcr:system path
                            if (eventPath.startsWith("/jcr:system") || eventPath.endsWith("/j:extractedText")) {
                                continue;
                            }

                            // is it a binary property?
                            Property p = (Property) s.getItem(eventPath);
                            if (p.getDefinition() == null || p.getType() != PropertyType.BINARY) {
                                continue;
                            }

                            doHandle(p.getParent(), event, s, isImport);
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
        jobDataMap.put(TextExtractorJob.JOB_WORKSPACE, getWorkspace());

        if (logger.isDebugEnabled()) {
            logger.debug("Scheduling text extraction background job for file " + fileNode.getPath());
        }

        schedulerService.scheduleJobAtEndOfRequest(jobDetail);
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
