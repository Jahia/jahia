package org.jahia.services.workflow.jbpm.custom;

import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import java.util.Map;

/**
 * Lock custom activity for jBPM workflow
 *
 * Lock the current node
 *
 */
public class Lock implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;

    public void execute(ActivityExecution execution) throws Exception {
        // todo lock .. ?
        execution.takeDefaultTransition();
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
        System.out.println("-- signal lock : " + signalName);
    }

}
