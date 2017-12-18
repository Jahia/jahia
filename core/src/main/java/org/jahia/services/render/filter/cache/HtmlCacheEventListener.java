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
package org.jahia.services.render.filter.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.observation.EventState;
import org.apache.jackrabbit.core.state.NoSuchItemStateException;
import org.jahia.services.content.*;
import org.jahia.services.query.QueryResultWrapper;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.seo.jcr.VanityUrlManager;
import org.jahia.settings.SettingsBean;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.query.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Output cache invalidation listener.
 *
 * @author rincevent
 * @since JAHIA 6.5
 */
public class HtmlCacheEventListener extends DefaultEventListener implements ExternalEventListener {

    private static Logger logger = LoggerFactory.getLogger(HtmlCacheEventListener.class);

    private ModuleCacheProvider cacheProvider;
    private SchedulerService schedulerService;

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

    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
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

        boolean isExternal = false;
        List<Event> list = new ArrayList<>();
        while (events.hasNext()) {
            Event event = (Event) events.next();
            try {
                isExternal = isExternal || isExternal(event);
                list.add(isExternal ? new FlushEvent(event.getPath(), event.getIdentifier(), event.getType()) : event);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (isExternal) {
            try {
                JobDetail jobDetail = BackgroundJob.createJahiaJob("Cache flush", HtmlCacheEventJob.class);
                jobDetail.setDurability(false);
                JobDataMap jobDataMap = jobDetail.getJobDataMap();
                jobDataMap.put("events", list);

                schedulerService.scheduleJobNow(jobDetail, true);

            } catch (SchedulerException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            processEvents(list, ((JCREventIterator) events).getSession(), true);
        }
    }

    public void processEvents(List<Event> events, JCRSessionWrapper sessionWrapper, boolean propagateToOtherClusterNodes) {

        final Cache depCache = cacheProvider.getDependenciesCache();
        final Set<String> flushed = new HashSet<String>();

        AclCacheKeyPartGenerator cacheKeyGenerator = (AclCacheKeyPartGenerator) cacheProvider.getKeyGenerator().getPartGenerator("acls");
        final Set<String> userGroupsKeyToFlush = new HashSet<String>();
        for (Event event : events) {
            if (logger.isDebugEnabled()) {
                logger.debug("Event: {}", event);
            }
            try {
                String eventNodePath = event.getPath();
                String path = eventNodePath;
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
                    eventNodePath = path;
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
                            public Object doInJCR(JCRSessionWrapper systemSession) throws RepositoryException {
                                final QueryManagerWrapper queryManager = systemSession.getWorkspace().getQueryManager();
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
                if (!flushed.contains(path)) {
                    flushed.add(path);
                    flushDependenciesOfPath(depCache, path, propagateToOtherClusterNodes);
                    try {
                        // if the path is the original one, we have the UUID in the event, otherwise get it from JCR
                        String uuid = path == eventNodePath ? event.getIdentifier() : sessionWrapper.getNode(path).getIdentifier();
                        flushDependenciesOfPath(depCache, uuid, propagateToOtherClusterNodes);
                    } catch (PathNotFoundException e) {
                        //
                    } catch (RepositoryException e) {
                        if (e.getCause() == null || !(e.getCause() instanceof NoSuchItemStateException)) {
                            throw e;
                        }
                    }
                    cacheProvider.flushRegexpDependenciesOfPath(path, propagateToOtherClusterNodes);
                }

                if (flushChilds) {
                    cacheProvider.flushChildrenDependenciesOfPath(path, propagateToOtherClusterNodes);
                }

                if (flushParent) {
                    path = StringUtils.substringBeforeLast(path, "/");
                    if (!flushed.contains(path)) {
                        flushed.add(path);
                        flushDependenciesOfPath(depCache, path, propagateToOtherClusterNodes);
                        try {
                            flushDependenciesOfPath(depCache, sessionWrapper.getNode(path).getIdentifier(), propagateToOtherClusterNodes);
                        } catch (PathNotFoundException e) {
                            //
                        } catch (RepositoryException e) {
                            if (e.getCause() == null || !(e.getCause() instanceof NoSuchItemStateException)) {
                                throw e;
                            }
                        }
                        cacheProvider.flushRegexpDependenciesOfPath(path, propagateToOtherClusterNodes);
                    }
                }

                if (flushForVanityUrl) {
                    path = StringUtils.substringBeforeLast(path, "/" + VanityUrlManager.VANITYURLMAPPINGS_NODE);
                    if (!flushed.contains(path)) {
                        flushed.add(path);
                        flushDependenciesOfPath(depCache, path, propagateToOtherClusterNodes);
                        try {
                            flushDependenciesOfPath(depCache, sessionWrapper.getNode(path).getIdentifier(), propagateToOtherClusterNodes);
                        } catch (PathNotFoundException e) {
                            //
                        } catch (RepositoryException e) {
                            if (e.getCause() == null || !(e.getCause() instanceof NoSuchItemStateException)) {
                                throw e;
                            }
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

    private void flushDependenciesOfPath(Cache depCache, String path, boolean propagateToOtherClusterNodes) {

        Element element = depCache.get(path);
        if (element != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Flushing dependencies for path: {}", path);
            }
            @SuppressWarnings("unchecked")
            Set<String> deps = (Set<String>) element.getObjectValue();
            if (deps.contains("ALL")) {
                cacheProvider.flushNonCacheableFragments();
            } else {
                for (String dep : deps) {
                    cacheProvider.removeNonCacheableFragmentsByEncodedPath(dep);
                }
            }
            cacheProvider.invalidate(path, propagateToOtherClusterNodes);
            depCache.remove(element.getObjectKey());
        }

        if (propagateToOtherClusterNodes && SettingsBean.getInstance().isClusterActivated()) {
            cacheProvider.propagatePathFlushToCluster(path);
        }
    }

    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public static class FlushEvent implements Event, Serializable {

        private static final long serialVersionUID = -4835978210219006748L;
        private String path;
        private String id;
        private int type;

        public FlushEvent(String path, String id, int type) {
            this.path = path;
            this.id = id;
            this.type = type;
        }

        @Override
        public String getUserID() {
            return null;
        }

        @Override
        public String getIdentifier() throws RepositoryException {
            return id;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Map getInfo() throws RepositoryException {
            return null;
        }

        @Override
        public String getUserData() throws RepositoryException {
            return null;
        }

        @Override
        public long getDate() throws RepositoryException {
            return 0;
        }

        @Override
        public int getType() {
            return type;
        }

        @Override
        public String getPath() throws RepositoryException {
            return path;
        }

        /**
         * @return a String representation of this <code>Event</code>.
         */
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Event: Path: ").append(path).append(", ID: ").append(id).append(", Type: ").append(EventState.valueOf(getType()));
            return sb.toString();
        }
    }
}
