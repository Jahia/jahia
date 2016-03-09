package org.jahia.services.render.filter.cache;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobExecutionContext;

import javax.jcr.RepositoryException;
import java.util.List;

/**
 * Simple job callback that will process cache flush based on JCR Events
 */
public class HtmlCacheEventJob extends BackgroundJob {
    @Override
    public void executeJahiaJob(final JobExecutionContext jobExecutionContext) throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                HtmlCacheEventListener htmlCacheEventListener = (HtmlCacheEventListener) SpringContextSingleton.getBean("htmlCacheEventListener");
                List<HtmlCacheEventListener.FlushEvent> events = (List<HtmlCacheEventListener.FlushEvent>) jobExecutionContext.getJobDetail().getJobDataMap().get("events");
                htmlCacheEventListener.processEvents(events, session, true);
                return null;
            }
        });
    }
}
