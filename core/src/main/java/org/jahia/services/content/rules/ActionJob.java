package org.jahia.services.content.rules;

import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.jcr.RepositoryException;

public class ActionJob extends BackgroundJob {
    private static transient Logger logger = Logger.getLogger(ActionJob.class);

    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            final JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();
            final JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
            final JCRSessionWrapper jcrSessionWrapper = sessionFactory.getCurrentUserSession();
            JCRNodeWrapper node = jcrSessionWrapper.getNodeByUUID(map.getString("node"));
            final BackgroundAction action = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getBackgroundActions().get(
                    map.getString("actionToExecute"));
            if (action != null) {
                BackgroundAction backgroundAction = (BackgroundAction) action;
                backgroundAction.executeBackgroundAction(node);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}