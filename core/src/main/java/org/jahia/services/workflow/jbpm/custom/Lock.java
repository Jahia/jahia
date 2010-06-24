package org.jahia.services.workflow.jbpm.custom;

import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.PublicationInfo;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import java.util.*;

/**
 * Lock custom activity for jBPM workflow
 *
 * Lock the current node
 *
 */
public class Lock implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;

    public void execute(ActivityExecution execution) throws Exception {
        List<PublicationInfo> info = (List<PublicationInfo>) execution.getVariable("publicationInfos");
        String workspace = (String) execution.getVariable("workspace");
        JCRPublicationService.getInstance().lockForPublication(info, workspace, "process-"+execution.getProcessInstance().getId());
        execution.takeDefaultTransition();
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    }

}
