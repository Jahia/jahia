package org.jahia.services.workflow.jbpm;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.drools.core.impl.EnvironmentFactory;
import org.drools.persistence.PersistenceContextManager;
import org.drools.persistence.TransactionManager;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.pipelines.Pipeline;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.*;
import org.jahia.services.workflow.jbpm.custom.AbstractTaskLifeCycleEventListener;
import org.jahia.services.workflow.jbpm.custom.email.AddressTemplate;
import org.jahia.services.workflow.jbpm.custom.email.MailTemplate;
import org.jahia.services.workflow.jbpm.custom.email.MailTemplateRegistry;
import org.jahia.utils.Patterns;
import org.jahia.utils.i18n.ResourceBundles;
import org.jbpm.process.audit.*;
import org.jbpm.process.audit.command.AbstractHistoryLogCommand;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.runtime.manager.impl.RuntimeEnvironmentBuilder;
import org.jbpm.services.task.impl.TaskServiceEntryPointImpl;
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
import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.WorkflowProcess;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * jBPM 6 Workflow Provider implementation
 */
public class JBPM6WorkflowProvider implements WorkflowProvider,
        InitializingBean,
        WorkflowObservationManagerAware {

    public static final List<Status> OPEN_STATUS_LIST = Arrays.asList(Status.Created, Status.InProgress, Status.Ready, Status.Reserved);
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
    private KieBuilder kieBuilder;
    private KieContainer kieContainer;
    private KieSession kieSession;
    private TaskService taskService;
    private JBPMListener listener = new JBPMListener(this);
    private Resource[] processes;
    private Resource[] mailTemplates;
    private MailTemplateRegistry mailTemplateRegistry;
    private RuntimeManager runtimeManager;
    private RuntimeEngine runtimeEngine;
    private AbstractPlatformTransactionManager platformTransactionManager;
    private EntityManagerFactory emf;
    private EntityManager em;
    private Map<String, WorkItemHandler> workItemHandlers = new TreeMap<String, WorkItemHandler>();
    private Map<String, AbstractTaskLifeCycleEventListener> taskAssignmentListeners = new TreeMap<String, AbstractTaskLifeCycleEventListener>();
    private Pipeline peopleAssignmentPipeline;
    private JahiaUserGroupCallback jahiaUserGroupCallback;
    private Map<ReleaseId, KieModule> kieModules = new HashMap<ReleaseId, KieModule>();

    public static JBPM6WorkflowProvider getInstance() {
        return instance;
    }

    public void setProcesses(Resource[] processes) {
        this.processes = processes;
    }

    public void setMailTemplates(Resource[] mailTemplates) {
        this.mailTemplates = mailTemplates;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setMailTemplateRegistry(MailTemplateRegistry mailTemplateRegistry) {
        this.mailTemplateRegistry = mailTemplateRegistry;
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
    }

    public WorkItemHandler unregisterWorkItemHandler(String name) {
        return workItemHandlers.remove(name);
    }

    public void registerTaskLifeCycleEventListener(String name, AbstractTaskLifeCycleEventListener taskAssignmentListener) {
        taskAssignmentListeners.put(name, taskAssignmentListener);
    }

    public AbstractTaskLifeCycleEventListener unregisterTaskLifeCycleEventListener(String name) {
        return taskAssignmentListeners.remove(name);
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

    public KieSession getKieSession() {
        if (kieSession != null) {
            return kieSession;
        }
        kieSession = runtimeEngine.getKieSession();

        for (Map.Entry<String, WorkItemHandler> workItemHandlerEntry : workItemHandlers.entrySet()) {
            kieSession.getWorkItemManager().registerWorkItemHandler(workItemHandlerEntry.getKey(), workItemHandlerEntry.getValue());
        }

        for (Map.Entry<String, AbstractTaskLifeCycleEventListener> taskAssignmentListenerEntry : taskAssignmentListeners.entrySet()) {
            AbstractTaskLifeCycleEventListener taskAssignmentListener = taskAssignmentListenerEntry.getValue();
            taskAssignmentListener.setEnvironment(kieSession.getEnvironment());
            taskAssignmentListener.setObservationManager(observationManager);
            taskAssignmentListener.setTaskService(taskService);
            if (taskService instanceof EventService) {
                ((EventService) taskService).registerTaskLifecycleEventListener(taskAssignmentListener);
            }
        }

        Map<String, Object> pipelineEnvironment = new HashMap<String, Object>();
        pipelineEnvironment.put(AbstractPeopleAssignmentValve.ENV_JBPM_WORKFLOW_PROVIDER, this);
        peopleAssignmentPipeline.setEnvironment(pipelineEnvironment);

        kieSession.addEventListener(new JBPMListener(this));

        return kieSession;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<WorkflowDefinition> getAvailableWorkflows(final Locale locale) {
        return executeCommand(new GenericCommand<List<WorkflowDefinition>>() {
            @Override
            public List<WorkflowDefinition> execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                KieBase kieBase = ksession.getKieBase();
                Collection<org.kie.api.definition.process.Process> processes = kieBase.getProcesses();
                List<WorkflowDefinition> workflowDefinitions = new ArrayList<WorkflowDefinition>();
                for (org.kie.api.definition.process.Process process : processes) {
                    workflowDefinitions.add(convertToWorkflowDefinition(process, locale, ksession));
                }
                return workflowDefinitions;
            }
        });
    }

    @Override
    public WorkflowDefinition getWorkflowDefinitionByKey(final String key, final Locale locale) {
        return executeCommand(new GenericCommand<WorkflowDefinition>() {
            @Override
            public WorkflowDefinition execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                KieBase kieBase = ksession.getKieBase();
                Collection<org.kie.api.definition.process.Process> processes = kieBase.getProcesses();
                for (org.kie.api.definition.process.Process process : processes) {
                    if (process.getName().equals(key)) {
                        return convertToWorkflowDefinition(process, locale, ksession);
                    }
                }
                return null;
            }
        });
    }

    @Override
    public List<Workflow> getActiveWorkflowsInformations(final List<String> processIds, final Locale locale) {
        return executeCommand(new GenericCommand<List<Workflow>>() {
            @Override
            public List<Workflow> execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                Collection<ProcessInstance> processInstances = getKieSession().getProcessInstances();
                List<Workflow> activeWorkflows = new ArrayList<Workflow>();
                for (ProcessInstance processInstance : processInstances) {
                    activeWorkflows.add(convertToWorkflow(processInstance, locale, ksession));
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

    public WorkflowDefinition getWorkflowDefinitionById(final String id, final Locale locale) {
        return executeCommand(new GenericCommand<WorkflowDefinition>() {
            @Override
            public WorkflowDefinition execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                return getWorkflowDefinitionById(id, locale, ksession);
            }
        });
    }

    private WorkflowDefinition getWorkflowDefinitionById(String id, Locale locale, KieSession ksession) {
        KieBase kieBase = getKieSession().getKieBase();
        org.kie.api.definition.process.Process process = kieBase.getProcess(id);
        return convertToWorkflowDefinition(process, locale, ksession);
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
    public Workflow getWorkflow(final String processId, final Locale locale) {
        return executeCommand(new GenericCommand<Workflow>() {
            @Override
            public Workflow execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                ProcessInstance processInstance = ksession.getProcessInstance(Long.parseLong(processId));
                return convertToWorkflow(processInstance, locale, ksession);
            }
        });
    }

    @Override
    public Set<WorkflowAction> getAvailableActions(final String processId, final Locale locale) {
        return executeCommand(new GenericCommand<Set<WorkflowAction>>() {
            @Override
            public Set<WorkflowAction> execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                ProcessInstance processInstance = getKieSession().getProcessInstance(Long.parseLong(processId));
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
                    String taskName = task.getNames().get(0).getText();
                    if (connectionIds.contains(taskName)) {
                        WorkflowAction workflowAction = convertToWorkflowTask(task, locale, ksession);
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
        });
    }

    @Override
    public List<WorkflowTask> getTasksForUser(final JahiaUser user, final Locale locale) {
        return executeCommand(new GenericCommand<List<WorkflowTask>>() {
            @Override
            public List<WorkflowTask> execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                List<WorkflowTask> availableTasks = new ArrayList<WorkflowTask>();
                List<TaskSummary> tasksOwned = taskService.getTasksOwnedByStatus(user.getUserKey(), OPEN_STATUS_LIST, locale.toString());
                if (tasksOwned != null && tasksOwned.size() > 0) {
                    availableTasks.addAll(convertToWorkflowTasks(locale, tasksOwned, ksession));
                }
                // how do we retrieve group tasks ?
                List<TaskSummary> potentialOwnerTasks = taskService.getTasksAssignedAsPotentialOwnerByStatus(user.getUserKey(), OPEN_STATUS_LIST, locale.toString());
                if (potentialOwnerTasks != null && potentialOwnerTasks.size() > 0) {
                    availableTasks.addAll(convertToWorkflowTasks(locale, potentialOwnerTasks, ksession));
                }
                List<TaskSummary> businessAdministratorTasks = taskService.getTasksAssignedAsBusinessAdministrator(user.getUserKey(), locale.toString());
                if (businessAdministratorTasks != null && businessAdministratorTasks.size() > 0) {
                    availableTasks.addAll(convertToWorkflowTasks(locale, businessAdministratorTasks, ksession));
                }
                return availableTasks;
            }
        });
    }

    private List<WorkflowTask> convertToWorkflowTasks(Locale locale, List<TaskSummary> taskSummaryList, KieSession ksession) {
        List<WorkflowTask> availableTasks = new LinkedList<WorkflowTask>();
        for (TaskSummary taskSummary : taskSummaryList) {
            try {
                Task task = taskService.getTaskById(taskSummary.getId());
                WorkflowTask workflowTask = convertToWorkflowTask(task, locale, ksession);
                availableTasks.add(workflowTask);
            } catch (Exception e) {
                logger.debug("Cannot get task " + taskSummary.getName() + " for user", e);
            }
        }
        return availableTasks;
    }

    @Override
    public List<Workflow> getWorkflowsForDefinition(final String definition, final Locale locale) {
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
                            workflows.add(convertToWorkflow(workflowProcessInstance, locale, ksession));
                        }
                    }
                }
                return workflows;


            }
        });
    }

    @Override
    public List<Workflow> getWorkflowsForUser(final JahiaUser user, final Locale locale) {
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
                            workflows.add(convertToWorkflow(processInstance, locale, ksession));
                        }
                    }
                }
                return workflows;
            }
        });
    }

    private ThreadLocal loop = new ThreadLocal();

    @Override
    public void assignTask(String taskId, final JahiaUser user) {
        if (loop.get() != null) {
            return;
        }
        try {
            loop.set(Boolean.TRUE);
            Task task = taskService.getTaskById(Long.parseLong(taskId));
            Map<String, Object> taskInputParameters = getTaskInputParameters(task);
            Map<String, Object> taskOutputParameters = getTaskOutputParameters(task, taskInputParameters);
            if (user == null) {
                taskService.release(task.getId(), null);
            } else {
                if (user.getUserKey().equals(task.getTaskData().getActualOwner().toString())) {
                    return;
                }

                if (!checkParticipation(task, user)) {
                    logger.error("Cannot assign task " + task.getId() + " to user " + user.getName() + ", user is not candidate");
                    return;
                }

                taskService.claim(Long.parseLong(taskId), user.getUserKey());
            }
            if (user != null) {
                taskOutputParameters.put("currentUser", user.getUserKey());
                ((TaskServiceEntryPointImpl) taskService).addContent(Long.parseLong(taskId), taskOutputParameters);
            }

            final String uuid = (String) taskOutputParameters.get("task-" + taskId);
            if (uuid != null) {
                try {
                    JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            JCRNodeWrapper nodeByUUID = session.getNodeByUUID(uuid);
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
                    e.printStackTrace();
                }
            }
        } finally {
            loop.set(null);
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
                if (user.getUserKey().equals(potentialOwner.toString())) {
                    return true;
                }
            } else if (potentialOwner instanceof Group) {
                if (groupManager.getUserMembership(user).contains(potentialOwner.toString())) {
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
    public void addParticipatingGroup(String taskId, JahiaGroup group, String role) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addComment(String processId, String comment, String user) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public WorkflowTask getWorkflowTask(final String taskId, final Locale locale) {
        return executeCommand(new GenericCommand<WorkflowTask>() {
            @Override
            public WorkflowTask execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                Task task = taskService.getTaskById(Long.parseLong(taskId));
                return convertToWorkflowTask(task, locale, ksession);
            }
        });
    }

    @Override
    public List<HistoryWorkflow> getHistoryWorkflowsForNode(String nodeId, Locale locale) {
        final List<HistoryWorkflow> workflows = new LinkedList<HistoryWorkflow>();
        return workflows;
    }

    @Override
    public List<HistoryWorkflow> getHistoryWorkflowsForPath(String path, Locale locale) {
        final List<HistoryWorkflow> workflows = new LinkedList<HistoryWorkflow>();
        return workflows;
    }

    @Override
    public List<HistoryWorkflow> getHistoryWorkflows(final List<String> processIds, final Locale locale) {
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
                            getWorkflowDefinitionById(processInstanceLog.getProcessId(), locale, ksession),
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
    public List<HistoryWorkflowTask> getHistoryWorkflowTasks(final String processId, final Locale locale) {
        return executeCommand(new AbstractHistoryLogCommand<List<HistoryWorkflowTask>>() {
            @Override
            public List<HistoryWorkflowTask> execute(Context context) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();

                final List<HistoryWorkflowTask> workflowTaskHistory = new LinkedList<HistoryWorkflowTask>();

                List<TaskSummary> tasksIds = taskService.getTasksByStatusByProcessInstanceId(Long.parseLong(processId), OPEN_STATUS_LIST, locale != null ? locale.getLanguage() : null);
                for (TaskSummary taskSummary : tasksIds) {
                    final HistoryWorkflowTask workflowTask = new HistoryWorkflowTask(Long.toString(taskSummary.getId()),
                            Long.toString(taskSummary.getProcessInstanceId()),
                            taskSummary.getName(),
                            key,
                            null,
                            taskSummary.getActivationTime(),
                            null,
                            null);
                    workflowTaskHistory.add(workflowTask);
                    if (locale != null) {
                        final WorkflowDefinition definition = getWorkflowDefinitionById(taskSummary.getProcessId(), locale, ksession);
                        ResourceBundle resourceBundle = getResourceBundle(locale, definition.getKey());
                        String rbActionName = i18nName(workflowTask.getName(), resourceBundle);
                        workflowTask.setDisplayName(rbActionName);
                    }
                }

                setLogEnvironment(context);
                ProcessInstanceLog processInstanceLog = auditLogService.findProcessInstance(Long.parseLong(processId));
                List<NodeInstanceLog> nodeInstanceLogs = auditLogService.findNodeInstances(processInstanceLog.getProcessInstanceId());
                for (NodeInstanceLog nodeInstanceLog : nodeInstanceLogs) {
                    if (nodeInstanceLog.getWorkItemId() != null) {
                        workflowTaskHistory.add(new HistoryWorkflowTask(nodeInstanceLog.getWorkItemId().toString(),
                                nodeInstanceLog.getProcessId(),
                                nodeInstanceLog.getNodeName(),
                                key,
                                "user", // @todo properly implement this
                                nodeInstanceLog.getDate(),
                                nodeInstanceLog.getDate(),
                                "outcome")); // @todo properly implement this.
                    }
                }
                return workflowTaskHistory;
            }
        });
    }

    @Override
    public void deleteProcess(String processId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private WorkflowDefinition convertToWorkflowDefinition(org.kie.api.definition.process.Process process, Locale locale, KieSession ksession) {
        WorkflowDefinition wf = new WorkflowDefinition(process.getName(), process.getName(), this.key);
        WorkflowProcess workflowProcess = (WorkflowProcess) process;

        String startFormName = workflowService.getFormForAction(wf.getKey(), "start");
        wf.setFormResourceName(startFormName);

        Node[] nodes = workflowProcess.getNodes();

        final Set<String> tasks = new LinkedHashSet<String>();
        tasks.add(WorkflowService.START_ROLE);
        for (Node node : nodes) {
            if (node instanceof HumanTaskNode) {
                tasks.add(node.getName());
            }
        }
        wf.setTasks(tasks);

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

    private Workflow convertToWorkflow(ProcessInstance instance, Locale locale, KieSession ksession) {
        WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) instance;
        final Workflow workflow = new Workflow(instance.getProcessName(), Long.toString(instance.getId()), key);
        final WorkflowDefinition definition = getWorkflowDefinitionById(instance.getProcessId(), locale, ksession);
        workflow.setWorkflowDefinition(definition);
        workflow.setAvailableActions(getAvailableActions(Long.toString(instance.getId()), locale));
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

    private WorkflowTask convertToWorkflowTask(Task task, Locale locale, KieSession ksession) {
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
            final WorkflowDefinition definition = getWorkflowDefinitionById(instance.getProcessId(), locale, ksession);
            workflowTask.setWorkflowDefinition(definition);
            i18nOfWorkflowAction(locale, workflowTask, definition.getKey());
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
        ResourceBundle resourceBundle = null;
        if (displayLocale != null) {
            resourceBundle = getResourceBundle(displayLocale, definitionKey);
            String rbActionName = i18nName(workflowAction.getName(), resourceBundle);
            workflowAction.setDisplayName(rbActionName);
        }
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

    private String i18nName(String actionName, ResourceBundle resourceBundle) {
        if (resourceBundle != null) {
            String key = Patterns.SPACE.matcher(actionName).replaceAll(".").trim().toLowerCase();
            try {
                actionName = resourceBundle.getString(key);
            } catch (MissingResourceException e) {
                logger.warn("Missing ressource : " + key + " in " + resourceBundle);
            }
        }
        return actionName;
    }

    public static String getI18NText(List<I18NText> i18NTexts, Locale locale) {
        if (locale == null || i18NTexts == null || i18NTexts.size() == 0) {
            return "";
        }
        for (I18NText i18NText : i18NTexts) {
            if (i18NText.getLanguage().equals(locale.toString())) {
                return i18NText.getText();
            } else if (i18NText.getLanguage().equals(locale.getLanguage())) {
                return i18NText.getText();
            }
        }
        return "";
    }


    private <T> T executeCommand(Command<T> t) {
        CommandBasedStatefulKnowledgeSession s = (CommandBasedStatefulKnowledgeSession) getKieSession();
        return s.execute(t);
    }

    public void addResource(Resource kieResource) throws IOException {
        kieFileSystem.write(kieServices.getResources().newUrlResource(kieResource.getURL()));
    }

    public void removeResource(Resource kieResource) throws IOException {
        kieFileSystem.delete(kieResource.getURL().getPath());
    }

    public void recompilePackages() {
        kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        KieContainer classPathKieContainer = kieServices.getKieClasspathContainer();
        Results classPathVerifyResults = classPathKieContainer.verify();
        kieBuilder.buildAll();

        kieContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());

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
            runtimeManager.disposeRuntimeEngine(runtimeEngine);
            runtimeManager.close();
        }

        runtimeManager = JahiaRuntimeManagerFactoryImpl.getInstance().newSingletonRuntimeManager(runtimeEnvironment);
        runtimeEngine = runtimeManager.getRuntimeEngine(EmptyContext.get());
        taskService = runtimeEngine.getTaskService();
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
