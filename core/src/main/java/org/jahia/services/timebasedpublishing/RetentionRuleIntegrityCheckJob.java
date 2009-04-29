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
 package org.jahia.services.timebasedpublishing;

import org.jahia.hibernate.manager.JahiaObjectDelegate;
import org.jahia.registries.ServicesRegistry;
import org.quartz.*;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 * A periodical job for checking retention rule integrity.
 *
 * User: hollis
 * Date: 9 ao?t 2005
 * Time: 17:48:11
 * To change this template use File | Settings | File Templates.
 */
public class RetentionRuleIntegrityCheckJob implements StatefulJob {

    public static final String JOB_NAME = "RetentionRuleIntegrityCheckJob";
    public static final String MAX_ELAPSED_INTERVAL = "MAX_ELAPSED_INTERVAL";
    public static final String ENABLED = "ENABLED";

    public static final String TRIGGER_NAME = "RetentionRuleIntegrityCheckTrigger";

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(RetentionRuleIntegrityCheckJob.class);

    public RetentionRuleIntegrityCheckJob () {
    }

    public void execute (JobExecutionContext context)
        throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        Long maxElapsedInterval = (Long)jobDataMap.get(MAX_ELAPSED_INTERVAL);
        Boolean enabled = (Boolean)jobDataMap.get(ENABLED);
        if ( enabled != null && !enabled.booleanValue() ){
            logger.info("retention rule integrity check disabled");
            return;
        }
        if (maxElapsedInterval== null || maxElapsedInterval.longValue()<(120000)){
            maxElapsedInterval = new Long(120000); // 2 min elapsed tim
        }
        try {
            List objs = ServicesRegistry.getInstance()
                .getTimeBasedPublishingService()
                .findInconsistentObjects(System.currentTimeMillis(),
                        maxElapsedInterval.longValue());
            Iterator iterator = objs.iterator();
            JahiaObjectDelegate jDelegate = null;
            while ( iterator.hasNext() ){
                jDelegate = (JahiaObjectDelegate)iterator.next();
                RetentionRule rule = jDelegate.getRule();
                if ( rule != null ){
                    logger.debug("restarting retention rule job as the attached object[" + jDelegate.getObjectKey().toString() + "] seems inconsistent");
                    try {
                        rule.startJob();
                    } catch ( Exception t ){
                        logger.debug("Error restarting retention rule job",t);
                    }
                }
            }
        } catch ( Exception t){
            logger.debug("Error launching retention rule integrity check",t);
        }
    }

}
