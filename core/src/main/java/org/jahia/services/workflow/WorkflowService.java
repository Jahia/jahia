/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.workflow;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 2 févr. 2010
 */
public class WorkflowService {
    private transient static Logger logger = Logger.getLogger(WorkflowService.class);


    private Map<String, WorkflowProvider> providers;
    private static WorkflowService instance;
    public static final String CANDIDATE = "candidate";

    public static WorkflowService getInstance() {
        if (instance == null) {
            instance = new WorkflowService();
        }
        return instance;
    }

    public void setProviders(Map<String, WorkflowProvider> providers) {
        this.providers = providers;
    }

    public Map<String, WorkflowProvider> getProviders() {
        return providers;
    }

    /**
     * This method list all possible workflows for the specified node.
     *
     * @param node
     * @return A list of available workflows per provider.
     */
    public List<WorkflowDefinition> getPossibleWorkflows(JCRNodeWrapper node) throws RepositoryException {
        List<WorkflowDefinition> workflowsByProvider = new ArrayList<WorkflowDefinition>();
        if (node.isNodeType("jnt:page")) {
            for (Map.Entry<String, WorkflowProvider> providerEntry : providers.entrySet()) {
                for (WorkflowDefinition definition : providerEntry.getValue().getAvailableWorkflows()) {
                    definition.setProvider(providerEntry.getKey());
                    workflowsByProvider.add(definition);
                }
            }
        }
        return workflowsByProvider;
    }

    /**
     * This method list all currently active workflow for the specified node.
     *
     * @param node
     * @return A map of active workflows per provider
     */
    public Map<String, List<Workflow>> getActiveWorkflows(JCRNodeWrapper node) {
        Map<String, List<Workflow>> workflowsByProvider = new LinkedHashMap<String, List<Workflow>>();
        try {
            if (node.isNodeType("jmix:workflow") && node.hasProperty(Constants.PROCESSID)) {
                Property p = node.getProperty(Constants.PROCESSID);
                Value[] values = p.getValues();
                for (Map.Entry<String, WorkflowProvider> entry : providers.entrySet()) {
                    final List<String> processIds = new ArrayList<String>(values.length);
                    for (Value value : values) {
                        String key = value.getString();
                        String processId = StringUtils.substringAfter(key, ":");
                        String providerKey = StringUtils.substringBefore(key, ":");
                        if (providerKey.equals(entry.getKey())) {
                            processIds.add(processId);
                        }
                    }
                    workflowsByProvider.put(entry.getKey(), entry.getValue().getActiveWorkflowsInformations(
                            processIds));
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return workflowsByProvider;
    }

    /**
     * This method list all actions available at execution time for a node.
     *
     * @param processId the process we want to advance
     * @param provider  The provider executing the process
     * @return a set of actions per workflows per provider.
     */
    public Set<WorkflowAction> getAvailableActions(String processId, String provider) {
        return providers.get(provider).getAvailableActions(processId);
    }

    /**
     * This method will call the underlying provider to signal the identified process.
     *
     * @param processId the process we want to advance
     * @param provider  The provider executing the process
     * @param s
     * @param args      List of args for the process
     */
    public void signalProcess(String processId, String transitionName, String provider, Map<String, Object> args) {
        providers.get(provider).signalProcess(processId, transitionName, args);
    }

    /**
     * This method will call the underlying provider to signal the identified process.
     *
     * @param processId the process we want to advance
     * @param provider  The provider executing the process
     * @param s
     * @param args      List of args for the process
     */
    public void signalProcess(String processId, String transitionName, String signalName, String provider, Map<String, Object> args) {
        providers.get(provider).signalProcess(processId, transitionName, signalName, args);
    }

    /**
     * This method will call the underlying provider to signal the identified process.
     *
     * @param stageNode
     * @param provider  The provider executing the process
     * @param args      Map of args for the process
     */
    public String startProcess(JCRNodeWrapper stageNode, String processKey, String provider, Map<String, Object> args)
            throws RepositoryException {
        args.put("nodeId", stageNode.getIdentifier());
        final String processId = providers.get(provider).startProcess(processKey, args);
        stageNode.checkout();
        if (!stageNode.isNodeType("jmix:workflow")) {
            stageNode.addMixin("jmix:workflow");
        }
        List<Value> values;
        if (stageNode.hasProperty(Constants.PROCESSID)) {
            values = new ArrayList<Value>(Arrays.asList(stageNode.getProperty(Constants.PROCESSID).getValues()));
        } else {
            values = new ArrayList<Value>();
        }
        values.add(stageNode.getSession().getValueFactory().createValue(provider + ":" + processId));
        stageNode.setProperty(Constants.PROCESSID, values.toArray(new Value[values.size()]));
        stageNode.getSession().save();
        return processId;
    }

    public List<WorkflowTask> getTasksForUser(JahiaUser user) {
        final List<WorkflowTask> workflowActions = new LinkedList<WorkflowTask>();
        for (Map.Entry<String, WorkflowProvider> providerEntry : providers.entrySet()) {
            workflowActions.addAll(providerEntry.getValue().getTasksForUser(user));
        }
        return workflowActions;
    }

    public void assignTask(String taskId, String provider, JahiaUser user) {
        logger.debug("Assigning user " + user + " to task " + taskId);
        providers.get(provider).assignTask(taskId, user);
    }

    public void completeTask(String taskId, String provider, String outcome, Map<String, Object> args) {
        providers.get(provider).completeTask(taskId, outcome, args);
    }

    public void addParticipatingGroup(String taskId, String provider, JahiaGroup group, String role) {
        providers.get(provider).addParticipatingGroup(taskId, group, role);
    }

    public void deleteTask(String taskId, String provider, String reason) {
        providers.get(provider).deleteTask(taskId, reason);
    }
}
