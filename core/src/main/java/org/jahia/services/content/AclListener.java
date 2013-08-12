/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content;

import org.jahia.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static Logger logger = LoggerFactory.getLogger(AclListener.class);

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
                } catch (InvalidItemStateException e) {                    
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
                    NodeIterator nodes = session.getWorkspace().getQueryManager().createQuery(
                            "select * from [" + Constants.JAHIANT_ROLE + "] as r where localname()='" + role + "' and isdescendantnode(r,['/roles'])",
                            Query.JCR_SQL2).execute().getNodes();
                    if (nodes.hasNext()) {
                        JCRNodeWrapper roleNode = (JCRNodeWrapper) nodes.nextNode();
                        do {
                            NodeIterator r = roleNode.getNodes();
                            while (r.hasNext()) {
                                JCRNodeWrapper externalPermissions = (JCRNodeWrapper) r.nextNode();
                                if (externalPermissions.isNodeType("jnt:externalPermissions")) {
                                    String path = externalPermissions.getProperty("j:path").getString();
                                    path = path.replaceFirst("^currentSite", ace.getResolveSite().getPath());
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
                                        refAce.setProperty("j:roles", new String[] {roleNode.getName()});
                                        refAce.setProperty("j:externalPermissionsName", externalPermissions.getName());
                                        refAce.setProperty("j:protected", true);
                                        refAce.setProperty("j:sourceAce", new Value[] { session.getValueFactory().createValue(ace, true)});
                                    } else {
                                        JCRNodeWrapper refAce = acl.getNode(n);
                                        refAce.getProperty("j:sourceAce").addValue(session.getValueFactory().createValue(ace, true));
                                    }
                                }
                            }
                            roleNode = roleNode.getParent();
                        } while (roleNode.isNodeType(Constants.JAHIANT_ROLE));
                    }
                }
                session.save();
            }
        } catch (RepositoryException e) {
            logger.error("Cannot propagate external ACL",e);
        }
    }

}
