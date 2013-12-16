package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.jbpm.BaseCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
* Get processes based on list of ids
*/
public class GetActiveWorkflowsInformationsCommand extends BaseCommand<List<Workflow>> {
    private final List<String> processIds;
    private final Locale uiLocale;

    public GetActiveWorkflowsInformationsCommand(List<String> processIds, Locale uiLocale) {
        this.processIds = processIds;
        this.uiLocale = uiLocale;
    }

    @Override
    public List<Workflow> execute() {
        List<Workflow> activeWorkflows = new ArrayList<Workflow>();
        for (String s : processIds) {
            activeWorkflows.add(convertToWorkflow(getKieSession().getProcessInstance(Long.parseLong(s)), uiLocale, getKieSession(), getTaskService(), getLogService()));
        }
        return activeWorkflows;
    }
}
