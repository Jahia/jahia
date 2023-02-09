/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.workflow.*;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryItem;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryProcess;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryTask;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.widget.poller.TaskEvent;
import org.jahia.ajax.gwt.client.widget.workflow.CustomWorkflow;
import org.jahia.ajax.gwt.commons.server.ChannelHolder;
import org.jahia.ajax.gwt.commons.server.JGroupsChannel;
import org.jahia.ajax.gwt.commons.server.ManagedGWTResource;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.atmosphere.AtmosphereServlet;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.*;
import org.jahia.services.workflow.*;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Workflow operation helper for the GWT backend.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 3:48:28 PM
 */
public class WorkflowHelper {
    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(WorkflowHelper.class);

    private WorkflowService service;
    private JahiaUserManagerService userManagerService;
    private JahiaGroupManagerService groupManagerService;
    private ContentDefinitionHelper contentDefinitionHelper;

    public void start() {
        service.addWorkflowListener(new PollingWorkflowListener());
    }

    public GWTJahiaWorkflowInfo getWorkflowInfo(String path, boolean includeActiveWorfklows, JCRSessionWrapper session, Locale locale, Locale uiLocale)
            throws GWTJahiaServiceException {
        try {
            GWTJahiaWorkflowInfo info = new GWTJahiaWorkflowInfo();

            Map<GWTJahiaWorkflowType, GWTJahiaWorkflowDefinition> gwtWorkflowDefinitions = new HashMap<GWTJahiaWorkflowType, GWTJahiaWorkflowDefinition>();
            info.setPossibleWorkflows(gwtWorkflowDefinitions);
            JCRNodeWrapper node = session.getNode(path);

            Map<String, WorkflowDefinition> wfs = service.getPossibleWorkflows(node, true, locale);
            for (Map.Entry<String, WorkflowDefinition> entry : wfs.entrySet()) {
                gwtWorkflowDefinitions.put(getGWTJahiaWorkflowType(entry.getKey()), getGWTJahiaWorkflowDefinition(entry.getValue()));
            }

            Map<GWTJahiaWorkflowType, GWTJahiaWorkflow> gwtWorkflows = new HashMap<GWTJahiaWorkflowType, GWTJahiaWorkflow>();
            info.setActiveWorkflows(gwtWorkflows);
            if (includeActiveWorfklows) {
                List<Workflow> actives = service.getActiveWorkflows(node, locale, uiLocale);
                for (Workflow workflow : actives) {
                    GWTJahiaWorkflow gwtWf = getGWTJahiaWorkflow(workflow);
                    gwtWorkflows.put(getGWTJahiaWorkflowType(service.getWorkflowType(workflow.getWorkflowDefinition())), gwtWf);
                    for (WorkflowAction workflowAction : workflow.getAvailableActions()) {
                        if (workflowAction instanceof WorkflowTask) {
                            WorkflowTask workflowTask = (WorkflowTask) workflowAction;
                            List<WorkflowParticipation> participations = workflowTask.getParticipations();
                            if (participations != null) {
                                for (WorkflowParticipation participation : participations) {
                                    JahiaPrincipal principal = participation.getJahiaPrincipal();
                                    if (principal instanceof JahiaGroup) {
                                        JCRGroupNode groupNode = groupManagerService.lookupGroupByPath(principal.getLocalPath());
                                        JCRUserNode userNode = userManagerService.lookupUserByPath(session.getUser().getLocalPath());
                                        if (groupNode != null && userNode != null && groupNode.isMember(userNode)) {
                                            gwtWf.getAvailableTasks().add(getGWTJahiaWorkflowTask(workflowTask, null));
                                            break;
                                        }
                                    }
                                    if (principal instanceof JahiaUser && principal.getLocalPath().equals(session.getUser().getLocalPath())) {
                                        gwtWf.getAvailableTasks().add(getGWTJahiaWorkflowTask(workflowTask, null));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return info;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Cannot get workflow info for " + path + ". Cause: " + e.getLocalizedMessage(), e);
        }
    }

    public GWTJahiaWorkflow getGWTJahiaWorkflow(Workflow wf) {
        GWTJahiaWorkflow gwtWf;
        gwtWf = new GWTJahiaWorkflow();
        gwtWf.setId(wf.getId());
        gwtWf.setProvider(wf.getProvider());
        gwtWf.setDefinition(getGWTJahiaWorkflowDefinition(wf.getWorkflowDefinition()));
        gwtWf.setAvailableTasks(new ArrayList<GWTJahiaWorkflowTask>());
        Map<String, Object> map = wf.getVariables();
        gwtWf.setStartTime(wf.getStartTime());
        gwtWf.setVariables(getPropertiesMap(map));
        if (map.get("customWorkflowInfo") != null) {
            gwtWf.setCustomWorkflowInfo((CustomWorkflow) map.get("customWorkflowInfo"));
        }
        gwtWf.setLocale(map.get("locale").toString());
        gwtWf.setWorkspace(map.get("workspace").toString());
        if (wf.getDuedate() != null) {
            gwtWf.setDuedate(wf.getDuedate());
        }
        return gwtWf;
    }

    public GWTJahiaWorkflowTask getGWTJahiaWorkflowTask(WorkflowTask workflowTask, JCRNodeWrapper nodeWrapper) {
        GWTJahiaWorkflowTask task = new GWTJahiaWorkflowTask();
        List<GWTJahiaWorkflowOutcome> gwtOutcomes = new ArrayList<GWTJahiaWorkflowOutcome>();
        task.setProvider(workflowTask.getProvider());
        task.setOutcomes(gwtOutcomes);
        task.setName(workflowTask.getName());
        task.setDisplayName(workflowTask.getDisplayName());
        task.setId(workflowTask.getId());
        task.setFormResourceName(workflowTask.getFormResourceName());
        task.setCreateTime(workflowTask.getCreateTime());
        task.setProcessId(workflowTask.getProcessId());
        Set<String> outcomes = workflowTask.getOutcomes();
        Map<String, String> outcomesPermissions = workflowTask.getOutcomesPermissions();
        List<String> display = workflowTask.getDisplayOutcomes();
        List<String> icons = workflowTask.getOutcomeIcons();
        int i = 0;
        for (String outcome : outcomes) {
            if (!outcomesPermissions.containsKey(outcome) || nodeWrapper == null || nodeWrapper.hasPermission(outcomesPermissions.get(outcome))) {
                GWTJahiaWorkflowOutcome gwtOutcome = new GWTJahiaWorkflowOutcome();
                gwtOutcome.setName(outcome);
                gwtOutcome.setLabel(display.get(i));
                gwtOutcome.setIcon(icons.get(i));
                gwtOutcomes.add(gwtOutcome);
            }
            i++;
        }
        task.setVariables(getPropertiesMap(workflowTask.getVariables()));
        return task;
    }

    private GWTJahiaWorkflowHistoryProcess getGWTJahiaHistoryProcess(HistoryWorkflow wf) {
        return new GWTJahiaWorkflowHistoryProcess(wf.getName(), wf.getDisplayName(), wf.getProcessId(), wf
                .getProvider(), wf.getWorkflowDefinition().getKey(), wf.isCompleted(), wf.getStartTime(), wf.getEndTime(), wf.getDuration(),
                getUsername(wf.getUser()), wf.getNodeId());
    }

    public GWTJahiaWorkflowDefinition getGWTJahiaWorkflowDefinition(String key, Locale uiLocale) {
        return getGWTJahiaWorkflowDefinition(service.getWorkflowDefinition(StringUtils.substringBefore(key, ":"), StringUtils.substringAfter(key, ":"), uiLocale));
    }

    public GWTJahiaWorkflowDefinition getGWTJahiaWorkflowDefinition(WorkflowDefinition workflow) {
        GWTJahiaWorkflowDefinition w = new GWTJahiaWorkflowDefinition();
        w.setProvider(workflow.getProvider());
        w.setName(workflow.getName());
        w.setId(workflow.getKey());
        w.setFormResourceName(workflow.getFormResourceName());
        w.setDisplayName(workflow.getDisplayName());
        return w;
    }

    public void startWorkflow(String path, GWTJahiaWorkflowDefinition def, JCRSessionWrapper session,
                              List<GWTJahiaNodeProperty> properties, List<String> comments)
            throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            Map<String, Object> map = getVariablesMap(properties, def.getFormResourceName());
            service.startProcessAsJob(Arrays.asList(node.getIdentifier()), session, def.getId(), def.getProvider(), map, comments);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Cannot start workflow " + path + ". Cause: " + e.getLocalizedMessage());
        }
    }

    public void startWorkflow(List<String> uuids, GWTJahiaWorkflowDefinition def, JCRSessionWrapper session,
                              List<GWTJahiaNodeProperty> properties, List<String> comments, Map<String, Object> args)
            throws GWTJahiaServiceException {

        try {
            Map<String, Object> map = getVariablesMap(properties, def.getFormResourceName());
            map.putAll(args);
            service.startProcessAsJob(uuids, session, def.getId(), def.getProvider(), map, comments);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Cannot start workflows " + uuids + ". Cause: " + e.getLocalizedMessage());
        }
    }

    public void abortWorkflow(String processId, String provider) {
        try {
            Workflow w = service.getWorkflow(provider, processId, null);
            if (w != null && w.getWorkflowDefinition().getWorkflowType().equals("publish")) {
                @SuppressWarnings("unchecked")
                List<String> info = (List<String>) w.getVariables().get("nodeIds");
                String workspace = (String) w.getVariables().get("workspace");
                JCRPublicationService.getInstance().unlockForPublication(info, workspace, "publication-process-" + processId);
            }
        } catch (Exception e) {
            logger.error("Cannot clear workflow locks", e);
        }
        service.abortProcess(processId, provider);
    }

    public void assignAndCompleteTask(GWTJahiaWorkflowTask task, GWTJahiaWorkflowOutcome outcome,
                                      JCRSessionWrapper session, List<GWTJahiaNodeProperty> properties) throws GWTJahiaServiceException {
        try {
            Map<String, Object> map = getVariablesMap(properties,task.getFormResourceName());
            service.assignAndCompleteTask(task.getId(), task.getProvider(), outcome.getName(), map, session.getUser());
        } catch (Exception e) {
            logger.error("Exception in task", e);
            throw new GWTJahiaServiceException("Cannot assign and complete task " + task.getName() + ". Cause: " + e.getLocalizedMessage(), e);
        }
    }

    private Map<String, GWTJahiaNodeProperty> getPropertiesMap(Map<String, Object> variables) {
        Map<String, GWTJahiaNodeProperty> properties = new HashMap<String, GWTJahiaNodeProperty>(variables.size());
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            GWTJahiaNodeProperty property = new GWTJahiaNodeProperty();
            property.setName(entry.getKey());
            Object variable = entry.getValue();
            if (variable instanceof List) {
                List list = (List) variable;
                List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>();
                for (Object o : list) {
                    if (o instanceof WorkflowVariable) {
                        values.add(new GWTJahiaNodePropertyValue(((WorkflowVariable) o).getValue(), ((WorkflowVariable) o).getType()));
                    }
                }
                if (!values.isEmpty()) {
                    property.setValues(values);
                    properties.put(entry.getKey(), property);
                }
            } else if (variable instanceof WorkflowVariable) {
                WorkflowVariable workflowVariable = (WorkflowVariable) variable;
                String value = workflowVariable.getValue();
                if (workflowVariable.getType() == GWTJahiaNodePropertyType.DATE) {
                    Calendar cal = ISO8601.parse(value);
                    value = ContentDefinitionHelper.dateTimeFormat.format(cal.getTime());
                }
                property.setValue(new GWTJahiaNodePropertyValue(value, workflowVariable.getType()));
                properties.put(entry.getKey(), property);
            }
        }
        return properties;
    }

    private Map<String, Object> getVariablesMap(List<GWTJahiaNodeProperty> properties, String nodeTypeName) throws RepositoryException {
        Map<String, Object> map = new HashMap<String, Object>();
        if (properties.size() > 0) {
            ExtendedNodeType nodeType = nodeTypeName != null ? NodeTypeRegistry.getInstance().getNodeType(nodeTypeName) : null;
            for (GWTJahiaNodeProperty property : properties) {
                List<GWTJahiaNodePropertyValue> propertyValues = property.getValues();
                ExtendedPropertyDefinition epd = nodeType.getPropertyDefinitionsAsMap().get(property.getName());
                if (property.isMultiple()) {
                    List<WorkflowVariable> values = new ArrayList<WorkflowVariable>();
                    for (GWTJahiaNodePropertyValue value : propertyValues) {
                        String s = contentDefinitionHelper.convertValue(value, epd).getString();
                        if (StringUtils.isNotBlank(s)) {
                            values.add(new WorkflowVariable(s, value.getType()));
                        }
                    }
                    map.put(property.getName(), values);
                } else if (!propertyValues.isEmpty()) {
                    GWTJahiaNodePropertyValue value = propertyValues.get(0);
                    String s = contentDefinitionHelper.convertValue(value, epd).getString();
                    if (StringUtils.isNotBlank(s)) {
                        map.put(property.getName(), new WorkflowVariable(s, value.getType()));
                    }
                }
            }
        }
        return map;
    }

    public void addCommentToWorkflow(GWTJahiaWorkflow workflow, JahiaUser user, String comment, Locale locale) {
        service.addComment(workflow.getId(), workflow.getProvider(), comment, user.getUserKey());
    }

    public List<GWTJahiaWorkflowComment> getWorkflowComments(GWTJahiaWorkflow workflow, Locale locale) {
        Workflow wf = service.getWorkflow(workflow.getProvider(), workflow.getId(), locale);
        List<GWTJahiaWorkflowComment> gwtComments = new ArrayList<GWTJahiaWorkflowComment>();
        if (wf == null) {
            return gwtComments;
        }
        List<WorkflowComment> comments = wf.getComments();

        if (comments == null) {
            return gwtComments;
        }

        for (WorkflowComment comment : comments) {
            final GWTJahiaWorkflowComment workflowComment = new GWTJahiaWorkflowComment();
            workflowComment.setComment(comment.getComment());
            workflowComment.setTime(comment.getTime());
            JCRUserNode userNode = userManagerService.lookupUserByPath(comment.getUser());
            if (userNode != null) {
                workflowComment.setUser(userNode.getName());
            } else {
                workflowComment.setUser(comment.getUser());
            }
            gwtComments.add(workflowComment);
        }

        return gwtComments;
    }

    public List<GWTJahiaWorkflowHistoryItem> getWorkflowHistoryProcesses(String nodeId, JCRSessionWrapper session, Locale uiLocale) throws GWTJahiaServiceException {
        List<GWTJahiaWorkflowHistoryItem> history = new ArrayList<GWTJahiaWorkflowHistoryItem>();
        try {
            // read all processes
            List<HistoryWorkflow> workflows = service.getHistoryWorkflows(session.getNodeByIdentifier(nodeId), uiLocale);
            for (HistoryWorkflow wf : workflows) {
                history.add(getGWTJahiaHistoryProcess(wf));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Cannot get workflow history. Cause: " + e.getLocalizedMessage(), e);
        }
        return history;
    }

    private String getUsername(String userPath) {
        return StringUtils.substringAfterLast(userPath, "/");
    }

    public List<GWTJahiaWorkflowHistoryItem> getWorkflowHistoryTasks(String provider, String processId, Locale uiLocale) throws GWTJahiaServiceException {
        List<GWTJahiaWorkflowHistoryItem> history = new ArrayList<GWTJahiaWorkflowHistoryItem>();
        // read tasks of the process
        List<HistoryWorkflowTask> tasks = service.getHistoryWorkflowTasks(processId, provider, uiLocale);

        for (HistoryWorkflowTask wfTask : tasks) {
            history.add(new GWTJahiaWorkflowHistoryTask(wfTask.getActionId(), wfTask.getName(),
                    wfTask.getDisplayName() + (wfTask.getDisplayOutcome() != null ? " : " + wfTask.getDisplayOutcome() : ""),
                    wfTask.getProcessId(), wfTask
                    .getProvider(), wfTask.isCompleted(), wfTask.getStartTime(), wfTask.getEndTime(), wfTask
                    .getDuration(), wfTask.getOutcome(), getUsername(wfTask.getUser())));
        }

        return history;
    }

    public List<GWTJahiaWorkflowHistoryItem> getWorkflowHistoryForUser(JahiaUser user, Locale locale, Locale uiLocale) throws GWTJahiaServiceException {
        List<GWTJahiaWorkflowHistoryItem> gwtWorkflows = new ArrayList<GWTJahiaWorkflowHistoryItem>();

        Map<String, GWTJahiaWorkflowHistoryProcess> gwtWorkflowsMap = new HashMap<String, GWTJahiaWorkflowHistoryProcess>();

        List<WorkflowTask> tasks = service.getTasksForUser(user, uiLocale);
        for (WorkflowTask task : tasks) {
            GWTJahiaWorkflowHistoryProcess gwtWfHistory = gwtWorkflowsMap.get(task.getProcessId());
            HistoryWorkflow historyWorkflow = service.getHistoryWorkflow(task.getProcessId(), task.getProvider(), uiLocale);
            if (historyWorkflow != null) {
                if (gwtWfHistory == null) {
                    gwtWfHistory = getGWTJahiaHistoryProcess(historyWorkflow);
                    Workflow wf = service.getWorkflow(historyWorkflow.getProvider(),historyWorkflow.getProcessId(), uiLocale);
                    if (wf != null && wf.getVariables() != null && wf.getVariables().get("jcr_title") != null) {
                        gwtWfHistory.setDisplayName(((WorkflowVariable) wf.getVariables().get("jcr_title")).getValue());
                    }
                    gwtWfHistory.setAvailableTasks(new ArrayList<GWTJahiaWorkflowTask>());
                    gwtWorkflowsMap.put(task.getProcessId(), gwtWfHistory);

                    List<String> nodeIds;
                    if (wf != null) {
                        gwtWfHistory.setRunningWorkflow(getGWTJahiaWorkflow(wf));
                        nodeIds = (List<String>) wf.getVariables().get("nodeIds");
                        if (nodeIds == null) {
                            nodeIds = Collections.singletonList((String) wf.getVariables().get("nodeId"));
                        }
                    } else {
                        nodeIds = Collections.singletonList(gwtWfHistory.getNodeId());
                    }

                    for (String nodeId : nodeIds) {
                        try {
                            Locale wflocale = locale;
                            if (wf != null && wf.getVariables().get("locale") != null) {
                                wflocale = (Locale) wf.getVariables().get("locale");
                            }
                            JCRNodeWrapper nodeWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(null, wflocale).getNodeByIdentifier(nodeId);
                            gwtWfHistory.set("nodeWrapper", ((NavigationHelper) SpringContextSingleton.getInstance().getContext().getBeansOfType(NavigationHelper.class).values().iterator().next()).getGWTJahiaNode(nodeWrapper));

                            gwtWorkflows.add(gwtWfHistory);

                            break;
                        } catch (ItemNotFoundException e) {
                            // Node cannot be found
                        } catch (RepositoryException e) {
                            logger.warn(e.getMessage(), e);
                        }
                    }
                }
                JCRNodeWrapper node = null;
                try {
                    if (gwtWfHistory.getNodeId() != null) {
                        node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByIdentifier(gwtWfHistory.getNodeId());
                    }
                } catch (RepositoryException e) {
                    logger.warn("Cannot read node {}", gwtWfHistory.getNodeId(), e);
                }
                gwtWfHistory.getAvailableTasks().add(getGWTJahiaWorkflowTask(task, node));
            }
        }

        List<Workflow> workflows = service.getWorkflowsForUser(user, uiLocale);
        for (Workflow wf : workflows) {
            GWTJahiaWorkflowHistoryProcess gwtWfHistory = gwtWorkflowsMap.get(wf.getId());
            if (gwtWfHistory == null) {
                gwtWfHistory = getGWTJahiaHistoryProcess(service.getHistoryWorkflow(wf.getId(), wf.getProvider(), uiLocale));
                try {
                    JCRNodeWrapper nodeWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(org.jahia.api.Constants.EDIT_WORKSPACE, locale).getNodeByIdentifier(
                            gwtWfHistory.getNodeId());
                    gwtWfHistory.set("nodeWrapper", ((NavigationHelper) SpringContextSingleton.getInstance().getContext().getBeansOfType(NavigationHelper.class).values().iterator().next()).getGWTJahiaNode(nodeWrapper));
                } catch (RepositoryException e) {
                    logger.warn(e.getMessage(), e);
                    continue;
                }
                gwtWorkflowsMap.put(wf.getId(), gwtWfHistory);
                gwtWorkflows.add(gwtWfHistory);
                gwtWfHistory.setRunningWorkflow(getGWTJahiaWorkflow(wf));
                gwtWfHistory.setAvailableTasks(new ArrayList<GWTJahiaWorkflowTask>());
            }
        }

        return gwtWorkflows;
    }

    public Map<GWTJahiaWorkflowType, List<GWTJahiaWorkflowDefinition>> getWorkflowRules(String path, JCRSessionWrapper session,
                                                                                        Locale uiLocale) throws GWTJahiaServiceException {
        try {
            Map<String, String> rev = new HashMap<String, String>();
            Map<GWTJahiaWorkflowType, List<GWTJahiaWorkflowDefinition>> result = new HashMap<GWTJahiaWorkflowType, List<GWTJahiaWorkflowDefinition>>();
            Map<String, List<GWTJahiaWorkflowDefinition>> keyToMap = new HashMap<String, List<GWTJahiaWorkflowDefinition>>();

            JCRNodeWrapper node = session.getNode(path);

            final Set<String> workflowTypes = service.getTypesOfWorkflow();
            for (String workflowType : workflowTypes) {
                List<GWTJahiaWorkflowDefinition> definitions = new ArrayList<GWTJahiaWorkflowDefinition>();
                List<WorkflowDefinition> workflowDefinitions = service.getWorkflowDefinitionsForType(workflowType,
                        node.getResolveSite(), uiLocale);
                for (WorkflowDefinition definition : workflowDefinitions) {
                    final GWTJahiaWorkflowDefinition workflowDefinition = getGWTJahiaWorkflowDefinition(definition);
                    definitions.add(workflowDefinition);
                    rev.put(definition.getKey(), workflowType);
                }
                GWTJahiaWorkflowType t = getGWTJahiaWorkflowType(workflowType);
                if (!definitions.isEmpty()) {
                    result.put(t, definitions);
                }
                keyToMap.put(workflowType, definitions);
            }

            // Get local definitions
            Collection<WorkflowRule> map = service.getWorkflowRules(node);
            for (WorkflowRule rule : map) {
                try {
                    final WorkflowDefinition definition = service.getWorkflowDefinition(rule.getProviderKey(),
                            rule.getWorkflowDefinitionKey(),
                            uiLocale);
                    if (definition != null) {
                        final GWTJahiaWorkflowDefinition workflowDefinition = getGWTJahiaWorkflowDefinition(definition);
                        workflowDefinition.set("active", Boolean.TRUE);
                        workflowDefinition.set("definitionPath", rule.getDefinitionPath());
                        workflowDefinition.set("workflowRootPath",rule.getWorkflowRootPath());
                        keyToMap.get(rev.get(definition.getKey())).remove(workflowDefinition);
                        keyToMap.get(rev.get(definition.getKey())).add(workflowDefinition);
                    } else {
                        logger.warn("Couldn't find definition for workflow " + rule.getWorkflowDefinitionKey());
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            return result;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Cannot get workflow rules for " + path + ". Cause: " + e.getLocalizedMessage(), e);
        }
    }

    private GWTJahiaWorkflowType getGWTJahiaWorkflowType(String workflowType) {
        GWTJahiaWorkflowType t = new GWTJahiaWorkflowType();
        t.setDisplayName(workflowType);
        t.setName(workflowType);
        return t;
    }

    public List<GWTJahiaWorkflowDefinition> getWorkflows(Locale locale) throws GWTJahiaServiceException {
        try {
            final Set<String> workflowTypes = service.getTypesOfWorkflow();
            List<GWTJahiaWorkflowDefinition> definitions = new ArrayList<GWTJahiaWorkflowDefinition>();
            for (String workflowType : workflowTypes) {
                final List<WorkflowDefinition> workflowDefinitions = service.getWorkflowDefinitionsForType(workflowType,
                        null, locale);
                for (WorkflowDefinition definition : workflowDefinitions) {
                    definitions.add(getGWTJahiaWorkflowDefinition(definition));
                }
            }
            return definitions;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Cannot get workflows for " + locale + ". Cause: " + e.getLocalizedMessage(), e);
        }
    }

    public void updateWorkflowRules(GWTJahiaNode gwtNode, Set<GWTJahiaWorkflowDefinition> actives, JCRSessionWrapper session) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(gwtNode.getPath());
            if (!node.isCheckedOut()) {
                session.checkout(node);
            }
            if (!actives.isEmpty()) {
                if (!node.isNodeType("jmix:workflowRulesable")) {
                    node.addMixin("jmix:workflowRulesable");
                }
                session.save();
            }
            if (node.hasNode(WorkflowService.WORKFLOWRULES_NODE_NAME)) {
                JCRNodeWrapper wfRulesNode = node.getNode(WorkflowService.WORKFLOWRULES_NODE_NAME);
                if (!wfRulesNode.isCheckedOut()) {
                    session.checkout(wfRulesNode);
                }
                Set<String> activeKeys = new HashSet<String>();
                for (GWTJahiaWorkflowDefinition definition : actives) {
                    final String defKey = definition.getProvider() + "_" + definition.getId();
                    activeKeys.add(defKey);

                    JCRNodeWrapper wfRuleNode;
                    if (!wfRulesNode.hasNode(defKey)) {
                        wfRuleNode = wfRulesNode.addNode(defKey, "jnt:workflowRule");
                        wfRuleNode.setProperty("j:workflow", definition.getProvider() + ":" + definition.getId());
                    } else {
                        wfRuleNode = wfRulesNode.getNode(defKey);
                    }
                }
                if (actives == null || actives.isEmpty()) {
                    // No more active definitions for this nodes
                    wfRulesNode.remove();
                    // Remove also associated workflows
                    if (node.isNodeType("jmix:workflowRulesable")) {
                        node.removeMixin("jmix:workflowRulesable");
                    }
                } else {
                    NodeIterator ni = wfRulesNode.getNodes();
                    while (ni.hasNext()) {
                        JCRNodeWrapper rule = (JCRNodeWrapper) ni.next();
                        if (!activeKeys.contains(rule.getName())) {
                            rule.remove();
                        }
                    }
                }
            }
            session.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Cannot update workflow rules. Cause: " + e.getLocalizedMessage(), e);
        }
    }

    public int getNumberOfTasksForUser(JahiaUser user) throws GWTJahiaServiceException {
        return getNumberOfTasksForUser(user, null);
    }

    private int getNumberOfTasksForUser(JahiaUser user, String excludedTaskId) throws GWTJahiaServiceException {
        int total = 0;
        List<WorkflowTask> tasks = service.getTasksForUser(user, null);
        for (WorkflowTask task : tasks) {
            if (excludedTaskId != null && excludedTaskId.equals(task.getId())) {
                continue;
            }
            Workflow workflow = service.getWorkflow(task.getProvider(), task.getProcessId(), null);
            if (workflow != null) {
                List<String> uuids = (List<String>) workflow.getVariables().get("nodeIds");
                if (uuids == null) {
                    uuids = Collections.singletonList((String) workflow.getVariables().get("nodeId"));
                }
                for (String uuid : uuids) {
                    try {
                        JCRSessionFactory.getInstance().getCurrentUserSession(null,null).getNodeByIdentifier(uuid);
                        total++;
                        break;
                    } catch (ItemNotFoundException e) {
                        //
                    } catch (RepositoryException e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            }
        }
        return total;
    }

    class PollingWorkflowListener extends WorkflowListener {

        @Override
        public void workflowEnded(HistoryWorkflow workflow) {
            JCRUserNode user = userManagerService.lookupUserByPath(workflow.getUser());
            final BroadcasterFactory broadcasterFactory = AtmosphereServlet.getBroadcasterFactory();
            Broadcaster broadcaster = broadcasterFactory.lookup(ManagedGWTResource.GWT_BROADCASTER_ID + user.getName());
            if (broadcaster != null) {
                TaskEvent taskEvent = new TaskEvent();
                Locale preferredLocale = UserPreferencesHelper.getPreferredLocale(user);
                if (preferredLocale == null) {
                    preferredLocale = LanguageCodeConverters.languageCodeToLocale(ServicesRegistry.getInstance().getJahiaSitesService().getDefaultSite().getDefaultLanguage());
                }
                workflow = service.getHistoryWorkflow(workflow.getProcessId(), workflow.getProvider(), preferredLocale);
                taskEvent.setEndedWorkflow((StringUtils.defaultString(workflow.getDisplayName(), workflow.getName())));

                broadcaster.broadcast(taskEvent);
            }
        }

        @Override
        public void taskEnded(WorkflowTask task) {
            update(task, false);
        }

        @Override
        public void newTaskCreated(WorkflowTask task) {
            update(task, true);
        }

        private void update(WorkflowTask task, boolean newTask) {
            final BroadcasterFactory broadcasterFactory = AtmosphereServlet.getBroadcasterFactory();
            if (broadcasterFactory != null) {

                Set<JCRUserNode> users = new HashSet<JCRUserNode>();
                for (WorkflowParticipation workflowParticipation : task.getParticipations()) {
                    JahiaPrincipal p = workflowParticipation.getJahiaPrincipal();
                    if (p instanceof JahiaUser) {
                        JCRUserNode u = userManagerService.lookupUserByPath(p.getLocalPath());
                        if (u != null) {
                            users.add(u);
                        }
                    } else if (p instanceof JahiaGroup) {
                        JCRGroupNode g = groupManagerService.lookupGroupByPath(p.getLocalPath());
                        if (g != null) {
                            users.addAll(g.getRecursiveUserMembers());
                        }
                    }
                }

                Map<Locale, TaskEvent> taskEventByLang = new HashMap<>();

                Locale defaultSiteLocale = null;
                ChannelHolder channelHolder = null;
                for (JCRUserNode user : users) {
                    if (user != null) {
                        Locale preferredLocale = UserPreferencesHelper.getPreferredLocale(user);
                        if (preferredLocale == null) {
                            // use the default language of the default site as preferred locale
                            if (null == defaultSiteLocale) {
                                defaultSiteLocale = LanguageCodeConverters.languageCodeToLocale(ServicesRegistry.getInstance().getJahiaSitesService().getDefaultSite().getDefaultLanguage());
                            }
                            preferredLocale = defaultSiteLocale;
                        }
                        TaskEvent taskEvent = taskEventByLang.get(preferredLocale);
                        if (taskEvent == null) {
                            taskEvent = new TaskEvent();
                            if (newTask) {
                                task = service.getWorkflowTask(task.getId(), task.getProvider(), preferredLocale);
                                taskEvent.setNewTask(StringUtils.defaultString(task.getDisplayName(), task.getName()));
                            }
                            if (!newTask) {
                                taskEvent.setEndedTask(task.getId());
                            }
                            taskEventByLang.put(preferredLocale, taskEvent);
                        }
                        String userName = user.getName();
                        Broadcaster broadcaster = broadcasterFactory.lookup(ManagedGWTResource.GWT_BROADCASTER_ID + userName);
                        if (broadcaster != null) {
                            broadcaster.broadcast(taskEvent);
                        } else {
                            try {
                                if (null == channelHolder) {
                                    channelHolder = (ChannelHolder) SpringContextSingleton.getBean("org.jahia.ajax.gwt.commons.server.ChannelHolderImpl");
                                }
                                JGroupsChannel jc = channelHolder.getChannel();
                                if (jc != null) {
                                   jc.send(ManagedGWTResource.GWT_BROADCASTER_ID + userName, taskEvent);
                                }
                            } catch (Exception e) {
                                logger.debug(e.getMessage(), e);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setService(WorkflowService service) {
        this.service = service;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    public void setContentDefinitionHelper(ContentDefinitionHelper contentDefinitionHelper) {
        this.contentDefinitionHelper = contentDefinitionHelper;
    }
}
