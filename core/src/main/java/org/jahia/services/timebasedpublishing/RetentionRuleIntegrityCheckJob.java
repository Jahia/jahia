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
