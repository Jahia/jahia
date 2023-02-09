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
package org.jahia.services.render;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.render.filter.RenderServiceAware;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.render.scripting.ScriptResolver;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.WebUtils;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import pl.touk.throwing.ThrowingFunction;
import pl.touk.throwing.ThrowingPredicate;

import javax.jcr.*;
import javax.servlet.http.HttpSession;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service to render node
 *
 * @author toto
 */
public class RenderService implements InitializingBean {

    private static class TemplatePriorityComparator implements Comparator<Template>, Serializable {
        private static final long serialVersionUID = 3462731866743025112L;

        @Override
        public int compare(Template o1, Template o2) {
            return o1.getPriority() - o2.getPriority();
        }
    }

    private static class TemplateNodePriorityComparator implements Comparator<JCRNodeWrapper>, Serializable {
        private static final long serialVersionUID = 5584826951926361906L;

        @Override
        public int compare(JCRNodeWrapper node1, JCRNodeWrapper node2) {
            long node1Priority = 0;
            long node2Priority = 0;
            try {
                if (node1.hasProperty("j:priority")) {
                    node1Priority = node1.getProperty("j:priority").getLong();
                }
                if (node2.hasProperty("j:priority")) {
                    node2Priority = node2.getProperty("j:priority").getLong();
                }
            } catch (RepositoryException e) {
                logger.error("Could not read property j:priority", e);
            }
            return - Long.compare(node1Priority, node2Priority);
        }
    }

    private static final Comparator<Template> TEMPLATE_PRIORITY_COMPARATOR = new TemplatePriorityComparator();

    private static final Comparator<JCRNodeWrapper> TEMPLATE_NODE_PRIORITY_COMPARATOR = new TemplateNodePriorityComparator();

    public static final String RENDER_SERVICE_TEMPLATES_CACHE = "RenderService.TemplatesCache";

    private Cache templatesCache;

    private ConcurrentMap<String, ReentrantReadWriteLock> templatesCacheLockAccess = new ConcurrentHashMap<>();

    private long templatesCacheLockTimeout;

    private ChannelService channelService;

    private RenderTimeMonitor renderTimeMonitor;

    private JahiaUserManagerService userManagerService;

    // Defines a list of node types on witch a themed content template can be found.
    private List<String> nodeTypesWithTheme;

    public static class RenderServiceBeanPostProcessor implements BeanPostProcessor {
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof RenderServiceAware) {
                ((RenderServiceAware) bean).setRenderService(getInstance());
            }
            if (bean instanceof RenderFilter) {
                logger.debug("Registering render filter {}", bean.getClass().getName());
                getInstance().addFilter((RenderFilter) bean);
            }
            return bean;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(RenderService.class);

    private void addFilter(RenderFilter renderFilter) {
        if (filters.contains(renderFilter)) {
            filters.remove(renderFilter);
        }
        filters.add(renderFilter);
    }

    private static volatile RenderService instance;

    public static RenderService getInstance() {
        if (instance == null) {
            synchronized (RenderService.class) {
                if (instance == null) {
                    instance = new RenderService();
                }
            }
        }
        return instance;
    }

    private String settingsPanelTheme;

    private JahiaTemplateManagerService templateManagerService;

    private Collection<ScriptResolver> scriptResolvers;

    private List<RenderFilter> filters = new LinkedList<RenderFilter>();

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    public void setScriptResolvers(Collection<ScriptResolver> scriptResolvers) {
        this.scriptResolvers = scriptResolvers;
    }

    public Collection<ScriptResolver> getScriptResolvers() {
        return scriptResolvers;
    }

    public List<String> getNodeTypesWithTheme() {
        return nodeTypesWithTheme;
    }

    public void setNodeTypesWithTheme(List<String> nodeTypesWithTheme) {
        this.nodeTypesWithTheme = nodeTypesWithTheme;
    }

    public void start() throws JahiaInitializationException {

    }

    public void stop() throws JahiaException {

    }

    /**
     * Render a specific resource and returns it as a StringBuffer.
     *
     * @param resource Resource to display
     * @param context  The render context
     * @return The rendered result
     * @throws RenderException in case of rendering issues
     */
    public String render(Resource resource, RenderContext context) throws RenderException {
        /*
            TODO BACKLOG-6561: used for retro compatibility for AggregateCacheFilter
                the stack of resources is now handle internally by the AggregateFilter
                we need to remove this the day we stop supporting the AggregateCacheFilter implementation
         */

        if (context.getResourcesStack().contains(resource)) {
            String resourceMessage = Messages.getInternal("label.render.loop", context.getUILocale());
            String formattedMessage = MessageFormat.format(resourceMessage, resource.getPath());
            logger.warn("Loop detected while rendering resource {}. Please check your content structure and references.", resource.getPath());
            return formattedMessage;
        }

        if (renderTimeMonitor != null) {
            renderTimeMonitor.track(context.getRequest());
        }

        String output = getRenderChainInstance().doFilter(context, resource);

        if (renderTimeMonitor != null) {
            renderTimeMonitor.monitor(resource, context);
        }

        return output;
    }

    /**
     * This resolves the executable script from the resource object. This should be able to find the proper script
     * depending of the template / template type. Currently resolves only simple JSPScript.
     * <p/>
     * If template cannot be resolved, fall back on default template
     *
     * @param resource The resource to display
     * @param context rendering context instance
     * @return An executable script
     * @throws RepositoryException in case of JCR-related errors
     * @throws TemplateNotFoundException in case the template could not be found for the supplied resource
     */
    public Script resolveScript(Resource resource, RenderContext context) throws RepositoryException, TemplateNotFoundException {
        for (ScriptResolver scriptResolver : scriptResolvers) {
            try {
                Script s = scriptResolver.resolveScript(resource, context);
                if (s != null) {
                    return s;
                }
            } catch (TemplateNotFoundException tnfe) {
                // skip this resolver and continue with the next one
            }
        }
        throw new TemplateNotFoundException("Unable to find the template for resource " + resource);
    }


    public boolean hasView(JCRNodeWrapper node, String key, String templateType, RenderContext renderContext) {
        try {
            if (hasView(node.getPrimaryNodeType(), key, renderContext.getSite(), templateType, renderContext)) {
                return true;
            }
            for (ExtendedNodeType type : node.getMixinNodeTypes()) {
                if (hasView(type, key, renderContext.getSite(), templateType, renderContext)) {
                    return true;
                }
            }
        } catch (RepositoryException e) {
            // ignore and continue resolution
        }
        return false;
    }

    public boolean hasView(ExtendedNodeType nt, String key, JCRSiteNode site, String templateType, RenderContext renderContext) {
        for (ScriptResolver scriptResolver : scriptResolvers) {
            if (scriptResolver.hasView(nt, key, site, templateType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a new instance of the {@link RenderChain} with all filters.
     *
     * @return a new instance of the {@link RenderChain} with all filters
     */
    public RenderChain getRenderChainInstance() {
        return new RenderChain(filters, templateManagerService.getRenderFilters());
    }

    public SortedSet<View> getViewsSet(ExtendedNodeType nt, JCRSiteNode site, String templateType) {
        SortedSet<View> set = new TreeSet<View>();
        for (ScriptResolver scriptResolver : scriptResolvers) {
            set.addAll(scriptResolver.getViewsSet(nt, site, templateType));
        }
        return set;
    }

    public Template resolveTemplate(Resource resource, RenderContext renderContext) throws RepositoryException {
        final JCRNodeWrapper node = resource.getNode();
        String templateName = resource.getTemplate();
        if ("default".equals(templateName)) {
            templateName = null;
        }

        Template template = null;

        JCRNodeWrapper site = renderContext.getSite();
        if (site == null) {
            site = node.getResolveSite();
        }

        if (node.isNodeType("jnt:template")) {
            // Display a template node in studio
            if (!node.hasProperty("j:view") || "default".equals(node.getProperty("j:view").getString())) {
                JCRNodeWrapper parent = node.getParent();
                while (!(parent.isNodeType("jnt:templatesFolder"))) {
                    template = new Template(parent.hasProperty("j:view") ? parent.getProperty("j:view").getString() :
                            templateName, parent.getIdentifier(), template, parent.getName());
                    parent = parent.getParent();
                }

                String packageName = "templates-system";
                if (parent.hasProperty("j:templateSetContext")) {
                    renderContext.setSite((JCRSiteNode) parent.getProperty("j:templateSetContext").getNode());
                    packageName = parent.getProperty("j:templateSetContext").getNode().getName();
                }
                Set<String> installed = new LinkedHashSet<String>();
                installed.add(packageName);
                for (JahiaTemplatesPackage aPackage : templateManagerService.getTemplatePackageById(packageName).getDependencies()) {
                    installed.add(aPackage.getId());
                }
                template = addContextualTemplates(resource, renderContext, template, parent, installed);
            }
        } else {
            if (resource.getTemplate().equals("default") && node.hasProperty("j:templateName")) {
                // A template node is specified on the current node
                templateName = node.getProperty("j:templateName").getString();
            }

            Set<String> installedModules = new LinkedHashSet<String>(((JCRSiteNode) site).getInstalledModulesWithAllDependencies());
            installedModules.add("templates-system");

            String type = "jnt:contentTemplate";
            if (resource.getNode().isNodeType("jnt:page")) {
                type = "jnt:pageTemplate";
            }

            // Allows to override template by theme for server settings and site settings
            String theme = getSettingsPanelsTheme(renderContext);
            if (theme != null
                    && JCRContentUtils.isNodeType(resource.getNode(), nodeTypesWithTheme)) {
                template = addTemplate(resource, renderContext, templateName + "-" + theme, installedModules, type);
            }
            if (template == null) {
                template = addTemplate(resource, renderContext, templateName, installedModules, type);
            }

            if (template != null) {
                // Add cascade of parent templates
                JCRNodeWrapper templateNode = resource.getNode().getSession().getNodeByIdentifier(template.getNode()).getParent();
                while (!(templateNode.isNodeType("jnt:templatesFolder"))) {
                    template = new Template(templateNode.hasProperty("j:view") ? templateNode.getProperty("j:view").getString() :
                            null, templateNode.getIdentifier(), template, templateNode.getName());

                    templateNode = templateNode.getParent();
                }
                template = addContextualTemplates(resource, renderContext, template, templateNode, installedModules);
            } else {
                return null;
            }
        }
        if (template != null) {
            Template currentTemplate = template;
            // Be sure to take the first template which has a defined view
            do {
                if (!currentTemplate.getView().equals("default")) {
                    template = currentTemplate;
                }
                currentTemplate = currentTemplate.getNext();
            } while (currentTemplate != null);
        } else {
            template = new Template(null, null, null, null);
        }

        return template;
    }

    private Template addContextualTemplates(Resource resource, RenderContext renderContext, Template template, JCRNodeWrapper parent, Set<String> modules) throws RepositoryException {
        if (parent.isNodeType("jnt:templatesFolder") && parent.hasProperty("j:rootTemplatePath")) {
            String rootTemplatePath = parent.getProperty("j:rootTemplatePath").getString();

            if (rootTemplatePath.contains("/")) {
                rootTemplatePath = StringUtils.substringAfterLast(rootTemplatePath, "/");
            }
            if (!StringUtils.isEmpty(rootTemplatePath)) {
                Template t = addTemplate(resource, renderContext, rootTemplatePath, modules, "jnt:template");

                if (t != null) {
                    t.setNext(template);
                    return t;
                }
            }
        }
        return template;
    }

    private Template addTemplate(Resource resource, RenderContext renderContext, String templateName, Set<String> installedModules, String type) throws RepositoryException {
        SortedSet<Template> templates = new TreeSet<Template>(TEMPLATE_PRIORITY_COMPARATOR);
        for (String s : installedModules) {
            JahiaTemplatesPackage pack = templateManagerService.getTemplatePackageById(s);
            if (pack != null) {
                JCRNodeWrapper templateNode = resource.getNode().getSession().getNode("/modules/" + s + "/" + pack.getVersion());
                templates.addAll(addTemplates(resource, renderContext, templateName, templateNode, type));
            }
        }
        return templates.isEmpty() ? null : templates.last();
    }

    private SortedSet<Template> addTemplates(Resource resource, RenderContext renderContext, String templateName,
                                             JCRNodeWrapper templateNode, String type) throws RepositoryException {
        List<JCRNodeWrapper> nodes = getTemplateNodes(templateName, templateNode.getPath(), type, templateName == null, templateNode.getSession());

        SortedSet<Template> templates = new TreeSet<Template>(TEMPLATE_PRIORITY_COMPARATOR);
        for (JCRNodeWrapper contentTemplateNode : nodes) {
            addTemplate(resource, renderContext, contentTemplateNode, templates);
        }
        return templates;
    }

    public List<JCRNodeWrapper> getTemplateNodes(String templateName, String path, String type, boolean defaultOnly, JCRSessionWrapper session) throws RepositoryException {
        String key = new StringBuilder(path)
                .append(type)
                .append(defaultOnly)
                .append(templateName != null ? templateName : "*all*").toString();

        return doExecuteWithTemplatesCacheLock(path, false, () -> {
            List<JCRNodeWrapper> nodes = new ArrayList<>();

            Element nodeIdsElement = templatesCache.get(key);
            @SuppressWarnings("unchecked")
            List<String> nodeIds = nodeIdsElement != null ? (List<String>) nodeIdsElement.getObjectValue() : null;
            if (nodeIds != null) {
                for (String nodeId : nodeIds) {
                    JCRNodeWrapper node = session.getNodeByIdentifier(nodeId);
                    nodes.add(node);
                }
            } else {
                long startTime = System.currentTimeMillis();

                List<JCRNodeWrapper> results = getDescendantNodesOfType(session.getNode(path + "/templates"), type);
                Stream<JCRNodeWrapper> stream = results.stream();
                nodes = stream.filter(ThrowingPredicate.unchecked(node -> (StringUtils.isBlank(templateName) || node.getName().equals(templateName))
                        && (!defaultOnly || (node.hasProperty("j:defaultTemplate") && node.getProperty("j:defaultTemplate").getBoolean()))))
                        .sorted(TEMPLATE_NODE_PRIORITY_COMPARATOR)
                        .collect(Collectors.toList());

                nodeIds = nodes.stream().map(ThrowingFunction.unchecked(Node::getIdentifier)).collect(Collectors.toList());

                templatesCache.put(new Element(key, nodeIds));

                if (logger.isDebugEnabled()) {
                    logger.debug("Generated cache entry for key {} with {} node IDs in {} ms",
                            new Object[] { key, nodeIds.size(), System.currentTimeMillis() - startTime });
                }
            }

            return nodes;
        });
    }

    private List<JCRNodeWrapper> getDescendantNodesOfType(JCRNodeWrapper node, String type) {
        List<JCRNodeWrapper> nodesToIterate = JCRContentUtils.getNodes(node, "jnt:template");
        List<JCRNodeWrapper> nodesToAdd = JCRContentUtils.getNodes(node, type);
        List<JCRNodeWrapper> results = new ArrayList<>(nodesToAdd);

        for (JCRNodeWrapper childNode : nodesToIterate) {
            results.addAll(getDescendantNodesOfType(childNode, type));
        }

        return results;
    }

    private void addTemplate(Resource resource, RenderContext renderContext, JCRNodeWrapper templateNode, SortedSet<Template> templates)
            throws RepositoryException {
        if (templateNode.hasProperty("j:applyOn")) {
            boolean ok = false;
            Value[] values = templateNode.getProperty("j:applyOn").getValues();
            for (Value value : values) {
                if (resource.getNode().isNodeType(value.getString())) {
                    ok = true;
                    break;
                }
            }
            if (values.length == 0) {
                ok = true;
            }
            if (!ok) {
                return;
            }
        }

        if (!checkChannel(renderContext, templateNode)) return;

        if (!checkTemplatePermission(resource, renderContext, templateNode)) return;

        templates.add(new Template(templateNode.hasProperty("j:view") ? templateNode.getProperty("j:view")
                .getString() : null, templateNode.getIdentifier(), null, templateNode.getName(), templateNode
                .hasProperty("j:priority") ? (int) templateNode.getProperty("j:priority").getLong() : 0));
    }

    private boolean checkChannel(RenderContext renderContext, JCRNodeWrapper templateNode) throws RepositoryException {
        if (templateNode.isNodeType("jmix:channelSelection") && templateNode.hasProperty("j:channelSelection")) {
            Property channelExclusionProperty = templateNode.getProperty("j:channelSelection");
            String includeOrExclude = templateNode.hasProperty("j:channelIncludeOrExclude") ? templateNode.getProperty("j:channelIncludeOrExclude").getString() : "exclude";
            Value[] channelExclusionValues = channelExclusionProperty.getValues();
            Channel currentChannel = renderContext.getChannel();
            for (Value channelExclusionValue : channelExclusionValues) {
                if (channelExclusionValue.getString() != null) {
                    boolean inList = channelService.matchChannel(channelExclusionValue.getString(), currentChannel);
                    if (inList && includeOrExclude.equals("exclude")) {
                        return renderContext.isEditMode() && (renderContext.getChannel() == null || renderContext.getChannel().getIdentifier().equals("generic"));
                    }
                    if (inList && includeOrExclude.equals("include")) {
                        return true;
                    }
                }
            }
            if (includeOrExclude.equals("include")) {
                return renderContext.isEditMode() && (renderContext.getChannel() == null || renderContext.getChannel().getIdentifier().equals("generic"));
            }
        }
        return true;
    }

    private boolean checkTemplatePermission(Resource resource, RenderContext renderContext, JCRNodeWrapper templateNode) throws RepositoryException {
        boolean invert = templateNode.hasProperty("j:invertCondition") && templateNode.getProperty("j:invertCondition").getBoolean();

        // renderContext.getMode() can be null when we display nodes from the repository explorer
        if (templateNode.hasProperty("j:requiredMode") && renderContext.getMode() != null) {
            String req = templateNode.getProperty("j:requiredMode").getString();
            if (!renderContext.getMode().equals(req)) {
                return invert;
            }
        }
        if (templateNode.hasProperty("j:requiredPermissionNames") || templateNode.hasProperty("j:requiredPermissions")) {
            final List<String> perms = new ArrayList<String>();
            if (templateNode.hasProperty("j:requiredPermissions") && !templateNode.hasProperty("j:requiredPermissionNames")) {
                final Value[] values = templateNode.getProperty("j:requiredPermissions").getValues();
                perms.addAll(JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<String>>() {
                    public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        List<String> permissionNames = new ArrayList<String>();
                        for (Value value : values) {
                            permissionNames.add(session.getNodeByUUID(value.getString()).getName());
                        }
                        return permissionNames;
                    }
                }));
            } else {
                final Value[] values = templateNode.getProperty("j:requiredPermissionNames").getValues();
                for (Value value : values) {
                    perms.add(value.getString());
                }
            }

            JCRNodeWrapper contextNode = resource.getNode();
            if (templateNode.hasProperty("j:contextNodePath")) {
                String contextPath = templateNode.getProperty("j:contextNodePath").getString();
                if (!StringUtils.isEmpty(contextPath)) {
                    if (contextPath.startsWith("/")) {
                        contextNode = contextNode.getSession().getNode(contextPath);
                    } else {
                        contextNode = contextNode.getNode(contextPath);
                    }
                }
            }
            for (String perm : perms) {
                if (!contextNode.hasPermission(perm)) {
                    return invert;
                }
            }
        }
        if (templateNode.hasProperty("j:requireLoggedUser") && templateNode.getProperty("j:requireLoggedUser").getBoolean()) {
            if (!renderContext.isLoggedIn()) {
                return invert;
            }
        }
        if (templateNode.hasProperty("j:requirePrivilegedUser") && templateNode.getProperty("j:requirePrivilegedUser").getBoolean()) {
            JCRUserNode userNode = userManagerService.lookupUserByPath(renderContext.getUser().getLocalPath());
            if (userNode != null && !userNode.isMemberOfGroup(null, JahiaGroupManagerService.PRIVILEGED_GROUPNAME)) {
                return invert;
            }
        }
        return !invert;
    }

    public void flushCache(String modulePath) {
        try {
            doExecuteWithTemplatesCacheLock(modulePath, true, () -> {
                @SuppressWarnings("unchecked")
                Collection<String> keys = new HashSet<String>(templatesCache.getKeys());
                for (String key : keys) {
                    if (key.startsWith(modulePath)) {
                        templatesCache.remove(key);
                    }
                }
                return null;
            });
        } catch (RepositoryException e) {
            // should not happen because there is no JCR code in the callback, but still necessary to respect throwable in fct signature
            logger.error(e.getMessage(), e);
        }
    }

    private <X> X doExecuteWithTemplatesCacheLock(String modulePath, boolean write, LockedExecutionCallback<X> lockedExecutionCallback) throws RepositoryException {

        // template cache locking is disabled, just execute the callback directly
        if (templatesCacheLockTimeout == 0) {
            return lockedExecutionCallback.doExecuteWithLock();
        }

        // get the lock
        ReentrantReadWriteLock reentrantReadWriteLock = templatesCacheLockAccess.computeIfAbsent(modulePath, k -> new ReentrantReadWriteLock());
        Lock lock = write ? reentrantReadWriteLock.writeLock() : reentrantReadWriteLock.readLock();

        // timeout configured
        if (templatesCacheLockTimeout > 0) {
            try {
                if (lock.tryLock(templatesCacheLockTimeout, TimeUnit.MILLISECONDS)) {
                    try {
                        return lockedExecutionCallback.doExecuteWithLock();
                    } finally {
                        lock.unlock();
                    }
                } else {
                    throw new RuntimeException(String.format("Timeout [%d] reached: fail to acquire [%s] lock on templates cache for [%s]",
                            templatesCacheLockTimeout, (write ? "WRITE" : "READ"), modulePath));
                }
            } catch (InterruptedException e) {
                throw new CancellationException(String.format("Thread interrupted: fail to acquire [%s] lock on templates cache for [%s]",
                        (write ? "WRITE" : "READ"), modulePath));
            }
        }

        // no timeout configured
        lock.lock();
        try {
            return lockedExecutionCallback.doExecuteWithLock();
        } finally {
            lock.unlock();
        }
    }

    private interface LockedExecutionCallback<T> {
        T doExecuteWithLock() throws RepositoryException;
    }

    public void setCacheProvider(EhCacheProvider cacheProvider) {
        CacheManager cacheManager = cacheProvider.getCacheManager();
        templatesCache = cacheManager.getCache(RENDER_SERVICE_TEMPLATES_CACHE);
        if (templatesCache == null) {
            cacheManager.addCache(RENDER_SERVICE_TEMPLATES_CACHE);
            templatesCache = cacheManager.getCache(RENDER_SERVICE_TEMPLATES_CACHE);
        }
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    public void setRenderTimeMonitor(RenderTimeMonitor renderChainMonitor) {
        this.renderTimeMonitor = renderChainMonitor;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsPanelTheme = settingsBean.getString("jahia.ui.settingsPanels.theme", null);
    }

    private String getSettingsPanelsTheme(RenderContext renderContext) {
        String mode = renderContext.getMode();
        if (mode != null && ("studio".equals(mode) || "studiovisual".equals(mode))) {
            // we do not use theme-specific settings panels in Studio mode
            return null;
        }
        String theme = null;
        if (settingsPanelTheme != null) {
            theme = settingsPanelTheme.equals("default") ? null : settingsPanelTheme;
        } else if (renderContext.getRequest() != null) {
            theme = WebUtils.getUITheme(renderContext.getRequest());
        }
        return theme;
    }

    /**
     * @param templatesCacheLockTimeout the templatesCacheLockTimeout to set
     */
    public void setTemplatesCacheLockTimeout(long templatesCacheLockTimeout) {
        this.templatesCacheLockTimeout = templatesCacheLockTimeout;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (templatesCacheLockTimeout != 0) {
            logger.info("Will be using extra locking on write operations into cache " + RENDER_SERVICE_TEMPLATES_CACHE
                    + " with a timeout of {} ms", templatesCacheLockTimeout);
        } else {
            logger.info(
                    "No extra locking will be used on write operations into cache " + RENDER_SERVICE_TEMPLATES_CACHE);
        }
    }
}
