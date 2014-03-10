/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.*;

public class AclListener extends DefaultEventListener {
    private static Logger logger = LoggerFactory.getLogger(AclListener.class);
    private JCRPublicationService publicationService;
    private static ThreadLocal<Boolean> inListener = new ThreadLocal<Boolean>();

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED +
                Event.PROPERTY_REMOVED;
    }

    public void onEvent(final EventIterator events) {
        final JCRSessionWrapper session = ((JCREventIterator) events).getSession();
        try {
            if (inListener.get() == Boolean.TRUE) {
                return;
            }
            inListener.set(Boolean.TRUE);
            final List<Event> aclEvents = new ArrayList<Event>();
            while (events.hasNext()) {
                Event next = events.nextEvent();
                if (next.getPath().contains("/j:acl/") || next.getPath().startsWith("/roles/")) {
                    aclEvents.add(next);
                }
            }
            if (aclEvents.isEmpty()) {
                return;
            }

            JCRTemplate.getInstance().doExecuteWithSystemSession(null, session.getWorkspace().getName(), session.getLocale(), new JCRCallback<Object>() {
                @Override
                public Object doInJCR(final JCRSessionWrapper systemSession) throws RepositoryException {
                    final Set<String> aceIdentifiers = new HashSet<String>();
                    final Set<String> addedExtPermIds = new HashSet<String>();
                    final Set<List<String>> removedExtPermissions = new HashSet<List<String>>();
                    final Set<String> removedRoles = new HashSet<String>();

                    parseEvents(systemSession, aclEvents, aceIdentifiers, addedExtPermIds, removedExtPermissions, removedRoles);

                    handleAclModifications(systemSession, aceIdentifiers, (JCREventIterator) events);
                    handleRoleModifications(systemSession, addedExtPermIds, removedExtPermissions, removedRoles);

                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Cannot propagate external ACL", e);
        } finally {
            inListener.set(Boolean.FALSE);
        }

    }

    private void parseEvents(JCRSessionWrapper systemSession, List<Event> aclEvents, Set<String> aceIdentifiers, Set<String> addedExtPermIds, Set<List<String>> removedExtPermissions, Set<String> removedRoles) throws RepositoryException {
        for (Event next : aclEvents) {
            if (next.getPath().contains("/j:acl/")) {
                if (next.getType() == Event.PROPERTY_ADDED || next.getType() == Event.PROPERTY_CHANGED) {
                    try {
                        JCRNodeWrapper nodeByIdentifier = systemSession.getNodeByIdentifier(
                                next.getIdentifier());
                        if (nodeByIdentifier.isNodeType("jnt:ace") && !nodeByIdentifier.isNodeType(
                                "jnt:externalAce") && nodeByIdentifier.getProperty(
                                "j:aceType").getValue().getString().equals("GRANT")) {
                            aceIdentifiers.add(next.getIdentifier());
                        }
                    } catch (ItemNotFoundException e) {
                        logger.error("unable to read node " + next.getPath());
                    }
                } else if (next.getType() == Event.NODE_REMOVED && StringUtils.substringAfterLast(next.getPath(),"/").startsWith("GRANT_")) {
                    aceIdentifiers.add(next.getIdentifier());
                }
            } else if (next.getPath().startsWith("/roles/")) {
                if (next.getType() == Event.NODE_ADDED) {
                    String identifier = next.getIdentifier();
                    JCRNodeWrapper nodeByIdentifier = systemSession.getNodeByIdentifier(identifier);
                    if (nodeByIdentifier.isNodeType("jnt:externalPermissions")) {
                        addedExtPermIds.add(identifier);
                    }
                } else if (next.getType() == Event.NODE_REMOVED) {
                    String path = next.getPath();
                    if (path.endsWith("-access")) {
                        removedExtPermissions.add(Arrays.asList(StringUtils.substringAfterLast(
                                        StringUtils.substringBeforeLast(path, "/"), "/"),
                                StringUtils.substringAfterLast(path, "/")
                        ));
                    } else {
                        removedRoles.add(StringUtils.substringAfterLast(path, "/"));
                    }
                }
            }
        }
    }

    private void handleAclModifications(final JCRSessionWrapper systemSession, Set<String> aceIdentifiers, final JCREventIterator events) throws RepositoryException {
        for (String aceIdentifier : aceIdentifiers) {
            final Set<String> roles = new HashSet<String>();

            JCRNodeWrapper ace = null;
            String principal = null;
            try {
                ace = systemSession.getNodeByIdentifier(aceIdentifier);
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
                // Item does not exist anymore, use empty roles set
            } catch (InvalidItemStateException e) {
                // Item does not exist anymore, use empty roles set
            }

            QueryManager q = systemSession.getWorkspace().getQueryManager();
            String sql = "select * from [jnt:externalAce] as ace where ace.[j:sourceAce] = '" + aceIdentifier + "'";
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
            if (!roles.isEmpty()) {
                if (systemSession.getWorkspace().getName().equals(Constants.LIVE_WORKSPACE)) {
                    final JCRNodeWrapper finalAce = ace;
                    final String fprincipal = principal;
                    JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE, systemSession.getLocale(), new JCRCallback<Object>() {
                        @Override
                        public Object doInJCR(JCRSessionWrapper defaultSystemSession) throws RepositoryException {
                            final boolean publish = events.getOperationType() == JCRObservationManager.WORKSPACE_CLONE || events.getLastOperationType() == JCRObservationManager.WORKSPACE_CLONE;
                            handleAclModifications(systemSession, defaultSystemSession, roles, finalAce, fprincipal, publish);
                            return null;
                        }
                    });
                } else {
                    handleAclModifications(systemSession, systemSession, roles, ace, principal, false);
                }
            }
            systemSession.save();
        }
    }

    private void handleAclModifications(JCRSessionWrapper session, JCRSessionWrapper defaultSession, Set<String> roles, JCRNodeWrapper ace, String principal, boolean publish) throws RepositoryException {
        for (String role : roles) {
            NodeIterator nodes = defaultSession.getWorkspace().getQueryManager().createQuery(
                    "select * from [" + Constants.JAHIANT_ROLE + "] as r where localname()='" + role +
                            "' and isdescendantnode(r,['/roles'])", Query.JCR_SQL2
            ).execute().getNodes();
            if (nodes.hasNext()) {
                JCRNodeWrapper roleNode = (JCRNodeWrapper) nodes.nextNode();
                do {
                    NodeIterator r = roleNode.getNodes();
                    while (r.hasNext()) {
                        JCRNodeWrapper externalPermissions = (JCRNodeWrapper) r.nextNode();
                        if (externalPermissions.isNodeType("jnt:externalPermissions")) {
                            if (publish) {
                                publishExternalACE(defaultSession, ace, principal, role, externalPermissions);
                            } else {
                                createOrUpdateExternalACE(session, ace, principal, role, externalPermissions);
                            }
                        }
                    }
                    roleNode = roleNode.getParent();
                } while (roleNode.isNodeType(Constants.JAHIANT_ROLE));
            }
        }
    }

    private void handleRoleModifications(JCRSessionWrapper session, Set<String> addedExtPermIds, Set<List<String>> removedExtPermissions, Set<String> removedRoles) {
        for (String extPermId : addedExtPermIds) {
            try {
                JCRNodeWrapper externalPermission = session.getNodeByIdentifier(extPermId);
                QueryManager q = session.getWorkspace().getQueryManager();
                String role = externalPermission.getParent().getName();
                String sql = "select * from [jnt:ace] as ace where ace.[j:roles] = '" + role + "'";
                QueryResult qr = q.createQuery(sql, Query.JCR_SQL2).execute();
                NodeIterator ni = qr.getNodes();
                while (ni.hasNext()) {
                    JCRNodeWrapper ace = (JCRNodeWrapper) ni.nextNode();
                    if (!ace.isNodeType("jnt:externalAce")) {
                        createOrUpdateExternalACE(session, ace, ace.getProperty("j:principal").getString(), role,
                                externalPermission);
                    }
                }
                session.save();
            } catch (RepositoryException e) {
                logger.error("Cannot create or update external ACE", e);
            }
        }

        for (List<String> roleExtPerm : removedExtPermissions) {
            try {
                QueryManager q = session.getWorkspace().getQueryManager();
                String sql = "select * from [jnt:externalAce] as ace where ace.[j:roles] = '" + roleExtPerm.get(0) +
                        "' and ace.[j:externalPermissionsName] ='" + roleExtPerm.get(1) + "'";
                QueryResult qr = q.createQuery(sql, Query.JCR_SQL2).execute();
                NodeIterator ni = qr.getNodes();
                while (ni.hasNext()) {
                    JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();
                    n.remove();
                }
                session.save();
            } catch (RepositoryException e) {
                logger.error("Cannot remove external ACE", e);
            }
        }

        for (String role : removedRoles) {
            try {
                QueryManager q = session.getWorkspace().getQueryManager();
                String sql = "select * from [jnt:ace] as ace where ace.[j:roles] = '" + role + "'";
                QueryResult qr = q.createQuery(sql, Query.JCR_SQL2).execute();
                NodeIterator ni = qr.getNodes();
                while (ni.hasNext()) {
                    JCRNodeWrapper ace = (JCRNodeWrapper) ni.nextNode();
                    if (!ace.isNodeType("jnt:externalAce")) {
                        Value[] roles = ace.getProperty("j:roles").getValues();
                        List<String> newRoles = new ArrayList<String>();
                        for (Value v : roles) {
                            String r = v.getString();
                            if (!role.equals(r)) {
                                newRoles.add(r);
                            }
                        }
                        if (newRoles.isEmpty()) {
                            ace.remove();
                        } else {
                            ace.setProperty("j:roles", newRoles.toArray(new String[newRoles.size()]));
                        }
                    }
                }
                session.save();
            } catch (RepositoryException e) {
                logger.error("Cannot remove external ACE", e);
            }
        }
    }

    private void createOrUpdateExternalACE(JCRSessionWrapper session, JCRNodeWrapper ace, String principal, String role,
                                           JCRNodeWrapper externalPermissions) throws RepositoryException {
        JCRNodeWrapper refNode = getRefAclNode(session, ace, role, externalPermissions);
        if (refNode == null) {
            return;
        }
        if (!refNode.hasNode("j:acl")) {
            refNode.addMixin("jmix:accessControlled");
            refNode.addNode("j:acl", "jnt:acl");
        }
        JCRNodeWrapper acl = refNode.getNode("j:acl");
        String n = "REF" + role + "_" + externalPermissions.getName() + "_" + JCRContentUtils.replaceColon(principal);
        if (!acl.hasNode(n)) {
            JCRNodeWrapper refAce = acl.addNode(n, "jnt:externalAce");
            refAce.setProperty("j:aceType", "GRANT");
            refAce.setProperty("j:principal", principal);
            refAce.setProperty("j:roles", new String[]{role});
            refAce.setProperty("j:externalPermissionsName", externalPermissions.getName());
            refAce.setProperty("j:protected", true);
            refAce.setProperty("j:sourceAce", new Value[]{session.getValueFactory().createValue(ace, true)});
        } else {
            JCRNodeWrapper refAce = acl.getNode(n);
            if (refAce.hasProperty("j:sourceAce")) {
                refAce.getProperty("j:sourceAce").addValue(session.getValueFactory().createValue(ace, true));
            } else {
                refAce.setProperty("j:sourceAce", new Value[]{session.getValueFactory().createValue(ace, true)});
            }
        }
    }

    private void publishExternalACE(JCRSessionWrapper session, JCRNodeWrapper ace, String principal, String role,
                                    JCRNodeWrapper externalPermissions) throws RepositoryException {
        JCRNodeWrapper refNode = getRefAclNode(session, ace, role, externalPermissions);
        if (refNode == null) {
            return;
        }
        String n = "REF" + role + "_" + externalPermissions.getName() + "_" + JCRContentUtils.replaceColon(principal);
        if (refNode.hasNode("j:acl/"+n)) {
            JCRNodeWrapper node = refNode.getNode("j:acl").getNode(n);
            publicationService.publishByMainId(node.getIdentifier());
        }
    }

    public JCRNodeWrapper getRefAclNode(JCRSessionWrapper session, JCRNodeWrapper ace, String role, JCRNodeWrapper externalPermissions) throws RepositoryException {
        String path = externalPermissions.getProperty("j:path").getString();
        path = path.replaceFirst("^currentSite", ace.getResolveSite().getPath());
        if (!session.nodeExists(path)) {
            logger.debug(
                    "Cannot create or update external ACE " + externalPermissions.getName() + " because the node " +
                            path + "doesn't exist."
            );
            return null;
        }
        logger.debug(ace.getPath() + " / " + role + " ---> " + externalPermissions.getName() + " on " + path);
        return session.getNode(path);
    }
}
