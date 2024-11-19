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
package org.jahia.services.content.rules;

import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background task that purges orphaned action jobs (in case the corresponding node is no longer present).
 *
 * @author Sergiy Shyrkov
 */
public class OrphanedActionPurgeJob extends BackgroundJob {

    private static final Logger logger = LoggerFactory.getLogger(OrphanedActionPurgeJob.class);

    @Override
    public void executeJahiaJob(JobExecutionContext ctx) throws Exception {
        long timer = System.currentTimeMillis();
        final JobDataMap data = ctx.getJobDetail().getJobDataMap();
        final String workspace = StringUtils.defaultIfEmpty(data.getString("workspace"), Constants.LIVE_WORKSPACE);
        final Set<String> jobGroupNames = getJobGroupNames(data);
        if (jobGroupNames == null || jobGroupNames.isEmpty()) {
            logger.debug("No job group names to scan. Skipping.");
            return;
        }

        Integer[] counts = JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null,
                new JCRCallback<Integer[]>() {
                    public Integer[] doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        logger.info("Start looking for orphaned action jobs in job groups {} and workspace {}",
                                jobGroupNames, workspace);

                        return processJobs(jobGroupNames, session);
                    }
                });

        logger.info("Finished scanning {} action jobs. Deleted {} orphaned jobs. Execution took {} ms", new Long[] {
                Long.valueOf(counts[0]), Long.valueOf(counts[1]), (System.currentTimeMillis() - timer) });
    }

    @SuppressWarnings("unchecked")
    protected Set<String> getJobGroupNames(JobDataMap data) {
        Object val = data.get("jobGroupNames");
        return val != null && val instanceof Set<?> ? (Set<String>) val : null;
    }

    private boolean jobValid(String uuid, JCRSessionWrapper session) throws RepositoryException {
        if (uuid != null) {
            try {
                session.getNodeByIdentifier(uuid);
            } catch (ItemNotFoundException e) {
                // node not present anymore
                uuid = null;
            }
        }
        return uuid != null;
    }

    private Integer[] processJobs(Set<String> jobGroupNames, JCRSessionWrapper session) {
        Scheduler scheduler = ServicesRegistry.getInstance().getSchedulerService().getScheduler();
        final Integer[] counts = { 0, 0 };

        try {
            for (String group : scheduler.getTriggerGroupNames()) {
                String[] triggerNames = scheduler.getTriggerNames(group);
                for (String triggerName : triggerNames) {
                    try {
                        Trigger trigger = scheduler.getTrigger(triggerName, group);
                        if (trigger != null && jobGroupNames.contains(trigger.getJobGroup())) {
                            JobDetail job = scheduler.getJobDetail(trigger.getJobName(), trigger.getJobGroup());
                            if (job != null) {
                                counts[0]++;
                                String uuid = job.getJobDataMap().getString(ActionJob.JOB_NODE_UUID);

                                if (!jobValid(uuid, session)) {
                                    counts[1]++;
                                    logger.info("Found orhpaned job {} with node UUID {}. Deleting job.",
                                            job.getFullName(), uuid);
                                    scheduler.deleteJob(job.getName(), job.getGroup());
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Error handling trigger " + group + "." + triggerName, e);
                    }
                }
            }
        } catch (SchedulerException e) {
            logger.warn(e.getMessage(), e);
        }

        return counts;
    }
}
