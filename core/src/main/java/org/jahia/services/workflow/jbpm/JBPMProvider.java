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
package org.jahia.services.workflow.jbpm;

import org.apache.log4j.Logger;
import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.WorkflowAction;
import org.jahia.services.workflow.WorkflowProvider;
import org.jbpm.api.*;
import org.springframework.beans.factory.InitializingBean;

import java.text.MessageFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 2 févr. 2010
 */
public class JBPMProvider implements WorkflowProvider, InitializingBean {
    private transient static Logger logger = Logger.getLogger(JBPMProvider.class);
    private RepositoryService repositoryService;
    private ExecutionService executionService;
    private ProcessEngine processEngine;
    private List<String> processes;

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
        repositoryService = processEngine.getRepositoryService();
        executionService = processEngine.getExecutionService();
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        if (processes != null && processes.size() > 0) {
            for (String process : processes) {
                repositoryService.createDeployment().addResourceFromClasspath(process).deploy();
            }
        }
    }

    public void setProcesses(List<String> processes) {
        this.processes = processes;
    }

    public List<Workflow> getAvailableWorkflows() {
        if (logger.isDebugEnabled()) {
            logger.debug(MessageFormat.format("List of all available process ({0}) : ",
                                              repositoryService.createProcessDefinitionQuery().count()));
        }
        final List<ProcessDefinition> definitionList = repositoryService.createProcessDefinitionQuery().list();
        List<Workflow> workflows = new LinkedList<Workflow>();
        for (ProcessDefinition definition : definitionList) {
            workflows.add(new Workflow(definition.getName(), definition.getId()));
            if (logger.isDebugEnabled()) {
                logger.debug("Process : " + definition);
            }
        }
        return workflows;
    }

    public List<Workflow> getActiveWorkflowsInformations(List<String> processIds) {
        List<Workflow> workflows = new LinkedList<Workflow>();
        for (String processId : processIds) {
            final ProcessInstance instance = executionService.findProcessInstanceById(processId);
            final Workflow workflow = new Workflow(instance.getName(), instance.getId());
            workflow.setAvailableActions(instance.findActiveActivityNames());
            workflows.add(workflow);
        }
        return workflows;
    }

    public String startProcess(String processKey, Map<String,Object> args) {
        return executionService.startProcessInstanceById(processKey,args).getId();
    }

    public void signalProcess(String processId, String transitionName, Map<String, Object> args) {
        final Execution in = executionService.findProcessInstanceById(processId).findActiveExecutionIn(transitionName);
        executionService.signalExecutionById(in.getId(),args);
    }

    public Set<WorkflowAction> getAvailableActions(String processId) {
        final ProcessInstance instance = executionService.findProcessInstanceById(processId);
        final Set<String> actions = instance.findActiveActivityNames();
        final Set<WorkflowAction> availableActions = new LinkedHashSet<WorkflowAction>(actions.size());
        for (String action : actions) {
            availableActions.add(new WorkflowAction(action));
        }
        return availableActions;
    }
}
