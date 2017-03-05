/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.files.FileCacheManager;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private int startLevel;

    private int levelsUp;

    public FlushCacheOnNodeBackgroundAction() {
        fileCacheManager = FileCacheManager.getInstance();
    }

    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
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
                }
                currentNodePath = StringUtils.substringBeforeLast(currentNodePath,"/");
            }
        }
    }

    public void setStartLevel(int startLevel) {
        this.startLevel = startLevel;
    }

    public void setLevelsUp(int endLevel) {
        this.levelsUp = endLevel;
    }
}
