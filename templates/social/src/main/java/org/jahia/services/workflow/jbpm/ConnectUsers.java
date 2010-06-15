package org.jahia.services.workflow.jbpm;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import javax.jcr.PathNotFoundException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Jun 15, 2010
 * Time: 2:14:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectUsers implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;

    public void execute(ActivityExecution execution) throws Exception {
        String id = (String) execution.getVariable("nodeId");
        String workspace = (String) execution.getVariable("workspace");
        Locale locale = (Locale) execution.getVariable("locale");
        String toPath = (String) execution.getVariable("to");

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRNodeWrapper fromUser = session.getNodeByUUID(id);
        JCRNodeWrapper toUser = session.getNode(toPath);
        // now let's connect this user's node to the target node.

        JCRNodeWrapper connectionsNode = null;
        try {
            connectionsNode = fromUser.getNode("connections");
            session.checkout(connectionsNode);
        } catch (PathNotFoundException pnfe) {
            session.checkout(fromUser);
            connectionsNode = fromUser.addNode("connections", "jnt:contentList");
        }
        JCRNodeWrapper userConnection = connectionsNode.addNode(fromUser.getName() + "-" + toUser.getName(), "jnt:userConnection");
        userConnection.setProperty("connectedFrom", fromUser);
        userConnection.setProperty("connectedTo", toUser);
        userConnection.setProperty("type", "collegue");
        session.save();
        execution.takeDefaultTransition();
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    }

}
