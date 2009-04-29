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

import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.hibernate.cache.JahiaBatchingClusterCacheHibernateProvider;
import org.quartz.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 9 ao�t 2005
 * Time: 17:48:11
 * To change this template use File | Settings | File Templates.
 */
public class RetentionRuleJob implements StatefulJob {

    public static final String JOB_NAME_PREFIX = "RetentionRuleJob_";
    public static final String JOB_GROUP_NAME = "RetentionRuleJob";
    public static final String FROM_DATE_ALREADY_HANDLED = "REACHED_FROM_DATE";
    public static final String RULE_ID = "RULE_ID";

    static final String TRIGGER_NAME_PREFIX = "RetentionRuleJobTrigger_";

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(RetentionRuleJob.class);

    public RetentionRuleJob () {
    }

    public void execute (JobExecutionContext context)
        throws JobExecutionException {

        try {
            JobDetail jobDetail = context.getJobDetail();
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            Integer ruleId = (Integer)jobDataMap.get(RULE_ID);
            if ( ruleId != null ){
                Boolean fromDateAlreadyHandled = (Boolean)jobDataMap.get(FROM_DATE_ALREADY_HANDLED);
                RetentionRuleEvent event = null;
                if ( fromDateAlreadyHandled == null){
                    jobDataMap.put(FROM_DATE_ALREADY_HANDLED,Boolean.TRUE);
                    event = new RetentionRuleEvent(this,
                            Jahia.getThreadParamBean(),
                            ruleId.intValue(),
                            RetentionRuleEvent.RULE_SCHEDULING_NOTIFICATION,
                            RetentionRuleEvent.VALID_FROM_DATE_REACHED);
                } else {
                    event = new RetentionRuleEvent(this,
                            Jahia.getThreadParamBean(),
                            ruleId.intValue(),
                            RetentionRuleEvent.RULE_SCHEDULING_NOTIFICATION,
                            RetentionRuleEvent.VALID_TO_DATE_REACHED);
                }
                if ( event != null ){
                    try {
                        ServicesRegistry.getInstance().getJahiaEventService()
                                .fireTimeBasedPublishingStateChange(event);
                    } catch ( Exception t){
                        logger.debug("Exception occured when processing timebase publishing",t);
                    }
                }
            }

            String status = BackgroundJob.STATUS_POOLED;
            Date nextFireTime = context.getNextFireTime();

            if (nextFireTime != null) {
                status = BackgroundJob.STATUS_POOLED;
                jobDataMap.putAsString(BackgroundJob.JOB_SCHEDULED,nextFireTime.getTime());
            } else {
                ServicesRegistry.getInstance().getSchedulerService().startRequest();
                RetentionRule rule = ServicesRegistry.getInstance().getTimeBasedPublishingService()
                        .getRetentionRule(ruleId.intValue());
                if ( !rule.getRuleType().equals(RetentionRule.RULE_START_AND_END_DATE) ){
                    rule.scheduleNextJob(Jahia.getThreadParamBean());
                }
            }
            jobDataMap.put(BackgroundJob.JOB_STATUS, status);
        } catch ( Exception t ){
            logger.debug("Exception executing retention rule",t);
        } finally {
            ServicesRegistry.getInstance().getJahiaEventService().fireAggregatedEvents();
            ServicesRegistry.getInstance().getCacheService().syncClusterNow();
            JahiaBatchingClusterCacheHibernateProvider.syncClusterNow();
            try {
                ServicesRegistry.getInstance().getSchedulerService().endRequest();
            } catch ( Exception t ){
                logger.debug("Exception scheduling waiting jobs", t);
            }
        }
    }

}
