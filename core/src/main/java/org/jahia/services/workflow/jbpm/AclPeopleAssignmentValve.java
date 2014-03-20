/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.services.workflow.jbpm;

import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.jbpm.custom.JahiaLocalHTWorkItemHandler;
import org.jbpm.services.task.impl.model.GroupImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.Task;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

/**
 * A valved used at task creation to assign people to the task based on the permissions on the corresponding
 * JCR content node.
 */
public class AclPeopleAssignmentValve extends AbstractPeopleAssignmentValve {
    @Override
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {

        JBPM6WorkflowProvider provider = (JBPM6WorkflowProvider) valveContext.getEnvironment().get(ENV_JBPM_WORKFLOW_PROVIDER);

        JahiaLocalHTWorkItemHandler.PeopleAssignmentContext peopleAssignmentContext = (JahiaLocalHTWorkItemHandler.PeopleAssignmentContext) context;
        Task task = peopleAssignmentContext.getTask();
        org.kie.api.definition.process.Process process = peopleAssignmentContext.getKieSession().getKieBase().getProcess(task.getTaskData().getProcessId());

        WorkflowDefinition def = new WorkflowDefinition(process.getName(), process.getName(), provider.getKey());
        try {
            String name = task.getNames().get(0).getText();

            PeopleAssignments peopleAssignments = task.getPeopleAssignments();
            List<OrganizationalEntity> potentialOwners = new ArrayList<OrganizationalEntity>();
            final List<JahiaPrincipal> principals = WorkflowService.getInstance().getAssignedRole(def, name, Long.toString(task.getTaskData().getProcessInstanceId()));
            for (JahiaPrincipal principal : principals) {
                if (principal instanceof JahiaGroup) {
                    potentialOwners.add(new GroupImpl(((JahiaGroup) principal).getGroupKey()));
                } else if (principal instanceof JahiaUser) {
                    potentialOwners.add(new UserImpl(((JahiaUser) principal).getUserKey()));
                }
            }
            final GroupImpl admin = new GroupImpl(ServicesRegistry.getInstance().getJahiaGroupManagerService().getAdministratorGroup(null).getGroupKey());
            potentialOwners.add(admin);
            peopleAssignments.getPotentialOwners().addAll(potentialOwners);

            List<OrganizationalEntity> administrators = new ArrayList<OrganizationalEntity>();
            administrators.add(admin);
            peopleAssignments.getBusinessAdministrators().addAll(administrators);
        } catch (RepositoryException e) {
            throw new RuntimeException("Error while setting up task assignees and creating a JCR task", e);
        }

        valveContext.invokeNext(context);
    }

    @Override
    public void initialize() {
    }
}
