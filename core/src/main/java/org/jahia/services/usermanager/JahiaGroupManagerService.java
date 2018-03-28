/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.usermanager;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.*;
import java.util.regex.Pattern;


public class JahiaGroupManagerService extends JahiaService {
    private static Logger logger = LoggerFactory.getLogger(JahiaGroupManagerService.class);

    public static final String USERS_GROUPNAME = "users";
    public static final String SITE_USERS_GROUPNAME = "site-users";
    public static final String ADMINISTRATORS_GROUPNAME = "administrators";
    public static final String PRIVILEGED_GROUPNAME = "privileged";
    public static final String SITE_PRIVILEGED_GROUPNAME = "site-privileged";
    public static final String SITE_ADMINISTRATORS_GROUPNAME = "site-administrators";
    public static final String GUEST_GROUPNAME = "guest";
    public static final String GUEST_GROUPPATH = "/groups/" + GUEST_GROUPNAME;
    public static final String USERS_GROUPPATH = "/groups/" + USERS_GROUPNAME;

    public static final Set<String> POWERFUL_GROUPS = new HashSet<String>(Arrays.asList(
            ADMINISTRATORS_GROUPNAME, SITE_ADMINISTRATORS_GROUPNAME, PRIVILEGED_GROUPNAME,
            SITE_PRIVILEGED_GROUPNAME));

    public static final Set<String> PROTECTED_GROUPS = new HashSet<String>(Arrays.asList(
            USERS_GROUPNAME, SITE_USERS_GROUPNAME, PRIVILEGED_GROUPNAME,
            SITE_PRIVILEGED_GROUPNAME, GUEST_GROUPNAME, ADMINISTRATORS_GROUPNAME));

    private static class PatternHolder {
        static final Pattern INSTANCE = Pattern.compile(org.jahia.settings.SettingsBean.getInstance().lookupString("userManagementGroupNamePattern"));
    }

    private static Pattern getGroupNamePattern() {
        return PatternHolder.INSTANCE;
    }

    private JahiaUserManagerService userManagerService;

    private Map<String, JahiaGroupManagerProvider> legacyGroupProviders = new HashMap<String, JahiaGroupManagerProvider>();
    
    private GroupCacheHelper cacheHelper;


    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final JahiaGroupManagerService INSTANCE = new JahiaGroupManagerService();
    }

    public static JahiaGroupManagerService getInstance() {
        return Holder.INSTANCE;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    @Override
    public void start() throws JahiaInitializationException {
        // do nothing
    }

    @Override
    public void stop() throws JahiaException {
        // do nothing
    }

    /**
     * @deprecated use lookupGroupByPath() instead
     */
    public JCRGroupNode lookupGroup(String groupPath) {
        return lookupGroupByPath(groupPath);
    }

    /**
     * Lookup the group information from the underlying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cache from the database.
     *
     * @param groupPath  Group's path
     * @return Return a reference on a the specified group name. Return null
     * if the group doesn't exist or when any error occurred.
     */
    public JCRGroupNode lookupGroupByPath(String groupPath) {
        try {
            return lookupGroupByPath(groupPath, JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null));
        } catch (RepositoryException e) {
            return null;
        }
    }

    /**
     * Lookup the group information from the underlaying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cahce from the database.
     *
     * @param groupPath  Group's path
     * @return Return a reference on a the specified group name. Return null
     * if the group doesn't exist or when any error occurred.
     */
    public JCRGroupNode lookupGroupByPath(String groupPath, JCRSessionWrapper session) {
        try {
            return (JCRGroupNode) session.getNode(groupPath);
        } catch (PathNotFoundException e) {
            // Method return just null if a group is not found
        } catch (ClassCastException e) {
            // Not a group
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Lookup the group information from the underlaying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cahce from the database.
     *
     * @param siteKey       siteKey the site id
     * @param name Group's unique identification name.
     * @return Return a reference on a the specified group name. Return null
     * if the group doesn't exist or when any error occured.
     */
    public JCRGroupNode lookupGroup(String siteKey, String name) {
        try {
            return lookupGroup(siteKey, name, JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null));
        } catch (RepositoryException e) {
            return null;
        }
    }

    /**
     * Lookup the group information from the underlaying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cahce from the database.
     *
     * @param siteKey       siteKey the site id
     * @param name Group's unique identification name.
     * @return Return a reference on a the specified group name. Return null
     * if the group doesn't exist or when any error occured.
     */
    public JCRGroupNode lookupGroup(String siteKey, String name, JCRSessionWrapper session) {
        final String path = getGroupPath(siteKey, name);
        if (path == null) {
            return null;
        }
        return lookupGroupByPath(path, session);
    }

    public String getGroupPath(String siteKey, String name) {
        return cacheHelper.getGroupPath(siteKey, name);
    }

    /**
     * This function checks on a gived site if the group name has already been
     * assigned to another group.
     *
     * @param siteKey siteKey the site key
     * @param name String representing the unique group name.
     * @return Return true if the specified group name has not been assigned yet,
     * return false on any failure.
     */
    public boolean groupExists(String siteKey, String name) {
        return lookupGroup(siteKey, name) != null;
    }

    /**
     * Get administrator or site administrator group
     */
    public JCRGroupNode getAdministratorGroup(String siteKey) throws RepositoryException {
        return getAdministratorGroup(siteKey, JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null));
    }

    /**
     * Get administrator or site administrator group
     */
    public JCRGroupNode getAdministratorGroup(String siteKey, JCRSessionWrapper session) throws RepositoryException {
        return lookupGroupByPath(siteKey == null ? "/groups/" + JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME : "/sites/" + siteKey + "/groups/" + JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME, session);
    }

    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group keys in the system.
     *
     * @return Return a List of identifier of all groups of this system.
     */
    public List<String> getGroupList() {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            List<String> groups = new ArrayList<String>();
            if (session.getWorkspace().getQueryManager() != null) {
                String query = "SELECT [j:nodename] FROM [" + Constants.JAHIANT_GROUP + "] as group ORDER BY group.[j:nodename]";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                QueryResult qr = q.execute();
                RowIterator rows = qr.getRows();
                while (rows.hasNext()) {
                    Row groupsFolderNode = rows.nextRow();
                    String groupName = groupsFolderNode.getPath();
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
     * @param siteKey the key of the site from which you wish to retrieve the list of group
     * @return Return a List of identifier of all groups of this site.
     */
    public List<String> getGroupList(String siteKey) {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            List<String> groups = new ArrayList<String>();
            if (session.getWorkspace().getQueryManager() != null) {
                String constraint = siteKey != null ? "(isdescendantnode(group,'/sites/" + siteKey + "/groups') or isdescendantnode(group,'/groups/providers'))" :  "isdescendantnode(group,'/groups')";
                String query = "SELECT [j:nodename] FROM [" + Constants.JAHIANT_GROUP + "] as group WHERE " + constraint + " ORDER BY group.[j:nodename]";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                QueryResult qr = q.execute();
                RowIterator rows = qr.getRows();
                while (rows.hasNext()) {
                    Row groupNode = rows.nextRow();
                    String groupPath = groupNode.getPath();
                    if (!groups.contains(groupPath)) {
                        groups.add(groupPath);
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
                        "SELECT [j:nodename] FROM [" + Constants.JAHIANT_GROUP + "] as group ORDER BY group.[j:nodename]";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                QueryResult qr = q.execute();
                RowIterator rows = qr.getRows();
                while (rows.hasNext()) {
                    Row groupNode = rows.nextRow();
                    String groupName = groupNode.getValue("j:nodename").getString();
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
     * Find groups according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param siteKey         site identifier
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "groupname"="*test*") or
     *                        null to search without criterias
     * @return a Set of JCRGroupNode elements that correspond to those search criterias
     */
    public Set<JCRGroupNode> searchGroups(String siteKey, Properties searchCriterias) {
        try {
            return searchGroups(siteKey, searchCriterias, JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null));
        } catch (RepositoryException e) {
            return null;
        }
    }

    /**
     * Find groups according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param siteKey         site identifier
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "groupname"="*test*") or
     *                        null to search without criterias
     * @param providers       the providers key to search on
     * @return a Set of JCRGroupNode elements that correspond to those search criterias
     */
    public Set<JCRGroupNode> searchGroups(String siteKey, Properties searchCriterias, String[] providers) {
        try {
            return searchGroups(siteKey, searchCriterias, providers, JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null));
        } catch (RepositoryException e) {
            return null;
        }
    }

    /**
     * Find groups according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param siteKey         site identifier
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "groupname"="*test*") or
     *                        null to search without criterias
     * @return a Set of JCRGroupNode elements that correspond to those search criterias
     */
    public Set<JCRGroupNode> searchGroups(String siteKey, Properties searchCriterias, JCRSessionWrapper session) {
        return searchGroups(siteKey, searchCriterias, null, session);
    }

    /**
     * Find groups according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param siteKey         site identifier
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "groupname"="*test*") or
     *                        null to search without criterias
     * @param providers       the providers key to search on
     * @return a Set of JCRGroupNode elements that correspond to those search criterias
     */
    public Set<JCRGroupNode> searchGroups(String siteKey, Properties searchCriterias, String[] providers, JCRSessionWrapper session) {
        return searchGroups(siteKey, searchCriterias, providers, false, session);
    }


    public Set<JCRGroupNode> searchGroups(String siteKey, Properties searchCriterias, String[] providers, boolean excludeProtected, JCRSessionWrapper session) {

        if (providers != null) {
            Set<JCRGroupNode> groups = new HashSet<JCRGroupNode>();

            for (String providerKey : providers) {
                groups.addAll(searchGroups(siteKey, searchCriterias, providerKey, excludeProtected, session));
            }
            return groups;
        } else {
            return searchGroups(siteKey, searchCriterias, (String) null, excludeProtected, session);
        }
    }

    public Set<JCRGroupNode> searchGroups(String siteKey, Properties searchCriterias, String providerKey, boolean excludeProtected, JCRSessionWrapper session) {
        try {
            Set<JCRGroupNode> groups = new HashSet<JCRGroupNode>();
            if (session.getWorkspace().getQueryManager() != null) {
                StringBuilder query = new StringBuilder(128);

                // Add provider to query
                if(providerKey != null) {
                    JCRStoreProvider provider = getProvider(siteKey, providerKey, session);
                    if (provider != null) {
                        query.append("(");
                        if (provider.isDefault()) {
                            query.append("g.[j:external] = false");
                        } else {
                            query.append("ISDESCENDANTNODE('").append(provider.getMountPoint()).append("')");
                        }
                        query.append(')');
                    } else {
                        return groups;
                    }
                }

                if (searchCriterias != null && searchCriterias.size() > 0) {
                    // Avoid wildcard attribute
                    if (!(searchCriterias.containsKey("*") && searchCriterias.size() == 1 &&
                            searchCriterias.getProperty("*").equals("*"))) {
                        Iterator<Map.Entry<Object, Object>> objectIterator =
                                searchCriterias.entrySet().iterator();
                        if (objectIterator.hasNext()) {
                            query.append(query.length() > 0 ? " AND " : "").append(" (");
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
                                    if (propertyValue.indexOf('*') != -1) {
                                        propertyValue = Patterns.STAR.matcher(propertyValue).replaceAll("%");
                                    } else {
                                        propertyValue = propertyValue + "%";
                                    }
                                }
                                propertyValue = JCRContentUtils.sqlEncode(propertyValue);
                                if ("*".equals(propertyKey)) {
                                    query.append("(CONTAINS(g.*,'").append(QueryParser.escape(Patterns.PERCENT.matcher(propertyValue).replaceAll(""))).append("') OR LOWER(g.[j:nodename]) LIKE '")
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

                if (query.length() > 0) {
                    query.insert(0, " and ");
                }
                if (excludeProtected) {
                    for (String g : PROTECTED_GROUPS) {
                        query.insert(0, " and [j:nodename] <> '" + g + "'");
                    }
                }
                String s = (siteKey == null) ? "/groups/" : "/sites/" + siteKey + "/groups/";
                query.insert(0, "SELECT * FROM [" + Constants.JAHIANT_GROUP + "] as g where isdescendantnode(g,'" + JCRContentUtils.sqlEncode(s) + "')");
                query.append(" ORDER BY g.[j:nodename]");
                if (logger.isDebugEnabled()) {
                    logger.debug(query.toString());
                }
                Query q = session.getWorkspace().getQueryManager().createQuery(query.toString(),
                        Query.JCR_SQL2);
                QueryResult qr = q.execute();
                NodeIterator ni = qr.getNodes();
                while (ni.hasNext()) {
                    groups.add((JCRGroupNode) ni.nextNode());
                }
            }

            return groups;
        } catch (RepositoryException e) {
            logger.error("Error while searching for groups", e);
            return new HashSet<JCRGroupNode>();
        }
    }

    /**
     * Provide the list of all available groups providers
     * @param siteKey the site to lookup on, if provided lookup under /sites/{siteKey}/groups/providers else the lookup is made under /groups/providers)
     * @param session the session used
     * @return list of JCRStoreProvider
     */
    public List<JCRStoreProvider> getProviderList(String siteKey, JCRSessionWrapper session){
        List<JCRStoreProvider> providers = new LinkedList<JCRStoreProvider>();
        try {
            providers.add(JCRSessionFactory.getInstance().getDefaultProvider());
            String providersParentPath = (siteKey == null ? "" : "/sites/" + siteKey) + "/groups/providers";
            JCRNodeWrapper providersNode = session.getNode(providersParentPath);
            NodeIterator iterator = providersNode.getNodes();
            while (iterator.hasNext()){
                JCRNodeWrapper providerNode = (JCRNodeWrapper) iterator.next();
                providers.add(providerNode.getProvider());
            }
        } catch (PathNotFoundException e) {
            // no providers node
        } catch (RepositoryException e) {
            logger.error("Error while retrieving user providers", e);
        }
        return providers;
    }

    /**
     * Provide the list of groups providers matching given keys
     * @param siteKey the site to lookup on, if provided lookup under /sites/{siteKey}/groups/providers else the lookup is made under /groups/providers)
     * @param providerKeys the keys
     * @param session the session used
     * @return list of JCRStoreProvider
     */
    public List<JCRStoreProvider> getProviders(String siteKey, String[] providerKeys, JCRSessionWrapper session){
        if(ArrayUtils.isEmpty(providerKeys)){
            return Collections.emptyList();
        }

        List<JCRStoreProvider> providers = new LinkedList<JCRStoreProvider>();
        for (JCRStoreProvider provider : getProviderList(siteKey, session)) {
            if (ArrayUtils.contains(providerKeys, provider.getKey())) {
                providers.add(provider);
            }
        }
        return providers;
    }

    /**
     * Retrieve the group provider corresponding to a given key
     * @param siteKey the site to lookup on, if provided lookup under /sites/{siteKey}/groups/providers else the lookup is made under /groups/providers)
     * @param providerKey the key
     * @param session the session used
     * @return JCRStoreProvider if it exist or null
     */
    public JCRStoreProvider getProvider(String siteKey, String providerKey, JCRSessionWrapper session){
        List<JCRStoreProvider> providers = getProviders(siteKey, new String[]{providerKey}, session);
        return providers.size() == 1 ? providers.get(0) : null;
    }

    public List<String> getUserMembership(String userName, String site) {
        final String userPath = userManagerService.getUserPath(userName, site);
        if (userPath == null) {
            return null;
        }
        return getMembershipByPath(userPath);
    }


    public boolean isMember(String username, String groupname, String groupSite) {
        return isMember(username, null, groupname, groupSite);
    }

    public boolean isMember(String username, String userSite, String groupname, String groupSite) {
        final List<String> userMembership = getUserMembership(username, userSite);
        return userMembership != null && userMembership.contains(getGroupPath(groupSite, groupname));
    }

    public boolean isAdminMember(String username, String siteKey) {
        return isAdminMember(username, null, siteKey);
    }

    public boolean isAdminMember(String username, String userSite, String siteKey) {
        return username.equals(userManagerService.getRootUserName()) ||
                isMember(username, userSite, siteKey == null ? JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME : JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME, siteKey);
    }

       /**
     * Create a new group in the system.
     *
     * @param hidden
     * @return a reference on a group object on success, or if the groupname
     * already exists or another error occured, null is returned.
     */
    public JCRGroupNode createGroup(final String siteKey, final String name, final Properties properties, final boolean hidden, JCRSessionWrapper session) {
        try {
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
            return nodeWrapper;
        } catch (RepositoryException e) {
            logger.error("Error while creating group", e);
            return null;
        }
    }

    /**
     * Delete a group from the system. Updates the database automatically, and
     * signal the ACL Manager that the group no longer exists.
     *
     * @param groupPath Path of the group to delete
     * @return Return true on success, or false on any failure.
     */
    public boolean deleteGroup(String groupPath, JCRSessionWrapper session) {

        try {
            Node node = session.getNode(groupPath);
            PropertyIterator pi = node.getWeakReferences("j:member");
            while (pi.hasNext()) {
                JCRPropertyWrapper member = (JCRPropertyWrapper) pi.next();
                member.getParent().remove();
            }

            // Update membership cache
            Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:member] as m where isdescendantnode(m,'" + JCRContentUtils.sqlEncode(node.getPath()) + "')", Query.JCR_SQL2);
            NodeIterator ni = q.execute().getNodes();
            while (ni.hasNext()) {
                cacheHelper.getMembershipCache().remove("/" + StringUtils.substringAfter(ni.nextNode().getPath(), "/j:members/"));
            }

            node.remove();
            return true;
        } catch (PathNotFoundException e) {
            return false;
        } catch (RepositoryException e) {
            logger.error("Error while deleting group", e);
        }
        return false;
    }


    /**
     * Validates provided group name against a regular expression pattern, specified in the Jahia configuration.
     *
     * @param name the group name to be validated
     * @return <code>true</code> if the specified group name matches the validation pattern
     */
    public boolean isGroupNameSyntaxCorrect(String name) {
        if (name == null || name.length() == 0) {
            return false;
        }

        boolean usernameCorrect = getGroupNamePattern().matcher(name)
                .matches();
        if (!usernameCorrect && logger.isDebugEnabled()) {
            logger
                    .debug("Validation failed for the user name: "
                            + name + " against pattern: "
                            + getGroupNamePattern().pattern());
        }
        return usernameCorrect;
    }

    public void flushMembershipCache(String memberPath, JCRSessionWrapper session) {
        final String key = StringUtils.substringAfter(memberPath, "/j:members/");

        // If member is a group, recurse on all members
        JCRGroupNode groupNode = lookupGroupByPath("/" + key, session);
        if (groupNode != null) {
            for (JCRNodeWrapper member : groupNode.getMembers()) {
                flushMembershipCache("/" + key + "/j:members" + member.getPath(), session);
            }
            if (groupNode.getPath().equals(JahiaGroupManagerService.GUEST_GROUPPATH) ||
                    groupNode.getPath().equals(JahiaGroupManagerService.USERS_GROUPPATH) ||
                    groupNode.getName().equals(JahiaGroupManagerService.SITE_USERS_GROUPNAME)) {
                cacheHelper.getMembershipCache().removeAll();
            }
        }

        if (key.contains("/")) {
            cacheHelper.getMembershipCache().remove("/" + key);
        } else {
            // Legacy members
            cacheHelper.getMembershipCache().removeAll();
        }
    }


    public void updatePathCacheAdded(String groupPath) {
        cacheHelper.updatePathCacheAdded(groupPath);
    }

    public void updatePathCacheRemoved(String groupPath) {
        cacheHelper.updatePathCacheRemoved(groupPath);
    }

    /**
     * get the list of olds group providers
     * this method is used to maintain compatibility with old groups providers
     * @deprecated
     */
    @Deprecated
    public List<? extends JahiaGroupManagerProvider> getProviderList (){
        return new ArrayList<JahiaGroupManagerProvider>(legacyGroupProviders.values());
    }

    /**
     * get the provider corresponding to a given key
     * this method is used to maintain compatibility with old groups providers
     * @deprecated
     */
    @Deprecated
    public JahiaGroupManagerProvider getProvider(String key){
        return legacyGroupProviders.get(key);
    }

    /**
     * register a group provider
     * this method is used to maintain compatibility with old groups providers
     * @deprecated
     */
    @Deprecated
    public void registerProvider(JahiaGroupManagerProvider jahiaGroupManagerProvider){
        legacyGroupProviders.put(jahiaGroupManagerProvider.getKey(), jahiaGroupManagerProvider);
        BridgeEvents.sendEvent(jahiaGroupManagerProvider.getKey(), BridgeEvents.GROUP_PROVIDER_REGISTER_BRIDGE_EVENT_KEY);
    }

    /**
     * unregister a group provider
     * this method is used to maintain compatibility with old groups providers
     * @deprecated
     */
    @Deprecated
    public void unregisterProvider(JahiaGroupManagerProvider jahiaGroupManagerProvider){
        legacyGroupProviders.remove(jahiaGroupManagerProvider.getKey());
        BridgeEvents.sendEvent(jahiaGroupManagerProvider.getKey(), BridgeEvents.GROUP_PROVIDER_UNREGISTER_BRIDGE_EVENT_KEY);
    }

    public void setCacheHelper(GroupCacheHelper cacheHelper) {
        this.cacheHelper = cacheHelper;
    }

    /**
     * Remove all cache entries for non existing groups
     */
    public void clearNonExistingGroupsCache() {
        cacheHelper.clearNonExistingGroupsCache();
    }

    /**
     * Return the list of groups to which the specified user has access.
     *
     * @param principalPath The user/group path
     * @return Return a List of strings holding all the group path to
     * which the user as access.
     */
    @SuppressWarnings("unchecked")
    public List<String> getMembershipByPath(String principalPath) {
        List<String> owners = (List<String>) cacheHelper.getMembershipCache().get(principalPath).getObjectValue();

        if (principalPath.startsWith("/users/") && !principalPath.equals(JahiaUserManagerService.GUEST_USERPATH)) {
            // Dynamically add all site-users groups for global users
            owners = owners != null ? new ArrayList<String>(owners) : new ArrayList<String>();
            for (String s : JahiaSitesService.getInstance().getSitesNames()) {
                String siteUsersGroup = "/sites/" + s + "/groups/" + JahiaGroupManagerService.SITE_USERS_GROUPNAME;
                owners.add(siteUsersGroup);
                owners.addAll((List<String>) cacheHelper.getMembershipCache().get(siteUsersGroup).getObjectValue());
            }
        }

        return owners;
    }

}
