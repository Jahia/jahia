package org.jahia.taglibs.workflow;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.taglibs.AbstractJahiaTag;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.List;

/**
 * Gets all possible definitions for an action
 */
public class WorkflowsForActionTag extends AbstractJahiaTag {
    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(WorkflowsForActionTag.class);

    private String var;

    private String workflowAction;

    private int scope = PageContext.PAGE_SCOPE;

    @Override
    public int doEndTag() throws JspException {
        List<WorkflowDefinition> defs = null;
        try {
            defs = WorkflowService.getInstance().getWorkflowDefinitionsForType(workflowAction, getUILocale());
        } catch (RepositoryException e) {
            logger.error("Could not retrieve workflows", e);
        }


        pageContext.setAttribute(var, defs, scope);
        var = null;
        workflowAction = null;
        scope = PageContext.PAGE_SCOPE;
        return super.doEndTag();
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setWorkflowAction(String workflowAction) {
        this.workflowAction = workflowAction;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }
}
