package org.jahia.services.workflow.jbpm.custom;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Lock custom activity for jBPM workflow
 *
 * Lock the current node
 *
 */
public class Unlock implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;

    public void execute(ActivityExecution execution) throws Exception {
        String id = (String) execution.getVariable("nodeId");
        String workspace = (String) execution.getVariable("workspace");
        Locale locale = (Locale) execution.getVariable("locale");
        JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(id);
        JCRPublicationService.getInstance().unlockForPublication(node.getPath(), workspace, Collections.singleton(locale.toString()), true, false);
        execution.takeDefaultTransition();
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    }

}