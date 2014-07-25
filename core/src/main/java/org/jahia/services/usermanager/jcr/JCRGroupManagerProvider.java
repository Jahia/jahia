/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.usermanager.jcr;

import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.*;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.*;

/**
 * JCR-based implementation of the Group manager provider interface
 *
 * @author rincevent
 * @since JAHIA 6.5
 */
public class JCRGroupManagerProvider extends JahiaGroupManagerProvider {
    private transient static Logger logger = LoggerFactory.getLogger(JCRGroupManagerProvider.class);
    private transient JCRTemplate jcrTemplate;
    private static JCRGroupManagerProvider mGroupManagerProvider = new JCRGroupManagerProvider();
    private JahiaUserManagerService userManagerProvider;

    /**
     * Create an new instance of the User Manager Service if the instance do not
     * exist, or return the existing instance.
     *
     * @return the instance of the User Manager Service
     */
    public static JCRGroupManagerProvider getInstance() {
        return mGroupManagerProvider;
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    /**
     * Create a new group in the system.
     *
     * @param hidden
     * @return a reference on a group object on success, or if the groupname
     * already exists or another error occurred, null is returned.
     */
    public JCRGroupNode createGroup(final String siteKey, final String name, final Properties properties,
                                    final boolean hidden) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JCRGroupNode>() {
                public JCRGroupNode doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRGroupNode nodeWrapper;
                    JCRNodeWrapper parentNodeWrapper;
                    if (siteKey == null) {
                        parentNodeWrapper = session.getNode("/groups");
                    } else {
                        parentNodeWrapper = session.getNode("/sites/" + siteKey + "/groups");
                    }
                    nodeWrapper = (JCRGroupNode) parentNodeWrapper.addNode(name, Constants.JAHIANT_GROUP);
                    nodeWrapper.setProperty(JCRGroupNode.J_HIDDEN, hidden);
                    if (properties != null) {
                        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                            if (entry.getValue() instanceof Boolean) {
                                nodeWrapper.setProperty((String) entry.getKey(), (Boolean) entry.getValue());
                            } else {
                                nodeWrapper.setProperty((String) entry.getKey(), (String) entry.getValue());
                            }
                        }
                    }
                    session.save();
                    return nodeWrapper;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while creating group", e);
            return null;
        }
    }

    public boolean deleteGroup(String groupPath) {
        try {
            Boolean aBoolean = jcrTemplate.doExecuteWithSystemSession(deleteCallback(groupPath));
            aBoolean = aBoolean && jcrTemplate.doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, deleteCallback(groupPath));
            return aBoolean;
        } catch (RepositoryException e) {
            logger.error("Error while deleting group", e);
        }
        return false;
    }

    private JCRCallback<Boolean> deleteCallback(final String groupPath) {
        return new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                Node node = null;
                try {
                    node = session.getNode(groupPath);
                } catch (ItemNotFoundException e) {
                    return true;
                }

                PropertyIterator pi = node.getWeakReferences("j:member");
                while (pi.hasNext()) {
                    JCRPropertyWrapper member = (JCRPropertyWrapper) pi.next();
                    member.getParent().remove();
                }

                node.remove();
                session.save();
                return true;
            }
        };
    }

    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group keys of a site.
     *
     * @param siteKey
     * @return Return a List of identifier of all groups of this site.
     * @author NK
     */
    public List<String> getGroupList(String siteKey) {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            List<String> groups = new ArrayList<String>();
            if (session.getWorkspace().getQueryManager() != null) {
                String groupPath;
                if (siteKey != null)
                    groupPath = "'/sites/'" + siteKey + "/groups'";
                else
                    groupPath = "'/groups";
                String query =
                        "SELECT [j:nodename] FROM [" + Constants.JAHIANT_GROUP + "] as group WHERE group.[j:external] = 'false' and ischildnode(group," + groupPath + "] ORDER BY group.[j:nodename]";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                QueryResult qr = q.execute();
                RowIterator rows = qr.getRows();
                while (rows.hasNext()) {
                    Row groupsFolderNode = rows.nextRow();
                    String groupName = "{jcr}" + groupsFolderNode.getValue("j:nodename").getString();
                    if (!groups.contains(groupName)) {
                        groups.add(groupName);
                    }
                }
            }
            return groups;
        } catch (RepositoryException e) {
            logger.error("Error retrieving group list", e);
            return new ArrayList<String>();
        }
    }

    public List<String> getGroupList() {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            List<String> groups = new ArrayList<String>();
            if (session.getWorkspace().getQueryManager() != null) {
                String query =
                        "SELECT [j:nodename] FROM [" + Constants.JAHIANT_GROUP + "] as group WHERE group.[j:external] = 'false' ORDER BY group.[j:nodename]";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                QueryResult qr = q.execute();
                RowIterator rows = qr.getRows();
                while (rows.hasNext()) {
                    Row groupsFolderNode = rows.nextRow();
                    String groupName = "{jcr}" + groupsFolderNode.getValue("j:nodename").getString();
                    if (!groups.contains(groupName)) {
                        groups.add(groupName);
                    }
                }
            }
            return groups;
        } catch (RepositoryException e) {
            logger.error("Error retrieving group list", e);
            return new ArrayList<String>();
        }
    }

    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group names.
     *
     * @return Return a List of strings containing all the group names.
     */
    public List<String> getGroupnameList() {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            List<String> groups = new ArrayList<String>();
            if (session.getWorkspace().getQueryManager() != null) {
                String query =
                        "SELECT [j:nodename] FROM [" + Constants.JAHIANT_GROUP + "] as group WHERE group.[j:external] = 'false' ORDER BY group.[j:nodename]";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                QueryResult qr = q.execute();
                RowIterator rows = qr.getRows();
                while (rows.hasNext()) {
                    Row groupsFolderNode = rows.nextRow();
                    String groupName = groupsFolderNode.getValue("j:nodename").getString();
                    if (!groups.contains(groupName)) {
                        groups.add(groupName);
                    }
                }
            }
            return groups;
        } catch (RepositoryException e) {
            logger.error("Error retrieving group name list", e);
            return new ArrayList<String>();
        }
    }

    /**
     * Return the list of groups to which the specified user has access.
     *
     * @param user Valid reference on an existing group.
     * @return Return a List of strings holding all the group names to
     * which the user as access. On any error, the returned List
     * might be null.
     */
    public List<String> getUserMembership(String username) throws RepositoryException {
        JCRUserNode userNode = userManagerProvider.lookupUser(username);
        if(userNode!=null) {
            try {
                JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
                Set<String> groups = new LinkedHashSet<String>();
                try {
                    recurseOnGroups(session, groups, userNode);
                } catch (JahiaException e) {
                    logger.warn("Error retrieving membership for user " + username, e);
                }
                if (!username.equals(JahiaUserManagerService.GUEST_USERNAME)) {
                    groups.add(JahiaGroupManagerService.USERS_GROUPPATH);
                }
                groups.add(JahiaGroupManagerService.GUEST_GROUPPATH);
                return new LinkedList<String>(groups);
            } catch (RepositoryException e) {
                logger.error("Error retrieving membership for user " + username + ", will return empty list", e);
            }
        }
        return new ArrayList<String>();
    }

    private void recurseOnGroups(JCRSessionWrapper session, Set<String> groups, JCRNodeWrapper principal) throws RepositoryException, JahiaException {
        PropertyIterator weakReferences = principal.getWeakReferences("j:member");
        while (weakReferences.hasNext()) {
            try {
                Property property = weakReferences.nextProperty();
                if (property.getPath().contains("j:members")) {
                    JCRNodeWrapper group = (JCRNodeWrapper) property.getParent().getParent().getParent();
                    if (group.isNodeType("jnt:group")) {
                        if (groups.add(group.getPath())) {
                            // recurse on the found group only we have not done it yet
                            recurseOnGroups(session, groups, group);
                        }
                    }
                }
            } catch (ItemNotFoundException e) {
                logger.warn("Cannot find group for " + principal.getPath(), e);
            }
        }
    }

    /**
     * Lookup the group information from the underlying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cache from the database.
     *
     * @param groupKey Group's unique identification key.
     * @return Return a reference on a the specified group name. Return null
     * if the group doesn't exist or when any error occurred.
     */
    public JCRGroupNode lookupGroup(String groupPath) throws RepositoryException {
        return lookupGroup(groupPath, false);
    }

    /**
     * Lookup the external group information from the JCR.
     *
     * @param name Group's unique identification name.
     * @return an instance of the {@link JCRGroup} for an external group or null
     * if the group cannot be found
     */
    public JCRGroupNode lookupExternalGroup(final String name) throws RepositoryException {
        return lookupGroup(name, true);
    }

    /**
     * Lookup the group information from the underlying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cache from the database.
     *
     * @param siteID the site id
     * @param name   Group's unique identification name.
     * @return Return a reference on a the specified group name. Return null
     * if the group doesn't exist or when any error occurred.
     */
    private JCRGroupNode lookupGroup(final String name, final boolean allowExternal) throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
        JCRGroupNode groupNode;
        groupNode = (JCRGroupNode) session.getNode(name);
        return groupNode;
    }


    /**
     * Remove the specified user from all the membership lists of all the groups.
     *
     * @param user Reference on an existing user.
     * @return Return true on success, or false on any failure.
     */
    public boolean removeUserFromAllGroups(final String userPath) {
        try {
            final String uuid = userManagerProvider.lookupUser(userPath).getIdentifier();
            if (uuid != null) {
                return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        if (session.getWorkspace().getQueryManager() != null) {
                            String query = "SELECT * FROM [jnt:member] as m where m.[j:member] = '" + uuid +
                                    "' ORDER BY m.[j:nodename]";
                            Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                            QueryResult qr = q.execute();
                            NodeIterator nodes = qr.getNodes();
                            while (nodes.hasNext()) {
                                Node memberNode = nodes.nextNode();
                                memberNode.remove();
                            }
                            session.save();
                            return true;
                        }
                        return false;
                    }
                });
            }
        } catch (RepositoryException e) {
            logger.error("Error while removing user from all groups", e);
        }
        return false;
    }

    @Override
    public JCRGroupNode getAmdinistratorGroup(String siteKey) throws RepositoryException {
        return lookupGroup(siteKey == null ? "/groups/" + JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME : "/sites/" + siteKey + "/" + JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME);

    }

    public Set<JCRGroupNode> searchGroups(final String siteKey, final Properties searchCriterias) {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            Set<JCRGroupNode> users = new HashSet<JCRGroupNode>();
            if (session.getWorkspace().getQueryManager() != null) {
                StringBuilder query = new StringBuilder(
                        "SELECT * FROM [" + Constants.JAHIANT_GROUP + "] as g WHERE g.[" +
                                JCRUserNode.J_EXTERNAL + "] = 'false'"
                );
                if (siteKey != null) {
                    query.append(" AND ISCHILDNODE(g, '/groups')");
                } else {
                    query.append(" AND ISCHILDNODE(g, '/sites/").append(siteKey).append("/groups')");
                }
                if (searchCriterias != null && searchCriterias.size() > 0) {
                    // Avoid wildcard attribute
                    if (!(searchCriterias.containsKey("*") && searchCriterias.size() == 1 &&
                            searchCriterias.getProperty("*").equals("*"))) {
                        Iterator<Map.Entry<Object, Object>> objectIterator =
                                searchCriterias.entrySet().iterator();
                        if (objectIterator.hasNext()) {
                            query.append(" AND (");
                            while (objectIterator.hasNext()) {
                                Map.Entry<Object, Object> entry = objectIterator.next();
                                String propertyKey = (String) entry.getKey();
                                if ("groupname".equals(propertyKey)) {
                                    propertyKey = "j:nodename";
                                }
                                String propertyValue = (String) entry.getValue();
                                if ("*".equals(propertyValue)) {
                                    propertyValue = "%";
                                } else {
                                    if (propertyValue.contains("*")) {
                                        propertyValue = Patterns.STAR.matcher(propertyValue).replaceAll("%");
                                    } else {
                                        propertyValue = propertyValue + "%";
                                    }
                                }
                                if ("*".equals(propertyKey)) {
                                    query.append("(CONTAINS(g.*,'").append(Patterns.PERCENT.matcher(propertyValue).replaceAll("")).append("') OR LOWER(g.[j:nodename]) LIKE '")
                                            .append(propertyValue.toLowerCase()).append("') ");
                                } else {
                                    query.append("LOWER(g.[").append(Patterns.DOT.matcher(propertyKey).replaceAll("\\\\.")).append("])")
                                            .append(" LIKE '").append(propertyValue.toLowerCase()).append("'");
                                }
                                if (objectIterator.hasNext()) {
                                    query.append(" OR ");
                                }
                            }
                            query.append(")");
                        }
                    }
                }
                query.append(" ORDER BY g.[j:nodename]");
                if (logger.isDebugEnabled()) {
                    logger.debug(query.toString());
                }
                Query q = session.getWorkspace().getQueryManager()
                        .createQuery(query.toString(), Query.JCR_SQL2);
                QueryResult qr = q.execute();
                NodeIterator ni = qr.getNodes();
                while (ni.hasNext()) {
                    Node usersFolderNode = ni.nextNode();
                    users.add((JCRGroupNode) usersFolderNode);
                }
            }
            return users;
        } catch (RepositoryException e) {
            logger.error("Error while searching groups", e);
            return new HashSet<JCRGroupNode>();
        }
    }

    @Override
    public void start() throws JahiaInitializationException {

    }

    public void stop() throws JahiaException {
        // do nothing
    }

    /**
     * @param userManagerProvider the userManagerProvider to set
     */
    public void setUserManagerProvider(JahiaUserManagerService userManagerProvider) {
        this.userManagerProvider = userManagerProvider;
    }

}