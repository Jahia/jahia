package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowTask;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.kie.api.runtime.KieSession;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.InternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
* Assign a task to a user
*/
public class AssignTaskCommand extends BaseCommand<List<WorkflowTask>> {
    private transient static Logger logger = LoggerFactory.getLogger(AssignTaskCommand.class);

    private final String taskId;
    private final JahiaUser user;

    public AssignTaskCommand(String taskId, JahiaUser user) {
        this.taskId = taskId;
        this.user = user;
    }

    @Override
    public List<WorkflowTask> execute() {
        KieSession ksession = getKieSession();
        long id = Long.parseLong(taskId);
        Task task = getTaskService().getTaskById(id);
        Map<String, Object> taskInputParameters = getTaskInputParameters(task, ksession, getTaskService());
        Map<String, Object> taskOutputParameters = getTaskOutputParameters(task, taskInputParameters, ksession, getTaskService());
        if (user == null) {
            getTaskService().release(task.getId(), JCRSessionFactory.getInstance().getCurrentUser().getUserKey());
        } else if (task.getTaskData().getActualOwner() != null && user.getUserKey().equals(task.getTaskData().getActualOwner().getId())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot assign task " + task.getId() + " to user " + user.getName() + ", user is already owner");
            }
        } else if (!checkParticipation(task, user)) {
            logger.error("Cannot assign task " + task.getId() + " to user " + user.getName() + ", user is not candidate");
        } else {
            getTaskService().claim(id, user.getUserKey());
        }
        JahiaUser actualUser = null;
        if (task.getTaskData().getActualOwner() != null) {
            actualUser = getUserManager().lookupUserByKey(task.getTaskData().getActualOwner().getId());
        }
        if (actualUser != null) {
            taskOutputParameters.put("currentUser", user.getUserKey());
            ((InternalTaskService) getTaskService()).addContent(id, taskOutputParameters);
        }
        updateTaskNode(actualUser, (String) taskOutputParameters.get("task-" + id));
        return null;
    }
}
