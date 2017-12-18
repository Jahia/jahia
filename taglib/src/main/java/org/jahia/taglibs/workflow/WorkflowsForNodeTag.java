/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.workflow;

import org.slf4j.Logger;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 
 * User: toto
 * Date: Mar 17, 2010
 * Time: 8:02:06 PM
 * 
 */
public class WorkflowsForNodeTag extends AbstractJahiaTag {
    private static final long serialVersionUID = 3090262422260075789L;

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(WorkflowsForNodeTag.class);

    private JCRNodeWrapper node;
    private String var;

    private String workflowAction;

    private int scope = PageContext.PAGE_SCOPE;

    private boolean checkPermission = true;

    private Locale locale;

    @Override
    public int doEndTag() throws JspException {
        List<WorkflowDefinition> defs = null;
        try {
            if (workflowAction != null) {
                WorkflowDefinition workflowForAction = WorkflowService.getInstance().getPossibleWorkflowForType(node, checkPermission, workflowAction, locale != null ? locale : getUILocale());
                if (workflowForAction != null) {
                    defs = Collections.singletonList(workflowForAction);
                } else {
                    defs = Collections.emptyList();
                }
            } else {
                defs = new ArrayList<WorkflowDefinition>(WorkflowService.getInstance().getPossibleWorkflows(node, checkPermission, locale != null ? locale : getUILocale()).values());
            }
        } catch (RepositoryException e) {
            logger.error("Could not retrieve workflows", e);
        }


        pageContext.setAttribute(var, defs, scope);
        node = null;
        var = null;
        workflowAction = null;
        scope = PageContext.PAGE_SCOPE;
        checkPermission = true;
        return super.doEndTag();
    }

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setWorkflowAction(String workflowAction) {
        this.workflowAction = workflowAction;
    }

    public void setCheckPermission(boolean checkPermission) {
        this.checkPermission = checkPermission;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
