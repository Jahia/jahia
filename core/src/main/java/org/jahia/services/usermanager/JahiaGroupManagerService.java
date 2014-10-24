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

package org.jahia.services.usermanager;

import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;


public class JahiaGroupManagerService extends JahiaService {
    private static Logger logger = LoggerFactory.getLogger(JahiaGroupManagerService.class);

    public static final String USERS_GROUPNAME = "users";
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

    private static class PatternHolder {
        static final Pattern INSTANCE = Pattern.compile(org.jahia.settings.SettingsBean.getInstance().lookupString("userManagementGroupNamePattern"));
    }

    private static Pattern getGroupNamePattern() {
        return PatternHolder.INSTANCE;
    }

    private JahiaUserManagerService userManagerService;
    private List<String> jahiaJcrEnforcedGroups;

    private EhCacheProvider ehCacheProvider;
    private SelfPopulatingCache groupPathByGroupNameCache;
    private SelfPopulatingCache membershipCache;

    private Map<String, JahiaGroupManagerProvider> legacyGroupProviders = new HashMap<String, JahiaGroupManagerProvider>();


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

    public void setJahiaJcrEnforcedGroups(List<String> jahiaJcrEnforcedGroups) {
        this.jahiaJcrEnforcedGroups = jahiaJcrEnforcedGroups;
    }

    public void setEhCacheProvider(EhCacheProvider ehCacheProvider) {
        this.ehCacheProvider = ehCacheProvider;
    }

    @Override
    public void start() throws JahiaInitializationException {

    }

    @Override
    public void stop() throws JahiaException {

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
        final String value = (String) getGroupPathByGroupNameCache().get(new GroupPathByGroupNameCacheKey(siteKey, name)).getObjectValue();
        if (value.equals("")) {
            return null;
        }
        return value;
    }

    public String internalGetGroupPath(String siteKey, String name) throws RepositoryException {
        String query = "SELECT [j:nodename] FROM [" + Constants.JAHIANT_GROUP + "] as group where localname()='"+name+"'";
        if (siteKey != null) {
            query += "and (isdescendantnode(group,'/sites/" + siteKey + "/groups') or isdescendantnode(group,'/groups/providers'))";
        } else {
            query += "and isdescendantnode(group,'/groups')";
        }

        Query q = JCRSessionFactory.getInstance().getCurrentSystemSession(null,null,null).getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
        RowIterator it = q.execute().getRows();
        if (!it.hasNext()) {
            return null;
        }
        return it.nextRow().getPath();
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
        return lookupGroupByPath(siteKey == null ? "/groups/" + JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME : "/sites/" + siteKey + "/" + JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME);
    }

    /**
     * Get administrator or site administrator group
     */
    public JCRGroupNode getAdministratorGroup(String siteKey, JCRSessionWrapper session) throws RepositoryException {
        return lookupGroupByPath(siteKey == null ? "/groups/" + JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME : "/sites/" + siteKey + "/" + JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME, session);
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
        try {
            Set<JCRGroupNode> users = new HashSet<JCRGroupNode>();
            if (session.getWorkspace().getQueryManager() != null) {
                StringBuilder query = new StringBuilder(
                        "SELECT * FROM [" + Constants.JAHIANT_GROUP + "] as g WHERE "
                );
                List<JCRStoreProvider> searchOnProviders = getProviders(siteKey, providers, session);
                if (!searchOnProviders.isEmpty()) {
                    query.append("(");
                    int initialLength = query.length();
                    for (JCRStoreProvider provider : searchOnProviders) {
                        query.append(query.length() > initialLength ? " OR " : "");
                        if (provider.isDefault()) {
                            query.append("(g.[j:external] = false AND ");
                            if(siteKey == null){
                                query.append("ISDESCENDANTNODE(g, '/groups'))");
                            } else {
                                query.append("ISDESCENDANTNODE(g, '/sites/").append(siteKey).append("/groups'))");
                            }
                        } else {
                            query.append("ISDESCENDANTNODE(g, '").append(provider.getMountPoint()).append("')");
                        }
                    }
                    query.append(")");
                } else {
                    if (siteKey == null) {
                        query.append("ISDESCENDANTNODE(g, '/groups')");
                    } else {
                        query.append("(ISDESCENDANTNODE(g, '/sites/").append(siteKey).append("/groups')").append(" OR ISDESCENDANTNODE(g, '/groups/providers'))");
                    }
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
            JCRNodeWrapper providersNode = session.getNode("/groups/providers");
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

    public List<String> getUserMembership(String userName) {
        final String userPath = userManagerService.getUserPath(userName);
        if (userPath == null) {
            return null;
        }
        return getMembershipByPath(userPath);
    }


    /**
     * Return the list of groups to which the specified user has access.
     *
     * @param principalPath The user/group path
     * @return Return a List of strings holding all the group path to
     * which the user as access.
     */
    public List<String> getMembershipByPath(String principalPath) {
        return (List<String>) getMembershipCache().get(principalPath).getObjectValue();
    }

    private List<String> internalGetMembershipByPath(String principalPath) {
        try {
            JCRNodeWrapper principalNode = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null).getNode(principalPath);
            Set<String> groups = new LinkedHashSet<String>();
            try {
                recurseOnGroups(groups, principalNode);
            } catch (JahiaException e) {
                logger.warn("Error retrieving membership for user " + principalPath, e);
            }
            if (principalNode instanceof JCRUserNode) {
                if (!JahiaUserManagerService.isGuest((JCRUserNode) principalNode)) {
                    groups.add(JahiaGroupManagerService.USERS_GROUPPATH);
                }
                groups.add(JahiaGroupManagerService.GUEST_GROUPPATH);
            }
            return new LinkedList<String>(groups);
        } catch (PathNotFoundException e) {
            // Non existing user/group
        } catch (RepositoryException e) {
            logger.error("Error retrieving membership for user " + principalPath + ", will return empty list", e);
        }
        return null;
    }

    private void recurseOnGroups(Set<String> groups, JCRNodeWrapper principal) throws RepositoryException, JahiaException {
        PropertyIterator weakReferences = principal.getWeakReferences("j:member");
        while (weakReferences.hasNext()) {
            try {
                Property property = weakReferences.nextProperty();
                if (property.getPath().contains("/j:members/")) {
                    JCRNodeWrapper group = (JCRNodeWrapper) property.getSession().getNode(StringUtils.substringBefore(property.getPath(), "/j:members/"));
                    if (group.isNodeType("jnt:group")) {
                        if (groups.add(group.getPath())) {
                            // recurse on the found group only we have not done it yet
                            recurseOnGroups(groups, group);
                        }
                    }
                }
            } catch (ItemNotFoundException e) {
                logger.warn("Cannot find group for " + principal.getPath(), e);
            }
        }
    }
    public boolean isMember(String username, String groupname, String siteKey) {
        final List<String> userMembership = getUserMembership(username);
        return userMembership != null && userMembership.contains(getGroupPath(siteKey, groupname));
    }

    public boolean isAdminMember(String username, String siteKey) {
        return username.equals(userManagerService.getRootUserName()) ||
                isMember(username, siteKey == null ? JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME : JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME, siteKey);
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
            Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:member] as m where isdescendantnode(m,'" + node.getPath() + "')", Query.JCR_SQL2);
            NodeIterator ni = q.execute().getNodes();
            if (ni.hasNext()) {
                getMembershipCache().remove("/" + StringUtils.substringAfter(ni.nextNode().getPath(), "/j:members/"));
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

    public void flushMembershipCache(String memberPath) {
        final String key = StringUtils.substringAfter(memberPath, "/j:members/");

        try {
            // If member is a group, recurse on all members
            JCRGroupNode groupNode = lookupGroupByPath("/" + key);
            if (groupNode != null) {
                for (JCRNodeWrapper member : groupNode.getMembers()) {
                    flushMembershipCache("/" + key + "/j:members" + member.getPath());
                }
            }
        } catch (ClassCastException e) {
            // Member is not a group
        }
        if (key.contains("/")) {
            getMembershipCache().refresh("/" + key);
        } else {
            getMembershipCache().removeAll();
        }
    }

    private static class GroupPathByGroupNameCacheKey implements Serializable {
        String siteKey;
        String groupName;

        private GroupPathByGroupNameCacheKey(String siteKey, String groupName) {
            this.siteKey = siteKey;
            this.groupName = groupName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GroupPathByGroupNameCacheKey that = (GroupPathByGroupNameCacheKey) o;

            if (!groupName.equals(that.groupName)) return false;
            if (siteKey != null ? !siteKey.equals(that.siteKey) : that.siteKey != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = siteKey != null ? siteKey.hashCode() : 0;
            result = 31 * result + groupName.hashCode();
            return result;
        }
    }

    private SelfPopulatingCache getGroupPathByGroupNameCache() {
        if (groupPathByGroupNameCache == null) {
            groupPathByGroupNameCache = ehCacheProvider.registerSelfPopulatingCache("org.jahia.services.usermanager.JahiaGroupManagerService.groupPathByGroupNameCache", new GroupPathByGroupNameCacheEntryFactory());
        }
        return groupPathByGroupNameCache;
    }

    public void updatePathCacheAdded(String groupPath) {
        getGroupPathByGroupNameCache().put(new Element(getPathCacheKey(groupPath), groupPath));
    }

    public void updatePathCacheRemoved(String groupPath) {
        getGroupPathByGroupNameCache().put(new Element(getPathCacheKey(groupPath), "", 0, userManagerService.getTimeToLiveForEmptyPath()));
    }

    private GroupPathByGroupNameCacheKey getPathCacheKey(String groupPath) {
        String groupName = StringUtils.substringAfterLast(groupPath, "/");
        String siteKey = null;
        if (groupPath.startsWith("/sites/")) {
            siteKey = StringUtils.substringBefore(StringUtils.removeStart(groupPath, "/sites/"), "/");
        }
        return new GroupPathByGroupNameCacheKey(siteKey, groupName);
    }

    class GroupPathByGroupNameCacheEntryFactory implements CacheEntryFactory {
        @Override
        public Object createEntry(final Object key) throws Exception {
            final GroupPathByGroupNameCacheKey c = (GroupPathByGroupNameCacheKey) key;
            String path = internalGetGroupPath(c.siteKey, c.groupName);
            if (path != null) {
                return new Element(key, path);
            } else {
                return new Element(key, "", 0, userManagerService.getTimeToLiveForEmptyPath());
            }
        }
    }

    private SelfPopulatingCache getMembershipCache() {
        if (membershipCache == null) {
            membershipCache = ehCacheProvider.registerSelfPopulatingCache("org.jahia.services.usermanager.JahiaGroupManagerService.membershipCache", new MembershipCacheEntryFactory());
        }
        return membershipCache;
    }

    class MembershipCacheEntryFactory implements CacheEntryFactory {
        @Override
        public Object createEntry(final Object key) throws Exception {
            return internalGetMembershipByPath((String) key);
        }
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
}
