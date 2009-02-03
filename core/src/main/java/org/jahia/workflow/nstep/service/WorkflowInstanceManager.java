/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

 package org.jahia.workflow.nstep.service;

import java.util.List;

import org.jahia.workflow.nstep.dao.UserDAO;
import org.jahia.workflow.nstep.dao.WorkflowDAO;
import org.jahia.workflow.nstep.dao.WorkflowInstanceDAO;
import org.jahia.workflow.nstep.model.WorkflowInstance;

/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 10 d�c. 2004
 * Time: 11:29:24
 * To change this template use File | Settings | File Templates.
 */
public interface WorkflowInstanceManager {
// -------------------------- OTHER METHODS --------------------------

    void abortWorkflow(String objectKey, String languageCode);

    WorkflowInstance getWorkflowInstanceById(String workflowInstanceId);

    WorkflowInstance getWorkflowInstanceByObjectKey(String objectKey, String languageCode);

    /**
     * Getter for property 'workflowInstances'.
     *
     * @return Value for property 'workflowInstances'.
     */
    List<WorkflowInstance> getWorkflowInstances();

    List<WorkflowInstance> getWorkflowInstancesByUser(String login);

    void initWorkflow(String workflowId, String login, String objectKey, String languageCode, String email);

    boolean rollbackWorkflow(String login, String objectKey, String languageCode);

    /**
     * Setter for property 'nstepUserDAO'.
     *
     * @param userDAO Value to set for property 'nstepUserDAO'.
     */
    void setNstepUserDAO(UserDAO userDAO);

    /**
     * Setter for property 'nstepWorkflowDAO'.
     *
     * @param workflowDAO Value to set for property 'nstepWorkflowDAO'.
     */
    void setNstepWorkflowDAO(WorkflowDAO workflowDAO);

    /**
     * Setter for property 'nstepWorkflowInstanceDAO'.
     *
     * @param instanceDAO Value to set for property 'nstepWorkflowInstanceDAO'.
     */
    void setNstepWorkflowInstanceDAO(WorkflowInstanceDAO instanceDAO);

    boolean stepForwardWorkflow(String login, String objectKey, String languageCode);
}

