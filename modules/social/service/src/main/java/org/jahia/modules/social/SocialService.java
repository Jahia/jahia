package org.jahia.modules.social;

import org.apache.log4j.Logger;
import org.drools.spi.KnowledgeHelper;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.content.rules.AddedNodeFact;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaLDAPUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.security.Principal;
import java.util.*;

/**
 * Social service class for manipulating social activities from the
 * right-hand-side (consequences) of rules.
 *
 * @author Serge Huber
 */
public class SocialService {

    private static Logger logger = Logger.getLogger(SocialService.class);
    public static final String JNT_SOCIAL_ACTIVITY = "jnt:socialActivity";
    public static final String JNT_SOCIAL_MESSAGE = "jnt:socialMessage";
    public static final String JNT_SOCIAL_CONNECTION = "jnt:socialConnection";

    /* Rules Consequence implementations */

    public void addActivity(final String activityType, final String user, final String messageKey, final AddedNodeFact nodeFact, final List<String> nodeTypeList, KnowledgeHelper drools) throws RepositoryException {
        final JCRUser fromJCRUser = getJCRUserFromUserKey(user);
        if (fromJCRUser == null) {
            logger.warn("No user found, not adding activity !");
            return;
        }
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper userNode = fromJCRUser.getNode(session);

                JCRNodeWrapper activitiesNode = null;
                try {
                    activitiesNode = userNode.getNode("activities");
                    session.checkout(activitiesNode);
                } catch (PathNotFoundException pnfe) {
                    session.checkout(userNode);
                    activitiesNode = userNode.addNode("activities", "jnt:contentList");
                }
                String nodeType = JNT_SOCIAL_ACTIVITY;
                String nodeName = nodeType.substring(nodeType.lastIndexOf(":") + 1);

                nodeName = JCRContentUtils.findAvailableNodeName(activitiesNode, nodeName);

                JCRNodeWrapper activityNode = activitiesNode.addNode(nodeName, JNT_SOCIAL_ACTIVITY);
                activityNode.setProperty("j:from", userNode);
                activityNode.setProperty("j:messageKey", messageKey);
                activityNode.setProperty("j:targetNode", nodeFact.getNode());
                String[] targetNodeTypes = nodeTypeList.toArray(new String[nodeTypeList.size()]);
                activityNode.setProperty("j:targetNodeTypes", targetNodeTypes);
                activityNode.setProperty("j:type", activityType);
                session.save();
                return true;
            }
        });
    }

    public void sendMessage(final String fromUser, final String toUser, final String subject, final String message, AddedNodeFact nodeFact, KnowledgeHelper drools) throws RepositoryException {
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
                JCRNodeWrapper destinationInboxNode = JCRContentUtils.getOrAddPath(session, toUser, "messages/inbox", Constants.JAHIANT_CONTENTLIST);
                String destinationInboxNodeName = JCRContentUtils.findAvailableNodeName(destinationInboxNode, fromUser.getName() + "_to_" + toUser.getName());
                JCRNodeWrapper destinationMessageNode = destinationInboxNode.addNode(destinationInboxNodeName, JNT_SOCIAL_MESSAGE);
                destinationMessageNode.setProperty("j:from", fromUser);
                destinationMessageNode.setProperty("j:to", toUser);
                destinationMessageNode.setProperty("j:subject", subject);
                destinationMessageNode.setProperty("j:body", body);
                destinationMessageNode.setProperty("j:read", false);

                JCRNodeWrapper sentMessagesBoxNode = JCRContentUtils.getOrAddPath(session, toUser, "messages/sent", Constants.JAHIANT_CONTENTLIST);
                String sentMessagesBoxNodeName = JCRContentUtils.findAvailableNodeName(sentMessagesBoxNode, fromUser.getName() + "_to_" + toUser.getName());
                JCRNodeWrapper sentMessageNode = sentMessagesBoxNode.addNode(sentMessagesBoxNodeName, JNT_SOCIAL_MESSAGE);
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

    public SortedSet<JCRNodeWrapper> getActivities(JCRSessionWrapper jcrSessionWrapper, JCRNodeWrapper node, long limit, long offset, String pathFilter) throws RepositoryException {
        Set<String> userPaths = getUserConnections(jcrSessionWrapper, node, true);
        return getActivities(jcrSessionWrapper, userPaths, limit, offset, pathFilter);
    }

    public Set<String> getACLConnections(JCRSessionWrapper jcrSessionWrapper, JCRNodeWrapper targetNode) throws RepositoryException {
        Set<String> userPaths = new HashSet<String>();

        Map<String, List<String[]>> aclEntries = targetNode.getAclEntries();
        for (Map.Entry<String, List<String[]>> curEntry : aclEntries.entrySet()) {
            String curPrincipal = curEntry.getKey();
            logger.debug("Resolving principal " + curPrincipal);
            String[] principalParts = curPrincipal.split(":");
            if ("u".equals(principalParts[0])) {
                JCRUser jcrUser = getJCRUserFromUserKey(principalParts[1]);
                userPaths.add(jcrUser.getNode(jcrSessionWrapper).getPath());
            } else if ("g".equals(principalParts[0])) {
                JahiaGroupManagerService groupManager = (JahiaGroupManagerService) SpringContextSingleton.getInstance().getContext().getBean("JahiaGroupManagerService");
                JahiaGroup group = groupManager.lookupGroup(principalParts[1]);
                Set<Principal> recursiveGroupMembers = group.getRecursiveUserMembers();
                for (Principal groupMember : recursiveGroupMembers) {
                    JCRUser jcrUser = getJCRUserFromUserKey(groupMember.getName());
                    userPaths.add(jcrUser.getNode(jcrSessionWrapper).getPath());                    
                }
            }
        }

        return userPaths;
    }

    public Set<String> getUserConnections(JCRSessionWrapper jcrSessionWrapper, JCRNodeWrapper userNode, boolean includeSelf) throws RepositoryException {
        QueryManager queryManager = jcrSessionWrapper.getWorkspace().getQueryManager();

        Set<String> userPaths = new HashSet<String>();

        if (includeSelf) {
            userPaths.add(userNode.getPath());
        }

        Query myConnectionsQuery = queryManager.createQuery("select * from ["+JNT_SOCIAL_CONNECTION+"] as uC where isdescendantnode(uC,['"+ userNode.getPath()+"'])", Query.JCR_SQL2);
        QueryResult myConnectionsResult = myConnectionsQuery.execute();

        NodeIterator myConnectionsIterator = myConnectionsResult.getNodes();
        while (myConnectionsIterator.hasNext()) {
            JCRNodeWrapper myConnectionNode = (JCRNodeWrapper) myConnectionsIterator.nextNode();
            JCRNodeWrapper connectedToNode = (JCRNodeWrapper) myConnectionNode.getProperty("j:connectedTo").getNode();
            userPaths.add(connectedToNode.getPath());
        }
        return userPaths;
    }


    public SortedSet<JCRNodeWrapper> getActivities(JCRSessionWrapper jcrSessionWrapper, Set<String> paths, long limit, long offset, String pathFilter) throws RepositoryException {
        SortedSet<JCRNodeWrapper> activitiesSet = new TreeSet<JCRNodeWrapper>(new Comparator<JCRNodeWrapper>() {

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

        /* todo here it would be better to do a query on all the paths, but it might be very slow. This would also solve the limit and offset problem */
        QueryManager queryManager = jcrSessionWrapper.getWorkspace().getQueryManager();
        for (String currentPath : paths) {
            Query activitiesQuery = queryManager.createQuery("select * from ["+JNT_SOCIAL_ACTIVITY+"] as uA where isdescendantnode(uA,['"+currentPath+"']) order by [jcr:created] desc", Query.JCR_SQL2);
            /* todo this usage of offset and limit is not really correct, we should perform this on the final aggregated list */
            activitiesQuery.setLimit(limit);
            activitiesQuery.setOffset(offset);
            QueryResult activitiesResult = activitiesQuery.execute();

            NodeIterator activitiesIterator = activitiesResult.getNodes();
            while (activitiesIterator.hasNext()) {
                JCRNodeWrapper activitiesNode = (JCRNodeWrapper) activitiesIterator.nextNode();
                if (pathFilter != null) {
                    /* todo maybe we could filter this using the JCR-SQL2 request directly ? */
                    try {
                        JCRPropertyWrapper targetNodeProperty = activitiesNode.getProperty("j:targetNode");
                        if (targetNodeProperty != null) {
                            String targetNodePath = targetNodeProperty.getNode().getPath();
                            if (targetNodePath.startsWith(pathFilter)) {
                                activitiesSet.add(activitiesNode);
                            }
                        }
                    } catch (PathNotFoundException pnfe) {
                        // we couldn't find the property, that's an acceptable situation.
                    }
                } else {
                    activitiesSet.add(activitiesNode);
                }
            }
        }
        return activitiesSet;
    }

    public void removeSocialConnection(JCRSessionWrapper jcrSessionWrapper, String fromId, String toId, String connectionType) throws RepositoryException {
        QueryManager queryManager = jcrSessionWrapper.getWorkspace().getQueryManager();

        // first we look for the first connection.
        Query connectionQuery = queryManager.createQuery("select * from ["+JNT_SOCIAL_CONNECTION+"] where [j:connectedFrom]='"+fromId+"' and [j:connectedTo]='"+toId+"' and [j:type]='"+connectionType+"'" , Query.JCR_SQL2);
        QueryResult connectionResult = connectionQuery.execute();
        NodeIterator connectionIterator = connectionResult.getNodes();
        while (connectionIterator.hasNext()) {
            Node connectionNode = connectionIterator.nextNode();
            jcrSessionWrapper.checkout(connectionNode.getParent());
            connectionNode.remove();
        }

        // now let's remove the reverse connection.
        Query reverseConnectionQuery = queryManager.createQuery("select * from ["+JNT_SOCIAL_CONNECTION+"] where [j:connectedFrom]='"+toId+"' and [j:connectedTo]='"+fromId+"' and [j:type]='"+connectionType+"'" , Query.JCR_SQL2);
        QueryResult reverseConnectionResult = reverseConnectionQuery.execute();
        NodeIterator reverseConnectionIterator = reverseConnectionResult.getNodes();
        while (reverseConnectionIterator.hasNext()) {
            Node connectionNode = reverseConnectionIterator.nextNode();
            jcrSessionWrapper.checkout(connectionNode.getParent());
            connectionNode.remove();
        }

        jcrSessionWrapper.save();
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
