/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.workflow.jbpm;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.*;
import org.jahia.utils.FileUtils;
import org.jbpm.api.*;
import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.history.HistoryActivityInstance;
import org.jbpm.api.history.HistoryProcessInstance;
import org.jbpm.api.history.HistoryProcessInstanceQuery;
import org.jbpm.api.history.HistoryTask;
import org.jbpm.api.job.Job;
import org.jbpm.api.model.Activity;
import org.jbpm.api.task.Participation;
import org.jbpm.api.task.Task;
import org.jbpm.jpdl.internal.activity.TaskActivity;
import org.jbpm.jpdl.internal.model.JpdlProcessDefinition;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.svc.HistoryServiceImpl;
import org.jbpm.pvm.internal.task.TaskDefinitionImpl;
import org.jbpm.pvm.internal.wire.usercode.UserCodeActivityBehaviour;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import java.text.MessageFormat;
import java.util.*;

/**
 * Implementation of the {@link WorkflowProvider} that uses JBoss jBPM engine.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 2 févr. 2010
 */
public class JBPMProvider implements WorkflowProvider, InitializingBean, JBPMEventGeneratorInterceptor.JBPMEventListener {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JBPMProvider.class);
    private String key;
    private WorkflowService workflowService;
    private RepositoryService repositoryService;
    private ExecutionService executionService;
    private HistoryService historyService;
    private ManagementService managementService;
    private Resource[] processes;
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
        managementService = processEngine.getManagementService();
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
        if (processes != null && processes.length > 0) {
            logger.info("Found " + processes.length + " workflow processes to be deployed.");
            List<Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
            for (Resource process : processes) {
                long lastModified = FileUtils.getLastModified(process);

                boolean needUpdate = true;
                boolean found = false;
                String fileName = process.getFilename();
                for (Deployment deployment : deploymentList) {
                    if (deployment.getName().equals(fileName)) {
                        found = true;
                        if (deployment.getTimestamp() >= lastModified) {
                            needUpdate = false;
                            break;
                        }
                    }
                }
                if (needUpdate) {
                    if (found) {
                        logger.info("Found workflow process " + fileName + ". Updating...");
                    } else {
                        logger.info("Found new workflow process " + fileName + ". Deploying...");
                    }
                    NewDeployment newDeployment = repositoryService.createDeployment();
                    newDeployment.addResourceFromInputStream(process.getFilename(), process.getInputStream());
                    newDeployment.setTimestamp(lastModified);
                    newDeployment.setName(fileName);
                    newDeployment.deploy();
                    logger.info("... done");
                } else {
                    logger.info("Found workflow process " + fileName + ". It is up-to-date.");
                }
            }
            logger.info("...workflow processes deployed.");
        }
    }

    public void setProcesses(Resource[] processes) {
        this.processes = processes;
    }

    public List<WorkflowDefinition> getAvailableWorkflows(Locale locale) {
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
            WorkflowDefinition wf = convertToWorkflowDefinition(definition, locale);
            workflows.put(definition.getName(), wf);
            versions.put(definition.getName(), definition.getVersion());
            if (logger.isDebugEnabled()) {
                logger.debug("Process : " + definition);
            }
        }
        return new ArrayList<WorkflowDefinition>(workflows.values());
    }

    public WorkflowDefinition getWorkflowDefinitionByKey(String key, Locale locale) {
        ProcessDefinition value = getProcessDefinitionByKey(key);
        WorkflowDefinition wf = convertToWorkflowDefinition(value, locale);
        return wf;
    }

    public WorkflowDefinition getWorkflowDefinitionById(String id, Locale locale) {
        ProcessDefinition value = getProcessDefinitionById(id);
        WorkflowDefinition wf = convertToWorkflowDefinition(value, locale);
        return wf;
    }

    private ProcessDefinition getProcessDefinitionByKey(String key) {
        if (logger.isDebugEnabled()) {
            logger.debug(MessageFormat.format("List of all available process ({0}) : ",
                    repositoryService.createProcessDefinitionQuery().count()));
        }
        final List<ProcessDefinition> definitionList =
                repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list();

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
        final List<ProcessDefinition> definitionList =
                repositoryService.createProcessDefinitionQuery().processDefinitionId(id).list();

        ProcessDefinition value = null;

        for (ProcessDefinition definition : definitionList) {
            if (value != null && value.getVersion() > definition.getVersion()) {
                continue;
            }
            value = definition;
        }
        return value;
    }

    public List<Workflow> getActiveWorkflowsInformations(List<String> processIds, Locale locale) {
        List<Workflow> workflows = new LinkedList<Workflow>();
        for (String processId : processIds) {
            final ProcessInstance instance = executionService.findProcessInstanceById(processId);
            if (instance != null) {
                final Workflow workflow = convertToWorkflow(instance, locale);
                workflows.add(workflow);
            }
        }
        return workflows;
    }

    public String startProcess(String processKey, Map<String, Object> args) {
        String res = executionService.startProcessInstanceByKey(processKey, args).getId();
        executionService.createVariable(res, "user", args.get("user"), true);
        executionService.createVariable(res, "nodeId", args.get("nodeId"), true);
        return res;
    }


    public void signalProcess(String processId, String transitionName, Map<String, Object> args) {
        final Execution in = executionService.findProcessInstanceById(processId).findActiveExecutionIn(transitionName);
        executionService.signalExecutionById(in.getId(), args);
    }

    public void signalProcess(String processId, String transitionName, String signalName, Map<String, Object> args) {
        final Execution in = executionService.findProcessInstanceById(processId).findActiveExecutionIn(transitionName);
        executionService.signalExecutionById(in.getId(), signalName, args);
    }

    public Workflow getWorkflow(String processId, Locale locale) {
        ProcessInstance pi = executionService.findProcessInstanceById(processId);
        return convertToWorkflow(pi, locale);
    }

    public Set<WorkflowAction> getAvailableActions(String processId, Locale locale) {
        final ProcessInstance instance = executionService.findProcessInstanceById(processId);
        final Set<String> actions = instance.findActiveActivityNames();
        final Set<WorkflowAction> availableActions = new LinkedHashSet<WorkflowAction>(actions.size());
        String definitionKey = getProcessDefinitionById(instance.getProcessDefinitionId()).getKey();

        for (String action : actions) {
            WorkflowAction workflowAction = null;
            if (taskService.createTaskQuery().processInstanceId(processId).activityName(action).count() > 0) {
                List<Task> taskList =
                        taskService.createTaskQuery().processInstanceId(processId).activityName(action).list();
                for (Task task : taskList) {
                    if (task.getActivityName().equals(action)) {
                        workflowAction = convertToWorkflowTask(task, locale);
                    }
                }
            } else {
                workflowAction = new WorkflowAction(action, key);
                i18nOfWorkflowAction(locale, workflowAction, definitionKey);
            }
            if (workflowAction != null) {
                availableActions.add(workflowAction);
            }
        }
        return availableActions;
    }

    public List<WorkflowTask> getTasksForUser(JahiaUser user, Locale locale) {
        final List<WorkflowTask> availableActions = new LinkedList<WorkflowTask>();
        List<Task> taskList = taskService.findPersonalTasks(user.getUserKey());
        for (Task task : taskList) {
            WorkflowTask action = convertToWorkflowTask(task, locale);
            availableActions.add(action);
        }
        taskList = taskService.findGroupTasks(user.getUserKey());
        for (Task task : taskList) {
            WorkflowTask action = convertToWorkflowTask(task, locale);
            availableActions.add(action);
        }
        return availableActions;
    }

    public List<Workflow> getWorkflowsForUser(JahiaUser user, Locale locale) {
        List<Workflow> list = new ArrayList<Workflow>();
        List<ProcessInstance> pi = executionService.createProcessInstanceQuery().list();
        for (ProcessInstance processInstance : pi) {
            String userkey = (String) executionService.getVariable(processInstance.getId(),"user");
            if (userkey != null && user.getUserKey().equals(userkey)) {
                list.add(convertToWorkflow(processInstance, locale));
            }
        }
        return list;
    }

    private WorkflowDefinition convertToWorkflowDefinition(ProcessDefinition value, Locale locale) {
        WorkflowDefinition wf = new WorkflowDefinition(value.getName(), value.getKey(), this.key);
        wf.setFormResourceName(repositoryService.getStartFormResourceName(value.getId(),
                repositoryService.getStartActivityNames(value.getId()).get(0)));
        if (value instanceof JpdlProcessDefinition) {
            JpdlProcessDefinition definition = (JpdlProcessDefinition) value;
            final Map<String, TaskDefinitionImpl> taskDefinitions = definition.getTaskDefinitions();
            final Set<String> tasks = new LinkedHashSet<String>();
            tasks.add(WorkflowService.START_ROLE);
            tasks.addAll(taskDefinitions.keySet());
            wf.setTasks(tasks);
        }
        if (locale != null) {
            try {
                ResourceBundle resourceBundle = getResourceBundle(locale, wf.getKey());
                wf.setDisplayName(resourceBundle.getString("name"));
            } catch (Exception e) {
                wf.setDisplayName(wf.getName());
            }
        }

        return wf;
    }

    private Workflow convertToWorkflow(ProcessInstance instance, Locale locale) {
        final Workflow workflow = new Workflow(instance.getName(), instance.getId(), key);
        final WorkflowDefinition definition = getWorkflowDefinitionById(instance.getProcessDefinitionId(), locale);
        workflow.setWorkflowDefinition(definition);
        workflow.setAvailableActions(getAvailableActions(instance.getId(), locale));
        Job job = managementService.createJobQuery().timers().processInstanceId(instance.getId()).uniqueResult();
        if (job != null) {
            workflow.setDuedate(job.getDueDate());
        }
        workflow.setStartTime(historyService.createHistoryProcessInstanceQuery().processInstanceId(instance.getId()).orderAsc(HistoryProcessInstanceQuery.PROPERTY_STARTTIME).uniqueResult().getStartTime());
        
        Object user = executionService.getVariable(instance.getId(), "user");
        if (user != null) {
            workflow.setStartUser(user.toString());
        }

        Set<String> variableNames = executionService.getVariableNames(instance.getId());
        workflow.setVariables(executionService.getVariables(instance.getId(), variableNames));

        return workflow;
    }

    private WorkflowTask convertToWorkflowTask(Task task, Locale locale) {
        WorkflowTask action = new WorkflowTask(task.getActivityName(), key);
        action.setDueDate(task.getDuedate());
        action.setDescription(task.getDescription());
        action.setCreateTime(task.getCreateTime());
        action.setProcessId(task.getExecutionId());
        if (task.getAssignee() != null) {
            action.setAssignee(
                    ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(task.getAssignee()));
        }
        action.setId(task.getId());
        action.setOutcome(taskService.getOutcomes(task.getId()));
        List<Participation> participationList = taskService.getTaskParticipations(task.getId());
        if (participationList.size() > 0) {
            List<WorkflowParticipation> participations = new ArrayList<WorkflowParticipation>();
            for (Participation participation : participationList) {
                if (participation.getGroupId() != null) {
                    participations
                            .add(new WorkflowParticipation(participationRolesInverted.get(participation.getType()),
                                    groupManager.lookupGroup(participation.getGroupId())));
                } else {
                    if (participation.getUserId() != null) {
                        participations
                                .add(new WorkflowParticipation(participationRolesInverted.get(participation.getType()),
                                        userManager.lookupUserByKey(participation.getUserId())));
                    }
                }
            }
            action.setParticipations(participations);
        }
        // Get form resource name
        action.setFormResourceName(task.getFormResourceName());

        // Get Tasks variables
        Set<String> variableNames = taskService.getVariableNames(task.getId());
        action.setVariables(taskService.getVariables(task.getId(), variableNames));
        final ProcessInstance instance = executionService.findProcessInstanceById(task.getExecutionId());
        if (instance != null) {
            final WorkflowDefinition definition = getWorkflowDefinitionById(instance.getProcessDefinitionId(), locale);
            action.setWorkflowDefinition(definition);
            i18nOfWorkflowAction(locale, action, definition.getKey());
        }
        return action;
    }

    public void assignTask(String taskId, JahiaUser user) {
        Task task = taskService.getTask(taskId);
        if (user == null) {
            taskService.assignTask(task.getId(), null);
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
            List<? extends Activity> list = ((JpdlProcessDefinition) definition).getActivities();
            for (Activity activity : list) {
                if (activity instanceof ActivityImpl) {
                    ActivityBehaviour activityBehaviour = ((ActivityImpl) activity).getActivityBehaviour();
                    if (activityBehaviour instanceof TaskActivity) {
                        // check the assignment handler .. ?
                        ((TaskActivity) activityBehaviour).getTaskDefinition().getAssignmentHandlerReference();
                        results.add(activity.getName());
                    }
                }
            }
        }

        return results;
    }

    public void addComment(String processId, String comment, String user) {
        List<WorkflowComment> comments = (List<WorkflowComment>) executionService.getVariable(processId, "comments");
        if (comments == null) {
            comments = new ArrayList<WorkflowComment>();
        }
        final WorkflowComment wfComment = new WorkflowComment();
        wfComment.setComment(comment);
        wfComment.setUser(user);
        wfComment.setTime(new Date());
        comments.add(wfComment);
        executionService.setVariable(processId, "comments", comments);
    }

    public WorkflowTask getWorkflowTask(String taskId, Locale locale) {
        return convertToWorkflowTask(taskService.getTask(taskId), locale);
    }

    public void registerListeners() {
        final List<ProcessDefinition> definitionList = repositoryService.createProcessDefinitionQuery().list();

        JBPMListener listener = new JBPMListener(this);

        for (ProcessDefinition definition : definitionList) {
            if (definition instanceof JpdlProcessDefinition) {
                ((JpdlProcessDefinition) definition).createEvent("start").createEventListenerReference(listener);
                ((JpdlProcessDefinition) definition).createEvent("end").createEventListenerReference(listener);
            }

        }

        // now let's connect to JBPM event generator we have added.
        JBPMEventGeneratorInterceptor.registerListener(this);

    }

    public String getProcessDefinitionType(ProcessDefinition definition) {
        ArrayList<String> results = new ArrayList<String>();

        if (definition instanceof JpdlProcessDefinition) {
            List<? extends Activity> list = ((JpdlProcessDefinition) definition).getActivities();

            for (Activity activity : list) {
                if (activity instanceof ActivityImpl) {
                    ActivityBehaviour activityBehaviour = ((ActivityImpl) activity).getActivityBehaviour();
                    if (activityBehaviour instanceof UserCodeActivityBehaviour) {

                        // check the assignment handler .. ?
                        ((TaskActivity) activityBehaviour).getTaskDefinition().getAssignmentHandlerReference();
                        results.add(activity.getName());
                    }
                }
            }
        }

        return "";
    }

    public List<HistoryWorkflow> getHistoryWorkflowsForNode(String nodeId, Locale locale) {
        HistoryProcessInstanceByVariableQuery q = new HistoryProcessInstanceByVariableQuery();
        q.setCommandService(((HistoryServiceImpl) historyService).getCommandService());
        q.variable("nodeId",nodeId);
        List<HistoryProcessInstance> list = q.list();

        List<HistoryWorkflow> historyItems = new LinkedList<HistoryWorkflow>();

        for (HistoryProcessInstance jbpmHistoryItem : list) {
            final HistoryWorkflow workflow = convertToHistoryWorkflow(jbpmHistoryItem, locale);
            historyItems.add(workflow);
        }

        return historyItems;
    }


    /**
     * Returns a list of process instance history records for the specified
     * process IDs. This method also returns "active" (i.e. not completed)
     * workflow process instance. Instances are sorted by start time descending,
     * i.e. newly started instances first.
     *
     * @param processIds list of process IDs to retrieve history records for
     * @param locale
     * @return a list of process instance history records for the specified
     *         process IDs
     */
    public List<HistoryWorkflow> getHistoryWorkflows(List<String> processIds, Locale locale) {
        List<HistoryWorkflow> historyItems = new LinkedList<HistoryWorkflow>();
        for (String processId : processIds) {
            HistoryProcessInstance jbpmHistoryItem = null;
            try {
                jbpmHistoryItem =
                        historyService.createHistoryProcessInstanceQuery().processInstanceId(processId).uniqueResult();
            } catch (JbpmException e) {
                logger.error(e.getMessage(), e);
            }
            if (jbpmHistoryItem == null) {
                logger.warn("History record for process instance with ID '" + processId + "' cannot be found");
                continue;
            }

            final HistoryWorkflow workflow = convertToHistoryWorkflow(jbpmHistoryItem, locale);
            historyItems.add(workflow);

        }

        return historyItems;
    }

    private HistoryWorkflow convertToHistoryWorkflow(HistoryProcessInstance jbpmHistoryItem, Locale locale) {
        ProcessDefinition def = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(jbpmHistoryItem.getProcessDefinitionId()).uniqueResult();
        final String startUser =
                (String) historyService.getVariable(jbpmHistoryItem.getProcessInstanceId(), "user");
        final String nodeId =
                (String) historyService.getVariable(jbpmHistoryItem.getProcessInstanceId(), "nodeId");

        String title = null;
        try {
            title = ((List<WorkflowVariable>) executionService.getVariable(jbpmHistoryItem.getProcessInstanceId(), "jcr:title")).get(0).getValue();
        } catch (Exception e) {
        }

        final HistoryWorkflow workflow = new HistoryWorkflow(jbpmHistoryItem.getProcessInstanceId(),
                def != null ? convertToWorkflowDefinition(def, locale) : null, def != null ? def.getName() : null,
                getKey(), startUser, jbpmHistoryItem.getStartTime(), jbpmHistoryItem.getEndTime(),
                jbpmHistoryItem.getEndActivityName(), nodeId);
        try {
            ResourceBundle resourceBundle = getResourceBundle(locale, def.getKey());
            workflow.setDisplayName(resourceBundle.getString("name"));
        } catch (Exception e) {
            workflow.setDisplayName(workflow.getName());
        }
        if (title != null) {
            workflow.setDisplayName(title + " - " + workflow.getDisplayName());
        }
        return workflow;
    }

    /**
     * Returns a list of history records for workflow tasks. This method also
     * returns not completed tasks.
     *
     * @param processId the process instance ID
     * @param locale
     * @return a list of history records for workflow tasks
     */
    public List<HistoryWorkflowTask> getHistoryWorkflowTasks(String processId, Locale locale) {
        List<HistoryWorkflowTask> historyItems = new LinkedList<HistoryWorkflowTask>();
        List<HistoryTask> jbpmTasks = null;
        try {
            jbpmTasks = historyService.createHistoryTaskQuery().executionId(processId).list();
        } catch (JbpmException e) {
            logger.error(
                    "History task records for process instance with ID '" + processId + "' cannot be found. Cause: " +
                            e.getMessage(), e);
        }
        if (jbpmTasks == null) {
            return Collections.emptyList();
        }

        for (HistoryTask jbpmHistoryTask : jbpmTasks) {
            final Task task = taskService.getTask(jbpmHistoryTask.getId());
            String name = "";
            if (task != null) {
                name = task != null ? task.getName() : "";
            } else {
                // So nice !
                List<HistoryActivityInstance> l = historyService.createHistoryActivityInstanceQuery().processInstanceId(jbpmHistoryTask.getExecutionId()).list();
                for (HistoryActivityInstance activityInstance : l) {
                    if (activityInstance.getStartTime().equals(jbpmHistoryTask.getCreateTime()) &&
                            activityInstance.getEndTime() != null && activityInstance.getEndTime().equals(jbpmHistoryTask.getEndTime())) {
                        name = activityInstance.getActivityName();
                        break;
                    }
                }
            }
            historyItems
                    .add(new HistoryWorkflowTask(jbpmHistoryTask.getId(), jbpmHistoryTask.getExecutionId(), name,
                            getKey(), jbpmHistoryTask.getAssignee(), jbpmHistoryTask.getCreateTime(), jbpmHistoryTask.getEndTime(),
                            jbpmHistoryTask.getOutcome()));
        }

        WorkflowDefinition def = getHistoryWorkflows(Collections.singletonList(processId), locale).get(0).getWorkflowDefinition();
        for (HistoryWorkflowTask task : historyItems) {
            ResourceBundle resourceBundle = getResourceBundle(locale, def.getKey());
            try {
                task.setDisplayName(
                        resourceBundle.getString(task.getName().replaceAll(" ", ".").trim().toLowerCase()));
            } catch (Exception e) {
                task.setDisplayName(task.getName());
            }

            String outcome = task.getOutcome();
            if (outcome != null) {
                String key = task.getName().replaceAll(" ", ".").trim().toLowerCase() + "." +
                        outcome.replaceAll(" ", ".").trim().toLowerCase();
                String displayOutcome;
                try {
                    displayOutcome = resourceBundle.getString(key);
                } catch (Exception e) {
                    logger.info("Missing ressource : " + key + " in " + resourceBundle);
                    displayOutcome = outcome;
                }

                task.setDisplayOutcome(displayOutcome);
            }
        }


        return historyItems;
    }

    public void deleteProcess(String processId) {
        executionService.deleteProcessInstanceCascade(processId);
    }

    private ResourceBundle getResourceBundle(Locale locale, final String definitionKey) {
    	try {
        return ResourceBundle
                .getBundle(WorkflowService.class.getPackage().getName() + "." + definitionKey.replaceAll(" ", ""),
                        locale);
    	} catch (Exception e) {
    		logger.error(e.getMessage(), e);
    		return null;
    	}
    }

    private void i18nOfWorkflowAction(Locale displayLocale, WorkflowAction workflowAction, final String definitionKey) {
        String rbActionName = workflowAction.getName();
        ResourceBundle resourceBundle = null;
        if (displayLocale != null) {
            resourceBundle = getResourceBundle(displayLocale, definitionKey);
        }
        if (resourceBundle != null) {
            try {
                rbActionName = resourceBundle.getString(workflowAction.getName().replaceAll(" ",
                        ".").trim().toLowerCase());
            } catch (MissingResourceException e) {
                logger.info("Missing ressource : " + key + " in " + resourceBundle);
            }
        }
        workflowAction.setDisplayName(rbActionName);
        if (workflowAction instanceof WorkflowTask) {
            WorkflowTask workflowTask = (WorkflowTask) workflowAction;
            Set<String> outcomes = workflowTask.getOutcomes();
            List<String> displayOutcomes = new LinkedList<String>();
            for (String outcome : outcomes) {
                String key = workflowAction.getName().replaceAll(" ", ".").trim().toLowerCase() + "." +
                        outcome.replaceAll(" ", ".").trim().toLowerCase();
                String s= outcome;
                if (resourceBundle != null) {
                    try {

                        s = resourceBundle.getString(key);
                    } catch (Exception e) {
                        logger.info("Missing ressource : " + key + " in " + resourceBundle);
                    }
                }
                displayOutcomes.add(s);
            }
            workflowTask.setDisplayOutcomes(displayOutcomes);
        }
    }


    public <T> boolean canProcess(Command<T> command) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T> void beforeCommand(Command<T> command) {
    }

    public <T> void afterCommand(Command<T> command) {

    }
}
