/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.history;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unused node version history purge operation as a background job.
 * TODO (TECH-1834): to be rework and replace by a better system to manage version history of removed nodes
 * @author Sergiy Shyrkov
 */
public class UnusedVersionHistoryJob extends BackgroundJob {
    private static Logger logger = LoggerFactory.getLogger(UnusedVersionHistoryJob.class);

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();

        long maxUnused = Long.parseLong(StringUtils.defaultString((String) data.get("maxUnused"), "0"));

        long purgeOlderThanTimestamp = getPurgeOlderThanTimestamp(data);

        long timer = System.currentTimeMillis();

        VersionHistoryCheckStatus status = NodeVersionHistoryHelper.checkUnused(maxUnused, true,
                purgeOlderThanTimestamp, null);

        logger.info("Purged unused version histories in {} ms. Status: {}",
                new String[] { String.valueOf(System.currentTimeMillis() - timer), status.toString() });
    }

    private long getPurgeOlderThanTimestamp(JobDataMap data) {
        long purgeOlderThanTimestamp = 0;

        String ageValue = (String) data.get("ageInDays");
        if (ageValue != null) {
            long age = Long.parseLong(ageValue);
            purgeOlderThanTimestamp = age > 0 ? (System.currentTimeMillis() - age * 24L * 60L * 60L * 1000L) : 0;
        } else {
            ageValue = (String) data.get("age");
            if (ageValue != null) {
                long age = Long.parseLong(ageValue);
                purgeOlderThanTimestamp = age > 0 ? (System.currentTimeMillis() - age) : 0;
            } else {
                ageValue = (String) data.get("ageTimestamp");
                if (ageValue != null) {
                    long age = Long.parseLong(ageValue);
                    purgeOlderThanTimestamp = age > 0 ? age : 0;
                }
            }
        }

        return purgeOlderThanTimestamp;
    }
}
