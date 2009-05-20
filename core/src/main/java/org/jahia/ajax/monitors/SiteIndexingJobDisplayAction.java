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
