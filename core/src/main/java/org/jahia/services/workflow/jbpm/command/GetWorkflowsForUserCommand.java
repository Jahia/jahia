package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
* Get all workflows related to a user
*/
public class GetWorkflowsForUserCommand extends BaseCommand<List<Workflow>> {
    private final JahiaUser user;
    private final Locale uiLocale;

    public GetWorkflowsForUserCommand(JahiaUser user, Locale uiLocale) {
        this.user = user;
        this.uiLocale = uiLocale;
    }

    @Override
    public List<Workflow> execute() {
        final List<Workflow> workflows = new LinkedList<Workflow>();
        Collection<ProcessInstance> processInstances = getKieSession().getProcessInstances();
        for (ProcessInstance processInstance : processInstances) {
            if (processInstance instanceof WorkflowProcessInstance) {
                WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) processInstance;
                String userKey = (String) workflowProcessInstance.getVariable("user");
                if (user.getUserKey().equals(userKey)) {
                    workflows.add(convertToWorkflow(processInstance, uiLocale, getKieSession(), getTaskService(), getLogService()));
                }
            }
        }
        return workflows;
    }
}
