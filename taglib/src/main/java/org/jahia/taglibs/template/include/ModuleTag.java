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

import org.jahia.data.beans.ContentBean;
import org.jahia.data.beans.CategoryBean;
import org.jahia.data.JahiaData;
import org.jahia.services.render.*;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.ParamParent;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.util.ReaderInputStream;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.math.BigDecimal;

/**
 * Handler for the &lt;template:module/&gt; tag, used to render content objects.
 * User: toto
 * Date: May 14, 2009
 * Time: 7:18:15 PM
 */
public class ModuleTag extends BodyTagSupport implements ParamParent {

    private static final long serialVersionUID = -8968618483176483281L;

	private static Logger logger = Logger.getLogger(ModuleTag.class);

    private String path;

    private JCRNodeWrapper node;

    private String nodeName;

    private String contentBeanName;

    private String template;

    private String templateType = "html";

    private boolean editable = true;

    private String nodeTypes;

    private String forcedTemplate = null;

    private String templateWrapper = null;

    private String autoCreateType = null;

    private String var = null;

    private StringBuffer buffer = new StringBuffer();

    private Map<String, String> parameters = new HashMap<String, String>();

    private String importString;

    public void setPath(String path) {
        this.path = path;
    }

    public void setNodeName(String node) {
        this.nodeName = node;
    }

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setContentBeanName(String contentBeanName) {
        this.contentBeanName = contentBeanName;
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

    public void setAutoCreateType(String autoCreateType) {
        this.autoCreateType = autoCreateType;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setImportString(String importString) {
        this.importString = importString;
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
                final JahiaData jData = (JahiaData) pageContext.getRequest().getAttribute("org.jahia.data.JahiaData");
                renderContext = new RenderContext((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse(), jData.getProcessingContext().getUser());
                renderContext.setSite(jData.getProcessingContext().getSite());
            }
//            if(templateWrapper != null) {
//                renderContext.setTemplateWrapper(templateWrapper);
//            }
            // add custom parameters
            Map<String, Object> oldParams = new HashMap<String, Object>(renderContext.getModuleParams());
            renderContext.getModuleParams().clear();

            buffer = new StringBuffer();

            try {
	            String charset = pageContext.getResponse().getCharacterEncoding();
	            for (Map.Entry<String, String> param : parameters.entrySet()) {
	            	renderContext.getModuleParams().put(URLDecoder.decode(param.getKey(), charset), URLDecoder.decode(param.getValue(), charset));
	            }

	            Resource currentResource = (Resource) pageContext.getAttribute("currentResource", PageContext.REQUEST_SCOPE);
	            if (currentResource != null) {
	                templateType = currentResource.getTemplateType();
	            }
	            if (nodeName != null) {
	                node = (JCRNodeWrapper) pageContext.findAttribute(nodeName);
	            } else if (contentBeanName != null) {
	                try {
	                    ContentBean bean = (ContentBean) pageContext.getAttribute(contentBeanName);
	                    node = bean.getContentObject().getJCRNode(Jahia.getThreadParamBean());
	                } catch (JahiaException e) {
	                    logger.error(e.getMessage(), e);
	                }
	            } else if (path != null && currentResource != null) {
	                try {
	                    if (!path.startsWith("/")) {
	                        JCRNodeWrapper nodeWrapper = currentResource.getNode();
	                        if (!path.equals("*") && nodeWrapper.hasNode(path)) {
	                            node = (JCRNodeWrapper) nodeWrapper.getNode(path);
	                        } else if (!path.equals("*") && renderContext.isEditMode() && autoCreateType != null) {
	                            node = nodeWrapper.addNode(path, autoCreateType);
	                            nodeWrapper.save();
	                        } else if (!path.equals("*") && renderContext.isEditMode() && importString != null) {
                                Session session = nodeWrapper.getSession();
                                session.importXML(nodeWrapper.getPath(), new ReaderInputStream(new StringReader(importString), "UTF-8"), ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
                                session.save();
                                node = (JCRNodeWrapper) nodeWrapper.getNode(path);
	                        } else {
	                            currentResource.getMissingResources().add(path);

	                            if (renderContext.isEditMode()) {
	                                printModuleStart("placeholder", nodeWrapper.getPath()+"/"+path, null, null);
	                                printModuleEnd();
	                            }
	                        }
	                    } else if (path.startsWith("/")) {
                            JCRSessionWrapper session = currentResource.getNode().getSession();
                            try {
	                            node = (JCRNodeWrapper) session.getItem(path);
                            } catch (PathNotFoundException e) {
                                if (renderContext.isEditMode() && autoCreateType != null) {
                                    JCRNodeWrapper parent = session.getNode(StringUtils.substringBeforeLast(path,"/"));
                                    node = parent.addNode(StringUtils.substringAfterLast(path,"/"), autoCreateType);
                                    session.save();
                                } else if (renderContext.isEditMode() && importString != null) {
                                    JCRNodeWrapper parent = session.getNode(StringUtils.substringBeforeLast(path,"/"));
                                    session.importXML(parent.getPath(), new ReaderInputStream(new StringReader(importString), "UTF-8"), ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
                                    session.save();
                                    node = (JCRNodeWrapper) session.getItem(path);
                                } else {
                                    String currentPath = currentResource.getNode().getPath();
                                    if (path.startsWith(currentPath+"/") && path.substring(currentPath.length()+1).indexOf('/') == -1) {
                                        currentResource.getMissingResources().add(path.substring(currentPath.length()+1));
                                    }

                                    if (renderContext.isEditMode()) {
                                        printModuleStart("placeholder", path, null, null);
                                        printModuleEnd();
                                    }
                                }
                            }
                        }
	                } catch (RepositoryException e) {
	                    logger.error(e.getMessage(), e);
	                }
	            }
	            if (node != null) {
	                // add externalLinks
                    if(nodeTypes==null) {
                        Integer level = (Integer) pageContext.getAttribute("areaNodeTypesRestrictionLevel", PageContext.REQUEST_SCOPE);
                        Integer currentLevel = (Integer) pageContext.getAttribute("org.jahia.modules.level", PageContext.REQUEST_SCOPE);
                        if(level!= null && currentLevel!= null && currentLevel == level+1)
                            nodeTypes = (String) pageContext.getAttribute("areaNodeTypesRestriction",PageContext.REQUEST_SCOPE);
                    }
	                if (node.getPath().endsWith("/*")) {
	                    if (renderContext.isEditMode() && editable) {
	                        printModuleStart("placeholder", node.getPath(), null, null);
	                        printModuleEnd();
	                    }

	                    return EVAL_PAGE;
	                }
	                if (nodeTypes != null && !"".equals(nodeTypes.trim())) {
	                    StringTokenizer st = new StringTokenizer(nodeTypes, " ");
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
	                                logger.error("Cannot test on "+tok,e);
	                            }
	                        }
	                    } catch (RepositoryException e) {
	                        logger.error(e,e);
	                    }
	                    if (!found) {
	                        return EVAL_PAGE;
	                    }
	                }

                    Resource resource = new Resource(node, templateType, template, forcedTemplate);

	                if (renderContext.isEditMode() && editable) {
	                    try {
	                        if (node.isNodeType("jmix:link")) {
	                            // no placeholder at all for links
	                            render(renderContext, resource);
	                        } else {
	                            String type = "existingNode";
	                            if (node.isNodeType("jnt:contentList") || node.isNodeType("jnt:containerList")) {
	                                type = "list";
	                            }
                                Script script = null;
                                try {
                                    script = RenderService.getInstance().resolveScript(resource, renderContext);
                                    printModuleStart(type, node.getPath(), resource.getResolvedTemplate(), script.getTemplate().getInfo());
                                } catch (TemplateNotFoundException e) {
                                    printModuleStart(type, node.getPath(), resource.getResolvedTemplate(), "Script not found");
                                } 

	                            JCRNodeWrapper w = new JCRNodeDecorator(node) {
	                                @Override
	                                public JCRPropertyWrapper getProperty(String s) throws PathNotFoundException, RepositoryException {
	                                    JCRPropertyWrapper p = (JCRPropertyWrapper) super.getProperty(s);
	                                    return new EditablePropertyWrapper(p);
	                                }
	                            };
	                            resource = new Resource(w, resource.getTemplateType(), resource.getTemplate(), resource.getForcedTemplate());
	                            render(renderContext, resource);
	                            printModuleEnd();
	                        }
	                    } catch (RepositoryException e) {
	                        logger.error(e.getMessage(), e);
	                    }
	                } else {
	                    render(renderContext, resource);
	                }
	            }
            } finally {
            	renderContext.getModuleParams().clear();
            	renderContext.getModuleParams().putAll(oldParams);
            }
        } catch (IOException ex) {
            throw new JspException(ex);
        } finally {
            if (var != null) {
                pageContext.setAttribute(var,buffer);
            }
            path = null;
            contentBeanName = null;
            node = null;
            template = null;
            forcedTemplate = null;
            templateWrapper = null;
            editable = true;
            templateType = "html";
            nodeTypes = null;
            autoCreateType = null;
            var = null;
            buffer = null;
            importString = null;
            parameters.clear();

    		Integer level = (Integer) pageContext.getAttribute("org.jahia.modules.level", PageContext.REQUEST_SCOPE);
    		pageContext.setAttribute("org.jahia.modules.level", level != null ? level -1 : 1, PageContext.REQUEST_SCOPE);

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

    private void printModuleEnd()  throws IOException {
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
