package org.jahia.services.workflow.jbpm.custom;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 6:47:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class Publish implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    
    public void execute(ActivityExecution execution) throws Exception {
        String id = (String) execution.getVariable("nodeId");
        JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(id);
        JCRPublicationService.getInstance().publish(node.getPath(), "default","live",null, false, false);
        execution.takeDefaultTransition();
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
        System.out.println("-- signal publish : " + signalName);
    }

}
