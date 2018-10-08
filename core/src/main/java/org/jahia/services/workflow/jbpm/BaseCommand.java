/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.workflow.jbpm;

import org.codehaus.plexus.util.StringUtils;
import org.drools.core.command.impl.FixedKnowledgeCommandContext;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.*;
import org.jahia.utils.i18n.ResourceBundles;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.KieBase;
import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.WorkflowProcess;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.*;
import org.kie.internal.command.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.persistence.EntityManager;

import java.util.*;
import java.util.stream.Collectors;

/**
* Created by toto on 16/12/13.
*/
public abstract class BaseCommand<T> implements GenericCommand<T> {
    private static final Comparator<Constraint> CONSTRAINT_PRIORITY_COMPARATOR = new Comparator<Constraint>() {
        @Override
        public int compare(Constraint o1, Constraint o2) {
            return Integer.compare(o2.getPriority(), o1.getPriority());
        }
        
    };

    private static final long serialVersionUID = -2742789169791810141L;

    private transient static Logger logger = LoggerFactory.getLogger(BaseCommand.class);

    private KnowledgeCommandContext context;
    private KieSession ksession;
    private TaskService taskService;
    private AuditLogService auditLogService;
    private EntityManager em;
    private RuntimeEngine runtimeEngine;
    private JbpmServicesPersistenceManager persistenceManager;
    private WorkflowService workflowService;
    private JahiaUserManagerService userManager;
    private JahiaGroupManagerService groupManager;
    private String key;
    private boolean localTransactionOwner = false;

    public KieSession getKieSession() {
        return ksession;
    }

    public TaskService getTaskService() {
        if (taskService == null) {
            taskService = runtimeEngine.getTaskService();
            localTransactionOwner = persistenceManager.beginTransaction();
        }
        return taskService;
    }

    public AuditLogService getLogService() {
        if (auditLogService == null) {
            final Environment environment = context.getKieSession().getEnvironment();
            this.auditLogService = new JPAAuditLogService(environment);
        }
        return auditLogService;
    }

    public EntityManager getEm() {
        return em;
    }

    public WorkflowService getWorkflowService() {
        return workflowService;
    }

    public JahiaUserManagerService getUserManager() {
        return userManager;
    }

    public JahiaGroupManagerService getGroupManager() {
        return groupManager;
    }

    public String getKey() {
        return key;
    }

    public void setRuntimeEngine(RuntimeEngine runtimeEngine) {
        this.runtimeEngine = runtimeEngine;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }

    public void setPersistenceManager(JbpmServicesPersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setUserManager(JahiaUserManagerService userManager) {
        this.userManager = userManager;
    }

    public void setGroupManager(JahiaGroupManagerService groupManager) {
        this.groupManager = groupManager;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public T execute(Context context) {
        if( ! (context instanceof KnowledgeCommandContext) ) {
            throw new UnsupportedOperationException("This command must be executed by a " + KieSession.class.getSimpleName() + " instance!");
        }
        KnowledgeCommandContext realContext = (FixedKnowledgeCommandContext) context;
        this.context = realContext;

        ksession = realContext.getKieSession();

        final Environment environment = realContext.getKieSession().getEnvironment();
//            environment.set("IS_SHARED_ENTITY_MANAGER", true);
//            final EntityManager cmdEM = ((EntityManagerHolder) TransactionSynchronizationManager.getResource("cmdEM")).getEntityManager();
//
//            cmdEM.joinTransaction();
//            environment.set(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, cmdEM);

        EntityManager jbpmEm = (EntityManager) SpringContextSingleton.getBean("jbpmEm");

        environment.set("IS_JTA_TRANSACTION", false);
        environment.set("IS_SHARED_ENTITY_MANAGER", true);
        environment.set(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, jbpmEm);

        em = jbpmEm;

        final JbpmServicesPersistenceManager persistenceManager = (JbpmServicesPersistenceManager) SpringContextSingleton.getBean("jbpmServicesPersistenceManager");

        boolean success = false;
        try {
            T r = execute();
            success = true;
            return r;
        } finally {
            if (taskService != null) {
                if (success) {
                    persistenceManager.endTransaction(localTransactionOwner);
                } else {
                    persistenceManager.rollBackTransaction(localTransactionOwner);
                }
            }
        }
    }

    public abstract T execute();


    protected WorkflowDefinition getWorkflowDefinitionById(String id, Locale uiLocale, KieBase kieBase) {
        org.kie.api.definition.process.Process process = kieBase.getProcess(id);
        return convertToWorkflowDefinition(process, uiLocale);
    }

    protected Set<WorkflowAction> getAvailableActions(KieSession ksession, TaskService taskService, String processId, Locale uiLocale) {
        Set<WorkflowAction> workflowActions = new HashSet<WorkflowAction>();
        List<TaskSummary> taskSummaries = taskService.getTasksByStatusByProcessInstanceId(Long.parseLong(processId), JBPM6WorkflowProvider.OPEN_STATUS_LIST, "en");
        for (TaskSummary taskSummary : taskSummaries) {
            Task task = taskService.getTaskById(taskSummary.getId());
            WorkflowAction workflowAction = convertToWorkflowTask(task, uiLocale, ksession, taskService);
            workflowActions.add(workflowAction);
        }
        return workflowActions;
    }

    protected List<WorkflowTask> convertToWorkflowTasks(Locale uiLocale, List<TaskSummary> taskSummaryList, KieSession ksession, TaskService taskService) {
        List<WorkflowTask> availableTasks = new LinkedList<WorkflowTask>();
        for (TaskSummary taskSummary : taskSummaryList) {
            try {
                Task task = taskService.getTaskById(taskSummary.getId());
                WorkflowTask workflowTask = convertToWorkflowTask(task, uiLocale, ksession, taskService);
                availableTasks.add(workflowTask);
            } catch (Exception e) {
                logger.debug("Cannot get task " + taskSummary.getName() + " for user", e);
            }
        }
        return availableTasks;
    }


    protected WorkflowDefinition convertToWorkflowDefinition(org.kie.api.definition.process.Process process, Locale uiLocale) {
        WorkflowDefinition wf = new WorkflowDefinition(process.getName(), process.getName(), this.key);
        WorkflowProcess workflowProcess = (WorkflowProcess) process;

        String startFormName = workflowService.getFormForAction(wf.getKey(), "start");
        wf.setFormResourceName(startFormName);
        wf.setPackageName(process.getPackageName());

        Node[] nodes = workflowProcess.getNodes();

        final Set<String> tasks = new LinkedHashSet<String>();
        tasks.add(WorkflowService.START_ROLE);
        for (Node node : nodes) {
            if (node instanceof HumanTaskNode) {
                tasks.add(node.getName());
            }
        }
        wf.setTasks(tasks);

        if (uiLocale != null) {
            try {
                ResourceBundle resourceBundle = getResourceBundle(uiLocale, process.getPackageName(), wf.getKey());
                wf.setDisplayName(resourceBundle.getString("name"));
            } catch (Exception e) {
                wf.setDisplayName(wf.getName());
            }
        }

        return wf;
    }

    protected Workflow convertToWorkflow(ProcessInstance instance, Locale uiLocale, KieSession ksession, TaskService taskService, AuditLogService auditLogService) {
        WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) instance;
        final Workflow workflow = new Workflow(instance.getProcessName(), Long.toString(instance.getId()), key);
        final WorkflowDefinition definition = getWorkflowDefinitionById(instance.getProcessId(), uiLocale, ksession.getKieBase());
        workflow.setWorkflowDefinition(definition);
        workflow.setAvailableActions(getAvailableActions(ksession, taskService, Long.toString(instance.getId()), uiLocale));
        /*
        Not sure how to handle this in jBPM 6 since we don't use timers in our processes
        Job job = managementService.createJobQuery().timers().processInstanceId(instance.getId()).uniqueResult();
        if (job != null) {
            workflow.setDuedate(job.getDueDate());
        }
        */
        ProcessInstanceLog processInstanceLog =  (ProcessInstanceLog) ((ProcessInstanceImpl)instance).getMetaData().get("ProcessInstanceLog");
        if (processInstanceLog == null) {
            processInstanceLog = auditLogService.findProcessInstance(instance.getId());
        }
        workflow.setStartTime(processInstanceLog.getStart());

        Object user = workflowProcessInstance.getVariable("user");
        if (user != null) {
            workflow.setStartUser(user.toString());
        }

        workflow.setVariables(((WorkflowProcessInstanceImpl) workflowProcessInstance).getVariables());

        return workflow;
    }

    protected WorkflowTask convertToWorkflowTask(Task task, Locale uiLocale, KieSession ksession, TaskService taskService) {
        final NodeInstance taskNodeInstance = getTaskNodeInstance(task, ksession);

        WorkflowTask workflowTask = new WorkflowTask(taskNodeInstance.getNode().getName(), key);
        workflowTask.setDueDate(task.getTaskData().getExpirationTime());
//        workflowTask.setDescription(getI18NText(task.getDescriptions(), locale));
        workflowTask.setCreateTime(task.getTaskData().getCreatedOn());
        workflowTask.setProcessId(Long.toString(task.getTaskData().getProcessInstanceId()));
        if (task.getTaskData().getActualOwner() != null) {
            workflowTask.setAssignee(
                    ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByPath(task.getTaskData().getActualOwner().getId()).getJahiaUser());
        }
        workflowTask.setId(Long.toString(task.getId()));
        Set<String> connectionIds = getTaskOutcomes(taskNodeInstance.getNode());
        workflowTask.setOutcome(connectionIds);
        PeopleAssignments peopleAssignements = task.getPeopleAssignments();
        List<WorkflowParticipation> participations = new ArrayList<WorkflowParticipation>();
        if (peopleAssignements.getPotentialOwners().size() > 0) {
            for (OrganizationalEntity organizationalEntity : peopleAssignements.getPotentialOwners()) {
                if (organizationalEntity instanceof Group) {
                    Group group = (Group) organizationalEntity;
                    JCRGroupNode jcrGroup = groupManager.lookupGroupByPath(group.getId());
                    if (jcrGroup != null) {
                        participations.add(new WorkflowParticipation(WorkflowService.CANDIDATE, jcrGroup
                                .getJahiaGroup()));
                    } else {
                        logger.warn("Unable to find group {} as a task assignment candidate for task. Skipping it.",
                                group.getId());
                    }
                } else {
                    if (organizationalEntity instanceof User) {
                        User user = (User) organizationalEntity;
                        JCRUserNode jcrUser = userManager.lookupUserByPath(user.getId());
                        if (jcrUser != null) {
                            participations.add(new WorkflowParticipation(WorkflowService.CANDIDATE, jcrUser
                                    .getJahiaUser()));
                        } else {
                            logger.warn("Unable to find user {} as a task assignment candidate for task. Skipping it.",
                                    user.getId());
                        }
                    }
                }
            }
        }
        workflowTask.setParticipations(participations);
        // Get form resource name
        long contentId = task.getTaskData().getDocumentContentId();
        Content taskContent = taskService.getContentById(contentId);
        Object contentData = ContentMarshallerHelper.unmarshall(taskContent.getContent(), ksession.getEnvironment());
        if (contentData instanceof Map) {
            Map<String, Object> taskParameters = (Map<String, Object>) contentData;
            workflowTask.setVariables(taskParameters);
        }

        // Get Tasks variables
        final ProcessInstance instance = ksession.getProcessInstance(task.getTaskData().getProcessInstanceId());
        if (instance != null) {
            final WorkflowDefinition definition = getWorkflowDefinitionById(instance.getProcessId(), uiLocale, ksession.getKieBase());
            workflowTask.setWorkflowDefinition(definition);
            i18nOfWorkflowAction(uiLocale, workflowTask, definition.getKey(), definition.getPackageName());
            workflowTask.setFormResourceName(workflowService.getFormForAction(definition.getKey(), workflowTask.getName()));
            // ((TaskImpl)task).getFormName()
        }
        return workflowTask;
    }

    protected NodeInstance getTaskNodeInstance(final Task task, final KieSession ksession) {
        WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) ksession.getProcessInstance(task.getTaskData().getProcessInstanceId());
        final long workItemId = task.getTaskData().getWorkItemId();
        NodeInstance taskNodeInstance = null;
        for (NodeInstance nodeInstance : workflowProcessInstance.getNodeInstances()) {
            if (nodeInstance instanceof WorkItemNodeInstance) {
                WorkItemNodeInstance workItemNodeInstance = (WorkItemNodeInstance) nodeInstance;
                if (workItemNodeInstance.getWorkItem().getId() == workItemId) {
                    taskNodeInstance = nodeInstance;
                    break;
                }
            }
        }
        return taskNodeInstance;
    }

    protected Set<String> getTaskOutcomes(final Node node) {
        Set<String> connectionIds = new LinkedHashSet<String>();
        if (node != null) {
            getOutgoingConnectionNames(connectionIds, node);
        }
        return connectionIds;
    }

    protected void getOutgoingConnectionNames(Set<String> connectionIds, Node node) {
        Map<String, List<Connection>> outgoingConnections = node.getOutgoingConnections();
        for (Map.Entry<String, List<Connection>> outgoingConnectionEntry : outgoingConnections.entrySet()) {
            for (Connection connection : outgoingConnectionEntry.getValue()) {
                if (connection.getTo() instanceof Split) {
                    connectionIds.addAll(getConstraintNamesOrderedByPriority(((Split) connection.getTo()).getConstraints().values()));
                } else {
                    String uniqueId = (String) connection.getMetaData().get("UniqueId");
                    connectionIds.add(uniqueId);
                }
            }
        }
    }

    private Collection<String> getConstraintNamesOrderedByPriority(Collection<Constraint> constraints) {
        List<Constraint> orderedConstraints = new LinkedList<>(constraints);

        Collections.sort(orderedConstraints, CONSTRAINT_PRIORITY_COMPARATOR);

        return orderedConstraints.stream().map(c -> c.getName()).collect(Collectors.toList());
    }

    protected void updateTaskNode(final JahiaUser user, final String taskUuid) {
        if (taskUuid != null) {
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, null, null, new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRNodeWrapper nodeByUUID = session.getNodeByUUID(taskUuid);
                        if (user != null) {
                            if (!nodeByUUID.hasProperty("assigneeUserKey") ||
                                    !nodeByUUID.getProperty("assigneeUserKey").getString().equals(user.getName())) {
                                nodeByUUID.setProperty("assigneeUserKey", user.getLocalPath());
                                session.save();
                            }
                        } else {
                            if (nodeByUUID.hasProperty("assigneeUserKey")) {
                                nodeByUUID.getProperty("assigneeUserKey").remove();
                                session.save();
                            }
                        }
                        return null;
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Cannot update task",e);
            }
        }
    }

    protected Map<String, Object> getTaskOutputParameters(Task task, Map<String, Object> taskInputParameters, KieSession ksession, TaskService taskService) {
        Map<String, Object> taskOutputParameters = null;
        if (taskInputParameters != null) {
            Content taskOutputContent = taskService.getContentById(task.getTaskData().getOutputContentId());
            if (taskOutputContent == null) {
                taskOutputParameters = new LinkedHashMap<String, Object>(taskInputParameters);
            } else {
                Object outputContentData = ContentMarshallerHelper.unmarshall(taskOutputContent.getContent(), ksession.getEnvironment());
                if (outputContentData instanceof Map) {
                    taskOutputParameters = (Map<String, Object>) outputContentData;
                }
            }
        }
        return taskOutputParameters;
    }

    protected Map<String, Object> getTaskInputParameters(Task task, KieSession ksession, TaskService taskService) {
        Content taskInputContent = taskService.getContentById(task.getTaskData().getDocumentContentId());
        Object inputContentData = ContentMarshallerHelper.unmarshall(taskInputContent.getContent(), ksession.getEnvironment());
        Map<String, Object> taskInputParameters = null;
        if (inputContentData instanceof Map) {
            taskInputParameters = (Map<String, Object>) inputContentData;
        }
        return taskInputParameters;
    }

    protected boolean checkParticipation(Task task, JahiaUser user) {
        PeopleAssignments peopleAssignments = task.getPeopleAssignments();
        List<OrganizationalEntity> potentialOwners = peopleAssignments.getPotentialOwners();
        if (potentialOwners == null || potentialOwners.isEmpty()) {
            return true;
        }
        for (OrganizationalEntity potentialOwner : potentialOwners) {
            if (potentialOwner instanceof User) {
                if (user.getUserKey().equals(potentialOwner.getId())) {
                    return true;
                }
            } else if (potentialOwner instanceof Group) {
                if (groupManager.getMembershipByPath(user.getLocalPath()).contains(potentialOwner.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected ResourceBundle getResourceBundle(Locale uiLocale, String packageName, final String definitionKey) {
        try {
            if (workflowService.getModuleForWorkflow(definitionKey) != null) {
                JahiaTemplatesPackage module = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(workflowService.getModuleForWorkflow(definitionKey));
                return ResourceBundles
                        .get(packageName + "." + StringUtils.replace(definitionKey, " ", ""), module, uiLocale);
            }
            return ResourceBundles
                    .get("org.jahia.modules.workflow." + StringUtils.replace(definitionKey, " ", ""), uiLocale);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    protected void i18nOfWorkflowAction(Locale uiLocale, WorkflowAction workflowAction, final String definitionKey, String packageName) {
        ResourceBundle resourceBundle = null;
        if (uiLocale != null) {
            resourceBundle = getResourceBundle(uiLocale, packageName, definitionKey);
            String rbActionName = i18nName(workflowAction.getName(), resourceBundle);
            workflowAction.setDisplayName(rbActionName);
        }
        if (workflowAction instanceof WorkflowTask) {
            WorkflowTask workflowTask = (WorkflowTask) workflowAction;
            Set<String> outcomes = workflowTask.getOutcomes();
            List<String> displayOutcomes = new LinkedList<String>();
            List<String> outcomeIcons = new LinkedList<String>();
            for (String outcome : outcomes) {
                String s = outcome;
                String icon = null;
                if (resourceBundle != null) {
                    String key = (StringUtils.replace(workflowAction.getName(), ' ', '.').trim() + "." + StringUtils
                            .replace(outcome, ' ', '.').trim()).toLowerCase();
                    try {
                        s = resourceBundle.getString(key);
                    } catch (Exception e) {
                        logger.warn("Missing ressource : " + key + " in " + resourceBundle);
                    }
                    try {
                        icon = resourceBundle.getString(key + ".icon");
                    } catch (MissingResourceException e) {
                        // ignore;
                    }
                }
                displayOutcomes.add(s);
                outcomeIcons.add(icon);
            }
            workflowTask.setDisplayOutcomes(displayOutcomes);
            workflowTask.setOutcomeIcons(outcomeIcons);
        }
    }

    protected String i18nName(String actionName, ResourceBundle resourceBundle) {
        if (resourceBundle != null) {
            String key = StringUtils.replace(actionName, ' ', '.').trim().toLowerCase();
            try {
                actionName = resourceBundle.getString(key);
            } catch (MissingResourceException e) {
                logger.warn("Missing ressource : " + key + " in " + resourceBundle);
            }
        }
        return actionName;
    }

    protected List<HistoryWorkflow> getHistoryWorkflows(List<String> processIds, Locale uiLocale) {
        List<HistoryWorkflow> historyWorkflows = new ArrayList<HistoryWorkflow>();
        for (String processId : processIds) {
            ProcessInstanceLog processInstanceLog = getLogService().findProcessInstance(Long.parseLong(processId));
            List<VariableInstanceLog> nodeIdVariableInstanceLogs = getLogService().findVariableInstances(Long.parseLong(processId), "nodeId");
            String nodeId = null;
            if (nodeIdVariableInstanceLogs.size() > 0) {
                nodeId = nodeIdVariableInstanceLogs.get(0).getValue();
            }
            String user = null;
            List<VariableInstanceLog> userVariableInstanceLogs = getLogService().findVariableInstances(Long.parseLong(processId), "user");
            if (userVariableInstanceLogs.size() > 0) {
                user = userVariableInstanceLogs.get(0).getValue();
            }
            final HistoryWorkflow historyWorkflow = new HistoryWorkflow(Long.toString(processInstanceLog.getProcessInstanceId()),
                    getWorkflowDefinitionById(processInstanceLog.getProcessId(), uiLocale, getKieSession().getKieBase()),
                    processInstanceLog.getProcessName(),
                    getKey(),
                    user,
                    processInstanceLog.getStart(),
                    processInstanceLog.getEnd(),
                    processInstanceLog.getOutcome(),
                    nodeId
            );
            historyWorkflow.setDisplayName(historyWorkflow.getWorkflowDefinition().getDisplayName());
            historyWorkflows.add(historyWorkflow);
        }
        return historyWorkflows;
    }


}
