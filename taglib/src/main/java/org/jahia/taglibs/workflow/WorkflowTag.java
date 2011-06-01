package org.jahia.taglibs.workflow;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 3/1/11
 * Time: 17:17
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowTag extends AbstractJahiaTag {
    private String provider;
    private String id;
    private String var;
    private int scope = PageContext.PAGE_SCOPE;

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public void setVar(String var) {
        this.var = var;
    }

    @Override
    public int doEndTag() throws JspException {
        Workflow workflow = WorkflowService.getInstance().getWorkflow(provider, id, getUILocale() );

        pageContext.setAttribute(var, workflow, scope);

        id = null;
        var = null;
        scope = PageContext.PAGE_SCOPE;
        return super.doEndTag();
    }

}
