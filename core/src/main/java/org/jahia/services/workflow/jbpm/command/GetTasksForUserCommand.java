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
