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
package org.jahia.services.scheduler;

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.IdentifierGeneratorFactory;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.bin.filters.jcr.JcrSessionFilter;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;
import org.quartz.*;

import java.util.Date;

/**
 * Base class for Jahia background tasks.
 *
 * @author toto
 */
public abstract class BackgroundJob implements StatefulJob {
    private final static Logger logger = LoggerFactory.getLogger(BackgroundJob.class);

    public static IdentifierGenerator idGen = IdentifierGeneratorFactory.newInstance().uuidVersionFourGenerator();

    //job details constants
    public static final String JOB_CREATED = "created";
    public static final String JOB_BEGIN = "begin";
    public static final String JOB_END = "end";
    public static final String JOB_STATUS = "status";
    public static final String JOB_DURATION = "duration";
    public static final String JOB_CURRENT_LOCALE = "currentLocale";
    public static final String JOB_SITEKEY = "sitekey";
    public static final String JOB_USERKEY = "userkey";
    public static final String JOB_MESSAGE = "message";

    public static final String STATUS_ADDED = "added";
    public static final String STATUS_SCHEDULED = "scheduled";
    public static final String STATUS_EXECUTING = "executing";
    public static final String STATUS_SUCCESSFUL = "successful";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_CANCELED = "canceled";

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
        jobDataMap.put(JOB_CREATED, new Date()); // creation date
        jobDataMap.put(JOB_STATUS, STATUS_ADDED); // status "added"
        final JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        JahiaUser currentUser = sessionFactory.getCurrentUser();
        if (currentUser != null) {
            jobDataMap.put(JOB_USERKEY, sessionFactory.getCurrentUser().getUserKey());
        }
        jobDataMap.put(JOB_CURRENT_LOCALE, sessionFactory.getCurrentLocale() != null ? sessionFactory
                .getCurrentLocale().toString() : null);

        jobDetail.setJobDataMap(jobDataMap);
        return jobDetail;
    }

    public static String getGroupName(Class<? extends BackgroundJob> c) {
        String name = c.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        JobDetail jobDetail = ctx.getJobDetail();
        JobDataMap data = jobDetail.getJobDataMap();
        final JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        try {
        	String userKey = data.getString(JOB_USERKEY);
        	if (userKey != null && !userKey.equals(JahiaLoginModule.SYSTEM)) {
                JCRUserNode userNode = JahiaUserManagerService.getInstance().lookup(userKey);
                if (userNode != null) {
                    sessionFactory.setCurrentUser(userNode.getJahiaUser());
	            	logger.debug("Executing job as user {}", userKey);
	            } else {
	            	logger.warn("Unable to lookup job user for key {}", userKey);
                    throw new JobExecutionException("Unable to lookup job user for key " + userKey);
	            }
        	} else {
                logger.debug("Executing job as system user");
            }
        	String langKey = data.getString(JOB_CURRENT_LOCALE);
        	if (langKey != null) {
        		sessionFactory.setCurrentLocale(LanguageCodeConverters.languageCodeToLocale(langKey));
            	logger.debug("Executing job with locale {}", langKey);
        	}

        	// do execute job
            executeJahiaJob(ctx);

        } catch (Exception e) {
            logger.error("Error executing job " + jobDetail.getKey(), e);
            data.put(JOB_STATUS, STATUS_FAILED);
            if (e instanceof JobExecutionException) {
                throw (JobExecutionException) e;
            } else {
                throw new JobExecutionException(e);
            }
        } finally {
            try {
            	this.postExecution(ctx);
            } finally {
                JcrSessionFilter.endRequest();
            }
        }
    }

    public abstract void executeJahiaJob(JobExecutionContext jobExecutionContext)
            throws Exception;

    /**
     * Sub class can perform specific post execution task
     *
     * @param jobExecutionContext
     */
    protected void postExecution(JobExecutionContext jobExecutionContext) {
        // by default do nothing
    }
}
