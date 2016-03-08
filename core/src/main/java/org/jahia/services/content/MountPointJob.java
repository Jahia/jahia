package org.jahia.services.content;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobExecutionContext;

import java.util.Map;

/**
 * Simple job callback that will process mount point events
 */
public class MountPointJob extends BackgroundJob {
    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        MountPointListener mountPointListener = (MountPointListener) SpringContextSingleton.getBean("mountPointListener");
        Map<String, MountPointListener.MountPointEventValue> events = (Map<String, MountPointListener.MountPointEventValue>) jobExecutionContext.getJobDetail().getJobDataMap().get("changeLog");
        mountPointListener.processEvents(events);
    }
}
