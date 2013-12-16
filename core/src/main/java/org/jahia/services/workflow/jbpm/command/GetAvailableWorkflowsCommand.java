package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.kie.api.KieBase;
import org.kie.api.definition.process.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
* Get available workflows definitions
*/
public class GetAvailableWorkflowsCommand extends BaseCommand<List<WorkflowDefinition>> {
    private final Locale uiLocale;

    public GetAvailableWorkflowsCommand(Locale uiLocale) {
        this.uiLocale = uiLocale;
    }

    @Override
    public List<WorkflowDefinition> execute() {
        KieBase kieBase = getKieSession().getKieBase();
        Collection<org.kie.api.definition.process.Process> processes = kieBase.getProcesses();
        List<WorkflowDefinition> workflowDefinitions = new ArrayList<WorkflowDefinition>();
        for (org.kie.api.definition.process.Process process : processes) {
            if (getWorkflowService().getWorkflowRegistration(process.getName()) != null) {
                workflowDefinitions.add(convertToWorkflowDefinition(process, uiLocale));
            }
        }
        return workflowDefinitions;
    }
}
