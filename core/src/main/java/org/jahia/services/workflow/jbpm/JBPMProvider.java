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
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.*;
import org.jbpm.api.*;
import org.jbpm.api.identity.Group;
import org.jbpm.api.identity.User;
import org.jbpm.api.task.Participation;
import org.jbpm.api.task.Task;
import org.springframework.beans.factory.InitializingBean;

import java.security.Principal;
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
    private TaskService taskService;
    private static Map<String, String> participationRoles = new HashMap<String, String>();
    private static Map<String, String> participationRolesInverted = new HashMap<String, String>();
    private JahiaGroupManagerService groupManager;
    private static JBPMProvider instance;
    private static final String PROVIDER = "jBPM";

    static {
        participationRoles.put(WorkflowService.CANDIDATE, Participation.CANDIDATE);
        participationRolesInverted.put(Participation.CANDIDATE, WorkflowService.CANDIDATE);
    }

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
        repositoryService = processEngine.getRepositoryService();
        executionService = processEngine.getExecutionService();
        taskService = processEngine.getTaskService();
    }

    public static JBPMProvider getInstance() {
        if (instance == null) {
            instance = new JBPMProvider();
        }
        return instance;
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

    public List<WorkflowDefinition> getAvailableWorkflows() {
        if (logger.isDebugEnabled()) {
            logger.debug(MessageFormat.format("List of all available process ({0}) : ",
                                              repositoryService.createProcessDefinitionQuery().count()));
        }
        final List<ProcessDefinition> definitionList = repositoryService.createProcessDefinitionQuery().list();
        List<WorkflowDefinition> workflows = new LinkedList<WorkflowDefinition>();
        for (ProcessDefinition definition : definitionList) {
            workflows.add(new WorkflowDefinition(definition.getName(),definition.getId()));
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
            final Workflow workflow = new Workflow(instance.getName(), instance.getId(), PROVIDER);
            workflow.setAvailableActions(getAvailableActions(instance.getId()));
            workflows.add(workflow);
        }
        return workflows;
    }

    public String startProcess(String processKey, Map<String, Object> args) {
        return executionService.startProcessInstanceById(processKey, args).getId();
    }

    public void signalProcess(String processId, String transitionName, Map<String, Object> args) {
        final Execution in = executionService.findProcessInstanceById(processId).findActiveExecutionIn(transitionName);
        executionService.signalExecutionById(in.getId(), args);
    }

    public void signalProcess(String processId, String transitionName, String signalName, Map<String, Object> args) {
        final Execution in = executionService.findProcessInstanceById(processId).findActiveExecutionIn(transitionName);
        executionService.signalExecutionById(in.getId(), signalName, args);
    }

    public Set<WorkflowAction> getAvailableActions(String processId) {
        final ProcessInstance instance = executionService.findProcessInstanceById(processId);
        final Set<String> actions = instance.findActiveActivityNames();
        final Set<WorkflowAction> availableActions = new LinkedHashSet<WorkflowAction>(actions.size());
        for (String action : actions) {
            WorkflowAction workflowAction = null;
            if (taskService.createTaskQuery().processInstanceId(processId).activityName(action).count() > 0) {
                List<Task> taskList = taskService.createTaskQuery().processInstanceId(processId).activityName(
                        action).list();
                for (Task task : taskList) {
                    if (task.getActivityName().equals(action)) {
                        workflowAction = convertToWorkflowTask(task);
                    }
                }
            } else {
                workflowAction = new WorkflowAction(action, PROVIDER);
            }
            if (workflowAction != null) {
                availableActions.add(workflowAction);
            }
        }
        return availableActions;
    }

    public List<WorkflowTask> getTasksForUser(JahiaUser user) {
        User jBPMUser = getJBPMUser(user);
        final List<WorkflowTask> availableActions = new LinkedList<WorkflowTask>();
        List<Task> taskList = taskService.findPersonalTasks(jBPMUser.getId());
        for (Task task : taskList) {
            WorkflowTask action = convertToWorkflowTask(task);
            availableActions.add(action);
        }
        taskList = taskService.findGroupTasks(jBPMUser.getId());
        for (Task task : taskList) {
            WorkflowTask action = convertToWorkflowTask(task);
            availableActions.add(action);
        }
        return availableActions;
    }

    private WorkflowTask convertToWorkflowTask(Task task) {
        WorkflowTask action = new WorkflowTask(task.getActivityName(), PROVIDER);
        action.setDueDate(task.getDuedate());
        action.setDescription(task.getDescription());
        action.setCreateTime(task.getCreateTime());
        if (task.getAssignee() != null) {
            action.setAssignee(ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(
                    task.getAssignee()));
        }
        action.setId(task.getId());
        action.setOutcome(taskService.getOutcomes(task.getId()));
        List<Participation> participationList = taskService.getTaskParticipations(task.getId());
        if (participationList.size() > 0) {
            List<WorkflowParticipation> participations = new ArrayList<WorkflowParticipation>();
            for (Participation participation : participationList) {
                participations.add(new WorkflowParticipation(participationRolesInverted.get(participation.getType()),
                                                             groupManager.lookupGroup(participation.getGroupId())));
            }
            action.setParticipations(participations);
        }
        return action;
    }

    public void assignTask(String taskId, JahiaUser user) {
        User jBPMUser = getJBPMUser(user);
        Task task = taskService.getTask(taskId);
        taskService.takeTask(task.getId(), jBPMUser.getId());
    }

    public void completeTask(String taskId, String outcome, Map<String, Object> args) {
        taskService.completeTask(taskId, outcome, args);
    }

    public void addParticipatingGroup(String taskId, JahiaGroup group, String role) {
        Group jbpmGroup = getJBPMGroup(group);
        String participationType = participationRoles.get(role);
        if (participationType != null) {
            taskService.addTaskParticipatingGroup(taskId, jbpmGroup.getId(), participationType);
        } else {
            taskService.addTaskParticipatingGroup(taskId, jbpmGroup.getId(), Participation.VIEWER);
        }
    }

    public void deleteTask(String taskId, String reason) {
        taskService.deleteTask(taskId, reason);
    }

    private User getJBPMUser(JahiaUser user) {
        IdentityService identityService = processEngine.getIdentityService();
        User jBPMUser = identityService.findUserById(user.getUserKey());
        if (jBPMUser == null) {
            identityService.createUser(user.getUserKey(), user.getProperty("j:firstName"), user.getProperty(
                    "j:lastName"), user.getProperty("j:email"));
            jBPMUser = identityService.findUserById(user.getUserKey());
        }
        return jBPMUser;
    }

    private Group getJBPMGroup(JahiaGroup group) {
        IdentityService identityService = processEngine.getIdentityService();
        Group jBPMGroup = identityService.findGroupById(group.getGroupKey());
        if (jBPMGroup == null) {
            identityService.createGroup(group.getGroupKey());
            jBPMGroup = identityService.findGroupById(group.getGroupKey());
        }
        Collection<Principal> principalCollection = group.getMembers();
        for (Principal principal : principalCollection) {
            if (principal instanceof JahiaUser) {
                User jbpmUser = getJBPMUser((JahiaUser) principal);
                List<String> groupIdsByUser = identityService.findGroupIdsByUser(jbpmUser.getId());
                if (groupIdsByUser == null || groupIdsByUser.size() == 0 || !groupIdsByUser.contains(jBPMGroup.getId())) {
                    identityService.createMembership(jbpmUser.getId(), jBPMGroup.getId());
                }
            }
        }
        return jBPMGroup;
    }

    public void setGroupManager(JahiaGroupManagerService groupManager) {
        this.groupManager = groupManager;
    }

    public String getGroupId(String groupName) {
        return getJBPMGroup(groupManager.lookupGroup("users")).getId();
    }
}
