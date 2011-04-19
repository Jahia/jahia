package org.jahia.services.workflow;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.utils.LanguageCodeConverters;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.util.List;
import java.util.Map;

/**
 * Assign and complete task
 */
public class StartProcessJob extends BackgroundJob {
    public static final String NODE_IDS = "nodeIds";
    public static final String PROVIDER = "provider";
    public static final String PROCESS_KEY = "processKey";
    public static final String MAP = "map";
    public static final String COMMENTS = "comments";

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        List<String> nodesIds = (List<String>) jobDataMap.get(NODE_IDS);
        String provider = (String) jobDataMap.get(PROVIDER);
        String processKey = (String) jobDataMap.get(PROCESS_KEY);
        Map<String,Object> map = (Map<String, Object>) jobDataMap.get(MAP);
        List<String> comments = (List<String>) jobDataMap.get(COMMENTS);
        String locale = (String) jobDataMap.get(JOB_CURRENT_LOCALE);

        WorkflowService.getInstance().startProcess(nodesIds, JCRSessionFactory.getInstance().getCurrentUserSession(null, LanguageCodeConverters.languageCodeToLocale(locale)),
                processKey, provider, map, comments);
    }
}
