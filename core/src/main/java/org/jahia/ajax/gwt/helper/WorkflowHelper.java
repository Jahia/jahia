package org.jahia.ajax.gwt.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.workflow.*;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryItem;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryProcess;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryTask;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.*;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.*;

/**
 * Workflow operation helper for the GWT backend.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 3:48:28 PM
 */
public class WorkflowHelper {
    private static final transient Logger logger = Logger.getLogger(WorkflowHelper.class);

    private WorkflowService service;

    public void setService(WorkflowService service) {
        this.service = service;
    }

    public GWTJahiaWorkflowInfo getWorkflowInfo(String path, JCRSessionWrapper session)
            throws GWTJahiaServiceException {
        try {
            GWTJahiaWorkflowInfo info = new GWTJahiaWorkflowInfo();

            List<GWTJahiaWorkflowDefinition> gwtWorkflowDefinitions = new ArrayList<GWTJahiaWorkflowDefinition>();
            info.setPossibleWorkflows(gwtWorkflowDefinitions);
            JCRNodeWrapper node = session.getNode(path);

            List<WorkflowDefinition> wfs = service.getPossibleWorkflows(node, session.getUser());
            for (WorkflowDefinition workflow : wfs) {
                gwtWorkflowDefinitions.add(createGWTJahiaWorkflowDefinition(workflow));
            }

            List<GWTJahiaWorkflowAction> gwtActions = new ArrayList<GWTJahiaWorkflowAction>();
            info.setAvailableActions(gwtActions);

            List<Workflow> actives = service.getActiveWorkflows(node);
            for (Workflow workflow : actives) {
                for (WorkflowAction workflowAction : workflow.getAvailableActions()) {
                    if (workflowAction instanceof WorkflowTask) {
                        WorkflowTask workflowTask = (WorkflowTask) workflowAction;
                        List<WorkflowParticipation> participations = workflowTask.getParticipations();
                        if (participations != null) {
                            for (WorkflowParticipation participation : participations) {
                                GWTJahiaWorkflowAction action = new GWTJahiaWorkflowAction();
                                gwtActions.add(action);
                                List<GWTJahiaWorkflowOutcome> gwtOutcomes = new ArrayList<GWTJahiaWorkflowOutcome>();
                                action.setProvider(workflow.getProvider());
                                action.setOutcomes(gwtOutcomes);
                                action.setName(workflowAction.getName());
                                action.setId(workflowTask.getId());
                                action.setFormResourceName(workflowTask.getFormResourceName());
                                action.setCreateTime(workflowTask.getCreateTime());
                                action.setProcessId(workflowTask.getProcessId());
                                Map<String, Object> map = workflowTask.getVariables();
                                Map<String, GWTJahiaNodeProperty> properties = new HashMap<String, GWTJahiaNodeProperty>(map.size());
                                for (Map.Entry<String, Object> entry : map.entrySet()) {
                                    if (entry.getValue() instanceof List) {
                                        List<WorkflowVariable> variable = (List<WorkflowVariable>) entry.getValue();
                                        GWTJahiaNodeProperty value = new GWTJahiaNodeProperty();
                                        value.setName(entry.getKey());
                                        for (WorkflowVariable workflowVariable : variable) {
                                            value.setValue(new GWTJahiaNodePropertyValue(workflowVariable.getValue(), workflowVariable.getType()));
                                        }
                                        properties.put(entry.getKey(), value);
                                    }
                                }
                                action.setVariables(properties);
                                action.setLocale(map.get("locale").toString());
                                action.setWorkspace(map.get("workspace").toString());
                                if ((participation.getJahiaPrincipal() instanceof JahiaGroup && ((JahiaGroup) participation.getJahiaPrincipal()).isMember(
                                        session.getUser())) || (participation.getJahiaPrincipal() instanceof JahiaUser && ((JahiaUser) participation.getJahiaPrincipal()).getUserKey().equals(
                                        session.getUser().getUserKey()))) {
                                    Set<String> outcomes = workflowTask.getOutcomes();
                                    for (String outcome : outcomes) {
                                        GWTJahiaWorkflowOutcome gwtOutcome = new GWTJahiaWorkflowOutcome();
                                        gwtOutcome.setName(outcome);
                                        gwtOutcome.setLabel(outcome);
                                        gwtOutcomes.add(gwtOutcome);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            return info;
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    private GWTJahiaWorkflowDefinition createGWTJahiaWorkflowDefinition(WorkflowDefinition workflow) {
        GWTJahiaWorkflowDefinition w = new GWTJahiaWorkflowDefinition();
        w.setProvider(workflow.getProvider());
        w.setName(workflow.getName());
        w.setId(workflow.getKey());
        w.setFormResourceName(workflow.getFormResourceName());
        return w;
    }

    public void startWorkflow(String path, GWTJahiaWorkflowDefinition def, JCRSessionWrapper session,
                              List<GWTJahiaNodeProperty> properties)
            throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            HashMap<String, Object> map = getVariablesMap(properties);
            service.startProcess(node, def.getId(), def.getProvider(), map);
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void assignAndCompleteTask(String path, GWTJahiaWorkflowAction action, GWTJahiaWorkflowOutcome outcome,
                                      JCRSessionWrapper session, List<GWTJahiaNodeProperty> properties) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            service.assignTask(action.getId(), action.getProvider(), session.getUser());
            HashMap<String, Object> map = getVariablesMap(properties);
            service.completeTask(action.getId(), action.getProvider(), outcome.getName(), map);
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    private HashMap<String, Object> getVariablesMap(List<GWTJahiaNodeProperty> properties) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (GWTJahiaNodeProperty property : properties) {
            List<GWTJahiaNodePropertyValue> propertyValues = property.getValues();
            List<WorkflowVariable> values = new ArrayList<WorkflowVariable>(propertyValues.size());
            for (GWTJahiaNodePropertyValue value : propertyValues) {
                values.add(new WorkflowVariable(value.getString(), value.getType()));
            }
            map.put(property.getName(), values);
        }
        return map;
    }

    public void addCommentToTask(GWTJahiaWorkflowAction action, String comment) {
        service.addCommentToTask(action.getId(), action.getProvider(), comment);
    }

    public List<GWTJahiaWorkflowTaskComment> getTaskComments(GWTJahiaWorkflowAction action) {
        WorkflowTask workflowTask = service.getWorkflowTask(action.getId(), action.getProvider());
        List<GWTJahiaWorkflowTaskComment> taskComments = new ArrayList<GWTJahiaWorkflowTaskComment>();
        List<WorkflowTaskComment> workflowTaskComments = workflowTask.getTaskComments();
        for (WorkflowTaskComment comment : workflowTaskComments) {
            GWTJahiaWorkflowTaskComment taskComment = new GWTJahiaWorkflowTaskComment();
            taskComment.setComment(comment.getComment());
            taskComment.setTime(comment.getTime());
            taskComment.setUser(comment.getUser());
            taskComments.add(taskComment);
        }
        return taskComments;
    }

    public List<GWTJahiaWorkflowHistoryItem> getWorkflowHistoryItems(String nodeId,
            GWTJahiaWorkflowHistoryItem historyItem, JCRSessionWrapper session) throws GWTJahiaServiceException {
        List<GWTJahiaWorkflowHistoryItem> history = new ArrayList<GWTJahiaWorkflowHistoryItem>();
        try {
            if (historyItem != null) {
                // read tasks of the process
                List<HistoryWorkflowTask> tasks = service.getHistoryWorkflowTasks(historyItem.getProcessId(),
                        historyItem.getProvider());
                for (HistoryWorkflowTask wfTask : tasks) {
                    final String userKey = wfTask.getAssignee();
                    String userName = "";
                    if (userKey != null) {
                        userName = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userKey).getName();
                    }
                    history.add(new GWTJahiaWorkflowHistoryTask(wfTask.getName() + (wfTask.getOutcome() != null ? " : " + wfTask.getOutcome():""), wfTask.getProcessId(), wfTask
                            .getProvider(), wfTask.isCompleted(), wfTask.getStartTime(), wfTask.getEndTime(), wfTask
                            .getDuration(), wfTask.getOutcome(), userName));
                }
            } else {
                // read all processes
                List<HistoryWorkflow> workflows = service.getHistoryWorkflows(session.getNodeByIdentifier(nodeId));
                for (HistoryWorkflow wf : workflows) {
                    history.add(new GWTJahiaWorkflowHistoryProcess(wf.getDefinitionKey(), wf.getProcessId(), wf
                            .getProvider(), wf.isFinished(), wf.getStartTime(), wf.getEndTime(), wf.getDuration()));
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
        return history;
    }

    public List<WorkflowTask> getAvailableTasksForUser(JahiaUser user) {
        return service.getTasksForUser(user);
    }
}
