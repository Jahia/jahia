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
package org.jahia.services.content.rules;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.files.FileCacheManager;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

import javax.jcr.RepositoryException;

/**
 * Background action that invalidates output caches for the node or its parents.
 *
 * @author Sergiy Shyrkov
 * @since JAHIA 6.6
 */
public class FlushCacheOnNodeBackgroundAction extends BaseBackgroundAction {

    private static Logger logger = LoggerFactory.getLogger(FlushCacheOnNodeBackgroundAction.class);

    private FileCacheManager fileCacheManager;
    private ModuleCacheProvider cacheProvider;
    private URLResolverFactory urlResolverFactory;

    private int startLevel;

    private int levelsUp;

    private String eventMessage;

    public FlushCacheOnNodeBackgroundAction() {
        fileCacheManager = FileCacheManager.getInstance();
    }

    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public void setUrlResolverFactory(URLResolverFactory urlResolverFactory) {
        this.urlResolverFactory = urlResolverFactory;
    }

    public void executeBackgroundAction(JCRNodeWrapper node) {
        String workspace = Constants.LIVE_WORKSPACE;
        boolean log = logger.isDebugEnabled();
        try {
            JCRNodeWrapper currentNode = node;
            workspace = node.getSession().getWorkspace().getName();
            for (int level = 0; level <= (startLevel + levelsUp); level++) {
                if (level >= startLevel) {
                    String path = currentNode.getPath();
                    cacheProvider.invalidate(path);
                    cacheProvider.invalidate(currentNode.getIdentifier());
                    if (log) {
                        logger.debug("Flushed output caches for node {}", path);
                    }
                    if (currentNode.isFile()) {
                        fileCacheManager.invalidate(workspace, path);
                        if (log) {
                            logger.debug("Flushed file cache for node {}", path);
                        }
                    }
                    cacheProvider.flushRegexpDependenciesOfPath(path, true);
                    if (log) {
                        logger.debug("Flushed regexp dependencies for node {}", path);
                    }
                    urlResolverFactory.flushCaches(path);
                    if (log) {
                        logger.debug("Flushed url resolver cache for node {}", path);
                    }
                }
                currentNode = currentNode.getParent();
            }
        } catch (RepositoryException e) {
            //Flush by path directly as node might not be visible anymore
            String currentNodePath = node.getPath();
            for (int level = 0; level <= (startLevel + levelsUp); level++) {
                if (level >= startLevel) {
                    cacheProvider.invalidate(currentNodePath);
                    fileCacheManager.invalidate(workspace, currentNodePath);
                    if (log) {
                        logger.debug("Flushed output and file caches for node {}", currentNodePath);
                    }
                    cacheProvider.flushRegexpDependenciesOfPath(currentNodePath, true);
                    if (log) {
                        logger.debug("Flushed regexp dependencies for node {}", currentNodePath);
                    }
                    urlResolverFactory.flushCaches(currentNodePath);
                    if (log) {
                        logger.debug("Flushed url resolver cache for node {}", currentNodePath);
                    }
                }
                currentNodePath = StringUtils.substringBeforeLast(currentNodePath,"/");
            }
        }
        if (eventMessage != null) {
            SpringContextSingleton.getInstance().publishEvent(new CacheFlushedEvent(node.getPath(), eventMessage, startLevel, levelsUp));
        }
    }

    public void setStartLevel(int startLevel) {
        this.startLevel = startLevel;
    }

    public void setLevelsUp(int endLevel) {
        this.levelsUp = endLevel;
    }

    public void setEventMessage(String eventMessage) {
        this.eventMessage = eventMessage;
    }

    public static class CacheFlushedEvent extends ApplicationEvent {
        private String reason;
        private int startLevel;
        private int levelsUp;

        public CacheFlushedEvent(String path, String reason, int startLevel, int levelsUp) {
            super(path);
            this.reason = reason;
            this.startLevel = startLevel;
            this.levelsUp = levelsUp;
        }

        public String getPath() {
            return (String) getSource();
        }

        public String getReason() {
            return reason;
        }

        public int getStartLevel() {
            return startLevel;
        }

        public int getLevelsUp() {
            return levelsUp;
        }
    }
}
