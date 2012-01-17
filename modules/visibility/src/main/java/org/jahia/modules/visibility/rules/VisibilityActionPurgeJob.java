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

package org.jahia.modules.visibility.rules;

import java.util.Collections;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.rules.ActionJob;
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
 * Background task that purges orphaned visibility actions (in case the corresponding node was deleted).
 * 
 * @author Sergiy Shyrkov
 */
public class VisibilityActionPurgeJob extends BackgroundJob {

    private static final Logger logger = LoggerFactory.getLogger(VisibilityActionPurgeJob.class);

    @Override
    public void executeJahiaJob(JobExecutionContext ctx) throws Exception {
        long timer = System.currentTimeMillis();
        final JobDataMap data = ctx.getJobDetail().getJobDataMap();
        final String workspace = StringUtils.defaultIfEmpty(data.getString("workspace"),
                Constants.LIVE_WORKSPACE);

        Integer[] counts = JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace,
                new JCRCallback<Integer[]>() {
                    @SuppressWarnings("unchecked")
                    public Integer[] doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        Set<String> jobGroupNames = Collections.emptySet();
                        Object val = data.get("visibilityJobGroupNames");
                        if (val != null && val instanceof Set<?>) {
                            jobGroupNames = (Set<String>) val;
                        }
                        logger.info(
                                "Start looking for orphaned start and end date visibility actions in job groups {} and workspace {}",
                                jobGroupNames, workspace);

                        return processJobs(jobGroupNames, session);
                    }
                });

        logger.info(
                "Finished scanning {} visibility action jobs. Deleted {} orphaned jobs. Execution took {} ms",
                new Long[] { Long.valueOf(counts[0]), Long.valueOf(counts[1]),
                        (System.currentTimeMillis() - timer) });
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
                            JobDetail job = scheduler.getJobDetail(trigger.getJobName(),
                                    trigger.getJobGroup());
                            if (job != null) {
                                counts[0]++;
                                String uuid = job.getJobDataMap()
                                        .getString(ActionJob.JOB_NODE_UUID);

                                if (!jobValid(uuid, session)) {
                                    counts[1]++;
                                    logger.info(
                                            "Found orhpaned job {} with node UUID {}. Deleting job.",
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
