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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.ParamParent;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.CategoryBean;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.jahia.services.render.*;
import org.jahia.services.render.scripting.Script;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;

/**
 * Handler for the &lt;template:module/&gt; tag, used to render content objects.
 * User: toto
 * Date: May 14, 2009
 * Time: 7:18:15 PM
 */
public class AreaTag extends BodyTagSupport implements ParamParent {

    private static final long serialVersionUID = -8968618483176483281L;

    private static Logger logger = Logger.getLogger(AreaTag.class);

    private String path;

    private String template;

    private String templateType = "html";

    private boolean editable = true;

    private String nodeTypes;

    private String areaType = "jnt:contentList";

    private String forcedTemplate = null;

    private String var = null;

    private StringBuffer buffer = new StringBuffer();

    private Map<String, String> parameters = new HashMap<String, String>();

    private boolean forceCreation = false;

    public void setPath(String path) {
        this.path = path;
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

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setForcedTemplate(String forcedTemplate) {
        this.forcedTemplate = forcedTemplate;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setForceCreation(boolean forceCreation) {
        this.forceCreation = forceCreation;
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
            // add custom parameters
            Map<String, Object> oldParams = new HashMap<String, Object>(renderContext.getModuleParams());
            renderContext.getModuleParams().clear();

            buffer = new StringBuffer();

            String charset = pageContext.getResponse().getCharacterEncoding();
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                renderContext.getModuleParams().put(URLDecoder.decode(param.getKey(), charset), URLDecoder.decode(param.getValue(), charset));
            }

            Resource currentResource = (Resource) pageContext.getAttribute("currentResource", PageContext.REQUEST_SCOPE);
            if (currentResource != null) {
                templateType = currentResource.getTemplateType();
            }
            JCRNodeWrapper node = null;

            if (!path.startsWith("/")) {
                JCRNodeWrapper nodeWrapper = currentResource.getNode();
                if (!path.equals("*") && nodeWrapper.hasNode(path)) {
                    node = nodeWrapper.getNode(path);
                } else if (!path.equals("*") && (forceCreation || renderContext.isEditMode())) {
                    if(!nodeWrapper.isCheckedOut())
                        nodeWrapper.checkout();
                    node = nodeWrapper.addNode(path, areaType);
                    currentResource.getNode().getSession().save();
                }
            } else if (path.startsWith("/")) {
                JCRSessionWrapper session = currentResource.getNode().getSession();
                try {
                    node = (JCRNodeWrapper) session.getItem(path);
                } catch (PathNotFoundException e) {
                    if (renderContext.isEditMode()) {
                        JCRNodeWrapper parent = session.getNode(StringUtils.substringBeforeLast(path, "/"));
                        if(!parent.isCheckedOut())
                            parent.checkout();
                        node = parent.addNode(StringUtils.substringAfterLast(path, "/"), areaType);
                        session.save();
                    }
                }
            }
            if (node != null) {
                if (node.getPath().endsWith("/*")) {
                    if (renderContext.isEditMode() && editable) {
                        printModuleStart("placeholder", node.getPath(), null, null);
                        printModuleEnd();
                    }

                    return EVAL_PAGE;
                }

                Resource resource = new Resource(node, templateType, template, forcedTemplate);

                if (renderContext.isEditMode() && editable) {
                    if (node.isNodeType("jmix:link")) {
                        // no placeholder at all for links
                        render(renderContext, resource);
                    } else {
                        Script script = null;
                        try {
                            script = RenderService.getInstance().resolveScript(resource, renderContext);
                            printModuleStart("area", node.getPath(), resource.getResolvedTemplate(), script.getTemplate().getInfo());
                        } catch (TemplateNotFoundException e) {
                            printModuleStart("area", node.getPath(), resource.getResolvedTemplate(), "Script not found");
                        }

                        JCRNodeWrapper w = new JCRNodeDecorator(node) {
                            @Override
                            public boolean isNodeType(String s) throws RepositoryException {
                                return nodeTypes == null ? super.isNodeType(s) : nodeTypes.contains(s);
                            }

                            @Override
                            public JCRPropertyWrapper getProperty(String s) throws PathNotFoundException, RepositoryException {
                                JCRPropertyWrapper p = (JCRPropertyWrapper) super.getProperty(s);
                                return new EditablePropertyWrapper(p);
                            }
                        };
                        resource = new Resource(w, resource.getTemplateType(), resource.getTemplate(), resource.getForcedTemplate());
                        if (nodeTypes != null) {
                            pageContext.setAttribute("areaNodeTypesRestriction", nodeTypes, PageContext.REQUEST_SCOPE);
                            pageContext.setAttribute("areaNodeTypesRestrictionLevel", pageContext.getAttribute("org.jahia.modules.level", PageContext.REQUEST_SCOPE), PageContext.REQUEST_SCOPE);
                        }
                        render(renderContext, resource);
                        if (nodeTypes != null) {
                            pageContext.removeAttribute("areaNodeTypesRestriction", PageContext.REQUEST_SCOPE);
                            pageContext.removeAttribute("areaNodeTypesRestrictionLevel", PageContext.REQUEST_SCOPE);
                        }
                        printModuleEnd();
                    }
                } else {
                    render(renderContext, resource);
                }
            }
        } catch (RepositoryException ex) {
            throw new JspException(ex);
        } catch (IOException ex) {
            throw new JspException(ex);
        } finally {
            if (var != null) {
                pageContext.setAttribute(var, buffer);
            }
            path = null;
            template = null;
            forcedTemplate = null;
            editable = true;
            templateType = "html";
            nodeTypes = null;
            var = null;
            buffer = null;
            parameters.clear();
            areaType = "jnt:contentList";

            Integer level = (Integer) pageContext.getAttribute("org.jahia.modules.level", PageContext.REQUEST_SCOPE);
            pageContext.setAttribute("org.jahia.modules.level", level != null ? level - 1 : 1, PageContext.REQUEST_SCOPE);
        }

        return EVAL_PAGE;
    }

    private void printModuleStart(String type, String path, String resolvedTemplate, String scriptInfo) throws IOException {

        buffer.append("<div class=\"jahia-template-gxt\" jahiatype=\"module\" ")
                .append("id=\"module")
                .append(UUID.randomUUID().toString())
                .append("\" type=\"")
                .append(type)
                .append("\" ")
                .append((scriptInfo != null) ? " scriptInfo=\"" + scriptInfo + "\"" : "")
                .append("\" path=\"").append(path)
                .append("\" ")
                .append((nodeTypes != null) ? "nodetypes=\"" + nodeTypes + "\"" : "")
                .append((resolvedTemplate != null) ? " template=\"" + resolvedTemplate + "\"" : "")
                .append(">");
        if (var == null) {
            pageContext.getOut().print(buffer);
            buffer.delete(0, buffer.length());
        }

    }

    private void printModuleEnd() throws IOException {
        buffer.append("</div>");
        if (var == null) {
            pageContext.getOut().print(buffer);
            buffer.delete(0, buffer.length());
        }
    }

    private void render(RenderContext renderContext, Resource resource) throws IOException {
        try {
            if (renderContext.isIncludeSubModules()) {
                buffer.append(RenderService.getInstance().render(resource, renderContext));
                if (var == null) {
                    pageContext.getOut().print(buffer);
                    buffer.delete(0, buffer.length());
                }
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

    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

    /**
     * Wraps property to add editable placeholder around text values
     */
    class EditablePropertyWrapper implements JCRPropertyWrapper {
        JCRPropertyWrapper p;

        EditablePropertyWrapper(JCRPropertyWrapper p) {
            this.p = p;
        }

        public CategoryBean getCategory() throws ValueFormatException, RepositoryException {
            return p.getCategory();
        }

        public void setValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
            p.setValue(value);
        }

        public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
            p.setValue(values);
        }

        public void setValue(String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
            p.setValue(value);
        }

        public void setValue(String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
            p.setValue(values);
        }

        public void setValue(InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
            p.setValue(value);
        }

        public void setValue(Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
            p.setValue(value);
        }

        public void setValue(long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
            p.setValue(value);
        }

        public void setValue(double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
            p.setValue(value);
        }

        public void setValue(BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
            p.setValue(value);
        }

        public void setValue(Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
            p.setValue(value);
        }

        public void setValue(boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
            p.setValue(value);
        }

        public void setValue(Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
            p.setValue(value);
        }

        public Value getValue() throws ValueFormatException, RepositoryException {
            return new EditableValueWrapper((JCRValueWrapper) p.getValue());
        }

        public Value[] getValues() throws ValueFormatException, RepositoryException {
            return p.getValues();
        }

        public String getString() throws ValueFormatException, RepositoryException {
            return p.getString();
        }

        public InputStream getStream() throws ValueFormatException, RepositoryException {
            return p.getStream();
        }

        public Binary getBinary() throws ValueFormatException, RepositoryException {
            return p.getBinary();
        }

        public long getLong() throws ValueFormatException, RepositoryException {
            return p.getLong();
        }

        public double getDouble() throws ValueFormatException, RepositoryException {
            return p.getDouble();
        }

        public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
            return p.getDecimal();
        }

        public Calendar getDate() throws ValueFormatException, RepositoryException {
            return p.getDate();
        }

        public boolean getBoolean() throws ValueFormatException, RepositoryException {
            return p.getBoolean();
        }

        public Node getNode() throws ValueFormatException, RepositoryException {
            return p.getNode();
        }

        public Property getProperty() throws ItemNotFoundException, ValueFormatException, RepositoryException {
            return p.getProperty();
        }

        public long getLength() throws ValueFormatException, RepositoryException {
            return p.getLength();
        }

        public long[] getLengths() throws ValueFormatException, RepositoryException {
            return p.getLengths();
        }

        public PropertyDefinition getDefinition() throws RepositoryException {
            return p.getDefinition();
        }

        public int getType() throws RepositoryException {
            return p.getType();
        }

        public boolean isMultiple() throws RepositoryException {
            return p.isMultiple();
        }

        public String getPath() throws RepositoryException {
            return p.getPath();
        }

        public String getName() throws RepositoryException {
            return p.getName();
        }

        public JCRItemWrapper getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
            return p.getAncestor(depth);
        }

        public JCRNodeWrapper getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
            return p.getParent();
        }

        public int getDepth() throws RepositoryException {
            return p.getDepth();
        }

        public JCRSessionWrapper getSession() throws RepositoryException {
            return p.getSession();
        }

        public boolean isNode() {
            return p.isNode();
        }

        public boolean isNew() {
            return p.isNew();
        }

        public boolean isModified() {
            return p.isModified();
        }

        public boolean isSame(Item otherItem) throws RepositoryException {
            return p.isSame(otherItem);
        }

        public void accept(ItemVisitor visitor) throws RepositoryException {
            p.accept(visitor);
        }

        public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
            p.save();
        }

        public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException {
            p.refresh(keepChanges);
        }

        public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
            p.remove();
        }

        public void saveSession() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
            p.saveSession();
        }

        class EditableValueWrapper implements JCRValueWrapper {
            private JCRValueWrapper v;

            EditableValueWrapper(JCRValueWrapper v) {
                this.v = v;
            }

            public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
                return v.getString();
            }

            public InputStream getStream() throws IllegalStateException, RepositoryException {
                return v.getStream();
            }

            public long getLong() throws ValueFormatException, IllegalStateException, RepositoryException {
                return v.getLong();
            }

            public double getDouble() throws ValueFormatException, IllegalStateException, RepositoryException {
                return v.getDouble();
            }

            public Calendar getDate() throws ValueFormatException, IllegalStateException, RepositoryException {
                return v.getDate();
            }

            public boolean getBoolean() throws ValueFormatException, IllegalStateException, RepositoryException {
                return v.getBoolean();
            }

            public int getType() {
                return v.getType();
            }

            public Node getNode() throws ValueFormatException, IllegalStateException, RepositoryException {
                return v.getNode();
            }

            public CategoryBean getCategory() throws ValueFormatException, RepositoryException {
                return v.getCategory();
            }

            public PropertyDefinition getDefinition() throws RepositoryException {
                return v.getDefinition();
            }

            public Date getTime() throws ValueFormatException, RepositoryException {
                return v.getTime();
            }

            public Binary getBinary() throws RepositoryException {
                return v.getBinary();
            }

            public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
                return v.getDecimal();
            }
        }

    }

}