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
package org.jahia.services.render.filter.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.observation.EventImpl;
import org.jahia.services.content.*;
import org.jahia.services.query.QueryResultWrapper;
import org.jahia.services.seo.jcr.VanityUrlManager;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.query.Query;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Output cache invalidation listener.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 * Created : 12 janv. 2010
 */
public class HtmlCacheEventListener extends DefaultEventListener implements ExternalEventListener {
    private static Logger logger = LoggerFactory.getLogger(HtmlCacheEventListener.class);

    private ModuleCacheProvider cacheProvider;
    private AggregateCacheFilter aggregateCacheFilter;

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED + Event.NODE_MOVED + Event.NODE_REMOVED;
    }

    @Override
    public boolean isDeep() {
        return false;
    }

    @Override
    public String getPath() {
        return "(?!/jcr:system).*";
    }

    /**
     * This method is called when a bundle of events is dispatched.
     *
     * @param events The event set received.
     */
    public void onEvent(EventIterator events) {
        final int operationType = ((JCREventIterator) events).getOperationType();
        if (logger.isDebugEnabled()) {
            logger.debug("{} events received. Operation type {}", events.getSize(), operationType);
        }
        final Cache depCache = cacheProvider.getDependenciesCache();
        final Cache regexpDepCache = cacheProvider.getRegexpDependenciesCache();
        final Set<String> flushed = new HashSet<String>();

        AclCacheKeyPartGenerator cacheKeyGenerator = (AclCacheKeyPartGenerator) cacheProvider.getKeyGenerator().getPartGenerator("acls");
        final Set<String> userGroupsKeyToFlush = new HashSet<String>();

        while (events.hasNext()) {
            Event event = (Event) events.next();
            if (logger.isDebugEnabled()) {
                logger.debug("Event: {}", event);
            }
            boolean propagateToOtherClusterNodes = !isExternal(event);
            try {
                String path = event.getPath();
                boolean flushParent = false;
                boolean flushChilds = false;
                boolean flushForVanityUrl = false;
                if (path.contains("j:view")) {
                    flushParent = true;
                }
                final int type = event.getType();
                if (path.contains("j:invalidLanguages")) {
                    flushParent = true;
                }
                if (type == Event.PROPERTY_ADDED || type == Event.PROPERTY_CHANGED || type == Event.PROPERTY_REMOVED) {
                    if (path.endsWith("/j:published")) {
                        flushParent = true;
                    }
                    path = path.substring(0, path.lastIndexOf("/"));
                } else if (type == Event.NODE_ADDED || type == Event.NODE_MOVED || type == Event.NODE_REMOVED) {
                    flushParent = true;
                }
                if (path.contains(VanityUrlManager.VANITYURLMAPPINGS_NODE)) {
                    flushForVanityUrl = true;
                }
                final String siteKey = JCRContentUtils.getSiteKey(path);
                if (path.contains("j:acl") && !path.endsWith("j:acl")) {
                    // Flushing cache of acl key for users as a group or an acl has been updated
                    if (cacheKeyGenerator != null) {

                        String nodeName = StringUtils.substringAfterLast(path, "/");

                        String key = "";
                        if (nodeName.startsWith("GRANT_")) {
                            key = StringUtils.substringAfter(path, "GRANT_");
                        } else if (nodeName.startsWith("DENY_")) {
                            key = StringUtils.substringAfter(path, "DENY_");
                        } else if (nodeName.startsWith("REF")) {
                            final int g = nodeName.indexOf("_g_");
                            final int u = nodeName.indexOf("_u_");
                            if (g == nodeName.lastIndexOf("_g_") && u == nodeName.lastIndexOf("_u_")) {
                                key = nodeName.substring(Math.max(u + 1, g + 1));
                            }
                        } else {
                            logger.warn("Cannot parse ACL event for : " + nodeName);
                        }
                        if (key.startsWith("u_")) {
                            if(siteKey != null) {
                                userGroupsKeyToFlush.add("u:" + key.substring(2) + ":" + siteKey);
                            }
                            userGroupsKeyToFlush.add("u:" + key.substring(2));
                        } else if (key.startsWith("g_")) {
                            if(siteKey != null) {
                                userGroupsKeyToFlush.add("g:" + key.substring(2) + ":" + siteKey);
                            }
                            userGroupsKeyToFlush.add("g:" + key.substring(2));
                        } else {
                            userGroupsKeyToFlush.add(key);
                        }
                    }
                    flushParent = true;
                    flushChilds = true;
                }
                if (type == Event.NODE_MOVED) {
                    if (cacheKeyGenerator != null) {
                        final String fPath = path;
                        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null, new JCRCallback<Object>() {
                            @Override
                            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                final QueryManagerWrapper queryManager = session.getWorkspace().getQueryManager();
                                QueryResultWrapper result = queryManager.createQuery("select * from ['jnt:ace'] where isdescendantnode('" + JCRContentUtils.sqlEncode(fPath) + "/')", Query.JCR_SQL2).execute();
                                for (JCRNodeWrapper nodeWrapper : result.getNodes()) {
                                    String principal = nodeWrapper.getProperty("j:principal").getString();
                                    if(siteKey != null) {
                                        userGroupsKeyToFlush.add(principal + ":" + siteKey);
                                    }
                                    userGroupsKeyToFlush.add(principal);
                                }
                                return null;
                            }
                        });
                    }
                    flushParent = true;
                    flushChilds = true;
                }
                if (path.endsWith("/j:requiredPermissionNames")) {
                    // Flushing cache of acl key for users as a group or an acl has been updated
                    if (cacheKeyGenerator != null) {
                        cacheKeyGenerator.flushPermissionCacheEntry(StringUtils.substringBeforeLast(path, "/j:requiredPermissionNames"), propagateToOtherClusterNodes);
                    }
                }
                path = StringUtils.substringBeforeLast(StringUtils.substringBeforeLast(path, "/j:translation"), "/j:acl");
                flushDependenciesOfPath(depCache, flushed, path, propagateToOtherClusterNodes);
                try {
                    flushDependenciesOfPath(depCache, flushed, ((JCREventIterator) events).getSession().getNode(path).getIdentifier(), propagateToOtherClusterNodes);
                } catch (PathNotFoundException e) {
                    if (event instanceof EventImpl && (((EventImpl) event).getChildId() != null)) {
                        flushDependenciesOfPath(depCache, flushed, ((EventImpl) event).getChildId().toString(), propagateToOtherClusterNodes);
                    }
                }
                flushRegexpDependenciesOfPath(regexpDepCache, path, propagateToOtherClusterNodes);

                if (flushChilds) {
                    flushChildsDependenciesOfPath(depCache, path, propagateToOtherClusterNodes);
                }

                if (flushParent) {
                    path = StringUtils.substringBeforeLast(path, "/");
                    flushDependenciesOfPath(depCache, flushed, path, propagateToOtherClusterNodes);
                    try {
                        flushDependenciesOfPath(depCache, flushed, ((JCREventIterator) events).getSession().getNode(path).getIdentifier(), propagateToOtherClusterNodes);
                    } catch (PathNotFoundException e) {
                        if (event instanceof EventImpl && (((EventImpl) event).getParentId() != null)) {
                            flushDependenciesOfPath(depCache, flushed, ((EventImpl) event).getParentId().toString(),
                                    propagateToOtherClusterNodes);
                        }
                    }
                    flushRegexpDependenciesOfPath(regexpDepCache, path, propagateToOtherClusterNodes);
                }

                if (flushForVanityUrl) {
                    path = StringUtils.substringBeforeLast(path, "/" + VanityUrlManager.VANITYURLMAPPINGS_NODE);
                    flushDependenciesOfPath(depCache, flushed, path, propagateToOtherClusterNodes);
                    try {
                        flushDependenciesOfPath(depCache, flushed, ((JCREventIterator) events).getSession().getNode(path).getIdentifier(), propagateToOtherClusterNodes);
                    } catch (PathNotFoundException e) {
                        if (event instanceof EventImpl && (((EventImpl) event).getChildId() != null)) {
                            flushDependenciesOfPath(depCache, flushed, ((EventImpl) event).getChildId().toString(), propagateToOtherClusterNodes);
                        }
                    }
                }

                if (cacheKeyGenerator != null) {
                    if (userGroupsKeyToFlush.contains("")) {
                        cacheKeyGenerator.flushUsersGroupsKey(propagateToOtherClusterNodes);
                    } else {
                        for (String key : userGroupsKeyToFlush) {
                            cacheKeyGenerator.flushUsersGroupsKey(key, propagateToOtherClusterNodes);
                        }
                    }
                }

            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

    private void flushDependenciesOfPath(Cache depCache, Set<String> flushed, String path, boolean propagateToOtherClusterNodes) {
        Element element = !flushed.contains(path) ? depCache.get(path) : null;
        if (element != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Flushing dependencies for path: {}", path);
            }
            flushed.add(path);
            if (logger.isDebugEnabled()) {
                logger.debug("Flushing path: {}", path);
            }
            Set<String> deps = (Set<String>) element.getObjectValue();
            if (deps.contains("ALL")) {
                AggregateCacheFilter.flushNotCacheableFragment();
            } else {
                for (String dep : deps) {
                    aggregateCacheFilter.removeNotCacheableFragment(dep);
                }
            }
            cacheProvider.invalidate(path, propagateToOtherClusterNodes);
            depCache.remove(element.getObjectKey());
        }
        if (SettingsBean.getInstance().isClusterActivated()) {
            cacheProvider.propagatePathFlushToCluster(path, propagateToOtherClusterNodes);
        }
    }

    private void flushRegexpDependenciesOfPath(Cache depCache, String path, boolean propagateToOtherClusterNodes) {
        if (logger.isDebugEnabled()) {
            logger.debug("Flushing dependencies for path: {}", path);
        }
        @SuppressWarnings("unchecked")
        List<String> keys = depCache.getKeys();
        for (String key : keys) {
            if (path.matches(key)) {
                cacheProvider.invalidateRegexp(key, propagateToOtherClusterNodes);
            }
        }
        if (SettingsBean.getInstance().isClusterActivated()) {
            cacheProvider.propagateFlushRegexpDependenciesOfPath(path, propagateToOtherClusterNodes);
        }
    }

    private void flushChildsDependenciesOfPath(Cache depCache, String path, boolean propagateToOtherClusterNodes) {
        if (logger.isDebugEnabled()) {
            logger.debug("Flushing dependencies for path: {}", path);
        }
        @SuppressWarnings("unchecked")
        List<String> keys = depCache.getKeys();
        for (String key : keys) {
            if (key.startsWith(path)) {
                cacheProvider.invalidate(key, propagateToOtherClusterNodes);
            }
        }
        if (SettingsBean.getInstance().isClusterActivated()) {
            cacheProvider.propagateChildrenDependenciesFlushToCluster(path, propagateToOtherClusterNodes);
        }
    }

    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public void setAggregateCacheFilter(AggregateCacheFilter aggregateCacheFilter) {
        this.aggregateCacheFilter = aggregateCacheFilter;
    }
}
