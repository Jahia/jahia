/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.widget.poller.ProcessPollingEvent;
import org.jahia.ajax.gwt.commons.server.ManagedGWTResource;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.PublicationJob;
import org.jahia.services.content.rules.ActionJob;
import org.jahia.services.content.rules.RuleJob;
import org.jahia.services.content.textextraction.TextExtractorJob;
import org.jahia.services.importexport.ImportJob;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.usermanager.JahiaUser;
import org.quartz.*;
import org.quartz.listeners.JobListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * User: toto
 * Date: Sep 17, 2010
 * Time: 2:04:17 PM
 */
public class SchedulerHelper {
    private Logger logger = LoggerFactory.getLogger(SchedulerHelper.class);

    private static final Comparator<GWTJahiaJobDetail> JOB_COMPARATOR = new Comparator<GWTJahiaJobDetail>() {
        public int compare(GWTJahiaJobDetail o1, GWTJahiaJobDetail o2) {
            return -o1.compareTo(o2);
        }
    };

    private SchedulerService scheduler;

    public void setScheduler(SchedulerService scheduler) {
        this.scheduler = scheduler;
    }

    public void start() {
        try {
            scheduler.getScheduler().addGlobalJobListener(new PollingSchedulerListener());
        } catch (SchedulerException e) {
            logger.error("Cannot register job listener", e);
        }
    }

    private Long getLong(JobDataMap jobDataMap, String key) {
        if (jobDataMap.get(key) == null) {
            return null;
        }
        return Long.parseLong(jobDataMap.getString(key));
    }

    private List<GWTJahiaJobDetail> convertToGWTJobs(List<JobDetail> jobDetails) {
        List<GWTJahiaJobDetail> jobs = new ArrayList<GWTJahiaJobDetail>();
        for (JobDetail jobDetail : jobDetails) {
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            Date created = (Date) jobDataMap.get(BackgroundJob.JOB_CREATED);
            final String status = jobDataMap.getString(BackgroundJob.JOB_STATUS);
            final String user = StringUtils.substringAfter(jobDataMap.getString(BackgroundJob.JOB_USERKEY), "}");
            final String message = jobDataMap.getString(BackgroundJob.JOB_MESSAGE);
            final Long beginTime = getLong(jobDataMap, BackgroundJob.JOB_BEGIN);
            final Long endTime = getLong(jobDataMap, BackgroundJob.JOB_END);
            final String site = jobDataMap.getString(BackgroundJob.JOB_SITEKEY);
            if (created == null && beginTime != null) {
                // this can happen for cron scheduler jobs.
                created = new Date(beginTime);
            }
            Long duration = getLong(jobDataMap, BackgroundJob.JOB_DURATION);
            if ((duration == null) && (beginTime != null) && (endTime == null) && BackgroundJob.STATUS_EXECUTING.equals(status)) {
                // here we have a currently running job, let's calculate the duration until now.
                duration = System.currentTimeMillis() - beginTime.longValue();
            }
            final String jobLocale = jobDataMap.getString(BackgroundJob.JOB_CURRENT_LOCALE);
            String targetNodeIdentifier = null;
            String targetAction = null;
            String targetWorkspace = null;

//            if ((jahiaUser != null) && (!jahiaUser.getUserKey().equals(user))) {
            // we must check whether the user has the permission to view other users's jobs
//                if (!jahiaUser.isPermitted(new PermissionIdentity("view-all-jobs"))) {
//                    // he doesn't we skip this entry.
//                    continue;
//                }
//            }

            String description = jobDetail.getDescription();
            final List<String> targetPaths = new ArrayList<String>();
            String fileName = jobDataMap.getString(ImportJob.FILENAME);
            if (BackgroundJob.getGroupName(PublicationJob.class).equals(jobDetail.getGroup())) {
                @SuppressWarnings("unchecked")
                List<GWTJahiaNodeProperty> publicationInfos = (List<GWTJahiaNodeProperty>) jobDataMap.get(PublicationJob.PUBLICATION_PROPERTIES);
                if (publicationInfos != null && publicationInfos.size() > 0) {
                    description += " " + publicationInfos.get(0).getValues();
                }
                final List<String> uuids = (List<String>) jobDataMap.get("publicationInfos");
                try {
                    JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                        @Override
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            for (String uuid : uuids) {
                                try {
                                    targetPaths.add(session.getNodeByIdentifier(uuid).getPath());
                                } catch (ItemNotFoundException e) {
                                    logger.debug("Cannot get item " + uuid, e);
                                }
                            }
                            return null;
                        }
                    });
                } catch (RepositoryException e) {
                    logger.error("Cannot get publication details", e);
                }
            } else if (BackgroundJob.getGroupName(ImportJob.class).equals(jobDetail.getGroup())) {
                String uri = (String) jobDataMap.get(ImportJob.URI);
                if (uri != null) {
                    targetPaths.add(uri);
                    description += " " + uri;
                } else {
                    String destinationParentPath = jobDataMap.getString(ImportJob.DESTINATION_PARENT_PATH);
                    targetPaths.add(destinationParentPath);
                }
            } else if (BackgroundJob.getGroupName(ActionJob.class).equals(jobDetail.getGroup())) {
                String actionToExecute = jobDataMap.getString(ActionJob.JOB_ACTION_TO_EXECUTE);
                targetAction = actionToExecute;
                String nodeUUID = jobDataMap.getString(ActionJob.JOB_NODE_UUID);
                targetNodeIdentifier = nodeUUID;
            } else if (BackgroundJob.getGroupName(RuleJob.class).equals(jobDetail.getGroup())) {
                String ruleToExecute = jobDataMap.getString(RuleJob.JOB_RULE_TO_EXECUTE);
                targetAction = ruleToExecute;
                String nodeUUID = jobDataMap.getString(RuleJob.JOB_NODE_UUID);
                targetNodeIdentifier = nodeUUID;
                String workspace = jobDataMap.getString(RuleJob.JOB_WORKSPACE);
                targetWorkspace = workspace;
            } else if (BackgroundJob.getGroupName(TextExtractorJob.class).equals(jobDetail.getGroup())) {
                String path = jobDataMap.getString(TextExtractorJob.JOB_PATH);
                String extractNodePath = jobDataMap.getString(TextExtractorJob.JOB_EXTRACTNODE_PATH);
                targetPaths.add(path);
                targetPaths.add(extractNodePath);
            }
            GWTJahiaJobDetail job = new GWTJahiaJobDetail(jobDetail.getName(), created, user, site, description,
                    status, message, targetPaths,
                    jobDetail.getGroup(), jobDetail.getJobClass().getName(), beginTime, endTime, duration, jobLocale, fileName, targetNodeIdentifier, targetAction, targetWorkspace);
            job.setLabelKey("label." + jobDetail.getGroup() + ".task");
            jobs.add(job);
        }
        return jobs;
    }

    public List<GWTJahiaJobDetail> getActiveJobs(Locale locale) throws GWTJahiaServiceException {
        try {
            List<JobDetail> l = scheduler.getAllActiveJobs();
            return convertToGWTJobs(l);
        } catch (Exception e) {
            throw new GWTJahiaServiceException("Error retrieving active jobs", e);
        }
    }

    public List<GWTJahiaJobDetail> getAllJobs(Locale locale, JahiaUser jahiaUser, Set<String> groupNames) throws GWTJahiaServiceException {
        try {
            List<JobDetail> jobDetails = null;
            if (groupNames == null) {
                jobDetails = scheduler.getAllJobs();
            } else {
                jobDetails = new ArrayList<JobDetail>();
                for (String groupName : groupNames) {
                    jobDetails.addAll(scheduler.getAllJobs(groupName));
                }
            }
            List<GWTJahiaJobDetail> gwtJobList = convertToGWTJobs(jobDetails);
            // do an inverse sort.
            Collections.sort(gwtJobList, JOB_COMPARATOR);
            return gwtJobList;
        } catch (Exception e) {
            throw new GWTJahiaServiceException("Cannot retrieve jobs. Cause: " + e.getLocalizedMessage(), e);
        }

    }

    public Boolean deleteJob(String jobName, String groupName) throws GWTJahiaServiceException {
        try {
            return scheduler.getScheduler().deleteJob(jobName, groupName);
        } catch (SchedulerException e) {
            throw new GWTJahiaServiceException("Cannot delete job " + jobName + ". Cause: " + e.getLocalizedMessage(), e);
        }
    }

    public List<String> getAllJobGroupNames() throws GWTJahiaServiceException {
        try {
            return Arrays.asList(scheduler.getScheduler().getJobGroupNames());
        } catch (SchedulerException e) {
            throw new GWTJahiaServiceException("Cannot get all job group names. Cause: " + e.getLocalizedMessage(), e);
        }
    }

    public Integer deleteAllCompletedJobs() throws GWTJahiaServiceException {
        try {
            return scheduler.deleteAllCompletedJobs();
        } catch (SchedulerException e) {
            throw new GWTJahiaServiceException("Cannot delete completed jobs. Cause: " + e.getLocalizedMessage(), e);
        }
    }


    class PollingSchedulerListener extends JobListenerSupport {
        int totalCount = -1;

        public String getName() {
            return "PollingSchedulerListener";
        }

        @Override
        public void jobToBeExecuted(JobExecutionContext context) {
            updateJobs(Arrays.asList(context.getJobDetail()), Collections.<JobDetail>emptyList());
        }

        @Override
        public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
            updateJobs(Collections.<JobDetail>emptyList(), Arrays.asList(context.getJobDetail()));
        }

        private void updateJobs(List<JobDetail> startedJob, List<JobDetail> endedJob) {
            try {
                if (totalCount == -1) {
                    totalCount = getActiveJobs(Locale.ENGLISH).size();
                } else {
                    totalCount += startedJob.size();
                }
                totalCount -= endedJob.size();
                final BroadcasterFactory broadcasterFactory = BroadcasterFactory.getDefault();
                if (broadcasterFactory != null) {
                    Broadcaster broadcaster = broadcasterFactory.lookup(ManagedGWTResource.GWT_BROADCASTER_ID);
                    if (broadcaster != null) {
                        ProcessPollingEvent pollingEvent = new ProcessPollingEvent();
                        if (startedJob != null) {
                            pollingEvent.setStartedJob(convertToGWTJobs(startedJob));
                        }
                        if (endedJob != null) {
                            pollingEvent.setEndedJob(convertToGWTJobs(endedJob));
                        }
                        pollingEvent.setTotalCount(totalCount);
                        broadcaster.broadcast(pollingEvent);
                    }
                }
            } catch (GWTJahiaServiceException e) {
                logger.error("Cannot parse jobs", e);
            }

        }
    }

}
