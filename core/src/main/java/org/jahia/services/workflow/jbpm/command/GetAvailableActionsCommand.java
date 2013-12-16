package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.workflow.WorkflowAction;
import org.jahia.services.workflow.jbpm.BaseCommand;

import java.util.Locale;
import java.util.Set;

/**
* Get available actions for a given process
*/
public class GetAvailableActionsCommand extends BaseCommand<Set<WorkflowAction>> {
    private static final long serialVersionUID = 7885301164037826410L;
    private final String processId;
    private final Locale uiLocale;

    public GetAvailableActionsCommand(String processId, Locale uiLocale) {
        this.processId = processId;
        this.uiLocale = uiLocale;
    }

    @Override
    public Set<WorkflowAction> execute() {
        return getAvailableActions(getKieSession(), getTaskService(), processId, uiLocale);

    }
}
