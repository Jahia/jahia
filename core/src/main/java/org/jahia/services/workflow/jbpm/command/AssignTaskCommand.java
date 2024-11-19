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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRUserNode;
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
        JCRUserNode actualUser = null;
        if (task.getTaskData().getActualOwner() != null) {
            actualUser = getUserManager().lookupUserByPath(task.getTaskData().getActualOwner().getId());
        }
        if (actualUser != null) {
            taskOutputParameters.put("currentUser", user.getUserKey());
            ((InternalTaskService) getTaskService()).addContent(id, taskOutputParameters);
            updateTaskNode(actualUser.getJahiaUser(), (String) taskOutputParameters.get("task-" + id));
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() +
                String.format("%n taskId: %s", taskId) +
                String.format("%n user: %s", user != null ? user.getName() : null);
    }
}
