/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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

