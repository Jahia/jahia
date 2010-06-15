package org.jahia.services.workflow.jbpm;

import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorkflowVariable;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.api.task.Assignable;
import org.jbpm.api.task.AssignmentHandler;
import org.jbpm.pvm.internal.task.TaskImpl;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Jun 15, 2010
 * Time: 2:11:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserConnectionTaskAssignementListener implements AssignmentHandler {
    private transient static Logger logger = Logger.getLogger(JBPMTaskAssignmentListener.class);

    /**
     * sets the actorId and candidates for the given task.
     */
    public void assign(Assignable assignable, OpenExecution execution) throws Exception {

        WorkflowDefinition def = (WorkflowDefinition) execution.getVariable("workflow");
        String id = (String) execution.getVariable("nodeId");
        List<WorkflowVariable> userKeyList = (List<WorkflowVariable>) execution.getVariable("userkey");
        if (userKeyList  != null && userKeyList.size() == 1) {
            String userKey = userKeyList.get(0).getValue();
            JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(id);
            String name = null;
            if (assignable instanceof TaskImpl) {
                name = ((TaskImpl)assignable).getActivityName();
            }
            assignable.addCandidateUser(userKey);
        }
        assignable.addCandidateGroup(ServicesRegistry.getInstance().getJahiaGroupManagerService().getAdministratorGroup(0).getGroupKey());
    }
}
