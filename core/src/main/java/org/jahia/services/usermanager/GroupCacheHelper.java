/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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

import java.io.Serializable;
import java.util.*;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.RowIterator;

import net.sf.ehcache.Element;
import net.sf.ehcache.config.Searchable;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;

import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;
import net.sf.ehcache.search.expression.EqualTo;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRUserNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Group manager cache helper.
 *
 * @author Sergiy Shyrkov
 */
public class GroupCacheHelper {

    private static final Logger logger = LoggerFactory.getLogger(GroupCacheHelper.class);
    private static final int DEFAULT_TTL_FOR_NON_EXISTING_GROUPS = 600;

    private EhCacheProvider ehCacheProvider;
    private volatile SelfPopulatingCache groupPathByGroupNameCache;
    private volatile SelfPopulatingCache membershipCache;
    private int timeToLiveForNonExistingGroups = DEFAULT_TTL_FOR_NON_EXISTING_GROUPS;

    private final class GroupPathByGroupNameCacheEntryFactory implements CacheEntryFactory {

        @Override
        public Object createEntry(final Object key) throws Exception {
            final GroupPathByGroupNameCacheKey k = (GroupPathByGroupNameCacheKey) key;
            String path = internalGetGroupPath(k.siteKey, k.groupName);
            if (path != null) {
                return new Element(key, path);
            } else {
                return new Element(key, StringUtils.EMPTY, 0, timeToLiveForNonExistingGroups);
            }
        }

        private String internalGetGroupPath(String siteKey, String name) throws RepositoryException {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT [j:nodename] FROM [");
            sb.append(Constants.JAHIANT_GROUP);
            sb.append("] as group where localname()='");
            sb.append(JCRContentUtils.sqlEncode(name)).append("' ");

            if (siteKey != null) {
                sb.append("and isdescendantnode(group,'/sites/");
                sb.append(JCRContentUtils.sqlEncode(siteKey));
                sb.append("/groups')");
            } else {
                sb.append("and isdescendantnode(group,'/groups')");
            }

            Query query = getQueryManager().createQuery(sb.toString(), Query.JCR_SQL2);
            query.setLimit(1);

            RowIterator it = query.execute().getRows();
            if (it.hasNext()) {
                return it.nextRow().getPath();
            }

            return null;
        }

        private QueryManagerWrapper getQueryManager() throws RepositoryException {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.LIVE_WORKSPACE, null, null);
            return session.getWorkspace().getQueryManager();
        }

    }

    private static final class GroupPathByGroupNameCacheKey implements Serializable {

        private static final long serialVersionUID = 6343178106505586294L;

        private String siteKey;
        private String groupName;
        private final int hash;

        private GroupPathByGroupNameCacheKey(String siteKey, String groupName) {
            this.siteKey = siteKey;
            this.groupName = groupName;
            this.hash = getHashCode();
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            GroupPathByGroupNameCacheKey that = (GroupPathByGroupNameCacheKey) o;

            if (!groupName.equals(that.groupName)) {
                return false;
            }

            return Objects.equals(siteKey, that.siteKey);
        }

        private int getHashCode() {
            int result = siteKey != null ? siteKey.hashCode() : 0;
            result = 31 * result + groupName.hashCode();
            return result;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    private final class MembershipCacheEntryFactory implements CacheEntryFactory {

        @Override
        public Object createEntry(final Object key) throws Exception {
            return internalGetMembershipByPath((String) key);
        }

        private List<String> internalGetMembershipByPath(String principalPath) {
            try {
                JCRNodeWrapper principalNode = JCRSessionFactory.getInstance()
                        .getCurrentSystemSession(Constants.LIVE_WORKSPACE, null, null)
                        .getNode(principalPath);

                Set<String> groups = new LinkedHashSet<>();
                try {
                    populateGroups(groups, principalNode);
                } catch (JahiaException e) {
                    logger.warn("Error retrieving membership for user " + principalPath, e);
                }

                if (principalNode instanceof JCRUserNode) {
                    JCRUserNode userNode = (JCRUserNode) principalNode;
                    if (!JahiaUserManagerService.isGuest(userNode)) {
                        populateSpecialGroups(groups, JahiaGroupManagerService.USERS_GROUPPATH);
                        if (userNode.getRealm() != null) {
                            String siteUsers = "/sites/" + userNode.getRealm() + "/groups/" + JahiaGroupManagerService.SITE_USERS_GROUPNAME;
                            populateSpecialGroups(groups, siteUsers);
                        }
                    }
                    populateSpecialGroups(groups, JahiaGroupManagerService.GUEST_GROUPPATH);
                }

                return new LinkedList<>(groups);

            } catch (PathNotFoundException e) {
                // Non existing user/group
            } catch (RepositoryException e) {
                logger.error("Error retrieving membership for user " + principalPath + ", will return empty list", e);
            }
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        private void populateSpecialGroups(Set<String> groups, String groupPath) {
            groups.add(groupPath);
            groups.addAll((List<String>) getMembershipCache().get(groupPath).getObjectValue());
        }

    }

    public String getGroupPath(String siteKey, String name) {
        final String value = (String) getGroupPathByGroupNameCache().get(new GroupPathByGroupNameCacheKey(siteKey, name)).getObjectValue();
        if (value.equals(StringUtils.EMPTY)) {
            return null;
        }
        return value;
    }

    private SelfPopulatingCache getGroupPathByGroupNameCache() {
        // Thread-safe lazy loading, using double-checked locking pattern
        // see https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        SelfPopulatingCache cache = groupPathByGroupNameCache;
        if (cache == null) {
            synchronized (this) {
                cache = groupPathByGroupNameCache;
                if (cache == null) {
                    groupPathByGroupNameCache = cache = ehCacheProvider.registerSelfPopulatingCache(
                            "org.jahia.services.usermanager.JahiaGroupManagerService.groupPathByGroupNameCache",
                            new Searchable(), new GroupPathByGroupNameCacheEntryFactory()
                    );
                }
            }
        }
        return cache;
    }

    SelfPopulatingCache getMembershipCache() {
        // Thread-safe lazy loading, using double-checked locking pattern
        // see https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        SelfPopulatingCache cache = membershipCache;
        if (cache == null) {
            synchronized (this) {
                cache = membershipCache;
                if (cache == null) {
                    membershipCache = cache = ehCacheProvider.registerSelfPopulatingCache(
                            "org.jahia.services.usermanager.JahiaGroupManagerService.membershipCache",
                            new MembershipCacheEntryFactory()
                    );
                }
            }
        }
        return cache;
    }

    private static GroupPathByGroupNameCacheKey getPathCacheKey(String groupPath) {
        return new GroupPathByGroupNameCacheKey(groupPath.startsWith("/sites/") ? StringUtils.substringBetween(
                groupPath, "/sites/", "/") : null, StringUtils.substringAfterLast(groupPath, "/"));
    }

    private void populateGroups(Set<String> groups, JCRNodeWrapper principal) throws RepositoryException, JahiaException {
        try {
            PropertyIterator weakReferences = principal.getWeakReferences("j:member");
            while (weakReferences.hasNext()) {
                try {
                    Property property = weakReferences.nextProperty();
                    if (property != null && property.getPath() != null && property.getPath().contains("/j:members/")) {
                        JCRNodeWrapper group = (JCRNodeWrapper) property.getSession().getNode(StringUtils.substringBefore(property.getPath(), "/j:members/"));
                        if (group.isNodeType(Constants.JAHIANT_GROUP)) {
                            if (groups.add(group.getPath())) {
                                // recurse on the found group only we have not done it yet
                                populateGroups(groups, group);
                            }
                        }
                    }
                } catch (ItemNotFoundException e) {
                    logger.warn("Cannot find group for " + principal.getPath(), e);
                }
            }
        } catch (InvalidItemStateException e) {
            logger.warn("Cannot find membership for " + principal.getPath(), e);
        }
    }


    public void setEhCacheProvider(EhCacheProvider ehCacheProvider) {
        this.ehCacheProvider = ehCacheProvider;
    }

    public void setTimeToLiveForNonExistingGroups(int timeToLiveForNonExistingGroups) {
        this.timeToLiveForNonExistingGroups = timeToLiveForNonExistingGroups;
    }

    public void updatePathCacheAdded(String groupPath) {
        getGroupPathByGroupNameCache().put(new Element(getPathCacheKey(groupPath), groupPath));
    }

    public void updatePathCacheRemoved(String groupPath) {
        getGroupPathByGroupNameCache().remove(getPathCacheKey(groupPath));
    }

    public void clearNonExistingGroupsCache() {
        SelfPopulatingCache cache = getGroupPathByGroupNameCache();
        Results results = cache.createQuery().addCriteria(new EqualTo("value", "")).includeKeys().execute();
        for (Result result : results.all()) {
            cache.remove(result.getKey());
        }
    }
}
