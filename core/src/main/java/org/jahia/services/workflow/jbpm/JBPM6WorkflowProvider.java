/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

import org.apache.jackrabbit.util.ISO9075;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.impl.EnvironmentFactory;
import org.drools.persistence.PersistenceContextManager;
import org.drools.persistence.TransactionManager;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.pipelines.Pipeline;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.*;
import org.jahia.services.workflow.jbpm.command.*;
import org.jahia.services.workflow.jbpm.custom.AbstractTaskLifeCycleEventListener;
import org.jbpm.runtime.manager.impl.RuntimeEnvironmentBuilder;
import org.jbpm.shared.services.impl.JbpmServicesPersistenceManagerImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.task.model.Status;
import org.kie.internal.runtime.manager.RuntimeEnvironment;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.task.api.EventService;
import org.kie.spring.persistence.KieSpringJpaManager;
import org.kie.spring.persistence.KieSpringTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.util.*;

/**
 * jBPM 6 Workflow Provider implementation
 */
public class JBPM6WorkflowProvider implements WorkflowProvider, WorkflowObservationManagerAware, JahiaAfterInitializationService {

    public static final List<Status> OPEN_STATUS_LIST = Arrays.asList(Status.Created, Status.InProgress, Status.Ready, Status.Reserved);
    public static final List<Status> OPEN_STATUS_LIST_NON_RESERVED = Arrays.asList(Status.Created, Status.InProgress, Status.Ready);
    public static final List<Status> RESERVED_STATUS_LIST = Arrays.asList(Status.Reserved);

    private static final Logger logger = LoggerFactory.getLogger(JBPM6WorkflowProvider.class);
    private static final JBPM6WorkflowProvider instance = new JBPM6WorkflowProvider();

    private String key;
    private WorkflowService workflowService;
    private WorkflowObservationManager observationManager;
    private JahiaUserManagerService userManager;
    private JahiaGroupManagerService groupManager;
    private KieRepository kieRepository;
    private KieServices kieServices;
    private KieFileSystem kieFileSystem;
    private JBPMListener listener = new JBPMListener(this);
    private RuntimeManager runtimeManager;
    private RuntimeEngine runtimeEngine;
    private AbstractPlatformTransactionManager platformTransactionManager;
    private EntityManagerFactory emf;
    private EntityManager sharedEm;
    private JbpmServicesPersistenceManagerImpl jbpmServicesPersistenceManager;
    private Map<String, WorkItemHandler> workItemHandlers = new TreeMap<String, WorkItemHandler>();
    private Map<String, AbstractTaskLifeCycleEventListener> taskLifeCycleEventListeners = new TreeMap<String, AbstractTaskLifeCycleEventListener>();
    private Pipeline peopleAssignmentPipeline;
    private JahiaUserGroupCallback jahiaUserGroupCallback;
    private KieContainer kieContainer;
    private TransactionManager transactionManager;
    private ThreadLocal<Boolean> loop = new ThreadLocal<Boolean>();
    private volatile boolean isInitialized = false;
    private SchedulerService schedulerService;

    private Map<String, Map<Locale, WorkflowDefinition>> definitionMap = new HashMap<>();

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

    public void setEntityManagerFactory(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void setSharedEntityManager(EntityManager em) {
        this.sharedEm = em;
    }

    public void setJbpmServicesPersistenceManager(JbpmServicesPersistenceManagerImpl jbpmServicesPersistenceManager) {
        this.jbpmServicesPersistenceManager = jbpmServicesPersistenceManager;
    }

    public synchronized void registerWorkItemHandler(String name, WorkItemHandler workItemHandler) {
        synchronized (workflowService) {
            workItemHandlers.put(name, workItemHandler);
            if (runtimeEngine != null) {
                runtimeEngine.getKieSession().getWorkItemManager().registerWorkItemHandler(name, workItemHandler);
            }
        }
    }

    public WorkItemHandler unregisterWorkItemHandler(String name) {
        synchronized (workflowService) {
            if (runtimeEngine != null) {
                runtimeEngine.getKieSession().getWorkItemManager().registerWorkItemHandler(name, null);
            }
            return workItemHandlers.remove(name);
        }
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

        workflowService.addProvider(this);
    }

    public void stop() {
        workflowService.removeProvider(this);

        // @todo implement clean shutdown of all jBPM & Drools environment
        runtimeManager.close();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<WorkflowDefinition> getAvailableWorkflows(final Locale uiLocale) {
        return executeCommand(new GetAvailableWorkflowsCommand(uiLocale));
    }

    @Override
    public WorkflowDefinition getWorkflowDefinitionByKey(final String key, final Locale uiLocale) {
        Map<Locale, WorkflowDefinition> defsByKey = definitionMap.get(key);
        if (defsByKey == null) {
            defsByKey = new HashMap<>();
            definitionMap.put(key, defsByKey);
        }
        WorkflowDefinition definition = defsByKey.get(uiLocale);
        if (definition == null) {
            definition = executeCommand(new GetWorkflowDefinitionCommand(key, uiLocale));
            defsByKey.put(uiLocale, definition);
        }

        return definition;
    }

    @Override
    public List<Workflow> getActiveWorkflowsInformations(final List<String> processIds, final Locale uiLocale) {
        return executeCommand(new GetActiveWorkflowsInformationsCommand(processIds, uiLocale));
    }

    @Override
    public String startProcess(final String processKey, final Map<String, Object> args) {
        return executeCommand(new StartProcessCommand(processKey, args));
    }

    @Override
    public void abortProcess(final String processId) {
        List<Long> taskIds = runtimeEngine.getTaskService().getTasksByProcessInstanceId(Long.parseLong(processId));
        for (Long taskId : taskIds) {
            Status status = runtimeEngine.getTaskService().getTaskById(taskId).getTaskData().getStatus();
            if (status == Status.Ready || status == Status.InProgress || status == Status.Reserved) {
                observationManager.notifyTaskEnded(key, Long.toString(taskId));
            }
        }
        executeCommand(new AbortProcessCommand(processId));
    }

    @Override
    public Workflow getWorkflow(final String processId, final Locale uiLocale) {
        return executeCommand(new GetWorkflowCommand(processId, uiLocale));
    }

    @Override
    public Set<WorkflowAction> getAvailableActions(final String processId, final Locale uiLocale) {
        return executeCommand(new GetAvailableActionsCommand(processId, uiLocale));
    }

    @Override
    public List<WorkflowTask> getTasksForUser(final JahiaUser user, final Locale uiLocale) {

        return executeCommand(new GetTasksForUserCommand(user, uiLocale));
    }

    @Override
    public List<Workflow> getWorkflowsForDefinition(final String definition, final Locale uiLocale) {
        return executeCommand(new GetWorkflowsForDefinitionCommand(definition, uiLocale));
    }

    @Override
    public List<Workflow> getWorkflowsForUser(final JahiaUser user, final Locale uiLocale) {
        return executeCommand(new GetWorkflowsForUserCommand(user, uiLocale));
    }

    @Override
    public void assignTask(final String taskId, final JahiaUser user) {
        logger.debug("[ASSIGN] Assigning task {} to user {}", taskId, user);
        if (loop.get() != null) {
            logger.debug("[ASSIGN] Skip assigning task {} to user {}", taskId, user);
            return;
        }
        try {
            loop.set(Boolean.TRUE);
            executeCommand(new AssignTaskCommand(taskId, user));
            logger.debug("[ASSIGN] Task {} is assigned user {}", taskId, user);
        } finally {
            loop.set(null);
        }
    }

    @Override
    public void completeTask(final String taskId, final JahiaUser jahiaUser, final String outcome, final Map<String, Object> args) {
        logger.debug("[COMPLETE] Completing task {} to user {}", taskId, jahiaUser.getUsername());
        if (loop.get() != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("[COMPLETE] Skip Completing task {} to user {}", taskId, jahiaUser.getUsername());
            }
            return;
        }
        try {
            loop.set(Boolean.TRUE);
            executeCommand(new CompleteTaskCommand(taskId, outcome, args, jahiaUser, observationManager));
            if (logger.isDebugEnabled()) {
                logger.debug("[COMPLETE] Task {} to user {} completed", taskId, jahiaUser.getUsername());
            }
        } finally {
            loop.set(null);
        }
    }

    @Override
    public void addComment(final String processId, final String comment, final String user) {
        executeCommand(new AddCommentCommand(processId, comment, user));
    }

    @Override
    public WorkflowTask getWorkflowTask(final String taskId, final Locale uiLocale) {
        return executeCommand(new GetWorkflowTaskCommand(taskId, uiLocale));
    }

    @Override
    public List<HistoryWorkflow> getHistoryWorkflowsForNode(final String nodeId, final Locale uiLocale) {
        return executeCommand(new GetHistoryWorkflowsForNodeCommand(nodeId, uiLocale));
    }

    @Override
    public List<HistoryWorkflow> getHistoryWorkflowsForPath(final String path, final Locale uiLocale) {
        return executeCommand(new GetHistoryWorkflowsForPathCommand(path, uiLocale));
    }

    @Override
    public List<HistoryWorkflow> getHistoryWorkflows(final List<String> processIds, final Locale uiLocale) {
        return executeCommand(new GetHistoryWorkflowCommand(processIds, uiLocale));
    }

    @Override
    public List<HistoryWorkflowTask> getHistoryWorkflowTasks(final String processId, final Locale uiLocale) {
        return executeCommand(new GetHistoryWorkflowTasksCommand(processId, uiLocale));
    }

    @Override
    public void deleteProcess(String processId) {
        // do nothing
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


    /**
     * Add a resource into KIE filesystem. It will use the same path as the one of the resource
     *
     * @param kieResource The resource to add
     * @throws IOException Resource cannot be read
     */
    public void addResource(Resource kieResource) throws IOException {
        synchronized (workflowService) {
            kieFileSystem.write("src/main/resources" + kieResource.getURL().getPath(), kieServices.getResources().newUrlResource(kieResource.getURL()));
            definitionMap.clear();
        }
    }

    /**
     * Add a resource into KIE filesystem at a specific path
     *
     * @param kieResource The resource to add
     * @param targetPath The target path
     * @throws IOException Resource cannot be read
     */
    public void addResource(Resource kieResource, String targetPath) throws IOException {
        synchronized (workflowService) {
            org.kie.api.io.Resource resource = kieServices.getResources().newUrlResource(kieResource.getURL());
            resource.setTargetPath(targetPath);
            kieFileSystem.write(resource);
            definitionMap.clear();
        }
    }

    /**
     * Remove a resource from KIE filesystem at the path of the specified resource
     *
     * @param kieResource The resource (only used for getting the path)
     * @throws IOException Resource cannot be read
     */
    public void removeResource(Resource kieResource) throws IOException {
        removeResource(kieResource.getURL().getPath());
    }

    /**
     * Remove a resource from KIE filesystem at the specified path
     *
     * @param path The path
     */
    public void removeResource(String path) {
        synchronized (workflowService) {
            kieFileSystem.delete("src/main/resources" + path);
            definitionMap.clear();
        }
    }

    public void recompilePackages() {

        synchronized (workflowService) {

            long timer = System.currentTimeMillis();
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            kieContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());

            transactionManager = new KieSpringTransactionManager(platformTransactionManager);

            PersistenceContextManager persistenceContextManager = createKieSpringContextManager(transactionManager);
            RuntimeEnvironment runtimeEnvironment = RuntimeEnvironmentBuilder.getDefault()
                    .entityManagerFactory(emf)
                    .schedulerService(new JahiaQuartzSchedulerService(schedulerService.getScheduler()))
                    .addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, transactionManager)
                    .addEnvironmentEntry(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, persistenceContextManager)
                    .knowledgeBase(kieContainer.getKieBase())
                    .classLoader(kieContainer.getClassLoader())
                    .registerableItemsFactory(new JahiaKModuleRegisterableItemsFactory(kieContainer, null, peopleAssignmentPipeline))
                    .userGroupCallback(jahiaUserGroupCallback)
                    .get();
            if (runtimeManager != null) {
                runtimeManager.close();
            }

            final JahiaRuntimeManagerFactoryImpl runtimeFactory = JahiaRuntimeManagerFactoryImpl.getInstance();
            runtimeFactory.setJbpmServicesPersistenceManager(jbpmServicesPersistenceManager);

            // Use singleton runtime manager - one manager/session/taskservice for all requests
            runtimeManager = runtimeFactory.newSingletonRuntimeManager(runtimeEnvironment);
            runtimeEngine = runtimeManager.getRuntimeEngine(EmptyContext.get());

            KieSession kieSession = runtimeEngine.getKieSession();

            for (Map.Entry<String, WorkItemHandler> workItemHandlerEntry : workItemHandlers.entrySet()) {
                kieSession.getWorkItemManager().registerWorkItemHandler(workItemHandlerEntry.getKey(), workItemHandlerEntry.getValue());
            }

            for (Map.Entry<String, AbstractTaskLifeCycleEventListener> taskLifeCycleEventListenerEntry : taskLifeCycleEventListeners.entrySet()) {
                AbstractTaskLifeCycleEventListener taskLifeCycleEventListener = taskLifeCycleEventListenerEntry.getValue();
                taskLifeCycleEventListener.setEnvironment(kieSession.getEnvironment());
                taskLifeCycleEventListener.setObservationManager(observationManager);
                taskLifeCycleEventListener.setTaskService(runtimeEngine.getTaskService());
                if (runtimeEngine.getTaskService() instanceof EventService) {
                    ((EventService) runtimeEngine.getTaskService()).registerTaskLifecycleEventListener(taskLifeCycleEventListener);
                }
            }

            Map<String, Object> pipelineEnvironment = new HashMap<String, Object>();
            pipelineEnvironment.put(AbstractPeopleAssignmentValve.ENV_JBPM_WORKFLOW_PROVIDER, this);
            peopleAssignmentPipeline.setEnvironment(pipelineEnvironment);

            kieSession.addEventListener(listener);
            isInitialized = true;
            logger.info("Rebuilding KIE base took {} ms", System.currentTimeMillis() - timer);
        }
    }

    private PersistenceContextManager createKieSpringContextManager(TransactionManager transactionManager) {

        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.APP_SCOPED_ENTITY_MANAGER, sharedEm);

        /** Put app EM as cmd-shared **/
        env.set(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, sharedEm);
        env.set("IS_SHARED_ENTITY_MANAGER", true);
        /*****/

        env.set("IS_JTA_TRANSACTION", false);

        env.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);
        PersistenceContextManager persistenceContextManager = new KieSpringJpaManager(env);
        env.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, persistenceContextManager);
        return persistenceContextManager;
    }


    private <T> T executeCommand(BaseCommand<T> t) {
        t.setRuntimeEngine(runtimeEngine);
        t.setEm(sharedEm);
        t.setPersistenceManager(jbpmServicesPersistenceManager);
        t.setGroupManager(groupManager);
        t.setUserManager(userManager);
        t.setWorkflowService(workflowService);
        t.setKey(key);
        CommandBasedStatefulKnowledgeSession s = (CommandBasedStatefulKnowledgeSession) runtimeEngine.getKieSession();
        return s.execute(t);
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        recompilePackages();
        workflowService.initAfterAllServicesAreStarted();
    }

    /**
     * @param schedulerService the schedulerService to set
     */
    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }
}
