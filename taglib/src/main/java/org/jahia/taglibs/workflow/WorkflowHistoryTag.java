package org.jahia.taglibs.workflow;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryItem;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryTask;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.workflow.HistoryWorkflowTask;
import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.taglibs.AbstractJahiaTag;
import org.slf4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Get past tasks of a running workflow
 */
public class WorkflowHistoryTag extends AbstractJahiaTag {
    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(WorkflowsForActionTag.class);

    private String var;

    private int scope = PageContext.PAGE_SCOPE;

    private Workflow workflow;

    @Override
    public int doEndTag() throws JspException {
        WorkflowService service = WorkflowService.getInstance();

        List<HistoryWorkflowTask> tasks = service.getHistoryWorkflowTasks(workflow.getId(),
                workflow.getProvider(), getUILocale());

        pageContext.setAttribute(var, tasks, scope);
        var = null;
        workflow = null;
        scope = PageContext.PAGE_SCOPE;

        return super.doEndTag();
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }
}
