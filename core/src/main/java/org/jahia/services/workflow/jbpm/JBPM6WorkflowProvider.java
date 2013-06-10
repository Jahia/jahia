package org.jahia.services.workflow.jbpm;

import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.*;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieRepository;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * jBPM 6 Workflow Provider implementation
 */
public class JBPM6WorkflowProvider implements WorkflowProvider,
        InitializingBean,
        WorkflowObservationManagerAware {

    private String key;
    private WorkflowService workflowService;
    private WorkflowObservationManager observationManager;
    private static JBPM6WorkflowProvider instance;
    private JahiaUserManagerService userManager;
    private JahiaGroupManagerService groupManager;
    private KieRepository kieRepository;
    private KieServices kieServices;
    private KieSession kieSession;
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
    }

    @Override
    public void addParticipatingGroup(String taskId, JahiaGroup group, String role) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteTask(String taskId, String reason) {
        //To change body of implemented methods use File | Settings | File Templates.
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
}
