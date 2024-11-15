/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.jahia.api.Constants;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.query.QueryResultWrapper;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.*;
import java.util.regex.Pattern;

public class AclListener extends DefaultEventListener {

    private static final Pattern CURRENT_SITE_PATTERN = Pattern.compile("^currentSite");
    private static final Logger logger = LoggerFactory.getLogger(AclListener.class);
    private static ThreadLocal<Boolean> inListener = new ThreadLocal<Boolean>();
    public static final List<String> PRIVILEGED_GROUPS = Arrays.asList("g:privileged", "g:site-privileged");

    private JCRPublicationService publicationService;
    private JahiaUserManagerService userService;
    private JahiaGroupManagerService groupService;
    private Map<String, String> foundRoles = new HashMap<String, String>();

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    public void setUserService(JahiaUserManagerService userService) {
        this.userService = userService;
    }

    public void setGroupService(JahiaGroupManagerService groupService) {
        this.groupService = groupService;
    }

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.NODE_MOVED +
                Event.PROPERTY_REMOVED;
    }

    @Override
    public void onEvent(final EventIterator events) {

        final JCRSessionWrapper session = ((JCREventIterator) events).getSession();
        if (inListener.get() == Boolean.TRUE) {
            return;
        }

        try {

            inListener.set(Boolean.TRUE);
            final List<Event> aclEvents = new ArrayList<Event>();
            while (events.hasNext()) {
                Event next = events.nextEvent();
                if (next.getPath().contains("/j:acl/") || next.getPath().startsWith("/roles/")) {
                    aclEvents.add(next);
                } else if (next.getType() == Event.NODE_MOVED && isCrossSiteMove(next.getInfo())) {
                    JCRNodeWrapper node = session.getNode(next.getPath());
                    NodeIterator descendantNodes = JCRContentUtils.getDescendantNodes(node, "jnt:ace");

                    while (descendantNodes.hasNext()) {
                        JCRNodeWrapper nextAce = (JCRNodeWrapper) descendantNodes.next();
                        aclEvents.add(new CrossSiteACLRemoveNodeEvent(next, nextAce));
                    }
                }
            }
            if (aclEvents.isEmpty()) {
                return;
            }

            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, session.getWorkspace().getName(), session.getLocale(), new JCRCallback<Object>() {

                @Override
                public Object doInJCR(final JCRSessionWrapper systemSession) throws RepositoryException {

                    final Set<String> aceIdentifiers = new HashSet<String>();
                    final Set<String> addedAceIdentifiers = new HashSet<String>();
                    final Set<String> removedAcePaths = new HashSet<String>();
                    final Set<String> addedExtPermIds = new HashSet<String>();
                    final Set<List<String>> removedExtPermissions = new HashSet<List<String>>();
                    final Set<String> removedRoles = new HashSet<String>();
                    final Set<String> crossSiteMovedAces = new HashSet<String>();

                    parseEvents(systemSession, aclEvents, aceIdentifiers, addedAceIdentifiers, removedAcePaths, addedExtPermIds, removedExtPermissions, removedRoles, crossSiteMovedAces);

                    handleAclModifications(systemSession, aceIdentifiers, addedAceIdentifiers, removedAcePaths, crossSiteMovedAces, (JCREventIterator) events);
                    handleRoleModifications(systemSession, addedExtPermIds, removedExtPermissions);

                    if (removedRoles.size() > 0) {
                        handleRemovedRole(systemSession, removedRoles);
                        // roles are only available on default workspace, so we explicitly clean live nodes
                        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, null, liveSession -> {
                            handleRemovedRole(liveSession, removedRoles);
                            return null;
                        });
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Cannot propagate external ACL", e);
        } finally {
            inListener.set(Boolean.FALSE);
        }
    }

    private void parseEvents(JCRSessionWrapper systemSession, List<Event> aclEvents, Set<String> aceIdentifiers, Set<String> addedAceIdentifiers, Set<String> removedAcePaths, Set<String> addedExtPermIds, Set<List<String>> removedExtPermissions, Set<String> removedRoles, Set<String> crossSiteMovedAces) throws RepositoryException {
        for (Event next : aclEvents) {
            if (next.getPath().contains("/j:acl/")) {
                if (next.getType() == Event.PROPERTY_ADDED || next.getType() == Event.PROPERTY_CHANGED) {
                    try {
                        JCRNodeWrapper nodeByIdentifier = systemSession.getNodeByIdentifier(
                                next.getIdentifier());
                        if (nodeByIdentifier.isNodeType("jnt:ace") && !nodeByIdentifier.isNodeType(
                                "jnt:externalAce") && nodeByIdentifier.getProperty(
                                "j:aceType").getValue().getString().equals("GRANT")) {
                            String identifier = next.getIdentifier();
                            if (identifier != null) {
                                aceIdentifiers.add(identifier);
                                if (next.getType() == Event.PROPERTY_ADDED) {
                                    addedAceIdentifiers.add(identifier);
                                }
                            }
                        }
                    } catch (ItemNotFoundException e) {
                        logger.error("unable to read node " + next.getPath());
                    }
                } else if (next.getType() == Event.NODE_REMOVED && StringUtils.substringAfterLast(next.getPath(), "/").startsWith("GRANT_")) {
                    String identifier = next.getIdentifier();
                    if (identifier != null) {
                        aceIdentifiers.add(identifier);
                        // Identify ace as one which was removed on one site and added to another
                        if (next instanceof CrossSiteACLRemoveNodeEvent) {
                            crossSiteMovedAces.add(identifier);
                        }
                    }
                    removedAcePaths.add(next.getPath());
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

    private void handleAclModifications(final JCRSessionWrapper systemSession, Set<String> aceIdentifiers, final Set<String> addedAceIdentifiers, Set<String> removedAcePaths, Set<String> crossSiteMovedAces, final JCREventIterator events) throws RepositoryException {

        final Map<String, Set<String>> privilegedAdded = new HashMap<String, Set<String>>();
        final Map<String, Set<String>> privilegedToCheck = new HashMap<String, Set<String>>();
        final Map<String, JCRNodeWrapper> roleNodes = new HashMap<String, JCRNodeWrapper>();
        for (final String aceIdentifier : aceIdentifiers) {
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

            if (!addedAceIdentifiers.contains(aceIdentifier)) {
                QueryManager q = systemSession.getWorkspace().getQueryManager();
                String sql = "select * from [jnt:externalAce] as ace where ace.[j:sourceAce] = '" + aceIdentifier + "'";
                QueryResult qr = q.createQuery(sql, Query.JCR_SQL2).execute();
                NodeIterator ni = qr.getNodes();
                while (ni.hasNext()) {
                    JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();
                    String role = n.getProperty("j:roles").getValues()[0].getString();
                    // When ace is moved cross site we still want to reset source values on external ace or remove the ace.
                    // External ace will be recreated later if it is removed.
                    if (!roles.contains(role) || crossSiteMovedAces.contains(aceIdentifier)) {
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
            }

            if (!roles.isEmpty()) {
                if (systemSession.getWorkspace().getName().equals(Constants.LIVE_WORKSPACE)) {
                    final JCRNodeWrapper finalAce = ace;
                    final String fprincipal = principal;

                    JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, systemSession.getLocale(), new JCRCallback<Object>() {

                        @Override
                        public Object doInJCR(JCRSessionWrapper defaultSystemSession) throws RepositoryException {
                            final boolean publish = events.getOperationType() == JCRObservationManager.WORKSPACE_CLONE || events.getLastOperationType() == JCRObservationManager.WORKSPACE_CLONE;
                            handleAclModifications(systemSession, defaultSystemSession, roles, finalAce, fprincipal, privilegedAdded, privilegedToCheck, new HashMap<String, JCRNodeWrapper>(), addedAceIdentifiers.contains(aceIdentifier), publish);
                            return null;
                        }
                    });
                } else {
                    handleAclModifications(systemSession, systemSession, roles, ace, principal, privilegedAdded, privilegedToCheck, roleNodes, addedAceIdentifiers.contains(aceIdentifier), false);
                }
            }
        }

        systemSession.save();

        if (!systemSession.getWorkspace().getName().equals(Constants.LIVE_WORKSPACE)) {
            // Handle removed ACL - these users which may have lost their privileged access
            for (String acePath : removedAcePaths) {
                final String name = StringUtils.substringAfterLast(acePath, "/");
                if (name.startsWith("REF")) {
                    continue;
                }
                String site;
                if (acePath.startsWith("/sites/")) {
                    site = StringUtils.substringBefore(acePath.substring("/sites/".length()), "/");
                } else {
                    site = "systemsite";
                }
                String principal = StringUtils.substringAfter(name, "_").replaceFirst("_", ":");
                if (principal.startsWith("jcr:read") || principal.startsWith("jcr:write")) {
                    principal = StringUtils.substringAfter(principal, "_").replaceFirst("_", ":");
                }
                if (!PRIVILEGED_GROUPS.contains(principal)) {
                    if (!privilegedToCheck.containsKey(site)) {
                        privilegedToCheck.put(site, new HashSet<String>());
                    }
                    privilegedToCheck.get(site).add(principal);
                }
            }

            // Users who have new privileged roles do not require check
            for (Map.Entry<String, Set<String>> entry : privilegedToCheck.entrySet()) {
                if (privilegedAdded.get(entry.getKey()) != null) {
                    entry.getValue().removeAll(privilegedAdded.get(entry.getKey()));
                }
            }

            Set<String> membershipModifiedForGroups = new HashSet<>();
            for (Map.Entry<String, Set<String>> entry : privilegedToCheck.entrySet()) {
                final String site = entry.getKey();
                final JCRGroupNode priv = groupService.lookupGroup(site, JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME, systemSession);
                if (priv != null) {
                    for (String principal : entry.getValue()) {
                        JCRNodeWrapper p = getPrincipal(site, principal);
                        if (p == null || !priv.isMember(p)) {
                            continue;
                        }
                        List<String> rolesName = new ArrayList<String>();
                        boolean needPrivileged = false;

                        StringBuilder sql = new StringBuilder();
                        sql.append("select ace.[j:roles] AS [rep:facet(facet.mincount=1)] from [jnt:ace] as ace");
                        sql.append(" where (not ([j:externalPermissionsName] is not null)) and ace.[j:aceType]='GRANT'");
                        sql.append(" and ace.[j:principal] = '");
                        sql.append(principal);
                        sql.append("' and (isdescendantnode(ace, ['/sites/");
                        sql.append(site);
                        sql.append("'])");

                        if (StringUtils.equals(site, JahiaSitesService.SYSTEM_SITE_KEY)) {
                            sql.append(" or isdescendantnode(ace, ['/mounts'])");
                            sql.append(" or isdescendantnode(ace, ['/j:acl'])");
                            sql.append(" or isdescendantnode(ace, ['/groups'])");
                            sql.append(" or isdescendantnode(ace, ['/users'])");
                            sql.append(" or isdescendantnode(ace, ['/modules'])");
                        }

                        sql.append(')');

                        rolesName.addAll(getRolesName(systemSession, sql.toString()));
                        try {
                            for (String roleName : rolesName) {
                                JCRNodeWrapper roleNode = getRole(systemSession, roleName, roleNodes);
                                if (roleNode != null) {
                                    if (roleNode.hasProperty("j:privilegedAccess") && roleNode.getProperty("j:privilegedAccess").getBoolean()) {
                                        needPrivileged = true;
                                        break;
                                    }
                                }
                            }
                        } catch (PathNotFoundException e) {
                            // ignore exception
                        }
                        if (!needPrivileged) {
                            logger.info(principal + " do not need privileged access");
                            priv.removeMember(p);
                            membershipModifiedForGroups.add(priv.getPath());
                        }
                    }
                }
            }

            for (Map.Entry<String, Set<String>> entry : privilegedAdded.entrySet()) {
                final String site = entry.getKey();
                final JCRGroupNode priv = groupService.lookupGroup(site, JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME, systemSession);
                if (priv != null) {
                    for (String principal : entry.getValue()) {
                        JCRNodeWrapper p = getPrincipal(site, principal);
                        if (p == null) {
                            continue;
                        }
                        Optional<JCRNodeWrapper> matchingObject = priv.getMembers().stream().filter(member -> member.getPath().equals(p.getPath())).findAny();
                        if (matchingObject.isPresent()) {
                            if (membershipModifiedForGroups.contains(priv.getPath())) {
                                // In case membership of the privileged group was modified, we must save this change to make it visible
                                // to the priv.isMember(p) invocation (which uses a different session to access JCR), and check membership
                                // of the principal p again.
                                systemSession.save();
                                membershipModifiedForGroups.clear();
                                if (priv.isMember(p)) {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                        }
                        logger.info(principal + " need privileged access");
                        priv.addMember(p);
                    }
                }
            }

        }

        systemSession.save();
    }

    private void handleAclModifications(JCRSessionWrapper session, JCRSessionWrapper defaultSession, Set<String> roles, JCRNodeWrapper ace, String principal, Map<String, Set<String>> privilegedAdded, Map<String, Set<String>> privilegedRemoved, Map<String, JCRNodeWrapper> roleNodes, boolean isNewAce, boolean publish) throws RepositoryException {

        boolean needPrivileged = false;

        for (String role : roles) {
            JCRNodeWrapper roleNode = getRole(defaultSession, role, roleNodes);
            if (roleNode != null) {
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
                    if (roleNode.hasProperty("j:privilegedAccess") && roleNode.getProperty("j:privilegedAccess").getBoolean()) {
                        needPrivileged = true;
                    }
                    roleNode = roleNode.getParent();
                } while (roleNode.isNodeType(Constants.JAHIANT_ROLE));
            }
        }

        if (!session.getWorkspace().getName().equals(Constants.LIVE_WORKSPACE)) {
            // Store new privileged users, or users which may have lost their privileged access
            if (!PRIVILEGED_GROUPS.contains(principal) && (needPrivileged || !isNewAce)) {
                Map<String, Set<String>> p = needPrivileged ? privilegedAdded : privilegedRemoved;
                if (!p.containsKey(ace.getResolveSite().getSiteKey())) {
                    p.put(ace.getResolveSite().getSiteKey(), new HashSet<String>());
                }
                p.get(ace.getResolveSite().getSiteKey()).add(principal);
            }
        }
    }

    private JCRNodeWrapper getRole(JCRSessionWrapper systemSession, String roleName, Map<String, JCRNodeWrapper> roleNodes) throws RepositoryException {
        if (roleNodes.containsKey(roleName)) {
            return roleNodes.get(roleName);
        }
        if (foundRoles.containsKey(roleName) && systemSession.itemExists(foundRoles.get(roleName))) {
            JCRNodeWrapper roleNode = systemSession.getNode(foundRoles.get(roleName));
            roleNodes.put(roleName, roleNode);
            return roleNode;
        }
        NodeIterator ni = systemSession.getWorkspace().getQueryManager().createQuery(
                "select * from [" + Constants.JAHIANT_ROLE + "] as r where localname()='" + JCRContentUtils.sqlEncode(roleName) + "' and isdescendantnode(r,['/roles'])",
                Query.JCR_SQL2).execute().getNodes();
        if (ni.hasNext()) {
            JCRNodeWrapper roleNode = (JCRNodeWrapper) ni.nextNode();
            roleNodes.put(roleName, roleNode);
            foundRoles.put(roleName, roleNode.getPath());
            return roleNode;
        }
        roleNodes.put(roleName, null);
        foundRoles.remove(roleName);
        return null;
    }

    private JCRNodeWrapper getPrincipal(String site, String principal) {
        JCRNodeWrapper p = null;
        String principalName = principal.substring(2);
        if (principal.startsWith("u:")) {
            p = userService.lookupUser(principalName, site);
        } else if (principal.startsWith("g:")) {
            p = groupService.lookupGroup(site, principalName);
            if (p == null) {
                p = groupService.lookupGroup(null, principalName);
            }
        }
        return p;
    }

    private List<String> getRolesName(JCRSessionWrapper session, String sql) throws RepositoryException {

        QueryManager q = session.getWorkspace().getQueryManager();
        // (not ([j:externalPermissionsName] is not null)) => do not return jnt:externalAce
        List<String> rolesName = new ArrayList<String>();
        QueryResultWrapper qr = (QueryResultWrapper) q.createQuery(sql, Query.JCR_SQL2).execute();

        for (FacetField facetField : qr.getFacetFields()) {
            if (facetField.getValues() != null) {
                for (FacetField.Count facetFieldValue : facetField.getValues()) {
                    rolesName.add(facetFieldValue.getName());
                }
            }
        }
        return rolesName;
    }

    private void handleRoleModifications(JCRSessionWrapper session, Set<String> addedExtPermIds, Set<List<String>> removedExtPermissions) {

        for (String extPermId : addedExtPermIds) {
            try {
                JCRNodeWrapper externalPermission = session.getNodeByIdentifier(extPermId);
                QueryManager q = session.getWorkspace().getQueryManager();
                String role = externalPermission.getParent().getName();
                String sql = "select * from [jnt:ace] as ace where ace.[j:roles] = '" + JCRContentUtils.sqlEncode(role) + "'";
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
                String sql = "select * from [jnt:externalAce] as ace where ace.[j:roles] = '" + JCRContentUtils.sqlEncode(roleExtPerm.get(0)) +
                        "' and ace.[j:externalPermissionsName] ='" + JCRContentUtils.sqlEncode(roleExtPerm.get(1)) + "'";
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
    }

    private void handleRemovedRole(JCRSessionWrapper session, Set<String> removedRoles) {
        for (String role : removedRoles) {
            try {
                QueryManager q = session.getWorkspace().getQueryManager();
                String sql = "select * from [jnt:ace] as ace where ace.[j:roles] = '" + JCRContentUtils.sqlEncode(role) + "'";
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

        final String externalPermissionsPath = externalPermissions.getPropertyAsString("j:path");
        if (CURRENT_SITE_PATTERN.matcher(externalPermissionsPath).find()) {
            JCRNodeWrapper p = getPrincipal(ace.getResolveSite().getSiteKey(), principal);
            if (p == null || p.getResolveSite() == null || (!p.getResolveSite().getSiteKey().equals("systemsite") && !p.getResolveSite().getSiteKey().equals(refNode.getResolveSite().getSiteKey()))) {
                return;
            }
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
        if (refNode.hasNode("j:acl/" + n)) {
            JCRNodeWrapper node = refNode.getNode("j:acl").getNode(n);
            publicationService.publishByMainId(node.getIdentifier());
        }
    }

    public JCRNodeWrapper getRefAclNode(JCRSessionWrapper session, JCRNodeWrapper ace, String role, JCRNodeWrapper externalPermissions) throws RepositoryException {
        String path = externalPermissions.getProperty("j:path").getString();
        path = CURRENT_SITE_PATTERN.matcher(path).replaceFirst(ace.getResolveSite().getPath());
        if (!session.nodeExists(path)) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Cannot create or update external ACE " + externalPermissions.getName() + " because the node " +
                                path + "doesn't exist."
                );
            }
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(ace.getPath() + " / " + role + " ---> " + externalPermissions.getName() + " on " + path);
        }
        return session.getNode(path);
    }

    private boolean isCrossSiteMove(Map<String, String> eventInfo) {
        String srcSite = StringUtils.substringBefore(StringUtils.substringAfter(eventInfo.get("srcAbsPath"), "/sites/"), "/");
        String destSite = StringUtils.substringBefore(StringUtils.substringAfter(eventInfo.get("destAbsPath"), "/sites/"), "/");

        if (srcSite == null || destSite == null) {
            return false;
        }

        return !srcSite.equals(destSite);
    }

    private class CrossSiteACLRemoveNodeEvent implements Event {
        private Event originalEvent;
        private JCRNodeWrapper movedAce;

        public CrossSiteACLRemoveNodeEvent(Event original, JCRNodeWrapper movedAce) {
           this.originalEvent = original;
           this.movedAce = movedAce;
        }

        @Override
        public int getType() {
            return Event.NODE_REMOVED;
        }

        @Override
        public String getPath() throws RepositoryException {
            String srcSite = getSrcSite();
            String destSite = getDestSite();
            return movedAce.getPath().replace(String.format("/%s/", destSite), String.format("/%s/", srcSite));
        }

        @Override
        public String getUserID() {
            return null;
        }

        @Override
        public String getIdentifier() throws RepositoryException {
            return movedAce.getIdentifier();
        }

        @Override
        public Map getInfo() throws RepositoryException {
            return null;
        }

        @Override
        public String getUserData() throws RepositoryException {
            return null;
        }

        @Override
        public long getDate() throws RepositoryException {
            return 0;
        }

        public String getSrcSite() throws RepositoryException {
            Map<String, String> eventInfo = originalEvent.getInfo();
            return StringUtils.substringBefore(StringUtils.substringAfterLast(eventInfo.get("srcAbsPath"), "/sites/"), "/");
        }

        public String getDestSite() throws RepositoryException {
            Map<String, String> eventInfo = originalEvent.getInfo();
            return StringUtils.substringBefore(StringUtils.substringAfterLast(eventInfo.get("destAbsPath"), "/sites/"), "/");
        }
    }
}
