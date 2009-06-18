package org.jahia.taglibs.template.include;

import org.jahia.data.beans.ContentBean;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.apache.log4j.Logger;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.IOException;

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

    private String node;

    private String contentBean;

    private String template;

    public void setPath(String path) {
        this.path = path;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public void setContentBean(String contentBean) {
        this.contentBean = contentBean;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @Override
    public int doStartTag() throws JspException {
        return super.doStartTag();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            Resource resource = null;
            if (node != null) {
                JCRNodeWrapper nodewrapper  = (JCRNodeWrapper) pageContext.getAttribute(node);
                resource = new Resource(nodewrapper, "html", template);
            } else if (contentBean != null) {
                try {
                    ContentBean bean = (ContentBean) pageContext.getAttribute(contentBean);
                    resource = new Resource(bean.getContentObject().getJCRNode(Jahia.getThreadParamBean()), "html", template);
                } catch (JahiaException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            if (resource != null) {
                try {
                    StringBuffer buffer = RenderService.getInstance().render(resource, (HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse());

                    pageContext.getOut().print(buffer.toString());
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            path = null;
            contentBean = null;
            node = null;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return EVAL_PAGE;
    }
}
