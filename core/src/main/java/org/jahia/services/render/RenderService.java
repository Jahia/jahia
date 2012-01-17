/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.CacheProvider;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.render.filter.RenderServiceAware;
import org.jahia.services.render.filter.cache.DefaultCacheKeyGenerator;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.render.scripting.ScriptResolver;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import java.io.IOException;
import java.util.*;

/**
 * Service to render node
 *
 * @author toto
 */
public class RenderService {

    public static final String RENDER_SERVICE_TEMPLATES_CACHE = "RenderService.TemplatesCache";

    public void setCacheKeyGenerator(DefaultCacheKeyGenerator cacheKeyGenerator) {
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    @SuppressWarnings("unchecked")
    public void setCacheProvider(CacheProvider cacheProvider) {
        templatesCache = (CacheImplementation<String, Template>) cacheProvider.newCacheImplementation(RENDER_SERVICE_TEMPLATES_CACHE);
    }

    public static class RenderServiceBeanPostProcessor implements BeanPostProcessor {
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof RenderServiceAware) {
                ((RenderServiceAware) bean).setRenderService(getInstance());
            }
            if (bean instanceof RenderFilter) {
                logger.info("Registering render filter {}", bean.getClass().getName());
                getInstance().addFilter((RenderFilter) bean);
            }
            return bean;
        }
    }
    
    private static final Logger logger = LoggerFactory.getLogger(RenderService.class);

    private void addFilter(RenderFilter renderFilter) {
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

    private JahiaTemplateManagerService templateManagerService;

    private Collection<ScriptResolver> scriptResolvers;

    private List<RenderFilter> filters = new LinkedList<RenderFilter>();

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    public void setScriptResolvers(Collection<ScriptResolver> scriptResolvers) {
        this.scriptResolvers = scriptResolvers;
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
        if (context.getResourcesStack().contains(resource)) {
            return "loop";
        }

        String output = getRenderChainInstance().doFilter(context, resource);

        return output;
    }

    /**
     * This resolves the executable script from the resource object. This should be able to find the proper script
     * depending of the template / template type. Currently resolves only simple JSPScript.
     * <p/>
     * If template cannot be resolved, fall back on default template
     *
     * @param resource The resource to display
     * @param context
     * @return An executable script
     * @throws RepositoryException
     * @throws IOException
     */
    public Script resolveScript(Resource resource, RenderContext context) throws RepositoryException, TemplateNotFoundException {
        for (ScriptResolver scriptResolver : scriptResolvers) {
            Script s = scriptResolver.resolveScript(resource);
            if (s != null) {
                return s;
            }
        }
        return null;
    }


    public boolean hasView(JCRNodeWrapper node, String key) {
        try {
            if (hasView(node.getPrimaryNodeType(), key, node.getResolveSite())) {
                return true;
            }
            for (ExtendedNodeType type : node.getMixinNodeTypes()) {
                if (hasView(type, key, node.getResolveSite())) {
                    return true;
                }
            }
        } catch (RepositoryException e) {

        }
        return false;
    }

    public boolean hasView(ExtendedNodeType nt, String key, JCRSiteNode site) {
        for (ScriptResolver scriptResolver : scriptResolvers) {
            if (scriptResolver.hasView(nt, key, site)) {
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

    public SortedSet<View> getViewsSet(ExtendedNodeType nt, JCRSiteNode site) {
        SortedSet<View> set = new TreeSet<View>();
        for (ScriptResolver scriptResolver : scriptResolvers) {
            set.addAll(scriptResolver.getViewsSet(nt, site));
        }
        return set;
    }
    
    public org.jahia.services.render.Template resolveTemplate(Resource resource, RenderContext renderContext) throws AccessDeniedException {
        final JCRNodeWrapper node = resource.getNode();
        String templateName = resource.getTemplate();
        if ("default".equals(templateName)) {
            templateName = null;
        }
        JCRNodeWrapper current = node;


        org.jahia.services.render.Template template = null;
        try {
            JCRNodeWrapper site;
            String jsite = null;
            if (renderContext.getRequest() != null) {
                jsite = renderContext.getRequest().getParameter("jsite");
            }
            if (jsite == null && renderContext.getMainResource() != null) {
                jsite = (String) renderContext.getMainResource().getModuleParams().get("jsite");
            }
            if(jsite!=null) {
                site = node.getSession().getNodeByIdentifier(jsite);
                renderContext.setSite((JCRSiteNode) site);
            } else {
                site = renderContext.getSite();
            }
            if (site == null) {
                site = node.getResolveSite();
            }
            JCRNodeWrapper templatesNode = null;
            if (site != null) {
                try {
                    templatesNode = site.getNode("templates");
                } catch (PathNotFoundException e) {
                }
            }

            if (current.isNodeType("jnt:template")) {
                // Display a template node in studio
                if (!current.hasProperty("j:view") || "default".equals(current.getProperty("j:view").getString())) {
                    JCRNodeWrapper parent = current.getParent();
                    while (!(parent.isNodeType("jnt:templatesFolder"))) {
                        template = new org.jahia.services.render.Template(parent.hasProperty("j:view") ? parent.getProperty("j:view").getString() :
                                templateName, parent.getIdentifier(), template);
                        parent = parent.getParent();
                    }
                    template = addContextualTemplates(node, templateName, template, parent);
                }
            } else {
                if (resource.getTemplate().equals("default") && current.hasProperty("j:templateNode")) {
                    // A template node is specified on the current node
                    JCRNodeWrapper templateNode = (JCRNodeWrapper) current.getProperty("j:templateNode").getNode();
//                    if (renderContext.getSite() != null && !templateNode.getResolveSite().equals(renderContext.getSite())) {
//                        try {
//                            templateNode = templatesNode.getSession().getNode(templateNode.getPath().replace("/sites/"+templateNode.getResolveSite().getSiteKey(), "/sites/"+renderContext.getSite().getSiteKey()));
//                        } catch (PathNotFoundException e) {
//                            // Cannot switch site context, template not found
//                        }
//                    }
                    if (!checkTemplatePermission(resource, renderContext, templateNode)) {
                        throw new AccessDeniedException(resource.getTemplate());
                    }
                    template = new org.jahia.services.render.Template(templateNode.hasProperty("j:view") ? templateNode.getProperty("j:view").getString() :
                            templateName, templateNode.getIdentifier(), template);
                } else if (templatesNode != null) {
                    template = addTemplates(resource, renderContext, templatesNode);
                }

                if (template == null) {
                    template = addTemplates(resource, renderContext,
                            node.getSession().getNode(JCRContentUtils.getSystemSitePath() + "/templates"));
                }

                if (template != null) {
                    // Add cascade of parent templates
                    JCRNodeWrapper templateNode = resource.getNode().getSession().getNodeByIdentifier(template.getNode()).getParent();
                    while (!(templateNode.isNodeType("jnt:templatesFolder"))) {
                        template = new org.jahia.services.render.Template(templateNode.hasProperty("j:view") ? templateNode.getProperty("j:view").getString() :
                                null, templateNode.getIdentifier(), template);
                        templateNode = templateNode.getParent();
                    }
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

                if ("default".equals(template.getView())) {
                    JCRNodeWrapper parent = node.getSession().getNodeByUUID(template.getNode()).getParent();
                    template = addContextualTemplates(node, templateName, template, parent);
                }
            } else {
                template = new Template(null,null,null);
            }

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RepositoryException e) {
            logger.error("Cannot find template", e);
        }
        return template;
    }

    private Template addContextualTemplates(JCRNodeWrapper node, String templateName, Template template, JCRNodeWrapper parent) throws RepositoryException {
        if (parent.isNodeType("jnt:templatesFolder") && parent.hasProperty("j:rootTemplatePath")) {
            String rootTemplatePath = parent.getProperty("j:rootTemplatePath").getString();
            JCRNodeWrapper systemSite = node.getSession().getNode(JCRContentUtils.getSystemSitePath());
            parent = systemSite.getNode("templates"+ rootTemplatePath);
            while (!(parent.isNodeType("jnt:templatesFolder"))) {
                template = new Template(parent.hasProperty("j:view") ? parent.getProperty("j:view").getString() :
                        templateName, parent.getIdentifier(), template);
                parent = parent.getParent();
            }
        }
        return template;
    }

    private CacheImplementation<String,Template> templatesCache;

    private DefaultCacheKeyGenerator cacheKeyGenerator;

    private org.jahia.services.render.Template addTemplates(Resource resource, RenderContext renderContext,
                                                            JCRNodeWrapper templateNode) throws RepositoryException {
        String type = "contentTemplate";
        if (resource.getNode().isNodeType("jnt:page")) {
            type = "pageTemplate";
        }
        boolean isNotDefaultTemplate = resource.getTemplate() != null && !resource.getTemplate().equals("default");

        String key = new StringBuffer(templateNode.getPath()).append(type).append(
                isNotDefaultTemplate ? resource.getTemplate() : "default").toString() + renderContext.getServletPath() + resource.getWorkspace() + renderContext.isLoggedIn() +
                     resource.getNode().getPrimaryNodeTypeName()+cacheKeyGenerator.appendAcls(resource, renderContext, false);

        Template template = templatesCache.get(key);

        if (template == null) {
            String query =
                    "select * from [jnt:" + type + "] as w where isdescendantnode(w, ['" + templateNode.getPath() +
                    "'])";
            if (isNotDefaultTemplate) {
                query += " and name(w)='" + resource.getTemplate() + "'";
            } else {
                query += " and [j:defaultTemplate]=true";
            }
            query += " order by [j:priority] desc";
            Query q = templateNode.getSession().getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
            QueryResult result = q.execute();
            NodeIterator ni = result.getNodes();

            List<Template> templates = new ArrayList<Template>();
            while (ni.hasNext()) {
                final JCRNodeWrapper contentTemplateNode = (JCRNodeWrapper) ni.nextNode();
                addTemplate(resource, renderContext, contentTemplateNode, templates);
            }
            if (templates.isEmpty()) {
                templatesCache.put(key, null, new EmptyTemplate(null,null,null));
                return null;
            } else {
                templatesCache.put(key,null, templates.get(0));
                return templates.get(0);
            }
        } else {
            return template.getClass().getName().equals(EmptyTemplate.class.getName()) ? null : template;
        }
    }

    private void addTemplate(Resource resource, RenderContext renderContext, JCRNodeWrapper templateNode, List<Template> templates)
            throws RepositoryException {
        boolean ok = true;
        if (templateNode.hasProperty("j:applyOn")) {
            ok = false;
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
        }
        if (!checkTemplatePermission(resource, renderContext, templateNode)) return;

        if (ok) {
            templates.add(new Template(
                    templateNode.hasProperty("j:view") ? templateNode.getProperty("j:view").getString() :
                            null, templateNode.getIdentifier(), null));
        }
    }

    private boolean checkTemplatePermission(Resource resource, RenderContext renderContext, JCRNodeWrapper templateNode) throws RepositoryException {
        if (templateNode.hasProperty("j:requiredMode")) {
            String req = templateNode.getProperty("j:requiredMode").getString();
            if (!renderContext.isContributionMode() && req.equals("contribute")) {
                return false;
            } else if (!renderContext.isEditMode() && req.equals("edit")) {
                return false;
            } else if (!renderContext.isLiveMode() && req.equals("live")) {
                return false;
            } else if (!renderContext.isPreviewMode() && !renderContext.isLiveMode() && req.equals("live-or-preview")) {
                return false;
            }

        }
        if (templateNode.hasProperty("j:requiredPermissions")) {
            final Value[] values = templateNode.getProperty("j:requiredPermissions").getValues();
            List<String> perms = JCRTemplate.getInstance().doExecuteWithSystemSession(null, new JCRCallback<List<String>>() {
                public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    List<String> permissionNames = new ArrayList<String>();
                    for (Value value : values) {
                        permissionNames.add(session.getNodeByUUID(value.getString()).getName());
                    }
                    return permissionNames;
                }
            });
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
                    return false;
                }
            }
        }
        if (templateNode.hasProperty("j:requireLoggedUser") && templateNode.getProperty("j:requireLoggedUser").getBoolean()) {
            if (!renderContext.isLoggedIn()) {
                return false;
            }
        }
        if (templateNode.hasProperty("j:requirePrivilegedUser") && templateNode.getProperty("j:requirePrivilegedUser").getBoolean()) {
            if (!renderContext.getUser().isMemberOfGroup(0,JahiaGroupManagerService.PRIVILEGED_GROUPNAME)) {
                return false;
            }
        }
        return true;
    }
    
    public void flushCache() {
        templatesCache.flushAll(true);
    }

}
