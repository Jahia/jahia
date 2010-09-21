package org.jahia.ajax.gwt.helper;

import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.utils.i18n.JahiaResourceBundle;
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

    public List<GWTJahiaJobDetail> getActiveJobs(Locale locale) throws GWTJahiaServiceException {
        List<GWTJahiaJobDetail> jobs = new ArrayList<GWTJahiaJobDetail>();
        try {
            List<JobDetail> l = scheduler.getAllActiveJobsDetails();
            for (JobDetail jobDetail : l) {
                final String type = (String) jobDetail.getJobDataMap().get(BackgroundJob.JOB_TYPE);
                final Date created = (Date) jobDetail.getJobDataMap().get(BackgroundJob.JOB_CREATED);
                GWTJahiaJobDetail job = new GWTJahiaJobDetail(jobDetail.getName(), type, created, jobDetail.getDescription(), jobDetail.getGroup(), jobDetail.getJobClass().getName());
                job.setLabel(JahiaResourceBundle.getJahiaInternalResource("label." + type + ".task", locale));
                jobs.add(job);

            }
        } catch (JahiaException e) {
            throw new GWTJahiaServiceException();
        }
        return jobs;
    }

    public List<GWTJahiaJobDetail> getAllJobs(Locale locale) throws GWTJahiaServiceException {
        List<GWTJahiaJobDetail> jobs = new ArrayList<GWTJahiaJobDetail>();
        try {
            List<JobDetail> l = scheduler.getAllJobsDetails();
            for (JobDetail jobDetail : l) {
                final String type = (String) jobDetail.getJobDataMap().get(BackgroundJob.JOB_TYPE);
                final Date created = (Date) jobDetail.getJobDataMap().get(BackgroundJob.JOB_CREATED);
                GWTJahiaJobDetail job = new GWTJahiaJobDetail(jobDetail.getName(), type, created, jobDetail.getDescription(), jobDetail.getGroup(), jobDetail.getJobClass().getName());
                job.setLabel(JahiaResourceBundle.getJahiaInternalResource("label." + type + ".task", locale));
                jobs.add(job);

            }
        } catch (JahiaException e) {
            throw new GWTJahiaServiceException();
        }
        return jobs;

    }
}
