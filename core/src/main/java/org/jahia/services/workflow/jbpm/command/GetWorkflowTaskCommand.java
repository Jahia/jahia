package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.workflow.WorkflowTask;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.kie.api.task.model.Task;

import java.util.Locale;

/**
* Get workflow task
*/
public class GetWorkflowTaskCommand extends BaseCommand<WorkflowTask> {
    private final String taskId;
    private final Locale uiLocale;

    public GetWorkflowTaskCommand(String taskId, Locale uiLocale) {
        this.taskId = taskId;
        this.uiLocale = uiLocale;
    }

    @Override
    public WorkflowTask execute() {
        Task task = getTaskService().getTaskById(Long.parseLong(taskId));
        return convertToWorkflowTask(task, uiLocale, getKieSession(), getTaskService());
    }
}
