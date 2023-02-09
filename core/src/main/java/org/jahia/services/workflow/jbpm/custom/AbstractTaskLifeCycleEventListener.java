/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.workflow.jbpm.custom;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
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
        if (JahiaContextLoaderListener.isRunning()) {
            workflowProvider.unregisterTaskLifeCycleEventListener(name);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getTaskInputParameters(Task task) {
        Content taskContent = taskService.getContentById(task.getTaskData().getDocumentContentId());
        Object contentData = ContentMarshallerHelper.unmarshall(taskContent.getContent(), environment);
        Map<String, Object> taskInputParameters = null;
        if (contentData instanceof Map) {
            taskInputParameters = (Map<String, Object>) contentData;
        }
        return taskInputParameters;
    }

    @SuppressWarnings("unchecked")
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
