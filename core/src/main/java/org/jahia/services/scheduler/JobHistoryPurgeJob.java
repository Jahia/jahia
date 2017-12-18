/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
        logger.info("Loaded the following purge strategy\n{}", purgeStrategyData);

        Map<Pattern, Long> purgeStrategy = new LinkedHashMap<Pattern, Long>(
                purgeStrategyData.size());

        for (Map.Entry<String, Long> entry : purgeStrategyData.entrySet()) {
            purgeStrategy.put(Pattern.compile(entry.getKey()), entry.getValue());
        }
        boolean purgeWithNoEndDate = jobDataMap.containsKey("purgeWithNoEndDate") ? jobDataMap
                .getBooleanValueFromString("purgeWithNoEndDate") : true;
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
