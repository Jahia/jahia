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
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.usermanager;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.RowIterator;

import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRUserNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Group manager cache helper.
 * 
 * @author Sergiy Shyrkov
 */
public class GroupCacheHelper {
    
    class GroupPathByGroupNameCacheEntryFactory implements CacheEntryFactory {
        @Override
        public Object createEntry(final Object key) throws Exception {
            final GroupPathByGroupNameCacheKey c = (GroupPathByGroupNameCacheKey) key;
            String path = internalGetGroupPath(c.siteKey, c.groupName);
            if (path != null) {
                return new Element(key, path);
            } else {
                return new Element(key, null, 0, timeToLiveForEmptyPath);
            }
        }
    }

    private static class GroupPathByGroupNameCacheKey implements Serializable {
        private static final long serialVersionUID = 6343178106505586294L;
        String groupName;
        final int hash;
        String siteKey;

        private GroupPathByGroupNameCacheKey(String siteKey, String groupName) {
            this.siteKey = siteKey;
            this.groupName = groupName;
            this.hash = getHashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            GroupPathByGroupNameCacheKey that = (GroupPathByGroupNameCacheKey) o;

            if (!groupName.equals(that.groupName))
                return false;
            if (siteKey != null ? !siteKey.equals(that.siteKey) : that.siteKey != null)
                return false;

            return true;
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

    class MembershipCacheEntryFactory implements CacheEntryFactory {
        @Override
        public Object createEntry(final Object key) throws Exception {
            return internalGetMembershipByPath((String) key);
        }
    }

    private static Logger logger = LoggerFactory.getLogger(GroupCacheHelper.class);

    private EhCacheProvider ehCacheProvider;

    private SelfPopulatingCache groupPathByGroupNameCache;

    private SelfPopulatingCache membershipCache;

    private int timeToLiveForEmptyPath = 600;

    public String getGroupPath(String siteKey, String name) {
        final String value = (String) getGroupPathByGroupNameCache().get(new GroupPathByGroupNameCacheKey(siteKey, name)).getObjectValue();
        return value;
    }

    private SelfPopulatingCache getGroupPathByGroupNameCache() {
        if (groupPathByGroupNameCache == null) {
            groupPathByGroupNameCache = ehCacheProvider.registerSelfPopulatingCache("org.jahia.services.usermanager.JahiaGroupManagerService.groupPathByGroupNameCache", new GroupPathByGroupNameCacheEntryFactory());
        }
        return groupPathByGroupNameCache;
    }

    SelfPopulatingCache getMembershipCache() {
        if (membershipCache == null) {
            membershipCache = ehCacheProvider.registerSelfPopulatingCache(
                    "org.jahia.services.usermanager.JahiaGroupManagerService.membershipCache",
                    new MembershipCacheEntryFactory());
        }
        return membershipCache;
    }

    private GroupPathByGroupNameCacheKey getPathCacheKey(String groupPath) {
        return new GroupPathByGroupNameCacheKey(groupPath.startsWith("/sites/") ? StringUtils.substringBetween(
                groupPath, "/sites/", "/") : null, StringUtils.substringAfterLast(groupPath, "/"));
    }

    public String internalGetGroupPath(String siteKey, String name) throws RepositoryException {
        StringBuilder buff = new StringBuilder(192);
        buff.append("SELECT [j:nodename] FROM [" + Constants.JAHIANT_GROUP + "] as group where localname()='")
                .append(name).append("' ");
        if (siteKey != null) {
            buff.append("and isdescendantnode(group,'/sites/").append(siteKey).append("/groups')");
        } else {
            buff.append("and isdescendantnode(group,'/groups')");
        }

        Query q = JCRSessionFactory.getInstance().getCurrentSystemSession("live", null, null).getWorkspace()
                .getQueryManager().createQuery(buff.toString(), Query.JCR_SQL2);
        q.setLimit(1);
        RowIterator it = q.execute().getRows();
        if (!it.hasNext()) {
            return null;
        }
        return it.nextRow().getPath();
    }

    private List<String> internalGetMembershipByPath(String principalPath) {
        try {
            JCRNodeWrapper principalNode = JCRSessionFactory.getInstance().getCurrentSystemSession("live", null, null)
                    .getNode(principalPath);
            Set<String> groups = new LinkedHashSet<String>();
            try {
                recurseOnGroups(groups, principalNode);
            } catch (JahiaException e) {
                logger.warn("Error retrieving membership for user " + principalPath, e);
            }
            if (principalNode instanceof JCRUserNode) {
                JCRUserNode userNode = (JCRUserNode) principalNode;
                if (!JahiaUserManagerService.isGuest(userNode)) {
                    recurseOnSpecialGroups(groups, JahiaGroupManagerService.USERS_GROUPPATH);
                    if (userNode.getRealm() != null) {
                        String siteUsers = "/sites/" + userNode.getRealm() + "/groups/" + JahiaGroupManagerService.SITE_USERS_GROUPNAME;
                        recurseOnSpecialGroups(groups, siteUsers);
                    }
                }
                recurseOnSpecialGroups(groups, JahiaGroupManagerService.GUEST_GROUPPATH);
            }
            return new LinkedList<String>(groups);
        } catch (PathNotFoundException e) {
            // Non existing user/group
        } catch (RepositoryException e) {
            logger.error("Error retrieving membership for user " + principalPath + ", will return empty list", e);
        }
        return null;
    }

    private void recurseOnSpecialGroups(Set<String> groups, String groupPath) {
        groups.add(groupPath);
        groups.addAll((List<String>) membershipCache.get(groupPath).getObjectValue());
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


    public void setEhCacheProvider(EhCacheProvider ehCacheProvider) {
        this.ehCacheProvider = ehCacheProvider;
    }

    public void setTimeToLiveForEmptyPath(int timeToLiveForEmptyPath) {
        this.timeToLiveForEmptyPath = timeToLiveForEmptyPath;
    }

    public void updatePathCacheAdded(String groupPath) {
        getGroupPathByGroupNameCache().put(new Element(getPathCacheKey(groupPath), groupPath));
    }

    public void updatePathCacheRemoved(String groupPath) {
        getGroupPathByGroupNameCache().remove(getPathCacheKey(groupPath));
    }
}
