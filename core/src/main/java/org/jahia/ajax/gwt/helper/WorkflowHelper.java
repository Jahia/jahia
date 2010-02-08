package org.jahia.ajax.gwt.helper;

import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowAction;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowOutcome;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.workflow.*;

import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 3:48:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowHelper {

    private WorkflowService service;

    public void setService(WorkflowService service) {
        this.service = service;
    }

    public GWTJahiaWorkflowInfo getWorkflowInfo(String path, JCRSessionWrapper session) throws GWTJahiaServiceException {
        try {
            GWTJahiaWorkflowInfo info = new GWTJahiaWorkflowInfo();

            List<GWTJahiaWorkflowDefinition> gwtWorkflowDefinitions = new ArrayList<GWTJahiaWorkflowDefinition>();
            info.setPossibleWorkflows(gwtWorkflowDefinitions);
            JCRNodeWrapper node = session.getNode(path);

            List<WorkflowDefinition> wfs = service.getPossibleWorkflows(node);
            for (WorkflowDefinition workflow : wfs) {
                GWTJahiaWorkflowDefinition w = new GWTJahiaWorkflowDefinition();
                w.setProvider(workflow.getProvider());
                w.setName(workflow.getName());
                w.setId(workflow.getKey());
                gwtWorkflowDefinitions.add(w);
            }

            List<GWTJahiaWorkflowAction> gwtActions = new ArrayList<GWTJahiaWorkflowAction>();
            info.setAvailableActions(gwtActions);

            Map<String, List<Workflow>> actives = service.getActiveWorkflows(node);
            for (Map.Entry<String, List<Workflow>> entry : actives.entrySet()) {
                for (Workflow workflow : entry.getValue()) {
                    for (WorkflowAction workflowAction : workflow.getAvailableActions()) {
                        if (workflowAction instanceof WorkflowTask) {
                            WorkflowTask workflowTask = (WorkflowTask) workflowAction;
                            List<WorkflowParticipation> participations = workflowTask.getParticipations();
                            for (WorkflowParticipation participation : participations) {
                                if (participation.getJahiaGroup().isMember(session.getUser())) {
                                    GWTJahiaWorkflowAction action = new GWTJahiaWorkflowAction();
                                    gwtActions.add(action);
                                    List<GWTJahiaWorkflowOutcome> gwtOutcomes = new ArrayList<GWTJahiaWorkflowOutcome>();
                                    action.setProvider(entry.getKey());
                                    action.setOutcomes(gwtOutcomes);
                                    action.setName(workflowAction.getName());
                                    action.setId(workflowTask.getId());
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

    public void startWorkflow(String path, GWTJahiaWorkflowDefinition def, JCRSessionWrapper session) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            service.startProcess(node, def.getId(), def.getProvider(), new HashMap<String,Object>());
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void assignAndCompleteTask(String path, GWTJahiaWorkflowAction action, GWTJahiaWorkflowOutcome outcome, JCRSessionWrapper session) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            service.assignTask(action.getId(), action.getProvider(), session.getUser());
            service.completeTask(action.getId(), action.getProvider(), outcome.getName(), new HashMap<String, Object>());
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

}
