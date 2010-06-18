package org.jahia.services.workflow.jbpm.custom;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.WorkflowVariable;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import java.util.*;

/**
 * Publish custom activity for jBPM workflow
 * <p/>
 * Publish the current node
 */
public class Publish implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    private transient static Logger logger = Logger.getLogger(Publish.class);

    public void execute(ActivityExecution execution) throws Exception {
        final String id = (String) execution.getVariable("nodeId");
        String workspace = (String) execution.getVariable("workspace");
        Locale locale = (Locale) execution.getVariable("locale");
        String username = (String) execution.getVariable("user");
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        final JahiaUserManagerService userMgr = ServicesRegistry.getInstance().getJahiaUserManagerService();
        JahiaUser user = userMgr.lookupUser(username);
        JahiaUser currentUser = sessionFactory.getCurrentUser();
        sessionFactory.setCurrentUser(user);
        JCRNodeWrapper node = sessionFactory.getCurrentUserSession().getNodeByUUID(id);
        logger.info("Launching publication of node " + node.getPath() + " at " + (new Date()).toString());
        JCRPublicationService.getInstance().publish(node.getPath(), workspace, Constants.LIVE_WORKSPACE,
                                                    Collections.singleton(locale.toString()), false);
        sessionFactory.setCurrentUser(currentUser);
        List<WorkflowVariable> workflowVariables = (List<WorkflowVariable>) execution.getVariable("endDate");
        if (workflowVariables.isEmpty()) {
            execution.take("to end");
        } else {
            execution.take("timeBasedUnpublish");
        }
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    }

}
