package org.jahia.services.workflow.jbpm;

import org.apache.jackrabbit.util.ISO9075;
import org.codehaus.plexus.util.StringUtils;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.drools.core.impl.EnvironmentFactory;
import org.drools.persistence.PersistenceContextManager;
import org.drools.persistence.TransactionManager;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.pipelines.Pipeline;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.*;
import org.jahia.services.workflow.jbpm.custom.AbstractTaskLifeCycleEventListener;
import org.jahia.utils.i18n.ResourceBundles;
import org.jbpm.process.audit.*;
import org.jbpm.process.audit.command.AbstractHistoryLogCommand;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.runtime.manager.impl.RuntimeEnvironmentBuilder;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.command.Command;
import org.kie.api.definition.process.*;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.*;
import org.kie.internal.command.Context;
import org.kie.internal.runtime.manager.RuntimeEnvironment;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.task.api.EventService;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.spring.persistence.KieSpringJpaManager;
import org.kie.spring.persistence.KieSpringTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

import javax.jcr.RepositoryException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.io.IOException;
import java.util.*;

/**
 * jBPM 6 Workflow Provider implementation
 */
public class JBPM6WorkflowProvider implements WorkflowProvider,
        InitializingBean,
        WorkflowObservationManagerAware {

    public static final List<Status> OPEN_STATUS_LIST = Arrays.asList(Status.Created, Status.InProgress, Status.Ready, Status.Reserved);
    private static final List<Status> OPEN_STATUS_LIST_NON_RESERVED = Arrays.asList(Status.Created, Status.InProgress, Status.Ready);
    private static final List<Status> RESERVED_STATUS_LIST = Arrays.asList(Status.Reserved);
    
    private transient static Logger logger = LoggerFactory.getLogger(JBPM6WorkflowProvider.class);
    private transient static JBPM6WorkflowProvider instance = new JBPM6WorkflowProvider();

    private String key;
    private WorkflowService workflowService;
    private WorkflowObservationManager observationManager;
    private JahiaUserManagerService userManager;
    private JahiaGroupManagerService groupManager;
    private KieRepository kieRepository;
    private KieServices kieServices;
    private KieFileSystem kieFileSystem;
    private KieSession kieSession;
    private KieBase kieBase;
    private TaskService taskService;
    private JBPMListener listener = new JBPMListener(this);
    private RuntimeManager runtimeManager;
    private RuntimeEngine runtimeEngine;
    private AbstractPlatformTransactionManager platformTransactionManager;
    private EntityManagerFactory emf;
    private EntityManager em;
    private Map<String, WorkItemHandler> workItemHandlers = new TreeMap<String, WorkItemHandler>();
    private Map<String, AbstractTaskLifeCycleEventListener> taskLifeCycleEventListeners = new TreeMap<String, AbstractTaskLifeCycleEventListener>();
    private Pipeline peopleAssignmentPipeline;
    private JahiaUserGroupCallback jahiaUserGroupCallback;

    public static JBPM6WorkflowProvider getInstance() {
        return instance;
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

    public void setWorkflowObservationManager(WorkflowObservationManager observationManager) {
        this.observationManager = observationManager;
        listener.setObservationManager(observationManager);
    }

    public void setGroupManager(JahiaGroupManagerService groupManager) {
        this.groupManager = groupManager;
    }

    public void setUserManager(JahiaUserManagerService userManager) {
        this.userManager = userManager;
    }

    public void setPeopleAssignmentPipeline(Pipeline peopleAssignmentPipeline) {
        this.peopleAssignmentPipeline = peopleAssignmentPipeline;
    }

    public void setJahiaUserGroupCallback(JahiaUserGroupCallback jahiaUserGroupCallback) {
        this.jahiaUserGroupCallback = jahiaUserGroupCallback;
    }

    public KieRepository getKieRepository() {
        return kieRepository;
    }

    public void setPlatformTransactionManager(AbstractPlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    public void setEmf(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public void registerWorkItemHandler(String name, WorkItemHandler workItemHandler) {
        workItemHandlers.put(name, workItemHandler);
        if (kieSession != null) {
            kieSession.getWorkItemManager().registerWorkItemHandler(name,workItemHandler);
        }
    }

    public WorkItemHandler unregisterWorkItemHandler(String name) {
        if (kieSession != null) {
            kieSession.getWorkItemManager().registerWorkItemHandler(name, null);
        }
        return workItemHandlers.remove(name);
    }

    public void registerTaskLifeCycleEventListener(String name, AbstractTaskLifeCycleEventListener taskAssignmentListener) {
        taskLifeCycleEventListeners.put(name, taskAssignmentListener);
    }

    public AbstractTaskLifeCycleEventListener unregisterTaskLifeCycleEventListener(String name) {
        return taskLifeCycleEventListeners.remove(name);
    }

    public void start() {

        kieServices = KieServices.Factory.get();
        kieRepository = kieServices.getRepository();
        kieFileSystem = kieServices.newKieFileSystem();

        recompilePackages();

        workflowService.addProvider(this);
    }

    public void stop() {
        workflowService.removeProvider(this);

        kieSession.dispose();

        // @todo implement clean shutdown of all jBPM & Drools environment
        runtimeManager.disposeRuntimeEngine(runtimeEngine);
        runtimeManager.close();
    }

    private synchronized KieSession getKieSession() {
        if (kieSession != null) {
            return kieSession;
        }
        kieSession = runtimeEngine.getKieSession();

        for (Map.Entry<String, WorkItemHandler> workItemHandlerEntry : workItemHandlers.entrySet()) {
            kieSession.getWorkItemManager().registerWorkItemHandler(workItemHandlerEntry.getKey(), workItemHandlerEntry.getValue());
        }

        for (Map.Entry<String, AbstractTaskLifeCycleEventListener> taskLifeCycleEventListenerEntry : taskLifeCycleEventListeners.entrySet()) {
            AbstractTaskLifeCycleEventListener taskLifeCycleEventListener = taskLifeCycleEventListenerEntry.getValue();
            taskLifeCycleEventListener.setEnvironment(kieSession.getEnvironment());
            taskLifeCycleEventListener.setObservationManager(observationManager);
            taskLifeCycleEventListener.setTaskService(taskService);
            if (taskService instanceof EventService) {
                ((EventService) taskService).registerTaskLifecycleEventListener(taskLifeCycleEventListener);
            }
        }

        Map<String, Object> pipelineEnvironment = new HashMap<String, Object>();
        pipelineEnvironment.put(AbstractPeopleAssignmentValve.ENV_JBPM_WORKFLOW_PROVIDER, this);
        peopleAssignmentPipeline.setEnvironment(pipelineEnvironment);

        kieSession.addEventListener(new JBPMListener(this));

        return kieSession;
    }

    private synchronized KieBase getKieBase() {
        if (kieBase == null) {
            kieBase = getKieSession().getKieBase();
        }
        return kieBase;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<WorkflowDefinition> getAvailableWorkflows(final Locale uiLocale) {
        KieBase kieBase = getKieBase();
        Collection<org.kie.api.definition.process.Process> processes = kieBase.getProcesses();
        List<WorkflowDefinition> workflowDefinitions = new ArrayList<WorkflowDefinition>();
        for (org.kie.api.definition.process.Process process : processes) {
            if (workflowService.getWorkflowRegistration(process.getName()) != null) {
                workflowDefinitions.add(convertToWorkflowDefinition(process, uiLocale));
            }
        }
        return workflowDefinitions;
    }

    @Override
    public WorkflowDefinition getWorkflowDefinitionByKey(final String key, final Locale uiLocale) {
        Collection<org.kie.api.definition.process.Process> processes = getKieBase().getProcesses();
        for (org.kie.api.definition.process.Process process : processes) {
            if (process.getName().equals(key)) {
                return convertToWorkflowDefinition(process, uiLocale);
            }
        }
        return null;
    }

    @Override
    public List<Workflow> getActiveWorkflowsInformations(final List<String> processIds, final Locale uiLocale) {
        return executeCommand(new GenericCommand<List<Workflow>>() {
            @Override
            public List<Workflow> execute(Context context) {
                KieSession ksession = getKieSession();
                List<Workflow> activeWorkflows = new ArrayList<Workflow>();
                for (String s : processIds) {
                    activeWorkflows.add(convertToWorkflow(ksession.getProcessInstance(Long.parseLong(s)), uiLocale, ksession));
                }
                return activeWorkflows;
            }
        });
    }

    @Override
    public String startProcess(String processKey, Map<String, Object> args) {
        ProcessInstance processInstance = getKieSession().startProcess(getEncodedProcessKey(processKey), args);
        return Long.toString(processInstance.getId());
    }

    public WorkflowDefinition getWorkflowDefinitionById(final String id, final Locale uiLocale) {
        return getWorkflowDefinitionById(id, uiLocale, getKieBase());
    }

    private WorkflowDefinition getWorkflowDefinitionById(String id, Locale uiLocale, KieBase kieBase) {
        org.kie.api.definition.process.Process process = kieBase.getProcess(id);
        return convertToWorkflowDefinition(process, uiLocale);
    }

    /**
     * This method is used to handle process IDs that start with a number, in order to retain compatibility with
     * older versions of Jahia that used process IDs that start with such characters
     *
     * @param processKey
     * @return
     */
    public static String getEncodedProcessKey(String processKey) {
        if (Character.isDigit(processKey.charAt(0))) {
            processKey = ISO9075.encode(processKey);
        }
        return processKey;
    }

    /**
     * This method is used to decode an ISO9075-encoded process key, since these need to be encoded when used
     * in XML files or even in some attribute values that enforce XML id formats.
     *
     * @param processKey
     * @return
     */
    public static String getDecodedProcessKey(String processKey) {
        return ISO9075.decode(processKey);
    }

    @Override
    public void abortProcess(String processId) {
        getKieSession().abortProcessInstance(Long.parseLong(processId));
    }

    @Override
    public Workflow getWorkflow(final String processId, final Locale uiLocale) {
        return executeCommand(new GenericCommand<Workflow>() {
            @Override
            public Workflow execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                ProcessInstance processInstance = ksession.getProcessInstance(Long.parseLong(processId));
                return convertToWorkflow(processInstance, uiLocale, ksession);
            }
        });
    }

    @Override
    public Set<WorkflowAction> getAvailableActions(final String processId, final Locale uiLocale) {
        return executeCommand(new GenericCommand<Set<WorkflowAction>>() {
            private static final long serialVersionUID = 7885301164037826410L;

            @Override
            public Set<WorkflowAction> execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();

                Set<WorkflowAction> workflowActions = new HashSet<WorkflowAction>();
                List<TaskSummary> taskSummaries = taskService.getTasksByStatusByProcessInstanceId(Long.parseLong(processId), OPEN_STATUS_LIST, "en");
                for (TaskSummary taskSummary : taskSummaries) {
                    Task task = taskService.getTaskById(taskSummary.getId());
                    WorkflowAction workflowAction = convertToWorkflowTask(task, uiLocale, ksession);
                    workflowActions.add(workflowAction);
                }

                return workflowActions;

            }
        });
    }

    @Override
    public List<WorkflowTask> getTasksForUser(final JahiaUser user, final Locale uiLocale) {
        return executeCommand(new GenericCommand<List<WorkflowTask>>() {
            @Override
            public List<WorkflowTask> execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                List<WorkflowTask> availableTasks = new ArrayList<WorkflowTask>();
                List<TaskSummary> tasksOwned = taskService.getTasksOwnedByStatus(user.getUserKey(), RESERVED_STATUS_LIST, "en");
                if (tasksOwned != null && tasksOwned.size() > 0) {
                    availableTasks.addAll(convertToWorkflowTasks(uiLocale, tasksOwned, ksession));
                }
                // how do we retrieve group tasks ?
                List<TaskSummary> potentialOwnerTasks = taskService.getTasksAssignedAsPotentialOwnerByStatus(user.getUserKey(), OPEN_STATUS_LIST_NON_RESERVED, "en");
                if (potentialOwnerTasks != null && potentialOwnerTasks.size() > 0) {
                    availableTasks.addAll(convertToWorkflowTasks(uiLocale, potentialOwnerTasks, ksession));
                }
                List<TaskSummary> businessAdministratorTasks = taskService.getTasksAssignedAsBusinessAdministrator(user.getUserKey(), "en");
                if (businessAdministratorTasks != null && businessAdministratorTasks.size() > 0) {
                    availableTasks.addAll(convertToWorkflowTasks(uiLocale, businessAdministratorTasks, ksession));
                }
                return availableTasks;
            }
        });
    }

    private List<WorkflowTask> convertToWorkflowTasks(Locale uiLocale, List<TaskSummary> taskSummaryList, KieSession ksession) {
        List<WorkflowTask> availableTasks = new LinkedList<WorkflowTask>();
        for (TaskSummary taskSummary : taskSummaryList) {
            try {
                Task task = taskService.getTaskById(taskSummary.getId());
                WorkflowTask workflowTask = convertToWorkflowTask(task, uiLocale, ksession);
                availableTasks.add(workflowTask);
            } catch (Exception e) {
                logger.debug("Cannot get task " + taskSummary.getName() + " for user", e);
            }
        }
        return availableTasks;
    }

    @Override
    public List<Workflow> getWorkflowsForDefinition(final String definition, final Locale uiLocale) {
        return executeCommand(new GenericCommand<List<Workflow>>() {
            @Override
            public List<Workflow> execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                final List<Workflow> workflows = new LinkedList<Workflow>();
                Collection<ProcessInstance> processInstances = getKieSession().getProcessInstances();
                for (ProcessInstance processInstance : processInstances) {
                    if (processInstance instanceof WorkflowProcessInstance) {
                        WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) processInstance;
                        if (workflowProcessInstance.getProcessName().equals(definition)) {
                            workflows.add(convertToWorkflow(workflowProcessInstance, uiLocale, ksession));
                        }
                    }
                }
                return workflows;


            }
        });
    }

    @Override
    public List<Workflow> getWorkflowsForUser(final JahiaUser user, final Locale uiLocale) {
        return executeCommand(new GenericCommand<List<Workflow>>() {
            @Override
            public List<Workflow> execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                final List<Workflow> workflows = new LinkedList<Workflow>();
                Collection<ProcessInstance> processInstances = getKieSession().getProcessInstances();
                for (ProcessInstance processInstance : processInstances) {
                    if (processInstance instanceof WorkflowProcessInstance) {
                        WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) processInstance;
                        String userKey = (String) workflowProcessInstance.getVariable("user");
                        if (user.getUserKey().equals(userKey)) {
                            workflows.add(convertToWorkflow(processInstance, uiLocale, ksession));
                        }
                    }
                }
                return workflows;
            }
        });
    }

    private ThreadLocal<Boolean> loop = new ThreadLocal<Boolean>();

    @Override
    public void assignTask(final String taskId, final JahiaUser user) {
        if (loop.get() != null) {
            return;
        }
        try {
            executeCommand(new GenericCommand<List<WorkflowTask>>() {
                @Override
                public List<WorkflowTask> execute(Context context) {
                    loop.set(Boolean.TRUE);
                    Task task = taskService.getTaskById(Long.parseLong(taskId));
                    Map<String, Object> taskInputParameters = getTaskInputParameters(task);
                    Map<String, Object> taskOutputParameters = getTaskOutputParameters(task, taskInputParameters);
                    if (user == null) {
                        taskService.release(task.getId(), JCRSessionFactory.getInstance().getCurrentUser().getUserKey());
                    } else if (task.getTaskData().getActualOwner() != null && user.getUserKey().equals(task.getTaskData().getActualOwner().getId())) {
                        logger.debug("Cannot assign task " + task.getId() + " to user " + user.getName() + ", user is already owner");
                    } else if (!checkParticipation(task, user)) {
                        logger.error("Cannot assign task " + task.getId() + " to user " + user.getName() + ", user is not candidate");
                    } else {
                        taskService.claim(Long.parseLong(taskId), user.getUserKey());
                    }
                    JahiaUser actualUser = null;
                    if (task.getTaskData().getActualOwner() != null) {
                        actualUser = userManager.lookupUserByKey(task.getTaskData().getActualOwner().getId());
                    }
                    if (actualUser != null) {
                        taskOutputParameters.put("currentUser", user.getUserKey());
                        ((InternalTaskService) taskService).addContent(Long.parseLong(taskId), taskOutputParameters);
                    }
                    updateTaskNode(actualUser, (String) taskOutputParameters.get("task-" + taskId));
                    return null;
                }
            });
        } finally {
            loop.set(null);
        }
    }

    private void updateTaskNode(final JahiaUser user, final String taskUuid) {
        if (taskUuid != null) {
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRNodeWrapper nodeByUUID = session.getNodeByUUID(taskUuid);
                        if (user != null) {
                            if (!nodeByUUID.hasProperty("assigneeUserKey") ||
                                    !nodeByUUID.getProperty("assigneeUserKey").getString().equals(user.getName())) {
                                nodeByUUID.setProperty("assigneeUserKey", user.getName());
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

    private Map<String, Object> getTaskOutputParameters(Task task, Map<String, Object> taskInputParameters) {
        Map<String, Object> taskOutputParameters = null;
        if (taskInputParameters != null) {
            Content taskOutputContent = taskService.getContentById(task.getTaskData().getOutputContentId());
            if (taskOutputContent == null) {
                taskOutputParameters = new LinkedHashMap<String, Object>(taskInputParameters);
            } else {
                Object outputContentData = ContentMarshallerHelper.unmarshall(taskOutputContent.getContent(), getKieSession().getEnvironment());
                if (outputContentData instanceof Map) {
                    taskOutputParameters = (Map<String, Object>) outputContentData;
                }
            }
        }
        return taskOutputParameters;
    }

    private Map<String, Object> getTaskInputParameters(Task task) {
        Content taskInputContent = taskService.getContentById(task.getTaskData().getDocumentContentId());
        Object inputContentData = ContentMarshallerHelper.unmarshall(taskInputContent.getContent(), getKieSession().getEnvironment());
        Map<String, Object> taskInputParameters = null;
        if (inputContentData instanceof Map) {
            taskInputParameters = (Map<String, Object>) inputContentData;
        }
        return taskInputParameters;
    }

    private boolean checkParticipation(Task task, JahiaUser user) {
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
                if (groupManager.getUserMembership(user).contains(potentialOwner.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void completeTask(String taskId, JahiaUser jahiaUser, final String outcome, Map<String, Object> args) {

        if (loop.get() != null) {
            return;
        }
        try {
            loop.set(Boolean.TRUE);
            Task task = taskService.getTaskById(Long.parseLong(taskId));
            Map<String, Object> taskInputParameters = getTaskInputParameters(task);
            Map<String, Object> taskOutputParameters = getTaskOutputParameters(task, taskInputParameters);
            final String uuid = (String) taskOutputParameters.get("task-" + taskId);
            if (uuid != null) {
                String workspace = (String) taskInputParameters.get("workspace");
                try {
                    JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace, new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            if (!session.getNodeByUUID(uuid).hasProperty("state") ||
                                    !session.getNodeByUUID(uuid).getProperty("state").getString().equals("finished")) {
                                session.getNodeByUUID(uuid).setProperty("finalOutcome", outcome);
                                session.getNodeByUUID(uuid).setProperty("state", "finished");
                                session.save();
                            }
                            return null;
                        }
                    });
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }

            observationManager.notifyTaskEnded(getKey(), taskId);

            ClassLoader l = null;

            try {
                String module = workflowService.getModuleForWorkflow(task.getTaskData().getProcessId());
                if (module != null) {
                    l = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(module).getChainedClassLoader());
                }
                if (args == null) {
                    args = new HashMap<String, Object>();
                }
                args.put("outcome", outcome);
                taskService.start(Long.parseLong(taskId), jahiaUser.getUserKey());
                taskService.complete(Long.parseLong(taskId), jahiaUser.getUserKey(), args);
            } finally {
                if (l != null) {
                    Thread.currentThread().setContextClassLoader(l);
                }
            }

        } finally {
            loop.set(null);
        }

    }

    @Override
    public void addComment(final String processId, final String comment, final String user) {
        executeCommand(new GenericCommand() {
            @Override
            public Object execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                ProcessInstance processInstance = ksession.getProcessInstance(Long.parseLong(processId));
                WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) processInstance;
                List<WorkflowComment> comments = (List<WorkflowComment>) workflowProcessInstance.getVariable("comments");
                if ( comments == null) {
                    comments = new ArrayList<WorkflowComment>();
                }
                final WorkflowComment wfComment = new WorkflowComment();
                wfComment.setComment(comment);
                wfComment.setUser(user);
                wfComment.setTime(new Date());
                comments.add(wfComment);
                workflowProcessInstance.setVariable("comments",comments);
                return null;
            }
        });
    }

    @Override
    public WorkflowTask getWorkflowTask(final String taskId, final Locale uiLocale) {
        return executeCommand(new GenericCommand<WorkflowTask>() {
            @Override
            public WorkflowTask execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                Task task = taskService.getTaskById(Long.parseLong(taskId));
                return convertToWorkflowTask(task, uiLocale, ksession);
            }
        });
    }

    @Override
    public List<HistoryWorkflow> getHistoryWorkflowsForNode(final String nodeId, Locale uiLocale) {
        @SuppressWarnings("unchecked")
        List<VariableInstanceLog> result = em
                .createQuery("FROM VariableInstanceLog v WHERE v.variableId = :variableId AND v.value = :variableValue")
                .setParameter("variableId", "nodeId")
                .setParameter("variableValue", nodeId).getResultList();
        
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> l = new ArrayList<String>();
        for (VariableInstanceLog log : result) {
            l.add(Long.toString(log.getProcessInstanceId()));
        }

        return getHistoryWorkflows(l, uiLocale);
    }

    @Override
    public List<HistoryWorkflow> getHistoryWorkflowsForPath(String path, Locale uiLocale) {
        @SuppressWarnings("unchecked")
        List<VariableInstanceLog> result = em
                .createQuery("FROM VariableInstanceLog v WHERE v.variableId = :variableId AND v.value like :variableValue")
                .setParameter("variableId", "nodePath")
                .setParameter("variableValue", path).getResultList();

        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> l = new ArrayList<String>();
        for (VariableInstanceLog log : result) {
            l.add(Long.toString(log.getProcessInstanceId()));
        }

        return getHistoryWorkflows(l, uiLocale);
    }

    @Override
    public List<HistoryWorkflow> getHistoryWorkflows(final List<String> processIds, final Locale uiLocale) {
        return executeCommand(new AbstractHistoryLogCommand<List<HistoryWorkflow>>() {
            @Override
            public List<HistoryWorkflow> execute(Context context) {
                setLogEnvironment(context);
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                List<HistoryWorkflow> historyWorkflows = new ArrayList<HistoryWorkflow>();
                for (String processId : processIds) {
                    ProcessInstanceLog processInstanceLog = auditLogService.findProcessInstance(Long.parseLong(processId));
                    List<VariableInstanceLog> nodeIdVariableInstanceLogs = auditLogService.findVariableInstances(Long.parseLong(processId), "nodeId");
                    String nodeId = null;
                    for (VariableInstanceLog nodeIdVariableInstanceLog : nodeIdVariableInstanceLogs) {
                        nodeId = nodeIdVariableInstanceLog.getValue();
                    }
                    final HistoryWorkflow historyWorkflow = new HistoryWorkflow(Long.toString(processInstanceLog.getId()),
                            getWorkflowDefinitionById(processInstanceLog.getProcessId(), uiLocale, ksession.getKieBase()),
                            processInstanceLog.getProcessName(),
                            key,
                            processInstanceLog.getIdentity(),
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
        });
    }

    @Override
    public List<HistoryWorkflowTask> getHistoryWorkflowTasks(final String processId, final Locale uiLocale) {
        return executeCommand(new AbstractHistoryLogCommand<List<HistoryWorkflowTask>>() {
            @Override
            public List<HistoryWorkflowTask> execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();

                final List<HistoryWorkflowTask> workflowTaskHistory = new LinkedList<HistoryWorkflowTask>();

                setLogEnvironment(context);
                ProcessInstanceLog processInstanceLog = auditLogService.findProcessInstance(Long.parseLong(processId));
                List<NodeInstanceLog> nodeInstanceLogs = auditLogService.findNodeInstances(processInstanceLog.getProcessInstanceId());
                for (NodeInstanceLog nodeInstanceLog : nodeInstanceLogs) {
                    if (nodeInstanceLog.getWorkItemId() != null && "HumanTaskNode".equals(nodeInstanceLog.getNodeType())) {
                        Task task = taskService.getTaskByWorkItemId(nodeInstanceLog.getWorkItemId());
                        final HistoryWorkflowTask workflowTask = new HistoryWorkflowTask(task.getId().toString(),
                                nodeInstanceLog.getProcessId(),
                                nodeInstanceLog.getNodeName(),
                                key,
                                task.getTaskData().getActualOwner() != null ? task.getTaskData().getActualOwner().getId() : null,
                                task.getTaskData().getCreatedOn(),
                                nodeInstanceLog.getDate(),
                                "outcome");

                        if (uiLocale != null) {
                            final WorkflowDefinition definition = getWorkflowDefinitionById(nodeInstanceLog.getProcessId(), uiLocale, ksession.getKieBase());
                            ResourceBundle resourceBundle = getResourceBundle(uiLocale, definition.getPackageName(), definition.getKey());
                            String rbActionName = i18nName(workflowTask.getName(), resourceBundle);
                            workflowTask.setDisplayName(rbActionName);
                        }
                        workflowTaskHistory.add(workflowTask);
                    }
                }

                List<TaskSummary> tasksIds = taskService.getTasksByStatusByProcessInstanceId(Long.parseLong(processId), OPEN_STATUS_LIST, "en");
                for (TaskSummary taskSummary : tasksIds) {
                    final HistoryWorkflowTask workflowTask = new HistoryWorkflowTask(Long.toString(taskSummary.getId()),
                            Long.toString(taskSummary.getProcessInstanceId()),
                            taskSummary.getName(),
                            key,
                            null, //taskSummary.getActualOwner() != null ? taskSummary.getActualOwner().getId() : null,
                            taskSummary.getCreatedOn(),
                            null,
                            null);
                    workflowTaskHistory.add(workflowTask);
                    if (uiLocale != null) {
                        final WorkflowDefinition definition = getWorkflowDefinitionById(taskSummary.getProcessId(), uiLocale, ksession.getKieBase());
                        ResourceBundle resourceBundle = getResourceBundle(uiLocale, definition.getPackageName(), definition.getKey());
                        String rbActionName = i18nName(workflowTask.getName(), resourceBundle);
                        workflowTask.setDisplayName(rbActionName);
                    }
                }

                return workflowTaskHistory;
            }
        });
    }

    @Override
    public void deleteProcess(String processId) {
        // do nothing
    }

    private WorkflowDefinition convertToWorkflowDefinition(org.kie.api.definition.process.Process process, Locale uiLocale) {
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

    private Workflow convertToWorkflow(ProcessInstance instance, Locale uiLocale, KieSession ksession) {
        WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) instance;
        final Workflow workflow = new Workflow(instance.getProcessName(), Long.toString(instance.getId()), key);
        final WorkflowDefinition definition = getWorkflowDefinitionById(instance.getProcessId(), uiLocale, ksession.getKieBase());
        workflow.setWorkflowDefinition(definition);
        workflow.setAvailableActions(getAvailableActions(Long.toString(instance.getId()), uiLocale));
        /*
        Not sure how to handle this in jBPM 6 since we don't use timers in our processes
        Job job = managementService.createJobQuery().timers().processInstanceId(instance.getId()).uniqueResult();
        if (job != null) {
            workflow.setDuedate(job.getDueDate());
        }
        */
        ProcessInstanceLog processInstanceLog =  (ProcessInstanceLog) ((ProcessInstanceImpl)instance).getMetaData().get("ProcessInstanceLog");
        if (processInstanceLog == null) {
            AuditLogService auditLogService = new JPAAuditLogService(kieSession.getEnvironment());
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

    private WorkflowTask convertToWorkflowTask(Task task, Locale uiLocale, KieSession ksession) {
        final NodeInstance taskNodeInstance = getTaskNodeInstance(task, ksession);

        WorkflowTask workflowTask = new WorkflowTask(taskNodeInstance.getNode().getName(), key);
        workflowTask.setDueDate(task.getTaskData().getExpirationTime());
//        workflowTask.setDescription(getI18NText(task.getDescriptions(), locale));
        workflowTask.setCreateTime(task.getTaskData().getCreatedOn());
        workflowTask.setProcessId(Long.toString(task.getTaskData().getProcessInstanceId()));
        if (task.getTaskData().getActualOwner() != null) {
            workflowTask.setAssignee(
                    ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(task.getTaskData().getActualOwner().getId()));
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
                    participations
                            .add(new WorkflowParticipation(WorkflowService.CANDIDATE,
                                    groupManager.lookupGroup(group.getId())));
                } else {
                    if (organizationalEntity instanceof User) {
                        User user = (User) organizationalEntity;
                        participations
                                .add(new WorkflowParticipation(WorkflowService.CANDIDATE,
                                        userManager.lookupUserByKey(user.getId())));
                    }
                }
            }
        }
        workflowTask.setParticipations(participations);
        // Get form resource name
        long contentId = task.getTaskData().getDocumentContentId();
        Content taskContent = taskService.getContentById(contentId);
        Object contentData = ContentMarshallerHelper.unmarshall(taskContent.getContent(), getKieSession().getEnvironment());
        if (contentData instanceof Map) {
            Map<String, Object> taskParameters = (Map<String, Object>) contentData;
            workflowTask.setVariables(taskParameters);
        }

        // Get Tasks variables
        final ProcessInstance instance = getKieSession().getProcessInstance(task.getTaskData().getProcessInstanceId());
        if (instance != null) {
            final WorkflowDefinition definition = getWorkflowDefinitionById(instance.getProcessId(), uiLocale, ksession.getKieBase());
            workflowTask.setWorkflowDefinition(definition);
            i18nOfWorkflowAction(uiLocale, workflowTask, definition.getKey(), definition.getPackageName());
            workflowTask.setFormResourceName(workflowService.getFormForAction(definition.getKey(), workflowTask.getName()));
            // ((TaskImpl)task).getFormName()
        }
        return workflowTask;
    }

    private NodeInstance getTaskNodeInstance(final Task task, final KieSession ksession) {
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

    private Set<String> getTaskOutcomes(final Node node) {
        Set<String> connectionIds = new TreeSet<String>();
        if (node != null) {
            getOutgoingConnectionNames(connectionIds, node);
        }
        return connectionIds;
    }

    private void getOutgoingConnectionNames(Set<String> connectionIds, Node node) {
        Map<String, List<Connection>> outgoingConnections = node.getOutgoingConnections();
        for (Map.Entry<String, List<Connection>> outgoingConnectionEntry : outgoingConnections.entrySet()) {
            for (Connection connection : outgoingConnectionEntry.getValue()) {
                if (connection.getTo() instanceof Split) {
                    for (Constraint constraint : ((Split) connection.getTo()).getConstraints().values()) {
                        connectionIds.add(constraint.getName());
                    }
                } else {
                    String uniqueId = (String) connection.getMetaData().get("UniqueId");
                    connectionIds.add(uniqueId);
                }
            }
        }
    }

    private ResourceBundle getResourceBundle(Locale uiLocale, String packageName, final String definitionKey) {
        try {
            if (workflowService.getModuleForWorkflow(definitionKey) != null) {
                JahiaTemplatesPackage module = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(workflowService.getModuleForWorkflow(definitionKey));
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

    private void i18nOfWorkflowAction(Locale uiLocale, WorkflowAction workflowAction, final String definitionKey, String packageName) {
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

    private String i18nName(String actionName, ResourceBundle resourceBundle) {
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

    private <T> T executeCommand(Command<T> t) {
        CommandBasedStatefulKnowledgeSession s = (CommandBasedStatefulKnowledgeSession) getKieSession();
        return s.execute(t);
    }

    public synchronized void addResource(Resource kieResource) throws IOException {
        kieFileSystem.write(kieServices.getResources().newUrlResource(kieResource.getURL()));
    }

    public synchronized void removeResource(Resource kieResource) throws IOException {
        kieFileSystem.delete(kieResource.getURL().getPath());
    }

    public synchronized void recompilePackages() {
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        KieContainer kieContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());

        TransactionManager transactionManager = new KieSpringTransactionManager(platformTransactionManager);
        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.APP_SCOPED_ENTITY_MANAGER, em);
        env.set(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, em);
        env.set("IS_JTA_TRANSACTION", false);
        env.set("IS_SHARED_ENTITY_MANAGER", true);

        env.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);
        PersistenceContextManager persistenceContextManager = new KieSpringJpaManager(env);
        env.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, persistenceContextManager);
        RuntimeEnvironment runtimeEnvironment = RuntimeEnvironmentBuilder.getDefault()
                .entityManagerFactory(emf)
                .addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, transactionManager)
                .addEnvironmentEntry(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, persistenceContextManager)
                        // .userGroupCallback(userGroupCallback)
                        // .addAsset(ResourceFactory.newClassPathResource(process), ResourceType.BPMN2)
                .knowledgeBase(kieContainer.getKieBase())
                .classLoader(kieContainer.getClassLoader())
                .registerableItemsFactory(new JahiaKModuleRegisterableItemsFactory(kieContainer, null, peopleAssignmentPipeline))
                .userGroupCallback(jahiaUserGroupCallback)
                .get();
        if (runtimeManager != null) {
            runtimeManager.close();
        }

        runtimeManager = JahiaRuntimeManagerFactoryImpl.getInstance().newSingletonRuntimeManager(runtimeEnvironment);
        runtimeEngine = runtimeManager.getRuntimeEngine(EmptyContext.get());
        taskService = runtimeEngine.getTaskService();
        kieBase = null;
        kieSession = null;
    }


//    public void addKieModule(KieModule kieModule) {
//        getKieRepository().addKieModule(kieModule);
//        // @todo add refresh of session, runtime manager
//
//        kieModules.put(kieModule.getReleaseId(), kieModule);
//        List<KieModule> kieModuleList = new ArrayList<KieModule>();
//        kieBuilder.setDependencies(kieModuleList.toArray(new KieModule[kieModuleList.size()]));
//        kieBuilder.buildAll();
//    }
}
