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

package org.jahia.taglibs.template.include;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.apache.taglibs.standard.tag.common.core.ParamParent;
import org.jahia.bin.Studio;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.*;
import org.jahia.services.render.scripting.Script;

import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Handler for the &lt;template:module/&gt; tag, used to render content objects.
 * User: toto
 * Date: May 14, 2009
 * Time: 7:18:15 PM
 */
public class ModuleTag extends BodyTagSupport implements ParamParent {

    private static final long serialVersionUID = -8968618483176483281L;

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ModuleTag.class);

    protected String path;

    protected JCRNodeWrapper node;

    protected String nodeName;

    protected String view;

    protected String templateType = null;

    protected boolean editable = true;

    protected String nodeTypes = null;

    protected int listLimit = -1;

    protected String constraints = null;

    protected String var = null;

    protected StringBuffer buffer = new StringBuffer();

    protected Map<String, String> parameters = new HashMap<String, String>();

    protected boolean checkConstraints = true;

    public String getPath() {
        return path;
    }

    public String getNodeName() {
        return nodeName;
    }

    public JCRNodeWrapper getNode() {
        return node;
    }

    public String getView() {
        return view;
    }

    public String getTemplateType() {
        return templateType;
    }

    public boolean isEditable() {
        return editable;
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

    public void setView(String view) {
        this.view = view;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public void setNodeTypes(String nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public void setListLimit(int listLimit) {
        this.listLimit = listLimit;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
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
            RenderContext renderContext =
                    (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
//            if (true)
//            throw new RuntimeException();
            buffer = new StringBuffer();

            Resource currentResource =
                    (Resource) pageContext.getAttribute("currentResource", PageContext.REQUEST_SCOPE);

            findNode(renderContext, currentResource);

            String resourceNodeType = null;
            if (parameters.containsKey("resourceNodeType")) {
                resourceNodeType = URLDecoder.decode(parameters.get("resourceNodeType"), "UTF-8");
            }

            if (node != null) {
                Integer currentLevel =
                        (Integer) pageContext.getAttribute("org.jahia.modules.level", PageContext.REQUEST_SCOPE);
                try {
                    constraints = ConstraintsHelper.getConstraints(node);
                } catch (RepositoryException e) {
                    logger.error("Error when getting list constraints", e);
                }
                if (checkConstraints && (path == null || path.equals("*"))) {
                    String constrainedNodeTypes = null;
                    if (currentLevel != null) {
                        constrainedNodeTypes = (String) pageContext.getAttribute(
                                "areaNodeTypesRestriction" + (currentLevel - 1), PageContext.REQUEST_SCOPE);
                    }
                    try {
                        if (constrainedNodeTypes != null && !"".equals(constrainedNodeTypes.trim())) {
                            StringTokenizer st = new StringTokenizer(constrainedNodeTypes, " ");
                            boolean found = false;
                            Node displayedNode = node;
                            if (node.isNodeType("jnt:contentReference") && node.hasProperty("j:node")) {
                                try {
                                    displayedNode = node.getProperty("j:node").getNode();
                                } catch (ItemNotFoundException e) {
                                    return EVAL_PAGE;
                                }
                            }
                            while (st.hasMoreTokens()) {
                                String tok = st.nextToken();
                                if (displayedNode.isNodeType(tok) || tok.equals(resourceNodeType)) {
                                    found = true;
                                    break;
                                }
                            }
                            // Remove test until we find a better solution to avoid displaying unecessary nodes
                            if (!found) {
                                return EVAL_PAGE;
                            }
                        }
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (templateType == null) {
                    templateType = currentResource.getTemplateType();
                }

                Resource resource = new Resource(node, templateType, view, getConfiguration());

                String charset = pageContext.getResponse().getCharacterEncoding();
                for (Map.Entry<String, String> param : parameters.entrySet()) {
                    resource.getModuleParams().put(URLDecoder.decode(param.getKey(), charset),
                            URLDecoder.decode(param.getValue(), charset));
                }

                if (resourceNodeType != null) {
                    try {
                        resource.setResourceNodeType(NodeTypeRegistry.getInstance().getNodeType(resourceNodeType));
                    } catch (NoSuchNodeTypeException e) {
                        throw new JspException(e);
                    }
                }

                try {
                    final boolean canEdit = canEdit(renderContext);
                    pageContext.getRequest().setAttribute("editableModule", canEdit);
                    if (canEdit) {
                        String type = getModuleType(renderContext);

                        Script script = null;
                        try {
                            script = RenderService.getInstance().resolveScript(resource, renderContext);
                            printModuleStart(type, node.getPath(), resource.getResolvedTemplate(),
                                    script.getView().getInfo(), null);
                        } catch (TemplateNotFoundException e) {
                            printModuleStart(type, node.getPath(), resource.getResolvedTemplate(), "Script not found",
                                    null);
                        }

                        currentResource.getDependencies().add(node.getCanonicalPath());
                        render(renderContext, resource);
                        //Copy dependencies to parent Resource (only for include of the same node)
                        currentResource.getRegexpDependencies().addAll(resource.getRegexpDependencies());
                        currentResource.getDependencies().addAll(resource.getDependencies());
                        printModuleEnd();
                    } else {
                        resource.getModuleParams().put("readOnly", Boolean.TRUE);
                        currentResource.getDependencies().add(node.getCanonicalPath());
                        render(renderContext, resource);
                        //Copy dependencies to parent Resource (only for include of the same node)
                        currentResource.getRegexpDependencies().addAll(resource.getRegexpDependencies());
                        currentResource.getDependencies().addAll(resource.getDependencies());
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
            view = null;
            editable = true;
            templateType = null;
            nodeTypes = null;
            listLimit = -1;
            constraints = null;
            var = null;
            buffer = null;

            if (!(this instanceof IncludeTag)) {
                Integer level =
                        (Integer) pageContext.getAttribute("org.jahia.modules.level", PageContext.REQUEST_SCOPE);
                pageContext.setAttribute("org.jahia.modules.level", level != null ? level - 1 : 1,
                        PageContext.REQUEST_SCOPE);
            }

            parameters.clear();

        }
        return EVAL_PAGE;
    }

    protected void findNode(RenderContext renderContext, Resource currentResource) throws IOException {
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
    }

    protected String getConfiguration() {
        return Resource.CONFIGURATION_MODULE;
    }

    protected boolean canEdit(RenderContext renderContext) {
        return renderContext.isEditMode() && editable &&
                !Boolean.TRUE.equals(renderContext.getRequest().getAttribute("inWrapper")) &&
                renderContext.getRequest().getAttribute("inArea") == null ;
    }

    protected void printModuleStart(String type, String path, String resolvedTemplate, String scriptInfo,
                                    String additionalParameters)
            throws RepositoryException, IOException {

        buffer.append("<div class=\"jahia-template-gxt\" jahiatype=\"module\" ").append("id=\"module")
                .append(UUID.randomUUID().toString()).append("\" type=\"").append(type).append("\"");

        buffer.append((scriptInfo != null) ? " scriptInfo=\"" + scriptInfo + "\"" : "").append(" path=\"").append(path)
                .append("\"");

        if (!StringUtils.isEmpty(nodeTypes)) {
            buffer.append(" nodetypes=\"" + nodeTypes + "\"");
        } else if (!StringUtils.isEmpty(constraints)) {
            buffer.append(" nodetypes=\"" + constraints + "\"");
        }

        if (listLimit > -1) {
            buffer.append(" listlimit=\"" + listLimit + "\"");
        }

        if (!StringUtils.isEmpty(constraints)) {
            String referenceTypes = ConstraintsHelper.getReferenceTypes(constraints, nodeTypes);
            buffer.append((!StringUtils.isEmpty(referenceTypes)) ? " referenceTypes=\"" + referenceTypes + "\"" : " referenceTypes=\"none\"");
        }

        if (additionalParameters != null) {
            buffer.append(" ").append(additionalParameters);
        }

        buffer.append((resolvedTemplate != null) ? " template=\"" + resolvedTemplate + "\"" : "").append(">");

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
//        try {
//
//            if (!(this instanceof IncludeTag) && (resource.getNode().isLocked() || !resource.getNode().getLockedLocales().isEmpty())) {
//            Node node = resource.getNode().getRealNode();
//
//            String out = node.getPath() + " shared : ";
//            if (node.hasProperty("j:lockTypes")) {
//                for (Value value : node.getProperty("j:lockTypes").getValues()) {
//                    out += "<div style=\"color:red\" >" + value.getString() + "</div>";
//                }
//            }
//            NodeIterator ni = node.getNodes("j:translation*");
//            while (ni.hasNext()) {
//                Node n = ni.nextNode();
//                out += ", " + n.getProperty("jcr:language").getString() + " : ";
//                if (n.isLocked()) {
//                    if (n.hasProperty("j:lockTypes")) {
//                        for (Value value : n.getProperty("j:lockTypes").getValues()) {
//                            out += "<div style=\"color:red\" >" + value.getString() + "</div>";
//                        }
//                    }
//                }
//            }
//
//            pageContext.getOut().print("<div>"+out+"</div>");
//            }
//
//        } catch (RepositoryException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }


        try {
            final Integer level =
                    (Integer) pageContext.getAttribute("org.jahia.modules.level", PageContext.REQUEST_SCOPE);

            String restriction = null;
            if (!StringUtils.isEmpty(nodeTypes)) {
                restriction = nodeTypes;
            } else if (!StringUtils.isEmpty(constraints)) {
                restriction = constraints;
            }

            boolean setRestrictions =
                    pageContext.getAttribute("areaNodeTypesRestriction" + level, PageContext.REQUEST_SCOPE) == null &&
                            !StringUtils.isEmpty(restriction);
            if (setRestrictions) {
                pageContext.setAttribute("areaNodeTypesRestriction" + level, restriction, PageContext.REQUEST_SCOPE);
            }

            buffer.append(RenderService.getInstance().render(resource, renderContext));
            if (var == null) {
                pageContext.getOut().print(buffer);
                buffer.delete(0, buffer.length());
            }
            if (setRestrictions) {
                pageContext.removeAttribute("areaNodeTypesRestriction" + level, PageContext.REQUEST_SCOPE);
            }
        } catch (TemplateNotFoundException io) {
            buffer.append(io);
            if (var == null) {
                pageContext.getOut().print(buffer);
                buffer.delete(0, buffer.length());
            }
        } catch (RenderException e) {
            if (!(e.getCause() instanceof AccessDeniedException)) {
                logger.error(e.getMessage(), e);
            }
            if(renderContext.isEditMode() && ((e.getCause() instanceof TemplateNotFoundException) || (e.getCause() instanceof AccessDeniedException))){
                buffer.append(e.getCause().getMessage());
                if (var == null) {
                    pageContext.getOut().print(buffer);
                    buffer.delete(0, buffer.length());
                }
            }
        }

    }

    protected String getModuleType(RenderContext renderContext) throws RepositoryException {
        String type = "existingNode";

        if (node.isNodeType("jmix:listContent")) {
            type = "list";
        } else if (node.isNodeType("jmix:bindedComponent")) {
            type = "bindedComponent";
        } else if (renderContext.getEditModeConfigName().equals(Studio.STUDIO_MODE) && !node.isNodeType("jmix:layoutComponentContent")) {
            type = "existingNodeWithHeader";
        }
        return type;
    }

    protected void missingResource(RenderContext renderContext, Resource currentResource)
            throws RepositoryException, IOException {
        String currentPath = currentResource.getNode().getPath();
        if (path.startsWith(currentPath + "/") && path.substring(currentPath.length() + 1).indexOf('/') == -1) {
            currentResource.getMissingResources().add(path.substring(currentPath.length() + 1));
        } else if (!path.startsWith("/")) {
            currentResource.getMissingResources().add(path);
        }

        if (!"*".equals(path) && (path.indexOf("/") == -1)) {
            // we have a named path that is missing, let's see if we can figure out it's node type.
            constraints = ConstraintsHelper.getConstraints(currentResource.getNode(), path);
        }


        if (canEdit(renderContext)) {
            if (currentResource.getNode().hasPermission("jcr:addChildNodes")) {
                printModuleStart("placeholder", path, null, null, null);
                printModuleEnd();
            }
        }
    }

    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

    public void setCheckConstraints(boolean checkConstraints) {
        this.checkConstraints = checkConstraints;
    }
}
