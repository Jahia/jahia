package org.jahia.modules.social;

import org.apache.log4j.Logger;
import org.drools.spi.KnowledgeHelper;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.usermanager.JahiaLDAPUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Social service class for manipulating social activities from the
 * right-hand-side (consequences) of rules.
 *
 * @author Serge Huber
 */
public class SocialService {

    private static Logger logger = Logger.getLogger(SocialService.class);

    /* Rules Consequence implementations */

    public void addActivity(final String user, final String message, KnowledgeHelper drools) throws RepositoryException {
        final JCRUser fromJCRUser = getJCRUserFromUserKey(user);
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper userNode = fromJCRUser.getNode(session);

                JCRNodeWrapper activitiesNode = null;
                try {
                    activitiesNode = userNode.getNode("activities");
                } catch (PathNotFoundException pnfe) {
                    activitiesNode = userNode.addNode("activities", "jnt:contentList");
                }
                String nodeType = "jnt:userActivity";
                String nodeName = nodeType.substring(nodeType.lastIndexOf(":") + 1);

                nodeName = JCRContentUtils.findAvailableNodeName(activitiesNode, nodeName);

                JCRNodeWrapper activityNode = activitiesNode.addNode(nodeName, "jnt:userActivity");
                activityNode.setProperty("j:from", userNode);
                activityNode.setProperty("j:message", message);

                session.save();
                return true;
            }
        });
    }

    public void sendMessage(final String fromUser, final String toUser, final String subject, final String message, KnowledgeHelper drools) throws RepositoryException {
        JCRUser fromJCRUser = getJCRUserFromUserKey(fromUser);
        if (fromJCRUser == null) {
            logger.warn("Couldn't find from user "+fromUser+" , aborting message sending...");
            return;
        }
        JCRUser toJCRUser = getJCRUserFromUserKey(toUser);
        if (toJCRUser == null) {
            logger.warn("Couldn't find to user "+toUser+" , aborting message sending...");
            return;
        }
        sendMessage(fromJCRUser.getIdentifier(), toJCRUser.getIdentifier(), subject, message);
    }

    /* General API */

    public boolean sendMessage(JCRNodeWrapper fromUserNode, String toUserKey, final String subject, final String body) throws RepositoryException {
        JCRUser jcrUser = getJCRUserFromUserKey(toUserKey);

        if (jcrUser == null) {
            return false;
        }

        final String fromUserIdentifier = fromUserNode.getIdentifier();
        final String toUserIdentifier = jcrUser.getIdentifier();

        sendMessage(fromUserIdentifier, toUserIdentifier, subject, body);
        return true;
    }

    private void sendMessage(final String fromUserIdentifier, final String toUserIdentifier, final String subject, final String body) throws RepositoryException {
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
                String destinationInboxNodeName = JCRContentUtils.findAvailableNodeName(destinationInboxNode, fromUser.getName() + "_to_" + toUser.getName());
                JCRNodeWrapper destinationMessageNode = destinationInboxNode.addNode(destinationInboxNodeName, "jnt:userMessage");
                destinationMessageNode.setProperty("j:from", fromUser);
                destinationMessageNode.setProperty("j:to", toUser);
                destinationMessageNode.setProperty("j:subject", subject);
                destinationMessageNode.setProperty("j:body", body);
                destinationMessageNode.setProperty("j:read", false);

                JCRNodeWrapper sentMessagesBoxNode = null;
                try {
                    sentMessagesBoxNode = fromUser.getNode("sentMessages");
                    session.checkout(sentMessagesBoxNode);
                } catch (PathNotFoundException pnfe) {
                    session.checkout(fromUser);
                    sentMessagesBoxNode = fromUser.addNode("sentMessages", "jnt:contentList");
                }
                String sentMessagesBoxNodeName = JCRContentUtils.findAvailableNodeName(sentMessagesBoxNode, fromUser.getName() + "_to_" + toUser.getName());
                JCRNodeWrapper sentMessageNode = sentMessagesBoxNode.addNode(sentMessagesBoxNodeName, "jnt:userMessage");
                sentMessageNode.setProperty("j:from", fromUser);
                sentMessageNode.setProperty("j:to", toUser);
                sentMessageNode.setProperty("j:subject", subject);
                sentMessageNode.setProperty("j:body", body);
                sentMessageNode.setProperty("j:read", false);

                session.save();
                return true;
            }
        });
    }

    public SortedSet<JCRNodeWrapper> getActivities(JCRSessionWrapper jcrSessionWrapper, JCRNodeWrapper node) throws RepositoryException {
        QueryManager queryManager = jcrSessionWrapper.getWorkspace().getQueryManager();

        Query myActivitiesQuery = queryManager.createQuery("select * from [jnt:userActivity] as uA where isdescendantnode(uA,['"+node.getPath()+"']) order by [jcr:created] desc", Query.JCR_SQL2);
        myActivitiesQuery.setLimit(100);
        QueryResult myActivitiesResult = myActivitiesQuery.execute();

        NodeIterator myActivitiesNodeIterator = myActivitiesResult.getNodes();

        SortedSet<JCRNodeWrapper> activitiesSet = new TreeSet(new Comparator<JCRNodeWrapper>() {

            public int compare(JCRNodeWrapper activityNode1, JCRNodeWrapper activityNode2) {
                try {
                    // we invert the order to sort with most recent dates on top.
                    return activityNode2.getProperty("jcr:created").getDate().compareTo(activityNode1.getProperty("jcr:created").getDate());
                } catch (RepositoryException e) {
                    logger.error("Error while comparing creation date on two activities, returning them as equal", e);
                    return 0;
                }
            }

        });

        while (myActivitiesNodeIterator.hasNext()) {
            activitiesSet.add((JCRNodeWrapper) myActivitiesNodeIterator.nextNode());
        }

        Query myConnectionsQuery = queryManager.createQuery("select * from [jnt:userConnection] as uC where isdescendantnode(uC,['"+node.getPath()+"'])", Query.JCR_SQL2);
        QueryResult myConnectionsResult = myConnectionsQuery.execute();

        NodeIterator myConnectionsIterator = myConnectionsResult.getNodes();
        while (myConnectionsIterator.hasNext()) {
            JCRNodeWrapper myConnectionNode = (JCRNodeWrapper) myConnectionsIterator.nextNode();
            JCRNodeWrapper connectedToNode = (JCRNodeWrapper) myConnectionNode.getProperty("j:connectedTo").getNode();
            Query myConnectionActivitiesQuery = queryManager.createQuery("select * from [jnt:userActivity] as uA where isdescendantnode(uA,['"+connectedToNode.getPath()+"']) order by [jcr:created] desc", Query.JCR_SQL2);
            myConnectionActivitiesQuery.setLimit(100);
            QueryResult myConnectionActivitiesResult = myConnectionActivitiesQuery.execute();

            NodeIterator myConnectionActivitiesIterator = myConnectionActivitiesResult.getNodes();
            while (myConnectionActivitiesIterator.hasNext()) {
                activitiesSet.add((JCRNodeWrapper) myConnectionActivitiesIterator.nextNode());
            }
        }
        return activitiesSet;
    }

    private JCRUser getJCRUserFromUserKey(String userKey) {
        JCRUser jcrUser = null;

        JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userKey);
        if (jahiaUser == null) {
            logger.error ("Couldn't lookup user with userKey" + userKey);
            return null;
        }
        if (jahiaUser instanceof JahiaLDAPUser) {
            JCRUserManagerProvider userManager = (JCRUserManagerProvider) SpringContextSingleton.getInstance().getContext().getBean("JCRUserManagerProvider");
            jcrUser = (JCRUser) userManager.lookupExternalUser(jahiaUser.getName());
        } else if (jahiaUser instanceof JCRUser) {
            jcrUser = (JCRUser) jahiaUser;
        } else {
            logger.error("Can't handle user of type " + jahiaUser.getClass().getName() + ", will not send a message users.");
            return null;
        }

        return jcrUser;
    }

}
