/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.*;
import org.jahia.services.workflow.*;
import org.jahia.taglibs.AbstractJahiaTag;
import org.slf4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * User: toto
 * Date: Mar 17, 2010
 * Time: 8:02:06 PM
 *
 */
public class TasksForNodeTag extends AbstractJahiaTag {
    private static final long serialVersionUID = -8866901816731959175L;
    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(TasksForNodeTag.class);
    private JCRNodeWrapper node;
    private String var;
    private int scope = PageContext.PAGE_SCOPE;
    private JahiaUser user;
    private Locale locale;

    @Override
    public int doEndTag() throws JspException {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(WorkflowService.class.getClassLoader());
            List<WorkflowTask> tasks = new ArrayList<WorkflowTask>();
            if (node != null) {
                Locale uiLocale = getUILocale();
                List<Workflow> actives = WorkflowService.getInstance().getActiveWorkflows(node, locale != null ? locale : uiLocale, uiLocale);
                if(logger.isDebugEnabled()){
                    if(actives.isEmpty()){
                        logger.debug("Could not find any active workflow for node : " +node.getPath());
                    } else {
                        logger.debug("We have found "+actives.size()+" active workflow(s) for node : " +node.getPath());
                    }
                }
                for (Workflow workflow : actives) {
                    for (WorkflowAction workflowAction : workflow.getAvailableActions()) {
                        if (workflowAction instanceof WorkflowTask) {
                            WorkflowTask workflowTask = (WorkflowTask) workflowAction;
                            List<WorkflowParticipation> participations = workflowTask.getParticipations();
                            if (participations != null) {
                                for (WorkflowParticipation participation : participations) {
                                    JahiaPrincipal principal = participation.getJahiaPrincipal();
                                    if (principal instanceof JahiaGroup) {
                                        JCRGroupNode groupNode = JahiaGroupManagerService.getInstance().lookupGroupByPath(principal.getLocalPath());
                                        JCRUserNode userNode = JahiaUserManagerService.getInstance().lookupUserByPath(getUser().getLocalPath());
                                        if (groupNode != null && userNode != null && groupNode.isMember(userNode)) {
                                            tasks.add(workflowTask);
                                            break;
                                        }
                                    }
                                    if (principal instanceof JahiaUser && principal.getLocalPath().equals(getUser().getLocalPath())) {
                                        tasks.add(workflowTask);
                                        break;
                                    }
                                }
                            } else {
                                logger.error("There is no possible participants for workflow task id "+workflowTask.getId()+" ("+workflowTask.getDescription()+")");
                            }
                        }
                    }
                }
                if(logger.isDebugEnabled()){
                    if(tasks.isEmpty()){
                        logger.debug("Could not find any tasks for user "+getUser().getName()+" on node : " +node.getPath());
                    } else {
                        logger.debug("We have found "+tasks.size()+" tasks to do for user "+getUser().getName()+" on node : " +node.getPath());
                    }
                }
            } else if (user != null) {
                tasks = WorkflowService.getInstance().getTasksForUser(user, locale != null ? locale : getUILocale());
                if(logger.isDebugEnabled()){
                    if(tasks.isEmpty()){
                        logger.debug("Could not find any tasks for user "+getUser().getName());
                    } else {
                        logger.debug("We have found "+tasks.size()+" tasks to do for user "+getUser().getName());
                    }
                }
            }

            pageContext.setAttribute(var, tasks, scope);
            node = null;
            var = null;
            scope = PageContext.PAGE_SCOPE;
            return super.doEndTag();
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
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

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}