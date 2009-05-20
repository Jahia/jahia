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
 package org.jahia.workflow.nstep.dao;

import org.jahia.workflow.nstep.model.WorkflowInstance;

import java.util.List;


/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

public interface WorkflowInstanceDAO extends org.jahia.workflow.nstep.dao.DAO {
// -------------------------- OTHER METHODS --------------------------

    public List<WorkflowInstance> getWorkflowInstances();

    public void removeWorkflowInstance(Long workflowInstanceId);

    public void removeWorkflowInstance(String objectKey);

    public void saveWorkflowInstance(WorkflowInstance workflowInstance);

    WorkflowInstance getWorkflowInstanceById(Long workflowInstanceId);

    WorkflowInstance getWorkflowInstanceByObjectKey(String objectKey,
                                                                                   String languageCode);

    List<WorkflowInstance> getWorkflowInstancesByUser(String login);
}

