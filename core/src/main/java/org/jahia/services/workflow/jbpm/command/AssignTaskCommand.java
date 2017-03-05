/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
}
