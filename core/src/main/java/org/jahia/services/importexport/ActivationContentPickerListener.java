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

import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.StructuralRelationship;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.version.EntryLoadRequest;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 26 avr. 2005
 * Time: 12:15:52
 * @version $Id$
 */
public class ActivationContentPickerListener extends JahiaEventListener {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ActivationContentPickerListener.class);
    private JahiaSitesService siteservice;

    public JahiaSitesService getSiteservice() {
        return siteservice;
    }

    public void setSiteservice(JahiaSitesService siteservice) {
        this.siteservice = siteservice;
    }

    public void aggregatedContentActivation(JahiaEvent theEvent) {

        Map<ObjectKey, Set<String>> seen = new HashMap<ObjectKey, Set<String>>();
        logger.debug("Activation content picker is looking for pickers ...");
        List<ContentActivationEvent> all = (List<ContentActivationEvent>) theEvent.getObject();
        try {
            for (ContentActivationEvent event : all) {
                ProcessingContext jParams = event.getProcessingContext();
                Set<String> langs = event.getLanguageCodes();
                ObjectKey objectKey = (ObjectKey) event.getObject();
                ContentObject contentObject = ContentObject.getContentObjectInstance(objectKey);
                if (contentObject != null ) {
                    Set<ContentObject> pickers = contentObject.getPickerObjects(StructuralRelationship.ACTIVATION_PICKER_LINK);
                    if (pickers.isEmpty()) {
                        ContentObject parent = contentObject.getParent(null, EntryLoadRequest.CURRENT, null);
                        if (parent != null ) {
                            pickers = parent.getPickerObjects(StructuralRelationship.ACTIVATION_PICKER_LINK);
                        }
                    }
                    for (ContentObject picker : pickers) {
                        do {
                            if (!seen.containsKey(picker.getObjectKey()) || !seen.get(picker.getObjectKey()).containsAll(langs)) {
                                Set<String> remainingLangs = new HashSet<String>(langs);

                                if (!seen.containsKey(picker.getObjectKey())) {
                                    seen.put(picker.getObjectKey(), langs);
                                } else {
                                    Set<String> s = seen.get(picker.getObjectKey());
                                    remainingLangs.removeAll(s);
                                    s.addAll(langs);
                                }

                                ContentObject parentPicker = picker.getParent(null, EntryLoadRequest.STAGED, null);
                                if (parentPicker != null && parentPicker.getPickedObject(StructuralRelationship.ACTIVATION_PICKER_LINK) == null) {
                                    JahiaSite site = siteservice.getSite(picker.getSiteID());
                                    List<Locale> settings = site.getLanguageSettingsAsLocales(false);
                                    List<String> settingsAsCodes = new ArrayList<String>();
                                    for (Locale locale : settings) {
                                        settingsAsCodes.add(locale.toString());
                                    }
                                    remainingLangs.retainAll(settingsAsCodes);
                                    if (!remainingLangs.isEmpty()) {
                                        logger.debug("Picker found = "+picker.getObjectKey());
                                        logger.debug(" languages to pick : "+remainingLangs);
                                        String pickerKey = picker.getObjectKey().toString();

                                        JobDetail jobDetail = BackgroundJob.createJahiaJob("Propagate changes to "+pickerKey, ActivationContentPickerJob.class, jParams);

                                        JobDataMap jobDataMap;
                                        jobDataMap = jobDetail.getJobDataMap();
                                        jobDataMap.put(ActivationContentPickerJob.LANGS, remainingLangs);
                                        jobDataMap.put(ActivationContentPickerJob.PICKER, picker.getObjectKey().toString());
                                        jobDataMap.put(ActivationContentPickerJob.JOB_DESTINATION_SITE,site.getSiteKey());
                                        jobDataMap.put(ActivationContentPickerJob.PICKERSITE, picker.getSiteID());
                                        jobDataMap.put(BackgroundJob.JOB_TYPE, ActivationContentPickerJob.PICKED_TYPE);
                                        SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
                                        schedulerServ.scheduleJobNow(jobDetail);
                                    }
                                    break;
                                }
                                picker = parentPicker;
                            }
                        } while (picker != null && (!seen.containsKey(picker.getObjectKey()) || !seen.get(picker.getObjectKey()).containsAll(langs)));
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.debug("Activation content picker / done");
    }
}

/**
* $Log$
* Revision 1.3  2006/01/09 11:11:29  tdraier
* content pick as a job
*
* Revision 1.2  2006/01/06 18:21:43  tdraier
* multi-sites and user fix
*
* Revision 1.1  2006/01/06 13:11:08  tdraier
* content picker and workflow updates
*
* Revision 1.22  2005/12/21 16:15:04  dpillot
* added operation type in jobdata
*
*/