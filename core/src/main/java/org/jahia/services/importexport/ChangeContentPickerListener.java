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

 package org.jahia.services.importexport;

import org.jahia.data.events.JahiaEventListener;
import org.jahia.data.events.JahiaEvent;
import org.jahia.services.workflow.WorkflowEvent;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.StructuralRelationship;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.registries.ServicesRegistry;
import org.jahia.params.ProcessingContext;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.quartz.JobDetail;
import org.quartz.JobDataMap;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 26 avr. 2005
 * Time: 12:15:52
 * @version $Id$
 */
public class ChangeContentPickerListener extends JahiaEventListener {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ChangeContentPickerListener.class);
    private static final JahiaSitesService siteservice=ServicesRegistry.getInstance().getJahiaSitesService();


    public void objectChanged(WorkflowEvent we) {
        try {

            if (ChangeContentPickerJob.isInContentPickJob()) {
                return;
            }

            ProcessingContext jParams = Jahia.getThreadParamBean();

            if (jParams == null )  {
                return;
            }

            ContentObject source = (ContentObject) we.getObject();
            if (source == null) {
                return;
            }
            ContentObject parent = source.getParent(null);

            if (source.getPickerObjects(StructuralRelationship.CHANGE_PICKER_LINK).isEmpty() && (parent==null || parent.getPickerObjects(StructuralRelationship.CHANGE_PICKER_LINK).isEmpty())) {
                return;
            }

            Class jobClass = ChangeContentPickerJob.class;

            String skey = source.getObjectKey().toString();
            JobDetail jobDetail = BackgroundJob.createJahiaJob("Propagate changes from "+skey, jobClass, jParams);

            JobDataMap jobDataMap;
            jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put("event", "objectChanged");
            jobDataMap.put("source", skey);
            jobDataMap.put("sitesource", siteservice.getSite(source.getSiteID()).getSiteKey());
            jobDataMap.put("type", "propagate2");
            SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
            schedulerServ.scheduleJobNow(jobDetail);
        } catch (JahiaException e) {
            logger.error("Cannot copy linked content",e);
        }
    }

    public void contentActivation(ContentActivationEvent theEvent) {
        try {
            ContentObject object = (ContentObject) ContentObject.getInstance((ObjectKey)theEvent.getObject());
//            for (Iterator iterator = theEvent.getLanguageCodes().iterator(); iterator.hasNext();) {
//                String lang = (String) iterator.next();
                WorkflowEvent we = new WorkflowEvent(theEvent.getSource(), object, theEvent.getUser(), null, false);
                objectChanged(we);
//            }
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void beforeStagingContentIsDeleted(JahiaEvent je) {
        try {
            ProcessingContext jParams = Jahia.getThreadParamBean();

            if (jParams == null )  {
                return;
            }


            ContentObject source = (ContentObject) je.getObject();
            if (source == null) {
                return;
            }

            if (source.getPickerObjects("").isEmpty()) {
                return;
            }

            Class jobClass = ChangeContentPickerJob.class;

            String skey = source.getObjectKey().toString();
            JobDetail jobDetail = BackgroundJob.createJahiaJob("Propagate changes from "+skey, jobClass, jParams);

            JobDataMap jobDataMap;
            jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put("event", "beforeStagingContentIsDeleted");
            jobDataMap.put("source", skey);
            jobDataMap.put("sitesource", siteservice.getSite(source.getSiteID()).getSiteKey());
            jobDataMap.put("type", "propagate1");
            SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
            schedulerServ.scheduleJobNow(jobDetail);
        } catch (JahiaException e) {
            logger.error("Cannot copy linked content",e);
        }
    }
}

