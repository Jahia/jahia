/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

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
