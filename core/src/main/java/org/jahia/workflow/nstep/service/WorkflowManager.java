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

import org.jahia.workflow.nstep.dao.WorkflowStepDAO;
import org.jahia.workflow.nstep.model.Workflow;

import java.util.List;

/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 10 d�c. 2004
 * Time: 10:19:12
 * To change this template use File | Settings | File Templates.
 */
public interface WorkflowManager {
// -------------------------- OTHER METHODS --------------------------

    Workflow getWorkflowById(String workflowId);

    Workflow getWorkflowByName(String workflowName);

    List<Workflow> getWorkflows();

    void removeWorkflow(String workflowId);

    void saveWorkflow(Workflow workflow);

    void setNstepWorkflowDAO(org.jahia.workflow.nstep.dao.WorkflowDAO workflowDAO);

    void setNstepWorkflowStepDAO(WorkflowStepDAO workflowStepDAO);
}

