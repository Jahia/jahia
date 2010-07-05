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
package org.jbpm.jpdl.internal.model;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.task.SwimlaneDefinitionImpl;
import org.jbpm.pvm.internal.task.TaskDefinitionImpl;

/**
 * @author Tom Baeyens
 */
public class JpdlProcessDefinition extends ProcessDefinitionImpl {

  private static final long serialVersionUID = 1L;

  Map<String, SwimlaneDefinitionImpl> swimlaneDefinitions = new HashMap<String, SwimlaneDefinitionImpl>();

  protected ExecutionImpl newProcessInstance() {
    return new ExecutionImpl();
  }

  public SwimlaneDefinitionImpl createSwimlaneDefinition(String name) {
    SwimlaneDefinitionImpl swimlaneDefinition = new SwimlaneDefinitionImpl();
    swimlaneDefinition.setName(name);
    swimlaneDefinitions.put(name, swimlaneDefinition);
    return swimlaneDefinition;
  }

  public SwimlaneDefinitionImpl getSwimlaneDefinition(String name) {
    return swimlaneDefinitions.get(name);
  }

  public TaskDefinitionImpl createTaskDefinition(String name) {
    TaskDefinitionImpl taskDefinition = new TaskDefinitionImpl();
    taskDefinitions.put(name, taskDefinition);
    return taskDefinition;
  }

  public Map<String, TaskDefinitionImpl> getTaskDefinitions() {
    return taskDefinitions;
  }
}
