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
package org.jahia.services.content.textextraction;

import org.apache.log4j.Logger;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.rules.ExtractionService;
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
                    if (n.hasProperty(Constants.JCR_MIMETYPE) && ExtractionService.getInstance().getContentTypes().contains(n.getProperty(Constants.JCR_MIMETYPE).getString())) {
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
                        Node file = p.getParent().getParent();
                        SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
                        JobDataMap jobDataMap;
                        jobDataMap = jobDetail.getJobDataMap();
                        jobDataMap.put(TextExtractorJob.PROVIDER, provider.getMountPoint());
                        jobDataMap.put(TextExtractorJob.PATH, file.getPath());
                        jobDataMap.put(TextExtractorJob.NAME, file.getName());
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
