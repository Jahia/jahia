/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.template.include;

import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.ParamParent;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.*;
import org.jahia.services.render.scripting.Script;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * Handler for the &lt;template:module/&gt; tag, used to render content objects.
 * User: toto
 * Date: May 14, 2009
 * Time: 7:18:15 PM
 */
public class ModuleTag extends BodyTagSupport implements ParamParent {

    private static final long serialVersionUID = -8968618483176483281L;

    private static Logger logger = Logger.getLogger(ModuleTag.class);

    protected String path;

    protected JCRNodeWrapper node;

    protected String nodeName;

    protected String template;

    protected String templateType = null;

    protected boolean editable = true;

    protected String nodeTypes;

    protected String forcedTemplate = null;

    protected String templateWrapper = null;

    protected String var = null;

    protected StringBuffer buffer = new StringBuffer();

    protected Map<String, String> parameters = new HashMap<String, String>();

    public String getPath() {
        return path;
    }

    public String getNodeName() {
        return nodeName;
    }

    public JCRNodeWrapper getNode() {
        return node;
    }

    public String getTemplate() {
        return template;
    }

    public String getTemplateType() {
        return templateType;
    }

    public boolean isEditable() {
        return editable;
    }

    public String getForcedTemplate() {
        return forcedTemplate;
    }

    public String getTemplateWrapper() {
        return templateWrapper;
    }

    public String getVar() {
        return var;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setNodeName(String node) {
        this.nodeName = node;
    }

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public void setNodeTypes(String nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setForcedTemplate(String forcedTemplate) {
        this.forcedTemplate = forcedTemplate;
    }

    public void setTemplateWrapper(String templateWrapper) {
        this.templateWrapper = templateWrapper;
    }

    public void setVar(String var) {
        this.var = var;
    }

    @Override
    public int doStartTag() throws JspException {
        Integer level = (Integer) pageContext.getAttribute("org.jahia.modules.level", PageContext.REQUEST_SCOPE);
        pageContext.setAttribute("org.jahia.modules.level", level != null ? level + 1 : 2, PageContext.REQUEST_SCOPE);
        return super.doStartTag();
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
            if (renderContext == null) {
                // final JahiaData jData = (JahiaData) pageContext.getRequest().getAttribute("org.jahia.data.JahiaData");
                // renderContext = new RenderContext((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse(), jData.getProcessingContext().getUser());
                // renderContext.setSite(jData.getProcessingContext().getSite());
            }
//            if(templateWrapper != null) {
//                renderContext.setTemplateWrapper(templateWrapper);
//            }
            // add custom parameters
//            Map<String, Object> oldParams = new HashMap<String, Object>(renderContext.getModuleParams());
//            renderContext.getModuleParams().clear();

            buffer = new StringBuffer();

            Resource currentResource = (Resource) pageContext.getAttribute("currentResource", PageContext.REQUEST_SCOPE);

            if (nodeName != null) {
                node = (JCRNodeWrapper) pageContext.findAttribute(nodeName);
            } else if (path != null && currentResource != null) {
                try {
                    if (!path.startsWith("/")) {
                        JCRNodeWrapper nodeWrapper = currentResource.getNode();
                        if (!path.equals("*") && nodeWrapper.hasNode(path)) {
                            node = (JCRNodeWrapper) nodeWrapper.getNode(path);
                        } else {
                            missingResource(renderContext, currentResource);
                        }
                    } else if (path.startsWith("/")) {
                        JCRSessionWrapper session = currentResource.getNode().getSession();
                        try {
                            node = (JCRNodeWrapper) session.getItem(path);
                        } catch (PathNotFoundException e) {
                            missingResource(renderContext, currentResource);
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (node != null) {
                Integer currentLevel = (Integer) pageContext.getAttribute("org.jahia.modules.level", PageContext.REQUEST_SCOPE);
                String constrainedNodeTypes = (String) pageContext.getAttribute("areaNodeTypesRestriction" + (currentLevel - 1), PageContext.REQUEST_SCOPE);

                if (constrainedNodeTypes != null && !"".equals(constrainedNodeTypes.trim())) {
                    StringTokenizer st = new StringTokenizer(constrainedNodeTypes, " ");
                    boolean found = false;
                    Node displayedNode = node;
                    try {
                        if (node.isNodeType("jnt:nodeReference") && node.hasProperty("j:node")) {
                            displayedNode = node.getProperty("j:node").getNode();
                        }
                        while (st.hasMoreTokens()) {
                            String tok = st.nextToken();
                            try {
                                if (displayedNode.isNodeType(tok)) {
                                    found = true;
                                    break;
                                }
                            } catch (RepositoryException e) {
                                logger.error("Cannot test on " + tok, e);
                            }
                        }
                    } catch (RepositoryException e) {
                        logger.error(e, e);
                    }
                    if (!found) {
                        return EVAL_PAGE;
                    }
                }

                if (templateType == null) {
                    templateType = currentResource.getTemplateType();
                }

                Resource resource = new Resource(node, templateType, template, forcedTemplate);

                String charset = pageContext.getResponse().getCharacterEncoding();
                for (Map.Entry<String, String> param : parameters.entrySet()) {
                    resource.getModuleParams().put(URLDecoder.decode(param.getKey(), charset), URLDecoder.decode(param.getValue(), charset));
                }

                if (parameters.containsKey("resourceNodeType")) {
                    try {
                        resource.setResourceNodeType(NodeTypeRegistry.getInstance().getNodeType(URLDecoder.decode(parameters.get("resourceNodeType"), "UTF-8")));
                    } catch (NoSuchNodeTypeException e) {
                        throw new JspException(e);
                    }
                }

                try {
                    boolean templateLocked = resource.getNode().isNodeType("jmix:templateLocked");
                    boolean isTemplateMode = pageContext.getAttribute("isTemplate", PageContext.REQUEST_SCOPE) != null && (Boolean) pageContext.getAttribute("isTemplate", PageContext.REQUEST_SCOPE);

                    if (renderContext.isEditMode() && editable && (isTemplateMode || !templateLocked)) {
                        String type = getModuleType();

                        Script script = null;
                        try {
                            script = RenderService.getInstance().resolveScript(resource, renderContext);
                            printModuleStart(type, templateLocked, node.getPath(), resource.getResolvedTemplate(), script.getTemplate().getInfo());
                        } catch (TemplateNotFoundException e) {
                            printModuleStart(type, templateLocked, node.getPath(), resource.getResolvedTemplate(), "Script not found");
                        }

                        render(renderContext, resource);

                        printModuleEnd();
                    } else {
                        render(renderContext, resource);
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (IOException ex) {
            throw new JspException(ex);
        } finally {
            if (var != null) {
                pageContext.setAttribute(var, buffer);
            }
            path = null;
            node = null;
            template = null;
            forcedTemplate = null;
            templateWrapper = null;
            editable = true;
            templateType = null;
            nodeTypes = null;
            var = null;
            buffer = null;

            if (!"true".equals(parameters.get("isInclude"))) {
                Integer level = (Integer) pageContext.getAttribute("org.jahia.modules.level", PageContext.REQUEST_SCOPE);
                pageContext.setAttribute("org.jahia.modules.level", level != null ? level - 1 : 1, PageContext.REQUEST_SCOPE);
            }

            parameters.clear();

        }
        return EVAL_PAGE;
    }

    protected void printModuleStart(String type, boolean templateLocked, String path, String resolvedTemplate, String scriptInfo) throws IOException {
        Integer currentLevel = (Integer) pageContext.getAttribute("org.jahia.modules.level", PageContext.REQUEST_SCOPE);
        String constrainedNodeTypes = (String) pageContext.getAttribute("areaNodeTypesRestriction" + (currentLevel - 1), PageContext.REQUEST_SCOPE);

        buffer.append("<div class=\"jahia-template-gxt\" jahiatype=\"module\" ")
                .append("id=\"module")
                .append(UUID.randomUUID().toString())
                .append("\" type=\"")
                .append(type)
                .append("\" ")
                .append((templateLocked) ? " templateLocked=\"true\"" : "")
                .append((scriptInfo != null) ? " scriptInfo=\"" + scriptInfo + "\"" : "")
                .append("path=\"").append(path)
                .append("\" ")
                .append((constrainedNodeTypes != null) ? "nodetypes=\"" + constrainedNodeTypes + "\"" : "")
                .append((resolvedTemplate != null) ? " template=\"" + resolvedTemplate + "\"" : "")
                .append(">");
        if (var == null) {
            pageContext.getOut().print(buffer);
            buffer.delete(0, buffer.length());
        }

    }

    protected void printModuleEnd() throws IOException {
        buffer.append("</div>");
        if (var == null) {
            pageContext.getOut().print(buffer);
            buffer.delete(0, buffer.length());
        }
    }

    protected void render(RenderContext renderContext, Resource resource) throws IOException {
        try {
            final Integer level = (Integer) pageContext.getAttribute("org.jahia.modules.level", PageContext.REQUEST_SCOPE);
            if (nodeTypes != null) {
                pageContext.setAttribute("areaNodeTypesRestriction" + level, nodeTypes, PageContext.REQUEST_SCOPE);
            }
            if (renderContext.isIncludeSubModules()) {
                buffer.append(RenderService.getInstance().render(resource, renderContext));
                if (var == null) {
                    pageContext.getOut().print(buffer);
                    buffer.delete(0, buffer.length());
                }
            }
            if (nodeTypes != null) {
                pageContext.removeAttribute("areaNodeTypesRestriction" + level, PageContext.REQUEST_SCOPE);
            }
        } catch (TemplateNotFoundException io) {
            buffer.append(io);
            if (var == null) {
                pageContext.getOut().print(buffer);
                buffer.delete(0, buffer.length());
            }
        } catch (RenderException e) {
            logger.error(e.getMessage(), e);
        }

    }

    protected String getModuleType() throws RepositoryException {
        String type = "existingNode";
        if (node.isNodeType("jnt:contentList") || node.isNodeType("jnt:containerList")) {
            type = "list";
        }
        return type;
    }

    protected void missingResource(RenderContext renderContext, Resource currentResource) throws RepositoryException, IOException {
        String currentPath = currentResource.getNode().getPath();
        if (path.startsWith(currentPath + "/") && path.substring(currentPath.length() + 1).indexOf('/') == -1) {
            currentResource.getMissingResources().add(path.substring(currentPath.length() + 1));
        } else if (!path.startsWith("/")) {
            currentResource.getMissingResources().add(path);
        }

        if (renderContext.isEditMode()) {
            boolean templateLocked = currentResource.getNode().isNodeType("jmix:templateLocked");
            if (!templateLocked) {
                printModuleStart("placeholder", templateLocked, path, null, null);
                printModuleEnd();
            }
        }
    }

    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

}
