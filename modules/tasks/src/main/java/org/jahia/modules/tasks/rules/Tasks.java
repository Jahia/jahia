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

package org.jahia.modules.tasks.rules;

import org.slf4j.Logger;
import org.drools.spi.KnowledgeHelper;
import org.jahia.bin.Jahia;
import org.jahia.services.tasks.Task;
import org.jahia.services.tasks.TaskService;

import javax.jcr.RepositoryException;
import java.util.Date;

/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 5 janv. 2010
 */
public class Tasks {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(Tasks.class);

    private static Tasks instance;
    private TaskService taskService;

    private Tasks() {
        super();
    }

    public static synchronized Tasks getInstance() {
        if (instance == null) {
            instance = new Tasks();
        }
        return instance;
    }

    public void createTask(String user, String title, String description, String priority, Date dueDate, String state,
            KnowledgeHelper drools) throws RepositoryException {
        Task task = new Task(title, description);
        if (priority != null) {
            task.setPriority(Task.Priority.valueOf(priority));
        }
        task.setDueDate(dueDate);
        if (state != null) {
            task.setState(Task.State.valueOf(state));
        }
        taskService.createTask(task, user);
    }

    public void createTask(String user, String title, String description, KnowledgeHelper drools)
            throws RepositoryException {
        createTask(user, title, description, null, null, null, drools);
    }

    public void createTaskForGroupMembers(String group, String title, String description, KnowledgeHelper drools)
    throws RepositoryException {
        Integer siteId = Jahia.getThreadParamBean() != null ? Jahia.getThreadParamBean().getSiteID() : null;
        if (siteId == null) {
            logger.warn("Current site cannot be detected. Skip adding new task for members of group '" + group + "'");
            return;
        }
        taskService.createTaskForGroup(new Task(title, description), group, siteId);
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }
}
