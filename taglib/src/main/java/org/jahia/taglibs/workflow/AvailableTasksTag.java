/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.workflow;

import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.rbac.Role;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaPrincipal;
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
    private JahiaUser user;

    @Override
    public int doEndTag() throws JspException {
        List<WorkflowTask> tasks = new ArrayList<WorkflowTask>();
        if (node != null) {
            List<Workflow> actives = WorkflowService.getInstance().getActiveWorkflows(node, getUILocale());
            for (Workflow workflow : actives) {
                for (WorkflowAction workflowAction : workflow.getAvailableActions()) {
                    if (workflowAction instanceof WorkflowTask) {
                        WorkflowTask workflowTask = (WorkflowTask) workflowAction;
                        List<WorkflowParticipation> participations = workflowTask.getParticipations();
                        if (participations != null) {
                            for (WorkflowParticipation participation : participations) {
                                JahiaPrincipal principal = participation.getJahiaPrincipal();
                                if ((principal instanceof JahiaGroup && ((JahiaGroup) principal).isMember(getUser())) ||
                                    (principal instanceof JahiaUser && ((JahiaUser) principal).getUserKey().equals(getUser().getUserKey())) ||
                                    (principal instanceof Role && (getUser().hasRole((Role) principal)))) {
                                    tasks.add(workflowTask);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } else if (user != null) {
            tasks = WorkflowService.getInstance().getTasksForUser(user, null);
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

    public void setUser(JahiaUser user) {
        this.user = user;
    }
}