/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
