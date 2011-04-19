package org.jahia.services.workflow;

import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.util.List;
import java.util.Map;

/**
 * Assign and complete task
 */
public class AssignAndCompleteTaskJob extends BackgroundJob {
    public static final String TASK_ID = "taskId";
    public static final String PROVIDER = "provider";
    public static final String OUTCOME = "outcome";
    public static final String MAP = "map";

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        String taskId = (String) jobDataMap.get(TASK_ID);
        String provider = (String) jobDataMap.get(PROVIDER);
        String outcome = (String) jobDataMap.get(OUTCOME);
        Map<String,Object> map = (Map<String, Object>) jobDataMap.get(MAP);

        WorkflowService.getInstance().assignAndCompleteTask(taskId, provider, outcome, map, JCRSessionFactory.getInstance().getCurrentUserSession().getUser());
    }
}
