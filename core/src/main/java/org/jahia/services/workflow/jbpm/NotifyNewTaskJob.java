package org.jahia.services.workflow.jbpm;

import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.workflow.WorkflowService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

/**
 * Background Job to notify a new task after creation
 */
public class NotifyNewTaskJob extends BackgroundJob {

    public static final String TASK_ID = "taskId";

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        WorkflowService.getInstance().getObservationManager().notifyNewTask("jBPM",jobDataMap.getString(TASK_ID));
    }
}
