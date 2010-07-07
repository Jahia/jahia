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

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.rbac.RoleIdentity;
import org.jahia.services.rbac.jcr.RoleBasedAccessControlService;
import org.jahia.services.rbac.jcr.RoleService;
import org.jahia.services.usermanager.*;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.*;
import java.util.*;

/**
 * Jahia service for managing content workflow.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 2 févr. 2010
 */
public class WorkflowService {
    private transient static Logger logger = Logger.getLogger(WorkflowService.class);


    private Map<String, WorkflowProvider> providers = new HashMap<String, WorkflowProvider>();
    private static WorkflowService instance;
    public static final String CANDIDATE = "candidate";
    public static final String START_ROLE = "start";
    private RoleService roleService;
    private RoleBasedAccessControlService rbacService;
    private Map<String,List<String>> workflowTypes;
    public static final String WORKFLOWRULES_NODE_NAME = "workflowrules";

    public static WorkflowService getInstance() {
        if (instance == null) {
            instance = new WorkflowService();
        }
        return instance;
    }

    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    public void setWorkflowTypes(Map<String, List<String>> workflowTypes) {
        this.workflowTypes = workflowTypes;
    }

    public Map<String, WorkflowProvider> getProviders() {
        return providers;
    }

    public void start() {
    }

    public void addProvider(WorkflowProvider provider) {
        providers.put(provider.getKey(), provider);
        /*try {
            List<WorkflowDefinition> list = getWorkflows();
            for (WorkflowDefinition definition : list) {
                addWorkflowRule("/","nt:base",definition,START_ROLE,"webmaster");
                iter()
            }
        } catch (RepositoryException e) {
            logger.error("Cannot register default workflow rule",e);
        }*/
    }

    /**
     * This method list all workflows deployed in the system
     *
     * @return A list of available workflows per provider.
     * @param locale
     */
    public List<WorkflowDefinition> getWorkflows(Locale locale) throws RepositoryException {
        List<WorkflowDefinition> workflowsByProvider = new ArrayList<WorkflowDefinition>();
        for (Map.Entry<String, WorkflowProvider> providerEntry : providers.entrySet()) {
            workflowsByProvider.addAll(providerEntry.getValue().getAvailableWorkflows());
        }
        if(locale!=null) {
            for (WorkflowDefinition definition : workflowsByProvider) {
                try {
                    ResourceBundle resourceBundle = getResourceBundle(definition,locale);
                    definition.setDisplayName(resourceBundle.getString("name"));
                } catch (Exception e) {
                    definition.setDisplayName(definition.getName());
                }
            }
        }
        return workflowsByProvider;
    }

    private ResourceBundle getResourceBundle(WorkflowDefinition definition,Locale locale) {
        return ResourceBundle.getBundle(this.getClass().getPackage().getName()+"."+definition.getKey().replaceAll(" ",""), locale);
    }

    public List<WorkflowDefinition> getWorkflowsForAction(String actionName, Locale locale) throws RepositoryException {
        List<String> l = workflowTypes.get(actionName);
        List<WorkflowDefinition> workflowsByProvider = new ArrayList<WorkflowDefinition>();
        for (Map.Entry<String, WorkflowProvider> providerEntry : providers.entrySet()) {
            List<WorkflowDefinition> defs = providerEntry.getValue().getAvailableWorkflows();
            for (WorkflowDefinition def : defs) {
                if (l.contains(def.getKey())) {
                    if(locale!=null) {
                        try {
                            ResourceBundle resourceBundle = getResourceBundle(def, locale);
                            def.setDisplayName(resourceBundle.getString("name"));
                        } catch (Exception e) {
                            def.setDisplayName(def.getName());
                        }
                    }
                    workflowsByProvider.add(def);
                }
            }
        }
        return workflowsByProvider;
    }

    /**
     * This method list all possible workflows for the specified node.
     *
     * @param node
     * @param user
     * @return A list of available workflows per provider.
     */
    public List<WorkflowDefinition> getPossibleWorkflows(final JCRNodeWrapper node, final JahiaUser user,Locale locale) throws RepositoryException {
        return getPossibleWorkflows(node, user, null, locale);
    }

    /**
     * This method return the workflow associated to an action, for the specified node.
     *
     * @param node
     * @param user
     * @return A list of available workflows per provider.
     */
    public WorkflowDefinition getPossibleWorkflowForAction(final JCRNodeWrapper node, final JahiaUser user, final String action,final Locale locale) throws RepositoryException {
        final List<WorkflowDefinition> workflowDefinitionList = getPossibleWorkflows(node, user, action, locale);
        if (workflowDefinitionList.isEmpty()) {
            return  null;
        }
        return workflowDefinitionList.get(0);
    }

    /**
     * This method list all possible workflows for the specified node.
     *
     * @param node
     * @param user
     * @return A list of available workflows per provider.
     */
    private List<WorkflowDefinition> getPossibleWorkflows(final JCRNodeWrapper node, final JahiaUser user, final String action,final Locale locale) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<WorkflowDefinition>>() {
            public List<WorkflowDefinition> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                List<WorkflowDefinition> workflowsForAction = null;
                if (action != null) {
                    workflowsForAction = getWorkflowsForAction(action,locale);
                }
                final Set<WorkflowDefinition> workflows = new LinkedHashSet<WorkflowDefinition>();

                JCRSiteNode site = node.resolveSite();

                Map<String,List<String[]>> rules = getWorkflowRules(node, null);
                for (String wfName : rules.keySet()) {
                    String workflowDefinitionKey = StringUtils.substringAfter(wfName, ":");
                    String providerKey = StringUtils.substringBefore(wfName, ":");
                    WorkflowDefinition definition = lookupProvider(providerKey).getWorkflowDefinitionByKey(
                            workflowDefinitionKey);
                    if(locale!=null) {
                        try {
                            ResourceBundle resourceBundle = getResourceBundle(definition, locale);
                            definition.setDisplayName(resourceBundle.getString("name"));
                        } catch (Exception e) {
                            definition.setDisplayName(definition.getName());
                        }
                    }
                    if (null == workflowsForAction || workflowsForAction.contains(definition)) {
                        List<String[]> l = rules.get(wfName);
                        for (String[] rule : l) {
                            String principal = rule[3];
                            String type = rule[1];
                            String privilege = rule[2];
                            if ("GRANT".equals(type) && START_ROLE.equals(privilege)) {
                                final String principalName = principal.substring(2);
                                if (principal.charAt(0) == 'u') {
                                    if (principalName.equals(user.getName())) {
                                        workflows.add(definition);
                                    }
                                } else if (principal.charAt(0) == 'g') {
                                    if (user.isMemberOfGroup(site.getID(), principalName)) {
                                        workflows.add(definition);
                                    }
                                } else if (principal.charAt(0) == 'r') {
                                    if (user.hasRole(new RoleIdentity(principalName, site.getSiteKey()))) {
                                        workflows.add(definition);
                                    }
                                }
                            }
                        }
                    }
                }
                return new LinkedList<WorkflowDefinition>(workflows);
            }
        });
    }

    public List<JahiaPrincipal> getAssignedRole(final JCRNodeWrapper node, final WorkflowDefinition definition,
                                                final String role) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<JahiaPrincipal>>() {
            public List<JahiaPrincipal> doInJCR(JCRSessionWrapper session) throws RepositoryException {

                List<JahiaPrincipal> principals = new ArrayList<JahiaPrincipal>();
                JahiaUserManagerService userService = ServicesRegistry.getInstance().getJahiaUserManagerService();
                JahiaGroupManagerService groupService = ServicesRegistry.getInstance().getJahiaGroupManagerService();

                JCRSiteNode site = node.resolveSite();

                Map<String,List<String[]>> rules = getWorkflowRules(node, null);

                List<String[]> l = rules.get(definition.getProvider() + ":" + definition.getKey());
                if (l != null) {
                    for (String[] rule : l) {
                        String principal = rule[3];
                        String type = rule[1];
                        String privilege = rule[2];

                        if ("GRANT".equals(type) && role.equals(privilege)) {
                            final String principalName = principal.substring(2);
                            if (principal.charAt(0) == 'u') {
                                JahiaUser jahiaUser = userService.lookupUser(principalName);
                                principals.add(jahiaUser);
                            } else if (principal.charAt(0) == 'g') {
                                JahiaGroup group = groupService.lookupGroup(site.getID(), principalName);
                                principals.add(group);
                            } else if (principal.charAt(0) == 'r') {
                                principals.add(new RoleIdentity(principalName, site.getSiteKey()));
                            }
                        }
                    }
                }
                return principals;
            }
        });
    }

    private List<JCRNodeWrapper> getApplicableWorkflowRules(final JCRNodeWrapper node, final JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper rules = null;
        JCRNodeWrapper nodeWrapper = node;
        while (rules==null) {
            try {
                rules = nodeWrapper.getNode(WORKFLOWRULES_NODE_NAME);
            } catch (RepositoryException e) {
                nodeWrapper = nodeWrapper.getParent();
            }
        }
        List<JCRNodeWrapper> rulesList = new ArrayList<JCRNodeWrapper>();
        NodeIterator ni = rules.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper rule = (JCRNodeWrapper) ni.next();
//            if (!rule.hasProperty("j:nodeType") || node.isNodeType(rule.getProperty("j:nodeType").getString())) {
                rulesList.add(rule);
//            }
        }
        return rulesList;
    }

    /**
     * This method list all currently active workflow for the specified node.
     *
     * @param node
     * @param locale
     * @return A list of active workflows per provider
     */
    public List<Workflow> getActiveWorkflows(JCRNodeWrapper node, Locale locale) {
        List<Workflow> workflows = new ArrayList<Workflow>();
        try {
            if (node.isNodeType("jmix:workflow") && node.hasProperty(Constants.PROCESSID)) {
                addActiveWorkflows(workflows, node.getProperty(Constants.PROCESSID),locale);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return workflows;
    }

    /**
     * This method list all currently active workflow for the specified node.
     *
     * @param node
     * @return A list of active workflows per provider
     */
    public Map<Locale,List<Workflow>> getActiveWorkflowsForAllLocales(JCRNodeWrapper node) {
        Map<Locale,List<Workflow>> workflowsByLocale = new HashMap<Locale,List<Workflow>>();
        try {
            if (node.isNodeType("jmix:workflow")) {
                NodeIterator ni = node.getNodes("j:translation*");
                while (ni.hasNext()) {
                    Node n = ((JCRNodeWrapper) ni.next()).getRealNode();
                    final String lang = n.getProperty("jcr:language").getString();
                    if (n.hasProperty(Constants.PROCESSID + "_"+lang)) {
                        List<Workflow> l = new ArrayList<Workflow>();
                        workflowsByLocale.put(LanguageCodeConverters.getLocaleFromCode(lang),l);
                        addActiveWorkflows(l, n.getProperty(Constants.PROCESSID + "_"+lang), null);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return workflowsByLocale;
    }

    private void addActiveWorkflows(List<Workflow> workflows, Property p, Locale locale) throws RepositoryException {
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
            if (!processIds.isEmpty()) {
                List<Workflow> workflowsInformations = entry.getValue().getActiveWorkflowsInformations(processIds);
                for (Workflow workflowsInformation : workflowsInformations) {
                    if(locale!=null) {
                        final WorkflowDefinition definition = workflowsInformation.getDefinition();
                        try {
                            ResourceBundle resourceBundle = getResourceBundle(definition, locale);
                            definition.setDisplayName(resourceBundle.getString("name"));
                            final Set<WorkflowAction> actionSet = workflowsInformation.getAvailableActions();
                            for (WorkflowAction action : actionSet) {
                                i18nOfWorkflowAction(locale, action,definition);
                            }
                        } catch (Exception e) {
                            definition.setDisplayName(definition.getName());
                        }
                    }
                }
                workflows.addAll(workflowsInformations);
            }
        }
    }

    /**
     * This method list all actions available at execution time for a node.
     *
     * @param processId the process we want to advance
     * @param provider  The provider executing the process
     * @return a set of actions per workflows per provider.
     */
    public Set<WorkflowAction> getAvailableActions(String processId, String provider) {
        return lookupProvider(provider).getAvailableActions(processId);
    }

    /**
     * This method will call the underlying provider to signal the identified process.
     *
     * @param processId the process we want to advance
     * @param provider  The provider executing the process
     * @param transitionName
     * @param args      List of args for the process
     */
    public void signalProcess(String processId, String transitionName, String provider, Map<String, Object> args) {
        lookupProvider(provider).signalProcess(processId, transitionName, args);
    }

    /**
     * This method will call the underlying provider to signal the identified process.
     *
     * @param processId the process we want to advance
     * @param provider  The provider executing the process
     * @param transitionName
     * @param args      List of args for the process
     */
    public void signalProcess(String processId, String transitionName, String signalName, String provider, Map<String, Object> args) {
        lookupProvider(provider).signalProcess(processId, transitionName, signalName, args);
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
        return startProcess(Arrays.asList(stageNode.getIdentifier()), stageNode.getSession(), processKey, provider,args);
    }

    public String startProcess(List<String> nodeIds, JCRSessionWrapper session, String processKey, String provider, Map<String, Object> args)
            throws RepositoryException {
        args.put("nodeId", nodeIds.iterator().next());
        args.put("nodeIds", nodeIds);
        args.put("workspace", session.getWorkspace().getName());
        args.put("locale", session.getLocale());
        args.put("workflow", lookupProvider(provider).getWorkflowDefinitionByKey(processKey));
        args.put("user", session.getUser().getUsername());
        final String processId = lookupProvider(provider).startProcess(processKey, args);
        if(logger.isDebugEnabled()) {
            logger.debug("A workflow "+processKey+" from "+provider+" has been started on nodes: "+nodeIds+
                         " from workspace "+args.get("workspace")+" in locale "+args.get("locale")+ " with id "+processId);
        }
        return processId;
    }

    public synchronized void addProcessId(JCRNodeWrapper stageNode, String provider, String processId) throws RepositoryException {
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
    }

    public synchronized void removeProcessId(JCRNodeWrapper stageNode, String provider, String processId) throws RepositoryException {
        stageNode.checkout();
        List<Value> values = new ArrayList<Value>(Arrays.asList(stageNode.getProperty(Constants.PROCESSID).getValues()));
        Value[] newValues = new Value[values.size()-1];
        int i = 0;
        for (Value value : values) {
            if (!value.getString().equals(provider + ":" + processId)) {
                newValues[i++] = value;
            }
        }
        stageNode.setProperty(Constants.PROCESSID, values.toArray(new Value[values.size()]));
        stageNode.getSession().save();
    }

    public List<WorkflowTask> getTasksForUser(JahiaUser user, Locale locale) {
        final List<WorkflowTask> workflowActions = new LinkedList<WorkflowTask>();
        for (Map.Entry<String, WorkflowProvider> providerEntry : providers.entrySet()) {
            workflowActions.addAll(providerEntry.getValue().getTasksForUser(user));
        }
        Locale displayLocale = locale;
        if(displayLocale==null) {
            displayLocale = UserPreferencesHelper.getPreferredLocale(user);
        }
        for (WorkflowTask workflowAction : workflowActions) {
            i18nOfWorkflowAction(displayLocale, workflowAction, workflowAction.getDefinition());
        }
        return workflowActions;
    }

    private void i18nOfWorkflowAction(Locale displayLocale, WorkflowAction workflowAction,
                                      WorkflowDefinition definition) {
        ResourceBundle resourceBundle = getResourceBundle(definition, displayLocale);
        workflowAction.setDisplayName(resourceBundle.getString(workflowAction.getName().replaceAll(" ",
                                                                                                   ".").trim().toLowerCase()));
        if (workflowAction instanceof WorkflowTask) {
            WorkflowTask workflowTask = (WorkflowTask) workflowAction;
            Set<String> outcomes = workflowTask.getOutcomes();
            List<String> displayOutcomes = new LinkedList<String>();
            for (String outcome : outcomes) {
                String key = workflowAction.getName().replaceAll(" ",
                                                                     ".").trim().toLowerCase() + "." + outcome.replaceAll(
                            " ", ".").trim().toLowerCase();
                String s;
                try {

                    s = resourceBundle.getString(key);
                } catch (Exception e) {
                    logger.info("Missing ressource : "+key+" in "+resourceBundle);
                    s = workflowAction.getName();
                }
                displayOutcomes.add(s);
            }
            workflowTask.setDisplayOutcomes(displayOutcomes);
        }
    }

    public void assignTask(String taskId, String provider, JahiaUser user) {
        if (logger.isDebugEnabled()) {
            logger.debug("Assigning user " + user + " to task " + taskId);
        }
        lookupProvider(provider).assignTask(taskId, user);
    }

    public void completeTask(String taskId, String provider, String outcome, Map<String, Object> args, JahiaUser user) {
        args.put("user", user.getUsername());
        lookupProvider(provider).completeTask(taskId, outcome, args);
    }

    public void addParticipatingGroup(String taskId, String provider, JahiaGroup group, String role) {
        lookupProvider(provider).addParticipatingGroup(taskId, group, role);
    }

    public void deleteTask(String taskId, String provider, String reason) {
        lookupProvider(provider).deleteTask(taskId, reason);
    }

    public void addWorkflowRule(final JCRNodeWrapper node, final WorkflowDefinition workflow,
                                final String task, final String principal) throws RepositoryException {
        // store the rule
        JCRNodeWrapper rules = null;
        try {
            rules = node.getNode(WORKFLOWRULES_NODE_NAME);
        } catch (RepositoryException e) {
            rules = node.addNode(WORKFLOWRULES_NODE_NAME, "jnt:workflowRules");
        }
        JCRNodeWrapper n;
        String wfName = workflow.getProvider() + "_" + workflow.getKey();
        if (rules.hasNode(wfName)) {
            n = rules.getNode(wfName);
        } else {
            n = rules.addNode(wfName, "jnt:workflowRule");
        }
        n.setProperty("j:workflow", workflow.getProvider() + ":" + workflow.getKey());
        String nodeName = "GRANT_" + principal.replace(':', '_');
        JCRNodeWrapper ace;
        if (n.hasNode(nodeName)) {
            ace = n.getNode(nodeName);
        } else {
            ace = n.addNode(nodeName, "jnt:ace");
        }
        ace.setProperty("j:principal", principal);
        ace.setProperty("j:protected", false);
        ace.setProperty("j:aceType", "GRANT");
        List<String> grClone = new ArrayList<String>();
        grClone.add(task);
        if (ace.hasProperty("j:privileges")) {
            final Value[] values = ace.getProperty("j:privileges").getValues();
            for (Value value : values) {
                final String s = value.getString();
                if (!task.equals(s)) {
                    grClone.add(s);
                }
            }
        }
        String[] grs = new String[grClone.size()];
        grClone.toArray(grs);
        ace.setProperty("j:privileges", grs);
    }

    private String getPermissionKey(String rule, WorkflowDefinition workflow, String role) {
        return rule + " - " + workflow.getName() + " - " + role;
    }

    public void removeWorkflowRule(final String key) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<String>() {
            public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                if (session.itemExists("/workflowrules/" + key)) {
                JCRNodeWrapper rule = session.getNode("/workflowrules/"+key);
                rule.remove();
                session.save();
                }
                return null;
            }
        });
    }

    public NodeIterator getAllWorkflowRules(JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper rule = session.getNode("/workflowrules/");
        if (rule != null) {
            return rule.getNodes();
        }
        return null;

    }

    public void setRoleBasedAccessControlService(RoleBasedAccessControlService roleBasedAccessControlService) {
        this.rbacService = roleBasedAccessControlService;
    }

    public void addCommentToTask(String taskId, String provider, String comment) {
        lookupProvider(provider).addComment(taskId,comment);
    }

    public WorkflowTask getWorkflowTask(String taskId, String provider, Locale locale) {
        WorkflowTask workflowTask = lookupProvider(provider).getWorkflowTask(taskId);
        if (locale != null) {
            ResourceBundle resourceBundle = getResourceBundle(workflowTask.getDefinition(), locale);
            Set<String> outcomes = workflowTask.getOutcomes();
            List<String> displayOutcomes = new LinkedList<String>();
            for (String outcome : outcomes) {
                String s = resourceBundle.getString(workflowTask.getName().replaceAll(" ",
                                                                                      ".").trim().toLowerCase() + "." + outcome.replaceAll(
                        " ", ".").trim().toLowerCase());
                displayOutcomes.add(s);
            }
            workflowTask.setDisplayOutcomes(displayOutcomes);
        }
        return workflowTask;
    }

    /**
     * Returns a list of process instance history records for the specified
     * node. This method also returns "active" (i.e. not completed) workflow
     * process instance.
     *
     * @param node the JCR node to retrieve history records for
     * @return a list of process instance history records for the specified node
     */
    public List<HistoryWorkflow> getHistoryWorkflows(JCRNodeWrapper node) {
        List<HistoryWorkflow> history = new LinkedList<HistoryWorkflow>();
        try {
            Value[] values = null;
            if (node.isNodeType("jmix:workflow") && node.hasProperty(Constants.PROCESSID)) {
                values = node.getProperty(Constants.PROCESSID).getValues();
            }
            if (values != null) {
                for (Map.Entry<String, WorkflowProvider> entry : providers.entrySet()) {
                    final List<String> processIds = new LinkedList<String>();
                    for (Value value : values) {
                        String key = value.getString();
                        String processId = StringUtils.substringAfter(key, ":");
                        String providerKey = StringUtils.substringBefore(key, ":");
                        if (providerKey.equals(entry.getKey())) {
                            processIds.add(processId);
                        }
                    }
                    if (!processIds.isEmpty()) {
                        history.addAll(entry.getValue().getHistoryWorkflows(processIds));
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return history;
    }

    /**
     * Returns a list of history records for workflow tasks.
     * This method also returns not completed tasks.
     *
     * @param workflowProcessId the process instance ID
     * @param providerKey the workflow provider key
     * @return a list of history records for workflow tasks
     */
    public List<HistoryWorkflowTask> getHistoryWorkflowTasks(String workflowProcessId, String providerKey) {
        return lookupProvider(providerKey).getHistoryWorkflowTasks(workflowProcessId);
    }

    protected WorkflowProvider lookupProvider(String key) {
        WorkflowProvider provider = providers.get(key);
        if (provider == null) {
            throw new JahiaRuntimeException("Unknown workflow provider with the key '" + key + "'");
        }

        return provider;
    }

    /**
     * This method list all currently active workflow for the specified node.
     *
     * @param node
     * @return A list of active workflows per provider
     */
    public boolean hasActivePublishWorkflow(JCRNodeWrapper node) {
        List<Workflow> workflows = new ArrayList<Workflow>();
        try {
            final List<WorkflowDefinition> forAction = getWorkflowsForAction("publish", null);
            if (node.isNodeType("jmix:workflow") && node.hasProperty(Constants.PROCESSID)) {
                addActiveWorkflows(workflows, node.getProperty(Constants.PROCESSID), null);
            }
            for (Workflow workflow : workflows) {
                if(forAction.contains(workflow.getDefinition())) {
                    return true;
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public void addWorkflowRule(JCRNodeWrapper node, String wfName, String task, String principal)
            throws RepositoryException {
        String provider = StringUtils.substringBefore(wfName,":");
        String wfKey = StringUtils.substringAfter(wfName,":");
        WorkflowDefinition definition = providers.get(provider).getWorkflowDefinitionByKey(wfKey);
        addWorkflowRule(node,definition,task,principal);
    }

    public Map<String, List<String[]>> getWorkflowRules(JCRNodeWrapper objectNode, Locale locale) {
        try {
            Map<String, List<String[]>> permissions = new ListOrderedMap();
            Map<String, List<String[]>> inheritedPerms = new ListOrderedMap();

            recurseonRules(permissions, inheritedPerms, objectNode);
            for (Map.Entry<String,List<String[]>> s : inheritedPerms.entrySet()) {
                if (permissions.containsKey(s.getKey())) {
                    List<String[]> l = permissions.get(s.getKey());
                    l.addAll(s.getValue());
                } else {
                    permissions.put(s.getKey(), s.getValue());
                }
            }
            return permissions;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    private void recurseonRules(Map<String, List<String[]>> results, Map<String, List<String[]>> inherited, Node n) throws RepositoryException {
        try {
            Map<String, List<String[]>> current = results;
            List<String> foundTypes = new ArrayList<String>();
            while (true) {
                if (n.hasNode(WORKFLOWRULES_NODE_NAME)) {
                    Node wfRules = n.getNode(WORKFLOWRULES_NODE_NAME);
                    NodeIterator rules = wfRules.getNodes();
                    while (rules.hasNext()) {
                        Node rule = rules.nextNode();
                        final String wfName = rule.getProperty("j:workflow").getString();
                        String name = StringUtils.substringAfter(wfName, ":");
                        String wftype = null;
                        for (Map.Entry<String, List<String>> entry : workflowTypes.entrySet()) {
                            if (entry.getValue().contains(name)) {
                                wftype = entry.getKey();
                            }
                        }
                        if (foundTypes.contains(wftype)) {
                            continue;
                        } else {
                            foundTypes.add(wftype);
                        }
                        Map<String, List<String[]>> localResults = new HashMap<String, List<String[]>>();
                        if(!rule.hasNodes()) {
                            localResults.put(wfName,new LinkedList<String[]>());
                        }
                        NodeIterator aces = rule.getNodes();
                        while (aces.hasNext()) {
                            Node ace = aces.nextNode();
                            if (ace.isNodeType("jnt:ace")) {
                                String principal = ace.getProperty("j:principal").getString();
                                String type = ace.getProperty("j:aceType").getString();
                                Value[] privileges = ace.getProperty("j:privileges").getValues();

                                if (!current.containsKey(wfName)) {
                                    List<String[]> p = localResults.get(wfName);
                                    if (p == null) {
                                        p = new ArrayList<String[]>();
                                        localResults.put(wfName, p);
                                    }
                                    for (Value privilege : privileges) {
//                                        , rule.getProperty("j:nodeType").getString()
                                        p.add(new String[]{n.getPath(), type, privilege.getString(), principal});
                                    }
                                }
                            }
                        }
                        current.putAll(localResults);
                        if (rule.hasProperty("j:inherit") && !rule.getProperty("j:inherit").getBoolean()) {
                            return;
                        }
                    }
                }
                n = n.getParent();
                current = inherited;
            }
        } catch (ItemNotFoundException e) {
            logger.debug(e);
        }
    }


//    public List<WorkflowRule> getWorkflowRules(final JCRNodeWrapper node, final JCRSessionWrapper session) throws RepositoryException {
//        JCRNodeWrapper rules = null;
//        JCRNodeWrapper nodeWrapper = node;
//        while (rules==null) {
//            try {
//                rules = nodeWrapper.getNode("workflowrules");
//            } catch (RepositoryException e) {
//                nodeWrapper = nodeWrapper.getParent();
//            }
//        }
//        List<JCRNodeWrapper> rulesList = new ArrayList<JCRNodeWrapper>();
//        NodeIterator ni = rules.getNodes();
//        while (ni.hasNext()) {
//            JCRNodeWrapper rule = (JCRNodeWrapper) ni.next();
//            WorkflowRule r = new WorkflowRule();
//
//            rulesList.add(rule);
//        }
//        return rulesList;
//    }
//

    public WorkflowDefinition getWorkflowDefinition(String provider, String id, Locale locale) {
        final WorkflowDefinition definition = lookupProvider(provider).getWorkflowDefinitionByKey(id);
        if (locale != null) {
            try {
                ResourceBundle resourceBundle = getResourceBundle(definition, locale);
                definition.setDisplayName(resourceBundle.getString("name"));

            } catch (Exception e) {
                definition.setDisplayName(definition.getName());
            }
        }
        return definition;
    }

    public Set<String> getTypesOfWorkflow() {
        return workflowTypes.keySet();
    }
}
