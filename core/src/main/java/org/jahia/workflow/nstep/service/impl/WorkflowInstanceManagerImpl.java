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

 package org.jahia.workflow.nstep.service.impl;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.workflow.nstep.dao.UserDAO;
import org.jahia.workflow.nstep.dao.WorkflowDAO;
import org.jahia.workflow.nstep.dao.WorkflowInstanceDAO;
import org.jahia.workflow.nstep.model.Workflow;
import org.jahia.workflow.nstep.model.WorkflowInstance;
import org.jahia.workflow.nstep.model.WorkflowStep;
import org.jahia.workflow.nstep.service.WorkflowInstanceManager;
import org.springframework.orm.ObjectRetrievalFailureException;

/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 10 d�c. 2004
 * Time: 12:30:52
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowInstanceManagerImpl implements WorkflowInstanceManager {
// ------------------------------ FIELDS ------------------------------

    private Log log = LogFactory.getLog(WorkflowInstanceManagerImpl.class);
    private UserDAO userDAO;
    private WorkflowDAO workflowDAO;
    private WorkflowInstanceDAO instanceDAO;

// --------------------- GETTER / SETTER METHODS ---------------------

    public List<WorkflowInstance> getWorkflowInstances() {
        return instanceDAO.getWorkflowInstances();
    }

    public void setNstepUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setNstepWorkflowDAO(org.jahia.workflow.nstep.dao.WorkflowDAO workflowDAO) {
        this.workflowDAO = workflowDAO;
    }

    public void setNstepWorkflowInstanceDAO(org.jahia.workflow.nstep.dao.WorkflowInstanceDAO instanceDAO) {
        this.instanceDAO = instanceDAO;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface WorkflowInstanceManager ---------------------


    public void abortWorkflow(String objectKey, String languageCode) {
        try {
            WorkflowInstance instance = instanceDAO.getWorkflowInstanceByObjectKey(objectKey, languageCode);
            instanceDAO.removeWorkflowInstance(instance.getId());
        } catch(ObjectRetrievalFailureException e) {
            log.debug("Try to abort a non existing workflow "+objectKey+" language "+languageCode,e);
        }
    }

    public WorkflowInstance getWorkflowInstanceById(String workflowInstanceId) {
        return instanceDAO.getWorkflowInstanceById(new Long(workflowInstanceId));
    }

    public WorkflowInstance getWorkflowInstanceByObjectKey(String objectKey,
                                                                                          String languageCode) {
        return instanceDAO.getWorkflowInstanceByObjectKey(objectKey, languageCode);
    }

    public List<WorkflowInstance> getWorkflowInstancesByUser(String login) {
        return instanceDAO.getWorkflowInstancesByUser(login);
    }

    public void initWorkflow(String workflowId, String login, String objectKey, String languageCode, String email) {
        Workflow workflow = workflowDAO.getWorkflowById(new Long(workflowId));
        org.jahia.workflow.nstep.model.User user = getUser(login);
        WorkflowInstance instance = new org.jahia.workflow.nstep.model.WorkflowInstance();
        instance.setObjectKey(objectKey);
        instance.setLanguageCode(languageCode);
        instance.setAuthorEmail(email);
        instance.setUser(user);
        instance.setWorkflow(workflow);
        instance.setStep((org.jahia.workflow.nstep.model.WorkflowStep) workflow.getSteps().get(0));
        instance.setStartDate(Calendar.getInstance().getTime());
        instanceDAO.saveWorkflowInstance(instance);
    }

    public boolean rollbackWorkflow(String login, String objectKey, String languageCode) {
        WorkflowInstance instance = instanceDAO.getWorkflowInstanceByObjectKey(
                objectKey, languageCode);
        final List<WorkflowStep> steps = instance.getWorkflow().getSteps();
        int indexCurrentStep = steps.indexOf(instance.getStep()) - 1;
        int index = (indexCurrentStep < 0 ? 0 : indexCurrentStep);
        if (log.isDebugEnabled()) {
            log.debug("Roolback workflow to step " + index);
        }
        instance.setStep((org.jahia.workflow.nstep.model.WorkflowStep) steps.get(index));
        instance.setUser(getUser(login));
        instance.setStartDate(Calendar.getInstance().getTime());
        instanceDAO.saveWorkflowInstance(instance);
        return index == 0;
    }

    /**
     * This method advance the workflow one step forward if it's possible else the workflow is finish.
     *
     * @param login
     * @param objectKey
     * @param languageCode
     *
     * @return false if workflow is not finished
     */
    public boolean stepForwardWorkflow(String login, String objectKey, String languageCode) {
        WorkflowInstance instance = instanceDAO.getWorkflowInstanceByObjectKey(
                objectKey, languageCode);
        final List<WorkflowStep> steps = instance.getWorkflow().getSteps();
        int indexCurrentStep = steps.indexOf(instance.getStep());
        instance.setStep((org.jahia.workflow.nstep.model.WorkflowStep) steps.get(indexCurrentStep + 1));
        instance.setUser(getUser(login));
        instance.setStartDate(Calendar.getInstance().getTime());
        instanceDAO.saveWorkflowInstance(instance);
        return (indexCurrentStep + 1) == (steps.size() - 1);
    }

// -------------------------- OTHER METHODS --------------------------

    private org.jahia.workflow.nstep.model.User getUser(String login) {
        org.jahia.workflow.nstep.model.User user;
        try {
            user = userDAO.getUserByLogin(login);
        } catch (ObjectRetrievalFailureException e) {
            user = new org.jahia.workflow.nstep.model.User();
            user.setLogin(login);
            userDAO.saveUser(user);
        }
        return user;
    }
}

