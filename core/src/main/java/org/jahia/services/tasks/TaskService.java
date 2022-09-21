/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.tasks;

import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;

import static org.jahia.api.Constants.JAHIANT_TASK;
import static org.jahia.api.Constants.JAHIANT_TASKS;

import org.jahia.services.content.*;
import org.jahia.services.usermanager.JahiaGroupManagerService;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.Set;

/**
 * Task management service.
 *
 * @author Sergiy Shyrkov
 */
public class TaskService {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TaskService.class);

    private static String getTasksPath(String userPath) {
        if (userPath == null || userPath.length() == 0) {
            throw new IllegalArgumentException("The user name cannot be null or empty.");
        }
        return userPath + "/tasks";
    }

    private JahiaGroupManagerService groupManager;

    private JahiaUserManagerService userManagerService;

    /**
     * Creates a task for the specified user.
     *
     * @param task the task to be created
     * @param forUser the user name, who gets this task
     * @throws RepositoryException in case of an error
     */
    public void createTask(final Task task, final String forUser) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSessionInSameWorkspaceAndLocale(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRUserNode userNode = userManagerService.lookupUser(forUser, session);
                if (userNode == null) {
                    return false;
                }
                createTask(task, userNode, session);

                session.save();

                return true;
            }
        });
    }

    /**
     * Creates a task for the specified user.
     *
     * @param task the task to be created
     * @param userNode the user node, who gets this task
     * @throws RepositoryException in case of an error
     */
    public void createTask(final Task task, final JCRUserNode userNode) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSessionInSameWorkspaceAndLocale(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                createTask(task, userNode, session);

                session.save();

                return true;
            }
        });
    }

    /**
     * Creates a task for the specified user.
     *
     * @param task the task to be created
     * @param userNode the user node, who gets this task
     * @param session the current session
     * @throws RepositoryException in case of an error
     */
    private void createTask(final Task task, final JCRUserNode userNode, JCRSessionWrapper session)
            throws RepositoryException {
        JCRNodeWrapper tasksNode = getUserTasksNode(userNode);
        session.checkout(tasksNode);
        JCRNodeWrapper taskNode = tasksNode.addNode(JCRContentUtils.findAvailableNodeName(tasksNode, "task"),
                JAHIANT_TASK);
        if (task.getTitle() != null) {
            taskNode.setProperty("jcr:title", task.getTitle());
        }
        if (task.getDescription() != null) {
            taskNode.setProperty("description", task.getDescription());
        }
        taskNode.setProperty("priority", task.getPriority().toString().toLowerCase());
        if (task.getDueDate() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(task.getDueDate());
            taskNode.setProperty("dueDate", calendar);
        }
        taskNode.setProperty("state", task.getState().toString().toLowerCase());
        try {
            taskNode.setProperty("assignee", userNode);
            // update assigneeUserKey. assignee is not used for defining the owner of the task
            taskNode.setProperty("assigneeUserKey", userNode.getPath());
        } catch (Exception e) {
            logger.warn("Unable to find user '" + userNode.getPath() + "' to assign a task", e);
        }
    }

    /**
     * Creates a task for all users of the specified group. Note that the task
     * will be created for users, who are currently members of the group, it is
     * not applied e.g. if a new user will be added to the group.
     *
     * @param task the task to be created
     * @param forGroup the group name, which members will get this task
     * @param siteKey the site key of the group
     * @throws RepositoryException in case of an error
     */
    public void createTaskForGroup(final Task task, String forGroup, final String siteKey) throws RepositoryException {
        JCRGroupNode group = groupManager.lookupGroup(siteKey, forGroup);
        if (group == null) {
            logger.warn("Group with the name '" + forGroup + "' is not found in site with ID '" + siteKey
                    + "'. Skipping creating tasks.");
            return;
        }
        final Set<JCRUserNode> members = group.getRecursiveUserMembers();
        if (logger.isDebugEnabled()) {
            if (members.isEmpty()) {
                logger.warn("Group with the name '" + forGroup + "' in site with ID '" + siteKey
                        + "' has not members. Skipping creating tasks.");
            } else {
                logger.warn("Creating task for " + members.size() + " members of the group '" + forGroup
                        + "' in site with ID '" + siteKey + "'.");
            }
        }
        if (!members.isEmpty()) {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    for (JCRUserNode principal : members) {
                        createTask(task, principal, session);
                    }

                    session.save();

                    return true;
                }
            });
        }
    }

    public void createTaskForGroup(final Task task, JCRGroupNode group) throws RepositoryException {
        if (group == null) {
            return;
        }
        final Set<JCRUserNode> members = group.getRecursiveUserMembers();
        if (logger.isDebugEnabled()) {
            if (members.isEmpty()) {
                logger.warn("Group '" + group.getPath() + "' has no members. Skipping creating tasks.");
            } else {
                logger.warn("Creating task for " + members.size() + " members of the group '" + group.getPath() + "'.");
            }
        }
        if (!members.isEmpty()) {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    for (JCRUserNode principal : members) {
                        createTask(task, principal, session);
                    }

                    session.save();

                    return true;
                }
            });
        }
    }

    private JCRNodeWrapper getUserTasksNode(final JCRUserNode userNode)
            throws RepositoryException {
        JCRNodeWrapper tasksNode = null;
        if (userNode.hasNode("tasks")) {
            tasksNode = userNode.getNode("tasks");
        } else {
            tasksNode = userNode.addNode("tasks", JAHIANT_TASKS);
        }

        return tasksNode;
    }

    public void setGroupManager(JahiaGroupManagerService groupManager) {
        this.groupManager = groupManager;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }
}
