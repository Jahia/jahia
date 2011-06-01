package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.content.PublicationJob;
import org.jahia.services.content.rules.ActionJob;
import org.jahia.services.content.rules.RuleJob;
import org.jahia.services.content.textextraction.TextExtractorJob;
import org.jahia.services.importexport.ImportJob;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;

import java.util.*;

/**
 * User: toto
 * Date: Sep 17, 2010
 * Time: 2:04:17 PM
 * 
 */
public class SchedulerHelper {
	
	private static final Comparator<GWTJahiaJobDetail> JOB_COMPARATOR = new Comparator<GWTJahiaJobDetail>() {
        public int compare(GWTJahiaJobDetail o1, GWTJahiaJobDetail o2) {
            return -o1.compareTo(o2);
        }
    };
    
    private SchedulerService scheduler;

    public void setScheduler(SchedulerService scheduler) {
        this.scheduler = scheduler;
    }

    private Long getLong(JobDataMap jobDataMap, String key) {
        if (jobDataMap.get(key) == null) {
            return null;
        }
        return Long.parseLong(jobDataMap.getString(key));
    }

    private List<GWTJahiaJobDetail> convertToGWTJobs(List<JobDetail> jobDetails, Locale locale, JahiaUser jahiaUser) {
        List<GWTJahiaJobDetail> jobs = new ArrayList<GWTJahiaJobDetail>();
        for (JobDetail jobDetail : jobDetails) {
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            Date created = (Date) jobDataMap.get(BackgroundJob.JOB_CREATED);
            final String status = jobDataMap.getString(BackgroundJob.JOB_STATUS);
            final String user = StringUtils.substringAfter(jobDataMap.getString(BackgroundJob.JOB_USERKEY), "}");
            final String message = jobDataMap.getString(BackgroundJob.JOB_MESSAGE);
            final Long beginTime = getLong(jobDataMap, BackgroundJob.JOB_BEGIN);
            final Long endTime = getLong(jobDataMap, BackgroundJob.JOB_END);
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
            GWTJahiaJobDetail job = new GWTJahiaJobDetail(jobDetail.getName(), created, user, description,
                    status, message, targetPaths,
                    jobDetail.getGroup(), jobDetail.getJobClass().getName(), beginTime, endTime, duration, jobLocale, fileName, targetNodeIdentifier, targetAction, targetWorkspace);
            job.setLabel(JahiaResourceBundle.getJahiaInternalResource("label." + jobDetail.getGroup() + ".task", locale));
            jobs.add(job);
        }
        return jobs;
    }

    public List<GWTJahiaJobDetail> getActiveJobs(Locale locale) throws GWTJahiaServiceException {
        try {
            List<JobDetail> l = scheduler.getAllActiveJobs();
            return convertToGWTJobs(l, locale, null);
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
            List<GWTJahiaJobDetail> gwtJobList = convertToGWTJobs(jobDetails, locale, jahiaUser);
            // do an inverse sort.
            Collections.sort(gwtJobList, JOB_COMPARATOR);
            return gwtJobList;
        } catch (Exception e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }

    }

    public Boolean deleteJob(String jobName, String groupName) throws GWTJahiaServiceException {
        try {
            return scheduler.getScheduler().deleteJob(jobName, groupName);
        } catch (SchedulerException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public List<String> getAllJobGroupNames() throws GWTJahiaServiceException {
        try {
            return Arrays.asList(scheduler.getScheduler().getJobGroupNames());
        } catch (SchedulerException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

	public Integer deleteAllCompletedJobs() throws GWTJahiaServiceException {
        try {
            return scheduler.deleteAllCompletedJobs();
        } catch (SchedulerException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }
}
