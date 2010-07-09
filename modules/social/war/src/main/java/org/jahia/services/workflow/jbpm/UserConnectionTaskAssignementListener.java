package org.jahia.services.workflow.jbpm;

import org.apache.commons.lang.StringUtils;
import org.jahia.registries.ServicesRegistry;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.api.task.Assignable;
import org.jbpm.api.task.AssignmentHandler;

/**
 * Assignment handler for user connection task.
 * 
 * @author Serge Huber
 */
public class UserConnectionTaskAssignementListener implements AssignmentHandler {
    
    private static final long serialVersionUID = 3356236148908996978L;

    /**
     * sets the actorId and candidates for the given task.
     */
    public void assign(Assignable assignable, OpenExecution execution) throws Exception {

        String to = (String) execution.getVariable("to");
        if (StringUtils.isNotEmpty(to)) {
            assignable.addCandidateUser(to);
        }
        assignable.addCandidateGroup(ServicesRegistry.getInstance().getJahiaGroupManagerService()
                .getAdministratorGroup(0).getGroupKey());
    }
}
