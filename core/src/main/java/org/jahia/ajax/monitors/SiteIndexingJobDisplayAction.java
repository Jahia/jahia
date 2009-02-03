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

package org.jahia.ajax.monitors;

import org.apache.log4j.Logger;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.search.JahiaSiteIndexingJob;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.content.NodeOperationResult;
import org.jahia.content.TreeOperationResult;
import org.jahia.bin.Jahia;
import org.quartz.JobDetail;
import org.quartz.JobDataMap;

import java.util.*;

/**
 * SiteIndexingJobDisplayAction
 *
 * @author joepi
 * @version $Id: $
 */
public class SiteIndexingJobDisplayAction extends AbstractPDisplayAction {

    private static final transient Logger logger = Logger.getLogger(SiteIndexingJobDisplayAction.class);
    private static List jobTypesToIgnore = new ArrayList();

    // storage of users variables
    // todo: manage multiples sessions for the same user
    //private static Map userresponses = Collections.synchronizedMap(new HashMap(256));
    //private static Map userreload = Collections.synchronizedMap(new HashMap(256));

    /**
     * constructor
     */
    public SiteIndexingJobDisplayAction() {
        super();
        logger.debug("initialisation of SiteIndexingJobDisplay action");

        //register itself as scheduler listener
        /*
        try {
            service.registerAsListener(this);
            isRegistered = true;
        } catch (SchedulerException e) {
            logger.error("error on register as listener:" + e);
        }
        */

    }

    public List getActiveJobsDetails() throws JahiaException {
        return getJobsDetails(true);
    }

    public List getJobTypesToIgnore() {
        return jobTypesToIgnore;
    }

    protected boolean needToReload(long time) {
        return false;
    }

    public static List getJobsDetails(boolean activeOnly) throws JahiaException {
        JahiaSitesService siteServ = ServicesRegistry.getInstance().getJahiaSitesService();
        Iterator sites = siteServ.getSites();
        JahiaSite site = null;
        JobDetail jobDetail = null;
        JobDataMap data = null;
        List jobDetails = new ArrayList();
        String value = null;
        String key = null;
        String serverId = null;
        Properties settings = null;
        while ( sites.hasNext() ){
            site = (JahiaSite)sites.next();
            settings = site.getSettings();
            Iterator it = settings.keySet().iterator();
            int pos = 0;
            while ( it.hasNext() ){
                key = (String)it.next();
                pos = key.indexOf(JahiaSiteIndexingJob.SITE_INDEXATION_JOBNAME);
                if ( pos != -1 ){
                    serverId = key.substring(0,pos);
                    jobDetail = new JobDetail(settings.getProperty(key,""),JahiaSiteIndexingJob.JOB_GROUP_NAME,
                            JahiaSiteIndexingJob.class);
                    data = jobDetail.getJobDataMap();
//                    data.setMutable(true); commented for quartz 1.6.0 compatibility
                    value = settings.getProperty(serverId + BackgroundJob.JOB_STATUS,"");
                    if ( activeOnly && !(BackgroundJob.STATUS_POOLED.equals(value)
                            || BackgroundJob.STATUS_RUNNING.equals(value)
                            || BackgroundJob.STATUS_WAITING.equals(value)
                            || BackgroundJob.STATUS_INTERRUPTED.equals(value)) ){
                        continue;
                    }
                    TreeOperationResult result = new TreeOperationResult();
                    if ( BackgroundJob.STATUS_FAILED.equals(value) || BackgroundJob.STATUS_ABORTED.equals(value)
                            || BackgroundJob.STATUS_INTERRUPTED.equals(value) ){
                        String jobResult = settings.getProperty(serverId + BackgroundJob.RESULT,"");
                        String localeCode = Locale.getDefault().toString();
                        if ( Jahia.getThreadParamBean() != null ){
                            localeCode = Jahia.getThreadParamBean().getLocale().getDisplayName();
                        }
                        NodeOperationResult nodeOperationResult =
                                new NodeOperationResult(null,localeCode,jobResult);
                        result.appendError(nodeOperationResult);
                        data.put(BackgroundJob.RESULT,result);
                    } else if ( BackgroundJob.STATUS_SUCCESSFUL.equals(value) ){
                        data.put(BackgroundJob.RESULT,result);
                    }
                    data.put(BackgroundJob.JOB_SERVER, serverId.substring(0,serverId.length()-1));
                    data.put(BackgroundJob.JOB_STATUS,value);
                    value = settings.getProperty(serverId + BackgroundJob.JOB_BEGIN,"");
                    data.put(BackgroundJob.JOB_BEGIN,value);
                    value = settings.getProperty(serverId + BackgroundJob.JOB_CREATED,"");
                    data.put(BackgroundJob.JOB_CREATED,value);
                    value = settings.getProperty(serverId + BackgroundJob.JOB_DURATION,"");
                    data.put(BackgroundJob.JOB_DURATION,value);
                    value = settings.getProperty(serverId + BackgroundJob.JOB_END,"");
                    data.put(BackgroundJob.JOB_END,value);
                    data.put(BackgroundJob.JOB_SITEKEY,site.getSiteKey());
                    value = settings.getProperty(serverId + BackgroundJob.JOB_SCHEDULED,"");
                    data.put(BackgroundJob.JOB_SCHEDULED,value);
                    value = settings.getProperty(serverId + BackgroundJob.JOB_USERKEY,"");
                    data.put(BackgroundJob.JOB_USERKEY,value);
                    data.put(BackgroundJob.JOB_TYPE,JahiaSiteIndexingJob.SITE_INDEXATION_JOB_TYPE);
                    data.put(JahiaSiteIndexingJob.INTERRUPT_STATUS,
                            site.getSettings().getProperty(serverId + JahiaSiteIndexingJob.INTERRUPT_STATUS,""));
                    jobDetails.add(jobDetail);
                }
            }
        }
        return jobDetails;
    }

}
