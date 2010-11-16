package org.jahia.services.content;

import org.jahia.api.Constants;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Publication job
 */
public class PublicationJob extends BackgroundJob {
    public static final String PUBLICATION_UUIDS = "publicationInfos";
    public static final String PUBLICATION_PROPERTIES = "publicationProperties";
    public static final String PUBLICATION_COMMENTS = "publicationComments";
    public static final String SOURCE = "source";
    public static final String DESTINATION = "destination";
    public static final String LOCK = "lock";

    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        List<String> uuids = (List<String>) jobDataMap.get(PUBLICATION_UUIDS);
        String source = (String) jobDataMap.get(SOURCE);
        String destination = (String) jobDataMap.get(DESTINATION);
        String lock = (String) jobDataMap.get(LOCK);
        List<String> comments = (List<String>) jobDataMap.get(PUBLICATION_COMMENTS);

        String label = "published_at_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(GregorianCalendar.getInstance().getTime());
        JCRPublicationService.getInstance().publish(uuids, source, destination, comments);

        if (lock != null) {
            JCRPublicationService.getInstance().unlockForPublication(uuids, source, lock);
        }

        JCRVersionService.getInstance().addVersionLabel(uuids, label, Constants.LIVE_WORKSPACE);
    }
}
