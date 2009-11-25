package org.jahia.services.content.rules;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

public class RuleJob implements StatefulJob {
    private static transient Logger logger = Logger.getLogger(RuleJob.class);

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        final JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();
        NodeWrapper wrapper = null;
        List<Object> list = new ArrayList<Object>();
        try {
            final JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(map.getString(
                    "user"));
            new ProcessingContext(SettingsBean.getInstance(), System.currentTimeMillis(), null, user, null,
                                  ProcessingContext.EDIT);
            final JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession();
            wrapper = new NodeWrapper(jcrSessionWrapper.getNodeByUUID(map.getString("node")));
            list.add(new JobRuleExecution(map.getString("ruleToExecute"), wrapper));
            final RulesListener listener = RulesListener.getInstance(map.getString("workspace"));
            listener.executeRules(list, listener.getGlobals(map.getString("user"), new ArrayList<Updateable>()));
            jcrSessionWrapper.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }
}