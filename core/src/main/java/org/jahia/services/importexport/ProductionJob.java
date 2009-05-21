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
 package org.jahia.services.importexport;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.apache.webdav.lib.WebdavResource;
import org.jahia.content.NodeOperationResult;
import org.jahia.content.TreeOperationResult;
import org.jahia.engines.EngineMessage;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.lock.LockKey;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.quartz.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 30 août 2005
 * Time: 10:46:21
 * To change this template use File | Settings | File Templates.
 */
public class ProductionJob extends BackgroundJob {
    public static final String JOB_NAME_PREFIX = "ProductionJob_";
    public static final String TRIGGER_NAME_PREFIX = "ProductionJobTrigger_";
    public static final String SITE_ID = "Site_Id";
    public static final String TARGET = "Target_Name";
    public static final String USERNAME = "Username";
    public static final String PASSWORD = "Password";
    public static final String PROFILE = "Profile";
    public static final String SITE_NAME = "Site_Name";
    public static final String ALIAS = "Alias";
    public static final String METADATA = "metadata";
    public static final String WORKFLOW = "workflow";
    public static final String ACL = "acl";
    public static final String AUTO_PUBLISH = "publish";
    
    private static final transient Logger logger = Logger.getLogger(ProductionJob.class);
    public static final String PRODUCTION_TYPE = "production";

    public void executeJahiaJob(JobExecutionContext jobExecutionContext, ProcessingContext jParams) throws JobExecutionException {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        int siteID = jobDataMap.getInt(SITE_ID);
        if (siteID > 0) {
            String targetName = jobDataMap.getString(TARGET);
            Date exportTime = jobExecutionContext.getScheduledFireTime();
            String username = jobDataMap.getString(USERNAME);
            String password = jobDataMap.getString(PASSWORD);
            String sitename = jobDataMap.getString(SITE_NAME);

            TreeOperationResult result = new TreeOperationResult();
            boolean metadata = !"false".equals(jobDataMap.getString(METADATA));
            boolean workflow = !"false".equals(jobDataMap.getString(WORKFLOW));
            boolean acl = !"false".equals(jobDataMap.getString(ACL));
            boolean publish = "true".equals(jobDataMap.getString(AUTO_PUBLISH));
            try {
                ServicesRegistry instance = ServicesRegistry.getInstance();
                JahiaSitesService jahiaSitesService = instance.getJahiaSitesService();
                JahiaSite site = jahiaSitesService.getSite(siteID);
                boolean execute = true;
                if (site == null) {
                    execute = false;
                } else {
                    String targetNames = site.getSettings().getProperty(ImportExportBaseService.PRODUCTION_TARGET_LIST_PROPERTY, "");
                    execute = targetNames.indexOf(jobDetail.getName().split("_")[2]) > -1;
                }
                if (execute) {
                    JahiaUser member = instance.getJahiaSiteUserManagerService().getMember(site.getID(), jobDataMap.getString(PROFILE));
                    if (logger.isDebugEnabled())
                        logger.debug("Calling execute of ProductionJob for site " + siteID + " exporting to " + targetName);

                    LockKey lock = LockKey.composeLockKey(LockKey.LIVEEXPORT_PAGE_TYPE, site.getHomePageID());
                    try {
                        if (ServicesRegistry.getInstance().getLockService().acquire(lock, member, jobDetail.getName(), BackgroundJob.getMaxExecutionTime()))
                        {
                            WebdavResource webdavSession = instance.getImportExportService().exportToSite(site, targetName, exportTime, username, password, member, sitename, metadata, workflow, acl, publish);
                            if (webdavSession.getStatusCode() == HttpStatus.SC_OK || webdavSession.getStatusCode() == HttpStatus.SC_CREATED)
                            {
                                logger.info("Production job execute well for site " + site.getSiteKey() + " to target " + targetName);
                            } else {
                                failed(result, null, targetName, sitename, "org.jahia.engines.importexport.export.productionFailed.label", jobDataMap);
                            }
                            webdavSession.close();
                        } else {
                            failed(result, null, targetName, sitename, "org.jahia.engines.importexport.export.cannotAcquireLock.label", jobDataMap);
                        }
                    } catch (IOException e) {
                        failed(result, e, targetName, sitename, "org.jahia.engines.importexport.export.productionFailed.label", jobDataMap);
                    } catch (SAXException e) {
                        failed(result, e, targetName, sitename, "org.jahia.engines.importexport.export.productionFailed.label", jobDataMap);
                    } finally{
                        ServicesRegistry.getInstance().getLockService().release(lock, member, jobDetail.getName());
                    }
                } else {
                    if (logger.isDebugEnabled())
                        logger.debug("Delete ProductionJob for site " + site.getSiteKey() + " " + jobDetail.getFullName());
                    //remove the job
                    try {
                        jobExecutionContext.getScheduler().deleteJob(jobDetail.getName(), jobDetail.getGroup());
                    } catch (SchedulerException e) {
                        failed(result, e, targetName, sitename, "org.jahia.engines.importexport.export.productionFailed.label", jobDataMap);
                    }
                }
            } catch (JahiaException e) {
                failed(result, e, targetName, sitename, "org.jahia.engines.importexport.export.productionFailed.label", jobDataMap);
            }
            jobDataMap.put(RESULT, result);
        }
    }

    private void failed(TreeOperationResult result, Throwable e, String targetName, String sitename, String msgKey, JobDataMap jobDataMap) throws JobExecutionException {
        result.setStatus(TreeOperationResult.FAILED_OPERATION_STATUS);
        result.appendError(new NodeOperationResult(null,null,null,new EngineMessage(msgKey, new Object[0])));
        jobDataMap.put(RESULT, result);
        if(e!=null)
            throw new JobExecutionException(e);
        else
            throw new JobExecutionException("Production job execute fail for site " + sitename + " to target " + targetName);
//        if (e != null) {
//            logger.error("Production job execute fail for site " + sitename + " to target " + targetName,e);
//        } else {
//            logger.error("Production job execute fail for site " + sitename + " to target " + targetName);
//        }
    }
}
