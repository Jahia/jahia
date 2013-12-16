package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.kie.api.runtime.process.ProcessInstance;

import java.util.Locale;

/**
* Get process from an id
*/
public class GetWorkflowCommand extends BaseCommand<Workflow> {
    private final String processId;
    private final Locale uiLocale;

    public GetWorkflowCommand(String processId, Locale uiLocale) {
        this.processId = processId;
        this.uiLocale = uiLocale;
    }

    @Override
    public Workflow execute() {
        ProcessInstance processInstance = getKieSession().getProcessInstance(Long.parseLong(processId));
        return convertToWorkflow(processInstance, uiLocale, getKieSession(), getTaskService(), getLogService());
    }
}
