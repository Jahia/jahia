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

package org.jahia.services.render.filter;

import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.slf4j.Logger;
import org.jahia.services.content.*;
import org.jahia.services.render.*;

import javax.jcr.AccessDeniedException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.util.*;

/**
 * WrapperFilter
 * <p/>
 * Looks for all registered wrappers in the resource and calls the associated scripts around the output.
 * Output is made available to the wrapper script through the "wrappedContent" request attribute.
 */
public class TemplateNodeFilter extends AbstractFilter {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TemplateNodeFilter.class);

    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (renderContext.getRequest().getAttribute("skipWrapper") == null && !renderContext.isAjaxRequest()) {
            chain.pushAttribute(renderContext.getRequest(), "inWrapper", Boolean.TRUE);
            Template template = null;
            Template previousTemplate = null;
            if (renderContext.getRequest().getAttribute("templateSet") == null) {
                template = pushBodyWrappers(resource, renderContext);
                renderContext.getRequest().setAttribute("templateSet", Boolean.TRUE);
            } else {
                previousTemplate = (Template) renderContext.getRequest().getAttribute("previousTemplate");
                if (previousTemplate != null) {
                    template = previousTemplate.next;
                }
            }

            if (template != null) {
                try {
                    JCRNodeWrapper templateNode = resource.getNode().getSession().getNodeByIdentifier(template.node);
                    renderContext.getRequest().setAttribute("previousTemplate", template);
                    renderContext.getRequest().setAttribute("wrappedResource", resource);
                    Resource wrapperResource = new Resource(templateNode,
                            resource.getTemplateType(), template.view, Resource.CONFIGURATION_WRAPPER);
                    if (service.hasTemplate(templateNode, template.getView())) {

                        Integer currentLevel =
                                (Integer) renderContext.getRequest().getAttribute("org.jahia.modules.level");
                        if (currentLevel != null) {
                            renderContext.getRequest().removeAttribute("areaNodeTypesRestriction" + (currentLevel));
                        }

                        String output = RenderService.getInstance().render(wrapperResource, renderContext);

                        renderContext.getRequest().setAttribute("previousTemplate", previousTemplate);

                        return output;
                    } else {
                        logger.warn("Cannot get wrapper " + template);
                    }
                } catch (TemplateNotFoundException e) {
                    logger.debug("Cannot find wrapper " + template, e);
                } catch (RenderException e) {
                    logger.error("Cannot execute wrapper " + template, e);
                }
            }
        } else if (renderContext.isAjaxRequest() && resource.getContextConfiguration().equals(Resource.CONFIGURATION_PAGE)) {
            String i = renderContext.getRequest().getParameter("jarea");
            if (i != null) {
                JCRNodeWrapper area = resource.getNode().getSession().getNodeByUUID(i);
                Resource wrapperResource = new Resource(area, resource.getTemplateType(), null, Resource.CONFIGURATION_MODULE);
                String output = RenderService.getInstance().render(wrapperResource, renderContext);
                return output;
            }
        }
        chain.pushAttribute(renderContext.getRequest(), "inWrapper",
                (renderContext.isAjaxRequest()) ? Boolean.TRUE :
                        renderContext.getRequest().getAttribute("inArea") != null ? Boolean.TRUE : Boolean.FALSE);
        return null;
    }


    public Template pushBodyWrappers(Resource resource, RenderContext renderContext) throws Exception{
        final JCRNodeWrapper node = resource.getNode();
        String templateName = resource.getTemplate();
        if ("default".equals(templateName)) {
            templateName = null;
        }
        JCRNodeWrapper current = node;


        Template template = null;
        try {
            JCRNodeWrapper site;
            String jsite = renderContext.getRequest().getParameter("jsite");
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
                    template = new Template(parent.hasProperty("j:view") ? parent.getProperty("j:view").getString() :
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
                    template = new Template(templateNode.hasProperty("j:view") ? templateNode.getProperty("j:view").getString() :
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
                        template = new Template(templateNode.hasProperty("j:view") ? templateNode.getProperty("j:view").getString() :
                                null, templateNode.getIdentifier(), template);
                        templateNode = templateNode.getParent();
                    }
                } else {
                    throw new TemplateNotFoundException(resource.getTemplate());
                }
            }
        } catch (RepositoryException e) {
            logger.error("Cannot find template", e);
        }
        return template;
    }

    private Template addTemplates(Resource resource, RenderContext renderContext, JCRNodeWrapper templateNode) throws RepositoryException {
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
        Query q = templateNode.getSession().getWorkspace().getQueryManager().createQuery(query,
                Query.JCR_SQL2);
        QueryResult result = q.execute();
        NodeIterator ni = result.getNodes();

        SortedMap<String, Template> templates = new TreeMap<String, Template>(new Comparator<String>() {
            public int compare(String o1, String o2) {
                if (o1 == o2) {
                    return 0;
                }
                if (o1 == null) {
                    return 1;
                } else if (o2 == null) {
                    return -1;
                }
                try {
                    if (NodeTypeRegistry.getInstance().getNodeType(o1).isNodeType(o2)) {
                        return -1;
                    } else {
                        return 1;
                    }
                } catch (NoSuchNodeTypeException e) {
                    e.printStackTrace();
                    return 1;
                }
            }
        });
        while (ni.hasNext()) {
            final JCRNodeWrapper contentTemplateNode = (JCRNodeWrapper) ni.nextNode();
            addTemplate(resource, renderContext, contentTemplateNode, templates);
        }
        if (templates.isEmpty()) {
            return null;
        } else {
            return templates.get(templates.firstKey());
        }
    }

    private void addTemplate(Resource resource, RenderContext renderContext, JCRNodeWrapper templateNode, Map<String, Template> templates)
            throws RepositoryException {
        boolean ok = true;
        String type = null;
        if (templateNode.hasProperty("j:applyOn")) {
            ok = false;
            Value[] values = templateNode.getProperty("j:applyOn").getValues();
            for (Value value : values) {
                if (resource.getNode().isNodeType(value.getString())) {
                    type = value.getString();
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
            templates.put(type,new Template(
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
            Value[] values = templateNode.getProperty("j:requiredPermissions").getValues();
            for (Value value : values) {
                if (!resource.getNode().hasPermission(((JCRValueWrapperImpl) value).getNode().getName())) {
                    return false;
                }
            }

        }
        return true;
    }

}