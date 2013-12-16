package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.jbpm.BaseCommand;

/**
* Abort a process
*/
public class AbortProcessCommand extends BaseCommand<Workflow> {
    private final String processId;

    public AbortProcessCommand(String processId) {
        this.processId = processId;
    }

    @Override
    public Workflow execute() {
        getKieSession().abortProcessInstance(Long.parseLong(processId));
        return null;
    }
}
