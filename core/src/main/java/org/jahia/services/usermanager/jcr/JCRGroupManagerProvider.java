/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.services.usermanager.jcr;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.drools.core.util.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.CacheHelper;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.*;
import org.jahia.utils.ClassLoaderUtils;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

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
public class JCRGroupManagerProvider extends JahiaGroupManagerProvider implements ApplicationListener<ProviderEvent> {
    private transient static Logger logger = LoggerFactory.getLogger(JCRGroupManagerProvider.class);
    private transient JCRTemplate jcrTemplate;
    private static JCRGroupManagerProvider mGroupManagerProvider = new JCRGroupManagerProvider();
    private JCRUserManagerProvider userManagerProvider;
    private transient JahiaSitesService sitesService;
    private transient Ehcache cache;
    private Ehcache membershipCache;
    private ClassLoader chainedClassLoader;
    private boolean chainedClassLoaderInitialized;
    private EhCacheProvider cacheProvider;
    public static final String JCR_GROUPMEMBERSHIP_CACHE = "JCRGroupMembershipCache";

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

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    /**
     * Create a new group in the system.
     *
     * @param hidden
     * @return a reference on a group object on success, or if the groupname
     * already exists or another error occurred, null is returned.
     */
    public JCRGroup createGroup(final int siteID, final String name, final Properties properties,
                                final boolean hidden) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JCRGroup>() {
                public JCRGroup doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper nodeWrapper;
                    JCRNodeWrapper parentNodeWrapper;
                    if (siteID == 0) {
                        parentNodeWrapper = session.getNode("/groups");
                    } else {
                        String siteName = sitesService.getSiteKeyById(siteID);
                        parentNodeWrapper = session.getNode("/sites/" + siteName + "/groups");
                    }
                    nodeWrapper = parentNodeWrapper.addNode(name, Constants.JAHIANT_GROUP);
                    nodeWrapper.setProperty(JCRGroup.J_HIDDEN, hidden);
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
                    JCRGroup jcrGroup = new JCRGroup(nodeWrapper, siteID);
                    cachePut(jcrGroup.getGroupKey(), jcrGroup);
                    return jcrGroup;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while creating group", e);
            return null;
        }
    }

    public boolean deleteGroup(JahiaGroup group) {
        if (group instanceof JCRGroup) {
            final JCRGroup jcrGroup = (JCRGroup) group;
            List<String> membership = null;
            try {
                membership = getMembership(jcrGroup);
                Boolean aBoolean = jcrTemplate.doExecuteWithSystemSession(deleteCallback(jcrGroup));
                aBoolean = aBoolean && jcrTemplate.doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, deleteCallback(jcrGroup));
                return aBoolean;
            } catch (RepositoryException e) {
                logger.error("Error while deleting group", e);
            } finally {
                cache.remove(group.getGroupKey());
                membershipCache.removeAll();
                invalidateCachesForMembership(membership);
            }
        }
        return false;
    }

    private JCRCallback<Boolean> deleteCallback(final JCRGroup jcrGroup) {
        return new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                Node node = null;
                try {
                    node = jcrGroup.getNode(session);
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
        return lookupGroup(siteID, siteID == 0 ? JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME : JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME);
    }

    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group keys of a site.
     *
     * @return Return a List of identifier of all groups of this site.
     * @author NK
     */
    public List<String> getGroupList() {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            List<String> groups = new ArrayList<String>();
            if (session.getWorkspace().getQueryManager() != null) {
                String query =
                        "SELECT [j:nodename] FROM [" + Constants.JAHIANT_GROUP + "] as group WHERE group.[" +
                                JCRGroup.J_EXTERNAL + "] = 'false' ORDER BY group.[j:nodename]";
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
     * group keys of a site.
     *
     * @return Return a List of identifier of all groups of this site.
     * @author NK
     */
    public List<String> getGroupList(final int siteID) {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            final List<String> groups = new ArrayList<String>();
            if (session.getWorkspace().getQueryManager() != null) {
                StringBuffer query = new StringBuffer("SELECT [j:nodename] FROM [" + Constants.JAHIANT_GROUP +
                        "] as group WHERE group.[" + JCRGroup.J_EXTERNAL +
                        "] = 'false'");
                if (siteID <= 0) {
                    query.append(" AND ISCHILDNODE(group, '/groups");
                    ;
                } else {
                    String siteName = sitesService.getSiteKeyById(siteID);
                    query.append(" AND ISCHILDNODE(group, '/sites/").append(siteName).append("/groups')");
                }
                query.append(" ORDER BY group.[j:nodename]");

                Query q = session.getWorkspace().getQueryManager().createQuery(query.toString(), Query.JCR_SQL2);
                QueryResult qr = q.execute();
                RowIterator rows = qr.getRows();
                while (rows.hasNext()) {
                    Row groupsFolderNode = rows.nextRow();
                    String groupName =
                            "{jcr}" + groupsFolderNode.getValue("j:nodename").getString() + ":" + siteID;
                    if (!groups.contains(groupName)) {
                        groups.add(groupName);
                    }
                }
            }
            return groups;
        } catch (RepositoryException e) {
            logger.error("Error retrieving group list for site " + siteID, e);
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
                        "SELECT [j:nodename] FROM [" + Constants.JAHIANT_GROUP + "] as group WHERE group.[" +
                                JCRGroup.J_EXTERNAL + "] = 'false' ORDER BY group.[j:nodename]";
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
     * Return a <code>List</code) of <code>String</code> representing all the
     * group names of a site.
     *
     * @return Return a List of strings containing all the group names.
     */
    public List<String> getGroupnameList(final int siteID) {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            List<String> groups = new ArrayList<String>();
            if (session.getWorkspace().getQueryManager() != null) {
                StringBuilder query = new StringBuilder("SELECT [j:nodename] FROM [" + Constants.JAHIANT_GROUP +
                        "] as group WHERE group.[" + JCRGroup.J_EXTERNAL +
                        "] = 'false'");
                if (siteID <= 0) {
                    query.append(" AND ISCHILDNODE(group, '/groups')");
                } else {
                    String siteName = sitesService.getSiteKeyById(siteID);
                    query.append(" AND ISCHILDNODE(group, '/sites/" + siteName + "/groups')");
                }
                query.append(" ORDER BY group.[j:nodename]");

                Query q = session.getWorkspace().getQueryManager().createQuery(query.toString(), Query.JCR_SQL2);
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
            logger.error("Error retrieving group name list for site " + siteID, e);
            return new ArrayList<String>();
        }
    }

    /**
     * Return an instance of the guest group
     *
     * @return Return the instance of the guest group. Return null on any failure.
     */
    public JahiaGroup getGuestGroup(int siteID) {
        return lookupGroup(0, JahiaGroupManagerService.GUEST_GROUPNAME);
    }

    /**
     * Return the list of groups to which the specified user has access.
     *
     * @param user Valid reference on an existing group.
     * @return Return a List of strings holding all the group names to
     * which the user as access. On any error, the returned List
     * might be null.
     */
    public List<String> getUserMembership(JahiaUser user) {
        return getMembership(user);
    }

    /**
     * Return the list of groups to which the specified principal (user or group) belongs.
     *
     * @param principal a JCR user or group
     * @return the list of groups to which the specified principal (user or group) belongs
     */
    public List<String> getMembership(final JahiaPrincipal principal) {
        String uuid = null;
        if (principal instanceof JCRPrincipal) {
            uuid = ((JCRPrincipal) principal).getIdentifier();
        } else {
            if (principal instanceof JahiaUser) {
                JCRUser extUser = userManagerProvider.lookupExternalUser((JahiaUser) principal);
                if (extUser != null) {
                    uuid = extUser.getIdentifier();
                }
            } else if (principal instanceof JahiaGroup) {
                JCRGroup extGroup = lookupExternalGroup(((JahiaGroup) principal).getGroupname());
                if (extGroup != null) {
                    uuid = extGroup.getIdentifier();
                }
            }
        }
        if (uuid != null) {
            try {
                final String principalId = uuid;
                Element membershipElement = membershipCache.get(uuid);
                @SuppressWarnings("unchecked")
                List<String> membership = (List<String>) (membershipElement != null ? membershipElement.getObjectValue() : null);
                if (membership != null) {
                    return membership;
                }
                JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
                Set<String> groups = new LinkedHashSet<String>();
                try {
                    recurseOnGroups(session, groups, principalId);
                } catch (JahiaException e) {
                    logger.warn("Error retrieving membership for user " + principal.getName(), e);
                }
                if (principal instanceof JahiaUser) {
                    if (!principal.getName().equals(JahiaUserManagerService.GUEST_USERNAME)) {
                        groups.add(JahiaGroupManagerService.USERS_GROUPNAME);
                    }
                    groups.add(JahiaGroupManagerService.GUEST_GROUPNAME);
                }
                List<String> result = new LinkedList<String>(groups);
                membershipCache.put(new Element(principalId, result));
                return result;
            } catch (RepositoryException e) {
                logger.error("Error retrieving membership for user " + principal.getName() + ", will return empty list", e);
            }
        }
        return new ArrayList<String>();
    }

    private void recurseOnGroups(JCRSessionWrapper session, Set<String> groups, String principalId) throws RepositoryException, JahiaException {
        JCRNodeWrapper node = session.getNodeByUUID(principalId);
        PropertyIterator weakReferences = node.getWeakReferences("j:member");
        while (weakReferences.hasNext()) {
            try {
                Property property = weakReferences.nextProperty();
                if (property.getPath().contains("j:members")) {
                    Node group = property.getParent().getParent().getParent();
                    if (group.isNodeType("jnt:group")) {
                        int siteID = 0;
                        try {
                            Node siteNode = group.getParent().getParent();
                            if (siteNode instanceof JCRSiteNode) {
                                siteID = ((JCRSiteNode) siteNode).getID();
                            }
                        } catch (NullPointerException e) {
                            siteID = 0;
                        }
                        if (groups.add(group.getName() + ":" + siteID)) {
                            // recurse on the found group only we have not done it yet
                            recurseOnGroups(session, groups, group.getIdentifier());
                        }
                    }
                }
            } catch (ItemNotFoundException e) {
                logger.warn("Cannot find group for " + node.getPath(), e);
            }
        }
    }

    /**
     * Return an instance of the users group.
     *
     * @return Return the instance of the users group. Return null on any failure
     */

    public JahiaGroup getUsersGroup(int siteID) {
        return lookupGroup(0, JahiaGroupManagerService.USERS_GROUPNAME);
    }

    /**
     * This function checks on a given site if the groupname has already been
     * assigned to another group.
     *
     * @param siteID siteID the site id
     * @param name   String representing the unique group name.
     * @return Return true if the specified username has not been assigned yet,
     * return false on any failure.
     */
    public boolean groupExists(final int siteID, final String name) {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            Node groupNode;
            if (siteID == 0) {
                groupNode = session.getNode("/groups/" + name.trim());
            } else {
                String siteName = sitesService.getSiteKeyById(siteID);
                groupNode = session.getNode("/sites/" + siteName + "/groups/" + name.trim());
            }
            return groupNode != null && !groupNode.getProperty(JCRGroup.J_EXTERNAL).getBoolean();
        } catch (PathNotFoundException e) {
            logger.debug("Error testing existence of group " + name + " for site " + siteID, e);
        } catch (RepositoryException e) {
            logger.warn("Error testing existence of group " + name + " for site " + siteID, e);
        }
        return false;
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
    public JahiaGroup lookupGroup(String groupKey) {
        int siteID = 0;
        final String name;
        if (groupKey.contains(":")) {
            String[] splittedGroupKey = Patterns.COLON.split(groupKey);
            siteID = Integer.valueOf(splittedGroupKey[1]);
            name = splittedGroupKey[0];
            if (JahiaGroupManagerService.GUEST_GROUPNAME.equals(name) || JahiaGroupManagerService.USERS_GROUPNAME.equals(name)) {
                siteID = 0;
            }
        } else {
            name = groupKey;
        }
        return lookupGroup(siteID, name);
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
    public JCRGroup lookupGroup(int siteID, final String name) {
        return lookupGroup(siteID, name, false);
    }

    /**
     * Lookup the external group information from the JCR.
     *
     * @param name Group's unique identification name.
     * @return an instance of the {@link JCRGroup} for an external group or null
     * if the group cannot be found
     */
    public JCRGroup lookupExternalGroup(final String name) {
        return lookupGroup(0, name, true);
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
    private JCRGroup lookupGroup(int siteID, final String name, final boolean allowExternal) {
        try {
            if (name.equals(JahiaGroupManagerService.GUEST_GROUPNAME) || name.equals(JahiaGroupManagerService.USERS_GROUPNAME)) {
                siteID = 0;
            }
            final String trueGroupKey = name + ":" + siteID;
            Element cacheEntry = cache.get(trueGroupKey);
            if (cacheEntry != null) {
                JCRGroup group = (JCRGroup) CacheHelper.getObjectValue(cacheEntry);
                if (group == null) {
                    return null;
                }
                return !allowExternal && group.isExternal() ? null : group;
            }
            final int siteID1 = siteID;

            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            Node groupNode;
            try {
                if (siteID1 <= 0) {
                    groupNode = session.getNode("/groups/" + name.trim());
                } else {
                    String site = sitesService.getSiteKeyById(siteID1);
                    if (StringUtils.isEmpty(site)) {
                        logger.debug("Site {} is not available", siteID1);
                        return null;
                    }
                    groupNode = session.getNode("/sites/" + site + "/groups/" + name.trim());
                }
            } catch (PathNotFoundException e) {
                cachePut(trueGroupKey, null);
                return null;
            }
            JCRGroup group = null;
            boolean external = groupNode.getProperty(JCRGroup.J_EXTERNAL).getBoolean();
            if (allowExternal || !external) {
                group = getGroup(groupNode, name, siteID1, external);
                cachePut(trueGroupKey, group);
            }
            return group;
        } catch (PathNotFoundException e) {
            logger.debug("Error while retrieving group " + name + " for site " + siteID, e);
        } catch (RepositoryException e) {
            logger.warn("Error while retrieving group " + name + " for site " + siteID, e);
        }
        return null;
    }

    private JCRGroup getGroup(Node usersFolderNode, String name, int siteID1, boolean external) {
        JCRGroup group;
        if (JahiaGroupManagerService.GUEST_GROUPNAME.equals(name)) {
            group = new GuestGroup(usersFolderNode, jcrTemplate, siteID1);
        } else if (JahiaGroupManagerService.USERS_GROUPNAME.equals(name)) {
            group = new UsersGroup(usersFolderNode, jcrTemplate, siteID1);
        } else {
            group = new JCRGroup(usersFolderNode, siteID1, external);
        }
        return group;
    }

    /**
     * Remove the specified user from all the membership lists of all the groups.
     *
     * @param user Reference on an existing user.
     * @return Return true on success, or false on any failure.
     */
    public boolean removeUserFromAllGroups(final JahiaUser user) {
        try {
            String uuid = null;
            if (user instanceof JCRUser) {
                uuid = ((JCRUser) user).getIdentifier();
            } else {
                JCRUser extUser = userManagerProvider.lookupExternalUser(user);
                if (extUser != null) {
                    uuid = extUser.getIdentifier();
                }
            }
            if (uuid != null) {
                final String id = uuid;
                return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        if (session.getWorkspace().getQueryManager() != null) {
                            String query = "SELECT * FROM [jnt:member] as m where m.[j:member] = '" + id +
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

    public Set<JahiaGroup> searchGroups(final int siteID, final Properties searchCriterias) {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            Set<JahiaGroup> users = new HashSet<JahiaGroup>();
            if (session.getWorkspace().getQueryManager() != null) {
                StringBuilder query = new StringBuilder(
                        "SELECT * FROM [" + Constants.JAHIANT_GROUP + "] as g WHERE g.[" +
                                JCRGroup.J_EXTERNAL + "] = 'false'"
                );
                if (siteID <= 0) {
                    query.append(" AND ISCHILDNODE(g, '/groups')");
                } else {
                    String siteName = sitesService.getSiteKeyById(siteID);
                    query.append(" AND ISCHILDNODE(g, '/sites/" + siteName + "/groups')");
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
                                    query.append(
                                            "(CONTAINS(g.*,'" + Patterns.PERCENT.matcher(propertyValue).replaceAll("")
                                                    + "') OR LOWER(g.[j:nodename]) LIKE '"
                                    )
                                            .append(propertyValue.toLowerCase()).append("') ");
                                } else {
                                    query.append("LOWER(g.[" + Patterns.DOT.matcher(propertyKey).replaceAll("\\\\.") + "])")
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
                    users.add(getGroup(usersFolderNode, usersFolderNode.getName(), siteID, false));
                }
            }
            return users;
        } catch (RepositoryException e) {
            logger.error("Error while searching groups", e);
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
    public void updateCache(final JahiaGroup jahiaGroup) {
        invalidateCacheRecursively(jahiaGroup);
    }

    /**
     * Invalidates group caches recursively considering membership of the group in other groups.
     *
     * @param jahiaGroup the group to invalidate caches for
     */
    public void invalidateCacheRecursively(JahiaGroup jahiaGroup) {
        cache.remove(jahiaGroup.getGroupKey());
        invalidateCachesForMembership(getMembership(jahiaGroup));
    }

    /**
     * Recursively invalidate group caches for the provided membership.
     *
     * @param membership list of group key to invalidate
     */
    protected void invalidateCachesForMembership(List<String> membership) {
        if (membership == null || membership.isEmpty()) {
            return;
        }

        JahiaGroupManagerService groupService = ServicesRegistry.getInstance().getJahiaGroupManagerService();

        for (String key : membership) {
            cache.remove(key);
            JahiaGroup grp = groupService.lookupGroup(key);
            if (grp != null) {
                invalidateCachesForMembership(getMembership(grp));
            }
        }
    }

    public void start() throws JahiaInitializationException {
        cache = getCacheInternal("JCRGroupCache");
        membershipCache = getCacheInternal(JCR_GROUPMEMBERSHIP_CACHE);
    }

    private Ehcache getCacheInternal(String cacheName) {
        Ehcache cacheInstance = null;
        if (cacheProvider != null) {
            CacheManager cacheManager = cacheProvider.getCacheManager();
            cacheInstance = cacheManager.getCache(cacheName);
            if (cacheInstance == null) {
                cacheManager.addCache(cacheName);
                cacheInstance = cacheManager.getCache(cacheName);
            }
        }

        return cacheInstance;
    }

    public void stop() throws JahiaException {
        // do nothing
    }

    /**
     * @param userManagerProvider the userManagerProvider to set
     */
    public void setUserManagerProvider(JCRUserManagerProvider userManagerProvider) {
        this.userManagerProvider = userManagerProvider;
    }

    public void updateMembershipCache(String identifier) {
        membershipCache.remove(identifier);
    }

    @Override
    public void flushCache() {
        super.flushCache();
        cache.removeAll();
        membershipCache.removeAll();
    }

    @Override
    public void onApplicationEvent(ProviderEvent event) {
        // provider either registered or unregistered
        chainedClassLoader = null;
        chainedClassLoaderInitialized = false;
    }

    ClassLoader getChainedClassloader() {
        if (chainedClassLoader != null || chainedClassLoaderInitialized) {
            return chainedClassLoader;
        }

        List<? extends JahiaUserManagerProvider> userProviders = JahiaUserManagerRoutingService.getInstance()
                .getProviderList();
        List<? extends JahiaGroupManagerProvider> groupProviders = JahiaGroupManagerRoutingService.getInstance()
                .getProviderList();

        if (userProviders.size() > 1 || groupProviders.size() > 1) {
            Set<ClassLoader> loaders = new LinkedHashSet<ClassLoader>();
            for (JahiaUserManagerProvider p : userProviders) {
                loaders.add(p.getClass().getClassLoader());
            }
            for (JahiaGroupManagerProvider p : groupProviders) {
                loaders.add(p.getClass().getClassLoader());
            }

            chainedClassLoader = ClassLoaderUtils.getChainedClassLoader(loaders.toArray(new ClassLoader[]{}));
        }

        chainedClassLoaderInitialized = true;

        return chainedClassLoader;
    }

    private void cachePut(String key, JCRGroup group) {
        cache.put(new Element(key,
                group != null && getChainedClassloader() != null ? new ProviderClassLoaderAwareCacheEntry(group)
                        : group
        ));
    }

    public void setCacheProvider(EhCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

}