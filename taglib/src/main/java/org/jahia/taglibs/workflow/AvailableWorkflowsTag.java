package org.jahia.taglibs.workflow;

import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 17, 2010
 * Time: 8:02:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class AvailableWorkflowsTag extends AbstractJahiaTag {
    private final static Logger logger = Logger.getLogger(AvailableWorkflowsTag.class);

    private JCRNodeWrapper node;
    private String var;
    private int scope = PageContext.PAGE_SCOPE;

    @Override
    public int doEndTag() throws JspException {
        List<WorkflowDefinition> defs = null;
        try {
            defs = WorkflowService.getInstance().getPossibleWorkflows(node, getUser());
        } catch (RepositoryException e) {
            logger.error("Could not retrieve workflows");
        }
        pageContext.setAttribute(var, defs, scope);
        node = null;
        var = null;
        scope = PageContext.PAGE_SCOPE;
        return super.doEndTag();
    }

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }
}
