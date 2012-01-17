/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.tasks;

import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import static org.jahia.api.Constants.JAHIANT_TASK;
import static org.jahia.api.Constants.JAHIANT_TASKS;
import org.jahia.services.content.*;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.security.Principal;
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
                createTask(task, forUser, session);

                session.save();

                return true;
            }
        });
    }

    /**
     * Creates a task for the specified user.
     * 
     * @param task the task to be created
     * @param forUser the user name, who gets this task
     * @param session the current session
     * @throws RepositoryException in case of an error
     */
    private void createTask(final Task task, final String forUser, JCRSessionWrapper session)
            throws RepositoryException {
        JCRNodeWrapper tasksNode = getUserTasksNode(forUser, session);
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
            taskNode.setProperty("assignee", session.getNode(userManagerService.getUserSplittingRule().getPathForUsername(forUser)).getIdentifier());
        } catch (Exception e) {
            logger.warn("Unable to find user '" + forUser + "' to assign a task", e);
        }
    }

    /**
     * Creates a task for all users of the specified group. Note that the task
     * will be created for users, who are currently members of the group, it is
     * not applied e.g. if a new user will be added to the group.
     * 
     * @param task the task to be created
     * @param forGroup the group name, which members will get this task
     * @param siteId the site ID of the group
     * @throws RepositoryException in case of an error
     */
    public void createTaskForGroup(final Task task, String forGroup, final int siteId) throws RepositoryException {
        JahiaGroup group = groupManager.lookupGroup(siteId, forGroup);
        if (group == null) {
            logger.warn("Group with the name '" + forGroup + "' is not found in site with ID '" + siteId
                    + "'. Skipping creating tasks.");
            return;
        }
        final Set<Principal> members = group.getRecursiveUserMembers();
        if (logger.isDebugEnabled()) {
            if (members.isEmpty()) {
                logger.warn("Group with the name '" + forGroup + "' in site with ID '" + siteId
                        + "' has not members. Skipping creating tasks.");
            } else {
                logger.warn("Creating task for " + members.size() + " members of the group '" + forGroup
                        + "' in site with ID '" + siteId + "'.");
            }
        }
        if (!members.isEmpty()) {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    for (Principal principal : members) {
                        createTask(task, principal.getName(), session);
                    }

                    session.save();

                    return true;
                }
            });
        }
    }

    private JCRNodeWrapper getUserTasksNode(final String username, JCRSessionWrapper session)
            throws RepositoryException {
        JCRNodeWrapper tasksNode = null;
        String pathForUsername = userManagerService.getUserSplittingRule().getPathForUsername(username);
        try {
            tasksNode = session.getNode(getTasksPath(pathForUsername));
        } catch (PathNotFoundException ex) {
            // no tasks node found
        }
        if (tasksNode == null) {
            // create it
            JCRNodeWrapper userNode = session.getNode(pathForUsername);
            session.checkout(userNode);
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
