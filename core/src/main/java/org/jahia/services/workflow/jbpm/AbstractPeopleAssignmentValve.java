package org.jahia.services.workflow.jbpm;

import org.jahia.pipelines.valves.Valve;

/**
 * An abstract valve implementation for PeopleAssignment valves to share common constants and other things.
 */
public abstract class AbstractPeopleAssignmentValve implements Valve {

    public static final String ENV_JBPM_WORKFLOW_PROVIDER = "jBPMWorkflowProvider";

}
