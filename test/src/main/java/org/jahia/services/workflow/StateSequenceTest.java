/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jahia.services.workflow;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class StateSequenceTest extends JbpmTestCase {

  String deploymentId;

  protected void setUp() throws Exception {
    super.setUp();

    deploymentId = repositoryService.createDeployment()
        .addResourceFromClasspath("org/jahia/services/workflow/sequence_process.jpdl.xml")
        .deploy();
  }

  protected void tearDown() throws Exception {
    repositoryService.deleteDeploymentCascade(deploymentId);

    super.tearDown();
  }

  public void testWaitStatesSequence() {
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("StateSequence");
    Execution executionInA = processInstance.findActiveExecutionIn("a");
    assertNotNull(executionInA);

    processInstance = executionService.signalExecutionById(executionInA.getId());
    Execution executionInB = processInstance.findActiveExecutionIn("b");
    assertNotNull(executionInB);

    processInstance = executionService.signalExecutionById(executionInB.getId());
    Execution executionInC = processInstance.findActiveExecutionIn("c");
    assertNotNull(executionInC);
  }
}
