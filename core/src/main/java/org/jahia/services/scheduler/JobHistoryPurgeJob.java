/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
