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
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    private List<GWTJahiaJobDetail> convertToGWTJobs(List<JobDetail> jobDetails, Locale locale) {
        List<GWTJahiaJobDetail> jobs = new ArrayList<GWTJahiaJobDetail>();
        for (JobDetail jobDetail : jobDetails) {
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            final String type = jobDataMap.getString(BackgroundJob.JOB_TYPE);
            final Date created = (Date) jobDataMap.get(BackgroundJob.JOB_CREATED);
            final String status = jobDataMap.getString(BackgroundJob.JOB_STATUS);
            final String user = jobDataMap.getString(BackgroundJob.JOB_USERKEY);
            final String message = jobDataMap.getString(BackgroundJob.JOB_MESSAGE);
            final List<String> relatedPaths = new ArrayList<String>();
            if (PublicationJob.PUBLICATION_TYPE.equals(type)) {
                List<PublicationInfo> publicationInfos = (List<PublicationInfo>) jobDataMap.get(PublicationJob.PUBLICATION_INFOS);
                for (PublicationInfo publicationInfo : publicationInfos) {
                    relatedPaths.add(publicationInfo.getRoot().getPath());
                }
            } else if (ImportJob.IMPORT_TYPE.equals(type)) {
                String uri = (String) jobDataMap.get(ImportJob.URI);
                relatedPaths.add(uri);
            } else if (ActionJob.ACTION_TYPE.equals(type)) {
                String actionToExecute = jobDataMap.getString(ActionJob.JOB_ACTION_TO_EXECUTE);
                String nodeUUID = jobDataMap.getString(ActionJob.JOB_NODE_UUID);
            } else if (RuleJob.RULE_TYPE.equals(type)) {
                String ruleToExecute = jobDataMap.getString(RuleJob.JOB_RULE_TO_EXECUTE);
                String nodeUUID = jobDataMap.getString(RuleJob.JOB_NODE_UUID);
                String workspace = jobDataMap.getString(RuleJob.JOB_WORKSPACE);
            } else if (TextExtractorJob.EXTRACTION_TYPE.equals(type)) {
                String path = jobDataMap.getString(TextExtractorJob.JOB_PATH);
                String provider = jobDataMap.getString(TextExtractorJob.JOB_PROVIDER);
                String extractNodePath = jobDataMap.getString(TextExtractorJob.JOB_EXTRACTNODE_PATH);
                relatedPaths.add(path);
                relatedPaths.add(extractNodePath);
            }
            GWTJahiaJobDetail job = new GWTJahiaJobDetail(jobDetail.getName(), type, created, user, jobDetail.getDescription(),
                    status, message, relatedPaths,
                    jobDetail.getGroup(), jobDetail.getJobClass().getName());
            job.setLabel(JahiaResourceBundle.getJahiaInternalResource("label." + type + ".task", locale));
            jobs.add(job);
        }
        return jobs;
    }

    public List<GWTJahiaJobDetail> getActiveJobs(Locale locale) throws GWTJahiaServiceException {
        try {
            List<JobDetail> l = scheduler.getAllActiveJobsDetails();
            return convertToGWTJobs(l, locale);
        } catch (JahiaException e) {
            throw new GWTJahiaServiceException("Error retrieving active jobs", e);
        }
    }

    public List<GWTJahiaJobDetail> getAllJobs(Locale locale) throws GWTJahiaServiceException {
        try {
            List<JobDetail> l = scheduler.getAllJobsDetails();
            return convertToGWTJobs(l, locale);
        } catch (JahiaException e) {
            throw new GWTJahiaServiceException();
        }

    }
}
