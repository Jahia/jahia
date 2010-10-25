/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.scheduler;

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.IdentifierGeneratorFactory;
import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;
import org.quartz.*;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * Date: 25 oct. 2005 - 16:34:07
 *
 * @author toto
 * @version $Id$
 */
public abstract class BackgroundJob implements StatefulJob {
    private final static Logger logger = Logger.getLogger(BackgroundJob.class);

    private static IdentifierGenerator idGen = IdentifierGeneratorFactory.newInstance().uuidVersionFourGenerator();

    //jobdetails Constants
    public static final String JOB_CREATED = "created";
    public static final String JOB_SCHEDULED = "scheduled";
    public static final String JOB_BEGIN = "begin";
    public static final String JOB_END = "end";
    public static final String JOB_SERVER = "server";
    public static final String JOB_STATUS = "status";
    public static final String JOB_DURATION = "duration";
    public static final String JOB_CURRENT_LOCALE = "currentLocale";
    public static final String JOB_SCHEME = "scheme";
    public static final String JOB_SERVERNAME = "servername";
    public static final String JOB_PARAMETER_MAP = "parameterMap";
    public static final String JOB_SERVERPORT = "serverport";
    public static final String JOB_OPMODE = "opmode";
    public static final String JOB_SITEKEY = "sitekey";
    public static final String JOB_USERKEY = "userkey";
    public static final String JOB_PID = "pid";
    public static final String JOB_DESTINATION_SITE = "sitedest";
    public static final String JOB_LOCKS = "locks";
    public static final String JOB_TITLE = "title";
    public static final String JOB_MESSAGE = "message";

    public static final String ACTIONS = "actions";
    public static final String RESULT = "result";

    public static final String STATUS_SUCCESSFUL = "successful";
    public static final String STATUS_RUNNING = "executing";
    public static final String STATUS_WAITING = "waiting";
    public static final String STATUS_POOLED = "pooled";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_ABORTED = "aborted";
    public static final String STATUS_INTERRUPTED = "interrupted";

    public static JobDetail createJahiaJob(String desc, Class<? extends BackgroundJob> jobClass) {
        // jobdetail is non-volatile,durable,non-recoverable
        JobDetail jobDetail = new JobDetail(getGroupName(jobClass) + "-" + idGen.nextIdentifier(),
                getGroupName(jobClass),
                jobClass,
                false,
                true,
                false);
        jobDetail.setDescription(desc);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(JOB_CREATED, new Date()); //creation
        final JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        jobDataMap.put(JOB_USERKEY, sessionFactory.getCurrentUser().getUserKey());
        jobDataMap.put(JOB_CURRENT_LOCALE, sessionFactory.getCurrentLocale() != null ? sessionFactory
                .getCurrentLocale().toString() : null);

        jobDetail.setJobDataMap(jobDataMap);
        return jobDetail;
    }

    public static String getGroupName(Class<? extends BackgroundJob> c) {
        String name = c.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public static int getMaxExecutionTime() {
        return 3600;
    }

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        // First some sanity checks, mostly because we could be executing jobs before Jahia has finished initializing
        if (ServicesRegistry.getInstance() == null) {
            return;
        }
        if (ServicesRegistry.getInstance().getJahiaUserManagerService() == null) {
            return;
        }

        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap data = jobDetail.getJobDataMap();
        long now = System.currentTimeMillis();
        logger.info("Background job " + jobDetail.getName() + " started @ " + new Date(now));
        String status = STATUS_FAILED;
        final JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        try {
            JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey((String) data.get(JOB_USERKEY));
            sessionFactory.setCurrentUser(user);
            sessionFactory.setCurrentLocale(LanguageCodeConverters.languageCodeToLocale((String) data.get(JOB_CURRENT_LOCALE)));

            executeJahiaJob(jobExecutionContext);

            status = data.getString(BackgroundJob.JOB_STATUS);
            if (!(BackgroundJob.STATUS_ABORTED.equals(status) ||
                    BackgroundJob.STATUS_FAILED.equals(status) ||
                    BackgroundJob.STATUS_INTERRUPTED.equals(status))) {
                status = STATUS_SUCCESSFUL;
            }
        } catch (Exception e) {
            logger.error("Cannot execute job", e);
            data.put(JOB_MESSAGE, e.toString());
            throw new JobExecutionException(e);
        } finally {

            ServicesRegistry.getInstance().getCacheService().syncClusterNow();

            data.putAsString(JOB_END, System.currentTimeMillis());
            // job begin is set by trigger listener in SchedulerService
            int duration = (int) ((Long.parseLong((String) data.get(JOB_END)) - Long.parseLong((String) data.get(JOB_BEGIN))) / 1000);
            data.putAsString(JOB_DURATION, duration);//duration

            logger.info("Background job (of type " + jobDetail.getGroup() + ") ended with status " + status + " executed in " + duration + "s");

            Date nextFireTime = jobExecutionContext.getNextFireTime();
            try {
                // Look for other triggers for next fire time
                Trigger[] trigs = jobExecutionContext.getScheduler().getTriggersOfJob(jobDetail.getName(), jobDetail.getGroup());
                for (int i = 0; i < trigs.length; i++) {
                    Trigger trig = trigs[i];
                    Date thisTriggerNextFireTime = trig.getNextFireTime();
                    if (thisTriggerNextFireTime != null && (nextFireTime == null || thisTriggerNextFireTime.before(nextFireTime))) {
                        nextFireTime = thisTriggerNextFireTime;
                    }
                }
            } catch (SchedulerException e) {
                logger.error("Cannot get triggers for job", e);
            }

            if (status == STATUS_FAILED) {
                try {
                    boolean ramScheduler = this instanceof RamJob;
                    ServicesRegistry.getInstance().getSchedulerService().unscheduleJob(jobDetail, ramScheduler);
                } catch (JahiaException e) {
                    logger.error("Cannot unschedule job", e);
                }
            } else {
                if (nextFireTime != null) {
                    status = STATUS_POOLED;
                    data.putAsString(JOB_SCHEDULED, nextFireTime.getTime());
                }
            }
            data.put(JOB_STATUS, status);
            this.postExecution(jobExecutionContext);
            sessionFactory.setCurrentUser(null);
            sessionFactory.setCurrentLocale(null);
            sessionFactory.closeAllSessions();
        }
    }

    public abstract void executeJahiaJob(JobExecutionContext jobExecutionContext)
            throws Exception;

    /**
     * Sub class can perform specific post execution task hehe
     *
     * @param jobExecutionContext
     */
    protected void postExecution(JobExecutionContext jobExecutionContext) {
        // by default do nothing
    }

}
