package org.jahia.services.content.rules;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.ArrayList;
import java.util.List;

public class RuleJob extends BackgroundJob {
    private static transient Logger logger = Logger.getLogger(RuleJob.class);

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        final JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();
        AddedNodeFact wrapper = null;
        List<Object> list = new ArrayList<Object>();
        final JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession();
        wrapper = new AddedNodeFact(jcrSessionWrapper.getNodeByUUID(map.getString("node")));
        list.add(new JobRuleExecution(map.getString("ruleToExecute"), wrapper));
        final RulesListener listener = RulesListener.getInstance(map.getString("workspace"));
        listener.executeRules(list, listener.getGlobals(map.getString("user"), new ArrayList<Updateable>()));
        jcrSessionWrapper.save();
    }
}