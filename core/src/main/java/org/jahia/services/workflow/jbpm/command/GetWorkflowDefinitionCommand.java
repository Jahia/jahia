package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.kie.api.definition.process.*;

import java.util.Collection;
import java.util.Locale;

/**
* Get workflow definition
*/
public class GetWorkflowDefinitionCommand extends BaseCommand<WorkflowDefinition> {
    private final String key;
    private final Locale uiLocale;

    public GetWorkflowDefinitionCommand(String key, Locale uiLocale) {
        this.key = key;
        this.uiLocale = uiLocale;
    }

    @Override
    public WorkflowDefinition execute() {
        Collection<org.kie.api.definition.process.Process> processes = getKieSession().getKieBase().getProcesses();
        for (org.kie.api.definition.process.Process process : processes) {
            if (process.getName().equals(key)) {
                return convertToWorkflowDefinition(process, uiLocale);
            }
        }
        return null;
    }
}
