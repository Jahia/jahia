package org.jahia.services.workflow.jbpm;

import org.apache.log4j.Logger;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.modules.social.SocialService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.usermanager.JahiaLDAPUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.services.workflow.WorkflowVariable;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.List;
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

    private transient static Logger logger = Logger.getLogger(ConnectUsers.class);

    private static final long serialVersionUID = 1L;

    public void execute(ActivityExecution execution) throws Exception {
        final String leftUserIdentifier = (String) execution.getVariable("nodeId");
        String workspace = (String) execution.getVariable("workspace");
        Locale locale = (Locale) execution.getVariable("locale");
        List<WorkflowVariable> userKeyList = (List<WorkflowVariable>) execution.getVariable("userkey");
        JCRUser jcrUser = null;
        if (userKeyList  != null && userKeyList.size() == 1) {
            String userKey = userKeyList.get(0).getValue();
            JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userKey);
            if (jahiaUser instanceof JahiaLDAPUser) {
                JCRUserManagerProvider userManager = (JCRUserManagerProvider) SpringContextSingleton.getInstance().getContext().getBean("JCRUserManagerProvider");
                jcrUser = (JCRUser) userManager.lookupExternalUser(jahiaUser.getName());
            } else if (jahiaUser instanceof JCRUser) {
                jcrUser = (JCRUser) jahiaUser;
            } else {
                logger.error("Can't handle user of type " + jahiaUser.getClass().getName() + ", will not connect the users.");
                return;
            }
        }
        final String rightUserIdentifier = jcrUser.getIdentifier();

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {

                JCRNodeWrapper leftUser = session.getNodeByUUID(leftUserIdentifier);
                JCRNodeWrapper rightUser = session.getNodeByUUID(rightUserIdentifier);
                // now let's connect this user's node to the target node.

                JCRNodeWrapper leftConnectionsNode = null;
                try {
                    leftConnectionsNode = leftUser.getNode("connections");
                    session.checkout(leftConnectionsNode);
                } catch (PathNotFoundException pnfe) {
                    session.checkout(leftUser);
                    leftConnectionsNode = leftUser.addNode("connections", "jnt:contentList");
                }
                JCRNodeWrapper leftUserConnection = leftConnectionsNode.addNode(leftUser.getName() + "-" + rightUser.getName(), SocialService.JNT_SOCIAL_CONNECTION);
                leftUserConnection.setProperty("j:connectedFrom", leftUser);
                leftUserConnection.setProperty("j:connectedTo", rightUser);
                leftUserConnection.setProperty("j:type", "collegue");

                // now let's do the connection in the other direction.
                JCRNodeWrapper rightConnectionsNode = null;
                try {
                    rightConnectionsNode = rightUser.getNode("connections");
                    session.checkout(rightConnectionsNode);
                } catch (PathNotFoundException pnfe) {
                    session.checkout(rightUser);
                    rightConnectionsNode = rightUser.addNode("connections", "jnt:contentList");
                }
                JCRNodeWrapper rightUserConnection = rightConnectionsNode.addNode(rightUser.getName() + "-" + leftUser.getName(), SocialService.JNT_SOCIAL_CONNECTION);
                rightUserConnection.setProperty("j:connectedFrom", rightUser);
                rightUserConnection.setProperty("j:connectedTo", leftUser);
                rightUserConnection.setProperty("j:type", "collegue");

                session.save();
                return true;
            }
        });
        execution.takeDefaultTransition();
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    }

}
