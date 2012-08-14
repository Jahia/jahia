package org.jahia.services.content;

import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.rules.AddedNodeFact;
import org.slf4j.Logger;

import javax.jcr.*;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AclListener extends DefaultEventListener {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(AclListener.class);

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED +
                Event.PROPERTY_REMOVED;
    }

    public void onEvent(EventIterator events) {
        JCRSessionWrapper session = ((JCREventIterator) events).getSession();
        Set<String> aceIdentifiers = new HashSet<String>();
        try {
            while (events.hasNext()) {
                Event next = events.nextEvent();
                if (next.getPath().contains("/j:acl/")) {
                    if (next.getType() == Event.PROPERTY_ADDED || next.getType() == Event.PROPERTY_CHANGED) {
                        JCRNodeWrapper nodeByIdentifier = session.getNodeByIdentifier(next.getIdentifier());
                        if (nodeByIdentifier.isNodeType("jnt:ace") && !nodeByIdentifier.isNodeType("jnt:externalAce") && nodeByIdentifier.getProperty("j:aceType").getValue().getString().equals("GRANT")) {
                            aceIdentifiers.add(next.getIdentifier());
                        }
                    } else if (next.getType() == Event.NODE_REMOVED) {
                        aceIdentifiers.add(next.getIdentifier());
                    }
                }
            }

            for (String aceIdentifier : aceIdentifiers) {
                Set<String> roles = new HashSet<String>();

                JCRNodeWrapper ace = null;
                String principal = null;
                try {
                    ace = session.getNodeByIdentifier(aceIdentifier);
                    principal = ace.getProperty("j:principal").getString();
                    if (ace.hasProperty("j:roles")) {
                        Value[] vals = ace.getProperty("j:roles").getValues();
                        for (Value val : vals) {
                            roles.add(val.getString());
                        }
                    } else {
                        logger.warn("Missing roles property for acl on " + ace.getPath());
                    }
                } catch (ItemNotFoundException e) {
                }

                QueryManager q = session.getWorkspace().getQueryManager();
                String sql = "select * from [jnt:externalAce] as ace where ace.[j:sourceAce] = '"+aceIdentifier+"'";
                QueryResult qr = q.createQuery(sql, Query.JCR_SQL2).execute();
                NodeIterator ni = qr.getNodes();
                while (ni.hasNext()) {
                    JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();
                    String role = n.getProperty("j:roles").getValues()[0].getString();
                    if (!roles.contains(role)) {
                        List<Value> newVals = new ArrayList<Value>();
                        for (Value value : n.getProperty("j:sourceAce").getValues()) {
                            if (!value.getString().equals(aceIdentifier)) {
                                newVals.add(value);
                            }
                        }
                        if (newVals.size() == 0) {
                            n.remove();
                        } else {
                            n.setProperty("j:sourceAce", newVals.toArray(new Value[newVals.size()]));
                        }
                    }
                }

                for (String role : roles) {
                    if (session.itemExists("/roles/"+role)) {
                        JCRNodeWrapper roleNode = session.getNode("/roles/"+role);
                        NodeIterator r = roleNode.getNodes();
                        while (r.hasNext()) {
                            JCRNodeWrapper externalPermissions = (JCRNodeWrapper) r.nextNode();
                            if (externalPermissions.isNodeType("jnt:externalPermissions")) {
                                String path = externalPermissions.getProperty("j:path").getString();
                                path = path.replace("$site-path", ace.getResolveSite().getPath());
                                logger.debug(ace.getPath() + " / " + role + " ---> " + externalPermissions.getName() +" on " + path);
                                JCRNodeWrapper refNode = session.getNode(path);
                                if (!refNode.hasNode("j:acl")) {
                                    refNode.addMixin("jmix:accessControlled");
                                    refNode.addNode("j:acl","jnt:acl");
                                }
                                JCRNodeWrapper acl = refNode.getNode("j:acl");
                                String n = "REF" + externalPermissions.getName() + "_" + JCRContentUtils.replaceColon(principal);
                                if (!acl.hasNode(n)) {
                                    JCRNodeWrapper refAce = acl.addNode(n, "jnt:externalAce");
                                    refAce.setProperty("j:aceType", "GRANT");
                                    refAce.setProperty("j:principal", principal);
                                    refAce.setProperty("j:roles", new String[] {role});
                                    refAce.setProperty("j:externalPermissionsName", externalPermissions.getName());
                                    refAce.setProperty("j:protected", true);
                                    refAce.setProperty("j:sourceAce", new Value[] { session.getValueFactory().createValue(ace, true)});
                                } else {
                                    JCRNodeWrapper refAce = acl.getNode(n);
                                    refAce.getProperty("j:sourceAce").addValue(session.getValueFactory().createValue(ace, true));
                                }
                            }
                        }
                    }
                }
                session.save();
            }
        } catch (RepositoryException e) {
            logger.error("Cannot propagate external ACL",e);
        }
    }

}
