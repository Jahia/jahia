package org.jahia.services.workflow.jbpm;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.usermanager.JahiaLDAPUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.services.workflow.WorkflowVariable;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import javax.jcr.PathNotFoundException;
import java.util.Collections;
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
        String id = (String) execution.getVariable("nodeId");
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

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRNodeWrapper fromUser = session.getNodeByUUID(id);
        JCRNodeWrapper toUser = session.getNodeByUUID(jcrUser.getIdentifier());
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
