/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.usermanager.jcr;

import org.jahia.services.content.decorator.JCRGroupNode;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerRoutingService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerRoutingService;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.security.Principal;
import java.util.*;

/**
 * Implementation of the JahiaGroup interface that uses the JCR API for storage
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 8 juil. 2009
 */
public class JCRGroup extends JahiaGroup implements JCRPrincipal {
    public static final String J_HIDDEN = "j:hidden";
    public static final String J_EXTERNAL = "j:external";
    public static final String J_EXTERNAL_SOURCE = "j:externalSource";
    public static final String J_DISPLAYABLE_NAME = "j:displayableName";

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRGroup.class);
    private String nodeUuid;
    private boolean external;
    private static final String PROVIDER_NAME = "jcr";
    private Properties properties = null;

    public JCRGroup(Node nodeWrapper, int siteID) {
        this(nodeWrapper, siteID, false);
    }

    public JCRGroup(Node nodeWrapper, int siteID, boolean isExternal) {
        super();
        this.mSiteID = siteID;
        try {
            this.nodeUuid = nodeWrapper.getIdentifier();
            this.mGroupname = nodeWrapper.getName();
            this.mGroupKey = mGroupname + ":" + siteID;
            this.hidden = nodeWrapper.getProperty(J_HIDDEN).getBoolean();
            this.mMembers = getMembersMap(nodeWrapper);
        } catch (RepositoryException e) {
            logger.error("Error while accessing repository", e);
        }
        this.external = isExternal;
    }

    /**
     * Get grp's properties list.
     *
     * @return Return a reference on the grp's properties list, or null if no
     *         property is present.
     */
    public Properties getProperties() {
        if (properties == null) {
            try {
                properties = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Properties>() {
                    public Properties doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        Properties properties = new Properties();
                        JCRGroupNode jcrGroupNode = (JCRGroupNode) getNode(session);
                        PropertyIterator iterator = jcrGroupNode.getProperties();
                        for (; iterator.hasNext();) {
                            Property property = iterator.nextProperty();
                            if (!property.isMultiple()) {
                                properties.put(property.getName(), property.getString());
                            }
                        }
                        return properties;
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Error while retrieving group properties", e);
            }
        } else {
            return properties;
        }
        return null;
    }

    /**
     * Retrieve the requested grp property.
     *
     * @param key Property's name.
     * @return Return the property's value of the specified key, or null if the
     *         property does not exist.
     */
    public String getProperty(final String key) {
        if (properties != null) {
            return (String) properties.get(key);
        }
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<String>() {
                public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    return getNode(session).getProperty(key).getString();
                }
            });
        } catch (PathNotFoundException pnfe) {
            // This is expected in the case the property doesn't exist in the repository. We will simply return null.
            return null;
        } catch (RepositoryException e) {
            logger.error("Error while retrieving group property " + key, e);
            return null;
        }
    }

    /**
     * Remove the specified property from the properties list.
     *
     * @param key Property's name.
     */
    public boolean removeProperty(final String key) {
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node node = getNode(session);
                    Property property = node.getProperty(key);
                    if (property != null) {
                        if (properties != null) {
                            properties.remove(key);
                        }
                        session.checkout(node);
                        property.remove();
                        session.save();
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }
            });
        } catch (RepositoryException e) {
            logger.warn("Error while removing property " + key, e);
        }
        return false;
    }

    /**
     * Add (or update if not already in the property list) a property key-value
     * pair in the grp's properties list.
     *
     * @param key   Property's name.
     * @param value Property's value.
     */
    public boolean setProperty(final String key, final String value) {
        try {
            if (J_EXTERNAL.equals(key)) {
                external = Boolean.valueOf(value);
            }

            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node node = getNode(session);
                    session.checkout(node);
                    node.setProperty(key, value);
                    session.save();
                    if (properties != null) {
                        properties.put(key, value);
                    }
                    return Boolean.TRUE;
                }
            });
        } catch (RepositoryException e) {
            logger.warn("Error while setting property " + key + " with value " + value, e);
        }
        return false;
    }

    /**
     * Adds the specified member to the group.
     *
     * @param principal The principal to add to this group.
     * @return Return true if the member was successfully added, false if the
     *         principal was already a member.
     */
    public boolean addMember(final Principal principal) {
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    if (isMember(principal)) {
                        return false;
                    }
                    if (principal.equals(JCRGroup.this)) {
                        return false;
                    }
                    JCRPrincipal jcrUser = null;
                    String name = principal.getName();
                    if (principal instanceof JCRUser) {
                        jcrUser = (JCRPrincipal) principal;
                    } else if (principal instanceof JCRGroup) {
                        name = name + "___" + ((JCRGroup) principal).getSiteID();
                        jcrUser = (JCRPrincipal) principal;
                    } else if (principal instanceof JahiaUser) {
                        JCRTemplate.getInstance().getProvider("/").deployExternalUser((JahiaUser) principal);
                        jcrUser = (JCRUser) JCRUserManagerProvider.getInstance().lookupExternalUser((JahiaUser) principal);
                    } else if (principal instanceof JahiaGroup) {
                        JCRTemplate.getInstance().getProvider("/").deployExternalGroup((JahiaGroup) principal);
                        jcrUser = (JCRGroup) JCRGroupManagerProvider.getInstance().lookupExternalGroup(principal.getName());
                    }
                    if (jcrUser != null) {
                        Node node = getNode(session);
                        Node members = node.getNode("j:members");
                        if (!members.hasNode(name)) {
                            members.checkout();
                            Node member = members.addNode(name, Constants.JAHIANT_MEMBER);
                            member.setProperty("j:member", jcrUser.getIdentifier());
                            JCRGroupManagerProvider.getInstance().updateMembershipCache(jcrUser.getIdentifier());
                            session.save();
                        }
                        mMembers.add(principal);

                        return true;
                    }
                    return false;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while adding group member", e);
        }
        return false;
    }

    /**
     * Returns the group's home page id.
     * -1 : undefined
     *
     * @return int The group homepage id.
     */
    public int getHomepageID() {
        return -1;
    }

    /**
     * Set the home page id.
     *
     * @param id the group homepage id.
     * @return false on error
     */
    public boolean setHomepageID(int id) {
        // TODO we will need to implement this if we want to support group homepages again.
        return false;
    }

    /**
     * Returns a hashcode for this principal.
     *
     * @return A hashcode for this principal.
     */
    public int hashCode() {
        return nodeUuid.hashCode();
    }

    /**
     * Returns members of this group. If members were not loaded before,
     * forces loading.
     *
     * @return members of this group
     */
    protected Set<Principal> getMembersMap() {
        if (mMembers == null) {
            try {
                return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Set<Principal>>() {
                    public Set<Principal> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        final Node node = getNode(session);
                        return getMembersMap(node);
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Error while retrieving group member map", e);
            }
        }
        return new HashSet<Principal>(mMembers);
    }

    private Set<Principal> getMembersMap(Node node) throws RepositoryException {
        if (mMembers == null) {
            Set<Principal> principalMap = new HashSet<Principal>();
            Node members = node.getNode("j:members");
            NodeIterator iterator = members.getNodes();
            while (iterator.hasNext()) {
                Node member = (Node) iterator.next();
                if (member.isNodeType(Constants.JAHIANT_MEMBER)) {
                    if (!member.hasProperty("j:member")) {
                        logger.warn("Missing member property, ignoring group member " + member.getName() + "...");
                        continue;
                    }
                    Property memberProperty = member.getProperty("j:member");
                    Node memberNode = null;
                    try {
                        memberNode = memberProperty.getNode();
                    } catch (ItemNotFoundException infe) {
                        logger.warn("Couldn't find group member " + member.getName() + "(uuid=" + memberProperty.getString() + "), ignoring...");
                    }
                    if (memberNode != null) {
                        if (memberNode.isNodeType(Constants.JAHIANT_USER)) {
                            JahiaUser jahiaUser = JahiaUserManagerRoutingService.getInstance().lookupUser(member.getName());
                            if (jahiaUser != null) {
                                principalMap.add(jahiaUser);
                            } else {
                                logger.warn("Member '" + member.getName() + "' cannot be found for group '" + node.getName()
                                        + "'");
                            }
                        } else {
                            String s = member.getName().replace("___", ":");
                            JahiaGroup g = JahiaGroupManagerRoutingService.getInstance().lookupGroup(s);
                            if (g != null) {
                                principalMap.add(g);
                            } else {
                                logger.warn("Member '" + member.getName() + "' cannot be found for group '" + node.getName()
                                        + "'");
                            }
                        }
                    }
                }
            }
            mMembers = principalMap;
            preloadedGroups = true;
        }
        return new HashSet<Principal>(mMembers);
    }

    /**
     * Removes the specified member from the group.
     *
     * @param principal The principal to remove from this group.
     * @return Return true if the principal was removed, or false if the
     *         principal was not a member.
     */
    public boolean removeMember(final Principal principal) {
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node group = getNode(session);
                    Node members = group.getNode("j:members");
                    String memberUuid = null;
                    if (principal instanceof JCRPrincipal) {
                        // internal user/group
                        memberUuid = ((JCRPrincipal) principal).getIdentifier();
                    } else if (principal instanceof JahiaUser) {
                        // external user
                        JCRUser externalUser = JCRUserManagerProvider.getInstance().lookupExternalUser((JahiaUser) principal);
                        if (externalUser != null) {
                            memberUuid = externalUser.getIdentifier();
                        } else {
                            logger.warn("User node for an external user with the name '" + principal.getName()
                                    + " cannot be found. Skip removing user from group " + group.getPath());
                        }
                    } else if (principal instanceof JahiaGroup) {
                        // external user
                        JCRGroup externalGroup = JCRGroupManagerProvider.getInstance().lookupExternalGroup(((JahiaGroup) principal).getGroupname());
                        if (externalGroup != null) {
                            memberUuid = externalGroup.getIdentifier();
                        } else {
                            logger.warn("JCR node for an external group with the name '" + principal.getName()
                                    + " cannot be found. Skip removing principal from group " + group.getPath());
                        }
                    } else {
                        logger.warn("Cannot remove membership for principal " + principal + " in group "
                                + group.getPath() + ". Do not know how to handle this principal type.");
                    }
                    return memberUuid != null ? removeMember(session, members, memberUuid) : Boolean.FALSE;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while removing member", e);
        }
        return false;
    }

    private boolean removeMember(JCRSessionWrapper session, Node members, String memberIdentifier) throws RepositoryException {
        if (session.getWorkspace().getQueryManager() != null) {
            String query = "SELECT * FROM [jnt:member] as m where m.[j:member] = '" + memberIdentifier + "' AND ISCHILDNODE(m, '" + members.getPath() + "') ORDER BY localname(m)";
            Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
            QueryResult qr = q.execute();
            NodeIterator nodes = qr.getNodes();
            while (nodes.hasNext()) {
                Node memberNode = nodes.nextNode();
                memberNode.checkout();
                memberNode.remove();
            }
            session.save();
            mMembers = null;
            mMembers = getMembersMap();
            JCRGroupManagerProvider.getInstance().updateMembershipCache(memberIdentifier);
            return true;
        }
        return false;
    }

    /**
     * Returns a string representation of this group.
     *
     * @return A string representation of this group.
     */
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer("Details of group [" + mGroupname + "] :\n");

        output.append("  - ID : ").append(getIdentifier()).append("\n");

        try {
            output.append("  - properties :");
            if (properties != null && !properties.isEmpty()) {
                output.append("\n");
                for (Map.Entry<Object, Object> property : properties.entrySet()) {
                    output.append("       ").append(property.getKey()).append(" -> [").append(property.getValue()).append(
                            "]\n");
                }
            } else {
                output.append(" -no properties-\n");
            }

            // Add the user members useranames detail
            output.append("  - members : ");

            if (mMembers != null) {
                if (mMembers.size() > 0) {
                    for (Principal member : mMembers) {
                        output.append(member.getName()).append("/");
                    }
                } else {
                    output.append(" -no members-\n");
                }
            } else {
                output.append(" -preloading of members disabled-\n");
            }
        } catch (Exception e) {
            // Group might be already deleted
            logger.debug("Error while generating toString output for group " + mGroupname, e);
        }

        return output.toString();
    }

    /**
     * Get the name of the provider of this group.
     *
     * @return String representation of the name of the provider of this group
     */
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    public JCRNodeWrapper getNode(JCRSessionWrapper session) throws RepositoryException {
        return session.getNodeByIdentifier(getIdentifier());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JCRGroup jcrGroup = (JCRGroup) o;

        return nodeUuid.equals(jcrGroup.nodeUuid);

    }

    public String getIdentifier() {
        return nodeUuid;
    }

    /**
     * @return the external
     */
    public boolean isExternal() {
        return external;
    }
}
