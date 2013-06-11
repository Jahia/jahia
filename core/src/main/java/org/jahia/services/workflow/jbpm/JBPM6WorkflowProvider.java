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
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public WorkflowDefinition getWorkflowDefinitionByKey(String key, Locale locale) {
        KieBase kieBase = kieSession.getKieBase();
        org.kie.api.definition.process.Process process = kieBase.getProcess(key);
        return convertToWorkflowDefinition(process, locale);
    }

    @Override
    public List<Workflow> getActiveWorkflowsInformations(List<String> processIds, Locale locale) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String startProcess(String processKey, Map<String, Object> args) {
        ProcessInstance processInstance = kieSession.startProcess(processKey, args);
        return Long.toString(processInstance.getId());
    }

    @Override
    public void signalProcess(String processId, String transitionName, Map<String, Object> args) {
        ProcessInstance processInstance = kieSession.getProcessInstance(Long.getLong(processId));

    }

    @Override
    public void signalProcess(String processId, String transitionName, String signalName, Map<String, Object> args) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void abortProcess(String processId) {
        kieSession.abortProcessInstance(Long.getLong(processId));
    }

    @Override
    public Workflow getWorkflow(String processId, Locale locale) {
        ProcessInstance processInstance = kieSession.getProcessInstance(Long.getLong(processId));
        String processDefinitionId = processInstance.getProcessId();
        KieBase kieBase = kieSession.getKieBase();
        org.kie.api.definition.process.Process process = kieBase.getProcess(processDefinitionId);

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<WorkflowAction> getAvailableActions(String processId, Locale locale) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void completeTask(String taskId, String outcome, Map<String, Object> args) {
        //To change body of implemented methods use File | Settings | File Templates.
        taskService.complete(taskId, userId, args);
    }

    @Override
    public void addParticipatingGroup(String taskId, JahiaGroup group, String role) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteTask(String taskId, String reason) {
        //To change body of implemented methods use File | Settings | File Templates.
        taskService.
    }

    @Override
    public List<String> getConfigurableRoles(String processKey) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addComment(String processId, String comment, String user) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public WorkflowTask getWorkflowTask(String taskId, Locale locale) {
        Task task = taskService.getTaskById(Long.getLong(taskId));
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

    private WorkflowDefinition convertToWorkflowDefinition(org.kie.api.definition.process.Process value, Locale locale) {
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
        final Workflow workflow = new Workflow(instance.getProcessName(), Long.toString(instance.getId()), key);
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
        WorkflowTask action = new WorkflowTask(task.getTaskData().getProcessId(), key);
        action.setDueDate(task.getDuedate());
        action.setDescription(task.getDescription());
        action.setCreateTime(task.getCreateTime());
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

}
