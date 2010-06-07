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
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.*;
import org.jbpm.api.*;
import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.history.HistoryComment;
import org.jbpm.api.history.HistoryProcessInstance;
import org.jbpm.api.history.HistoryTask;
import org.jbpm.api.task.Participation;
import org.jbpm.api.task.Task;
import org.jbpm.jpdl.internal.activity.TaskActivity;
import org.jbpm.jpdl.internal.model.JpdlProcessDefinition;
import org.jbpm.pvm.internal.model.Activity;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.wire.usercode.UserCodeActivityBehaviour;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

/**
 * Implementation of the {@link WorkflowProvider} that uses JBoss jBPM engine.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 2 févr. 2010
 */
public class JBPMProvider implements WorkflowProvider, InitializingBean {
    private transient static Logger logger = Logger.getLogger(JBPMProvider.class);
    private String key;
    private WorkflowService workflowService;
    private RepositoryService repositoryService;
    private ExecutionService executionService;
    private HistoryService historyService;
    private List<String> processes;
    private TaskService taskService;
    private static Map<String, String> participationRoles = new HashMap<String, String>();
    private static Map<String, String> participationRolesInverted = new HashMap<String, String>();
    private JahiaUserManagerService userManager;
    private JahiaGroupManagerService groupManager;
    private static JBPMProvider instance;

    static {
        participationRoles.put(WorkflowService.CANDIDATE, Participation.CANDIDATE);
        participationRolesInverted.put(Participation.CANDIDATE, WorkflowService.CANDIDATE);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public WorkflowService getWorkflowService() {
        return workflowService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setGroupManager(JahiaGroupManagerService groupManager) {
        this.groupManager = groupManager;
    }

    public void setUserManager(JahiaUserManagerService userManager) {
        this.userManager = userManager;
    }

    public void setProcessEngine(ProcessEngine processEngine) {
        repositoryService = processEngine.getRepositoryService();
        executionService = processEngine.getExecutionService();
        taskService = processEngine.getTaskService();
        historyService = processEngine.getHistoryService();
    }

    public static JBPMProvider getInstance() {
        if (instance == null) {
            instance = new JBPMProvider();
        }
        return instance;
    }

    public void start() {
        registerListeners();
        workflowService.addProvider(this);
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
            List<Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
            for (String process : processes) {
                URL resource = Thread.currentThread().getContextClassLoader().getResource(process);
                File file = new File(resource.toURI());
                boolean needUpdate = true;
                for (Deployment deployment : deploymentList) {
                    if(deployment.getName().equals(process) && deployment.getTimestamp()<=file.lastModified()) {
                        needUpdate = false;
                        break;
                    }
                }
                if (needUpdate) {
                    NewDeployment newDeployment = repositoryService.createDeployment();
                    newDeployment.addResourceFromClasspath(process);
                    newDeployment.setTimestamp(file.lastModified());
                    newDeployment.setName(process);
                    newDeployment.deploy();
                }
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

        Map<String, Integer> versions = new HashMap<String, Integer>();
        Map<String, WorkflowDefinition> workflows = new HashMap<String, WorkflowDefinition>();

        for (ProcessDefinition definition : definitionList) {
            if (versions.containsKey(definition.getName())) {
                if (versions.get(definition.getName()) > definition.getVersion()) {
                    continue;
                }
            }
            WorkflowDefinition wf = new WorkflowDefinition(definition.getName(), definition.getKey(), key);
            wf.setFormResourceName(repositoryService.getStartFormResourceName(definition.getId(),repositoryService.getStartActivityNames(definition.getId()).get(0)));
            workflows.put(definition.getName(), wf);
            versions.put(definition.getName(), definition.getVersion());
            if (logger.isDebugEnabled()) {
                logger.debug("Process : " + definition);
            }
        }
        return new ArrayList<WorkflowDefinition>(workflows.values());
    }

    public WorkflowDefinition getWorkflowDefinitionByKey(String key) {
        ProcessDefinition value = getProcessDefinitionByKey(key);
        WorkflowDefinition wf = new WorkflowDefinition(value.getName(), value.getKey(), this.key);
        wf.setFormResourceName(repositoryService.getStartFormResourceName(value.getId(),repositoryService.getStartActivityNames(value.getId()).get(0)));
        return wf;
    }

    public WorkflowDefinition getWorkflowDefinitionById(String id) {
        ProcessDefinition value = getProcessDefinitionById(id);
        WorkflowDefinition wf = new WorkflowDefinition(value.getName(), value.getKey(), this.key);
        wf.setFormResourceName(repositoryService.getStartFormResourceName(value.getId(),repositoryService.getStartActivityNames(value.getId()).get(0)));
        return wf;
    }

    private ProcessDefinition getProcessDefinitionByKey(String key) {
        if (logger.isDebugEnabled()) {
            logger.debug(MessageFormat.format("List of all available process ({0}) : ",
                                              repositoryService.createProcessDefinitionQuery().count()));
        }
        final List<ProcessDefinition> definitionList = repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list();

        ProcessDefinition value = null;

        for (ProcessDefinition definition : definitionList) {
            if (value != null && value.getVersion() > definition.getVersion()) {
                continue;
            }
            value = definition;
        }
        return value;
    }

    private ProcessDefinition getProcessDefinitionById(String id) {
        if (logger.isDebugEnabled()) {
            logger.debug(MessageFormat.format("List of all available process ({0}) : ",
                                              repositoryService.createProcessDefinitionQuery().count()));
        }
        final List<ProcessDefinition> definitionList = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).list();

        ProcessDefinition value = null;

        for (ProcessDefinition definition : definitionList) {
            if (value != null && value.getVersion() > definition.getVersion()) {
                continue;
            }
            value = definition;
        }
        return value;
    }

    public List<Workflow> getActiveWorkflowsInformations(List<String> processIds) {
        List<Workflow> workflows = new LinkedList<Workflow>();
        for (String processId : processIds) {
            final ProcessInstance instance = executionService.findProcessInstanceById(processId);
            if (instance != null) {
                final Workflow workflow = new Workflow(instance.getName(), instance.getId(), key);
                workflow.setAvailableActions(getAvailableActions(instance.getId()));
                final WorkflowDefinition definition = getWorkflowDefinitionById(instance.getProcessDefinitionId());
                workflow.setDefinition(definition);
                workflows.add(workflow);
            }
        }
        return workflows;
    }

    public String startProcess(String processKey, Map<String, Object> args) {
        return executionService.startProcessInstanceByKey(processKey, args).getId();
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
                workflowAction = new WorkflowAction(action, key);
            }
            if (workflowAction != null) {
                availableActions.add(workflowAction);
            }
        }
        return availableActions;
    }

    public List<WorkflowTask> getTasksForUser(JahiaUser user) {
        final List<WorkflowTask> availableActions = new LinkedList<WorkflowTask>();
        List<Task> taskList = taskService.findPersonalTasks(user.getUserKey());
        for (Task task : taskList) {
            WorkflowTask action = convertToWorkflowTask(task);
            availableActions.add(action);
        }
        taskList = taskService.findGroupTasks(user.getUserKey());
        for (Task task : taskList) {
            WorkflowTask action = convertToWorkflowTask(task);
            availableActions.add(action);
        }
        return availableActions;
    }

    private WorkflowTask convertToWorkflowTask(Task task) {
        WorkflowTask action = new WorkflowTask(task.getActivityName(), key);
        action.setDueDate(task.getDuedate());
        action.setDescription(task.getDescription());
        action.setCreateTime(task.getCreateTime());
        action.setProcessId(task.getExecutionId());
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
                if (participation.getGroupId() != null) {
                    participations.add(new WorkflowParticipation(participationRolesInverted.get(participation.getType()),
                            groupManager.lookupGroup(participation.getGroupId())));
                } else {
                    participations.add(new WorkflowParticipation(participationRolesInverted.get(participation.getType()),
                            userManager.lookupUserByKey(participation.getUserId())));                    
                }
            }
            action.setParticipations(participations);
        }
        // Get form resource name
        action.setFormResourceName(task.getFormResourceName());
        // Get task comments
        List<HistoryComment> taskComments = taskService.getTaskComments(task.getId());
        List<WorkflowTaskComment> comments = new ArrayList<WorkflowTaskComment>(taskComments.size());
        for (HistoryComment taskComment : taskComments) {
            comments.add(new WorkflowTaskComment(taskComment.getMessage(),taskComment.getTime(), taskComment.getUserId()));
        }
        action.setTaskComments(comments);
        // Get Tasks variables
        Set<String> variableNames = taskService.getVariableNames(task.getId());
        action.setVariables(taskService.getVariables(task.getId(),variableNames));
        return action;
    }

    public void assignTask(String taskId, JahiaUser user) {
        Task task = taskService.getTask(taskId);
        if(user==null) {
            taskService.assignTask(task.getId(),null);
        } else {
        if (user.getUserKey().equals(task.getAssignee())) {
            return;
        }
        taskService.takeTask(task.getId(), user.getUserKey());
    }
    }

    public void completeTask(String taskId, String outcome, Map<String, Object> args) {
        taskService.completeTask(taskId, outcome, args);
    }

    public void addParticipatingGroup(String taskId, JahiaGroup group, String role) {
        String participationType = participationRoles.get(role);
        if (participationType != null) {
            taskService.addTaskParticipatingGroup(taskId, group.getGroupKey(), participationType);
        } else {
            taskService.addTaskParticipatingGroup(taskId, group.getGroupKey(), Participation.VIEWER);
        }
    }

    public void deleteTask(String taskId, String reason) {
        taskService.deleteTask(taskId, reason);
    }

    public List<String> getConfigurableRoles(String processKey) {
        ArrayList<String> results = new ArrayList<String>();

        ProcessDefinition definition = getProcessDefinitionByKey(processKey);

        if (definition instanceof JpdlProcessDefinition) {
            List<? extends Activity> list = ((JpdlProcessDefinition)definition).getActivities();
            for (Activity activity : list) {
                if (activity instanceof ActivityImpl) {
                    ActivityBehaviour activityBehaviour = ((ActivityImpl)activity).getActivityBehaviour();
                    if (activityBehaviour instanceof TaskActivity) {
                        // check the assignation handler .. ?
                        Object o = ((TaskActivity)activityBehaviour).getTaskDefinition().getAssignmentHandlerReference();
                        results.add(activity.getName());
                    }
                }
            }
        }

        return results;
    }

    public void addComment(String taskId, String comment) {
        taskService.addTaskComment(taskId, comment);
    }

    public WorkflowTask getWorkflowTask(String taskId) {
        return convertToWorkflowTask(taskService.getTask(taskId));
    }

    public void registerListeners() {
        final List<ProcessDefinition> definitionList = repositoryService.createProcessDefinitionQuery().list();

        JBPMListener listener = new JBPMListener(this);

        for (ProcessDefinition definition : definitionList) {
            if (definition instanceof JpdlProcessDefinition) {
                ((JpdlProcessDefinition)definition).createEvent("start").createEventListenerReference(listener);
                ((JpdlProcessDefinition)definition).createEvent("end").createEventListenerReference(listener);
            }

        }
    }

    public String getProcessDefinitionType(ProcessDefinition definition) {
        ArrayList<String> results = new ArrayList<String>();

        if (definition instanceof JpdlProcessDefinition) {
            List<? extends Activity> list = ((JpdlProcessDefinition)definition).getActivities();

            for (Activity activity : list) {
                if (activity instanceof ActivityImpl) {
                    ActivityBehaviour activityBehaviour = ((ActivityImpl)activity).getActivityBehaviour();
                    if (activityBehaviour instanceof UserCodeActivityBehaviour) {

                        // check the assignation handler .. ?
                        Object o = ((TaskActivity)activityBehaviour).getTaskDefinition().getAssignmentHandlerReference();
                        results.add(activity.getName());
                    }
                }
            }
        }

        return "";
    }

    /**
     * Returns a list of process instance history records for the specified
     * process IDs. This method also returns "active" (i.e. not completed)
     * workflow process instance. Instances are sorted by start time descending,
     * i.e. newly started instances first.
     * 
     * @param processIds list of process IDs to retrieve history records for
     * @return a list of process instance history records for the specified
     *         process IDs
     */
    public List<HistoryWorkflow> getHistoryWorkflows(List<String> processIds) {
        List<HistoryWorkflow> historyItems = new LinkedList<HistoryWorkflow>();
        Map<String, String> processDefIdToKeyMapping = new HashMap<String, String>(1);
        for (String processId : processIds) {
            HistoryProcessInstance jbpmHistoryItem = null;
            try {
                jbpmHistoryItem = historyService.createHistoryProcessInstanceQuery().processInstanceId(processId)
                        .uniqueResult();
            } catch (JbpmException e) {
                logger.error(e.getMessage(), e);
            }
            if (jbpmHistoryItem == null) {
                logger.warn("History record for process instance with ID '" + processId + "' cannot be found");
                continue;
            }

            String processDefKey = processDefIdToKeyMapping.get(jbpmHistoryItem.getProcessDefinitionId());
            if (processDefKey == null) {
                ProcessDefinition def = repositoryService.createProcessDefinitionQuery().processDefinitionId(
                        jbpmHistoryItem.getProcessDefinitionId()).uniqueResult();
                if (def != null) {
                    processDefKey = def.getKey();
                    processDefIdToKeyMapping.put(jbpmHistoryItem.getProcessDefinitionId(),processDefKey);
                } else {
                    logger.warn("Cannot find process definition by ID " + jbpmHistoryItem.getProcessDefinitionId());
                }
            }
            historyItems.add(new HistoryWorkflow(jbpmHistoryItem.getProcessInstanceId(), processDefKey,
                    jbpmHistoryItem.getKey(), getKey(), jbpmHistoryItem.getStartTime(), jbpmHistoryItem.getEndTime(),
                    jbpmHistoryItem.getEndActivityName()));
        }

        return historyItems;
    }

    /**
     * Returns a list of history records for workflow tasks. This method also
     * returns not completed tasks.
     * 
     * @param processId the process instance ID
     * @return a list of history records for workflow tasks
     */
    public List<HistoryWorkflowTask> getHistoryWorkflowTasks(String processId) {
        List<HistoryWorkflowTask> historyItems = new LinkedList<HistoryWorkflowTask>();
        List<HistoryTask> jbpmTasks = null;
        try {
            jbpmTasks = historyService.createHistoryTaskQuery().executionId(processId).list();
        } catch (JbpmException e) {
            logger.error("History task records for process instance with ID '" + processId
                    + "' cannot be found. Cause: " + e.getMessage(), e);
        }
        if (jbpmTasks == null) {
            return Collections.emptyList();
        }

        for (HistoryTask jbpmHistoryTask : jbpmTasks) {
            final Task task = taskService.getTask(jbpmHistoryTask.getId());
            historyItems.add(new HistoryWorkflowTask(jbpmHistoryTask.getExecutionId(), (task != null ? task.getName() : ""), getKey(), jbpmHistoryTask
                    .getCreateTime(), jbpmHistoryTask.getEndTime(), jbpmHistoryTask.getOutcome(), jbpmHistoryTask
                    .getAssignee()));
        }

        return historyItems;
    }
}
