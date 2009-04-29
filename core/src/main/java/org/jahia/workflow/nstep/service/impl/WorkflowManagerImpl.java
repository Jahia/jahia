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
 package org.jahia.workflow.nstep.service.impl;

import java.util.List;

import org.jahia.workflow.nstep.dao.WorkflowDAO;
import org.jahia.workflow.nstep.dao.WorkflowStepDAO;
import org.jahia.workflow.nstep.model.Workflow;
import org.jahia.workflow.nstep.model.WorkflowStep;
import org.jahia.workflow.nstep.service.WorkflowManager;

/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 10 d�c. 2004
 * Time: 10:43:46
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowManagerImpl implements WorkflowManager {
// ------------------------------ FIELDS ------------------------------

    private WorkflowDAO dao;
    private WorkflowStepDAO stepDAO;

// --------------------- GETTER / SETTER METHODS ---------------------

    public List<Workflow> getWorkflows() {
        return dao.getWorkflows();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setNstepWorkflowDAO(WorkflowDAO workflowDAO) {
        this.dao = workflowDAO;
    }

    public void setNstepWorkflowStepDAO(WorkflowStepDAO workflowStepDAO) {
        stepDAO = workflowStepDAO;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface WorkflowManager ---------------------


    public Workflow getWorkflowById(String workflowId) {
        return dao.getWorkflowById(new Long(workflowId));  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Workflow getWorkflowByName(String workflowName) {
        return dao.getWorkflowByName(workflowName);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeWorkflow(String workflowId) {
        dao.removeWorkflow(new Long(workflowId));
    }

    public void saveWorkflow(Workflow workflow) {
        List<WorkflowStep> list = workflow.getSteps();
        for (int i = 0; i < list.size(); i++) {
            WorkflowStep step = (WorkflowStep) list.get(i);
            stepDAO.saveWorkflowStep(step);
        }
        dao.saveWorkflow(workflow);
    }
}

