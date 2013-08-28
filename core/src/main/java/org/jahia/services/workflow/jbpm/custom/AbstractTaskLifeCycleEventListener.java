package org.jahia.services.workflow.jbpm.custom;

import org.jahia.services.workflow.WorkflowObservationManager;
import org.jahia.services.workflow.jbpm.JBPM6WorkflowProvider;
import org.jbpm.services.task.lifecycle.listeners.TaskLifeCycleEventListener;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.jbpm.shared.services.impl.events.JbpmServicesEventListener;
import org.kie.api.runtime.Environment;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Task;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstract task assignment listener so that we can register new listeners from modules.
 */
public abstract class AbstractTaskLifeCycleEventListener extends JbpmServicesEventListener<Task> implements TaskLifeCycleEventListener, InitializingBean, DisposableBean {

    protected TaskService taskService;
    protected Environment environment;
    protected WorkflowObservationManager observationManager;
    protected JBPM6WorkflowProvider workflowProvider;
    protected String name;

    public void setName(String name) {
        this.name = name;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public void setObservationManager(WorkflowObservationManager observationManager) {
        this.observationManager = observationManager;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setWorkflowProvider(JBPM6WorkflowProvider workflowProvider) {
        this.workflowProvider = workflowProvider;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        workflowProvider.registerTaskLifeCycleEventListener(name, this);
    }

    @Override
    public void destroy() throws Exception {
        workflowProvider.unregisterTaskLifeCycleEventListener(name);
    }

    public Map<String, Object> getTaskInputParameters(Task task) {
        Content taskContent = taskService.getContentById(task.getTaskData().getDocumentContentId());
        Object contentData = ContentMarshallerHelper.unmarshall(taskContent.getContent(), environment);
        Map<String, Object> taskInputParameters = null;
        if (contentData instanceof Map) {
            taskInputParameters = (Map<String, Object>) contentData;
        }
        return taskInputParameters;
    }

    public Map<String, Object> getTaskOutputParameters(Task task, Map<String, Object> taskInputParameters) {
        Map<String, Object> taskOutputParameters = null;
        if (taskInputParameters != null) {
            Content taskOutputContent = taskService.getContentById(task.getTaskData().getOutputContentId());
            if (taskOutputContent == null) {
                taskOutputParameters = new LinkedHashMap<String, Object>(taskInputParameters);
            } else {
                Object outputContentData = ContentMarshallerHelper.unmarshall(taskOutputContent.getContent(), environment);
                if (outputContentData instanceof Map) {
                    taskOutputParameters = (Map<String, Object>) outputContentData;
                }
            }
        }
        return taskOutputParameters;
    }

}
