/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.workflow.jbpm.command;

import com.google.common.base.Joiner;
import org.apache.jackrabbit.util.ISO9075;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowObservationManager;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.jahia.services.workflow.jbpm.JBPM6WorkflowProvider;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.task.model.Task;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;

/**
* Complete a task previously assigned
*/
public class CompleteTaskCommand extends BaseCommand<Object> {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(CompleteTaskCommand.class);

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

        NodeInstance taskNodeInstance = getTaskNodeInstance(task, ksession);
        String processId = taskNodeInstance.getProcessInstance().getProcessId();
        Map<String, String> permissions = workflowService.getWorkflowRegistration(ISO9075.decode(processId)).getPermissions();
        String permission = permissions.get(taskNodeInstance.getNodeName() + "." + outcome);
        if (permission != null) {
            String nodeId = (String) taskNodeInstance.getProcessInstance().getVariable("nodeId");
            try {
                if (nodeId != null && !JCRTemplate.getInstance().doExecute(jahiaUser, null, null,
                        session -> session.getNodeByIdentifier(nodeId).hasPermission(permission)
                )) {
                    logger.error("User does not have permission to complete {} with {}", taskNodeInstance.getNodeName(), outcome);
                    return null;
                }
            } catch (RepositoryException e) {
                logger.warn("Cannot read node {}", nodeId, e);
            }
        }

        Map<String, Object> taskInputParameters = getTaskInputParameters(task, getKieSession(), getTaskService());
        Map<String, Object> taskOutputParameters = getTaskOutputParameters(task, taskInputParameters, getKieSession(), getTaskService());
        final String uuid = (String) taskOutputParameters.get("task-" + taskId);
        if (uuid != null) {
            String workspace = (String) taskInputParameters.get("workspace");
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(jahiaUser, workspace, null, new JCRCallback<Object>() {
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
                logger.error(e.getMessage(), e);
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

    @Override
    public String toString() {
        return super.toString() +
                String.format("%n taskId: %s", taskId) +
                String.format("%n outcome: %s", outcome) +
                String.format("%n jahiaUser: %s", jahiaUser != null ? jahiaUser.getName() : null) +
                String.format("%n args: %s", Joiner.on(",").withKeyValueSeparator("=").join(args));
    }
}
