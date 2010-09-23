package org.jahia.services.content;

import org.jahia.api.Constants;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import javax.jcr.RepositoryException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Publication job
 */
public class PublicationJob extends BackgroundJob {
    public static final String PUBLICATION_TYPE = "publication";
    public static final String PUBLICATION_INFOS = "publicationInfos";
    public static final String SOURCE = "source";
    public static final String DESTINATION = "destination";
    public static final String LOCK = "lock";

    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        List<PublicationInfo> info = (List<PublicationInfo>) jobDataMap.get(PUBLICATION_INFOS);
        String source = (String) jobDataMap.get(SOURCE);
        String destination = (String) jobDataMap.get(DESTINATION);
        String lock = (String) jobDataMap.get(LOCK);

        String label = "published_at_"+ new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(GregorianCalendar.getInstance().getTime());
        if (lock != null) {
            JCRPublicationService.getInstance().unlockForPublication(info, source, lock);
        }
        JCRPublicationService.getInstance().publish(info, source, destination);
        for (PublicationInfo publicationInfo : info) {
            label(publicationInfo, source, label);
        }
    }

    private void label(PublicationInfo publicationInfo, String source, String label) throws RepositoryException {
        JCRVersionService.getInstance().addVersionLabel(publicationInfo.getAllUuids(),label, Constants.LIVE_WORKSPACE);
        List<PublicationInfo> refs = publicationInfo.getAllReferences();
        for (PublicationInfo ref : refs) {
            label(ref, source, label);
        }
    }
}
