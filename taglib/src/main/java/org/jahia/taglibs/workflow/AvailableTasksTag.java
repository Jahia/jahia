package org.jahia.taglibs.workflow;

import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.*;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 17, 2010
 * Time: 8:02:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class AvailableTasksTag extends AbstractJahiaTag {
    private final static Logger logger = Logger.getLogger(AvailableTasksTag.class);

    private JCRNodeWrapper node;
    private String var;
    private int scope = PageContext.PAGE_SCOPE;

    @Override
    public int doEndTag() throws JspException {
        List<WorkflowTask> tasks = new ArrayList<WorkflowTask>();
        List<Workflow> actives = WorkflowService.getInstance().getActiveWorkflows(node);
        for (Workflow workflow : actives) {
            for (WorkflowAction workflowAction : workflow.getAvailableActions()) {
                if (workflowAction instanceof WorkflowTask) {
                    WorkflowTask workflowTask = (WorkflowTask) workflowAction;
                    List<WorkflowParticipation> participations = workflowTask.getParticipations();
                    if (participations != null) {
                        for (WorkflowParticipation participation : participations) {
                            if ((participation.getJahiaPrincipal() instanceof JahiaGroup && ((JahiaGroup)participation.getJahiaPrincipal()).isMember(getUser())) ||
                                    (participation.getJahiaPrincipal() instanceof JahiaUser && ((JahiaUser)participation.getJahiaPrincipal()).getUserKey().equals(getUser().getUserKey()))) {
                                tasks.add(workflowTask);
                            }
                        }
                    }
                }
            }
        }

        pageContext.setAttribute(var, tasks, scope);
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