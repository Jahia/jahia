package org.jahia.services.workflow.jbpm;

import org.apache.commons.lang.StringUtils;
import org.drools.container.spring.beans.persistence.DroolsSpringJpaManager;
import org.drools.container.spring.beans.persistence.DroolsSpringTransactionManager;
import org.drools.core.impl.EnvironmentFactory;
import org.drools.persistence.PersistenceContextManager;
import org.drools.persistence.TransactionManager;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.*;
import org.jahia.services.workflow.jbpm.custom.email.AddressTemplate;
import org.jahia.services.workflow.jbpm.custom.email.MailTemplate;
import org.jahia.services.workflow.jbpm.custom.email.MailTemplateRegistry;
import org.jahia.utils.Patterns;
import org.jahia.utils.i18n.ResourceBundles;
import org.jbpm.process.audit.JPAProcessInstanceDbLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.runtime.manager.impl.KModuleRegisterableItemsFactory;
import org.jbpm.runtime.manager.impl.RuntimeEnvironmentBuilder;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
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
import org.kie.internal.runtime.manager.RuntimeEnvironment;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

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
        JBPMTaskLifeCycleEventListener.setObservationManager(observationManager);
    }

    public void setGroupManager(JahiaGroupManagerService groupManager) {
        this.groupManager = groupManager;
    }

    public void setUserManager(JahiaUserManagerService userManager) {
        this.userManager = userManager;
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

    public void start() {

        kieServices = KieServices.Factory.get();
        kieRepository = kieServices.getRepository();

        List<org.kie.api.io.Resource> fileSystemResources = new ArrayList<org.kie.api.io.Resource>();

        for (Resource process : processes) {
            try {
                fileSystemResources.add(kieServices.getResources().newUrlResource(process.getURL()));
            } catch (IOException e) {
                logger.error("Error while trying to add process resource " + process, e);
            }
        }

        kieFileSystem = kieServices.newKieFileSystem();

        for (org.kie.api.io.Resource kieResource : fileSystemResources) {
            kieFileSystem.write(kieResource);
        }

        kieBuilder = kieServices.newKieBuilder(kieFileSystem);

        kieBuilder.buildAll();

        kieContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());

        TransactionManager transactionManager = new DroolsSpringTransactionManager(platformTransactionManager);
        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.APP_SCOPED_ENTITY_MANAGER, em);
        env.set(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, em);
        env.set("IS_JTA_TRANSACTION", false);
        env.set("IS_SHARED_ENTITY_MANAGER", true);
        env.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);
        PersistenceContextManager persistenceContextManager = new DroolsSpringJpaManager(env);
        env.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, persistenceContextManager);
        RuntimeEnvironment runtimeEnvironment = RuntimeEnvironmentBuilder.getDefault()
                .entityManagerFactory(emf)
                .addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, transactionManager)
                .addEnvironmentEntry(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, persistenceContextManager)
                        // .userGroupCallback(userGroupCallback)
                        // .addAsset(ResourceFactory.newClassPathResource(process), ResourceType.BPMN2)
                .knowledgeBase(kieContainer.getKieBase())
                .classLoader(kieContainer.getClassLoader())
                .registerableItemsFactory(new KModuleRegisterableItemsFactory(kieContainer, null))
                .get();
        // runtimeManager = RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);
        runtimeManager = JahiaRuntimeManagerFactoryImpl.getInstance().newSingletonRuntimeManager(runtimeEnvironment);
        runtimeEngine = runtimeManager.getRuntimeEngine(EmptyContext.get());
        taskService = runtimeEngine.getTaskService();

        if (mailTemplates != null && mailTemplates.length > 0) {
            logger.info("Found {} workflow mail templates to be deployed.", mailTemplates.length);

            List keys = Arrays.asList("from", "to", "cc", "bcc",
                    "from-users", "to-users", "cc-users", "bcc-users",
                    "from-groups", "to-groups", "cc-groups", "bcc-groups",
                    "subject", "text", "html", "language");

            for (Resource mailTemplateResource : mailTemplates) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(mailTemplateResource.getInputStream(), "UTF-8"));
                    MailTemplate mailTemplate = new MailTemplate();
                    mailTemplate.setLanguage("velocity");
                    mailTemplate.setFrom(new AddressTemplate());
                    mailTemplate.setTo(new AddressTemplate());
                    mailTemplate.setCc(new AddressTemplate());
                    mailTemplate.setBcc(new AddressTemplate());

                    int currentField = -1;
                    String currentLine;
                    StringBuilder buf = new StringBuilder();
                    while ((currentLine = reader.readLine()) != null) {
                        if (currentLine.contains(":")) {
                            String prefix = StringUtils.substringBefore(currentLine, ":");
                            if (keys.contains(prefix.toLowerCase())) {
                                JBPMModuleProcessLoader.setMailTemplateField(mailTemplate, currentField, buf);
                                buf = new StringBuilder();
                                currentField = keys.indexOf(prefix.toLowerCase());
                                currentLine = StringUtils.substringAfter(currentLine, ":").trim();
                            }
                        } else {
                            buf.append('\n');
                        }
                        buf.append(currentLine);
                    }
                    JBPMModuleProcessLoader.setMailTemplateField(mailTemplate, currentField, buf);
                    mailTemplateRegistry.addTemplate(StringUtils.substringBeforeLast(mailTemplateResource.getFilename(), "."), mailTemplate);
                } catch (IOException e) {
                    logger.error("Error reading mail template " + mailTemplateResource, e);
                }

            }
        }

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

        JBPMTaskLifeCycleEventListener.setProvider(this);
        JBPMTaskLifeCycleEventListener.setEnvironment(kieSession.getEnvironment());
        JBPMTaskLifeCycleEventListener.setTaskService(taskService);

        return kieSession;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<WorkflowDefinition> getAvailableWorkflows(Locale locale) {
        KieBase kieBase = getKieSession().getKieBase();
        Collection<org.kie.api.definition.process.Process> processes = kieBase.getProcesses();
        List<WorkflowDefinition> workflowDefinitions = new ArrayList<WorkflowDefinition>();
        for (org.kie.api.definition.process.Process process : processes) {
            workflowDefinitions.add(convertToWorkflowDefinition(process, locale));
        }
        return workflowDefinitions;
    }

    @Override
    public WorkflowDefinition getWorkflowDefinitionByKey(String key, Locale locale) {
        KieBase kieBase = getKieSession().getKieBase();
        Collection<org.kie.api.definition.process.Process> processes = kieBase.getProcesses();
        for (org.kie.api.definition.process.Process process : processes) {
            if (process.getName().equals(key)) {
                return convertToWorkflowDefinition(process, locale);
            }
        }
        return null;
    }

    public WorkflowDefinition getWorkflowDefinitionById(String id, Locale locale) {
        KieBase kieBase = getKieSession().getKieBase();
        org.kie.api.definition.process.Process process = kieBase.getProcess(id);
        return convertToWorkflowDefinition(process, locale);
    }

    @Override
    public List<Workflow> getActiveWorkflowsInformations(List<String> processIds, Locale locale) {
        Collection<ProcessInstance> processInstances = getKieSession().getProcessInstances();
        List<Workflow> activeWorkflows = new ArrayList<Workflow>();
        for (ProcessInstance processInstance : processInstances) {
            activeWorkflows.add(convertToWorkflow(processInstance, locale));
        }
        return activeWorkflows;
    }

    @Override
    public String startProcess(String processKey, Map<String, Object> args) {
        ProcessInstance processInstance = getKieSession().startProcess(processKey, args);
        return Long.toString(processInstance.getId());
    }

    @Override
    public void abortProcess(String processId) {
        getKieSession().abortProcessInstance(Long.parseLong(processId));
    }

    @Override
    public Workflow getWorkflow(String processId, Locale locale) {
        ProcessInstance processInstance = getKieSession().getProcessInstance(Long.parseLong(processId));
        String processDefinitionId = processInstance.getProcessId();
        KieBase kieBase = getKieSession().getKieBase();

        return convertToWorkflow(processInstance, locale);
    }

    @Override
    public Set<WorkflowAction> getAvailableActions(String processId, Locale locale) {
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
        final List<WorkflowTask> availableTasks = new LinkedList<WorkflowTask>();
        List<TaskSummary> taskSummaryList = taskService.getTasksOwned(user.getUserKey(), locale.toString());
        for (TaskSummary taskSummary : taskSummaryList) {
            try {
                Task task = taskService.getTaskById(taskSummary.getId());
                WorkflowTask workflowTask = convertToWorkflowTask(task, locale);
                availableTasks.add(workflowTask);
            } catch (Exception e) {
                logger.debug("Cannot get task " + taskSummary.getName() + " for user", e);
            }
        }
        // how do we retrieve group tasks ?
        return availableTasks;
    }

    @Override
    public List<Workflow> getWorkflowsForDefinition(String definition, Locale locale) {
        final List<Workflow> workflows = new LinkedList<Workflow>();
        return workflows;
    }

    @Override
    public List<Workflow> getWorkflowsForUser(JahiaUser user, Locale locale) {
        final List<Workflow> workflows = new LinkedList<Workflow>();
        return workflows;
    }

    @Override
    public void assignTask(String taskId, JahiaUser user) {
        taskService.claim(Long.parseLong(taskId), user.getUserKey());
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void completeTask(String taskId, JahiaUser jahiaUser, String outcome, Map<String, Object> args) {
        //To change body of implemented methods use File | Settings | File Templates.
        args.put("outcome", outcome);
        taskService.complete(Long.parseLong(taskId), jahiaUser.getUserKey(), args);
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
    public WorkflowTask getWorkflowTask(String taskId, Locale locale) {
        Task task = taskService.getTaskById(Long.parseLong(taskId));
        return convertToWorkflowTask(task, locale);
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
    public List<HistoryWorkflow> getHistoryWorkflows(List<String> processIds, Locale locale) {
        List<HistoryWorkflow> historyWorkflows = new ArrayList<HistoryWorkflow>();
        for (String processId : processIds) {
            ProcessInstanceLog processInstanceLog = JPAProcessInstanceDbLog.findProcessInstance(Long.parseLong(processId));
            List<VariableInstanceLog> nodeIdVariableInstanceLogs = JPAProcessInstanceDbLog.findVariableInstances(Long.parseLong(processId), "JCRNodeId");
            String nodeId = null;
            for (VariableInstanceLog nodeIdVariableInstanceLog : nodeIdVariableInstanceLogs) {
                nodeId = nodeIdVariableInstanceLog.getValue();
            }
            historyWorkflows.add(new HistoryWorkflow(Long.toString(processInstanceLog.getId()),
                    getWorkflowDefinitionById(processInstanceLog.getProcessId(), locale),
                    processInstanceLog.getProcessName(),
                    key,
                    processInstanceLog.getIdentity(),
                    processInstanceLog.getStart(),
                    processInstanceLog.getEnd(),
                    processInstanceLog.getOutcome(),
                    nodeId
            ));
        }
        return historyWorkflows;
    }

    @Override
    public List<HistoryWorkflowTask> getHistoryWorkflowTasks(String processId, Locale locale) {
        final List<HistoryWorkflowTask> workflows = new LinkedList<HistoryWorkflowTask>();
        return workflows;
    }

    @Override
    public void deleteProcess(String processId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private WorkflowDefinition convertToWorkflowDefinition(org.kie.api.definition.process.Process process, Locale locale) {
        WorkflowDefinition wf = new WorkflowDefinition(process.getName(), process.getName(), this.key);
        String startFormName = null;
        WorkflowProcess workflowProcess = (WorkflowProcess) process;
        if (workflowProcess.getNodes().length > 0) {
            startFormName = (String) workflowProcess.getNodes()[0].getMetaData().get("FormName");
        }
        wf.setFormResourceName(startFormName);
        if (process instanceof WorkflowProcess) {
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
        final WorkflowDefinition definition = getWorkflowDefinitionById(instance.getProcessId(), locale);
        workflow.setWorkflowDefinition(definition);
        workflow.setAvailableActions(getAvailableActions(Long.toString(instance.getId()), locale));
        /*
        Not sure how to handle this in jBPM 6 since we don't use timers in our processes
        Job job = managementService.createJobQuery().timers().processInstanceId(instance.getId()).uniqueResult();
        if (job != null) {
            workflow.setDuedate(job.getDueDate());
        }
        */
        ProcessInstanceLog processInstanceLog = JPAProcessInstanceDbLog.findProcessInstance(instance.getId());
        workflow.setStartTime(processInstanceLog.getStart());

        Object user = workflowProcessInstance.getVariable("User");
        if (user != null) {
            workflow.setStartUser(user.toString());
        }

        workflow.setVariables(((WorkflowProcessInstanceImpl) workflowProcessInstance).getVariables());

        return workflow;
    }

    private WorkflowTask convertToWorkflowTask(Task task, Locale locale) {
        WorkflowTask workflowTask = new WorkflowTask(task.getTaskData().getProcessId(), key);
        workflowTask.setDueDate(task.getTaskData().getExpirationTime());
        workflowTask.setDescription(getI18NText(task.getDescriptions(), locale).getText());
        workflowTask.setCreateTime(task.getTaskData().getCreatedOn());
        workflowTask.setProcessId(task.getTaskData().getProcessId());
        if (task.getTaskData().getActualOwner() != null) {
            workflowTask.setAssignee(
                    ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(task.getTaskData().getActualOwner().toString()));
        }
        workflowTask.setId(Long.toString(task.getId()));
        Set<String> connectionIds = getTaskOutcomes(task);
        workflowTask.setOutcome(connectionIds);
        PeopleAssignments peopleAssignements = task.getPeopleAssignments();
        if (peopleAssignements.getPotentialOwners().size() > 0) {
            List<WorkflowParticipation> participations = new ArrayList<WorkflowParticipation>();
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
            workflowTask.setParticipations(participations);
        }
        // Get form resource name
        long contentId = task.getTaskData().getDocumentContentId();
        Content taskContent = taskService.getContentById(contentId);
        Object contentData = ContentMarshallerHelper.unmarshall(taskContent.getContent(), getKieSession().getEnvironment());
        if (contentData instanceof Map) {
            Map<String, Object> taskParameters = (Map<String, Object>) contentData;
            workflowTask.setFormResourceName((String) taskParameters.get("FormName"));
            workflowTask.setVariables(taskParameters);
        }

        // Get Tasks variables
        final ProcessInstance instance = getKieSession().getProcessInstance(task.getTaskData().getProcessInstanceId());
        if (instance != null) {
            final WorkflowDefinition definition = getWorkflowDefinitionById(instance.getProcessId(), locale);
            workflowTask.setWorkflowDefinition(definition);
            i18nOfWorkflowAction(locale, workflowTask, definition.getKey());
        }
        return workflowTask;
    }

    public Set<String> getTaskOutcomes(Task task) {
        long workItemId = task.getTaskData().getWorkItemId();
        WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) getKieSession().getProcessInstance(task.getTaskData().getProcessInstanceId());
        NodeInstance nodeInstance = workflowProcessInstance.getNodeInstance(workItemId);
        Map<String, List<Connection>> outgoingConnections = nodeInstance.getNode().getOutgoingConnections();
        Set<String> connectionIds = new TreeSet<String>();
        for (Map.Entry<String, List<Connection>> outgoingConnectionEntry : outgoingConnections.entrySet()) {
            for (Connection connection : outgoingConnectionEntry.getValue()) {
                String uniqueId = (String) connection.getMetaData().get("UniqueId");
                connectionIds.add(uniqueId);
            }
        }
        return connectionIds;
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

    public static I18NText getI18NText(List<I18NText> i18NTexts, Locale locale) {
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
