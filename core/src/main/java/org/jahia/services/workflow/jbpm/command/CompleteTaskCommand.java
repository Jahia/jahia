package org.jahia.services.workflow.jbpm.command;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowObservationManager;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.jahia.services.workflow.jbpm.JBPM6WorkflowProvider;
import org.kie.api.task.model.Task;

import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;

/**
* Complete a task previously assigned
*/
public class CompleteTaskCommand extends BaseCommand<Object> {
    private JBPM6WorkflowProvider jbpm6WorkflowProvider;
    private final String taskId;
    private final String outcome;
    private final Map<String, Object> args;
    private final JahiaUser jahiaUser;
    private final WorkflowObservationManager observationManager;

    public CompleteTaskCommand(String taskId, String outcome, Map<String, Object> args, JahiaUser jahiaUser, WorkflowObservationManager observationManager) {
        this.taskId = taskId;
        this.outcome = outcome;
        this.args = args;
        this.jahiaUser = jahiaUser;
        this.observationManager = observationManager;
    }

    @Override
    public Object execute() {
        long id = Long.parseLong(taskId);
        Task task = getTaskService().getTaskById(id);
        Map<String, Object> taskInputParameters = getTaskInputParameters(task, getKieSession(), getTaskService());
        Map<String, Object> taskOutputParameters = getTaskOutputParameters(task, taskInputParameters, getKieSession(), getTaskService());
        final String uuid = (String) taskOutputParameters.get("task-" + taskId);
        if (uuid != null) {
            String workspace = (String) taskInputParameters.get("workspace");
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace, new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        if (!session.getNodeByUUID(uuid).hasProperty("state") ||
                                !session.getNodeByUUID(uuid).getProperty("state").getString().equals("finished")) {
                            session.getNodeByUUID(uuid).setProperty("finalOutcome", outcome);
                            session.getNodeByUUID(uuid).setProperty("state", "finished");
                            session.save();
                        }
                        return null;
                    }
                });
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }

        observationManager.notifyTaskEnded(getKey(), taskId);

        ClassLoader l = null;

        try {
            String module = getWorkflowService().getModuleForWorkflow(task.getTaskData().getProcessId());
            if (module != null) {
                l = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(module).getChainedClassLoader());
            }
            Map<String, Object> argsMap = args;
            if (argsMap == null) {
                argsMap = new HashMap<String, Object>();
            }
            argsMap.put("outcome", outcome);
            getTaskService().start(id, jahiaUser.getUserKey());
            getTaskService().complete(id, jahiaUser.getUserKey(), argsMap);
        } finally {
            if (l != null) {
                Thread.currentThread().setContextClassLoader(l);
            }
        }
        return null;
    }
}
