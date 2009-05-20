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
package org.jahia.services.content.textextraction;

import org.apache.log4j.Logger;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import javax.jcr.*;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.Locale;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 29 janv. 2008
 * Time: 13:43:06
 * To change this template use File | Settings | File Templates.
 */
public class TextExtractionListener extends DefaultEventListener {
    
    private static final transient Logger logger = Logger.getLogger(TextExtractionListener.class);

    public TextExtractionListener() {
    }

    public int getEventTypes() {
        return Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED;
    }

    public String getPath() {
        return "/";
    }

    public String[] getNodeTypes() {
        return new String[] { Constants.JAHIANT_RESOURCE };
    }

    public void onEvent(EventIterator eventIterator) {

        try {
            Session s = provider.getSystemSession();
            try {
                while (eventIterator.hasNext()) {
                    Event event = eventIterator.nextEvent();
                    if (isExternal(event)) {
                        continue;
                    }

                    Property p = (Property) s.getItem(event.getPath());
                    if (p.getType() != PropertyType.BINARY) {
                        continue;
                    }
                    Node n = p.getParent();
                    if (n.hasProperty(Constants.JCR_MIMETYPE) && TextExtractorJob.getContentTypes().contains(n.getProperty(Constants.JCR_MIMETYPE).getString())) {
                        if (n.hasProperty(Constants.EXTRACTION_DATE)) {
                            Calendar lastModified = n.getProperty(Constants.JCR_LASTMODIFIED).getDate();
                            Calendar extractionDate = n.getProperty(Constants.EXTRACTION_DATE).getDate();
                            if (extractionDate.after(lastModified) || extractionDate.equals(lastModified)) {
                                continue;
                            }
                        }
                        JahiaUser member = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(event.getUserID());

                        ProcessingContext jParams = Jahia.getThreadParamBean();
                        if (jParams == null) {
                            jParams = new ProcessingContext(org.jahia.settings.SettingsBean.getInstance(), System.currentTimeMillis(), null, member, null);
                            jParams.setCurrentLocale(Locale.getDefault());
                        }

                        JobDetail jobDetail = BackgroundJob.createJahiaJob("Text extraction for "+p.getParent().getName(), TextExtractorJob.class, jParams);

                        SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
                        JobDataMap jobDataMap;
                        jobDataMap = jobDetail.getJobDataMap();
                        jobDataMap.put(TextExtractorJob.PROVIDER, provider.getMountPoint());
                        jobDataMap.put(TextExtractorJob.PATH, event.getPath());
                        jobDataMap.put(TextExtractorJob.NAME, p.getParent().getParent().getName());
                        jobDataMap.put(BackgroundJob.JOB_TYPE, TextExtractorJob.EXTRACTION_TYPE);
                        schedulerServ.scheduleJobNow(jobDetail);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                s.logout();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
