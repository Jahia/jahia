/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.engines.pdisplay.server;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.service.process.ProcessDisplayService;
import org.jahia.ajax.gwt.engines.pdisplay.server.util.GWTProcessJobComparator;
import org.jahia.ajax.gwt.commons.server.AbstractJahiaGWTServiceImpl;
import org.jahia.ajax.gwt.commons.server.rpc.ProcessDisplayHelper;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.data.GWTJahiaProcessJob;
import org.jahia.ajax.gwt.client.data.process.GWTJahiaProcessJobPreference;
import org.jahia.ajax.gwt.client.data.process.GWTJahiaProcessJobStat;
import org.jahia.bin.Jahia;
import org.jahia.content.NodeOperationResult;
import org.jahia.content.TreeOperationResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.search.JahiaSiteIndexingJob;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.content.textextraction.TextExtractorJob;
import org.jahia.params.ParamBean;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;

import java.util.*;

/**
 * User: jahia
 * Date: 10 janv. 2008
 * Time: 11:39:22
 */
public class ProcessDisplayServiceImpl extends AbstractJahiaGWTServiceImpl implements ProcessDisplayService {
    private static transient final JahiaSitesService JAHIA_SITES_SERVICE = ServicesRegistry.getInstance().getJahiaSitesService();
    private static transient final SchedulerService SCHEDULER_SERVICE = ServicesRegistry.getInstance().getSchedulerService();

    private static transient final Logger logger = Logger.getLogger(ProcessDisplayServiceImpl.class);
    private static final String PREF_MAX_JOBS = "maxJobs";
    private static final String PREF_JOBS_PER_PAGE = "jobsPerPage";
    private static final String PREF_MONITORING_AUTOREFRESH = "monitoringAutoRefresh";
    public static final String PREF_PAGE_REFRESH = "refreshAtEndOfAnyPageWorkflow";

    private static final String PREF_JOB_TYPES_TO_IGNORE = "jobTypesToIgnore";
    private static final String PREF_JOB_STATUS_TO_IGNORE = "jobStatusToIgnore";
    private static final String PREF_ONLY_CURRENT_USER = "onlyCurrentUser";
    private static final String SESSION_LAST_JOB_COMPLETE_TIME = "org.jahia.ajax.gwt.engines.pdisplay.server.executing.job.name";


    public GWTJahiaProcessJobStat getGWTProcessJobStat(int mode) {
        GWTJahiaProcessJobStat gwtProcessJobStat = new GWTJahiaProcessJobStat();
        long lastExecutedJobAsLong = SCHEDULER_SERVICE.getLastJobCompletedTime();
        boolean jobIsExecuting = false;
        // current executing jobs
        try {

            List currentlyExecutingJobs = SCHEDULER_SERVICE.getCurrentlyExecutingJobs();
            if (currentlyExecutingJobs != null && currentlyExecutingJobs.size() > 0) {
                gwtProcessJobStat.setJobExecuting(true);
            } else {
                gwtProcessJobStat.setJobExecuting(false);
            }
            // get all job list
            List jobList = getAllJobsDetails();
            int waitingJobNumber = 0;
            int nextJobCurrentUserIndex = -1;
            String nextJobCurrentUserType = "-";
            int maxIndex = jobList.size();
            gwtProcessJobStat.setNumberJobs(maxIndex);
            Long lastExecutedJobTimeInSession = (Long) getThreadLocalRequest().getSession().getAttribute(SESSION_LAST_JOB_COMPLETE_TIME);
            for (int jobIndex = 0; jobIndex < maxIndex; jobIndex++) {
                JobDetail currentJobDetail = (JobDetail) jobList.get(jobIndex);
                JobDataMap currentJobDataMap = currentJobDetail.getJobDataMap();
                //String currentJobName = currentJobDetail.getName();

                // job: type
                String type = currentJobDataMap.getString(BackgroundJob.JOB_TYPE);

                // job: status
                String currentJobStatus = currentJobDataMap.getString(BackgroundJob.JOB_STATUS);

                // job: user key
                String currentJobUserKey = currentJobDataMap.getString(BackgroundJob.JOB_USERKEY);
                if (currentJobStatus.equalsIgnoreCase(BackgroundJob.STATUS_WAITING)) {
                    waitingJobNumber++;
                    if (currentJobUserKey.equalsIgnoreCase(getRemoteUser())) {
                        if (nextJobCurrentUserIndex == -1) {
                            nextJobCurrentUserType = type;
                        }
                    } else {
                        // update nex job index
                        nextJobCurrentUserIndex++;
                    }
                }
                if (currentJobStatus.equalsIgnoreCase(BackgroundJob.STATUS_RUNNING)) {
                    // update nex job index
                    nextJobCurrentUserIndex++;
                    nextJobCurrentUserType = type;
                    jobIsExecuting = true;
                }
            }

            // set need to refresh flag            
            if (jobIsExecuting) {
                gwtProcessJobStat.setNeedRefresh(true);
            } else {
                if (lastExecutedJobTimeInSession != null) {
                    gwtProcessJobStat.setNeedRefresh(lastExecutedJobTimeInSession.floatValue() != lastExecutedJobAsLong);
                }
            }


            // set values
            gwtProcessJobStat.setLastJobCompletedTime(ProcessDisplayHelper.prettyPrintDate(getLocale(), lastExecutedJobAsLong));
            gwtProcessJobStat.setNumberWaitingJobs(waitingJobNumber);
            gwtProcessJobStat.setNextJobCurrentUserIndex(nextJobCurrentUserIndex);
            gwtProcessJobStat.setNextJobCurrentUserType(nextJobCurrentUserType);
        } catch (JahiaException e) {
            logger.error("Unable to get number of running job.", e);
        }
        return gwtProcessJobStat;
    }

    /**
     * Get pDisplay preferences
     *
     * @return pdisplay preferences
     */
    public GWTJahiaProcessJobPreference getPreferences() throws GWTJahiaServiceException {
        final GWTJahiaProcessJobPreference gwtJahiaProcessJobPreferences;
        try {
            // max jobs
            String value = getGenericPreferenceValue(PREF_MAX_JOBS);
            int maxJobs = 100;
            if (value != null && value.length() > 0) {
                try {
                    maxJobs = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    logger.error(value, e);
                }
            }

            // type of job to ignore
            List<String> jobTypesToIgnore = new ArrayList<String>();
            value = getGenericPreferenceValue(PREF_JOB_TYPES_TO_IGNORE);
            if (value != null) {
                StringTokenizer tokenizer = new StringTokenizer(value, "_");
                while (tokenizer.hasMoreTokens()) {
                    jobTypesToIgnore.add(tokenizer.nextToken());
                }
            }

            // jobs status tomignore
            List<String> jobStatusToIgnore = new ArrayList<String>();
            value = getGenericPreferenceValue(PREF_JOB_STATUS_TO_IGNORE);
            if (value != null) {
                StringTokenizer tokenizer = new StringTokenizer(value, "_");
                while (tokenizer.hasMoreTokens()) {
                    jobStatusToIgnore.add(tokenizer.nextToken());
                }
            }

            // only current user flag
            value = getGenericPreferenceValue(PREF_ONLY_CURRENT_USER);
            boolean onlyCurrentUser = false;
            if (value != null) {
                onlyCurrentUser = Boolean.parseBoolean(value);
            }


            int jobsPerPage = 50;
            value = getGenericPreferenceValue(PREF_JOBS_PER_PAGE);
            if (value != null && value.length() > 0) {
                try {
                    jobsPerPage = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    logger.error(value, e);
                }
            }

            boolean autoRefresh = false;
            value = getGenericPreferenceValue(PREF_MONITORING_AUTOREFRESH);
            if (value != null && value.length() > 0) {
                try {
                    autoRefresh = Boolean.parseBoolean(value);
                } catch (NumberFormatException e) {
                    logger.error(value, e);
                }
            }

            boolean refreshAtEndOfAnyPageWorkflow = false;
            value = getGenericPreferenceValue(PREF_PAGE_REFRESH);
            if (value != null && value.length() > 0) {
                try {
                    refreshAtEndOfAnyPageWorkflow = Boolean.parseBoolean(value);
                } catch (NumberFormatException e) {
                    logger.error(value, e);
                }
            }

            gwtJahiaProcessJobPreferences = new GWTJahiaProcessJobPreference();
            gwtJahiaProcessJobPreferences.setMaxJobs(maxJobs);
            gwtJahiaProcessJobPreferences.setAutoRefresh(autoRefresh);
            gwtJahiaProcessJobPreferences.setRefreshAtEndOfAnyPageWorkflow(refreshAtEndOfAnyPageWorkflow);
            gwtJahiaProcessJobPreferences.setJobsPerPage(jobsPerPage);
            gwtJahiaProcessJobPreferences.setJobsStatusToIgnore(jobStatusToIgnore);
            gwtJahiaProcessJobPreferences.setJobsTypeToIgnore(jobTypesToIgnore);
            gwtJahiaProcessJobPreferences.setOnlyCurrentUser(onlyCurrentUser);
            return gwtJahiaProcessJobPreferences;
        } catch (Exception e) {
            logger.error(e, e);
            throw new GWTJahiaServiceException("Can't retrieve process job preferences\n\n" + e.toString());
        }


    }

    /**
     * Save pdisplay preferences
     */
    public void savePreferences(GWTJahiaProcessJobPreference gwtJahiaProcessJobPreferences) {
        int dataType = gwtJahiaProcessJobPreferences.getDataType();
        if (dataType == GWTJahiaProcessJobPreference.PREF_FILTER) {
            List<String> jobsTypeToIgnore = gwtJahiaProcessJobPreferences.getJobsTypeToIgnore();
            List<String> jobsStatusToIgnore = gwtJahiaProcessJobPreferences.getJobsStatusToIgnore();
            boolean onlyCurrentUser = gwtJahiaProcessJobPreferences.isOnlyCurrentUser();
            logger.debug(jobsTypeToIgnore + "," + jobsStatusToIgnore + "," + onlyCurrentUser);

            // jobs type To Ignore
            setGenericPreferenceValue(PREF_JOB_TYPES_TO_IGNORE, getListAsString(jobsTypeToIgnore));

            // jobs type To Ignore
            setGenericPreferenceValue(PREF_JOB_STATUS_TO_IGNORE, getListAsString(jobsStatusToIgnore));

            // only current user
            setGenericPreferenceValue(PREF_ONLY_CURRENT_USER, String.valueOf(onlyCurrentUser));
        } else if (dataType == GWTJahiaProcessJobPreference.PREF_GENERAL) {
            int maxJobs = gwtJahiaProcessJobPreferences.getMaxJobs();
            setGenericPreferenceValue(PREF_MAX_JOBS, String.valueOf(maxJobs));

            int jobsPerPage = gwtJahiaProcessJobPreferences.getJobsPerPage();
            setGenericPreferenceValue(PREF_JOBS_PER_PAGE, String.valueOf(jobsPerPage));

            boolean autoRefresh = gwtJahiaProcessJobPreferences.isAutoRefresh();
            setGenericPreferenceValue(PREF_MONITORING_AUTOREFRESH, String.valueOf(autoRefresh));

            boolean refreshAtEndOfAnyPageWorkflow = gwtJahiaProcessJobPreferences.isRefreshAtEndOfAnyPageWorkflow();
            setGenericPreferenceValue(PREF_PAGE_REFRESH, String.valueOf(refreshAtEndOfAnyPageWorkflow));
        } else {
            logger.error("Pdisplay pref: Data type unknown");
        }

    }

    /**
     * Get a list as strg: ex {"a","b","c"} --> a_b_c
     *
     * @param l a list
     * @return the concat string using "_"
     */
    private String getListAsString(List<String> l) {
        String str = null;
        for (String current : l) {
            if (str == null) {
                str = current;
            } else {
                str = str + "_" + current;
            }
        }
        return str;
    }

    /**
     * Find process job depending on user preferences
     *
     * @param offset        the offset
     * @param sortParameter sort parameter to use
     * @param isAscending   ascending order
     * @return data list in a paging load result
     */
    public PagingLoadResult<GWTJahiaProcessJob> findGWTProcessJobs(int offset, String sortParameter, boolean isAscending) throws GWTJahiaServiceException {
        List<GWTJahiaProcessJob> subList = new ArrayList<GWTJahiaProcessJob>();
        try {
            List<GWTJahiaProcessJob> processJobList = computeProcessJobList(getPreferences());
            // sort job list depending on the sortParameter value
            Collections.sort(processJobList, new GWTProcessJobComparator<GWTJahiaProcessJob>(Integer.parseInt(sortParameter), isAscending));
            // get sub list
            int lastIndex = offset + getPreferences().getJobsPerPage();
            for (int i = offset; i < lastIndex; i++) {
                if (i < processJobList.size()) {
                    subList.add(processJobList.get(i));
                } else {
                    break;
                }
            }
            return new BasePagingLoadResult<GWTJahiaProcessJob>(subList, offset, processJobList.size());
        } catch (Exception e) {
            logger.error(e, e);
            throw new GWTJahiaServiceException("Could not retrieve the jobs :\n\n" + e.toString());
        }
    }

    public void deleteJob(GWTJahiaProcessJob gwtProcessJob) {
        String jobName = gwtProcessJob.getJobName();
        String groupName = gwtProcessJob.getJobGroupName();
        if (logger.isDebugEnabled()) {
            logger.debug("delete job with name: " + jobName + "," + groupName);
        }
        try {
            SCHEDULER_SERVICE.deleteJob(jobName, groupName);
        } catch (JahiaException e) {
            logger.error("Unable to delete a job with the name '" + jobName
                    + "' from group '" + groupName + "'", e);
        }
    }


    public static List<JobDetail> getAllJobsDetails() throws JahiaException {
         if(schedulerIsShutdown()){
            return new ArrayList<JobDetail>();
        }
        List<JobDetail> jobs = SCHEDULER_SERVICE.getAllJobsDetails();
        return jobs;
    }

    public static List<JobDetail> getAllActiveJobsDetails() throws JahiaException {
        if(schedulerIsShutdown()){
            return new ArrayList<JobDetail>();
        }
        List<JobDetail> jobs = SCHEDULER_SERVICE.getAllActiveJobsDetails();
        return jobs;
    }

    private List<GWTJahiaProcessJob> computeProcessJobList(GWTJahiaProcessJobPreference gwtJahiaProcessJobPreferences) throws JahiaException {
        ParamBean jParams = retrieveParamBean();
        int jobsTime = gwtJahiaProcessJobPreferences.getMaxJobs();
        List<String> jobTypesToIgnore = gwtJahiaProcessJobPreferences.getJobsTypeToIgnore();

        // some job type should never be displayed
        jobTypesToIgnore.add(TextExtractorJob.EXTRACTION_TYPE);
        jobTypesToIgnore.add("timebased");

        List<String> jobStatusToIgnore = gwtJahiaProcessJobPreferences.getJobsStatusToIgnore();
        boolean onlyCurrentUserJob = gwtJahiaProcessJobPreferences.isOnlyCurrentUser() || !jParams.getUser().isAdminMember(jParams.getSiteID());
        boolean onlyCurrentSiteJob = !jParams.getUser().isAdminMember(0);
        List<GWTJahiaProcessJob> processJobList = new ArrayList<GWTJahiaProcessJob>();
        List<JobDetail> selectedJobsDetailList = getAllJobsDetails();
        // create GWT objects list
        for (int processIndex = 0; (processIndex < selectedJobsDetailList.size()) && (processIndex < jobsTime); processIndex++) {
            JobDetail currentJobDetail = selectedJobsDetailList.get(processIndex);
            JobDataMap currentJobDataMap = currentJobDetail.getJobDataMap();
            // job: type
            String type = currentJobDataMap.getString(BackgroundJob.JOB_TYPE);

            // job: status
            String currentJobStatus = currentJobDataMap.getString(BackgroundJob.JOB_STATUS);

            // job: user key
            String currentJobUserKey = currentJobDataMap.getString(BackgroundJob.JOB_USERKEY);
            if (currentJobUserKey == null) {
                currentJobUserKey = "";
            }

            String currentJobSiteKey = currentJobDataMap.getString(BackgroundJob.JOB_SITEKEY);

            // add only job that are not ignored
            boolean ignoreDueSite = currentJobSiteKey != null && onlyCurrentSiteJob && !currentJobSiteKey.equals(jParams.getSiteKey());
            boolean ignoreDueType = jobTypesToIgnore != null && jobTypesToIgnore.contains(type);
            boolean ignoreDueStatus = jobStatusToIgnore != null && jobStatusToIgnore.contains(currentJobStatus);
            boolean ignoreDueOwner = currentJobUserKey != null && onlyCurrentUserJob && !currentJobUserKey.equals(getRemoteUser());
            logger.debug("ignore due to type: " + ignoreDueType);
            logger.debug("ignore due to status: " + ignoreDueStatus);
            logger.debug("ignore due to owner: " + ignoreDueOwner);

            if (logger.isDebugEnabled()) {
                logger.debug("Type ignore list size: " + jobTypesToIgnore.size());
                for (int i = 0; i < jobTypesToIgnore.size(); i++) {
                    logger.debug("Type to ignore: " + jobTypesToIgnore.get(i));
                }
            }

            if (!ignoreDueSite && !ignoreDueType && !ignoreDueStatus && !ignoreDueOwner) {
                GWTJahiaProcessJob gwtProcessJob = ProcessDisplayHelper.getGWTJahiaProcessJob(currentJobDetail, jParams);

                processJobList.add(gwtProcessJob);

            } else {
                logger.debug("ignoring job");
            }
        }
        // nested for "need to refresh" flag

        getThreadLocalRequest().getSession().setAttribute(SESSION_LAST_JOB_COMPLETE_TIME, Long.valueOf(SCHEDULER_SERVICE.getLastJobCompletedTime()));
        return processJobList;
    }

    private static List<JobDetail> getSiteIndexingJobs(boolean activeOnly) throws JahiaException {
        List<JobDetail> jobDetails = new ArrayList<JobDetail>();

        // get all site
        Iterator jahiaSitesEnum = JAHIA_SITES_SERVICE.getSites();

        while (jahiaSitesEnum.hasNext()) {
            // current jahia site and settings
            JahiaSite currentJahiaSite = (JahiaSite) jahiaSitesEnum.next();
            Properties currentJahiaSiteSettings = currentJahiaSite.getSettings();
            Iterator currentJahiaSiteSettingsIterator = currentJahiaSiteSettings.keySet().iterator();
            while (currentJahiaSiteSettingsIterator.hasNext()) {
                String key = (String) currentJahiaSiteSettingsIterator.next();
                // get indexing jobs
                int siteIndexingJobNamePosition = key.indexOf(JahiaSiteIndexingJob.SITE_INDEXATION_JOBNAME);
                if (siteIndexingJobNamePosition != -1) {
                    String currentServerId = key.substring(0, siteIndexingJobNamePosition);
                    JobDetail currentJobDetail = new JobDetail(currentJahiaSiteSettings.getProperty(key, ""), JahiaSiteIndexingJob.JOB_GROUP_NAME, JahiaSiteIndexingJob.class);
                    JobDataMap currentJobDataMap = currentJobDetail.getJobDataMap();
//                    currentJobDataMap.setMutable(true); commented for compatibility with quartz 1.6
                    String currentJobStatus = currentJahiaSiteSettings.getProperty(currentServerId + BackgroundJob.JOB_STATUS, "");
                    if (activeOnly && !(BackgroundJob.STATUS_POOLED.equals(currentJobStatus)
                            || BackgroundJob.STATUS_RUNNING.equals(currentJobStatus)
                            || BackgroundJob.STATUS_WAITING.equals(currentJobStatus)
                            || BackgroundJob.STATUS_INTERRUPTED.equals(currentJobStatus))) {
                        continue;
                    }
                    TreeOperationResult result = new TreeOperationResult();
                    if (BackgroundJob.STATUS_FAILED.equals(currentJobStatus)
                            || BackgroundJob.STATUS_ABORTED.equals(currentJobStatus)
                            || BackgroundJob.STATUS_INTERRUPTED.equals(currentJobStatus)) {
                        String jobResult = currentJahiaSiteSettings.getProperty(currentServerId + BackgroundJob.RESULT, "");
                        String localeCode = Locale.getDefault().toString();
                        if (Jahia.getThreadParamBean() != null) {
                            localeCode = Jahia.getThreadParamBean().getLocale().getDisplayName();
                        }
                        NodeOperationResult nodeOperationResult = new NodeOperationResult(null, localeCode, jobResult);
                        result.appendError(nodeOperationResult);
                        currentJobDataMap.put(BackgroundJob.RESULT, result);
                    } else if (BackgroundJob.STATUS_SUCCESSFUL.equals(currentJobStatus)) {
                        currentJobDataMap.put(BackgroundJob.RESULT, result);
                    }
                    // job: server
                    String currentJobServer = currentServerId.substring(0, currentServerId.length() - 1);
                    currentJobDataMap.put(BackgroundJob.JOB_SERVER, currentJobServer);

                    // job: status
                    currentJobDataMap.put(BackgroundJob.JOB_STATUS, currentJobStatus);

                    // job: begin
                    String currentJobBegin = currentJahiaSiteSettings.getProperty(currentServerId + BackgroundJob.JOB_BEGIN, "");
                    currentJobDataMap.put(BackgroundJob.JOB_BEGIN, currentJobBegin);

                    // job: created
                    String currentJobCreated = currentJahiaSiteSettings.getProperty(currentServerId + BackgroundJob.JOB_CREATED, "");
                    currentJobDataMap.put(BackgroundJob.JOB_CREATED, currentJobCreated);

                    // job: duration
                    String currentJobDuration = currentJahiaSiteSettings.getProperty(currentServerId + BackgroundJob.JOB_DURATION, "");
                    currentJobDataMap.put(BackgroundJob.JOB_DURATION, currentJobDuration);

                    //job: end
                    String currentJobEnd = currentJahiaSiteSettings.getProperty(currentServerId + BackgroundJob.JOB_END, "");
                    currentJobDataMap.put(BackgroundJob.JOB_END, currentJobEnd);

                    //job: site key
                    String currentJobSiteKey = currentJahiaSite.getSiteKey();
                    currentJobDataMap.put(BackgroundJob.JOB_SITEKEY, currentJobSiteKey);

                    // job: scheduled
                    String currentJobScheduled = currentJahiaSiteSettings.getProperty(currentServerId + BackgroundJob.JOB_SCHEDULED, "");
                    currentJobDataMap.put(BackgroundJob.JOB_SCHEDULED, currentJobScheduled);

                    // job: userkey
                    String currentJobUserKey = currentJahiaSiteSettings.getProperty(currentServerId + BackgroundJob.JOB_USERKEY, "");
                    currentJobDataMap.put(BackgroundJob.JOB_USERKEY, currentJobUserKey);

                    //job: type
                    String currentJobType = JahiaSiteIndexingJob.SITE_INDEXATION_JOB_TYPE;
                    currentJobDataMap.put(BackgroundJob.JOB_TYPE, currentJobType);

                    //jon interrupt status
                    String currentJobInterruptStatus = currentJahiaSite.getSettings().getProperty(currentServerId + JahiaSiteIndexingJob.INTERRUPT_STATUS, "");
                    currentJobDataMap.put(JahiaSiteIndexingJob.INTERRUPT_STATUS, currentJobInterruptStatus);

                    // add to jobs detail list
                    jobDetails.add(currentJobDetail);
                }
            }
        }
        return jobDetails;
    }

    private static boolean schedulerIsShutdown(){
        try {
            return SCHEDULER_SERVICE.getScheduler().isShutdown();
        } catch (SchedulerException e) {
            logger.error(e,e);
            return false;
        }
    }


}
