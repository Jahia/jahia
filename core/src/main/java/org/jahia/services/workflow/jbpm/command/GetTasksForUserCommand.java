package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowTask;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.jahia.services.workflow.jbpm.JBPM6WorkflowProvider;
import org.kie.api.task.model.TaskSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
* Get tasks for a user
*/
public class GetTasksForUserCommand extends BaseCommand<List<WorkflowTask>> {
    private final JahiaUser user;
    private final Locale uiLocale;

    public GetTasksForUserCommand(JahiaUser user, Locale uiLocale) {
        this.user = user;
        this.uiLocale = uiLocale;
    }

    @Override
    public List<WorkflowTask> execute() {
        final List<TaskSummary> tasksOwned = getTaskService().getTasksOwnedByStatus(user.getUserKey(), JBPM6WorkflowProvider.RESERVED_STATUS_LIST, "en");
        final List<TaskSummary> potentialOwnerTasks = getTaskService().getTasksAssignedAsPotentialOwnerByStatus(user.getUserKey(), JBPM6WorkflowProvider.OPEN_STATUS_LIST_NON_RESERVED, "en");
        final List<TaskSummary> businessAdministratorTasks = getTaskService().getTasksAssignedAsBusinessAdministrator(user.getUserKey(), "en");

        List<WorkflowTask> availableTasks = new ArrayList<WorkflowTask>();
        if (tasksOwned != null && tasksOwned.size() > 0) {
            availableTasks.addAll(convertToWorkflowTasks(uiLocale, tasksOwned, getKieSession(), getTaskService()));
        }
        // how do we retrieve group tasks ?
        if (potentialOwnerTasks != null && potentialOwnerTasks.size() > 0) {
            availableTasks.addAll(convertToWorkflowTasks(uiLocale, potentialOwnerTasks, getKieSession(), getTaskService()));
        }
        if (businessAdministratorTasks != null && businessAdministratorTasks.size() > 0) {
            availableTasks.addAll(convertToWorkflowTasks(uiLocale, businessAdministratorTasks, getKieSession(), getTaskService()));
        }
        return availableTasks;
    }
}
