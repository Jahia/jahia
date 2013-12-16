package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
* Get workflow definition from name
*/
public class GetWorkflowsForDefinitionCommand extends BaseCommand<List<Workflow>> {
    private final String definition;
    private final Locale uiLocale;

    public GetWorkflowsForDefinitionCommand(String definition, Locale uiLocale) {
        this.definition = definition;
        this.uiLocale = uiLocale;
    }

    @Override
    public List<Workflow> execute() {
        final List<Workflow> workflows = new LinkedList<Workflow>();
        Collection<ProcessInstance> processInstances = getKieSession().getProcessInstances();
        for (ProcessInstance processInstance : processInstances) {
            if (processInstance instanceof WorkflowProcessInstance) {
                WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) processInstance;
                if (workflowProcessInstance.getProcessName().equals(definition)) {
                    workflows.add(convertToWorkflow(workflowProcessInstance, uiLocale, getKieSession(), getTaskService(), getLogService()));
                }
            }
        }
        return workflows;


    }
}
