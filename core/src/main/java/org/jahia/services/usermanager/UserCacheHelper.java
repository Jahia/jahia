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

import javax.jcr.RepositoryException;
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
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.query.QueryWrapper;
import org.jahia.services.sites.JahiaSitesService;

/**
 * User path by user name cache helper.
 *
 * @author Sergiy Shyrkov
 */
public class UserCacheHelper {

    private static final int DEFAULT_TTL_FOR_NON_EXISTING_USERS = 600;

    private EhCacheProvider ehCacheProvider;
    private volatile SelfPopulatingCache userPathByUserNameCache;
    private int timeToLiveForNonExistingUsers = DEFAULT_TTL_FOR_NON_EXISTING_USERS;

    private final class UserPathByUserNameCacheEntryFactory implements CacheEntryFactory {

        @Override
        public Object createEntry(final Object key) throws Exception {
            UserPathCacheKey k = (UserPathCacheKey) key;
            String path = internalGetUserPath(k.user, k.site);
            if (path != null) {
                return new Element(key, path);
            } else {
                return new Element(key, StringUtils.EMPTY, 0, timeToLiveForNonExistingUsers);
            }
        }

        private String internalGetUserPath(String name, String siteName) throws RepositoryException {
            StringBuilder statement = new StringBuilder();
            statement.append("SELECT [j:nodename] from [jnt:user] where localname()='");
            statement.append(JCRContentUtils.sqlEncode(name));
            statement.append("' and isdescendantnode('");
            if (siteName != null) {
                statement.append(JahiaSitesService.SITES_JCR_PATH);
                statement.append('/');
                statement.append(JCRContentUtils.sqlEncode(siteName));
            }
            statement.append("/users/')");

            String path = queryUserPathInWorkspace(statement, Constants.LIVE_WORKSPACE);
            if (path == null) {
                path = queryUserPathInWorkspace(statement, null);
            }
            return path;
        }

        private String queryUserPathInWorkspace(StringBuilder q, String workspace) throws RepositoryException {
            final QueryWrapper query = JCRSessionFactory
                    .getInstance()
                    .getCurrentSystemSession(workspace, null, null)
                    .getWorkspace()
                    .getQueryManager()
                    .createQuery(q.toString(), Query.JCR_SQL2);
            query.setLimit(1);

            RowIterator it = query.execute().getRows();
            if (it.hasNext()) {
                return it.nextRow().getPath();
            }

            return null;
        }

    }

    private static final class UserPathCacheKey implements Serializable {

        private static final long serialVersionUID = -727853070149556455L;

        private final int hash;
        private final String site;
        private final String user;

        private UserPathCacheKey(String user, String site) {
            super();
            this.user = user;
            this.site = site;
            this.hash = getHashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (null == other || other.getClass() != this.getClass()) {
                return false;
            }

            UserPathCacheKey otherKey = (UserPathCacheKey) other;

            return StringUtils.equals(this.user, otherKey.user) && StringUtils.equals(this.site, otherKey.site);
        }

        private final int getHashCode() {
            int iTotal = 17;
            iTotal = 37 * iTotal + (user != null ? user.hashCode() : 0);
            iTotal = 37 * iTotal + (site != null ? site.hashCode() : 0);
            return iTotal;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    public String getUserPath(String name, String site) {
        final String value = (String) getUserPathByUserNameCache().get(
                new UserPathCacheKey(name, StringUtils.isEmpty(site) ? null : site))
                .getObjectValue();
        if (value.equals(StringUtils.EMPTY)) {
            return null;
        }
        return value;
    }

    private SelfPopulatingCache getUserPathByUserNameCache() {
        // Thread-safe lazy loading, using double-checked locking pattern
        // see https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        SelfPopulatingCache cache = userPathByUserNameCache;
        if (cache == null) {
            synchronized (this) {
                cache = userPathByUserNameCache;
                if (cache == null) {
                    userPathByUserNameCache = cache = ehCacheProvider.registerSelfPopulatingCache(
                            "org.jahia.services.usermanager.JahiaUserManagerService.userPathByUserNameCache",
                            new Searchable(), new UserPathByUserNameCacheEntryFactory()
                    );
                }
            }
        }
        return cache;
    }

    public void setEhCacheProvider(EhCacheProvider ehCacheProvider) {
        this.ehCacheProvider = ehCacheProvider;
    }

    public void setTimeToLiveForNonExistingUsers(int timeToLiveForNonExistingUsers) {
        this.timeToLiveForNonExistingUsers = timeToLiveForNonExistingUsers;
    }

    public void updateAdded(String userPath) {
        getUserPathByUserNameCache().put(
                new Element(new UserPathCacheKey(StringUtils.substringAfterLast(userPath, "/"), getSiteKey(userPath)),
                        userPath));
    }

    public void updateRemoved(String userPath) {
        getUserPathByUserNameCache().remove(
                new UserPathCacheKey(StringUtils.substringAfterLast(userPath, "/"), getSiteKey(userPath)));
    }

    private String getSiteKey(String userPath) {
        return userPath.startsWith("/sites/") ? StringUtils.substringBetween(userPath, "/sites/", "/") : null;
    }

    public void clearNonExistingUsersCache() {
        SelfPopulatingCache cache = getUserPathByUserNameCache();
        Results results = cache.createQuery().addCriteria(new EqualTo("value", "")).includeKeys().execute();
        for (Result result : results.all()) {
            cache.remove(result.getKey());
        }
    }
}
