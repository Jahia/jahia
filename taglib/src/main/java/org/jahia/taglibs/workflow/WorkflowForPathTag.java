package org.jahia.taglibs.workflow;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.workflow.HistoryWorkflow;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.taglibs.AbstractJahiaTag;
import org.slf4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.List;

/**
 * Get past tasks of a running workflow
 */
public class WorkflowForPathTag extends AbstractJahiaTag {
    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(WorkflowForPathTag.class);

    private String var;

    private int scope = PageContext.PAGE_SCOPE;

    private String path;

    @Override
    public int doEndTag() throws JspException {
        WorkflowService service = WorkflowService.getInstance();
        List<HistoryWorkflow> history = service.getHistoryWorkflowsByPath(path, getUILocale());

        pageContext.setAttribute(var, history, scope);
        var = null;
        path = null;
        scope = PageContext.PAGE_SCOPE;

        return super.doEndTag();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }
}
