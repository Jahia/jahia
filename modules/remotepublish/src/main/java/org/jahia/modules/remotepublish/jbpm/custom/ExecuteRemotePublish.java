package org.jahia.modules.remotepublish.jbpm.custom;

import org.apache.log4j.Logger;
import org.jahia.modules.remotepublish.RemotePublicationService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.workflow.WorkflowVariable;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Publish custom activity for jBPM workflow
 * <p/>
 * Publish the current node
 */
public class ExecuteRemotePublish implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    private static transient Logger logger = Logger.getLogger(ExecuteRemotePublish.class);

    public void execute(ActivityExecution execution) throws Exception {
        List<WorkflowVariable> wfVar = (List<WorkflowVariable>) execution.getVariable("remotePublicationToExecute");
        if (wfVar != null) {
            String id = wfVar.get(0).getValue();
            String workspace = (String) execution.getVariable("workspace");
            Locale locale = (Locale) execution.getVariable("locale");

            JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(id);
            RemotePublicationService.getInstance().executeRemotePublication(node);
            execution.takeDefaultTransition();
        }
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    }

}