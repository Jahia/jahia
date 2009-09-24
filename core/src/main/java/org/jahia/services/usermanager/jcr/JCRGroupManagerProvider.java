/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.usermanager.jcr;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerProvider;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 8 juil. 2009
 */
public class JCRGroupManagerProvider extends JahiaGroupManagerProvider {

    public static final String USERS_GROUPNAME = "users";
    public static final String ADMINISTRATORS_GROUPNAME = "administrators";
    public static final String GUEST_GROUPNAME = "guest";

    private transient static Logger logger = Logger.getLogger(JCRGroupManagerProvider.class);
    private transient JCRTemplate jcrTemplate;
    private static JCRGroupManagerProvider mGroupManagerProvider;
    private transient JahiaSitesService sitesService;
    private transient CacheService cacheService;
    private transient Cache cache;

    /**
     * Create an new instance of the User Manager Service if the instance do not
     * exist, or return the existing instance.
     *
     * @return Return the instance of the User Manager Service.
     */
    public static JCRGroupManagerProvider getInstance() {
        if (mGroupManagerProvider == null) {
            mGroupManagerProvider = new JCRGroupManagerProvider();
        }
        return mGroupManagerProvider;
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Create a new group in the system.
     *
     * @param hidden
     * @return Retrun a reference on a group object on success, or if the groupname
     *         already exists or another error occured, null is returned.
     */
    public JahiaGroup createGroup(final int siteID, final String name, final Properties properties,
                                  final boolean hidden) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaGroup>() {
                public JahiaGroup doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        JCRNodeWrapper nodeWrapper;
                        if (siteID == 0) {
                            JCRNodeWrapper parentNodeWrapper = session.getNode("/" + Constants.CONTENT + "/groups");
                            nodeWrapper = parentNodeWrapper.addNode(name, Constants.JAHIANT_GROUP);
                        } else {
                            String siteName = sitesService.getSite(siteID).getSiteKey();
                            JCRNodeWrapper parentNodeWrapper = session.getNode(
                                    "/" + Constants.CONTENT + "/sites/" + siteName + "/groups");
                            nodeWrapper = parentNodeWrapper.addNode(name, Constants.JAHIANT_GROUP);
                        }
                        nodeWrapper.setProperty(JCRGroup.J_HIDDEN, hidden);
                        if (properties != null) {
                            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                                nodeWrapper.setProperty((String) entry.getKey(), (String) entry.getValue());
                            }
                        }
                        session.save();
                        return new JCRGroup(nodeWrapper, jcrTemplate.getSessionFactory(), siteID);
                    } catch (JahiaException e) {
                        logger.error(e);
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e);
            return null;
        }
    }

    public boolean deleteGroup(JahiaGroup group) {
        if (group instanceof JCRGroup) {
            final JCRGroup jcrGroup = (JCRGroup) group;
            try {
                return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        Node node = session.getNodeByUUID(jcrGroup.getNodeUuid());
                        node.remove();
                        session.save();
                        return true;
                    }
                });

            } catch (RepositoryException e) {
                logger.error(e);
            }
        }
        return false;
    }

    /**
     * Get all JahiaSite objects where the user has an access.
     *
     * @param user, the user you want to get his access grantes sites list.
     * @return Return a List containing all JahiaSite objects where the user has an access.
     */
    public List<JahiaSite> getAdminGrantedSites(JahiaUser user) {
        return null;
    }

    /**
     *
     */
    public JahiaGroup getAdministratorGroup(int siteID) {
        return lookupGroup(siteID, ADMINISTRATORS_GROUPNAME);
    }

    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group keys of a site.
     *
     * @return Return a List of identifier of all groups of this site.
     * @auhtor NK
     */
    public List<String> getGroupList() {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<List<String>>() {
                public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    List<String> groups = new ArrayList<String>();
                    if (session.getWorkspace().getQueryManager() != null) {
                        String query = "SELECT group.[j:nodename] FROM [" + Constants.JAHIANT_GROUP + "] as group ORDER BY group.[j:nodename]";
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
                }
            });
        } catch (RepositoryException e) {
            logger.error(e);
            return new ArrayList<String>();
        }
    }

    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group keys of a site.
     *
     * @return Return a List of identifier of all groups of this site.
     * @auhtor NK
     */
    public List<String> getGroupList(final int siteID) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<List<String>>() {
                public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    final List<String> groups = new ArrayList<String>();
                    try {
                        if (session.getWorkspace().getQueryManager() != null) {
                            String siteName = sitesService.getSite(siteID).getSiteKey();
                            String query = "SELECT group.[j:nodename] FROM [" + Constants.JAHIANT_GROUP + "] as group WHERE group.[jcr:path] LIKE '/" + Constants.CONTENT + "/sites/" + siteName + "/groups/%' ORDER BY group.[j:nodename]";
                            Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                            QueryResult qr = q.execute();
                            RowIterator rows = qr.getRows();
                            while (rows.hasNext()) {
                                Row groupsFolderNode = rows.nextRow();
                                String groupName = "{jcr}" + groupsFolderNode.getValue(
                                        "j:nodename").getString() + ":" + siteID;
                                if (!groups.contains(groupName)) {
                                    groups.add(groupName);
                                }
                            }
                        }
                    } catch (JahiaException e) {
                        logger.error(e);
                    }
                    return groups;
                }
            });

        } catch (RepositoryException e) {
            logger.error(e);
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
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<List<String>>() {
                public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    List<String> groups = new ArrayList<String>();
                    if (session.getWorkspace().getQueryManager() != null) {
                        String query = "SELECT group[j:nodename] FROM [" + Constants.JAHIANT_GROUP + "] as group ORDER BY group.[j:nodename]";
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
                }
            });
        } catch (RepositoryException e) {
            logger.error(e);
            return new ArrayList<String>();
        }
    }

    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group names of a site.
     *
     * @return Return a List of strings containing all the group names.
     */
    public List<String> getGroupnameList(final int siteID) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<List<String>>() {
                public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    List<String> groups = new ArrayList<String>();
                    try {
                        if (session.getWorkspace().getQueryManager() != null) {
                            String siteName = sitesService.getSite(siteID).getSiteKey();
                            String query = "SELECT group.[j:nodename] FROM [" + Constants.JAHIANT_GROUP + "] as group WHERE group.[jcr:path] LIKE '/" + Constants.CONTENT + "/sites/" + siteName + "/groups/%' ORDER BY group.[j:nodename]";
                            Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                            QueryResult qr = q.execute();
                            RowIterator rows = qr.getRows();
                            while (rows.hasNext()) {
                                Row groupsFolderNode = rows.nextRow();
                                String groupName = groupsFolderNode.getValue("j:nodename").getString() + ":" + siteID;
                                if (!groups.contains(groupName)) {
                                    groups.add(groupName);
                                }
                            }
                        }
                    } catch (JahiaException e) {
                        logger.error(e);
                    }
                    return groups;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e);
            return new ArrayList<String>();
        }
    }

    /**
     * Return an instance of the guest group
     *
     * @return Return the instance of the guest group. Return null on any failure.
     */
    public JahiaGroup getGuestGroup(int siteID) {
        return lookupGroup(0, GUEST_GROUPNAME);
    }

    /**
     * Return the list of groups to which the specified user has access.
     *
     * @param user Valid reference on an existing group.
     * @return Return a List of strings holding all the group names to
     *         which the user as access. On any error, the returned List
     *         might be null.
     */
    public List<String> getUserMembership(JahiaUser user) {
        if (user instanceof JCRUser) {
            final JCRUser jcrUser = (JCRUser) user;
            try {
                return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<List<String>>() {
                    public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        List<String> groups = new ArrayList<String>();
                        try {
                            if (session.getWorkspace().getQueryManager() != null) {
                                String query = "SELECT * FROM [jnt:member] as m where m.[j:member] = '" + jcrUser.getNodeUuid() + "' ORDER BY m.[j:nodename]";
                                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                                QueryResult qr = q.execute();
                                NodeIterator nodes = qr.getNodes();
                                while (nodes.hasNext()) {
                                    Node memberNode = nodes.nextNode();
                                    Node group = memberNode.getParent().getParent();
                                    int siteID;
                                    try {
                                        siteID = sitesService.getSiteByKey(
                                                group.getParent().getParent().getName()).getID();
                                    } catch (NullPointerException e) {
                                        siteID = 0;
                                    }
                                    groups.add(group.getName() + ":" + siteID);
                                }
                            }
                        } catch (JahiaException e) {
                            logger.error(e);
                        }
                        return groups;
                    }
                });
            } catch (RepositoryException e) {
                logger.error(e);
            }
        }
        return new ArrayList<String>();
    }

    /**
     * Return an instance of the users group.
     *
     * @return Return the instance of the users group. Return null on any failure
     */
    public JahiaGroup getUsersGroup(int siteID) {
        return lookupGroup(0, USERS_GROUPNAME);
    }

    /**
     * This function checks on a gived site if the groupname has already been
     * assigned to another group.
     *
     * @param int       siteID the site id
     * @param groupname String representing the unique group name.
     * @return Return true if the specified username has not been assigned yet,
     *         return false on any failure.
     */
    public boolean groupExists(final int siteID, final String name) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        Node usersFolderNode;
                        if (siteID == 0) {
                            usersFolderNode = session.getNode("/" + Constants.CONTENT + "/groups/" + name.trim());
                        } else {
                            String siteName = sitesService.getSite(siteID).getSiteKey();
                            usersFolderNode = session.getNode(
                                    "/" + Constants.CONTENT + "/sites/" + siteName + "/groups/" + name.trim());
                        }
                        return usersFolderNode != null;
                    } catch (JahiaException e) {
                        logger.error(e);
                        return false;
                    }
                }
            });

        } catch (PathNotFoundException e) {
            logger.debug(e);
        } catch (RepositoryException e) {
            logger.warn(e);
        }
        return false;
    }

    /**
     * Lookup the group information from the underlaying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cahce from the database.
     *
     * @param String groupKey Group's unique identification key.
     * @return Return a reference on a the specified group name. Return null
     *         if the group doesn't exist or when any error occured.
     */
    public JahiaGroup lookupGroup(String groupKey) {
        JCRSessionWrapper session = null;
        try {
            if (cache == null) {
                start();
            }
            int siteID = 0;
            final String name;
            if (groupKey.contains(":")) {
                String[] splittedGroupKey = groupKey.split(":");
                siteID = Integer.valueOf(splittedGroupKey[1]);
                name = splittedGroupKey[0];
                if (GUEST_GROUPNAME.equals(name) || USERS_GROUPNAME.equals(name)) {
                    siteID = 0;
                }
            } else {
                name = groupKey;
            }
            final String trueGroupKey = name + ":" + siteID;
            if (cache.containsKey(trueGroupKey)) {
                return (JahiaGroup) cache.get(trueGroupKey);
            }
            final int siteID1 = siteID;
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaGroup>() {
                public JahiaGroup doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        Node usersFolderNode;
                        if (siteID1 == 0) {
                            usersFolderNode = session.getNode("/" + Constants.CONTENT + "/groups/" + name.trim());
                        } else {
                            String siteName = sitesService.getSite(siteID1).getSiteKey();
                            usersFolderNode = session.getNode(
                                    "/" + Constants.CONTENT + "/sites/" + siteName + "/groups/" + name.trim());
                        }
                        JCRGroup group = new JCRGroup(usersFolderNode, jcrTemplate.getSessionFactory(), siteID1);
                        cache.put(trueGroupKey, group);
                        return group;
                    } catch (JahiaException e) {
                        logger.error(e);
                        return null;
                    }
                }
            });
        } catch (PathNotFoundException e) {
            logger.debug(e);
        } catch (RepositoryException e) {
            logger.warn(e);
        } catch (JahiaInitializationException e) {
            logger.error(e);
        } catch (JahiaException e) {
            logger.error(e);
        }
        return null;
    }

    /**
     * Lookup the group information from the underlaying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cahce from the database.
     *
     * @param int  siteID the site id
     * @param name Group's unique identification name.
     * @return Return a reference on a the specified group name. Return null
     *         if the group doesn't exist or when any error occured.
     */
    public JahiaGroup lookupGroup(int siteID, final String name) {
        JCRSessionWrapper session = null;
        try {
            if (cache == null) {
                start();
            }
            if (name.equals(GUEST_GROUPNAME) || name.equals(USERS_GROUPNAME)) {
                siteID = 0;
            }
            final String trueGroupKey = name + ":" + siteID;
            if (cache.containsKey(trueGroupKey)) {
                return (JahiaGroup) cache.get(trueGroupKey);
            }
            final int siteID1 = siteID;
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaGroup>() {
                public JahiaGroup doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        Node usersFolderNode;
                        if (siteID1 <= 0) {
                            usersFolderNode = session.getNode("/" + Constants.CONTENT + "/groups/" + name.trim());
                        } else {
                            String siteName = sitesService.getSite(siteID1).getSiteKey();
                            usersFolderNode = session.getNode(
                                    "/" + Constants.CONTENT + "/sites/" + siteName + "/groups/" + name.trim());
                        }
                        JCRGroup group = new JCRGroup(usersFolderNode, jcrTemplate.getSessionFactory(), siteID1);
                        cache.put(trueGroupKey, group);
                        return group;
                    } catch (JahiaException e) {
                        logger.error(e);
                        return null;
                    }
                }
            });

        } catch (PathNotFoundException e) {
            logger.debug(e);
        } catch (RepositoryException e) {
            logger.warn(e);
        } catch (JahiaInitializationException e) {
            logger.error(e);
        }
        return null;
    }

    /**
     * Remove the specified user from all the membership lists of all the groups.
     *
     * @param user Reference on an existing user.
     * @return Return true on success, or false on any failure.
     */
    public boolean removeUserFromAllGroups(final JahiaUser user) {
        try {
            if (user instanceof JCRUser) {
                return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRUser jcrUser = (JCRUser) user;
                        if (session.getWorkspace().getQueryManager() != null) {
                            String query = "SELECT * FROM [jnt:member] as m where m.[j:member] = '" + jcrUser.getNodeUuid() + "' ORDER BY m.[j:nodename]";
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
            logger.error(e);
        }
        return false;
    }

    public Set<JahiaGroup> searchGroups(final int siteID, final Properties searchCriterias) {

        Session session = null;
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Set<JahiaGroup>>() {
                public Set<JahiaGroup> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Set<JahiaGroup> users = new HashSet<JahiaGroup>();
                    if (session.getWorkspace().getQueryManager() != null) {
                        StringBuffer query = new StringBuffer("SELECT * FROM [" + Constants.JAHIANT_GROUP + "] as g");
                        if (searchCriterias != null && searchCriterias.size() > 0) {
                            // Avoid wildcard attribute
                            if (!(searchCriterias.containsKey(
                                    "*") && searchCriterias.size() == 1 && searchCriterias.getProperty("*").equals(
                                    "*"))) {
                                query.append(" WHERE ");
                                Iterator<Map.Entry<Object, Object>> objectIterator = searchCriterias.entrySet().iterator();
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
                                        propertyValue = propertyValue + "%";
                                    }
                                    if ("*".equals(propertyKey)) {
                                        query.append("CONTAINS(g.*,'" + propertyValue.replaceAll("%", "") + "')");
                                    } else {
                                        query.append("g.[" + propertyKey.replaceAll("\\.", "\\\\.") + "]").append(
                                                " LIKE '").append(propertyValue).append("'");
                                    }
                                    if (objectIterator.hasNext()) {
                                        query.append(" OR ");
                                    }
                                }
                            }
                        }
                        query.append(" ORDER BY g.[j:nodename]");
                        if (logger.isDebugEnabled()) {
                            logger.debug(query);
                        }
                        Query q = session.getWorkspace().getQueryManager().createQuery(query.toString(),
                                                                                       Query.JCR_SQL2);
                        QueryResult qr = q.execute();
                        NodeIterator ni = qr.getNodes();
                        while (ni.hasNext()) {
                            Node usersFolderNode = ni.nextNode();
                            users.add(new JCRGroup(usersFolderNode, jcrTemplate.getSessionFactory(), siteID));
                        }
                    }
                    return users;
                }
            });

        } catch (RepositoryException e) {
            logger.error(e);
            return new HashSet<JahiaGroup>();
        }
    }

    /**
     * This method indicates that any internal cache for a provider should be
     * updated because the value has changed and needs to be transmitted to the
     * other nodes in a clustering environment.
     *
     * @param jahiaGroup JahiaGroup the group to be updated in the cache.
     */
    public void updateCache(JahiaGroup jahiaGroup) {
        cache.remove(jahiaGroup.getGroupKey());
    }

    public void start() throws JahiaInitializationException {
        if (cacheService != null) {
            cache = cacheService.createCacheInstance("JCR_GROUP_CACHE");
        }
    }

    public void stop() throws JahiaException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
