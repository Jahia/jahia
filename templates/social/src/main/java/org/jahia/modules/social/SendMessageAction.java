package org.jahia.modules.social;

import org.apache.log4j.Logger;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.usermanager.JahiaLDAPUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.json.JSONObject;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Action to send a message from one user to another.
 *
 * @author loom
 *         Date: Jun 22, 2010
 *         Time: 9:53:53 AM
 */
public class SendMessageAction implements Action {

    private static Logger logger = Logger.getLogger(SendMessageAction.class);

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {

        JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(
                resource.getWorkspace(), resource.getLocale());

        final JCRNodeWrapper node = resource.getNode();

        final String toUserKey = req.getParameter("j:to");
        final String subject = req.getParameter("j:subject");
        final String body = req.getParameter("j:body");

        JCRUser jcrUser = null;

        JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(toUserKey);
        if (jahiaUser instanceof JahiaLDAPUser) {
            JCRUserManagerProvider userManager = (JCRUserManagerProvider) SpringContextSingleton.getInstance().getContext().getBean("JCRUserManagerProvider");
            jcrUser = (JCRUser) userManager.lookupExternalUser(jahiaUser.getName());
        } else if (jahiaUser instanceof JCRUser) {
            jcrUser = (JCRUser) jahiaUser;
        } else {
            logger.error("Can't handle user of type " + jahiaUser.getClass().getName() + ", will not send a message users.");
            return new ActionResult(HttpServletResponse.SC_BAD_REQUEST, null, null);
        }

        final String fromUserIdentifier = node.getIdentifier();
        final String toUserIdentifier = jcrUser.getIdentifier();

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {

                JCRNodeWrapper fromUser = session.getNodeByUUID(fromUserIdentifier);
                JCRNodeWrapper toUser = session.getNodeByUUID(toUserIdentifier);
                // now let's connect this user's node to the target node.

                // now let's do the connection in the other direction.
                JCRNodeWrapper destinationInboxNode = null;
                try {
                    destinationInboxNode = toUser.getNode("inboundMessages");
                    session.checkout(destinationInboxNode);
                } catch (PathNotFoundException pnfe) {
                    session.checkout(toUser);
                    destinationInboxNode = toUser.addNode("inboundMessages", "jnt:contentList");
                }
                JCRNodeWrapper sendMessageNode = destinationInboxNode.addNode(fromUser.getName() + "to" + toUser.getName(), "jnt:userMessage");
                sendMessageNode.setProperty("j:from", fromUser);
                sendMessageNode.setProperty("j:to", toUser);
                sendMessageNode.setProperty("j:subject", subject);
                sendMessageNode.setProperty("j:body", body);
                sendMessageNode.setProperty("j:read", false);

                JCRNodeWrapper sentMessagesBoxNode = null;
                try {
                    sentMessagesBoxNode = fromUser.getNode("sentMessages");
                    session.checkout(sentMessagesBoxNode);
                } catch (PathNotFoundException pnfe) {
                    session.checkout(fromUser);
                    sentMessagesBoxNode = fromUser.addNode("sentMessages", "jnt:contentList");
                }
                JCRNodeWrapper destinationMessageNode = sentMessagesBoxNode.addNode(fromUser.getName() + "to" + toUser.getName(), "jnt:userMessage");
                destinationMessageNode.setProperty("j:from", fromUser);
                destinationMessageNode.setProperty("j:to", toUser);
                destinationMessageNode.setProperty("j:subject", subject);
                destinationMessageNode.setProperty("j:body", body);
                destinationMessageNode.setProperty("j:read", false);

                session.save();
                return true;
            }
        });

        /* TODO to be implemented */
        JSONObject results = new JSONObject();

        return new ActionResult(HttpServletResponse.SC_OK, null, results);
    }
}