/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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

