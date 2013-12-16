package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.workflow.HistoryWorkflow;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
* Get the process history
*/
public class GetHistoryWorkflowCommand extends BaseCommand<List<HistoryWorkflow>> {
    private final List<String> processIds;
    private final Locale uiLocale;

    public GetHistoryWorkflowCommand(List<String> processIds, Locale uiLocale) {
        this.processIds = processIds;
        this.uiLocale = uiLocale;
    }

    @Override
    public List<HistoryWorkflow> execute() {
        return getHistoryWorkflows(processIds, uiLocale);
    }

}
