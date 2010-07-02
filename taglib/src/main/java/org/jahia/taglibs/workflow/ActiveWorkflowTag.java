package org.jahia.taglibs.workflow;

import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.workflow.*;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: May 14, 2010
 * Time: 3:13:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class ActiveWorkflowTag extends AbstractJahiaTag {
    private final static Logger logger = Logger.getLogger(AvailableTasksTag.class);

    private JCRNodeWrapper node;
    private String var;
    private int scope = PageContext.PAGE_SCOPE;

    public int doEndTag() throws JspException {
        List<Workflow> wfs = WorkflowService.getInstance().getActiveWorkflows(node, getUILocale());
        pageContext.setAttribute(var, wfs, scope);
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