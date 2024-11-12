/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

    @Override
    public String toString() {
        return super.toString() +
                String.format("%n uiLocale: %s", uiLocale) +
                String.format("%n user: %s", user != null ? user.getName() : null);
    }
}
