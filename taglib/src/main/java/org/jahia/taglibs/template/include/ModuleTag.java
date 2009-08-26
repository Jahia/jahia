package org.jahia.taglibs.template.include;

import org.jahia.data.beans.ContentBean;
import org.jahia.data.beans.CategoryBean;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.RenderContext;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRNodeDecorator;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.apache.log4j.Logger;

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
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Handler for the &lt;template:module/&gt; tag, used to render content objects.
 * User: toto
 * Date: May 14, 2009
 * Time: 7:18:15 PM
 */
public class ModuleTag extends BodyTagSupport {

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

    private String workspace = null;

    private Locale locale = null;

    private String lockTemplate = null;

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

    public void setLockTemplate(String lockTemplate) {
        this.lockTemplate = lockTemplate;
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
                renderContext = new RenderContext((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse());
            }
            Resource currentResource = (Resource) pageContext.getAttribute("currentResource", PageContext.REQUEST_SCOPE);
            if (currentResource != null) {
                templateType = currentResource.getTemplateType();
                workspace = currentResource.getWorkspace();
                locale = currentResource.getLocale();
            }
            if (locale == null) {
                locale = Jahia.getThreadParamBean().getCurrentLocale();
            }
            if (workspace == null) {
                if (Jahia.getThreadParamBean().getOperationMode().equals("normal")) {
                    workspace = "live";
                } else {
                    workspace = "default";
                }
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
                        } else {
                            currentResource.getMissingResources().add(path);
                            Map<String, Object> extraParams = new HashMap<String, Object>();

                            if (renderContext.isEditMode()) {
                                printPlaceholderModuleStart("placeholder", nodeWrapper.getPath()+"/"+path);
                                printPlaceholderModuleEnd();
                            }
                        }
                    } else if (path.startsWith("/")) {
                        try {
                            node = (JCRNodeWrapper) currentResource.getNode().getSession().getItem(path);
                        } catch (PathNotFoundException e) {
                            String currentPath = currentResource.getNode().getPath();
                            if (path.startsWith(currentPath+"/") && path.substring(currentPath.length()+1).indexOf('/') == -1) {
                                currentResource.getMissingResources().add(path.substring(currentPath.length()+1));
                            }

                            if (renderContext.isEditMode()) {
                                printPlaceholderModuleStart("placeholder", path);
                                printPlaceholderModuleEnd();
                            }
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (node != null) {
                if (nodeTypes != null) {
                    StringTokenizer st = new StringTokenizer(nodeTypes, " ");
                    boolean found = false;
                    while (st.hasMoreTokens()) {
                        String tok = st.nextToken();
                        try {
                            if (node.isNodeType(tok)) {
                                found = true;
                                break;
                            }
                        } catch (RepositoryException e) {
                            logger.error("Cannot test on "+tok,e);
                        }
                    }
                    if (!found) {
                        return EVAL_PAGE;
                    }
                }
                // set user template if exists
                if (lockTemplate != null) {
                    template = lockTemplate;
                }
                else {
                    try {
                        if (node.hasProperty("j:defaultTemplate")) {
                            template = node.getProperty("j:defaultTemplate").getValue().getString();
                        }
                    } catch (RepositoryException e) {
                        logger.error("error finding template in node : "+node,e);
                    }
                }
                Resource resource = new Resource(node, workspace , locale, templateType, template);

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
                            printPlaceholderModuleStart(type, node.getPath());

                            JCRNodeWrapper w = new JCRNodeDecorator(node) {
                                @Override
                                public Property getProperty(String s) throws PathNotFoundException, RepositoryException {
                                    JCRPropertyWrapper p = (JCRPropertyWrapper) super.getProperty(s);
                                    return new EditablePropertyWrapper(p);
                                }
                            };
                            resource = new Resource(w, resource.getWorkspace(), resource.getLocale(), resource.getTemplateType(), resource.getTemplate());
                            render(renderContext, resource);
                            if (node.isNodeType("jnt:contentList") || node.isNodeType("jnt:containerList")) {
                                printPlaceholderModuleStart("placeholder", node.getPath()+"/*");
                                printPlaceholderModuleEnd();
                            }
                            printPlaceholderModuleEnd();
                        }
                    } catch (RepositoryException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                } else {
                    render(renderContext, resource);
                }

            }
            path = null;
            contentBeanName = null;
            node = null;
            template = null;
            editable = true;
            templateType = "html";
            workspace = null;
            nodeTypes = null;
        } catch (IOException ex) {
            throw new JspException(ex);
        } finally {
    		Integer level = (Integer) pageContext.getAttribute("org.jahia.modules.level", PageContext.REQUEST_SCOPE);
    		pageContext.setAttribute("org.jahia.modules.level", level != null ? level -1 : 1, PageContext.REQUEST_SCOPE);
        	
        }
        return EVAL_PAGE;
    }

    private void printPlaceholderModuleStart(String type, String path) throws IOException {
        pageContext.getOut().print("<div class=\"jahia-template-gxt\" jahiatype=\"placeholder\" " +
                "id=\"placeholder"+ UUID.randomUUID().toString()+"\" type=\""+type+"\" path=\""+ path +"\" " +
                ((nodeTypes != null) ? "nodetypes=\""+nodeTypes+"\"" : "") + " template=\""+template+"\">");
    }

    private void printPlaceholderModuleEnd() throws IOException {
        pageContext.getOut().print("</div>");
    }

    private void render(RenderContext renderContext, Resource resource) throws IOException {
        try {
            if (renderContext.isIncludeSubModules()) {
                pageContext.getOut().print(RenderService.getInstance().render(resource, renderContext));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException io) {
             pageContext.getOut().print(io);
        }

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

        public void setValue(long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
            p.setValue(value);
        }

        public void setValue(double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
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

        public long getLong() throws ValueFormatException, RepositoryException {
            return p.getLong();
        }

        public double getDouble() throws ValueFormatException, RepositoryException {
            return p.getDouble();
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

        public String getPath() throws RepositoryException {
            return p.getPath();
        }

        public String getName() throws RepositoryException {
            return p.getName();
        }

        public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
            return p.getAncestor(depth);
        }

        public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
            return p.getParent();
        }

        public int getDepth() throws RepositoryException {
            return p.getDepth();
        }

        public Session getSession() throws RepositoryException {
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

        class EditableValueWrapper implements JCRValueWrapper {
            private JCRValueWrapper v;

            EditableValueWrapper(JCRValueWrapper v) {
                this.v = v;
            }

            public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
                return  "<div class=\"jahia-template-gxt\" jahiatype=\"placeholder\" " +
                        "id=\"placeholder"+ UUID.randomUUID().toString()+"\" type=\"text\" path=\""+ path +"\" >" +
                        v.getString() + "</div>";
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
        }

    }

}
