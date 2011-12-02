package org.jahia.taglibs.workflow;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.workflow.HistoryWorkflowTask;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.taglibs.AbstractJahiaTag;
import org.slf4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.List;

/**
 * Get past tasks of a running workflow
 */
public class WorkflowHistoryTag extends AbstractJahiaTag {
    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(WorkflowHistoryTag.class);

    private String var;

    private int scope = PageContext.PAGE_SCOPE;

    private String workflowId;
    private String workflowProvider;

    @Override
    public int doEndTag() throws JspException {
        WorkflowService service = WorkflowService.getInstance();

        List<HistoryWorkflowTask> tasks = service.getHistoryWorkflowTasks(workflowId,
                workflowProvider, getUILocale());

        pageContext.setAttribute(var, tasks, scope);
        var = null;
        workflowId = null;
        workflowProvider = null;
        scope = PageContext.PAGE_SCOPE;

        return super.doEndTag();
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public void setWorkflowProvider(String workflowProvider) {
        this.workflowProvider = workflowProvider;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }
}
