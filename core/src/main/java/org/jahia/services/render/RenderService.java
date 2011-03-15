/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.render.filter.RenderServiceAware;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.render.scripting.ScriptResolver;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.jcr.AccessDeniedException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
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

    public static class RenderServiceBeanPostProcessor implements BeanPostProcessor {
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof RenderServiceAware) {
                ((RenderServiceAware) bean).setRenderService(getInstance());
            }
            if(bean instanceof RenderFilter) {
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

        RenderChain renderChain = new RenderChain();
        renderChain.addFilters(filters);
        renderChain.addFilters(templateManagerService.getRenderFilters());
        String output = renderChain.doFilter(context, resource);

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
            Script s = scriptResolver.resolveScript(resource, context);
            if (s != null) {
                return s;
            }
        }
        return null;
    }


    public boolean hasView(JCRNodeWrapper node, String key) {
        try {
            if (hasView(node.getPrimaryNodeType(), key)) {
                return true;
            }
            for (ExtendedNodeType type : node.getMixinNodeTypes()) {
                if (hasView(type, key)) {
                    return true;
                }
            }
        } catch (RepositoryException e) {

        }
        return false;
    }
    public boolean hasView(ExtendedNodeType nt, String key) {
        for (ScriptResolver scriptResolver : scriptResolvers) {
            if (scriptResolver.hasView(nt, key)) {
                return true;
            }
        }
        return false;
    }

    public SortedSet<View> getAllViewsSet() {
        SortedSet<View> set = new TreeSet<View>();
        for (ScriptResolver scriptResolver : scriptResolvers) {
            set.addAll(scriptResolver.getAllViewsSet());
        }
        return set;
    }

    public SortedSet<View> getViewsSet(ExtendedNodeType nt) {
        SortedSet<View> set = new TreeSet<View>();
        for (ScriptResolver scriptResolver : scriptResolvers) {
            set.addAll(scriptResolver.getViewsSet(nt));
        }
        return set;
    }
    
    public org.jahia.services.render.Template resolveTemplate(Resource resource, RenderContext renderContext) throws Exception{
        final JCRNodeWrapper node = resource.getNode();
        String templateName = resource.getTemplate();
        if ("default".equals(templateName)) {
            templateName = null;
        }
        JCRNodeWrapper current = node;


        org.jahia.services.render.Template template = null;
        try {
            JCRNodeWrapper site;
            String jsite = renderContext.getRequest().getParameter("jsite");
            if (jsite == null) {
                jsite = (String) renderContext.getMainResource().getModuleParams().get("jsite");
            }
            if(jsite!=null) {
                site = node.getSession().getNodeByIdentifier(jsite);
                renderContext.setSite((JCRSiteNode) site);
            } else {
                site = node.getResolveSite();
            }
            JCRNodeWrapper templatesNode = null;
            if (site != null && site.hasNode("templates")) {
                templatesNode = site.getNode("templates");
            }

            if (current.isNodeType("jnt:template")) {
                // Display a template node in studio
                JCRNodeWrapper parent = current.getParent();
                while (!(parent.isNodeType("jnt:templatesFolder"))) {
                    template = new org.jahia.services.render.Template(parent.hasProperty("j:view") ? parent.getProperty("j:view").getString() :
                            templateName, parent.getIdentifier(), template);
                    parent = parent.getParent();
                }
            } else {
                if (resource.getTemplate().equals("default") && current.hasProperty("j:templateNode")) {
                    // A template node is specified on the current node
                    JCRNodeWrapper templateNode = (JCRNodeWrapper) current.getProperty("j:templateNode").getNode();
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
                    throw new TemplateNotFoundException(resource.getTemplate());
                }
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RepositoryException e) {
            logger.error("Cannot find template", e);
        }
        return template;
    }

    private org.jahia.services.render.Template addTemplates(Resource resource, RenderContext renderContext, JCRNodeWrapper templateNode) throws RepositoryException {
        String type = "contentTemplate";
        if (resource.getNode().isNodeType("jnt:page")) {
            type = "pageTemplate";
        }

        String query =
                "select * from [jnt:" + type + "] as w where isdescendantnode(w, ['" + templateNode.getPath() + "'])";
        if (resource.getTemplate() != null && !resource.getTemplate().equals("default")) {
            query += " and name(w)='"+ resource.getTemplate()+"'";
        } else {
            query += " and [j:defaultTemplate]=true";
        }
        query += " order by [j:priority] desc";
        Query q = templateNode.getSession().getWorkspace().getQueryManager().createQuery(query,
                Query.JCR_SQL2);
        QueryResult result = q.execute();
        NodeIterator ni = result.getNodes();

        List<Template> templates = new ArrayList<Template>();
//        SortedMap<String, View> templates = new TreeMap<String, View>(new Comparator<String>() {
//            public int compare(String o1, String o2) {
//                if (o1 == o2) {
//                    return 0;
//                }
//                if (o1 == null) {
//                    return 1;
//                } else if (o2 == null) {
//                    return -1;
//                }
//                try {
//                    if (NodeTypeRegistry.getInstance().getNodeType(o1).isNodeType(o2)) {
//                        return -1;
//                    } else {
//                        return 1;
//                    }
//                } catch (NoSuchNodeTypeException e) {
//                    e.printStackTrace();
//                    return 1;
//                }
//            }
//        });
        while (ni.hasNext()) {
            final JCRNodeWrapper contentTemplateNode = (JCRNodeWrapper) ni.nextNode();
            addTemplate(resource, renderContext, contentTemplateNode, templates);
        }
        if (templates.isEmpty()) {
            return null;
        } else {
            return templates.get(0);
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
            if (renderContext.isContributionMode() && !req.equals("contribute")) {
                return false;
            } else if (!renderContext.isContributionMode() && !req.equals("live")) {
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
            for (String perm : perms) {
                if (!resource.getNode().hasPermission(perm)) {
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
            if (!renderContext.getUser().isMemberOfGroup(0,"privileged")) {
                return false;
            }
        }
        return true;
    }
    

}
