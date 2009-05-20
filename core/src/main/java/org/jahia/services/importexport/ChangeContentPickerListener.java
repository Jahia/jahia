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

