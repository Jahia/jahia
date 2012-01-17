/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.workflow.jbpm;

import org.slf4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.api.task.Assignable;
import org.jbpm.api.task.AssignmentHandler;
import org.jbpm.pvm.internal.task.TaskImpl;

import java.util.List;


/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 4 févr. 2010
 */
public class JBPMTaskAssignmentListener implements AssignmentHandler {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JBPMTaskAssignmentListener.class);

    /**
     * sets the actorId and candidates for the given task.
     */
    public void assign(Assignable assignable, OpenExecution execution) throws Exception {

        WorkflowDefinition def = (WorkflowDefinition) execution.getVariable("workflow");
        String id = (String) execution.getVariable("nodeId");
        JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(id);
        String name = null;
        if (assignable instanceof TaskImpl) {
            name = ((TaskImpl)assignable).getActivityName();
        }
        List<JahiaPrincipal> principals = WorkflowService.getInstance().getAssignedRole(node, def, name, execution.getProcessInstance().getId());
        for (JahiaPrincipal principal : principals) {
            if (principal instanceof JahiaGroup) {
                assignable.addCandidateGroup(((JahiaGroup)principal).getGroupKey());
            } else if (principal instanceof JahiaUser) {
                assignable.addCandidateUser(((JahiaUser)principal).getUserKey());
            }
        }
        assignable.addCandidateGroup(ServicesRegistry.getInstance().getJahiaGroupManagerService().getAdministratorGroup(0).getGroupKey());
    }
}
