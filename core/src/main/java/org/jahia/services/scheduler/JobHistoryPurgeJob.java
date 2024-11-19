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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.jahia.registries.ServicesRegistry;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background task that purges completed jobs based on the configured expiration.
 *
 * @author Sergiy Shyrkov
 */
public class JobHistoryPurgeJob extends BackgroundJob {

    private static final Logger logger = LoggerFactory.getLogger(JobHistoryPurgeJob.class);

    @Override
    public void executeJahiaJob(JobExecutionContext ctx) throws Exception {
        JobDataMap jobDataMap = ctx.getJobDetail().getJobDataMap();
        @SuppressWarnings("unchecked")
        Map<String, Long> purgeStrategyData = (Map<String, Long>) jobDataMap.get("purgeStrategy");
        if (purgeStrategyData.isEmpty()) {
            logger.info("No purge strategy configured. Skip execution of this task.");
            return;
        }
        logger.info("Loaded the following purge strategy {}", purgeStrategyData);

        Map<Pattern, Long> purgeStrategy = new LinkedHashMap<>(
                purgeStrategyData.size());

        for (Map.Entry<String, Long> entry : purgeStrategyData.entrySet()) {
            purgeStrategy.put(Pattern.compile(entry.getKey()), entry.getValue());
        }
        boolean purgeWithNoEndDate = !jobDataMap.containsKey("purgeWithNoEndDate")
                || jobDataMap.getBooleanValueFromString("purgeWithNoEndDate");
        ServicesRegistry.getInstance().getSchedulerService()
                .deleteAllCompletedJobs(purgeStrategy, purgeWithNoEndDate);
    }

    protected Long getAge(String jobName, String jobGroup, Map<Pattern, Long> purgeStrategy) {
        Long expiration = null;
        String key = jobGroup + "." + jobName;
        for (Map.Entry<Pattern, Long> purgeEntry : purgeStrategy.entrySet()) {
            if (purgeEntry.getKey().matcher(key).matches()) {
                expiration = purgeEntry.getValue();
                break;
            }
        }

        return expiration;
    }

}
