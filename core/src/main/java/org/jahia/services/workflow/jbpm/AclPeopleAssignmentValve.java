package org.jahia.services.workflow.jbpm;

import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.jbpm.custom.JahiaLocalHTWorkItemHandler;
import org.jbpm.services.task.impl.model.GroupImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.runtime.process.WorkItem;
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
        WorkItem workItem = peopleAssignmentContext.getWorkItem();
//        String nodeId = (String) workItem.getParameter("nodeId");
        WorkflowDefinition def = provider.getWorkflowDefinitionById(task.getTaskData().getProcessId(), null);
        JCRNodeWrapper node = null;
        try {
//            node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(nodeId);
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
            List<OrganizationalEntity> administrators = new ArrayList<OrganizationalEntity>();
            administrators.add(new GroupImpl(ServicesRegistry.getInstance().getJahiaGroupManagerService().getAdministratorGroup(null).getGroupKey()));
            peopleAssignments.getBusinessAdministrators().addAll(administrators);
            peopleAssignments.getPotentialOwners().addAll(potentialOwners);
        } catch (RepositoryException e) {
            throw new RuntimeException("Error while setting up task assignees and creating a JCR task", e);
        }

        valveContext.invokeNext(context);
    }

    @Override
    public void initialize() {
    }
}
