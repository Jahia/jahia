package org.jahia.services.workflow.jbpm;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.*;
import org.jahia.utils.Patterns;
import org.jahia.utils.i18n.ResourceBundles;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieRepository;
import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.WorkflowProcess;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * jBPM 6 Workflow Provider implementation
 */
public class JBPM6WorkflowProvider implements WorkflowProvider,
        InitializingBean,
        WorkflowObservationManagerAware {

    private transient static Logger logger = LoggerFactory.getLogger(JBPM6WorkflowProvider.class);

    private String key;
    private WorkflowService workflowService;
    private WorkflowObservationManager observationManager;
    private static JBPM6WorkflowProvider instance;
    private JahiaUserManagerService userManager;
    private JahiaGroupManagerService groupManager;
    private KieRepository kieRepository;
    private KieServices kieServices;
    private KieSession kieSession;
    private TaskService taskService;
    private JBPMListener listener = new JBPMListener(this);

    public void setKey(String key) {
        this.key = key;
    }

    public WorkflowService getWorkflowService() {
        return workflowService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setWorkflowObservationManager(WorkflowObservationManager observationManager) {
        this.observationManager = observationManager;
        listener.setObservationManager(observationManager);
        JBPMTaskAssignmentListener.setObservationManager(observationManager);
    }

    public void setGroupManager(JahiaGroupManagerService groupManager) {
        this.groupManager = groupManager;
    }

    public void setUserManager(JahiaUserManagerService userManager) {
        this.userManager = userManager;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<WorkflowDefinition> getAvailableWorkflows(Locale locale) {
        KieBase kieBase = kieSession.getKieBase();
        Collection<org.kie.api.definition.process.Process> processes = kieBase.getProcesses();
        List<WorkflowDefinition> workflowDefinitions = new ArrayList<WorkflowDefinition>();
        for (org.kie.api.definition.process.Process process : processes) {
            workflowDefinitions.add(convertToWorkflowDefinition(process, locale));
        }
        return workflowDefinitions;
    }

    @Override
    public WorkflowDefinition getWorkflowDefinitionByKey(String key, Locale locale) {
        KieBase kieBase = kieSession.getKieBase();
        Collection<org.kie.api.definition.process.Process> processes = kieBase.getProcesses();
        for (org.kie.api.definition.process.Process process : processes) {
            if (process.getName().equals(key)) {
                return convertToWorkflowDefinition(process, locale);
            }
        }
        return null;
    }

    public WorkflowDefinition getWorkflowDefinitionById(String id, Locale locale) {
        KieBase kieBase = kieSession.getKieBase();
        org.kie.api.definition.process.Process process = kieBase.getProcess(id);
        return convertToWorkflowDefinition(process, locale);
    }

    @Override
    public List<Workflow> getActiveWorkflowsInformations(List<String> processIds, Locale locale) {
        Collection<ProcessInstance> processInstances = kieSession.getProcessInstances();
        List<Workflow> activeWorkflows = new ArrayList<Workflow>();
        for (ProcessInstance processInstance : processInstances) {
            activeWorkflows.add(convertToWorkflow(processInstance, locale));
        }
        return activeWorkflows;
    }

    @Override
    public String startProcess(String processKey, Map<String, Object> args) {
        ProcessInstance processInstance = kieSession.startProcess(processKey, args);
        return Long.toString(processInstance.getId());
    }

    @Override
    public void signalProcess(String processId, String transitionName, Map<String, Object> args) {
        ProcessInstance processInstance = kieSession.getProcessInstance(Long.parseLong(processId));

    }

    @Override
    public void signalProcess(String processId, String transitionName, String signalName, Map<String, Object> args) {
    }

    @Override
    public void abortProcess(String processId) {
        kieSession.abortProcessInstance(Long.parseLong(processId));
    }

    @Override
    public Workflow getWorkflow(String processId, Locale locale) {
        ProcessInstance processInstance = kieSession.getProcessInstance(Long.parseLong(processId));
        String processDefinitionId = processInstance.getProcessId();
        KieBase kieBase = kieSession.getKieBase();

        return convertToWorkflow(processInstance, locale);
    }

    @Override
    public Set<WorkflowAction> getAvailableActions(String processId, Locale locale) {
        ProcessInstance processInstance = kieSession.getProcessInstance(Long.parseLong(processId));
        Set<String> connectionIds = new TreeSet<String>();
        if (processInstance instanceof WorkflowProcessInstance) {
            WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) processInstance;
            Collection<NodeInstance> activeNodeInstances = workflowProcessInstance.getNodeInstances();
            for (NodeInstance nodeInstance : activeNodeInstances) {
                Map<String, List<Connection>> outgoingConnections = nodeInstance.getNode().getOutgoingConnections();
                for (Map.Entry<String, List<Connection>> outgoingConnectionEntry : outgoingConnections.entrySet()) {
                    for (Connection connection : outgoingConnectionEntry.getValue()) {
                        String uniqueId = (String) connection.getMetaData().get("UniqueId");
                        connectionIds.add(uniqueId);
                    }
                }
            }
        }

        Set<WorkflowAction> workflowActions = new HashSet<WorkflowAction>();
        List<Long> taskIds = taskService.getTasksByProcessInstanceId(Long.parseLong(processId));
        for (Long taskId : taskIds) {
            Task task = taskService.getTaskById(taskId);
            String taskName = getI18NText(task.getNames(), locale).getText();
            if (connectionIds.contains(taskName)) {
                WorkflowAction workflowAction = convertToWorkflowTask(task, locale);
                workflowActions.add(workflowAction);
                connectionIds.remove(taskName);
            }
        }
        for (String connectionId : connectionIds) {
            WorkflowAction workflowAction = new WorkflowAction(connectionId, key);
            i18nOfWorkflowAction(locale, workflowAction, processInstance.getProcess().getName());
            workflowActions.add(workflowAction);
        }

        return workflowActions;
    }

    @Override
    public List<WorkflowTask> getTasksForUser(JahiaUser user, Locale locale) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Workflow> getWorkflowsForDefinition(String definition, Locale locale) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Workflow> getWorkflowsForUser(JahiaUser user, Locale locale) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void assignTask(String taskId, JahiaUser user) {
        taskService.claim(Long.parseLong(taskId), user.getUserKey());
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void completeTask(String taskId, String outcome, Map<String, Object> args) {
        //To change body of implemented methods use File | Settings | File Templates.
        args.put("outcome", outcome);
        taskService.complete(taskId, userId, args);
    }

    @Override
    public void addParticipatingGroup(String taskId, JahiaGroup group, String role) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteTask(String taskId, String reason) {
        taskService ???
    }

    @Override
    public void addComment(String processId, String comment, String user) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public WorkflowTask getWorkflowTask(String taskId, Locale locale) {
        Task task = taskService.getTaskById(Long.parseLong(taskId));
        return convertToWorkflowTask(task, locale);
    }

    @Override
    public List<HistoryWorkflow> getHistoryWorkflowsForNode(String nodeId, Locale locale) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<HistoryWorkflow> getHistoryWorkflowsForPath(String path, Locale locale) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<HistoryWorkflow> getHistoryWorkflows(List<String> processIds, Locale locale) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<HistoryWorkflowTask> getHistoryWorkflowTasks(String processId, Locale locale) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteProcess(String processId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private WorkflowDefinition convertToWorkflowDefinition(org.kie.api.definition.process.Process process, Locale locale) {
        WorkflowDefinition wf = new WorkflowDefinition(process.getName(), process.getName(), this.key);
        wf.setFormResourceName(repositoryService.getStartFormResourceName(process.getId(),
                repositoryService.getStartActivityNames(process.getId()).get(0)));
        if (process instanceof WorkflowProcess) {
            WorkflowProcess workflowProcess = (WorkflowProcess) process;
            Node[] nodes = workflowProcess.getNodes();

            final Set<String> tasks = new LinkedHashSet<String>();
            tasks.add(WorkflowService.START_ROLE);
            for (Node node : nodes) {
                tasks.add(node.getName());
            }
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
        WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) instance;
        final Workflow workflow = new Workflow(instance.getProcessName(), Long.toString(instance.getId()), key);
        final WorkflowDefinition definition = getWorkflowDefinitionById(instance.getId(), locale);
        workflow.setWorkflowDefinition(definition);
        workflow.setAvailableActions(getAvailableActions(Long.toString(instance.getId()), locale));
        Job job = managementService.createJobQuery().timers().processInstanceId(instance.getId()).uniqueResult();
        if (job != null) {
            workflow.setDuedate(job.getDueDate());
        }
        workflow.setStartTime(
                historyService.createHistoryProcessInstanceQuery().processInstanceId(instance.getId()).orderAsc(HistoryProcessInstanceQuery.PROPERTY_STARTTIME).uniqueResult().getStartTime());

        Object user = executionService.getVariable(instance.getId(), "user");
        if (user != null) {
            workflow.setStartUser(user.toString());
        }

        Set<String> variableNames = executionService.getVariableNames(instance.getId());
        workflow.setVariables(executionService.getVariables(instance.getId(), variableNames));

        return workflow;
    }

    private WorkflowTask convertToWorkflowTask(Task task, Locale locale) {
        WorkflowTask action = new WorkflowTask(task.getTaskData().getProcessId(), key);
        action.setDueDate(task.getTaskData().getDuedate());
        action.setDescription(task.getDescriptions().);
        action.setCreateTime(task.getTaskData().getCreatedOn());
        action.setProcessId(executionService.findExecutionById(task.getExecutionId()).getProcessInstance().getId());
        if (task.getAssignee() != null) {
            action.setAssignee(
                    ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(task.getAssignee()));
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

    private ResourceBundle getResourceBundle(Locale locale, final String definitionKey) {
        try {
            if (workflowService.getModuleForWorkflow(definitionKey) != null) {
                JahiaTemplatesPackage module = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(workflowService.getModuleForWorkflow(definitionKey));
                return ResourceBundles
                        .get("org.jahia.modules.custom.workflow." + Patterns.SPACE.matcher(definitionKey).replaceAll(""), module, locale);
            }
            return ResourceBundles
                    .get("org.jahia.modules.workflow." + Patterns.SPACE.matcher(definitionKey).replaceAll(""), locale);
        } catch (Exception e) {
            logger.error(e.getMessage());
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
            String key = Patterns.SPACE.matcher(workflowAction.getName()).replaceAll(".").trim().toLowerCase();
            try {
                rbActionName = resourceBundle.getString(key);
            } catch (MissingResourceException e) {
                logger.warn("Missing ressource : " + key + " in " + resourceBundle);
            }
        }
        workflowAction.setDisplayName(rbActionName);
        if (workflowAction instanceof WorkflowTask) {
            WorkflowTask workflowTask = (WorkflowTask) workflowAction;
            Set<String> outcomes = workflowTask.getOutcomes();
            List<String> displayOutcomes = new LinkedList<String>();
            List<String> outcomeIcons = new LinkedList<String>();
            for (String outcome : outcomes) {
                String key = Patterns.SPACE.matcher(workflowAction.getName()).replaceAll(".").trim().toLowerCase() + "." +
                        Patterns.SPACE.matcher(outcome).replaceAll(".").trim().toLowerCase();
                String s = outcome;
                if (resourceBundle != null) {
                    try {
                        s = resourceBundle.getString(key);
                    } catch (Exception e) {
                        logger.warn("Missing ressource : " + key + " in " + resourceBundle);
                    }
                }
                displayOutcomes.add(s);
                String icon = null;
                if (resourceBundle != null) {
                    try {
                        icon = resourceBundle.getString(key + ".icon");
                    } catch (MissingResourceException e) {
                        // ignore;
                    }
                }
                outcomeIcons.add(icon);
            }
            workflowTask.setDisplayOutcomes(displayOutcomes);
            workflowTask.setOutcomeIcons(outcomeIcons);
        }
    }

    private I18NText getI18NText(List<I18NText> i18NTexts, Locale locale) {
        for (I18NText i18NText : i18NTexts) {
            if (i18NText.getLanguage().equals(locale.toString())) {
                return i18NText;
            } else if (i18NText.getLanguage().equals(locale.getLanguage())) {
                return i18NText;
            }
        }
        return null;
    }

}
