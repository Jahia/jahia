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
package org.jahia.services.content.rules;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.files.FileCacheManager;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.Map;

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
            CacheFlushedEvent event = new CacheFlushedEvent(node.getPath(), eventMessage, startLevel, levelsUp);
            SpringContextSingleton.getInstance().publishEvent(event);
            FrameworkService.sendEvent(CacheFlushedEvent.TOPIC, event.toMap(), true);
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
        public static final String TOPIC = "org/jahia/services/content/rules/cacheFlushed";
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

        public Map<String, Object> toMap() {
            return Collections.singletonMap("event", this);
        }
    }
}
