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

import org.jahia.workflow.nstep.model.Workflow;

import java.util.List;


/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

public interface WorkflowDAO extends org.jahia.workflow.nstep.dao.DAO {
// -------------------------- OTHER METHODS --------------------------

    public Workflow getWorkflowById(Long workflowId);

    public Workflow getWorkflowByName(String workflowName);

    public List<Workflow> getWorkflows();

    public void removeWorkflow(Long workflowId);

    public void saveWorkflow(org.jahia.workflow.nstep.model.Workflow workflow);
}

