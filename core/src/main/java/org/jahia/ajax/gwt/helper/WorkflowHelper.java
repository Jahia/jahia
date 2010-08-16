/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.workflow.*;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryItem;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryProcess;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryTask;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.PublicationInfo;
import org.jahia.services.rbac.Role;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.*;

import javax.jcr.RepositoryException;
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

    public GWTJahiaWorkflowInfo getWorkflowInfo(String path, JCRSessionWrapper session, Locale locale)
            throws GWTJahiaServiceException {
        try {
            GWTJahiaWorkflowInfo info = new GWTJahiaWorkflowInfo();

            List<GWTJahiaWorkflowDefinition> gwtWorkflowDefinitions = new ArrayList<GWTJahiaWorkflowDefinition>();
            info.setPossibleWorkflows(gwtWorkflowDefinitions);
            JCRNodeWrapper node = session.getNode(path);

            List<WorkflowDefinition> wfs = service.getPossibleWorkflows(node, session.getUser(),locale);
            for (WorkflowDefinition workflow : wfs) {
                gwtWorkflowDefinitions.add(createGWTJahiaWorkflowDefinition(workflow));
            }

            List<GWTJahiaWorkflowAction> gwtActions = new ArrayList<GWTJahiaWorkflowAction>();
            info.setAvailableActions(gwtActions);

            List<Workflow> actives = service.getActiveWorkflows(node,locale);
            for (Workflow workflow : actives) {
                if(workflow.getDuedate()!=null) {
                    info.setDuedate(workflow.getDuedate());
                }
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
                                action.setDisplayName(workflowAction.getDisplayName());
                                action.setId(workflowTask.getId());
                                action.setFormResourceName(workflowTask.getFormResourceName());
                                action.setCreateTime(workflowTask.getCreateTime());
                                action.setProcessId(workflowTask.getProcessId());
                                Map<String, Object> map = workflowTask.getVariables();
                                Map<String, GWTJahiaNodeProperty> properties = new HashMap<String, GWTJahiaNodeProperty>(map.size());
                                for (Map.Entry<String, Object> entry : map.entrySet()) {
                                    if (entry.getValue() instanceof List) {
                                        List variable = (List) entry.getValue();
                                        GWTJahiaNodeProperty value = new GWTJahiaNodeProperty();
                                        value.setName(entry.getKey());
                                        for (Object workflowVariable : variable) {
                                            if (workflowVariable instanceof WorkflowVariable) {
                                                value.setValue(new GWTJahiaNodePropertyValue(((WorkflowVariable)workflowVariable).getValue(), ((WorkflowVariable)workflowVariable).getType()));
                                            }
                                        }
                                        if (value.getValues() != null) {
                                            properties.put(entry.getKey(), value);
                                        }
                                    }
                                }
                                action.setVariables(properties);
                                action.setOriginalVariables(map);
                                action.setLocale(map.get("locale").toString());
                                action.setWorkspace(map.get("workspace").toString());
                                JahiaPrincipal principal = participation.getJahiaPrincipal();
                                if ((principal instanceof JahiaGroup && ((JahiaGroup) principal).isMember(session.getUser())) ||
                                    (principal instanceof JahiaUser && ((JahiaUser) principal).getUserKey().equals(session.getUser().getUserKey())) ||
                                    (principal instanceof Role && (session.getUser().hasRole((Role) principal)))) {
                                    Set<String> outcomes = workflowTask.getOutcomes();
                                    List<String> display = workflowTask.getDisplayOutcomes();
                                    int i=0;
                                    for (String outcome : outcomes) {
                                        GWTJahiaWorkflowOutcome gwtOutcome = new GWTJahiaWorkflowOutcome();
                                        gwtOutcome.setName(outcome);
                                        gwtOutcome.setLabel(display.get(i++));
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
        w.setDisplayName(workflow.getDisplayName());
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
            service.completeTask(action.getId(), action.getProvider(), outcome.getName(), map,session.getUser());
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
            boolean toBeAdded = false;
            for (GWTJahiaNodePropertyValue value : propertyValues) {
                String s = value.getString();
                if(s!=null && !"".equals(s)) {
                    values.add(new WorkflowVariable(s, value.getType()));
                    toBeAdded=true;
                }
            }
            if(toBeAdded) {
                map.put(property.getName(), values);
            } else {
                map.put(property.getName(),new ArrayList<WorkflowVariable>());
            }
        }
        return map;
    }

    public void addCommentToTask(GWTJahiaWorkflowAction action, String comment) {
        service.addCommentToTask(action.getId(), action.getProvider(), comment);
    }

    public List<GWTJahiaWorkflowTaskComment> getTaskComments(GWTJahiaWorkflowAction action) {
        WorkflowTask workflowTask = service.getWorkflowTask(action.getId(), action.getProvider(),null);
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

    public List<GWTJahiaWorkflowHistoryItem> getWorkflowHistoryItems(String nodeId, GWTJahiaWorkflowHistoryItem historyItem,
                                                                     JCRSessionWrapper session, Locale locale) throws GWTJahiaServiceException {
        List<GWTJahiaWorkflowHistoryItem> history = new ArrayList<GWTJahiaWorkflowHistoryItem>();
        try {
            if (historyItem != null) {
                // read tasks of the process
                List<HistoryWorkflowTask> tasks = service.getHistoryWorkflowTasks(historyItem.getProcessId(),
                        historyItem.getProvider(), locale);

                for (HistoryWorkflowTask wfTask : tasks) {
                    final String userKey = wfTask.getAssignee();
                    String userName = "";
                    if (userKey != null) {
                        userName = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userKey).getName();
                    }
                    history.add(new GWTJahiaWorkflowHistoryTask(wfTask.getName(),
                            wfTask.getDisplayName() + (wfTask.getDisplayOutcome() != null ? " : " + wfTask.getDisplayOutcome():""),
                            wfTask.getProcessId(), wfTask
                            .getProvider(), wfTask.isCompleted(), wfTask.getStartTime(), wfTask.getEndTime(), wfTask
                            .getDuration(), wfTask.getOutcome(), userName));
                }
            } else {
                // read all processes
                List<HistoryWorkflow> workflows = service.getHistoryWorkflows(session.getNodeByIdentifier(nodeId),
                        locale);
                for (HistoryWorkflow wf : workflows) {
                    history.add(new GWTJahiaWorkflowHistoryProcess(wf.getName(), wf.getDisplayName(), wf.getProcessId(), wf
                            .getProvider(), wf.getDefinition().getKey(), wf.isFinished(), wf.getStartTime(), wf.getEndTime(), wf.getDuration(),
                            null));
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
        return history;
    }

    public List<WorkflowTask> getAvailableTasksForUser(JahiaUser user, Locale locale) {
        return service.getTasksForUser(user,locale);
    }

    public Map<GWTJahiaWorkflowDefinition,GWTJahiaNodeACL> getWorkflowRules(String path, JCRSessionWrapper session,
                                                                            Locale locale) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            Map<GWTJahiaWorkflowDefinition,GWTJahiaNodeACL> defAclMap = new HashMap<GWTJahiaWorkflowDefinition, GWTJahiaNodeACL>();
            final Map<String, List<String[]>> map = service.getWorkflowRules(node, locale);
            for (Map.Entry<String, List<String[]>> entry : map.entrySet()) {
                try {
                    String wf = entry.getKey();
                    final WorkflowDefinition definition = service.getWorkflowDefinition(StringUtils.substringBefore(wf,
                                                                                                                    ":"),
                                                                                        StringUtils.substringAfter(wf,
                                                                                                                   ":"),
                                                                                        locale);
                    final GWTJahiaWorkflowDefinition workflowDefinition = createGWTJahiaWorkflowDefinition(definition);
                    GWTJahiaNodeACL acl = new GWTJahiaNodeACL();
                    Map<String, List<String>> permissions = new HashMap<String, List<String>>();
                    permissions.put("tasks", new LinkedList<String>(definition.getTasks()));
                    acl.setAvailablePermissions(permissions);
                    Map<String, GWTJahiaNodeACE> aces = new HashMap<String, GWTJahiaNodeACE>();
                    for (String[] acesString : entry.getValue()) {
                        String principal = acesString[3];
                        GWTJahiaNodeACE ace;
                        Map<String, String> perms;
                        Map<String, String> inheritedPerms;
                        if (!aces.containsKey(principal)) {
                            ace = new GWTJahiaNodeACE();
                            ace.setPrincipalType(principal.charAt(0));
                            ace.setPrincipal(principal.substring(2));
                            perms = new HashMap<String, String>();
                            inheritedPerms = new HashMap<String, String>();
                        } else {
                            ace = aces.get(principal);
                            perms = ace.getPermissions();
                            inheritedPerms = ace.getInheritedPermissions();
                        }
                        String inheritedFrom = null;
                        if (!path.equals(acesString[0])) {
                            inheritedFrom = acesString[0];
                            inheritedPerms.put(acesString[2], acesString[1]);
                        } else {
                            perms.put(acesString[2], acesString[1]);
                        }

                        ace.setInheritedFrom(inheritedFrom);
                        ace.setInheritedPermissions(inheritedPerms);
                        ace.setPermissions(perms);
                        final boolean b = perms.isEmpty();
                        ace.setInherited(b);

                        aces.put(principal, ace);
                    }
                    acl.setAce(new LinkedList<GWTJahiaNodeACE>(aces.values()));
                    acl.setPermissionsDependencies(new HashMap<String, List<String>>());
                    defAclMap.put(workflowDefinition, acl);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            return defAclMap;
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public List<GWTJahiaWorkflowDefinition> getWorkflows(Locale locale) throws GWTJahiaServiceException {
        try {
            final Set<String> workflowTypes = service.getTypesOfWorkflow();
            List<GWTJahiaWorkflowDefinition> definitions = new ArrayList<GWTJahiaWorkflowDefinition>();
            for (String workflowType : workflowTypes) {
                final List<WorkflowDefinition> workflowDefinitions = service.getWorkflowsForAction(workflowType,
                                                                                                   locale);
                for (WorkflowDefinition definition : workflowDefinitions) {
                    definitions.add(createGWTJahiaWorkflowDefinition(definition));
                }
            }
            return definitions;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void updateWorkflowRules(String path, List<GWTJahiaWorkflowDefinition> actives,
                                    List<GWTJahiaWorkflowDefinition> deleted, JCRSessionWrapper session,
                                    Locale locale) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            if(!node.isCheckedOut()) {
                session.checkout(node);
            }
            // If existing remove all unchecked nodes
            if (node.hasNode(WorkflowService.WORKFLOWRULES_NODE_NAME)) {
                JCRNodeWrapper wfRulesNode = node.getNode(WorkflowService.WORKFLOWRULES_NODE_NAME);
                if(!wfRulesNode.isCheckedOut()) {
                    session.checkout(wfRulesNode);
                }
                for (GWTJahiaWorkflowDefinition definition : deleted) {
                    final String defKey = definition.getProvider() + "_" + definition.getId();
                    if (wfRulesNode.hasNode(defKey)) {
                        wfRulesNode.getNode(defKey).remove();
                    }
                }
                if (actives == null || actives.isEmpty()) {
                    // No more active definitions for this nodes
                    wfRulesNode.remove();
                    // Remove also associated workflows
                    if (node.isNodeType("jmix:worklfowRulesable")) {
                        node.removeMixin("jmix:worklfowRulesable");
                    }
                    if (node.isNodeType("jmix:publication")) {
                        node.removeMixin("jmix:publication");
                    }
                }
            }
            if (actives != null && !actives.isEmpty()) {
                // Add or let untouch existing node
                if (!node.isNodeType("jmix:worklfowRulesable")) {
                    node.addMixin("jmix:worklfowRulesable");
                }
                if (!node.isNodeType("jmix:publication")) {
                    node.addMixin("jmix:publication");
                }
                try {
                    node = node.getNode(WorkflowService.WORKFLOWRULES_NODE_NAME);
                    if(!node.isCheckedOut()) {
                        session.checkout(node);
                    }
                } catch (RepositoryException e) {
                    node = node.addNode(WorkflowService.WORKFLOWRULES_NODE_NAME, "jnt:workflowRules");
                }
                for (GWTJahiaWorkflowDefinition definition : actives) {
                    final String defKey = definition.getProvider() + "_" + definition.getId();
                    if (!node.hasNode(defKey)) {
                        final JCRNodeWrapper wfRuleNode = node.addNode(defKey, "jnt:workflowRule");
                        wfRuleNode.setProperty("j:workflow", definition.getProvider() + ":" + definition.getId());
                    }
                }
            }
            session.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void updateWorkflowRulesACL(String path, GWTJahiaWorkflowDefinition workflowDefinition,
                                       GWTJahiaNodeACL nodeACL, JCRSessionWrapper session, Locale locale)
            throws GWTJahiaServiceException {
        JCRNodeWrapper node = null;
        try {
            node = session.getNode(path);
            updateWorkflowRules(path, Arrays.asList(workflowDefinition), new LinkedList<GWTJahiaWorkflowDefinition>(),
                                session, locale);
            // If existing remove all unchecked nodes
            if (node.hasNode(WorkflowService.WORKFLOWRULES_NODE_NAME)) {
                node = node.getNode(WorkflowService.WORKFLOWRULES_NODE_NAME);
                final String defKey = workflowDefinition.getProvider() + "_" + workflowDefinition.getId();
                if (node.hasNode(defKey)) {
                    node = node.getNode(defKey);
                    if(!node.isCheckedOut()) {
                        session.checkout(node);
                    }
                    // So we have our jnt:worklfowRule object let's manage the ACE now.
                    List<GWTJahiaNodeACE> aces = nodeACL.getAce();
                    boolean asLocalAce = false;
                    for (GWTJahiaNodeACE ace : aces) {
                        String principal = ace.getPrincipal();
                        if (!ace.isInherited()) {
                            asLocalAce = true;
                            Map<String, String> permissions = ace.getPermissions();
                            List<String> granted = new LinkedList<String>();
                            List<String> denied = new LinkedList<String>();
                            for (Map.Entry<String, String> entry : permissions.entrySet()) {
                                if ("GRANT".equals(entry.getValue())) {
                                    granted.add(entry.getKey());
                                } else {
                                    denied.add(entry.getKey());
                                }
                            }
                            saveAce(node, ace, principal, granted, "GRANT");
                            saveAce(node, ace, principal, denied, "DENY");
                        } else {
                            String nodeName = "GRANT_" + ace.getPrincipalType() + "_" + principal;
                            JCRNodeWrapper aceNode;
                            if (node.hasNode(nodeName)) {
                                aceNode = node.getNode(nodeName);
                                aceNode.remove();
                            }
                            nodeName = "DENY_" + ace.getPrincipalType() + "_" + principal;
                            if (node.hasNode(nodeName)) {
                                aceNode = node.getNode(nodeName);
                                aceNode.remove();
                            }
                        }
                    }
                    if (!asLocalAce) {
                        node.remove();
                    }
                }
                session.save();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    private void saveAce(JCRNodeWrapper node, GWTJahiaNodeACE ace, String principal, List<String> permissions, String typeOfPermission)
            throws RepositoryException {
        String nodeName = typeOfPermission+"_" + ace.getPrincipalType() + "_" + principal;
        JCRNodeWrapper aceNode;
        if (node.hasNode(nodeName)) {
            aceNode = node.getNode(nodeName);
            if(!aceNode.isCheckedOut()) {
                node.getSession().checkout(aceNode);
            }
        } else {
            aceNode = node.addNode(nodeName, "jnt:ace");
        }
        aceNode.setProperty("j:principal", ace.getPrincipalType() + ":" + principal);
        aceNode.setProperty("j:protected", false);
        aceNode.setProperty("j:aceType", typeOfPermission);
        String[] grs = new String[permissions.size()];
        permissions.toArray(grs);
        aceNode.setProperty("j:privileges", grs);
    }
}
