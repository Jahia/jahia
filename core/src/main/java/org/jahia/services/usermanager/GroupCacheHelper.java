/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.usermanager;

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

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.RowIterator;
import java.io.Serializable;
import java.util.*;

/**
 * Group manager cache helper.
 *
 * @author Sergiy Shyrkov
 */
public class GroupCacheHelper {

    private static final Logger logger = LoggerFactory.getLogger(GroupCacheHelper.class);
    private static final int DEFAULT_TTL_FOR_NON_EXISTING_GROUPS = 600;
    private static final int DEFAULT_TTL_FOR_EXTERNAL_MEMBERSHIP = 7200;

    private EhCacheProvider ehCacheProvider;
    private volatile SelfPopulatingCache groupPathByGroupNameCache;
    private volatile SelfPopulatingCache membershipCache;

    private int membershipCacheTimeToLive = DEFAULT_TTL_FOR_EXTERNAL_MEMBERSHIP;

    private int timeToLiveForNonExistingGroups = DEFAULT_TTL_FOR_NON_EXISTING_GROUPS;

    private static GroupPathByGroupNameCacheKey getPathCacheKey(String groupPath) {
        return new GroupPathByGroupNameCacheKey(groupPath.startsWith("/sites/") ? StringUtils.substringBetween(
                groupPath, "/sites/", "/") : null, StringUtils.substringAfterLast(groupPath, "/"));
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
                    int timeToLive = (int) membershipCache.getCacheConfiguration().getTimeToLiveSeconds();
                    if (timeToLive > 0) {
                        membershipCacheTimeToLive = timeToLive;
                    }
                }
            }
        }
        return cache;
    }

    private void populateGroups(Set<String> groups, JCRNodeWrapper principal) throws RepositoryException, JahiaException {
        try {
            PropertyIterator weakReferences = principal.getWeakReferences("j:member");
            while (weakReferences.hasNext()) {
                populateGroup(groups, principal, weakReferences);
            }
        } catch (InvalidItemStateException e) {
            logger.warn("Cannot find membership for " + principal.getPath(), e);
        }
    }

    private void populateGroup(Set<String> groups, JCRNodeWrapper principal, PropertyIterator weakReferences) throws RepositoryException, JahiaException {
        try {
            Property property = weakReferences.nextProperty();
            if (property != null && property.getPath() != null && property.getPath().contains("/j:members/")) {
                JCRNodeWrapper group = (JCRNodeWrapper) property.getSession().getNode(StringUtils.substringBefore(property.getPath(), "/j:members/"));
                if (group.isNodeType(Constants.JAHIANT_GROUP) && groups.add(group.getPath())) {
                    // recurse on the found group only we have not done it yet
                    populateGroups(groups, group);
                }
            }
        } catch (ItemNotFoundException e) {
            logger.warn("Cannot find group for " + principal.getPath(), e);
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

    private static final class GroupPathByGroupNameCacheKey implements Serializable {

        private static final long serialVersionUID = 6343178106505586294L;
        private final int hash;
        private String siteKey;
        private String groupName;

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

    private final class MembershipCacheEntryFactory implements CacheEntryFactory {

        @Override
        public Object createEntry(final Object key) throws Exception {
            try {
                return internalGetMembershipByPath((String) key);
            } catch (PathNotFoundException e) {
                // Non existing user/group
            } catch (RepositoryException e) {
                logger.error("Error retrieving membership for user " + (String) key + ", will return empty list", e);
            }
            return Collections.emptyList();
        }

        private Serializable internalGetMembershipByPath(String principalPath) throws RepositoryException {
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
            LinkedList<String> result = new LinkedList<>(groups);
            if (!principalNode.getProvider().isDefault()) {
                Element e = new Element(principalPath, result);
                e.setTimeToLive(membershipCacheTimeToLive);
                return e;
            } else {
                return result;
            }
        }

        @SuppressWarnings("unchecked")
        private void populateSpecialGroups(Set<String> groups, String groupPath) {
            groups.add(groupPath);
            groups.addAll((List<String>) getMembershipCache().get(groupPath).getObjectValue());
        }

    }
}
