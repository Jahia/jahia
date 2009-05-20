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
 package org.jahia.workflow.nstep.dao.hibernate;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.jahia.workflow.nstep.model.Workflow;
import org.jahia.workflow.nstep.model.WorkflowStep;
import org.jahia.workflow.nstep.model.WorkflowInstance;
import org.jahia.workflow.nstep.dao.WorkflowDAO;
import org.jahia.workflow.nstep.dao.WorkflowInstanceDAO;
import org.jahia.workflow.nstep.dao.WorkflowStepDAO;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
/**
 * Created by Rincevent.
 * Date: 14 oct. 2005 - 13:11:44
 * @author Rincevent
 * @version $Id$
 *
 */

/**
 * $Log $
 */
public class WorkflowInstanceDAOTest extends AbstractTransactionalDataSourceSpringContextTests {

    protected String[] getConfigLocations() {
        return new String[] {"spring/applicationContext-hibernate.xml","spring/applicationContext-dao.xml"};
    }

    public void testDeleteOnCascade() throws Exception {
        ConfigurableApplicationContext context = getContext(getConfigLocations());
        Workflow workflow = new Workflow();
        workflow.setName("Test Cascade");
        WorkflowDAO workflowDAO = (WorkflowDAO) context.getBean("nstepWorkflowDAO");
        workflowDAO.saveWorkflow(workflow);
        WorkflowStepDAO workflowStepDAODAO = (WorkflowStepDAO) context.getBean("nstepWorkflowStepDAO");
        List steps = new ArrayList(2);
        for (int i=0; i<2;i++) {
            WorkflowStep step = new WorkflowStep();
            step.setName("Test Cascade Step "+i);
            workflowStepDAODAO.saveWorkflowStep(step);
            steps.add(step);
        }
        workflow.setSteps(steps);
        WorkflowInstance workflowInstance = new WorkflowInstance();
        WorkflowStep step = (WorkflowStep) steps.get(0);
        workflowInstance.setStep(step);
        workflowInstance.setObjectKey("Test Cascade Instance");
        workflowInstance.setLanguageCode("fr");
        workflowInstance.setWorkflow(workflow);
        WorkflowInstanceDAO instanceDAO = (WorkflowInstanceDAO) context.getBean("nstepWorkflowInstanceDAO");
        instanceDAO.saveWorkflowInstance(workflowInstance);
        Set instanceList = new HashSet();
        instanceList.add(workflowInstance);
        step.setWorkflowInstances(instanceList);
        workflowDAO.saveWorkflow(workflow);
        assertNotNull(workflowInstance.getId());
        Long workflowInstanceId = new Long(workflowInstance.getId().longValue());
        // Ensure that when deleting workflow we delete all instances and steps
        workflowDAO.removeWorkflow(workflow.getId());
        try {
            instanceDAO.getWorkflowInstanceById(workflowInstanceId);
            fail("We should not have found this instance");
        } catch(ObjectRetrievalFailureException e) {
            assertTrue(e.getIdentifier().equals(workflowInstance.getId()));
        }
    }
}
