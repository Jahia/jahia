package org.jahia.ajax.gwt.helper;

import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.PublicationInfo;
import org.jahia.services.content.PublicationJob;
import org.jahia.services.content.rules.ActionJob;
import org.jahia.services.content.rules.RuleJob;
import org.jahia.services.content.textextraction.TextExtractorJob;
import org.jahia.services.importexport.ImportJob;
import org.jahia.services.rbac.PermissionIdentity;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 17, 2010
 * Time: 2:04:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class SchedulerHelper {
    private SchedulerService scheduler;

    public SchedulerService getScheduler() {
        return scheduler;
    }

    public void setScheduler(SchedulerService scheduler) {
        this.scheduler = scheduler;
    }

    private Long getLong(JobDataMap jobDataMap, String key) {
        if (jobDataMap.get(key) == null) {
            return null;
        }
        return Long.parseLong(jobDataMap.getString(key));
    }

    private Integer getInteger(JobDataMap jobDataMap, String key) {
        if (jobDataMap.get(key) == null) {
            return null;
        }
        return Integer.parseInt(jobDataMap.getString(key));
    }

    private List<GWTJahiaJobDetail> convertToGWTJobs(List<JobDetail> jobDetails, Locale locale, JahiaUser jahiaUser) {
        List<GWTJahiaJobDetail> jobs = new ArrayList<GWTJahiaJobDetail>();
        for (JobDetail jobDetail : jobDetails) {
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            final Date created = (Date) jobDataMap.get(BackgroundJob.JOB_CREATED);
            final String status = jobDataMap.getString(BackgroundJob.JOB_STATUS);
            final String user = jobDataMap.getString(BackgroundJob.JOB_USERKEY);
            final String message = jobDataMap.getString(BackgroundJob.JOB_MESSAGE);
            final Long beginTime = getLong(jobDataMap, BackgroundJob.JOB_BEGIN);
            final Long endTime = getLong(jobDataMap, BackgroundJob.JOB_END);
            final Integer durationInSeconds = getInteger(jobDataMap, BackgroundJob.JOB_DURATION);
            final String jobLocale = jobDataMap.getString(BackgroundJob.JOB_CURRENT_LOCALE);

            if ((jahiaUser != null) && (!jahiaUser.getUserKey().equals(user))) {
                // we must check whether the user has the permission to view other users's jobs
                if (!jahiaUser.isPermitted(new PermissionIdentity("job-manager", "managers", null))) {
                    // he doesn't we skip this entry.
                    continue;
                }
            }

            String description = jobDetail.getDescription();
            final List<String> relatedPaths = new ArrayList<String>();
            String fileName = jobDataMap.getString(ImportJob.FILENAME);
            if (BackgroundJob.getGroupName(PublicationJob.class).equals(jobDetail.getGroup())) {
                List<PublicationInfo> publicationInfos = (List<PublicationInfo>) jobDataMap.get(PublicationJob.PUBLICATION_INFOS);
                if (publicationInfos != null && publicationInfos.size() > 0) {
                    description += " " + publicationInfos.get(0).getRoot().getPath();
                }
            } else if (BackgroundJob.getGroupName(ImportJob.class).equals(jobDetail.getGroup())) {
                String uri = (String) jobDataMap.get(ImportJob.URI);
                if (uri != null) {
                    relatedPaths.add(uri);
                    description += " " + uri;
                } else {
                    String destinationParentPath = jobDataMap.getString(ImportJob.DESTINATION_PARENT_PATH);
                    relatedPaths.add(destinationParentPath);
                }
            } else if (BackgroundJob.getGroupName(ActionJob.class).equals(jobDetail.getGroup())) {
                String actionToExecute = jobDataMap.getString(ActionJob.JOB_ACTION_TO_EXECUTE);
                String nodeUUID = jobDataMap.getString(ActionJob.JOB_NODE_UUID);
            } else if (BackgroundJob.getGroupName(RuleJob.class).equals(jobDetail.getGroup())) {
                String ruleToExecute = jobDataMap.getString(RuleJob.JOB_RULE_TO_EXECUTE);
                String nodeUUID = jobDataMap.getString(RuleJob.JOB_NODE_UUID);
                String workspace = jobDataMap.getString(RuleJob.JOB_WORKSPACE);
            } else if (BackgroundJob.getGroupName(TextExtractorJob.class).equals(jobDetail.getGroup())) {
                String path = jobDataMap.getString(TextExtractorJob.JOB_PATH);
                String provider = jobDataMap.getString(TextExtractorJob.JOB_PROVIDER);
                String extractNodePath = jobDataMap.getString(TextExtractorJob.JOB_EXTRACTNODE_PATH);
                relatedPaths.add(path);
                relatedPaths.add(extractNodePath);
            }
            GWTJahiaJobDetail job = new GWTJahiaJobDetail(jobDetail.getName(), created, user, description,
                    status, message, relatedPaths,
                    jobDetail.getGroup(), jobDetail.getJobClass().getName(), beginTime, endTime, durationInSeconds, jobLocale, fileName);
            job.setLabel(JahiaResourceBundle.getJahiaInternalResource("label." + jobDetail.getGroup() + ".task", locale));
            jobs.add(job);
        }
        return jobs;
    }

    public List<GWTJahiaJobDetail> getActiveJobs(Locale locale) throws GWTJahiaServiceException {
        try {
            List<JobDetail> l = scheduler.getAllActiveJobsDetails();
            return convertToGWTJobs(l, locale, null);
        } catch (JahiaException e) {
            throw new GWTJahiaServiceException("Error retrieving active jobs", e);
        }
    }

    public List<GWTJahiaJobDetail> getAllJobs(Locale locale, JahiaUser jahiaUser, Set<String> groupNames) throws GWTJahiaServiceException {
        try {
            List<JobDetail> jobDetails = null;
            if (groupNames == null) {
                jobDetails = scheduler.getAllJobsDetails();
            } else {
                jobDetails = new ArrayList<JobDetail>();
                for (String groupName : groupNames) {
                    jobDetails.addAll(scheduler.getAllJobsDetails(groupName));
                }
            }
            List<GWTJahiaJobDetail> gwtJobList = convertToGWTJobs(jobDetails, locale, jahiaUser);
            // do an inverse sort.
            Collections.sort(gwtJobList, new Comparator<GWTJahiaJobDetail>() {
                public int compare(GWTJahiaJobDetail o1, GWTJahiaJobDetail o2) {
                    return -o1.compareTo(o2);
                }
            });
            return gwtJobList;
        } catch (JahiaException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }

    }

    public Boolean deleteJob(String jobName, String groupName) throws GWTJahiaServiceException {
        try {
            return scheduler.deleteJob(jobName, groupName);
        } catch (JahiaException e) {
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
}
