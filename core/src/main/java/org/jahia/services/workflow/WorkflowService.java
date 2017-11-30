/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.workflow;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.apache.jackrabbit.core.security.JahiaPrivilegeRegistry;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.query.QueryWrapper;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.readonlymode.ReadOnlyModeCapable;
import org.jahia.settings.readonlymode.ReadOnlyModeException;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.security.Privilege;

import java.util.*;

/**
 * Jahia service for managing content workflow.
 *
 * @author rincevent
 * @since JAHIA 6.5
 */
public class WorkflowService implements BeanPostProcessor, ApplicationListener<JahiaTemplateManagerService.ModuleDeployedOnSiteEvent>, ReadOnlyModeCapable {

    public static final String CANDIDATE = "candidate";
    public static final String START_ROLE = "start";
    public static final String WORKFLOWRULES_NODE_NAME = "j:workflowRules";

    private static final Logger logger = LoggerFactory.getLogger(WorkflowService.class);

    private static WorkflowService instance = new WorkflowService();;

    private Map<String, WorkflowProvider> providers = new HashMap<String, WorkflowProvider>();
    private Map<String, WorklowTypeRegistration> workflowRegistrationByDefinition = new HashMap<String, WorklowTypeRegistration>();
    private Map<String, String> modulesForWorkflowDefinition = new HashMap<String, String>();
    private JCRTemplate jcrTemplate;
    private WorkflowObservationManager observationManager = new WorkflowObservationManager(this);
    private CacheService cacheService;
    private Cache<String, Map<String, WorkflowRule>> cache;
    private boolean servicesStarted = false;

    private volatile boolean readOnly;

    /**
     * Returns a singleton instance of this service.
     *
     * @return a singleton instance of this service
     */
    public static WorkflowService getInstance() {
        return instance;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void start() throws JahiaInitializationException {
        if (cacheService != null) {
            cache = cacheService.getCache("WorkflowRuleCache", true);
        }
    }

    /**
     * Performs the registration of the provided workflow type.
     *
     * @param type the helper object instance for registerng a new workflow type
     */
    public synchronized void registerWorkflowType(final WorklowTypeRegistration type) {

        if (type != null && !workflowRegistrationByDefinition.containsKey(type.getDefinition())) {

            workflowRegistrationByDefinition.put(type.getDefinition(), type);

            // During startup, when things are still being registered by multiple threads, registration cannot be
            // completed reliably. So, avoid finishing it and rely on registerWorkflowTypes invoked during the post-
            // initialization phase instead.
            if (servicesStarted) {
                doRegisterWorkflowType(type);
            }
        }
    }

    private void doRegisterWorkflowType(final WorklowTypeRegistration type) {

        if (type.getModule() != null) {

            modulesForWorkflowDefinition.put(type.getDefinition(), type.getModule().getId());
            for (WorkflowProvider provider : providers.values()) {
                final WorkflowDefinition def = provider.getWorkflowDefinitionByKey(type.getDefinition(), null);
                if (def != null) {

                    try {

                        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {

                            @Override
                            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                boolean updated = initializePermission(session, def, type.getModule());
                                if (updated) {
                                    session.save();
                                    JahiaPrivilegeRegistry.addModulePrivileges(session, "/modules/" + type.getModule().getIdWithVersion());
                                }
                                return null;
                            }
                        });

                    } catch (RepositoryException e) {
                        logger.error("Cannot register workflow permissions", e);
                    }

                    type.setProvider(provider.getKey());
                    cache.flush();
                    ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry().addPackageForResourceBundle(def.getPackageName() + "." + type.getDefinition(), type.getModule());
                    break;
                }
            }
        }

        if (type.getProvider() == null) {
            workflowRegistrationByDefinition.remove(type.getDefinition());
            modulesForWorkflowDefinition.remove(type.getDefinition());
        }
    }

    public synchronized void registerWorkflowTypes() {
        for (WorklowTypeRegistration registration : new LinkedList<WorklowTypeRegistration>(
                workflowRegistrationByDefinition.values())) {
            doRegisterWorkflowType(registration);
        }
    }

    /**
     * Performs the unregistration of the provided workflow type.
     *
     * @param type the helper object instance for unregisterng a workflow type
     */
    public synchronized void unregisterWorkflowType(WorklowTypeRegistration type) {
        if (workflowRegistrationByDefinition.get(type.getDefinition()) == type) {
            workflowRegistrationByDefinition.remove(type.getDefinition());
            modulesForWorkflowDefinition.remove(type.getDefinition());
            cache.flush();
        }
    }

    /**
     * Returns a map with the registered workflow providers.
     *
     * @return a map with the registered workflow providers
     */
    public Map<String, WorkflowProvider> getProviders() {
        return providers;
    }

    /**
     * Adds the specified workflow provider into the registry.
     *
     * @param provider a workflow provider to be registered
     */
    public synchronized void addProvider(final WorkflowProvider provider) {
        providers.put(provider.getKey(), provider);
        if (provider instanceof WorkflowObservationManagerAware) {
            ((WorkflowObservationManagerAware) provider).setWorkflowObservationManager(observationManager);
        }
    }

    /**
     * Removes the specified provider from the registry.
     *
     * @param provider the provider to be removed
     */
    public synchronized void removeProvider(final WorkflowProvider provider) {
        providers.remove(provider.getKey());
    }

    private synchronized boolean initializePermission(JCRSessionWrapper session, WorkflowDefinition definition, JahiaTemplatesPackage module) throws RepositoryException {
        boolean updated = false;
        Map<String, String> map = workflowRegistrationByDefinition.get(definition.getKey()).getPermissions();
        if (map == null) {
            map = new HashMap<String, String>();
            workflowRegistrationByDefinition.get(definition.getKey()).setPermissions(map);
        }
        Set<String> tasks = definition.getTasks();

        final String permissionPath = "/modules/" + module.getIdWithVersion() + "/permissions";

        for (String task : tasks) {
            if (!map.containsKey(task)) {
                String permissionName = Patterns.SPACE.matcher(definition.getKey()).replaceAll("-") + "-" + Patterns.SPACE.matcher(task).replaceAll("-");

                if (!session.itemExists(permissionPath + "/workflow-tasks/" + permissionName)) {
                    logger.info("Create workflow permission : " + permissionName);
                    JCRNodeWrapper perms = session.getNode(permissionPath);
                    if (!perms.hasNode("workflow-tasks")) {
                        perms.addNode("workflow-tasks", "jnt:permission");
                    }
                    perms.getNode("workflow-tasks").addNode(permissionName, "jnt:permission");
                    updated = true;
                }
                map.put(task, "/workflow-tasks/" + permissionName);
            }
        }
        return updated;
    }

    /**
     * This method list all workflows deployed in the system
     *
     * @param displayLocale the UI display locale
     * @return A list of available workflows per provider.
     * @throws RepositoryException in case of an error
     */
    public List<WorkflowDefinition> getWorkflows(Locale displayLocale) throws RepositoryException {
        List<WorkflowDefinition> workflowsByProvider = new ArrayList<WorkflowDefinition>();
        for (Map.Entry<String, WorkflowProvider> providerEntry : providers.entrySet()) {
            workflowsByProvider.addAll(providerEntry.getValue().getAvailableWorkflows(displayLocale));
        }
        return workflowsByProvider;
    }

    /**
     * Returns a list of available workflow definitions for the specified type.
     *
     * @param type   workflow type
     * @param uiLocale the locale used to localize workflow labels  @return a list of available workflow definitions for the specified type
     * @throws RepositoryException in case of an error
     */
    public List<WorkflowDefinition> getWorkflowDefinitionsForType(String type, Locale uiLocale) throws RepositoryException {
        return getWorkflowDefinitionsForType(type, null, uiLocale);
    }

    /**
     * Returns a list of available workflow definitions for the specified type.
     *
     * @param type   workflow type
     * @param siteNode site node
     * @param uiLocale the locale used to localize workflow labels  @return a list of available workflow definitions for the specified type
     * @throws RepositoryException in case of an error
     */
    public List<WorkflowDefinition> getWorkflowDefinitionsForType(String type, JCRSiteNode siteNode, Locale uiLocale) throws RepositoryException {
        List<WorkflowDefinition> workflowsByProvider = new ArrayList<WorkflowDefinition>();
        for (Map.Entry<String, WorkflowProvider> providerEntry : providers.entrySet()) {
            List<WorkflowDefinition> defs = providerEntry.getValue().getAvailableWorkflows(uiLocale);
            for (WorkflowDefinition def : defs) {
                WorklowTypeRegistration worklowTypeRegistration = workflowRegistrationByDefinition.get(def.getKey());
                if (worklowTypeRegistration.getType().equals(type) && (siteNode == null || isRegistrationAvailableForSite(siteNode, worklowTypeRegistration))) {
                    workflowsByProvider.add(def);
                }
            }
        }
        return workflowsByProvider;
    }

    private boolean isRegistrationAvailableForSite(JCRSiteNode siteNode, WorklowTypeRegistration worklowTypeRegistration) {
        return worklowTypeRegistration.getModule().getModuleType().equals("system") ||
                siteNode.getInstalledModulesWithAllDependencies().contains(worklowTypeRegistration.getModule().getId());
    }

    /**
     * This method list all possible workflows for the specified node.
     *
     * @param node
     * @param checkPermission
     * @return A list of available workflows per provider.
     */
    public Map<String, WorkflowDefinition> getPossibleWorkflows(final JCRNodeWrapper node, boolean checkPermission, Locale uiLocale)
            throws RepositoryException {
        List<WorkflowDefinition> l = getPossibleWorkflows(node, checkPermission, null, uiLocale);
        Map<String, WorkflowDefinition> res = new HashMap<String, WorkflowDefinition>();
        for (WorkflowDefinition workflowDefinition : l) {
            res.put(workflowRegistrationByDefinition.get(workflowDefinition.getKey()).getType(), workflowDefinition);
        }
        return res;
    }

    /**
     * This method return the workflow associated to an type, for the specified node.
     *
     * @param node
     * @param checkPermission
     * @return A list of available workflows per provider.
     */
    public WorkflowDefinition getPossibleWorkflowForType(final JCRNodeWrapper node, final boolean checkPermission,
                                                         final String type, final Locale locale)
            throws RepositoryException {
        final List<WorkflowDefinition> workflowDefinitionList = getPossibleWorkflows(node, checkPermission, type, locale);
        if (workflowDefinitionList.isEmpty()) {
            return null;
        }
        return workflowDefinitionList.get(0);
    }

    /**
     * This method list all possible workflows for the specified node.
     *
     * @param node
     * @param checkPermission
     * @return A list of available workflows per provider.
     */
    private List<WorkflowDefinition> getPossibleWorkflows(final JCRNodeWrapper node, final boolean checkPermission,
                                                          final String type, final Locale uiLocale)
            throws RepositoryException {
        final Set<WorkflowDefinition> workflows = new LinkedHashSet<WorkflowDefinition>();

        Collection<WorkflowRule> rules = getWorkflowRulesForType(node, checkPermission, type);
        for (WorkflowRule ruledef : rules) {
            WorkflowDefinition definition =
                    lookupProvider(ruledef.getProviderKey()).getWorkflowDefinitionByKey(ruledef.getWorkflowDefinitionKey(), uiLocale);
            if (definition != null) {
                workflows.add(definition);
            }
        }
        return new LinkedList<WorkflowDefinition>(workflows);
    }

    public List<JahiaPrincipal> getAssignedRole(final WorkflowDefinition definition,
                                                final String activityName, final String processId) throws RepositoryException {

        return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<List<JahiaPrincipal>>() {

            @Override
            public List<JahiaPrincipal> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return getAssignedRole(definition, activityName, processId, session);
            }
        });
    }

    public List<JahiaPrincipal> getAssignedRole(WorkflowDefinition definition, String activityName, String processId, JCRSessionWrapper session) throws RepositoryException {
        List<JahiaPrincipal> principals = Collections.emptyList();
        Map<String, String> perms = workflowRegistrationByDefinition.get(definition.getKey()).getPermissions();
        String permPath = perms != null ? perms.get(activityName) : null;
        if (permPath == null) {
            return principals;
        }

        Workflow w = getWorkflow(definition.getProvider(), processId, null);
        JCRNodeWrapper node = session.getNodeByIdentifier((String) w.getVariables().get("nodeId"));
        if (permPath.indexOf("$") > -1) {
            if (w != null) {
                for (Map.Entry<String, Object> entry : w.getVariables().entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof List) {
                        List<?> list = (List<?>) entry.getValue();
                        StringBuilder sb = new StringBuilder();
                        Iterator<?> iterator = list.iterator();
                        while (iterator.hasNext()) {
                            Object o = iterator.next();
                            if (o instanceof WorkflowVariable) {
                                sb.append(((WorkflowVariable) o).getValue());
                            }
                            if (iterator.hasNext()) {
                                sb.append(",");
                            }
                        }
                        permPath = permPath.replace("$" + entry.getKey(), iterator.toString());
                    } else if (value instanceof WorkflowVariable) {
                        permPath = permPath.replace("$" + entry.getKey(), ((WorkflowVariable) value).getValue());
                    }
                }
            }
        }
        try {
            if (!permPath.contains("/")) {
                Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:permission] where name()='" + JCRContentUtils.sqlEncode(permPath) + "'", Query.JCR_SQL2);
                NodeIterator ni = q.execute().getNodes();
                if (ni.hasNext()) {
                    permPath = StringUtils.substringAfter(ni.nextNode().getPath(), "/permissions");

                } else {
                    return principals;
                }
            }

            Set<String> roles = new HashSet<String>();
            Set<String> extPerms = new HashSet<String>();

            while (!StringUtils.isEmpty(permPath)) {
                String permissionName = permPath.contains("/") ? StringUtils.substringAfterLast(permPath, "/") : permPath;
                NodeIterator ni = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:role] where [j:permissionNames] = '" + JCRContentUtils.sqlEncode(permissionName) + "'", Query.JCR_SQL2).execute().getNodes();
                while (ni.hasNext()) {
                    JCRNodeWrapper roleNode = (JCRNodeWrapper) ni.next();
                    roles.add(roleNode.getName());
                }
                ni = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:externalPermissions] where [j:permissionNames] = '" + JCRContentUtils.sqlEncode(permissionName) + "'", Query.JCR_SQL2).execute().getNodes();
                while (ni.hasNext()) {
                    JCRNodeWrapper roleNode = (JCRNodeWrapper) ni.next();
                    extPerms.add(roleNode.getParent().getName() + "/" + roleNode.getName());
                }
                permPath = permPath.contains("/") ? StringUtils.substringBeforeLast(permPath, "/") : "";
            }

            Map<String, List<String[]>> m = node.getAclEntries();
            principals = new LinkedList<JahiaPrincipal>();
            JahiaUserManagerService userService = ServicesRegistry.getInstance()
                    .getJahiaUserManagerService();
            JahiaGroupManagerService groupService = ServicesRegistry.getInstance()
                    .getJahiaGroupManagerService();

            JCRSiteNode site = null;

            for (Map.Entry<String, List<String[]>> entry : m.entrySet()) {
                for (String[] strings : entry.getValue()) {
                    if (strings[1].equals("GRANT") && roles.contains(strings[2]) || strings[1].equals("EXTERNAL") && extPerms.contains(strings[2])) {
                        String principal = entry.getKey();
                        final String principalName = principal.substring(2);
                        if (site == null) {
                            site = node.getResolveSite();
                        }
                        if (principal.charAt(0) == 'u') {
                            JCRUserNode userNode = userService.lookupUser(principalName, strings[0].startsWith("/sites/") ? site.getSiteKey() : null);
                            if (userNode != null) {
                                logger.debug("user {} is granted", userNode.getUserKey());
                                JahiaUser jahiaUser = userNode.getJahiaUser();
                                principals.add(jahiaUser);
                            }
                        } else if (principal.charAt(0) == 'g') {
                            JCRGroupNode group = groupService.lookupGroup(site.getSiteKey(),
                                    principalName);
                            if (group == null) {
                                group = groupService.lookupGroup(null, principalName);
                            }
                            if (group != null) {
                                logger.debug("group {} is granted", group.getGroupKey());
                                principals.add(group.getJahiaGroup());
                            }
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (BeansException e) {
            logger.error(e.getMessage(), e);
        }
        return principals;
    }

    /**
     * This method list all currently active workflow for the specified node.
     *
     * @param node
     * @param locale the content locale
     * @param displayLocale the UI display locale
     * @return A list of active workflows per provider
     */
    public List<Workflow> getActiveWorkflows(JCRNodeWrapper node, Locale locale, Locale displayLocale) {
        List<Workflow> workflows = new ArrayList<Workflow>();
        try {
            Node n = node;
            if (n.isNodeType(Constants.JAHIAMIX_WORKFLOW) && n.hasProperty(Constants.PROCESSID)) {
                addActiveWorkflows(workflows, n.getProperty(Constants.PROCESSID), displayLocale);
            }
            try {
                if (locale != null && node.hasTranslations()) {
                    n = node.getI18N(locale);
                    if (n.isNodeType(Constants.JAHIAMIX_WORKFLOW) && n.hasProperty(Constants.PROCESSID)) {
                        addActiveWorkflows(workflows, n.getProperty(Constants.PROCESSID), displayLocale);
                    }
                }
            } catch (ItemNotFoundException e) {
                return workflows;
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
    public Map<Locale, List<Workflow>> getActiveWorkflowsForAllLocales(JCRNodeWrapper node) {
        Map<Locale, List<Workflow>> workflowsByLocale = new HashMap<Locale, List<Workflow>>();
        try {
            if (node.isNodeType(Constants.JAHIAMIX_WORKFLOW)) {
                NodeIterator ni = node.getNodes("j:translation*");
                while (ni.hasNext()) {
                    Node n = ((JCRNodeWrapper) ni.next()).getRealNode();
                    final String lang = n.getProperty("jcr:language").getString();
                    if (n.hasProperty(Constants.PROCESSID)) {
                        List<Workflow> l = new ArrayList<Workflow>();
                        workflowsByLocale.put(LanguageCodeConverters.getLocaleFromCode(lang), l);
                        addActiveWorkflows(l, n.getProperty(Constants.PROCESSID), null);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return workflowsByLocale;
    }

    private void addActiveWorkflows(List<Workflow> workflows, Property p, Locale displayLocale) throws RepositoryException {
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
                List<Workflow> workflowsInformations = entry.getValue().getActiveWorkflowsInformations(processIds,
                        displayLocale);
                workflows.addAll(workflowsInformations);
            }
        }
    }

    /**
     * This method list all actions available at execution time for a node.
     *
     * @param processId the process we want to advance
     * @param provider  The provider executing the process
     * @param locale
     * @return a set of actions per workflows per provider.
     */
    public Set<WorkflowAction> getAvailableActions(String processId, String provider, Locale locale) {
        return lookupProvider(provider).getAvailableActions(processId, locale);
    }

    /**
     * This method will call the underlying provider to signal the identified process.
     *
     * @param processId the process we want to advance
     * @param provider  The provider executing the process
     */
    public void abortProcess(String processId, String provider) {

        assertWritable();

        WorkflowProvider workflowProvider = lookupProvider(provider);

        Workflow workflow = workflowProvider.getWorkflow(processId, null);
        final Set<String> actionIds = new HashSet<String>();
        for (final WorkflowAction action : workflow.getAvailableActions()) {
            if (action instanceof WorkflowTask) {
                actionIds.add(((WorkflowTask) action).getId());
            }
        }

        if (!actionIds.isEmpty()) {

            try {

                JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {

                    @Override
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        for (String actionId : actionIds) {
                            QueryWrapper q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:workflowTask] where [taskId]='" + actionId + "'", Query.JCR_SQL2);
                            JCRNodeIteratorWrapper ni = q.execute().getNodes();
                            for (JCRNodeWrapper wrapper : ni) {
                                wrapper.remove();
                            }
                        }
                        session.save();
                        return false;
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Cannot remove tasks", e);
            }
        }

        workflowProvider.abortProcess(processId);
    }

    public void startProcessAsJob(List<String> nodeIds, JCRSessionWrapper session, String processKey, String provider,
                                  Map<String, Object> args, List<String> comments) throws RepositoryException, SchedulerException {
        JobDetail jobDetail = BackgroundJob.createJahiaJob("StartProcess", StartProcessJob.class);
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put(BackgroundJob.JOB_USERKEY, session.getUserNode().getUserKey());
        jobDataMap.put(BackgroundJob.JOB_CURRENT_LOCALE, session.getLocale().toString());
        jobDataMap.put(StartProcessJob.NODE_IDS, nodeIds);
        jobDataMap.put(StartProcessJob.PROVIDER, provider);
        jobDataMap.put(StartProcessJob.PROCESS_KEY, processKey);
        jobDataMap.put(StartProcessJob.MAP, args);
        jobDataMap.put(StartProcessJob.COMMENTS, comments);

        ServicesRegistry.getInstance().getSchedulerService().scheduleJobNow(jobDetail);
    }

    public String startProcess(List<String> nodeIds, JCRSessionWrapper session, String processKey, String provider,
                               Map<String, Object> args, List<String> comments) throws RepositoryException {

        assertWritable();

        long startTime = System.currentTimeMillis();

        // retrieve the permission, required to start the workflow
        String startPermission = getPermissionForStart(workflowRegistrationByDefinition.get(processKey));

        WorkflowProvider providerImpl = lookupProvider(provider);
        List<String> checkedNodeIds = new ArrayList<String>();
        for (String nodeId : nodeIds) {
            try {
                JCRNodeWrapper n = session.getNodeByIdentifier(nodeId);
                if (startPermission == null || n.hasPermission(startPermission)) {
                    checkedNodeIds.add(nodeId);
                }
            } catch (ItemNotFoundException e) {
                // Item does not exist
            }
        }
        if (checkedNodeIds.isEmpty()) {
            return null;
        }
        String mainId = checkedNodeIds.iterator().next();
        Map<String, Object> newArgs = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            newArgs.put(entry.getKey().replaceAll(":", "_"), entry.getValue());
        }
        newArgs.put("nodeId", mainId);
        try {
            newArgs.put("nodePath", session.getNodeByIdentifier(mainId).getPath());
        } catch (ItemNotFoundException e) {
            // Node not found
        }
        newArgs.put("nodeIds", checkedNodeIds);
        newArgs.put("workspace", session.getWorkspace().getName());
        newArgs.put("locale", session.getLocale());
        newArgs.put("workflow", providerImpl.getWorkflowDefinitionByKey(processKey, session.getLocale()));
        newArgs.put("user", session.getUser() != null ? session.getUser().getUserKey() : null);
        if (comments != null && comments.size() > 0) {
            addCommentsToVariables(newArgs, comments, session.getUser().getUserKey());
        }
        final String processId = providerImpl.startProcess(processKey, newArgs);
        if (logger.isDebugEnabled()) {
            // if trace is enabled we log also the UUIDs of all nodes
            logger.debug(
                    "A workflow {} from {} has been started on {}{} nodes{}"
                            + " from workspace {} in locale {} with id {}{} in {} ms",
                    new Object[] { processKey, provider, checkedNodeIds.size(),
                            nodeIds.size() > checkedNodeIds.size() ? " (originally " + nodeIds.size() + ")" : "",
                            logger.isTraceEnabled() ? ": " + checkedNodeIds : "", newArgs.get("workspace"),
                            newArgs.get("locale"), processId,
                            startPermission != null ? " checking for permission " + startPermission : "",
                            System.currentTimeMillis() - startTime });
        }

        return processId;
    }

    private void addCommentsToVariables(Map<String, Object> args, List<String> comments, String userKey) {
        @SuppressWarnings("unchecked")
        List<WorkflowComment> wfComments = (List<WorkflowComment>) args.get("comments");
        if (wfComments == null) {
            wfComments = new LinkedList<WorkflowComment>();
            args.put("comments", wfComments);
        }
        Date timestamp = new Date();
        for (String comment : comments) {
            wfComments.add(new WorkflowComment(comment, timestamp, userKey));
        }
    }

    public synchronized void addProcessId(JCRNodeWrapper stageNode, String provider, String processId)
            throws RepositoryException {
        stageNode.checkout();
        if (!stageNode.isNodeType(Constants.JAHIAMIX_WORKFLOW)) {
            stageNode.addMixin(Constants.JAHIAMIX_WORKFLOW);
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

    public synchronized void removeProcessId(JCRNodeWrapper stageNode, String provider, String processId)
            throws RepositoryException {
        if (!stageNode.hasProperty(Constants.PROCESSID)) {
            return;
        }
        stageNode.checkout();
        List<Value> values =
                new ArrayList<Value>(Arrays.asList(stageNode.getProperty(Constants.PROCESSID).getValues()));
        List<Value> newValues = new ArrayList<Value>();
        for (Value value : values) {
            if (!value.getString().equals(provider + ":" + processId)) {
                newValues.add(value);
            }
        }
        if (newValues.isEmpty()) {
            if (stageNode.hasProperty(Constants.PROCESSID)) {
                stageNode.getProperty(Constants.PROCESSID).remove();
            }
        } else {
            stageNode.setProperty(Constants.PROCESSID, newValues.toArray(new Value[newValues.size()]));
        }
        stageNode.getSession().save();
    }

    public List<WorkflowTask> getTasksForUser(JahiaUser user, Locale uiLocale) {
        final List<WorkflowTask> workflowActions = new LinkedList<WorkflowTask>();
        for (Map.Entry<String, WorkflowProvider> providerEntry : providers.entrySet()) {
            workflowActions.addAll(providerEntry.getValue().getTasksForUser(user, uiLocale));
        }
        return workflowActions;
    }

    public List<Workflow> getWorkflowsForUser(JahiaUser user, Locale uiLocale) {
        final List<Workflow> workflow = new LinkedList<Workflow>();
        for (Map.Entry<String, WorkflowProvider> providerEntry : providers.entrySet()) {
            workflow.addAll(providerEntry.getValue().getWorkflowsForUser(user, uiLocale));
        }
        return workflow;
    }

    public List<Workflow> getWorkflowsForType(String type, Locale uiLocale) {
        List<Workflow> list = new ArrayList<Workflow>();
        for (WorklowTypeRegistration registration : workflowRegistrationByDefinition.values()) {
            if (registration.getType().equals(type)) {
                list.addAll(getWorkflowsForDefinition(registration.getDefinition(), uiLocale));
            }
        }
        return list;
    }

    public List<Workflow> getWorkflowsForDefinition(String definition, Locale uiLocale) {
        List<Workflow> list = new ArrayList<Workflow>();
        for (WorkflowProvider provider : providers.values()) {
            list.addAll(provider.getWorkflowsForDefinition(definition, uiLocale));
        }
        return list;
    }


    public void assignTask(String taskId, String provider, JahiaUser user) {
        assertWritable();
        logger.debug("Assigning user {} to task {}", user, taskId);
        lookupProvider(provider).assignTask(taskId, user);
    }

    public void completeTask(String taskId, JahiaUser user, String provider, String outcome, Map<String, Object> args) {
        assertWritable();
        lookupProvider(provider).completeTask(taskId, user, outcome, args);
    }

    public void assignAndCompleteTaskAsJob(String taskId, String provider, String outcome, Map<String, Object> args, JahiaUser user) throws RepositoryException, SchedulerException {
        JobDetail jobDetail = BackgroundJob.createJahiaJob("AssignAndCompleteTask", AssignAndCompleteTaskJob.class);
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put(BackgroundJob.JOB_USERKEY, user.getUserKey());
        jobDataMap.put(AssignAndCompleteTaskJob.TASK_ID, taskId);
        jobDataMap.put(AssignAndCompleteTaskJob.PROVIDER, provider);
        jobDataMap.put(AssignAndCompleteTaskJob.OUTCOME, outcome);
        jobDataMap.put(AssignAndCompleteTaskJob.MAP, args);

        ServicesRegistry.getInstance().getSchedulerService().scheduleJobNow(jobDetail);
    }

    public void assignAndCompleteTask(String taskId, String provider, String outcome, Map<String, Object> args, JahiaUser user) {
        assignTask(taskId, provider, user);
        completeTask(taskId, user, provider, outcome, args);
    }

    public void addWorkflowRule(final JCRNodeWrapper node, final WorkflowDefinition workflow) throws RepositoryException {
        // store the rule
        JCRNodeWrapper rules = null;
        try {
            rules = node.getNode(WORKFLOWRULES_NODE_NAME);
        } catch (RepositoryException e) {
            if (!node.isCheckedOut()) {
                node.checkout();
            }
            node.addMixin("jmix:workflowRulesable");
            rules = node.addNode(WORKFLOWRULES_NODE_NAME, "jnt:workflowRules");
        }
        JCRNodeWrapper n;
        String wfName = workflow.getProvider() + "_" + workflow.getKey();
        if (rules.hasNode(wfName)) {
            n = rules.getNode(wfName);
        } else {
            n = rules.addNode(wfName, "jnt:workflowRule");
        }
        if (!n.isCheckedOut()) {
            n.checkout();
        }
        n.setProperty("j:workflow", workflow.getProvider() + ":" + workflow.getKey());
    }

    public void addComment(String processId, String provider, String comment, String user) {
        assertWritable();
        lookupProvider(provider).addComment(processId, comment, user);
    }

    public WorkflowTask getWorkflowTask(String taskId, String provider, Locale displayLocale) {
        WorkflowTask workflowTask = lookupProvider(provider).getWorkflowTask(taskId, displayLocale);
        return workflowTask;
    }

    public HistoryWorkflow getHistoryWorkflow(String id, String provider, Locale uiLocale) {
        List<HistoryWorkflow> list = lookupProvider(provider).getHistoryWorkflows(Collections.singletonList(id), uiLocale);
        if (!list.isEmpty()) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * Returns a list of process instance history records for the specified
     * node. This method also returns "active" (i.e. not completed) workflow
     * process instance.
     *
     * @param node   the JCR node to retrieve history records for
     * @param uiLocale the current UI locale
     * @return a list of process instance history records for the specified node
     */
    public List<HistoryWorkflow> getHistoryWorkflows(JCRNodeWrapper node, Locale uiLocale) {
        List<HistoryWorkflow> history = new LinkedList<HistoryWorkflow>();
        try {
            for (WorkflowProvider workflowProvider : providers.values()) {
                history.addAll(workflowProvider.getHistoryWorkflowsForNode(node.getIdentifier(), uiLocale));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return history;
    }

    /**
     * Returns a list of process instance history records for the specified
     * path. This method also returns "active" (i.e. not completed) workflow
     * process instance.
     *
     * @param path   the Path of the node to retrieve history records for
     * @param locale
     * @return a list of process instance history records for the specified node
     */
    public List<HistoryWorkflow> getHistoryWorkflowsByPath(String path, Locale locale) {
        List<HistoryWorkflow> history = new LinkedList<HistoryWorkflow>();
        for (WorkflowProvider workflowProvider : providers.values()) {
            history.addAll(workflowProvider.getHistoryWorkflowsForPath(path, locale));
        }
        return history;
    }

    /**
     * Returns a list of history records for workflow tasks.
     * This method also returns not completed tasks.
     *
     * @param workflowProcessId the process instance ID
     * @param providerKey       the workflow provider key
     * @param uiLocale current UI display locale
     * @return a list of history records for workflow tasks
     */
    public List<HistoryWorkflowTask> getHistoryWorkflowTasks(String workflowProcessId, String providerKey,
                                                             Locale uiLocale) {
        List<HistoryWorkflowTask> list = lookupProvider(providerKey).getHistoryWorkflowTasks(workflowProcessId, uiLocale);
        return list;
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
     * @param type
     * @return A list of active workflows per provider
     */
    public boolean hasActiveWorkflowForType(JCRNodeWrapper node, String type) {
        List<Workflow> workflows = new ArrayList<Workflow>();
        try {
            final List<WorkflowDefinition> forAction = getWorkflowDefinitionsForType(type, null);
            if (node.isNodeType(Constants.JAHIAMIX_WORKFLOW) && node.hasProperty(Constants.PROCESSID)) {
                addActiveWorkflows(workflows, node.getProperty(Constants.PROCESSID), node.getSession().getLocale());
            }
            for (Workflow workflow : workflows) {
                if (forAction.contains(workflow.getWorkflowDefinition())) {
                    return true;
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public void addWorkflowRule(JCRNodeWrapper node, String wfName)
            throws RepositoryException {
        String provider = StringUtils.substringBefore(wfName, ":");
        String wfKey = StringUtils.substringAfter(wfName, ":");
        WorkflowDefinition definition = lookupProvider(provider).getWorkflowDefinitionByKey(wfKey, node.getSession().getLocale());
        addWorkflowRule(node, definition);
    }

    public WorkflowRule getWorkflowRuleForAction(JCRNodeWrapper objectNode, boolean checkPermission, String action) throws RepositoryException {
        Collection<WorkflowRule> rules = getWorkflowRulesForType(objectNode, checkPermission, action);
        if (rules.isEmpty()) {
            return null;
        } else {
            return rules.iterator().next();
        }
    }

    private Collection<WorkflowRule> getWorkflowRulesForType(JCRNodeWrapper objectNode, boolean checkPermission, String type) throws RepositoryException {

        Collection<WorkflowRule> results = new LinkedHashSet<WorkflowRule>();
        Collection<WorkflowRule> rules = getWorkflowRules(objectNode);

        for (WorkflowRule rule : rules) {
            final WorklowTypeRegistration worklowTypeRegistration = workflowRegistrationByDefinition.get(rule.getWorkflowDefinitionKey());
            if (type == null || worklowTypeRegistration.getType().equals(type)) {
                String permName = checkPermission ? getPermissionForStart(worklowTypeRegistration) : null;
                if (permName == null || objectNode.hasPermission(permName)) {
                            results.add(rule);
                        }
                    }
                }
        return results;
    }

    public Collection<WorkflowRule> getWorkflowRules(JCRNodeWrapper objectNode) {

        try {

            Map<String, WorkflowRule> rules = recurseOnRules(objectNode);
            Map<String, List<String>> perms = new HashMap<>();

            JCRNodeWrapper rootNode = objectNode.getSession().getNode("/");
            JahiaAccessManager accessControlManager = (JahiaAccessManager) rootNode.getRealNode().getSession().getAccessControlManager();
            final Map<String, List<String[]>> aclEntries = objectNode.getAclEntries() ;
            if (aclEntries != null) {
                for (List<String[]> list : aclEntries.values()) {
                    for (String[] strings : list) {
                        for (Privilege privilege : accessControlManager.getPermissionsInRole(strings[2])) {
                            if (!perms.containsKey(strings[0])) {
                                perms.put(strings[0], new ArrayList<String>());
                            }
                            perms.get(strings[0]).add(JCRContentUtils.getJCRName(privilege.getName(), objectNode.getRealNode().getSession().getWorkspace().getNamespaceRegistry()));
                        }
                    }
                }
            }
            Map<String, WorkflowRule> rulesCopy = new HashMap<>(rules);
            for (Map.Entry<String, WorkflowRule> ruleEntry : rules.entrySet()) {
                WorkflowRule rule = ruleEntry.getValue();
                for (Map.Entry<String, List<String>> aclEntry : perms.entrySet()) {
                    if (aclEntry.getKey().startsWith(rule.getDefinitionPath().equals("/") ? "/" : rule.getDefinitionPath() + "/")) {
                        if (!Collections.disjoint(aclEntry.getValue(), rule.getPermissions().values())) {
                            rule = new WorkflowRule(aclEntry.getKey(), ruleEntry.getValue().getDefinitionPath(), rule.getProviderKey(), rule.getWorkflowDefinitionKey(), rule.getPermissions());
                            rulesCopy.put(ruleEntry.getKey(), rule);
                        }
                    }
                }
            }
            return Collections.unmodifiableCollection(rulesCopy.values());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private Map<String, WorkflowRule> recurseOnRules(final JCRNodeWrapper n)
            throws RepositoryException {

        String nodePath = n.getPath();
        Map<String, WorkflowRule> results = cache.get(nodePath);
        if (results != null) {
            return results;
        }

        if ("/".equals(nodePath)) {
            results = getDefaultRules(n);
        } else {

            if (n.isNodeType("jnt:virtualsite")) {
                results = getDefaultRules(n);
            } else {
                results = recurseOnRules(n.getParent());
            }

            if (n.hasNode(WORKFLOWRULES_NODE_NAME)) {
                results = new HashMap<String, WorkflowRule>(results);

                Node wfRules = n.getNode(WORKFLOWRULES_NODE_NAME);
                NodeIterator rules = wfRules.getNodes();
                while (rules.hasNext()) {
                    Node rule = rules.nextNode();
                    final String wfName = rule.getProperty("j:workflow").getString();
                    String name = StringUtils.substringAfter(wfName, ":");
                    String prov = StringUtils.substringBefore(wfName, ":");
                    final WorklowTypeRegistration type = workflowRegistrationByDefinition.get(name);
                    if (type == null) {
                        continue;
                    }
                    String wftype = type.getType();

                    results.put(wftype, new WorkflowRule(nodePath, nodePath, prov, name, type.getPermissions()));
                }
            }
        }

        cache.put(nodePath, results);
        return results;
    }

    @Override
    public void onApplicationEvent(JahiaTemplateManagerService.ModuleDeployedOnSiteEvent event) {
        cache.flush();
    }

    private Map<String, WorkflowRule> getDefaultRules(JCRNodeWrapper n) throws RepositoryException {
        Map<String, WorkflowRule> results = new HashMap<String, WorkflowRule>();
        Map<String, WorklowTypeRegistration> m = new HashMap<String, WorklowTypeRegistration>();
        for (WorklowTypeRegistration registration : workflowRegistrationByDefinition.values()) {
            if (registration.isCanBeUsedForDefault() &&
                    (!m.containsKey(registration.getType()) || m.get(registration.getType()).getDefaultPriority() < registration.getDefaultPriority()) &&
                    isRegistrationAvailableForSite(n.getResolveSite(), registration)) {
                m.put(registration.getType(), registration);
            }
        }
        for (Map.Entry<String, WorklowTypeRegistration> entry : m.entrySet()) {
            results.put(entry.getValue().getType(), new WorkflowRule("/", "/", entry.getValue().getProvider(), entry.getValue().getDefinition(), entry.getValue().getPermissions()));
        }
        return results;
    }

    public Workflow getWorkflow(String provider, String id, Locale displayLocale) {
        return lookupProvider(provider).getWorkflow(id, displayLocale);
    }

    public WorkflowDefinition getWorkflowDefinition(String provider, String id, Locale locale) {
        if (getWorkflowRegistration(id) == null) {
            return null;
        }
        final WorkflowDefinition definition = lookupProvider(provider).getWorkflowDefinitionByKey(id, locale);
        return definition;
    }


    public WorklowTypeRegistration getWorkflowRegistration(String definitionKey) {
        return workflowRegistrationByDefinition.get(definitionKey);
    }

    public String getWorkflowType(WorkflowDefinition def) {
        return workflowRegistrationByDefinition.get(def.getKey()).getType();
    }

    public String getFormForAction(String definitionKey, String action) {
        if (workflowRegistrationByDefinition.get(definitionKey).getForms() != null) {
            return workflowRegistrationByDefinition.get(definitionKey).getForms().get(action);
        }
        return null;
    }


    public String getModuleForWorkflow(String key) {
        return modulesForWorkflowDefinition.get(key);
    }

    public Set<String> getTypesOfWorkflow() {
        Set<String> s = new HashSet<String>();
        for (WorklowTypeRegistration registration : workflowRegistrationByDefinition.values()) {
            s.add(registration.getType());
        }
        return s;
    }

    public void deleteProcess(String processId, String provider) {
        assertWritable();
        lookupProvider(provider).deleteProcess(processId);
    }

    public void addWorkflowListener(WorkflowListener listener) {
        observationManager.addWorkflowListener(listener);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        if (bean instanceof WorklowTypeRegistration) {
            WorklowTypeRegistration registration = (WorklowTypeRegistration) bean;
            registerWorkflowType(registration);
            logger.info("Registering workflow type \"" + registration.getType()
                    + "\" with definition \"" + registration.getDefinition()
                    + "\" and permissions: " + registration.getPermissions());
        }
        return bean;
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public WorkflowObservationManager getObservationManager() {
        return observationManager;
    }

    // TODO: implement JahiaAfterInitializationService instead of relying on invocation of this method from JBPM6WorkflowProvider.
    public synchronized void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        servicesStarted = true;
        registerWorkflowTypes();
    }

    /**
     * Returns the permission name, required to start the workflow of the specified type.
     *
     * @param worklowTypeRegistration
     *            the workflow type registration object
     * @return the permission name, required to start the workflow of the specified type
     */
    private String getPermissionForStart(WorklowTypeRegistration worklowTypeRegistration) {
        String startPermission = null;
        if (worklowTypeRegistration != null && worklowTypeRegistration.getPermissions() != null) {
            startPermission = worklowTypeRegistration.getPermissions().get(START_ROLE);
            if (startPermission != null) {
                int pos = startPermission.lastIndexOf('/');
                if (pos != -1 && pos < startPermission.length() - 1) {
                    startPermission = startPermission.substring(pos);
                }
            }
        }

        return startPermission;
    }

    @Override
    public void switchReadOnlyMode(boolean enable) {
        readOnly = enable;
    }

    @Override
    public int getReadOnlyModePriority() {
        return 0;
    }

    private void assertWritable() {
        if (readOnly) {
            throw new ReadOnlyModeException("The Workflow Service is in read only mode: no operations that modify workflow state are available");
        }
    }
}
