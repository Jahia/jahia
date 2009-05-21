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
