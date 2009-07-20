package org.jahia.taglibs.template.include;

import org.jahia.data.beans.ContentBean;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.RenderContext;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.taglibs.internal.gwt.GWTIncluder;
import org.apache.log4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 14, 2009
 * Time: 7:18:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModuleTag extends BodyTagSupport {
    private static Logger logger = Logger.getLogger(ModuleTag.class);

    private String path;

    private JCRNodeWrapper node;

    private String nodeName;

    private String contentBeanName;

    private String template;

    private String templateType = "html";

    private String workspace = null;

    private Locale locale = null;

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

    @Override
    public int doStartTag() throws JspException {
        return super.doStartTag();    //To change body of overridden methods use File | Settings | File Templates.
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
                JCRNodeWrapper nodeWrapper = currentResource.getNode();
                try {
                    if (nodeWrapper.hasNode(path)) {
                        node = (JCRNodeWrapper) nodeWrapper.getNode(path);
                    } else {
                        if (currentResource != null) {
                            currentResource.getMissingResources().add(path);
                        }
//                        pageContext.getOut().print(GWTIncluder.generateJahiaModulePlaceHolder(false,null,"placeHolder","placeholder"+ UUID.randomUUID().toString(),new HashMap()));
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (node != null) {
                Resource resource = new Resource(node, workspace , locale, templateType, template);

                try {
                    if (renderContext.isIncludeSubModules()) {
                        pageContext.getOut().print(RenderService.getInstance().render(resource, renderContext));
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            path = null;
            contentBeanName = null;
            node = null;
            template = null;
            templateType = "html";
            workspace = null;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return EVAL_PAGE;
    }
}
